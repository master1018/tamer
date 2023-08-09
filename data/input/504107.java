public class WorkingMessage {
    private static final String TAG = "WorkingMessage";
    private static final boolean DEBUG = false;
    public static final String ACTION_SENDING_SMS = "android.intent.action.SENDING_SMS";
    public static final String EXTRA_SMS_MESSAGE = "android.mms.extra.MESSAGE";
    public static final String EXTRA_SMS_RECIPIENTS = "android.mms.extra.RECIPIENTS";
    public static final String EXTRA_SMS_THREAD_ID = "android.mms.extra.THREAD_ID";
    private final Context mContext;
    private final ContentResolver mContentResolver;
    private static final int RECIPIENTS_REQUIRE_MMS = (1 << 0);     
    private static final int HAS_SUBJECT = (1 << 1);                
    private static final int HAS_ATTACHMENT = (1 << 2);             
    private static final int LENGTH_REQUIRES_MMS = (1 << 3);        
    private static final int FORCE_MMS = (1 << 4);                  
    private int mMmsState;
    public static final int OK = 0;
    public static final int UNKNOWN_ERROR = -1;
    public static final int MESSAGE_SIZE_EXCEEDED = -2;
    public static final int UNSUPPORTED_TYPE = -3;
    public static final int IMAGE_TOO_LARGE = -4;
    public static final int TEXT = 0;
    public static final int IMAGE = 1;
    public static final int VIDEO = 2;
    public static final int AUDIO = 3;
    public static final int SLIDESHOW = 4;
    private int mAttachmentType;
    private Conversation mConversation;
    private CharSequence mText;
    private SlideshowModel mSlideshow;
    private Uri mMessageUri;
    private CharSequence mSubject;
    private boolean mDiscarded = false;
    private static boolean sMmsEnabled = MmsConfig.getMmsEnabled();
    private final MessageStatusListener mStatusListener;
    private List<String> mWorkingRecipients;
    private static final String[] MMS_OUTBOX_PROJECTION = {
        Mms._ID,            
        Mms.MESSAGE_SIZE    
    };
    private static final int MMS_MESSAGE_SIZE_INDEX  = 1;
    public interface MessageStatusListener {
        void onProtocolChanged(boolean mms);
        void onAttachmentChanged();
        void onPreMessageSent();
        void onMessageSent();
        void onMaxPendingMessagesReached();
        void onAttachmentError(int error);
    }
    private WorkingMessage(ComposeMessageActivity activity) {
        mContext = activity;
        mContentResolver = mContext.getContentResolver();
        mStatusListener = activity;
        mAttachmentType = TEXT;
        mText = "";
    }
    public static WorkingMessage createEmpty(ComposeMessageActivity activity) {
        WorkingMessage msg = new WorkingMessage(activity);
        return msg;
    }
    public static WorkingMessage load(ComposeMessageActivity activity, Uri uri) {
        if (!uri.toString().startsWith(Mms.Draft.CONTENT_URI.toString())) {
            PduPersister persister = PduPersister.getPduPersister(activity);
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                LogTag.debug("load: moving %s to drafts", uri);
            }
            try {
                uri = persister.move(uri, Mms.Draft.CONTENT_URI);
            } catch (MmsException e) {
                LogTag.error("Can't move %s to drafts", uri);
                return null;
            }
        }
        WorkingMessage msg = new WorkingMessage(activity);
        if (msg.loadFromUri(uri)) {
            return msg;
        }
        return null;
    }
    private void correctAttachmentState() {
        int slideCount = mSlideshow.size();
        if (slideCount == 0) {
            mAttachmentType = TEXT;
            mSlideshow = null;
            asyncDelete(mMessageUri, null, null);
            mMessageUri = null;
        } else if (slideCount > 1) {
            mAttachmentType = SLIDESHOW;
        } else {
            SlideModel slide = mSlideshow.get(0);
            if (slide.hasImage()) {
                mAttachmentType = IMAGE;
            } else if (slide.hasVideo()) {
                mAttachmentType = VIDEO;
            } else if (slide.hasAudio()) {
                mAttachmentType = AUDIO;
            }
        }
        updateState(HAS_ATTACHMENT, hasAttachment(), false);
    }
    private boolean loadFromUri(Uri uri) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("loadFromUri %s", uri);
        try {
            mSlideshow = SlideshowModel.createFromMessageUri(mContext, uri);
        } catch (MmsException e) {
            LogTag.error("Couldn't load URI %s", uri);
            return false;
        }
        mMessageUri = uri;
        syncTextFromSlideshow();
        correctAttachmentState();
        return true;
    }
    public static WorkingMessage loadDraft(ComposeMessageActivity activity,
                                           Conversation conv) {
        WorkingMessage msg = new WorkingMessage(activity);
        if (msg.loadFromConversation(conv)) {
            return msg;
        } else {
            return createEmpty(activity);
        }
    }
    private boolean loadFromConversation(Conversation conv) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("loadFromConversation %s", conv);
        long threadId = conv.getThreadId();
        if (threadId <= 0) {
            return false;
        }
        mText = readDraftSmsMessage(conv);
        if (!TextUtils.isEmpty(mText)) {
            return true;
        }
        StringBuilder sb = new StringBuilder();
        Uri uri = readDraftMmsMessage(mContext, threadId, sb);
        if (uri != null) {
            if (loadFromUri(uri)) {
                if (sb.length() > 0) {
                    setSubject(sb.toString(), false);
                }
                return true;
            }
        }
        return false;
    }
    public void setText(CharSequence s) {
        mText = s;
    }
    public CharSequence getText() {
        return mText;
    }
    public boolean hasText() {
        return mText != null && TextUtils.getTrimmedLength(mText) > 0;
    }
    public int setAttachment(int type, Uri dataUri, boolean append) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("setAttachment type=%d uri %s", type, dataUri);
        }
        int result = OK;
        ensureSlideshow();
        try {
            if (append) {
                appendMedia(type, dataUri);
            } else {
                changeMedia(type, dataUri);
            }
        } catch (MmsException e) {
            result = UNKNOWN_ERROR;
        } catch (UnsupportContentTypeException e) {
            result = UNSUPPORTED_TYPE;
        } catch (ExceedMessageSizeException e) {
            result = MESSAGE_SIZE_EXCEEDED;
        } catch (ResolutionException e) {
            result = IMAGE_TOO_LARGE;
        }
        if (result == OK) {
            mAttachmentType = type;
            mStatusListener.onAttachmentChanged();
        } else if (append) {
            SlideshowEditor slideShowEditor = new SlideshowEditor(mContext, mSlideshow);
            slideShowEditor.removeSlide(mSlideshow.size() - 1);
        }
        updateState(HAS_ATTACHMENT, hasAttachment(), true);
        correctAttachmentState();
        return result;
    }
    public boolean isWorthSaving() {
        if (hasText() || hasSubject() || hasAttachment() || hasSlideshow()) {
            return true;
        }
        if (isFakeMmsForDraft()) {
            return true;
        }
        return false;
    }
    public boolean isFakeMmsForDraft() {
        return (mMmsState & FORCE_MMS) > 0;
    }
    private void ensureSlideshow() {
        if (mSlideshow != null) {
            return;
        }
        SlideshowModel slideshow = SlideshowModel.createNew(mContext);
        SlideModel slide = new SlideModel(slideshow);
        slideshow.add(slide);
        mSlideshow = slideshow;
    }
    private void changeMedia(int type, Uri uri) throws MmsException {
        SlideModel slide = mSlideshow.get(0);
        MediaModel media;
        if (slide == null) {
            Log.w(LogTag.TAG, "[WorkingMessage] changeMedia: no slides!");
            return;
        }
        slide.removeImage();
        slide.removeVideo();
        slide.removeAudio();
        if (type == TEXT) {
            return;
        }
        if (type == IMAGE) {
            media = new ImageModel(mContext, uri, mSlideshow.getLayout().getImageRegion());
        } else if (type == VIDEO) {
            media = new VideoModel(mContext, uri, mSlideshow.getLayout().getImageRegion());
        } else if (type == AUDIO) {
            media = new AudioModel(mContext, uri);
        } else {
            throw new IllegalArgumentException("changeMedia type=" + type + ", uri=" + uri);
        }
        slide.add(media);
        if (type == VIDEO || type == AUDIO) {
            slide.updateDuration(media.getDuration());
        }
    }
    private void appendMedia(int type, Uri uri) throws MmsException {
        if (type == TEXT) {
            return;
        }
        boolean addNewSlide = true;
        if (mSlideshow.size() == 1 && !mSlideshow.isSimple()) {
            addNewSlide = false;
        }
        if (addNewSlide) {
            SlideshowEditor slideShowEditor = new SlideshowEditor(mContext, mSlideshow);
            if (!slideShowEditor.addNewSlide()) {
                return;
            }
        }
        MediaModel media;
        SlideModel slide = mSlideshow.get(mSlideshow.size() - 1);
        if (type == IMAGE) {
            media = new ImageModel(mContext, uri, mSlideshow.getLayout().getImageRegion());
        } else if (type == VIDEO) {
            media = new VideoModel(mContext, uri, mSlideshow.getLayout().getImageRegion());
        } else if (type == AUDIO) {
            media = new AudioModel(mContext, uri);
        } else {
            throw new IllegalArgumentException("changeMedia type=" + type + ", uri=" + uri);
        }
        slide.add(media);
        if (type == VIDEO || type == AUDIO) {
            slide.updateDuration(media.getDuration());
        }
    }
    public boolean hasAttachment() {
        return (mAttachmentType > TEXT);
    }
    public SlideshowModel getSlideshow() {
        return mSlideshow;
    }
    public boolean hasSlideshow() {
        return (mAttachmentType == SLIDESHOW);
    }
    public void setSubject(CharSequence s, boolean notify) {
        mSubject = s;
        updateState(HAS_SUBJECT, (s != null), notify);
    }
    public CharSequence getSubject() {
        return mSubject;
    }
    public boolean hasSubject() {
        return mSubject != null && TextUtils.getTrimmedLength(mSubject) > 0;
    }
    private void syncTextToSlideshow() {
        if (mSlideshow == null || mSlideshow.size() != 1)
            return;
        SlideModel slide = mSlideshow.get(0);
        TextModel text;
        if (!slide.hasText()) {
            text = new TextModel(mContext, ContentType.TEXT_PLAIN, "text_0.txt",
                                           mSlideshow.getLayout().getTextRegion());
            slide.add(text);
        } else {
            text = slide.getText();
        }
        text.setText(mText);
    }
    private void syncTextFromSlideshow() {
        if (mSlideshow.size() != 1) {
            return;
        }
        SlideModel slide = mSlideshow.get(0);
        if (slide == null || !slide.hasText()) {
            return;
        }
        mText = slide.getText().getText();
    }
    private void removeSubjectIfEmpty(boolean notify) {
        if (!hasSubject()) {
            setSubject(null, notify);
        }
    }
    private void prepareForSave(boolean notify) {
        syncWorkingRecipients();
        if (requiresMms()) {
            ensureSlideshow();
            syncTextToSlideshow();
            removeSubjectIfEmpty(notify);
        }
    }
    public void syncWorkingRecipients() {
        if (mWorkingRecipients != null) {
            ContactList recipients = ContactList.getByNumbers(mWorkingRecipients, false);
            mConversation.setRecipients(recipients);
            mWorkingRecipients = null;
        }
    }
    public void removeFakeMmsForDraft() {
        updateState(FORCE_MMS, false, false);
    }
    public Uri saveAsMms(boolean notify) {
        if (DEBUG) LogTag.debug("save mConversation=%s", mConversation);
        if (mDiscarded) {
            throw new IllegalStateException("save() called after discard()");
        }
        updateState(FORCE_MMS, true, notify);
        prepareForSave(true );
        mConversation.ensureThreadId();
        mConversation.setDraftState(true);
        PduPersister persister = PduPersister.getPduPersister(mContext);
        SendReq sendReq = makeSendReq(mConversation, mSubject);
        if (mMessageUri == null) {
            mMessageUri = createDraftMmsMessage(persister, sendReq, mSlideshow);
        } else {
            updateDraftMmsMessage(mMessageUri, persister, mSlideshow, sendReq);
        }
        return mMessageUri;
    }
    public void saveDraft() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("saveDraft");
        }
        if (mDiscarded) {
            return;
        }
        if (mConversation == null) {
            throw new IllegalStateException("saveDraft() called with no conversation");
        }
        prepareForSave(false );
        if (requiresMms()) {
            asyncUpdateDraftMmsMessage(mConversation);
        } else {
            String content = mText.toString();
            if (!TextUtils.isEmpty(content)) {
                asyncUpdateDraftSmsMessage(mConversation, content);
            }
        }
        mConversation.setDraftState(true);
    }
    synchronized public void discard() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("discard");
        }
        if (mDiscarded == true) {
            return;
        }
        mDiscarded = true;
        if (mMessageUri != null) {
            asyncDelete(mMessageUri, null, null);
        }
        clearConversation(mConversation);
    }
    public void unDiscard() {
        if (DEBUG) LogTag.debug("unDiscard");
        mDiscarded = false;
    }
    public boolean isDiscarded() {
        return mDiscarded;
    }
    public void writeStateToBundle(Bundle bundle) {
        if (hasSubject()) {
            bundle.putString("subject", mSubject.toString());
        }
        if (mMessageUri != null) {
            bundle.putParcelable("msg_uri", mMessageUri);
        } else if (hasText()) {
            bundle.putString("sms_body", mText.toString());
        }
    }
    public void readStateFromBundle(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        String subject = bundle.getString("subject");
        setSubject(subject, false);
        Uri uri = (Uri)bundle.getParcelable("msg_uri");
        if (uri != null) {
            loadFromUri(uri);
            return;
        } else {
            String body = bundle.getString("sms_body");
            mText = body;
        }
    }
    public void setWorkingRecipients(List<String> numbers) {
        mWorkingRecipients = numbers;
    }
    public void setConversation(Conversation conv) {
        if (DEBUG) LogTag.debug("setConversation %s -> %s", mConversation, conv);
        mConversation = conv;
        setHasEmail(conv.getRecipients().containsEmail(), false);
    }
    public void setHasEmail(boolean hasEmail, boolean notify) {
        if (MmsConfig.getEmailGateway() != null) {
            updateState(RECIPIENTS_REQUIRE_MMS, false, notify);
        } else {
            updateState(RECIPIENTS_REQUIRE_MMS, hasEmail, notify);
        }
    }
    public boolean requiresMms() {
        return (mMmsState > 0);
    }
    private static String stateString(int state) {
        if (state == 0)
            return "<none>";
        StringBuilder sb = new StringBuilder();
        if ((state & RECIPIENTS_REQUIRE_MMS) > 0)
            sb.append("RECIPIENTS_REQUIRE_MMS | ");
        if ((state & HAS_SUBJECT) > 0)
            sb.append("HAS_SUBJECT | ");
        if ((state & HAS_ATTACHMENT) > 0)
            sb.append("HAS_ATTACHMENT | ");
        if ((state & LENGTH_REQUIRES_MMS) > 0)
            sb.append("LENGTH_REQUIRES_MMS | ");
        if ((state & FORCE_MMS) > 0)
            sb.append("FORCE_MMS | ");
        sb.delete(sb.length() - 3, sb.length());
        return sb.toString();
    }
    private void updateState(int state, boolean on, boolean notify) {
        if (!sMmsEnabled) {
            return;
        }
        int oldState = mMmsState;
        if (on) {
            mMmsState |= state;
        } else {
            mMmsState &= ~state;
        }
        if (mMmsState == FORCE_MMS && ((oldState & ~FORCE_MMS) > 0)) {
            mMmsState = 0;
        }
        if (notify) {
            if (oldState == 0 && mMmsState != 0) {
                mStatusListener.onProtocolChanged(true);
            } else if (oldState != 0 && mMmsState == 0) {
                mStatusListener.onProtocolChanged(false);
            }
        }
        if (oldState != mMmsState) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) LogTag.debug("updateState: %s%s = %s",
                    on ? "+" : "-",
                    stateString(state), stateString(mMmsState));
        }
    }
    public void send() {
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            LogTag.debug("send");
        }
        prepareForSave(true );
        final Conversation conv = mConversation;
        String msgTxt = mText.toString();
        if (requiresMms() || addressContainsEmailToMms(conv, msgTxt)) {
            final Uri mmsUri = mMessageUri;
            final PduPersister persister = PduPersister.getPduPersister(mContext);
            final SlideshowModel slideshow = mSlideshow;
            final SendReq sendReq = makeSendReq(conv, mSubject);
            new Thread(new Runnable() {
                public void run() {
                    slideshow.prepareForSend();
                    sendMmsWorker(conv, mmsUri, persister, slideshow, sendReq);
                }
            }).start();
        } else {
            final String msgText = mText.toString();
            new Thread(new Runnable() {
                public void run() {
                    preSendSmsWorker(conv, msgText);
                }
            }).start();
        }
        RecipientIdCache.updateNumbers(conv.getThreadId(), conv.getRecipients());
        mDiscarded = true;
    }
    private boolean addressContainsEmailToMms(Conversation conv, String text) {
        if (MmsConfig.getEmailGateway() != null) {
            String[] dests = conv.getRecipients().getNumbers();
            int length = dests.length;
            for (int i = 0; i < length; i++) {
                if (Mms.isEmailAddress(dests[i]) || MessageUtils.isAlias(dests[i])) {
                    String mtext = dests[i] + " " + text;
                    int[] params = SmsMessage.calculateLength(mtext, false);
                    if (params[0] > 1) {
                        updateState(RECIPIENTS_REQUIRE_MMS, true, true);
                        ensureSlideshow();
                        syncTextToSlideshow();
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private void preSendSmsWorker(Conversation conv, String msgText) {
        UserHappinessSignals.userAcceptedImeText(mContext);
        mStatusListener.onPreMessageSent();
        long threadId = conv.ensureThreadId();
        final String semiSepRecipients = conv.getRecipients().serialize();
        sendSmsWorker(msgText, semiSepRecipients, threadId);
        deleteDraftSmsMessage(threadId);
    }
    private void sendSmsWorker(String msgText, String semiSepRecipients, long threadId) {
        String[] dests = TextUtils.split(semiSepRecipients, ";");
        if (Log.isLoggable(LogTag.TRANSACTION, Log.VERBOSE)) {
            LogTag.debug("sendSmsWorker sending message");
        }
        MessageSender sender = new SmsMessageSender(mContext, dests, msgText, threadId);
        try {
            sender.sendMessage(threadId);
            Recycler.getSmsRecycler().deleteOldMessagesByThreadId(mContext, threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS message, threadId=" + threadId, e);
        }
        mStatusListener.onMessageSent();
    }
    private void sendMmsWorker(Conversation conv, Uri mmsUri, PduPersister persister,
                               SlideshowModel slideshow, SendReq sendReq) {
        UserHappinessSignals.userAcceptedImeText(mContext);
        Cursor cursor = null;
        try {
            cursor = SqliteWrapper.query(mContext, mContentResolver,
                    Mms.Outbox.CONTENT_URI, MMS_OUTBOX_PROJECTION, null, null, null);
            if (cursor != null) {
                long maxMessageSize = MmsConfig.getMaxSizeScaleForPendingMmsAllowed() *
                    MmsConfig.getMaxMessageSize();
                long totalPendingSize = 0;
                while (cursor.moveToNext()) {
                    totalPendingSize += cursor.getLong(MMS_MESSAGE_SIZE_INDEX);
                }
                if (totalPendingSize >= maxMessageSize) {
                    unDiscard();    
                    mStatusListener.onMaxPendingMessagesReached();
                    return;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        mStatusListener.onPreMessageSent();
        long threadId = conv.ensureThreadId();
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("sendMmsWorker: update draft MMS message " + mmsUri);
        }
        if (mmsUri == null) {
            mmsUri = createDraftMmsMessage(persister, sendReq, slideshow);
        } else {
            updateDraftMmsMessage(mmsUri, persister, slideshow, sendReq);
        }
        deleteDraftSmsMessage(threadId);
        int error = 0;
        try {
            slideshow.finalResize(mmsUri);
        } catch (ExceedMessageSizeException e1) {
            error = MESSAGE_SIZE_EXCEEDED;
        } catch (MmsException e1) {
            error = UNKNOWN_ERROR;
        }
        if (error != 0) {
            markMmsMessageWithError(mmsUri);
            mStatusListener.onAttachmentError(error);
            return;
        }
        MessageSender sender = new MmsMessageSender(mContext, mmsUri,
                slideshow.getCurrentMessageSize());
        try {
            if (!sender.sendMessage(threadId)) {
                SqliteWrapper.delete(mContext, mContentResolver, mmsUri, null, null);
            }
            Recycler.getMmsRecycler().deleteOldMessagesByThreadId(mContext, threadId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send message: " + mmsUri + ", threadId=" + threadId, e);
        }
        mStatusListener.onMessageSent();
    }
    private void markMmsMessageWithError(Uri mmsUri) {
        try {
            PduPersister p = PduPersister.getPduPersister(mContext);
            p.move(mmsUri, Mms.Outbox.CONTENT_URI);
            ContentValues values = new ContentValues(1);
            values.put(PendingMessages.ERROR_TYPE, MmsSms.ERR_TYPE_GENERIC_PERMANENT);
            long msgId = ContentUris.parseId(mmsUri);
            SqliteWrapper.update(mContext, mContentResolver,
                    PendingMessages.CONTENT_URI,
                    values, PendingMessages._ID + "=" + msgId, null);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to move message to outbox and mark as error: " + mmsUri, e);
        }
    }
    private static final String[] MMS_DRAFT_PROJECTION = {
        Mms._ID,                
        Mms.SUBJECT,            
        Mms.SUBJECT_CHARSET     
    };
    private static final int MMS_ID_INDEX         = 0;
    private static final int MMS_SUBJECT_INDEX    = 1;
    private static final int MMS_SUBJECT_CS_INDEX = 2;
    private static Uri readDraftMmsMessage(Context context, long threadId, StringBuilder sb) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("readDraftMmsMessage tid=%d", threadId);
        }
        Cursor cursor;
        ContentResolver cr = context.getContentResolver();
        final String selection = Mms.THREAD_ID + " = " + threadId;
        cursor = SqliteWrapper.query(context, cr,
                Mms.Draft.CONTENT_URI, MMS_DRAFT_PROJECTION,
                selection, null, null);
        Uri uri;
        try {
            if (cursor.moveToFirst()) {
                uri = ContentUris.withAppendedId(Mms.Draft.CONTENT_URI,
                        cursor.getLong(MMS_ID_INDEX));
                String subject = MessageUtils.extractEncStrFromCursor(cursor, MMS_SUBJECT_INDEX,
                        MMS_SUBJECT_CS_INDEX);
                if (subject != null) {
                    sb.append(subject);
                }
                return uri;
            }
        } finally {
            cursor.close();
        }
        return null;
    }
    private static SendReq makeSendReq(Conversation conv, CharSequence subject) {
        String[] dests = conv.getRecipients().getNumbers(true );
        SendReq req = new SendReq();
        EncodedStringValue[] encodedNumbers = EncodedStringValue.encodeStrings(dests);
        if (encodedNumbers != null) {
            req.setTo(encodedNumbers);
        }
        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject.toString()));
        }
        req.setDate(System.currentTimeMillis() / 1000L);
        return req;
    }
    private static Uri createDraftMmsMessage(PduPersister persister, SendReq sendReq,
            SlideshowModel slideshow) {
        try {
            PduBody pb = slideshow.toPduBody();
            sendReq.setBody(pb);
            Uri res = persister.persist(sendReq, Mms.Draft.CONTENT_URI);
            slideshow.sync(pb);
            return res;
        } catch (MmsException e) {
            return null;
        }
    }
    private void asyncUpdateDraftMmsMessage(final Conversation conv) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("asyncUpdateDraftMmsMessage conv=%s mMessageUri=%s", conv, mMessageUri);
        }
        final PduPersister persister = PduPersister.getPduPersister(mContext);
        final SendReq sendReq = makeSendReq(conv, mSubject);
        new Thread(new Runnable() {
            public void run() {
                conv.ensureThreadId();
                conv.setDraftState(true);
                if (mMessageUri == null) {
                    mMessageUri = createDraftMmsMessage(persister, sendReq, mSlideshow);
                } else {
                    updateDraftMmsMessage(mMessageUri, persister, mSlideshow, sendReq);
                }
                asyncDeleteDraftSmsMessage(conv);
            }
        }).start();
    }
    private static void updateDraftMmsMessage(Uri uri, PduPersister persister,
            SlideshowModel slideshow, SendReq sendReq) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("updateDraftMmsMessage uri=%s", uri);
        }
        if (uri == null) {
            Log.e(TAG, "updateDraftMmsMessage null uri");
            return;
        }
        persister.updateHeaders(uri, sendReq);
        final PduBody pb = slideshow.toPduBody();
        try {
            persister.updateParts(uri, pb);
        } catch (MmsException e) {
            Log.e(TAG, "updateDraftMmsMessage: cannot update message " + uri);
        }
        slideshow.sync(pb);
    }
    private static final String SMS_DRAFT_WHERE = Sms.TYPE + "=" + Sms.MESSAGE_TYPE_DRAFT;
    private static final String[] SMS_BODY_PROJECTION = { Sms.BODY };
    private static final int SMS_BODY_INDEX = 0;
    private String readDraftSmsMessage(Conversation conv) {
        long thread_id = conv.getThreadId();
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("readDraftSmsMessage tid=%d", thread_id);
        }
        if (thread_id <= 0 || !conv.hasDraft()) {
            return "";
        }
        Uri thread_uri = ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, thread_id);
        String body = "";
        Cursor c = SqliteWrapper.query(mContext, mContentResolver,
                        thread_uri, SMS_BODY_PROJECTION, SMS_DRAFT_WHERE, null, null);
        boolean haveDraft = false;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    body = c.getString(SMS_BODY_INDEX);
                    haveDraft = true;
                }
            } finally {
                c.close();
            }
        }
        if (haveDraft && conv.getMessageCount() == 0) {
            clearConversation(conv);
        }
        return body;
    }
    private void clearConversation(final Conversation conv) {
        asyncDeleteDraftSmsMessage(conv);
        if (conv.getMessageCount() == 0) {
            if (DEBUG) LogTag.debug("clearConversation calling clearThreadId");
            conv.clearThreadId();
        }
        conv.setDraftState(false);
    }
    private void asyncUpdateDraftSmsMessage(final Conversation conv, final String contents) {
        new Thread(new Runnable() {
            public void run() {
                long threadId = conv.ensureThreadId();
                conv.setDraftState(true);
                updateDraftSmsMessage(threadId, contents);
            }
        }).start();
    }
    private void updateDraftSmsMessage(long thread_id, String contents) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("updateDraftSmsMessage tid=%d, contents=\"%s\"", thread_id, contents);
        }
        if (thread_id <= 0) {
            return;
        }
        ContentValues values = new ContentValues(3);
        values.put(Sms.THREAD_ID, thread_id);
        values.put(Sms.BODY, contents);
        values.put(Sms.TYPE, Sms.MESSAGE_TYPE_DRAFT);
        SqliteWrapper.insert(mContext, mContentResolver, Sms.CONTENT_URI, values);
        asyncDeleteDraftMmsMessage(thread_id);
    }
    private void asyncDelete(final Uri uri, final String selection, final String[] selectionArgs) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            LogTag.debug("asyncDelete %s where %s", uri, selection);
        }
        new Thread(new Runnable() {
            public void run() {
                SqliteWrapper.delete(mContext, mContentResolver, uri, selection, selectionArgs);
            }
        }).start();
    }
    private void asyncDeleteDraftSmsMessage(Conversation conv) {
        long threadId = conv.getThreadId();
        if (threadId > 0) {
            asyncDelete(ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                SMS_DRAFT_WHERE, null);
        }
    }
    private void deleteDraftSmsMessage(long threadId) {
        SqliteWrapper.delete(mContext, mContentResolver,
                ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, threadId),
                SMS_DRAFT_WHERE, null);
    }
    private void asyncDeleteDraftMmsMessage(long threadId) {
        final String where = Mms.THREAD_ID + " = " + threadId;
        asyncDelete(Mms.Draft.CONTENT_URI, where, null);
    }
}
