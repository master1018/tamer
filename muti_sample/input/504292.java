public class MessageView extends Activity implements OnClickListener {
    private static final String EXTRA_MESSAGE_ID = "com.android.email.MessageView_message_id";
    private static final String EXTRA_MAILBOX_ID = "com.android.email.MessageView_mailbox_id";
     static final String EXTRA_DISABLE_REPLY = "com.android.email.MessageView_disable_reply";
    private static final String STATE_MESSAGE_ID = "messageId";
    private static final Pattern IMG_TAG_START_REGEX = Pattern.compile("<(?i)img\\s+");
    private static final Pattern WEB_URL_PROTOCOL = Pattern.compile("(?i)http|https:
    private static final String[] BODY_CONTENT_PROJECTION = new String[] {
        Body.RECORD_ID, BodyColumns.MESSAGE_KEY,
        BodyColumns.HTML_CONTENT, BodyColumns.TEXT_CONTENT
    };
    private static final String[] PRESENCE_STATUS_PROJECTION =
        new String[] { Contacts.CONTACT_PRESENCE };
    private static final int BODY_CONTENT_COLUMN_RECORD_ID = 0;
    private static final int BODY_CONTENT_COLUMN_MESSAGE_KEY = 1;
    private static final int BODY_CONTENT_COLUMN_HTML_CONTENT = 2;
    private static final int BODY_CONTENT_COLUMN_TEXT_CONTENT = 3;
    private TextView mSubjectView;
    private TextView mFromView;
    private TextView mDateView;
    private TextView mTimeView;
    private TextView mToView;
    private TextView mCcView;
    private View mCcContainerView;
    private WebView mMessageContentView;
    private LinearLayout mAttachments;
    private ImageView mAttachmentIcon;
    private ImageView mFavoriteIcon;
    private View mShowPicturesSection;
    private View mInviteSection;
    private ImageView mSenderPresenceView;
    private ProgressDialog mProgressDialog;
    private View mScrollView;
    private TextView mMeetingYes;
    private TextView mMeetingMaybe;
    private TextView mMeetingNo;
    private int mPreviousMeetingResponse = -1;
    private long mAccountId;
    private long mMessageId;
    private long mMailboxId;
    private Message mMessage;
    private long mWaitForLoadMessageId;
    private LoadMessageTask mLoadMessageTask;
    private LoadBodyTask mLoadBodyTask;
    private LoadAttachmentsTask mLoadAttachmentsTask;
    private PresenceCheckTask mPresenceCheckTask;
    private long mLoadAttachmentId;         
    private boolean mLoadAttachmentSave;    
    private String mLoadAttachmentName;     
    private java.text.DateFormat mDateFormat;
    private java.text.DateFormat mTimeFormat;
    private Drawable mFavoriteIconOn;
    private Drawable mFavoriteIconOff;
    private MessageViewHandler mHandler;
    private Controller mController;
    private ControllerResults mControllerCallback;
    private View mMoveToNewer;
    private View mMoveToOlder;
    private LoadMessageListTask mLoadMessageListTask;
    private Cursor mMessageListCursor;
    private ContentObserver mCursorObserver;
    private String mHtmlTextRaw;
    private String mHtmlTextWebView;
    private boolean mDisableReplyAndForward;
    private class MessageViewHandler extends Handler {
        private static final int MSG_PROGRESS = 1;
        private static final int MSG_ATTACHMENT_PROGRESS = 2;
        private static final int MSG_LOAD_CONTENT_URI = 3;
        private static final int MSG_SET_ATTACHMENTS_ENABLED = 4;
        private static final int MSG_LOAD_BODY_ERROR = 5;
        private static final int MSG_NETWORK_ERROR = 6;
        private static final int MSG_FETCHING_ATTACHMENT = 10;
        private static final int MSG_VIEW_ATTACHMENT_ERROR = 12;
        private static final int MSG_UPDATE_ATTACHMENT_ICON = 18;
        private static final int MSG_FINISH_LOAD_ATTACHMENT = 19;
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS:
                    setProgressBarIndeterminateVisibility(msg.arg1 != 0);
                    break;
                case MSG_ATTACHMENT_PROGRESS:
                    boolean progress = (msg.arg1 != 0);
                    if (progress) {
                        mProgressDialog.setMessage(
                                getString(R.string.message_view_fetching_attachment_progress,
                                        mLoadAttachmentName));
                        mProgressDialog.show();
                    } else {
                        mProgressDialog.dismiss();
                    }
                    setProgressBarIndeterminateVisibility(progress);
                    break;
                case MSG_LOAD_CONTENT_URI:
                    String uriString = (String) msg.obj;
                    if (mMessageContentView != null) {
                        mMessageContentView.loadUrl(uriString);
                    }
                    break;
                case MSG_SET_ATTACHMENTS_ENABLED:
                    for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
                        AttachmentInfo attachment =
                            (AttachmentInfo) mAttachments.getChildAt(i).getTag();
                        attachment.viewButton.setEnabled(msg.arg1 == 1);
                        attachment.downloadButton.setEnabled(msg.arg1 == 1);
                    }
                    break;
                case MSG_LOAD_BODY_ERROR:
                    Toast.makeText(MessageView.this,
                            R.string.error_loading_message_body, Toast.LENGTH_LONG).show();
                    break;
                case MSG_NETWORK_ERROR:
                    Toast.makeText(MessageView.this,
                            R.string.status_network_error, Toast.LENGTH_LONG).show();
                    break;
                case MSG_FETCHING_ATTACHMENT:
                    Toast.makeText(MessageView.this,
                            getString(R.string.message_view_fetching_attachment_toast),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MSG_VIEW_ATTACHMENT_ERROR:
                    Toast.makeText(MessageView.this,
                            getString(R.string.message_view_display_attachment_toast),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MSG_UPDATE_ATTACHMENT_ICON:
                    ((AttachmentInfo) mAttachments.getChildAt(msg.arg1).getTag())
                        .iconView.setImageBitmap((Bitmap) msg.obj);
                    break;
                case MSG_FINISH_LOAD_ATTACHMENT:
                    long attachmentId = (Long)msg.obj;
                    doFinishLoadAttachment(attachmentId);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        public void attachmentProgress(boolean progress) {
            android.os.Message msg = android.os.Message.obtain(this, MSG_ATTACHMENT_PROGRESS);
            msg.arg1 = progress ? 1 : 0;
            sendMessage(msg);
        }
        public void progress(boolean progress) {
            android.os.Message msg = android.os.Message.obtain(this, MSG_PROGRESS);
            msg.arg1 = progress ? 1 : 0;
            sendMessage(msg);
        }
        public void loadContentUri(String uriString) {
            android.os.Message msg = android.os.Message.obtain(this, MSG_LOAD_CONTENT_URI);
            msg.obj = uriString;
            sendMessage(msg);
        }
        public void setAttachmentsEnabled(boolean enabled) {
            android.os.Message msg = android.os.Message.obtain(this, MSG_SET_ATTACHMENTS_ENABLED);
            msg.arg1 = enabled ? 1 : 0;
            sendMessage(msg);
        }
        public void loadBodyError() {
            sendEmptyMessage(MSG_LOAD_BODY_ERROR);
        }
        public void networkError() {
            sendEmptyMessage(MSG_NETWORK_ERROR);
        }
        public void fetchingAttachment() {
            sendEmptyMessage(MSG_FETCHING_ATTACHMENT);
        }
        public void attachmentViewError() {
            sendEmptyMessage(MSG_VIEW_ATTACHMENT_ERROR);
        }
        public void updateAttachmentIcon(int pos, Bitmap icon) {
            android.os.Message msg = android.os.Message.obtain(this, MSG_UPDATE_ATTACHMENT_ICON);
            msg.arg1 = pos;
            msg.obj = icon;
            sendMessage(msg);
        }
        public void finishLoadAttachment(long attachmentId) {
            android.os.Message msg = android.os.Message.obtain(this, MSG_FINISH_LOAD_ATTACHMENT);
            msg.obj = Long.valueOf(attachmentId);
            sendMessage(msg);
        }
    }
    private static class AttachmentInfo {
        public String name;
        public String contentType;
        public long size;
        public long attachmentId;
        public Button viewButton;
        public Button downloadButton;
        public ImageView iconView;
    }
    public static void actionView(Context context, long messageId, long mailboxId,
            boolean disableReplyAndForward) {
        if (messageId < 0) {
            throw new IllegalArgumentException("MessageView invalid messageId " + messageId);
        }
        Intent i = new Intent(context, MessageView.class);
        i.putExtra(EXTRA_MESSAGE_ID, messageId);
        i.putExtra(EXTRA_MAILBOX_ID, mailboxId);
        i.putExtra(EXTRA_DISABLE_REPLY, disableReplyAndForward);
        context.startActivity(i);
    }
    public static void actionView(Context context, long messageId, long mailboxId) {
        actionView(context, messageId, mailboxId, false);
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.message_view);
        mHandler = new MessageViewHandler();
        mControllerCallback = new ControllerResults();
        mSubjectView = (TextView) findViewById(R.id.subject);
        mFromView = (TextView) findViewById(R.id.from);
        mToView = (TextView) findViewById(R.id.to);
        mCcView = (TextView) findViewById(R.id.cc);
        mCcContainerView = findViewById(R.id.cc_container);
        mDateView = (TextView) findViewById(R.id.date);
        mTimeView = (TextView) findViewById(R.id.time);
        mMessageContentView = (WebView) findViewById(R.id.message_content);
        mAttachments = (LinearLayout) findViewById(R.id.attachments);
        mAttachmentIcon = (ImageView) findViewById(R.id.attachment);
        mFavoriteIcon = (ImageView) findViewById(R.id.favorite);
        mShowPicturesSection = findViewById(R.id.show_pictures_section);
        mInviteSection = findViewById(R.id.invite_section);
        mSenderPresenceView = (ImageView) findViewById(R.id.presence);
        mMoveToNewer = findViewById(R.id.moveToNewer);
        mMoveToOlder = findViewById(R.id.moveToOlder);
        mScrollView = findViewById(R.id.scrollview);
        mMoveToNewer.setOnClickListener(this);
        mMoveToOlder.setOnClickListener(this);
        mFromView.setOnClickListener(this);
        mSenderPresenceView.setOnClickListener(this);
        mFavoriteIcon.setOnClickListener(this);
        findViewById(R.id.reply).setOnClickListener(this);
        findViewById(R.id.reply_all).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.show_pictures).setOnClickListener(this);
        mMeetingYes = (TextView) findViewById(R.id.accept);
        mMeetingMaybe = (TextView) findViewById(R.id.maybe);
        mMeetingNo = (TextView) findViewById(R.id.decline);
        mMeetingYes.setOnClickListener(this);
        mMeetingMaybe.setOnClickListener(this);
        mMeetingNo.setOnClickListener(this);
        findViewById(R.id.invite_link).setOnClickListener(this);
        mMessageContentView.setVerticalScrollBarEnabled(false);
        mMessageContentView.getSettings().setBlockNetworkLoads(true);
        mMessageContentView.getSettings().setSupportZoom(false);
        mMessageContentView.setWebViewClient(new CustomWebViewClient());
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDateFormat = android.text.format.DateFormat.getDateFormat(this);   
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);   
        mFavoriteIconOn = getResources().getDrawable(R.drawable.btn_star_big_buttonless_on);
        mFavoriteIconOff = getResources().getDrawable(R.drawable.btn_star_big_buttonless_off);
        initFromIntent();
        if (icicle != null) {
            mMessageId = icicle.getLong(STATE_MESSAGE_ID, mMessageId);
        }
        mController = Controller.getInstance(getApplication());
        mCursorObserver = new ContentObserver(mHandler){
                @Override
                public void onChange(boolean selfChange) {
                    if (mLoadMessageListTask == null && mMessageListCursor != null) {
                        mLoadMessageListTask = new LoadMessageListTask(mMailboxId);
                        mLoadMessageListTask.execute();
                    }
                }
            };
        messageChanged();
    }
     void initFromIntent() {
        Intent intent = getIntent();
        mMessageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
        mMailboxId = intent.getLongExtra(EXTRA_MAILBOX_ID, -1);
        mDisableReplyAndForward = intent.getBooleanExtra(EXTRA_DISABLE_REPLY, false);
        if (mDisableReplyAndForward) {
            findViewById(R.id.reply).setEnabled(false);
            findViewById(R.id.reply_all).setEnabled(false);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (mMessageId != -1) {
            state.putLong(STATE_MESSAGE_ID, mMessageId);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mWaitForLoadMessageId = -1;
        mController.addResultCallback(mControllerCallback);
        if (Email.getNotifyUiAccountsChanged()) {
            Welcome.actionStart(this);
            finish();
            return;
        }
        if (mMessage != null) {
            startPresenceCheck();
            if (mLoadMessageListTask == null && mMailboxId != -1) {
                mLoadMessageListTask = new LoadMessageListTask(mMailboxId);
                mLoadMessageListTask.execute();
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mController.removeResultCallback(mControllerCallback);
        closeMessageListCursor();
    }
    private void closeMessageListCursor() {
        if (mMessageListCursor != null) {
            mMessageListCursor.unregisterContentObserver(mCursorObserver);
            mMessageListCursor.close();
            mMessageListCursor = null;
        }
    }
    private void cancelAllTasks() {
        Utility.cancelTaskInterrupt(mLoadMessageTask);
        mLoadMessageTask = null;
        Utility.cancelTaskInterrupt(mLoadBodyTask);
        mLoadBodyTask = null;
        Utility.cancelTaskInterrupt(mLoadAttachmentsTask);
        mLoadAttachmentsTask = null;
        Utility.cancelTaskInterrupt(mLoadMessageListTask);
        mLoadMessageListTask = null;
        Utility.cancelTaskInterrupt(mPresenceCheckTask);
        mPresenceCheckTask = null;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAllTasks();
        synchronized (this) {
            mMessageContentView.destroy();
            mMessageContentView = null;
        }
    }
    private void onDelete() {
        if (mMessage != null) {
            long messageIdToDelete = mMessageId;
            boolean moved = moveToOlder() || moveToNewer();
            mController.deleteMessage(messageIdToDelete, mMessage.mAccountKey);
            Toast.makeText(this, getResources().getQuantityString(R.plurals.message_deleted_toast,
                    1), Toast.LENGTH_SHORT).show();
            if (!moved) {
                finish();
            }
        }
    }
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && url.toLowerCase().startsWith("mailto:")) {
                return MessageCompose.actionCompose(MessageView.this, url, mAccountId);
            }
            boolean result = false;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            try {
                startActivity(intent);
                result = true;
            } catch (ActivityNotFoundException ex) {
            }
            return result;
        }
    }
    private void onClickSender() {
        if (mMessage == null) return;
        final Address senderEmail = Address.unpackFirst(mMessage.mFrom);
        if (senderEmail == null) return;
        final ContentResolver resolver = getContentResolver();
        final String address = senderEmail.getAddress();
        final Uri dataUri = Uri.withAppendedPath(CommonDataKinds.Email.CONTENT_FILTER_URI,
                Uri.encode(address));
        final Uri lookupUri = ContactsContract.Data.getContactLookupUri(resolver, dataUri);
        if (lookupUri != null) {
            QuickContact.showQuickContact(this, mSenderPresenceView, lookupUri,
                    QuickContact.MODE_LARGE, null);
        } else {
            final Uri mailUri = Uri.fromParts("mailto", address, null);
            final Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                    mailUri);
            intent.putExtra(ContactsContract.Intents.EXTRA_CREATE_DESCRIPTION,
                    senderEmail.toString());
            final String senderPersonal = senderEmail.getPersonal();
            if (!TextUtils.isEmpty(senderPersonal)) {
                intent.putExtra(ContactsContract.Intents.Insert.NAME, senderPersonal);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            startActivity(intent);
        }
    }
    private void onClickFavorite() {
        if (mMessage != null) {
            boolean newFavorite = ! mMessage.mFlagFavorite;
            mFavoriteIcon.setImageDrawable(newFavorite ? mFavoriteIconOn : mFavoriteIconOff);
            mMessage.mFlagFavorite = newFavorite;
            mController.setMessageFavorite(mMessageId, newFavorite);
        }
    }
    private void onReply() {
        if (mMessage != null) {
            MessageCompose.actionReply(this, mMessage.mId, false);
            finish();
        }
    }
    private void onReplyAll() {
        if (mMessage != null) {
            MessageCompose.actionReply(this, mMessage.mId, true);
            finish();
        }
    }
    private void onForward() {
        if (mMessage != null) {
            MessageCompose.actionForward(this, mMessage.mId);
            finish();
        }
    }
    private boolean moveToOlder() {
        if (mMessageListCursor != null
                && !mMessageListCursor.isLast()
                && mMessageListCursor.moveToNext()) {
            mMessageId = mMessageListCursor.getLong(0);
            messageChanged();
            return true;
        }
        return false;
    }
    private boolean moveToNewer() {
        if (mMessageListCursor != null
                && !mMessageListCursor.isFirst()
                && mMessageListCursor.moveToPrevious()) {
            mMessageId = mMessageListCursor.getLong(0);
            messageChanged();
            return true;
        }
        return false;
    }
    private void onMarkAsRead(boolean isRead) {
        if (mMessage != null && mMessage.mFlagRead != isRead) {
            mMessage.mFlagRead = isRead;
            mController.setMessageRead(mMessageId, isRead);
        }
    }
     static File createUniqueFile(File directory, String filename) {
        File file = new File(directory, filename);
        if (!file.exists()) {
            return file;
        }
        int index = filename.lastIndexOf('.');
        String format;
        if (index != -1) {
            String name = filename.substring(0, index);
            String extension = filename.substring(index);
            format = name + "-%d" + extension;
        }
        else {
            format = filename + "-%d";
        }
        for (int i = 2; i < Integer.MAX_VALUE; i++) {
            file = new File(directory, String.format(format, i));
            if (!file.exists()) {
                return file;
            }
        }
        return null;
    }
    private void onRespond(int response, int toastResId) {
        if (mPreviousMeetingResponse != response) {
            mController.sendMeetingResponse(mMessageId, response, mControllerCallback);
            mPreviousMeetingResponse = response;
        }
        Toast.makeText(this, toastResId, Toast.LENGTH_SHORT).show();
        if (!moveToOlder()) {
            finish(); 
        }
    }
    private void onDownloadAttachment(AttachmentInfo attachment) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this,
                    getString(R.string.message_view_status_attachment_not_saved),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mLoadAttachmentId = attachment.attachmentId;
        mLoadAttachmentSave = true;
        mLoadAttachmentName = attachment.name;
        mController.loadAttachment(attachment.attachmentId, mMessageId, mMessage.mMailboxKey,
                mAccountId, mControllerCallback);
    }
    private void onViewAttachment(AttachmentInfo attachment) {
        mLoadAttachmentId = attachment.attachmentId;
        mLoadAttachmentSave = false;
        mLoadAttachmentName = attachment.name;
        mController.loadAttachment(attachment.attachmentId, mMessageId, mMessage.mMailboxKey,
                mAccountId, mControllerCallback);
    }
    private void onShowPictures() {
        if (mMessage != null) {
            if (mMessageContentView != null) {
                mMessageContentView.getSettings().setBlockNetworkLoads(false);
                if (mHtmlTextWebView != null) {
                    mMessageContentView.loadDataWithBaseURL("email:
                                                            "text/html", "utf-8", null);
                }
            }
            mShowPicturesSection.setVisibility(View.GONE);
        }
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.from:
            case R.id.presence:
                onClickSender();
                break;
            case R.id.favorite:
                onClickFavorite();
                break;
            case R.id.reply:
                onReply();
                break;
            case R.id.reply_all:
                onReplyAll();
                break;
            case R.id.delete:
                onDelete();
                break;
            case R.id.moveToOlder:
                moveToOlder();
                break;
            case R.id.moveToNewer:
                moveToNewer();
                break;
            case R.id.download:
                onDownloadAttachment((AttachmentInfo) view.getTag());
                break;
            case R.id.view:
                onViewAttachment((AttachmentInfo) view.getTag());
                break;
            case R.id.show_pictures:
                onShowPictures();
                break;
            case R.id.accept:
                onRespond(EmailServiceConstants.MEETING_REQUEST_ACCEPTED,
                         R.string.message_view_invite_toast_yes);
                break;
            case R.id.maybe:
                onRespond(EmailServiceConstants.MEETING_REQUEST_TENTATIVE,
                         R.string.message_view_invite_toast_maybe);
                break;
            case R.id.decline:
                onRespond(EmailServiceConstants.MEETING_REQUEST_DECLINED,
                         R.string.message_view_invite_toast_no);
                break;
            case R.id.invite_link:
                String startTime =
                    new PackedString(mMessage.mMeetingInfo).get(MeetingInfo.MEETING_DTSTART);
                if (startTime != null) {
                    long epochTimeMillis = Utility.parseEmailDateTimeToMillis(startTime);
                    Uri uri = Uri.parse("content:
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    intent.putExtra("VIEW", "DAY");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                } else {
                    Email.log("meetingInfo without DTSTART " + mMessage.mMeetingInfo);
                }
                break;
        }
    }
   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       boolean handled = handleMenuItem(item.getItemId());
       if (!handled) {
           handled = super.onOptionsItemSelected(item);
       }
       return handled;
   }
    boolean handleMenuItem(int menuItemId) {
       switch (menuItemId) {
           case R.id.delete:
               onDelete();
               break;
           case R.id.reply:
               onReply();
               break;
           case R.id.reply_all:
               onReplyAll();
               break;
           case R.id.forward:
               onForward();
               break;
           case R.id.mark_as_unread:
               onMarkAsRead(false);
               finish();
               break;
           default:
               return false;
       }
       return true;
   }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.message_view_option, menu);
        if (mDisableReplyAndForward) {
            menu.findItem(R.id.forward).setEnabled(false);
            menu.findItem(R.id.reply).setEnabled(false);
            menu.findItem(R.id.reply_all).setEnabled(false);
        }
        return true;
    }
    private void messageChanged() {
        if (Email.DEBUG) {
            Email.log("MessageView: messageChanged to id=" + mMessageId);
        }
        cancelAllTasks();
        setTitle("");
        if (mMessageContentView != null) {
            mMessageContentView.scrollTo(0, 0);
            mMessageContentView.loadUrl("file:
        }
        mScrollView.scrollTo(0, 0);
        mAttachments.removeAllViews();
        mAttachments.setVisibility(View.GONE);
        mAttachmentIcon.setVisibility(View.GONE);
        mLoadMessageTask = new LoadMessageTask(mMessageId, true);
        mLoadMessageTask.execute();
        updateNavigationArrows(mMessageListCursor);
    }
    private void repositionMessageListCursor() {
        if (Email.DEBUG) {
            Email.log("MessageView: reposition to id=" + mMessageId);
        }
        mMessageListCursor.moveToPosition(-1);
        while (mMessageListCursor.moveToNext() && mMessageListCursor.getLong(0) != mMessageId) {
        }
        if (mMessageListCursor.isAfterLast()) {
            finish();
        }
        updateNavigationArrows(mMessageListCursor);
    }
    private void updateNavigationArrows(Cursor cursor) {
        if (cursor != null) {
            boolean hasNewer, hasOlder;
            if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
                hasNewer = hasOlder = false;
            } else {
                hasNewer = !cursor.isFirst();
                hasOlder = !cursor.isLast();
            }
            mMoveToNewer.setVisibility(hasNewer ? View.VISIBLE : View.INVISIBLE);
            mMoveToOlder.setVisibility(hasOlder ? View.VISIBLE : View.INVISIBLE);
        }
    }
    private Bitmap getPreviewIcon(AttachmentInfo attachment) {
        try {
            return BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(
                            AttachmentProvider.getAttachmentThumbnailUri(
                                    mAccountId, attachment.attachmentId,
                                    62,
                                    62)));
        }
        catch (Exception e) {
            return null;
        }
    }
    public static String formatSize(float size) {
        long kb = 1024;
        long mb = (kb * 1024);
        long gb  = (mb * 1024);
        if (size < kb) {
            return String.format("%d bytes", (int) size);
        }
        else if (size < mb) {
            return String.format("%.1f kB", size / kb);
        }
        else if (size < gb) {
            return String.format("%.1f MB", size / mb);
        }
        else {
            return String.format("%.1f GB", size / gb);
        }
    }
    private void updateAttachmentThumbnail(long attachmentId) {
        for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
            AttachmentInfo attachment = (AttachmentInfo) mAttachments.getChildAt(i).getTag();
            if (attachment.attachmentId == attachmentId) {
                Bitmap previewIcon = getPreviewIcon(attachment);
                if (previewIcon != null) {
                    mHandler.updateAttachmentIcon(i, previewIcon);
                }
                return;
            }
        }
    }
    private void addAttachment(Attachment attachment) {
        AttachmentInfo attachmentInfo = new AttachmentInfo();
        attachmentInfo.size = attachment.mSize;
        attachmentInfo.contentType =
                AttachmentProvider.inferMimeType(attachment.mFileName, attachment.mMimeType);
        attachmentInfo.name = attachment.mFileName;
        attachmentInfo.attachmentId = attachment.mId;
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.message_view_attachment, null);
        TextView attachmentName = (TextView)view.findViewById(R.id.attachment_name);
        TextView attachmentInfoView = (TextView)view.findViewById(R.id.attachment_info);
        ImageView attachmentIcon = (ImageView)view.findViewById(R.id.attachment_icon);
        Button attachmentView = (Button)view.findViewById(R.id.view);
        Button attachmentDownload = (Button)view.findViewById(R.id.download);
        if ((!MimeUtility.mimeTypeMatches(attachmentInfo.contentType,
                Email.ACCEPTABLE_ATTACHMENT_VIEW_TYPES))
                || (MimeUtility.mimeTypeMatches(attachmentInfo.contentType,
                        Email.UNACCEPTABLE_ATTACHMENT_VIEW_TYPES))) {
            attachmentView.setVisibility(View.GONE);
        }
        if ((!MimeUtility.mimeTypeMatches(attachmentInfo.contentType,
                Email.ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES))
                || (MimeUtility.mimeTypeMatches(attachmentInfo.contentType,
                        Email.UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES))) {
            attachmentDownload.setVisibility(View.GONE);
        }
        if (attachmentInfo.size > Email.MAX_ATTACHMENT_DOWNLOAD_SIZE) {
            attachmentView.setVisibility(View.GONE);
            attachmentDownload.setVisibility(View.GONE);
        }
        attachmentInfo.viewButton = attachmentView;
        attachmentInfo.downloadButton = attachmentDownload;
        attachmentInfo.iconView = attachmentIcon;
        view.setTag(attachmentInfo);
        attachmentView.setOnClickListener(this);
        attachmentView.setTag(attachmentInfo);
        attachmentDownload.setOnClickListener(this);
        attachmentDownload.setTag(attachmentInfo);
        attachmentName.setText(attachmentInfo.name);
        attachmentInfoView.setText(formatSize(attachmentInfo.size));
        Bitmap previewIcon = getPreviewIcon(attachmentInfo);
        if (previewIcon != null) {
            attachmentIcon.setImageBitmap(previewIcon);
        }
        mAttachments.addView(view);
        mAttachments.setVisibility(View.VISIBLE);
    }
    private class PresenceCheckTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... emails) {
            Cursor cursor =
                    getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    PRESENCE_STATUS_PROJECTION, CommonDataKinds.Email.DATA + "=?", emails, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(0);
                        int icon = StatusUpdates.getPresenceIconResourceId(status);
                        return icon;
                    }
                } finally {
                    cursor.close();
                }
            }
            return 0;
        }
        @Override
        protected void onPostExecute(Integer icon) {
            if (icon == null) {
                return;
            }
            updateSenderPresence(icon);
        }
    }
    private void startPresenceCheck() {
        if (mMessage != null) {
            Address sender = Address.unpackFirst(mMessage.mFrom);
            if (sender != null) {
                String email = sender.getAddress();
                if (email != null) {
                    mPresenceCheckTask = new PresenceCheckTask();
                    mPresenceCheckTask.execute(email);
                    return;
                }
            }
        }
        updateSenderPresence(0);
    }
    private void updateSenderPresence(int presenceIconId) {
        if (presenceIconId == 0) {
            presenceIconId = R.drawable.presence_inactive;
        }
        mSenderPresenceView.setImageResource(presenceIconId);
    }
    private class LoadMessageListTask extends AsyncTask<Void, Void, Cursor> {
        private long mLocalMailboxId;
        public LoadMessageListTask(long mailboxId) {
            mLocalMailboxId = mailboxId;
        }
        @Override
        protected Cursor doInBackground(Void... params) {
            String selection =
                Utility.buildMailboxIdSelection(getContentResolver(), mLocalMailboxId);
            Cursor c = getContentResolver().query(EmailContent.Message.CONTENT_URI,
                    EmailContent.ID_PROJECTION,
                    selection, null,
                    EmailContent.MessageColumns.TIMESTAMP + " DESC");
            return c;
        }
        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor == null) {
                return;
            }
            MessageView.this.mLoadMessageListTask = null;
            if (cursor.isClosed()) {
                return;
            }
            closeMessageListCursor();
            mMessageListCursor = cursor;
            mMessageListCursor.registerContentObserver(MessageView.this.mCursorObserver);
            repositionMessageListCursor();
        }
    }
    private class LoadMessageTask extends AsyncTask<Void, Void, Message> {
        private long mId;
        private boolean mOkToFetch;
        public LoadMessageTask(long messageId, boolean okToFetch) {
            mId = messageId;
            mOkToFetch = okToFetch;
        }
        @Override
        protected Message doInBackground(Void... params) {
            if (mId == Long.MIN_VALUE)  {
                return null;
            }
            return Message.restoreMessageWithId(MessageView.this, mId);
        }
        @Override
        protected void onPostExecute(Message message) {
            if (isCancelled()) {
                return;
            }
            if (message == null) {
                if (mId != Long.MIN_VALUE) {
                    finish();
                }
                return;
            }
            reloadUiFromMessage(message, mOkToFetch);
            startPresenceCheck();
        }
    }
    private class LoadBodyTask extends AsyncTask<Void, Void, String[]> {
        private long mId;
        public LoadBodyTask(long messageId) {
            mId = messageId;
        }
        @Override
        protected String[] doInBackground(Void... params) {
            try {
                String text = null;
                String html = Body.restoreBodyHtmlWithMessageId(MessageView.this, mId);
                if (html == null) {
                    text = Body.restoreBodyTextWithMessageId(MessageView.this, mId);
                }
                return new String[] { text, html };
            } catch (RuntimeException re) {
                Log.d(Email.LOG_TAG, "Exception while loading message body: " + re.toString());
                mHandler.loadBodyError();
                return new String[] { null, null };
            }
        }
        @Override
        protected void onPostExecute(String[] results) {
            if (results == null) {
                return;
            }
            reloadUiFromBody(results[0], results[1]);    
            onMarkAsRead(true);
        }
    }
    private class LoadAttachmentsTask extends AsyncTask<Long, Void, Attachment[]> {
        @Override
        protected Attachment[] doInBackground(Long... messageIds) {
            return Attachment.restoreAttachmentsWithMessageId(MessageView.this, messageIds[0]);
        }
        @Override
        protected void onPostExecute(Attachment[] attachments) {
            if (attachments == null) {
                return;
            }
            boolean htmlChanged = false;
            for (Attachment attachment : attachments) {
                if (mHtmlTextRaw != null && attachment.mContentId != null
                        && attachment.mContentUri != null) {
                    String contentIdRe =
                        "\\s+(?i)src=\"cid(?-i):\\Q" + attachment.mContentId + "\\E\"";
                    String srcContentUri = " src=\"" + attachment.mContentUri + "\"";
                    mHtmlTextRaw = mHtmlTextRaw.replaceAll(contentIdRe, srcContentUri);
                    htmlChanged = true;
                } else {
                    addAttachment(attachment);
                }
            }
            mHtmlTextWebView = mHtmlTextRaw;
            mHtmlTextRaw = null;
            if (htmlChanged && mMessageContentView != null) {
                mMessageContentView.loadDataWithBaseURL("email:
                                                        "text/html", "utf-8", null);
            }
        }
    }
    private void reloadUiFromMessage(Message message, boolean okToFetch) {
        mMessage = message;
        mAccountId = message.mAccountKey;
        if (mMailboxId == -1) {
            mMailboxId = message.mMailboxKey;
        }
        if (mMessageListCursor == null) {
            mLoadMessageListTask = new LoadMessageListTask(mMailboxId);
            mLoadMessageListTask.execute();
        }
        mSubjectView.setText(message.mSubject);
        mFromView.setText(Address.toFriendly(Address.unpack(message.mFrom)));
        Date date = new Date(message.mTimeStamp);
        mTimeView.setText(mTimeFormat.format(date));
        mDateView.setText(Utility.isDateToday(date) ? null : mDateFormat.format(date));
        mToView.setText(Address.toFriendly(Address.unpack(message.mTo)));
        String friendlyCc = Address.toFriendly(Address.unpack(message.mCc));
        mCcView.setText(friendlyCc);
        mCcContainerView.setVisibility((friendlyCc != null) ? View.VISIBLE : View.GONE);
        mAttachmentIcon.setVisibility(message.mAttachments != null ? View.VISIBLE : View.GONE);
        mFavoriteIcon.setImageDrawable(message.mFlagFavorite ? mFavoriteIconOn : mFavoriteIconOff);
        mInviteSection.setVisibility((message.mFlags & Message.FLAG_INCOMING_MEETING_INVITE) != 0 ?
                View.VISIBLE : View.GONE);
        if (okToFetch && message.mFlagLoaded != Message.FLAG_LOADED_COMPLETE) {
            mWaitForLoadMessageId = message.mId;
            mController.loadMessageForView(message.mId, mControllerCallback);
        } else {
            mWaitForLoadMessageId = -1;
            mLoadBodyTask = new LoadBodyTask(message.mId);
            mLoadBodyTask.execute();
        }
    }
    private void reloadUiFromBody(String bodyText, String bodyHtml) {
        String text = null;
        mHtmlTextRaw = null;
        boolean hasImages = false;
        if (bodyHtml == null) {
            text = bodyText;
            StringBuffer sb = new StringBuffer("<html><body>");
            if (text != null) {
                text = EmailHtmlUtil.escapeCharacterToDisplay(text);
                Matcher m = Patterns.WEB_URL.matcher(text);
                while (m.find()) {
                    int start = m.start();
                    if (start == 0 || text.charAt(start - 1) != '@') {
                        String url = m.group();
                        Matcher proto = WEB_URL_PROTOCOL.matcher(url);
                        String link;
                        if (proto.find()) {
                            link = proto.group().toLowerCase() + url.substring(proto.end());
                        } else {
                            link = "http:
                        }
                        String href = String.format("<a href=\"%s\">%s</a>", link, url);
                        m.appendReplacement(sb, href);
                    }
                    else {
                        m.appendReplacement(sb, "$0");
                    }
                }
                m.appendTail(sb);
            }
            sb.append("</body></html>");
            text = sb.toString();
        } else {
            text = bodyHtml;
            mHtmlTextRaw = bodyHtml;
            hasImages = IMG_TAG_START_REGEX.matcher(text).find();
        }
        mShowPicturesSection.setVisibility(hasImages ? View.VISIBLE : View.GONE);
        if (mMessageContentView != null) {
            mMessageContentView.loadDataWithBaseURL("email:
        }
        mLoadAttachmentsTask = new LoadAttachmentsTask();
        mLoadAttachmentsTask.execute(mMessage.mId);
    }
    private class ControllerResults implements Controller.Result {
        public void loadMessageForViewCallback(MessagingException result, long messageId,
                int progress) {
            if (messageId != MessageView.this.mMessageId
                    || messageId != MessageView.this.mWaitForLoadMessageId) {
                return;
            }
            if (result == null) {
                switch (progress) {
                    case 0:
                        mHandler.progress(true);
                        mHandler.loadContentUri("file:
                        break;
                    case 100:
                        mWaitForLoadMessageId = -1;
                        mHandler.progress(false);
                        cancelAllTasks();
                        mLoadMessageTask = new LoadMessageTask(mMessageId, false);
                        mLoadMessageTask.execute();
                        break;
                    default:
                        break;
                }
            } else {
                mWaitForLoadMessageId = -1;
                mHandler.progress(false);
                mHandler.networkError();
                mHandler.loadContentUri("file:
            }
        }
        public void loadAttachmentCallback(MessagingException result, long messageId,
                long attachmentId, int progress) {
            if (messageId == MessageView.this.mMessageId) {
                if (result == null) {
                    switch (progress) {
                        case 0:
                            mHandler.setAttachmentsEnabled(false);
                            mHandler.attachmentProgress(true);
                            mHandler.fetchingAttachment();
                            break;
                        case 100:
                            mHandler.setAttachmentsEnabled(true);
                            mHandler.attachmentProgress(false);
                            updateAttachmentThumbnail(attachmentId);
                            mHandler.finishLoadAttachment(attachmentId);
                            break;
                        default:
                            break;
                    }
                } else {
                    mHandler.setAttachmentsEnabled(true);
                    mHandler.attachmentProgress(false);
                    mHandler.networkError();
                }
            }
        }
        public void updateMailboxCallback(MessagingException result, long accountId,
                long mailboxId, int progress, int numNewMessages) {
            if (result != null || progress == 100) {
                Email.updateMailboxRefreshTime(mailboxId);
            }
        }
        public void updateMailboxListCallback(MessagingException result, long accountId,
                int progress) {
        }
        public void serviceCheckMailCallback(MessagingException result, long accountId,
                long mailboxId, int progress, long tag) {
        }
        public void sendMailCallback(MessagingException result, long accountId, long messageId,
                int progress) {
        }
    }
    private void doFinishLoadAttachment(long attachmentId) {
        if (attachmentId != mLoadAttachmentId) {
            return;
        }
        Attachment attachment =
            Attachment.restoreAttachmentWithId(MessageView.this, attachmentId);
        Uri attachmentUri = AttachmentProvider.getAttachmentUri(mAccountId, attachment.mId);
        Uri contentUri =
            AttachmentProvider.resolveAttachmentIdToContentUri(getContentResolver(), attachmentUri);
        if (mLoadAttachmentSave) {
            try {
                File file = createUniqueFile(Environment.getExternalStorageDirectory(),
                        attachment.mFileName);
                InputStream in = getContentResolver().openInputStream(contentUri);
                OutputStream out = new FileOutputStream(file);
                IOUtils.copy(in, out);
                out.flush();
                out.close();
                in.close();
                Toast.makeText(MessageView.this, String.format(
                        getString(R.string.message_view_status_attachment_saved), file.getName()),
                        Toast.LENGTH_LONG).show();
                new MediaScannerNotifier(this, file, mHandler);
            } catch (IOException ioe) {
                Toast.makeText(MessageView.this,
                        getString(R.string.message_view_status_attachment_not_saved),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(contentUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                mHandler.attachmentViewError();
            }
        }
    }
    private static class MediaScannerNotifier implements MediaScannerConnectionClient {
        private Context mContext;
        private MediaScannerConnection mConnection;
        private File mFile;
        private MessageViewHandler mHandler;
        public MediaScannerNotifier(Context context, File file, MessageViewHandler handler) {
            mContext = context;
            mFile = file;
            mHandler = handler;
            mConnection = new MediaScannerConnection(context, this);
            mConnection.connect();
        }
        public void onMediaScannerConnected() {
            mConnection.scanFile(mFile.getAbsolutePath(), null);
        }
        public void onScanCompleted(String path, Uri uri) {
            try {
                if (uri != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    mContext.startActivity(intent);
                }
            } catch (ActivityNotFoundException e) {
                mHandler.attachmentViewError();
            } finally {
                mConnection.disconnect();
                mContext = null;
                mHandler = null;
            }
        }
    }
}
