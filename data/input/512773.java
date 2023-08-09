public class ComposeMessageActivity extends Activity
        implements View.OnClickListener, TextView.OnEditorActionListener,
        MessageStatusListener, Contact.UpdateListener {
    public static final int REQUEST_CODE_ATTACH_IMAGE     = 10;
    public static final int REQUEST_CODE_TAKE_PICTURE     = 11;
    public static final int REQUEST_CODE_ATTACH_VIDEO     = 12;
    public static final int REQUEST_CODE_TAKE_VIDEO       = 13;
    public static final int REQUEST_CODE_ATTACH_SOUND     = 14;
    public static final int REQUEST_CODE_RECORD_SOUND     = 15;
    public static final int REQUEST_CODE_CREATE_SLIDESHOW = 16;
    public static final int REQUEST_CODE_ECM_EXIT_DIALOG  = 17;
    public static final int REQUEST_CODE_ADD_CONTACT      = 18;
    private static final String TAG = "Mms/compose";
    private static final boolean DEBUG = false;
    private static final boolean TRACE = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int MENU_ADD_SUBJECT           = 0;
    private static final int MENU_DELETE_THREAD         = 1;
    private static final int MENU_ADD_ATTACHMENT        = 2;
    private static final int MENU_DISCARD               = 3;
    private static final int MENU_SEND                  = 4;
    private static final int MENU_CALL_RECIPIENT        = 5;
    private static final int MENU_CONVERSATION_LIST     = 6;
    private static final int MENU_VIEW_CONTACT          = 12;
    private static final int MENU_ADD_TO_CONTACTS       = 13;
    private static final int MENU_EDIT_MESSAGE          = 14;
    private static final int MENU_VIEW_SLIDESHOW        = 16;
    private static final int MENU_VIEW_MESSAGE_DETAILS  = 17;
    private static final int MENU_DELETE_MESSAGE        = 18;
    private static final int MENU_SEARCH                = 19;
    private static final int MENU_DELIVERY_REPORT       = 20;
    private static final int MENU_FORWARD_MESSAGE       = 21;
    private static final int MENU_CALL_BACK             = 22;
    private static final int MENU_SEND_EMAIL            = 23;
    private static final int MENU_COPY_MESSAGE_TEXT     = 24;
    private static final int MENU_COPY_TO_SDCARD        = 25;
    private static final int MENU_INSERT_SMILEY         = 26;
    private static final int MENU_ADD_ADDRESS_TO_CONTACTS = 27;
    private static final int MENU_LOCK_MESSAGE          = 28;
    private static final int MENU_UNLOCK_MESSAGE        = 29;
    private static final int MENU_COPY_TO_DRM_PROVIDER  = 30;
    private static final int RECIPIENTS_MAX_LENGTH = 312;
    private static final int MESSAGE_LIST_QUERY_TOKEN = 9527;
    private static final int DELETE_MESSAGE_TOKEN  = 9700;
    private static final int CHARS_REMAINING_BEFORE_COUNTER_SHOWN = 10;
    private static final long NO_DATE_FOR_DIALOG = -1L;
    private static final String EXIT_ECM_RESULT = "exit_ecm_result";
    private ContentResolver mContentResolver;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private Conversation mConversation;     
    private boolean mExitOnSent;            
    private View mTopPanel;                 
    private View mBottomPanel;              
    private EditText mTextEditor;           
    private TextView mTextCounter;          
    private Button mSendButton;             
    private EditText mSubjectTextEditor;    
    private AttachmentEditor mAttachmentEditor;
    private MessageListView mMsgListView;        
    public MessageListAdapter mMsgListAdapter;  
    private RecipientsEditor mRecipientsEditor;  
    private boolean mIsKeyboardOpen;             
    private boolean mIsLandscape;                
    private boolean mPossiblePendingNotification;   
    private boolean mToastForDraftSave;   
    private boolean mSentMessage;       
    private WorkingMessage mWorkingMessage;         
    private AlertDialog mSmileyDialog;
    private boolean mWaitingForSubActivity;
    private int mLastRecipientCount;            
    private AttachmentTypeSelectorAdapter mAttachmentTypeSelectorAdapter;
    private boolean mSendingMessage;    
    private Intent mAddContactIntent;   
    @SuppressWarnings("unused")
    private static void log(String logMsg) {
        Thread current = Thread.currentThread();
        long tid = current.getId();
        StackTraceElement[] stack = current.getStackTrace();
        String methodName = stack[3].getMethodName();
        logMsg = "[" + tid + "] [" + methodName + "] " + logMsg;
        Log.d(TAG, logMsg);
    }
    private void editSlideshow() {
        Uri dataUri = mWorkingMessage.saveAsMms(false);
        Intent intent = new Intent(this, SlideshowEditActivity.class);
        intent.setData(dataUri);
        startActivityForResult(intent, REQUEST_CODE_CREATE_SLIDESHOW);
    }
    private final Handler mAttachmentEditorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AttachmentEditor.MSG_EDIT_SLIDESHOW: {
                    editSlideshow();
                    break;
                }
                case AttachmentEditor.MSG_SEND_SLIDESHOW: {
                    if (isPreparedForSending()) {
                        ComposeMessageActivity.this.confirmSendMessageIfNeeded();
                    }
                    break;
                }
                case AttachmentEditor.MSG_VIEW_IMAGE:
                case AttachmentEditor.MSG_PLAY_VIDEO:
                case AttachmentEditor.MSG_PLAY_AUDIO:
                case AttachmentEditor.MSG_PLAY_SLIDESHOW:
                    MessageUtils.viewMmsMessageAttachment(ComposeMessageActivity.this,
                            mWorkingMessage);
                    break;
                case AttachmentEditor.MSG_REPLACE_IMAGE:
                case AttachmentEditor.MSG_REPLACE_VIDEO:
                case AttachmentEditor.MSG_REPLACE_AUDIO:
                    showAddAttachmentDialog(true);
                    break;
                case AttachmentEditor.MSG_REMOVE_ATTACHMENT:
                    mWorkingMessage.setAttachment(WorkingMessage.TEXT, null, false);
                    break;
                default:
                    break;
            }
        }
    };
    private final Handler mMessageListItemHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String type;
            switch (msg.what) {
                case MessageListItem.MSG_LIST_EDIT_MMS:
                    type = "mms";
                    break;
                case MessageListItem.MSG_LIST_EDIT_SMS:
                    type = "sms";
                    break;
                default:
                    Log.w(TAG, "Unknown message: " + msg.what);
                    return;
            }
            MessageItem msgItem = getMessageItem(type, (Long) msg.obj, false);
            if (msgItem != null) {
                editMessageItem(msgItem);
                drawBottomPanel();
            }
        }
    };
    private final OnKeyListener mSubjectKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if ((keyCode == KeyEvent.KEYCODE_DEL) && (mSubjectTextEditor.length() == 0)) {
                showSubjectEditor(false);
                mWorkingMessage.setSubject(null, true);
                return true;
            }
            return false;
        }
    };
    private MessageItem getMessageItem(String type, long msgId,
            boolean createFromCursorIfNotInCache) {
        return mMsgListAdapter.getCachedMessageItem(type, msgId,
                createFromCursorIfNotInCache ? mMsgListAdapter.getCursor() : null);
    }
    private boolean isCursorValid() {
        Cursor cursor = mMsgListAdapter.getCursor();
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            Log.e(TAG, "Bad cursor.", new RuntimeException());
            return false;
        }
        return true;
    }
    private void resetCounter() {
        mTextCounter.setText("");
        mTextCounter.setVisibility(View.GONE);
    }
    private void updateCounter(CharSequence text, int start, int before, int count) {
        WorkingMessage workingMessage = mWorkingMessage;
        if (workingMessage.requiresMms()) {
            final boolean textRemoved = (before > count);
            if (!textRemoved) {
                setSendButtonText(workingMessage.requiresMms());
                return;
            }
        }
        int[] params = SmsMessage.calculateLength(text, false);
        int msgCount = params[0];
        int remainingInCurrentMessage = params[2];
        boolean showCounter = false;
        if (!workingMessage.requiresMms() &&
                (msgCount > 1 ||
                 remainingInCurrentMessage <= CHARS_REMAINING_BEFORE_COUNTER_SHOWN)) {
            showCounter = true;
        }
        setSendButtonText(workingMessage.requiresMms());
        if (showCounter) {
            String counterText = msgCount > 1 ? remainingInCurrentMessage + " / " + msgCount
                    : String.valueOf(remainingInCurrentMessage);
            mTextCounter.setText(counterText);
            mTextCounter.setVisibility(View.VISIBLE);
        } else {
            mTextCounter.setVisibility(View.GONE);
        }
    }
    @Override
    public void startActivityForResult(Intent intent, int requestCode)
    {
        if (requestCode >= 0) {
            mWaitingForSubActivity = true;
        }
        super.startActivityForResult(intent, requestCode);
    }
    private void toastConvertInfo(boolean toMms) {
        final int resId = toMms ? R.string.converting_to_picture_message
                : R.string.converting_to_text_message;
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
    private class DeleteMessageListener implements OnClickListener {
        private final Uri mDeleteUri;
        private final boolean mDeleteLocked;
        public DeleteMessageListener(Uri uri, boolean deleteLocked) {
            mDeleteUri = uri;
            mDeleteLocked = deleteLocked;
        }
        public DeleteMessageListener(long msgId, String type, boolean deleteLocked) {
            if ("mms".equals(type)) {
                mDeleteUri = ContentUris.withAppendedId(Mms.CONTENT_URI, msgId);
            } else {
                mDeleteUri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgId);
            }
            mDeleteLocked = deleteLocked;
        }
        public void onClick(DialogInterface dialog, int whichButton) {
            mBackgroundQueryHandler.startDelete(DELETE_MESSAGE_TOKEN,
                    null, mDeleteUri, mDeleteLocked ? null : "locked=0", null);
        }
    }
    private class DiscardDraftListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            mWorkingMessage.discard();
            finish();
        }
    }
    private class SendIgnoreInvalidRecipientListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            sendMessage(true);
        }
    }
    private class CancelSendingListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton) {
            if (isRecipientsEditorVisible()) {
                mRecipientsEditor.requestFocus();
            }
        }
    }
    private void confirmSendMessageIfNeeded() {
        if (!isRecipientsEditorVisible()) {
            sendMessage(true);
            return;
        }
        boolean isMms = mWorkingMessage.requiresMms();
        if (mRecipientsEditor.hasInvalidRecipient(isMms)) {
            if (mRecipientsEditor.hasValidRecipient(isMms)) {
                String title = getResourcesString(R.string.has_invalid_recipient,
                        mRecipientsEditor.formatInvalidNumbers(isMms));
                new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(title)
                    .setMessage(R.string.invalid_recipient_message)
                    .setPositiveButton(R.string.try_to_send,
                            new SendIgnoreInvalidRecipientListener())
                    .setNegativeButton(R.string.no, new CancelSendingListener())
                    .show();
            } else {
                new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.cannot_send_message)
                    .setMessage(R.string.cannot_send_message_reason)
                    .setPositiveButton(R.string.yes, new CancelSendingListener())
                    .show();
            }
        } else {
            sendMessage(true);
        }
    }
    private final TextWatcher mRecipientsWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onUserInteraction();
        }
        public void afterTextChanged(Editable s) {
            if (!isRecipientsEditorVisible()) {
                IllegalStateException e = new IllegalStateException(
                        "afterTextChanged called with invisible mRecipientsEditor");
                Log.w(TAG,
                     "RecipientsWatcher: afterTextChanged called with invisible mRecipientsEditor");
                return;
            }
            mWorkingMessage.setWorkingRecipients(mRecipientsEditor.getNumbers());
            mWorkingMessage.setHasEmail(mRecipientsEditor.containsEmail(), true);
            checkForTooManyRecipients();
            for (int pos = s.length() - 1; pos >= 0; pos--) {
                char c = s.charAt(pos);
                if (c == ' ')
                    continue;
                if (c == ',') {
                    updateTitle(mConversation.getRecipients());
                }
                break;
            }
            updateSendButtonState();
        }
    };
    private void checkForTooManyRecipients() {
        final int recipientLimit = MmsConfig.getRecipientLimit();
        if (recipientLimit != Integer.MAX_VALUE) {
            final int recipientCount = recipientCount();
            boolean tooMany = recipientCount > recipientLimit;
            if (recipientCount != mLastRecipientCount) {
                mLastRecipientCount = recipientCount;
                if (tooMany) {
                    String tooManyMsg = getString(R.string.too_many_recipients, recipientCount,
                            recipientLimit);
                    Toast.makeText(ComposeMessageActivity.this,
                            tooManyMsg, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private final OnCreateContextMenuListener mRecipientsMenuCreateListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v,
                ContextMenuInfo menuInfo) {
            if (menuInfo != null) {
                Contact c = ((RecipientContextMenuInfo) menuInfo).recipient;
                RecipientsMenuClickListener l = new RecipientsMenuClickListener(c);
                menu.setHeaderTitle(c.getName());
                if (c.existsInDatabase()) {
                    menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact)
                            .setOnMenuItemClickListener(l);
                } else if (canAddToContacts(c)){
                    menu.add(0, MENU_ADD_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                            .setOnMenuItemClickListener(l);
                }
            }
        }
    };
    private final class RecipientsMenuClickListener implements MenuItem.OnMenuItemClickListener {
        private final Contact mRecipient;
        RecipientsMenuClickListener(Contact recipient) {
            mRecipient = recipient;
        }
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case MENU_VIEW_CONTACT: {
                    Uri contactUri = mRecipient.getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                    return true;
                }
                case MENU_ADD_TO_CONTACTS: {
                    mAddContactIntent = ConversationList.createAddContactIntent(
                            mRecipient.getNumber());
                    ComposeMessageActivity.this.startActivityForResult(mAddContactIntent,
                            REQUEST_CODE_ADD_CONTACT);
                    return true;
                }
            }
            return false;
        }
    }
    private boolean canAddToContacts(Contact contact) {
        final String name = contact.getName();
        if (!TextUtils.isEmpty(contact.getNumber())) {
            char c = contact.getNumber().charAt(0);
            if (isSpecialChar(c)) {
                return false;
            }
        }
        if (!TextUtils.isEmpty(name)) {
            char c = name.charAt(0);
            if (isSpecialChar(c)) {
                return false;
            }
        }
        if (!(Mms.isEmailAddress(name) || Mms.isPhoneNumber(name) ||
                MessageUtils.isLocalNumber(contact.getNumber()))) {     
            return false;
        }
        return true;
    }
    private boolean isSpecialChar(char c) {
        return c == '*' || c == '%' || c == '$';
    }
    private void addPositionBasedMenuItems(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo");
            return;
        }
        final int position = info.position;
        addUriSpecificMenuItems(menu, v, position);
    }
    private Uri getSelectedUriFromMessageList(ListView listView, int position) {
        MessageListItem msglistItem = (MessageListItem) listView.getChildAt(position);
        if (msglistItem == null) {
            return null;
        }
        TextView textView;
        CharSequence text = null;
        int selStart = -1;
        int selEnd = -1;
        textView = (TextView) msglistItem.findViewById(R.id.text_view);
        if (textView != null) {
            text = textView.getText();
            selStart = textView.getSelectionStart();
            selEnd = textView.getSelectionEnd();
        }
        if (selStart == -1) {
            textView = (TextView) msglistItem.findViewById(R.id.body_text_view);
            if (textView != null) {
                text = textView.getText();
                selStart = textView.getSelectionStart();
                selEnd = textView.getSelectionEnd();
            }
        }
        if (selStart != selEnd) {
            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);
            URLSpan[] urls = ((Spanned) text).getSpans(min, max,
                                                        URLSpan.class);
            if (urls.length == 1) {
                return Uri.parse(urls[0].getURL());
            }
        }
        return null;
    }
    private void addUriSpecificMenuItems(ContextMenu menu, View v, int position) {
        Uri uri = getSelectedUriFromMessageList((ListView) v, position);
        if (uri != null) {
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
            menu.addIntentOptions(0, 0, 0,
                    new android.content.ComponentName(this, ComposeMessageActivity.class),
                    null, intent, 0, null);
        }
    }
    private final void addCallAndContactMenuItems(
            ContextMenu menu, MsgListMenuClickListener l, MessageItem msgItem) {
        StringBuilder textToSpannify = new StringBuilder();
        if (msgItem.mBoxId == Mms.MESSAGE_BOX_INBOX) {
            textToSpannify.append(msgItem.mAddress + ": ");
        }
        textToSpannify.append(msgItem.mBody);
        SpannableString msg = new SpannableString(textToSpannify.toString());
        Linkify.addLinks(msg, Linkify.ALL);
        ArrayList<String> uris =
            MessageUtils.extractUris(msg.getSpans(0, msg.length(), URLSpan.class));
        while (uris.size() > 0) {
            String uriString = uris.remove(0);
            while (uris.contains(uriString)) {
                uris.remove(uriString);
            }
            int sep = uriString.indexOf(":");
            String prefix = null;
            if (sep >= 0) {
                prefix = uriString.substring(0, sep);
                uriString = uriString.substring(sep + 1);
            }
            boolean addToContacts = false;
            if ("mailto".equalsIgnoreCase(prefix))  {
                String sendEmailString = getString(
                        R.string.menu_send_email).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("mailto:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                menu.add(0, MENU_SEND_EMAIL, 0, sendEmailString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
                addToContacts = !haveEmailContact(uriString);
            } else if ("tel".equalsIgnoreCase(prefix)) {
                String callBackString = getString(
                        R.string.menu_call_back).replace("%s", uriString);
                Intent intent = new Intent(Intent.ACTION_CALL,
                        Uri.parse("tel:" + uriString));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                menu.add(0, MENU_CALL_BACK, 0, callBackString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
                addToContacts = !isNumberInContacts(uriString);
            }
            if (addToContacts) {
                Intent intent = ConversationList.createAddContactIntent(uriString);
                String addContactString = getString(
                        R.string.menu_add_address_to_contacts).replace("%s", uriString);
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, addContactString)
                    .setOnMenuItemClickListener(l)
                    .setIntent(intent);
            }
        }
    }
    private boolean haveEmailContact(String emailAddress) {
        Cursor cursor = SqliteWrapper.query(this, getContentResolver(),
                Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(emailAddress)),
                new String[] { Contacts.DISPLAY_NAME }, null, null, null);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    if (!TextUtils.isEmpty(name)) {
                        return true;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return false;
    }
    private boolean isNumberInContacts(String phoneNumber) {
        return Contact.get(phoneNumber, false).existsInDatabase();
    }
    private final OnCreateContextMenuListener mMsgListMenuCreateListener =
        new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            Cursor cursor = mMsgListAdapter.getCursor();
            String type = cursor.getString(COLUMN_MSG_TYPE);
            long msgId = cursor.getLong(COLUMN_ID);
            addPositionBasedMenuItems(menu, v, menuInfo);
            MessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
            if (msgItem == null) {
                Log.e(TAG, "Cannot load message item for type = " + type
                        + ", msgId = " + msgId);
                return;
            }
            menu.setHeaderTitle(R.string.message_options);
            MsgListMenuClickListener l = new MsgListMenuClickListener();
            if (msgItem.mLocked) {
                menu.add(0, MENU_UNLOCK_MESSAGE, 0, R.string.menu_unlock)
                    .setOnMenuItemClickListener(l);
            } else {
                menu.add(0, MENU_LOCK_MESSAGE, 0, R.string.menu_lock)
                    .setOnMenuItemClickListener(l);
            }
            if (msgItem.isMms()) {
                switch (msgItem.mBoxId) {
                    case Mms.MESSAGE_BOX_INBOX:
                        break;
                    case Mms.MESSAGE_BOX_OUTBOX:
                        if (getRecipients().size() == 1) {
                            menu.add(0, MENU_EDIT_MESSAGE, 0, R.string.menu_edit)
                                    .setOnMenuItemClickListener(l);
                        }
                        break;
                }
                switch (msgItem.mAttachmentType) {
                    case WorkingMessage.TEXT:
                        break;
                    case WorkingMessage.VIDEO:
                    case WorkingMessage.IMAGE:
                        if (haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                            .setOnMenuItemClickListener(l);
                        }
                        break;
                    case WorkingMessage.SLIDESHOW:
                    default:
                        menu.add(0, MENU_VIEW_SLIDESHOW, 0, R.string.view_slideshow)
                        .setOnMenuItemClickListener(l);
                        if (haveSomethingToCopyToSDCard(msgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
                            .setOnMenuItemClickListener(l);
                        }
                        if (haveSomethingToCopyToDrmProvider(msgItem.mMsgId)) {
                            menu.add(0, MENU_COPY_TO_DRM_PROVIDER, 0,
                                    getDrmMimeMenuStringRsrc(msgItem.mMsgId))
                            .setOnMenuItemClickListener(l);
                        }
                        break;
                }
            } else {
                if (getRecipients().size() == 1 &&
                        (msgItem.mBoxId == Sms.MESSAGE_TYPE_OUTBOX ||
                        msgItem.mBoxId == Sms.MESSAGE_TYPE_FAILED)) {
                    menu.add(0, MENU_EDIT_MESSAGE, 0, R.string.menu_edit)
                            .setOnMenuItemClickListener(l);
                }
            }
            addCallAndContactMenuItems(menu, l, msgItem);
            if (msgItem.isDownloaded()) {
                menu.add(0, MENU_FORWARD_MESSAGE, 0, R.string.menu_forward)
                        .setOnMenuItemClickListener(l);
            }
            if (msgItem.isSms()) {
                menu.add(0, MENU_COPY_MESSAGE_TEXT, 0, R.string.copy_message_text)
                        .setOnMenuItemClickListener(l);
            }
            menu.add(0, MENU_VIEW_MESSAGE_DETAILS, 0, R.string.view_message_details)
                    .setOnMenuItemClickListener(l);
            menu.add(0, MENU_DELETE_MESSAGE, 0, R.string.delete_message)
                    .setOnMenuItemClickListener(l);
            if (msgItem.mDeliveryStatus != MessageItem.DeliveryStatus.NONE || msgItem.mReadReport) {
                menu.add(0, MENU_DELIVERY_REPORT, 0, R.string.view_delivery_report)
                        .setOnMenuItemClickListener(l);
            }
        }
    };
    private void editMessageItem(MessageItem msgItem) {
        if ("sms".equals(msgItem.mType)) {
            editSmsMessageItem(msgItem);
        } else {
            editMmsMessageItem(msgItem);
        }
        if (msgItem.isFailedMessage() && mMsgListAdapter.getCount() <= 1) {
            initRecipientsEditor();
        }
    }
    private void editSmsMessageItem(MessageItem msgItem) {
        synchronized(mConversation) {
            if (mConversation.getMessageCount() <= 1) {
                mConversation.clearThreadId();
            }
        }
        Uri uri = ContentUris.withAppendedId(Sms.CONTENT_URI, msgItem.mMsgId);
        SqliteWrapper.delete(ComposeMessageActivity.this,
                mContentResolver, uri, null, null);
        mWorkingMessage.setText(msgItem.mBody);
    }
    private void editMmsMessageItem(MessageItem msgItem) {
        mWorkingMessage.discard();
        mWorkingMessage = WorkingMessage.load(this, msgItem.mMessageUri);
        mWorkingMessage.setConversation(mConversation);
        mAttachmentEditor.update(mWorkingMessage);
        drawTopPanel();
        mWorkingMessage.setSubject(msgItem.mSubject, false);
        if (mWorkingMessage.hasSubject()) {
            showSubjectEditor(true);
        }
    }
    private void copyToClipboard(String str) {
        ClipboardManager clip =
            (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(str);
    }
    private void forwardMessage(MessageItem msgItem) {
        Intent intent = createIntent(this, 0);
        intent.putExtra("exit_on_sent", true);
        intent.putExtra("forwarded_message", true);
        if (msgItem.mType.equals("sms")) {
            intent.putExtra("sms_body", msgItem.mBody);
        } else {
            SendReq sendReq = new SendReq();
            String subject = getString(R.string.forward_prefix);
            if (msgItem.mSubject != null) {
                subject += msgItem.mSubject;
            }
            sendReq.setSubject(new EncodedStringValue(subject));
            sendReq.setBody(msgItem.mSlideshow.makeCopy(
                    ComposeMessageActivity.this));
            Uri uri = null;
            try {
                PduPersister persister = PduPersister.getPduPersister(this);
                uri = persister.persist(sendReq, Mms.Draft.CONTENT_URI);
            } catch (MmsException e) {
                Log.e(TAG, "Failed to copy message: " + msgItem.mMessageUri, e);
                Toast.makeText(ComposeMessageActivity.this,
                        R.string.cannot_save_message, Toast.LENGTH_SHORT).show();
                return;
            }
            intent.putExtra("msg_uri", uri);
            intent.putExtra("subject", subject);
        }
        intent.setClassName(this, "com.android.mms.ui.ForwardMessageActivity");
        startActivity(intent);
    }
    private final class MsgListMenuClickListener implements MenuItem.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            if (!isCursorValid()) {
                return false;
            }
            Cursor cursor = mMsgListAdapter.getCursor();
            String type = cursor.getString(COLUMN_MSG_TYPE);
            long msgId = cursor.getLong(COLUMN_ID);
            MessageItem msgItem = getMessageItem(type, msgId, true);
            if (msgItem == null) {
                return false;
            }
            switch (item.getItemId()) {
                case MENU_EDIT_MESSAGE:
                    editMessageItem(msgItem);
                    drawBottomPanel();
                    return true;
                case MENU_COPY_MESSAGE_TEXT:
                    copyToClipboard(msgItem.mBody);
                    return true;
                case MENU_FORWARD_MESSAGE:
                    forwardMessage(msgItem);
                    return true;
                case MENU_VIEW_SLIDESHOW:
                    MessageUtils.viewMmsMessageAttachment(ComposeMessageActivity.this,
                            ContentUris.withAppendedId(Mms.CONTENT_URI, msgId), null);
                    return true;
                case MENU_VIEW_MESSAGE_DETAILS: {
                    String messageDetails = MessageUtils.getMessageDetails(
                            ComposeMessageActivity.this, cursor, msgItem.mMessageSize);
                    new AlertDialog.Builder(ComposeMessageActivity.this)
                            .setTitle(R.string.message_details_title)
                            .setMessage(messageDetails)
                            .setPositiveButton(android.R.string.ok, null)
                            .setCancelable(true)
                            .show();
                    return true;
                }
                case MENU_DELETE_MESSAGE: {
                    DeleteMessageListener l = new DeleteMessageListener(
                            msgItem.mMessageUri, msgItem.mLocked);
                    confirmDeleteDialog(l, msgItem.mLocked);
                    return true;
                }
                case MENU_DELIVERY_REPORT:
                    showDeliveryReport(msgId, type);
                    return true;
                case MENU_COPY_TO_SDCARD: {
                    int resId = copyMedia(msgId) ? R.string.copy_to_sdcard_success :
                        R.string.copy_to_sdcard_fail;
                    Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
                    return true;
                }
                case MENU_COPY_TO_DRM_PROVIDER: {
                    int resId = getDrmMimeSavedStringRsrc(msgId, copyToDrmProvider(msgId));
                    Toast.makeText(ComposeMessageActivity.this, resId, Toast.LENGTH_SHORT).show();
                    return true;
                }
                case MENU_LOCK_MESSAGE: {
                    lockMessage(msgItem, true);
                    return true;
                }
                case MENU_UNLOCK_MESSAGE: {
                    lockMessage(msgItem, false);
                    return true;
                }
                default:
                    return false;
            }
        }
    }
    private void lockMessage(MessageItem msgItem, boolean locked) {
        Uri uri;
        if ("sms".equals(msgItem.mType)) {
            uri = Sms.CONTENT_URI;
        } else {
            uri = Mms.CONTENT_URI;
        }
        final Uri lockUri = ContentUris.withAppendedId(uri, msgItem.mMsgId);
        final ContentValues values = new ContentValues(1);
        values.put("locked", locked ? 1 : 0);
        new Thread(new Runnable() {
            public void run() {
                getContentResolver().update(lockUri,
                        values, null, null);
            }
        }).start();
    }
    private boolean haveSomethingToCopyToSDCard(long msgId) {
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return false;
        }
        boolean result = false;
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("[CMA] haveSomethingToCopyToSDCard: part[" + i + "] contentType=" + type);
            }
            if (ContentType.isImageType(type) || ContentType.isVideoType(type) ||
                    ContentType.isAudioType(type)) {
                result = true;
                break;
            }
        }
        return result;
    }
    private boolean haveSomethingToCopyToDrmProvider(long msgId) {
        String mimeType = getDrmMimeType(msgId);
        return isAudioMimeType(mimeType);
    }
    private static class PduBodyCache {
        private static PduBody mLastPduBody;
        private static Uri mLastUri;
        static public PduBody getPduBody(Context context, Uri contentUri) {
            if (contentUri.equals(mLastUri)) {
                return mLastPduBody;
            }
            try {
                mLastPduBody = SlideshowModel.getPduBody(context, contentUri);
                mLastUri = contentUri;
             } catch (MmsException e) {
                 Log.e(TAG, e.getMessage(), e);
                 return null;
             }
             return mLastPduBody;
        }
    };
    private boolean copyToDrmProvider(long msgId) {
        boolean result = true;
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return false;
        }
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (ContentType.isDrmType(type)) {
                result &= copyPartToDrmProvider(part);
            }
        }
        return result;
    }
    private String mimeTypeOfDrmPart(PduPart part) {
        Uri uri = part.getDataUri();
        InputStream input = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;
                DrmRawContent content = new DrmRawContent(fin, fin.available(),
                        DrmRawContent.DRM_MIMETYPE_MESSAGE_STRING);
                String mimeType = content.getContentType();
                return mimeType;
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException caught while opening or reading stream", e);
        } catch (DrmException e) {
            Log.e(TAG, "DrmException caught ", e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught while closing stream", e);
                }
            }
        }
        return null;
    }
    private String getDrmMimeType(long msgId) {
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return null;
        }
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (ContentType.isDrmType(type)) {
                return mimeTypeOfDrmPart(part);
            }
        }
        return null;
    }
    private int getDrmMimeMenuStringRsrc(long msgId) {
        String mimeType = getDrmMimeType(msgId);
        if (isAudioMimeType(mimeType)) {
            return R.string.save_ringtone;
        }
        return 0;
    }
    private int getDrmMimeSavedStringRsrc(long msgId, boolean success) {
        String mimeType = getDrmMimeType(msgId);
        if (isAudioMimeType(mimeType)) {
            return success ? R.string.saved_ringtone : R.string.saved_ringtone_fail;
        }
        return 0;
    }
    private boolean isAudioMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("audio/");
    }
    private boolean isImageMimeType(String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }
    private boolean copyPartToDrmProvider(PduPart part) {
        Uri uri = part.getDataUri();
        InputStream input = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;
                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }
                String title = new String(location);
                int index;
                if ((index = title.indexOf(".")) == -1) {
                    String type = new String(part.getContentType());
                } else {
                    title = title.substring(0, index);
                }
                Intent item = DrmStore.addDrmFile(mContentResolver, fin, title);
                if (item == null) {
                    Log.w(TAG, "unable to add file " + uri + " to DrmProvider");
                    return false;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }
    private boolean copyMedia(long msgId) {
        boolean result = true;
        PduBody body = PduBodyCache.getPduBody(this,
                ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        if (body == null) {
            return false;
        }
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (ContentType.isImageType(type) || ContentType.isVideoType(type) ||
                    ContentType.isAudioType(type)) {
                result &= copyPart(part, Long.toHexString(msgId));   
            }
        }
        return result;
    }
    private boolean copyPart(PduPart part, String fallback) {
        Uri uri = part.getDataUri();
        InputStream input = null;
        FileOutputStream fout = null;
        try {
            input = mContentResolver.openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;
                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }
                String fileName;
                if (location == null) {
                    fileName = fallback;
                } else {
                    fileName = new String(location);
                }
                String dir = Environment.getExternalStorageDirectory() + "/"
                                + Environment.DIRECTORY_DOWNLOADS  + "/";
                String extension;
                int index;
                if ((index = fileName.indexOf(".")) == -1) {
                    String type = new String(part.getContentType());
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                }
                File file = getUniqueDestination(dir + fileName, extension);
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                    return false;
                }
                fout = new FileOutputStream(file);
                byte[] buffer = new byte[8000];
                int size = 0;
                while ((size=fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, size);
                }
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(file)));
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return false;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return false;
                }
            }
        }
        return true;
    }
    private File getUniqueDestination(String base, String extension) {
        File file = new File(base + "." + extension);
        for (int i = 2; file.exists(); i++) {
            file = new File(base + "_" + i + "." + extension);
        }
        return file;
    }
    private void showDeliveryReport(long messageId, String type) {
        Intent intent = new Intent(this, DeliveryReportActivity.class);
        intent.putExtra("message_id", messageId);
        intent.putExtra("message_type", type);
        startActivity(intent);
    }
    private final IntentFilter mHttpProgressFilter = new IntentFilter(PROGRESS_STATUS_ACTION);
    private final BroadcastReceiver mHttpProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PROGRESS_STATUS_ACTION.equals(intent.getAction())) {
                long token = intent.getLongExtra("token",
                                    SendingProgressTokenManager.NO_TOKEN);
                if (token != mConversation.getThreadId()) {
                    return;
                }
                int progress = intent.getIntExtra("progress", 0);
                switch (progress) {
                    case PROGRESS_START:
                        setProgressBarVisibility(true);
                        break;
                    case PROGRESS_ABORT:
                    case PROGRESS_COMPLETE:
                        setProgressBarVisibility(false);
                        break;
                    default:
                        setProgress(100 * progress);
                }
            }
        }
    };
    private static ContactList sEmptyContactList;
    private ContactList getRecipients() {
        if (isRecipientsEditorVisible()) {
            if (sEmptyContactList == null) {
                sEmptyContactList = new ContactList();
            }
            return sEmptyContactList;
        }
        return mConversation.getRecipients();
    }
    private void updateTitle(ContactList list) {
        String s;
        switch (list.size()) {
            case 0: {
                String recipient = "";
                if (mRecipientsEditor != null) {
                    recipient = mRecipientsEditor.getText().toString();
                }
                s = recipient;
                break;
            }
            case 1: {
                s = list.get(0).getNameAndNumber();
                break;
            }
            default: {
                s = list.formatNames(", ");
                break;
            }
        }
        getWindow().setTitle(s);
    }
    private void initRecipientsEditor() {
        if (isRecipientsEditorVisible()) {
            return;
        }
        ContactList recipients = getRecipients();
        ViewStub stub = (ViewStub)findViewById(R.id.recipients_editor_stub);
        if (stub != null) {
            mRecipientsEditor = (RecipientsEditor) stub.inflate();
        } else {
            mRecipientsEditor = (RecipientsEditor)findViewById(R.id.recipients_editor);
            mRecipientsEditor.setVisibility(View.VISIBLE);
        }
        mRecipientsEditor.setAdapter(new RecipientsAdapter(this));
        mRecipientsEditor.populate(recipients);
        mRecipientsEditor.setOnCreateContextMenuListener(mRecipientsMenuCreateListener);
        mRecipientsEditor.addTextChangedListener(mRecipientsWatcher);
        mRecipientsEditor.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(RECIPIENTS_MAX_LENGTH) });
        mRecipientsEditor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRecipientsEditor.getRecipientCount() == 1) {
                    final InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputManager == null || !inputManager.isFullscreenMode()) {
                        mTextEditor.requestFocus();
                    }
                }
            }
        });
        mRecipientsEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    RecipientsEditor editor = (RecipientsEditor) v;
                    ContactList contacts = editor.constructContactsFromInput();
                    updateTitle(contacts);
                }
            }
        });
        mTopPanel.setVisibility(View.VISIBLE);
    }
    public static boolean cancelFailedToDeliverNotification(Intent intent, Context context) {
        if (MessagingNotification.isFailedToDeliver(intent)) {
            MessagingNotification.cancelNotification(context,
                        MessagingNotification.MESSAGE_FAILED_NOTIFICATION_ID);
            return true;
        }
        return false;
    }
    public static boolean cancelFailedDownloadNotification(Intent intent, Context context) {
        if (MessagingNotification.isFailedToDownload(intent)) {
            MessagingNotification.cancelNotification(context,
                        MessagingNotification.DOWNLOAD_FAILED_NOTIFICATION_ID);
            return true;
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compose_message_activity);
        setProgressBarVisibility(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initResourceRefs();
        mContentResolver = getContentResolver();
        mBackgroundQueryHandler = new BackgroundQueryHandler(mContentResolver);
        initialize(savedInstanceState);
        if (TRACE) {
            android.os.Debug.startMethodTracing("compose");
        }
    }
    private void showSubjectEditor(boolean show) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("showSubjectEditor: " + show);
        }
        if (mSubjectTextEditor == null) {
            if (show == false) {
                return;
            }
            mSubjectTextEditor = (EditText)findViewById(R.id.subject);
        }
        mSubjectTextEditor.setOnKeyListener(show ? mSubjectKeyListener : null);
        if (show) {
            mSubjectTextEditor.addTextChangedListener(mSubjectEditorWatcher);
        } else {
            mSubjectTextEditor.removeTextChangedListener(mSubjectEditorWatcher);
        }
        mSubjectTextEditor.setText(mWorkingMessage.getSubject());
        mSubjectTextEditor.setVisibility(show ? View.VISIBLE : View.GONE);
        hideOrShowTopPanel();
    }
    private void hideOrShowTopPanel() {
        boolean anySubViewsVisible = (isSubjectEditorVisible() || isRecipientsEditorVisible());
        mTopPanel.setVisibility(anySubViewsVisible ? View.VISIBLE : View.GONE);
    }
    public void initialize(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mWorkingMessage = WorkingMessage.createEmpty(this);
        initActivityState(savedInstanceState, intent);
        log("initialize: savedInstanceState = " + savedInstanceState +
                " intent = " + intent +
                " mConversation = " + mConversation);
        if (cancelFailedToDeliverNotification(getIntent(), this)) {
            undeliveredMessageDialog(getMessageDate(null));
        }
        cancelFailedDownloadNotification(getIntent(), this);
        initMessageList();
        boolean isForwardedMessage = false;
        if (!handleSendIntent(intent)) {
            isForwardedMessage = handleForwardedMessage();
            if (!isForwardedMessage) {
                loadDraft();
            }
        }
        mWorkingMessage.setConversation(mConversation);
        if (mConversation.getThreadId() <= 0) {
            hideRecipientEditor();
            initRecipientsEditor();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            hideRecipientEditor();
        }
        updateSendButtonState();
        drawTopPanel();
        drawBottomPanel();
        mAttachmentEditor.update(mWorkingMessage);
        Configuration config = getResources().getConfiguration();
        mIsKeyboardOpen = config.keyboardHidden == KEYBOARDHIDDEN_NO;
        mIsLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        onKeyboardStateChanged(mIsKeyboardOpen);
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("initialize: update title, mConversation=" + mConversation.toString());
        }
        updateTitle(mConversation.getRecipients());
        if (isForwardedMessage && isRecipientsEditorVisible()) {
            mRecipientsEditor.requestFocus();
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Conversation conversation = null;
        mSentMessage = false;
        long threadId = intent.getLongExtra("thread_id", 0);
        Uri intentUri = intent.getData();
        boolean sameThread = false;
        if (threadId > 0) {
            conversation = Conversation.get(this, threadId, false);
        } else {
            if (mConversation.getThreadId() == 0) {
                mWorkingMessage.syncWorkingRecipients();
                sameThread = mConversation.sameRecipient(intentUri);
            }
            if (!sameThread) {
                conversation = Conversation.get(this, intentUri, false);
            }
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("onNewIntent: data=" + intentUri + ", thread_id extra is " + threadId);
            log("     new conversation=" + conversation + ", mConversation=" + mConversation);
        }
        if (conversation != null) {
            conversation.blockMarkAsRead(true);
            sameThread = (conversation.getThreadId() == mConversation.getThreadId() &&
                    conversation.equals(mConversation));
        }
        if (sameThread) {
            log("onNewIntent: same conversation");
        } else {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("onNewIntent: different conversation, initialize...");
            }
            saveDraft();    
            initialize(null);
            loadMessageContent();
        }
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (mWorkingMessage.isDiscarded()) {
            if (mWorkingMessage.isWorthSaving()) {
                mWorkingMessage.unDiscard();    
            } else if (isRecipientsEditorVisible()) {
                goToConversationList();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mConversation.blockMarkAsRead(true);
        initFocus();
        registerReceiver(mHttpProgressReceiver, mHttpProgressFilter);
        loadMessageContent();
        mWorkingMessage.syncWorkingRecipients();
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("onStart: update title, mConversation=" + mConversation.toString());
        }
        updateTitle(mConversation.getRecipients());
    }
    public void loadMessageContent() {
        startMsgListQuery();
        updateSendFailedNotification();
        drawBottomPanel();
    }
    private void updateSendFailedNotification() {
        final long threadId = mConversation.getThreadId();
        if (threadId <= 0)
            return;
        new Thread(new Runnable() {
            public void run() {
                MessagingNotification.updateSendFailedNotificationForThread(
                        ComposeMessageActivity.this, threadId);
            }
        }).run();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("recipients", getRecipients().serialize());
        mWorkingMessage.writeStateToBundle(outState);
        if (mExitOnSent) {
            outState.putBoolean("exit_on_sent", mExitOnSent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        addRecipientsListeners();
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("onResume: update title, mConversation=" + mConversation.toString());
        }
        mMessageListItemHandler.postDelayed(new Runnable() {
            public void run() {
                ContactList recipients = isRecipientsEditorVisible() ?
                        mRecipientsEditor.constructContactsFromInput() : getRecipients();
                updateTitle(recipients);
            }
        }, 100);
    }
    @Override
    protected void onPause() {
        super.onPause();
        removeRecipientsListeners();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mConversation.blockMarkAsRead(false);
        if (mMsgListAdapter != null) {
            mMsgListAdapter.changeCursor(null);
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("onStop: save draft");
        }
        saveDraft();
        unregisterReceiver(mHttpProgressReceiver);
    }
    @Override
    protected void onDestroy() {
        if (TRACE) {
            android.os.Debug.stopMethodTracing();
        }
        super.onDestroy();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (LOCAL_LOGV) {
            Log.v(TAG, "onConfigurationChanged: " + newConfig);
        }
        mIsKeyboardOpen = newConfig.keyboardHidden == KEYBOARDHIDDEN_NO;
        boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (mIsLandscape != isLandscape) {
            mIsLandscape = isLandscape;
            mAttachmentEditor.update(mWorkingMessage);
        }
        onKeyboardStateChanged(mIsKeyboardOpen);
    }
    private void onKeyboardStateChanged(boolean isKeyboardOpen) {
        if (isKeyboardOpen) {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusableInTouchMode(true);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusableInTouchMode(true);
            }
            mTextEditor.setFocusableInTouchMode(true);
            mTextEditor.setHint(R.string.type_to_compose_text_enter_to_send);
        } else {
            if (mRecipientsEditor != null) {
                mRecipientsEditor.setFocusable(false);
            }
            if (mSubjectTextEditor != null) {
                mSubjectTextEditor.setFocusable(false);
            }
            mTextEditor.setFocusable(false);
            mTextEditor.setHint(R.string.open_keyboard_to_compose_message);
        }
    }
    @Override
    public void onUserInteraction() {
        checkPendingNotification();
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            checkPendingNotification();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                if ((mMsgListAdapter != null) && mMsgListView.isFocused()) {
                    Cursor cursor;
                    try {
                        cursor = (Cursor) mMsgListView.getSelectedItem();
                    } catch (ClassCastException e) {
                        Log.e(TAG, "Unexpected ClassCastException.", e);
                        return super.onKeyDown(keyCode, event);
                    }
                    if (cursor != null) {
                        boolean locked = cursor.getInt(COLUMN_MMS_LOCKED) != 0;
                        DeleteMessageListener l = new DeleteMessageListener(
                                cursor.getLong(COLUMN_ID),
                                cursor.getString(COLUMN_MSG_TYPE),
                                locked);
                        confirmDeleteDialog(l, locked);
                        return true;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_BACK:
                exitComposeMessageActivity(new Runnable() {
                    public void run() {
                        finish();
                    }
                });
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exitComposeMessageActivity(final Runnable exit) {
        if (!mWorkingMessage.isWorthSaving()) {
            exit.run();
            return;
        }
        if (isRecipientsEditorVisible() &&
                !mRecipientsEditor.hasValidRecipient(mWorkingMessage.requiresMms())) {
            MessageUtils.showDiscardDraftConfirmDialog(this, new DiscardDraftListener());
            return;
        }
        mToastForDraftSave = true;
        exit.run();
    }
    private void goToConversationList() {
        finish();
        startActivity(new Intent(this, ConversationList.class));
    }
    private void hideRecipientEditor() {
        if (mRecipientsEditor != null) {
            mRecipientsEditor.removeTextChangedListener(mRecipientsWatcher);
            mRecipientsEditor.setVisibility(View.GONE);
            hideOrShowTopPanel();
        }
    }
    private boolean isRecipientsEditorVisible() {
        return (null != mRecipientsEditor)
                    && (View.VISIBLE == mRecipientsEditor.getVisibility());
    }
    private boolean isSubjectEditorVisible() {
        return (null != mSubjectTextEditor)
                    && (View.VISIBLE == mSubjectTextEditor.getVisibility());
    }
    public void onAttachmentChanged() {
        runOnUiThread(new Runnable() {
            public void run() {
                drawBottomPanel();
                updateSendButtonState();
                mAttachmentEditor.update(mWorkingMessage);
            }
        });
    }
    public void onProtocolChanged(final boolean mms) {
        runOnUiThread(new Runnable() {
            public void run() {
                toastConvertInfo(mms);
                setSendButtonText(mms);
            }
        });
    }
    private void setSendButtonText(boolean isMms) {
        Button sendButton = mSendButton;
        sendButton.setText(R.string.send);
        if (isMms) {
            sendButton.append("\n");
            SpannableString spannable = new SpannableString(getString(R.string.mms));
            int mmsTextSize = (int) (sendButton.getTextSize() * 0.75f);
            spannable.setSpan(new AbsoluteSizeSpan(mmsTextSize), 0, spannable.length(), 0);
            sendButton.append(spannable);
            mTextCounter.setText("");
        }
    }
    Runnable mResetMessageRunnable = new Runnable() {
        public void run() {
            resetMessage();
        }
    };
    public void onPreMessageSent() {
        runOnUiThread(mResetMessageRunnable);
    }
    public void onMessageSent() {
        if (mMsgListAdapter.getCount() == 0) {
            startMsgListQuery();
        }
    }
    public void onMaxPendingMessagesReached() {
        saveDraft();
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(ComposeMessageActivity.this, R.string.too_many_unsent_mms,
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    public void onAttachmentError(final int error) {
        runOnUiThread(new Runnable() {
            public void run() {
                handleAddAttachmentError(error, R.string.type_picture);
                onMessageSent();        
            }
        });
    }
    private boolean isRecipientCallable() {
        ContactList recipients = getRecipients();
        return (recipients.size() == 1 && !recipients.containsEmail());
    }
    private void dialRecipient() {
        String number = getRecipients().get(0).getNumber();
        Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        startActivity(dialIntent);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (isRecipientCallable()) {
            menu.add(0, MENU_CALL_RECIPIENT, 0, R.string.menu_call).setIcon(
                    R.drawable.ic_menu_call);
        }
        ContactList recipients = getRecipients();
        if (recipients.size() == 1 && recipients.get(0).existsInDatabase()) {
            menu.add(0, MENU_VIEW_CONTACT, 0, R.string.menu_view_contact).setIcon(
                    R.drawable.ic_menu_contact);
        }
        if (MmsConfig.getMmsEnabled()) {
            if (!isSubjectEditorVisible()) {
                menu.add(0, MENU_ADD_SUBJECT, 0, R.string.add_subject).setIcon(
                        R.drawable.ic_menu_edit);
            }
            if (!mWorkingMessage.hasAttachment()) {
                menu.add(0, MENU_ADD_ATTACHMENT, 0, R.string.add_attachment).setIcon(
                        R.drawable.ic_menu_attachment);
            }
        }
        if (isPreparedForSending()) {
            menu.add(0, MENU_SEND, 0, R.string.send).setIcon(android.R.drawable.ic_menu_send);
        }
        menu.add(0, MENU_INSERT_SMILEY, 0, R.string.menu_insert_smiley).setIcon(
                R.drawable.ic_menu_emoticons);
        if (mMsgListAdapter.getCount() > 0) {
            Cursor cursor = mMsgListAdapter.getCursor();
            if ((null != cursor) && (cursor.getCount() > 0)) {
                menu.add(0, MENU_DELETE_THREAD, 0, R.string.delete_thread).setIcon(
                    android.R.drawable.ic_menu_delete);
            }
        } else {
            menu.add(0, MENU_DISCARD, 0, R.string.discard).setIcon(android.R.drawable.ic_menu_delete);
        }
        menu.add(0, MENU_CONVERSATION_LIST, 0, R.string.all_threads).setIcon(
                R.drawable.ic_menu_friendslist);
        buildAddAddressToContactMenuItem(menu);
        return true;
    }
    private void buildAddAddressToContactMenuItem(Menu menu) {
        for (Contact c : getRecipients()) {
            if (!c.existsInDatabase() && canAddToContacts(c)) {
                Intent intent = ConversationList.createAddContactIntent(c.getNumber());
                menu.add(0, MENU_ADD_ADDRESS_TO_CONTACTS, 0, R.string.menu_add_to_contacts)
                    .setIcon(android.R.drawable.ic_menu_add)
                    .setIntent(intent);
                break;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_SUBJECT:
                showSubjectEditor(true);
                mWorkingMessage.setSubject("", true);
                mSubjectTextEditor.requestFocus();
                break;
            case MENU_ADD_ATTACHMENT:
                showAddAttachmentDialog(false);
                break;
            case MENU_DISCARD:
                mWorkingMessage.discard();
                finish();
                break;
            case MENU_SEND:
                if (isPreparedForSending()) {
                    confirmSendMessageIfNeeded();
                }
                break;
            case MENU_SEARCH:
                onSearchRequested();
                break;
            case MENU_DELETE_THREAD:
                confirmDeleteThread(mConversation.getThreadId());
                break;
            case MENU_CONVERSATION_LIST:
                exitComposeMessageActivity(new Runnable() {
                    public void run() {
                        goToConversationList();
                    }
                });
                break;
            case MENU_CALL_RECIPIENT:
                dialRecipient();
                break;
            case MENU_INSERT_SMILEY:
                showSmileyDialog();
                break;
            case MENU_VIEW_CONTACT: {
                ContactList list = getRecipients();
                if (list.size() == 1 && list.get(0).existsInDatabase()) {
                    Uri contactUri = list.get(0).getUri();
                    Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(intent);
                }
                break;
            }
            case MENU_ADD_ADDRESS_TO_CONTACTS:
                mAddContactIntent = item.getIntent();
                startActivityForResult(mAddContactIntent, REQUEST_CODE_ADD_CONTACT);
                break;
        }
        return true;
    }
    private void confirmDeleteThread(long threadId) {
        Conversation.startQueryHaveLockedMessages(mBackgroundQueryHandler,
                threadId, ConversationList.HAVE_LOCKED_MESSAGES_TOKEN);
    }
    private int getVideoCaptureDurationLimit() {
        return CamcorderProfile.get(CamcorderProfile.QUALITY_LOW).duration;
    }
    private void addAttachment(int type, boolean replace) {
        int currentSlideSize = 0;
        SlideshowModel slideShow = mWorkingMessage.getSlideshow();
        if (replace && slideShow != null) {
            SlideModel slide = slideShow.get(0);
            currentSlideSize = slide.getSlideSize();
        }
        switch (type) {
            case AttachmentTypeSelectorAdapter.ADD_IMAGE:
                MessageUtils.selectImage(this, REQUEST_CODE_ATTACH_IMAGE);
                break;
            case AttachmentTypeSelectorAdapter.TAKE_PICTURE: {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Mms.ScrapSpace.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
                break;
            }
            case AttachmentTypeSelectorAdapter.ADD_VIDEO:
                MessageUtils.selectVideo(this, REQUEST_CODE_ATTACH_VIDEO);
                break;
            case AttachmentTypeSelectorAdapter.RECORD_VIDEO: {
                long sizeLimit = MmsConfig.getMaxMessageSize() - SlideshowModel.SLIDESHOW_SLOP;
                if (slideShow != null) {
                    sizeLimit -= slideShow.getCurrentMessageSize();
                    sizeLimit += currentSlideSize;
                }
                if (sizeLimit > 0) {
                    int durationLimit = getVideoCaptureDurationLimit();
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    intent.putExtra("android.intent.extra.sizeLimit", sizeLimit);
                    intent.putExtra("android.intent.extra.durationLimit", durationLimit);
                    startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
                }
                else {
                    Toast.makeText(this,
                            getString(R.string.message_too_big_for_video),
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case AttachmentTypeSelectorAdapter.ADD_SOUND:
                MessageUtils.selectAudio(this, REQUEST_CODE_ATTACH_SOUND);
                break;
            case AttachmentTypeSelectorAdapter.RECORD_SOUND:
                MessageUtils.recordSound(this, REQUEST_CODE_RECORD_SOUND);
                break;
            case AttachmentTypeSelectorAdapter.ADD_SLIDESHOW:
                editSlideshow();
                break;
            default:
                break;
        }
    }
    private void showAddAttachmentDialog(final boolean replace) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_dialog_attach);
        builder.setTitle(R.string.add_attachment);
        if (mAttachmentTypeSelectorAdapter == null) {
            mAttachmentTypeSelectorAdapter = new AttachmentTypeSelectorAdapter(
                    this, AttachmentTypeSelectorAdapter.MODE_WITH_SLIDESHOW);
        }
        builder.setAdapter(mAttachmentTypeSelectorAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                addAttachment(mAttachmentTypeSelectorAdapter.buttonToCommand(which), replace);
            }
        });
        builder.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            log("onActivityResult: requestCode=" + requestCode
                    + ", resultCode=" + resultCode + ", data=" + data);
        }
        mWaitingForSubActivity = false;          
        if (mWorkingMessage.isFakeMmsForDraft()) {
            mWorkingMessage.removeFakeMmsForDraft();
        }
        if (requestCode != REQUEST_CODE_TAKE_PICTURE) {
            if (data == null) {
                return;
            }
        } else if (resultCode != RESULT_OK){
            if (DEBUG) log("onActivityResult: bail due to resultCode=" + resultCode);
            return;
        }
        switch(requestCode) {
            case REQUEST_CODE_CREATE_SLIDESHOW:
                if (data != null) {
                    WorkingMessage newMessage = WorkingMessage.load(this, data.getData());
                    if (newMessage != null) {
                        mWorkingMessage = newMessage;
                        mWorkingMessage.setConversation(mConversation);
                        mAttachmentEditor.update(mWorkingMessage);
                        drawTopPanel();
                        updateSendButtonState();
                    }
                }
                break;
            case REQUEST_CODE_TAKE_PICTURE: {
                File file = new File(Mms.ScrapSpace.SCRAP_FILE_PATH);
                Uri uri = Uri.fromFile(file);
                addImage(uri, false);
                break;
            }
            case REQUEST_CODE_ATTACH_IMAGE: {
                addImage(data.getData(), false);
                break;
            }
            case REQUEST_CODE_TAKE_VIDEO:
            case REQUEST_CODE_ATTACH_VIDEO:
                addVideo(data.getData(), false);
                break;
            case REQUEST_CODE_ATTACH_SOUND: {
                Uri uri = (Uri) data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (Settings.System.DEFAULT_RINGTONE_URI.equals(uri)) {
                    break;
                }
                addAudio(uri);
                break;
            }
            case REQUEST_CODE_RECORD_SOUND:
                addAudio(data.getData());
                break;
            case REQUEST_CODE_ECM_EXIT_DIALOG:
                boolean outOfEmergencyMode = data.getBooleanExtra(EXIT_ECM_RESULT, false);
                if (outOfEmergencyMode) {
                    sendMessage(false);
                }
                break;
            case REQUEST_CODE_ADD_CONTACT:
                if (mAddContactIntent != null) {
                    String address =
                        mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.EMAIL);
                    if (address == null) {
                        address =
                            mAddContactIntent.getStringExtra(ContactsContract.Intents.Insert.PHONE);
                    }
                    if (address != null) {
                        Contact contact = Contact.get(address, false);
                        if (contact != null) {
                            contact.reload();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    private final ResizeImageResultCallback mResizeImageCallback = new ResizeImageResultCallback() {
        public void onResizeResult(PduPart part, boolean append) {
            if (part == null) {
                handleAddAttachmentError(WorkingMessage.UNKNOWN_ERROR, R.string.type_picture);
                return;
            }
            Context context = ComposeMessageActivity.this;
            PduPersister persister = PduPersister.getPduPersister(context);
            int result;
            Uri messageUri = mWorkingMessage.saveAsMms(true);
            try {
                Uri dataUri = persister.persistPart(part, ContentUris.parseId(messageUri));
                result = mWorkingMessage.setAttachment(WorkingMessage.IMAGE, dataUri, append);
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("ResizeImageResultCallback: dataUri=" + dataUri);
                }
            } catch (MmsException e) {
                result = WorkingMessage.UNKNOWN_ERROR;
            }
            handleAddAttachmentError(result, R.string.type_picture);
        }
    };
    private void handleAddAttachmentError(final int error, final int mediaTypeStringId) {
        if (error == WorkingMessage.OK) {
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                Resources res = getResources();
                String mediaType = res.getString(mediaTypeStringId);
                String title, message;
                switch(error) {
                case WorkingMessage.UNKNOWN_ERROR:
                    message = res.getString(R.string.failed_to_add_media, mediaType);
                    Toast.makeText(ComposeMessageActivity.this, message, Toast.LENGTH_SHORT).show();
                    return;
                case WorkingMessage.UNSUPPORTED_TYPE:
                    title = res.getString(R.string.unsupported_media_format, mediaType);
                    message = res.getString(R.string.select_different_media, mediaType);
                    break;
                case WorkingMessage.MESSAGE_SIZE_EXCEEDED:
                    title = res.getString(R.string.exceed_message_size_limitation, mediaType);
                    message = res.getString(R.string.failed_to_add_media, mediaType);
                    break;
                case WorkingMessage.IMAGE_TOO_LARGE:
                    title = res.getString(R.string.failed_to_resize_image);
                    message = res.getString(R.string.resize_image_error_information);
                    break;
                default:
                    throw new IllegalArgumentException("unknown error " + error);
                }
                MessageUtils.showErrorDialog(ComposeMessageActivity.this, title, message);
            }
        });
    }
    private void addImage(Uri uri, boolean append) {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("addImage: append=" + append + ", uri=" + uri);
        }
        int result = mWorkingMessage.setAttachment(WorkingMessage.IMAGE, uri, append);
        if (result == WorkingMessage.IMAGE_TOO_LARGE ||
            result == WorkingMessage.MESSAGE_SIZE_EXCEEDED) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("addImage: resize image " + uri);
            }
            MessageUtils.resizeImageAsync(this,
                    uri, mAttachmentEditorHandler, mResizeImageCallback, append);
            return;
        }
        handleAddAttachmentError(result, R.string.type_picture);
    }
    private void addVideo(Uri uri, boolean append) {
        if (uri != null) {
            int result = mWorkingMessage.setAttachment(WorkingMessage.VIDEO, uri, append);
            handleAddAttachmentError(result, R.string.type_video);
        }
    }
    private void addAudio(Uri uri) {
        int result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, uri, false);
        handleAddAttachmentError(result, R.string.type_audio);
    }
    private boolean handleForwardedMessage() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("forwarded_message", false) == false) {
            return false;
        }
        Uri uri = intent.getParcelableExtra("msg_uri");
        if (Log.isLoggable(LogTag.APP, Log.DEBUG)) {
            log("handle forwarded message " + uri);
        }
        if (uri != null) {
            mWorkingMessage = WorkingMessage.load(this, uri);
            mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
        } else {
            mWorkingMessage.setText(intent.getStringExtra("sms_body"));
        }
        mMsgListAdapter.changeCursor(null);
        return true;
    }
    private boolean handleSendIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return false;
        }
        final String mimeType = intent.getType();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
                addAttachment(mimeType, uri, false);
                return true;
            } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
                mWorkingMessage.setText(extras.getString(Intent.EXTRA_TEXT));
                return true;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) &&
                extras.containsKey(Intent.EXTRA_STREAM)) {
            SlideshowModel slideShow = mWorkingMessage.getSlideshow();
            final ArrayList<Parcelable> uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
            int currentSlideCount = slideShow != null ? slideShow.size() : 0;
            int importCount = uris.size();
            if (importCount + currentSlideCount > SlideshowEditor.MAX_SLIDE_NUM) {
                importCount = Math.min(SlideshowEditor.MAX_SLIDE_NUM - currentSlideCount,
                        importCount);
                Toast.makeText(ComposeMessageActivity.this,
                        getString(R.string.too_many_attachments,
                                SlideshowEditor.MAX_SLIDE_NUM, importCount),
                                Toast.LENGTH_LONG).show();
            }
            final AlertDialog dialog = new AlertDialog.Builder(ComposeMessageActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.adding_attachments_title)
                .setMessage(R.string.adding_attachments)
                .create();
            final Runnable showProgress = new Runnable() {
                public void run() {
                    dialog.show();
                }
            };
            mAttachmentEditorHandler.postDelayed(showProgress, 1000);
            final int numberToImport = importCount;
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < numberToImport; i++) {
                        Parcelable uri = uris.get(i);
                        addAttachment(mimeType, (Uri) uri, true);
                    }
                    mAttachmentEditorHandler.removeCallbacks(showProgress);
                    dialog.dismiss();
                }
            }).start();
            return true;
        }
        return false;
    }
    private static final String mVideoUri = Video.Media.getContentUri("external").toString();
    private static final String mImageUri = Images.Media.getContentUri("external").toString();
    private void addAttachment(String type, Uri uri, boolean append) {
        if (uri != null) {
            boolean wildcard = "*
    private void ensureCorrectButtonHeight() {
        int currentTextLines = mTextEditor.getLineCount();
        if (currentTextLines <= 2) {
            mTextCounter.setVisibility(View.GONE);
        }
        else if (currentTextLines > 2 && mTextCounter.getVisibility() == View.GONE) {
            mTextCounter.setVisibility(View.INVISIBLE);
        }
    }
    private final TextWatcher mSubjectEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mWorkingMessage.setSubject(s, true);
        }
        public void afterTextChanged(Editable s) { }
    };
    private void initResourceRefs() {
        mMsgListView = (MessageListView) findViewById(R.id.history);
        mMsgListView.setDivider(null);      
        mBottomPanel = findViewById(R.id.bottom_panel);
        mTextEditor = (EditText) findViewById(R.id.embedded_text_editor);
        mTextEditor.setOnEditorActionListener(this);
        mTextEditor.addTextChangedListener(mTextEditorWatcher);
        mTextCounter = (TextView) findViewById(R.id.text_counter);
        mSendButton = (Button) findViewById(R.id.send_button);
        mSendButton.setOnClickListener(this);
        mTopPanel = findViewById(R.id.recipients_subject_linear);
        mTopPanel.setFocusable(false);
        mAttachmentEditor = (AttachmentEditor) findViewById(R.id.attachment_editor);
        mAttachmentEditor.setHandler(mAttachmentEditorHandler);
    }
    private void confirmDeleteDialog(OnClickListener listener, boolean locked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(locked ? R.string.confirm_dialog_locked_title :
            R.string.confirm_dialog_title);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(true);
        builder.setMessage(locked ? R.string.confirm_delete_locked_message :
                    R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.delete, listener);
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
    void undeliveredMessageDialog(long date) {
        String body;
        LinearLayout dialog = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.retry_sending_dialog, null);
        if (date >= 0) {
            body = getString(R.string.undelivered_msg_dialog_body,
                    MessageUtils.formatTimeStampString(this, date));
        } else {
            body = getString(R.string.undelivered_sms_dialog_body);
        }
        ((TextView) dialog.findViewById(R.id.body_text_view)).setText(body);
        Toast undeliveredDialog = new Toast(this);
        undeliveredDialog.setView(dialog);
        undeliveredDialog.setDuration(Toast.LENGTH_LONG);
        undeliveredDialog.show();
    }
    private void startMsgListQuery() {
        Uri conversationUri = mConversation.getUri();
        if (conversationUri == null) {
            return;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("startMsgListQuery for " + conversationUri);
        }
        mBackgroundQueryHandler.cancelOperation(MESSAGE_LIST_QUERY_TOKEN);
        try {
            mBackgroundQueryHandler.startQuery(
                    MESSAGE_LIST_QUERY_TOKEN, null, conversationUri,
                    PROJECTION, null, null, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(this, e);
        }
    }
    private void initMessageList() {
        if (mMsgListAdapter != null) {
            return;
        }
        String highlightString = getIntent().getStringExtra("highlight");
        Pattern highlight = highlightString == null
            ? null
            : Pattern.compile("\\b" + Pattern.quote(highlightString), Pattern.CASE_INSENSITIVE);
        mMsgListAdapter = new MessageListAdapter(this, null, mMsgListView, true, highlight);
        mMsgListAdapter.setOnDataSetChangedListener(mDataSetChangedListener);
        mMsgListAdapter.setMsgListItemHandler(mMessageListItemHandler);
        mMsgListView.setAdapter(mMsgListAdapter);
        mMsgListView.setItemsCanFocus(false);
        mMsgListView.setVisibility(View.VISIBLE);
        mMsgListView.setOnCreateContextMenuListener(mMsgListMenuCreateListener);
        mMsgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    ((MessageListItem) view).onMessageListItemClick();
                }
            }
        });
    }
    private void loadDraft() {
        if (mWorkingMessage.isWorthSaving()) {
            Log.w(TAG, "loadDraft() called with non-empty working message");
            return;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("loadDraft: call WorkingMessage.loadDraft");
        }
        mWorkingMessage = WorkingMessage.loadDraft(this, mConversation);
    }
    private void saveDraft() {
        if (mWorkingMessage.isDiscarded()) {
            return;
        }
        if (!mWaitingForSubActivity && !mWorkingMessage.isWorthSaving()) {
            if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                log("saveDraft: not worth saving, discard WorkingMessage and bail");
            }
            mWorkingMessage.discard();
            return;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("saveDraft: call WorkingMessage.saveDraft");
        }
        mWorkingMessage.saveDraft();
        if (mToastForDraftSave) {
            Toast.makeText(this, R.string.message_saved_as_draft,
                    Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isPreparedForSending() {
        int recipientCount = recipientCount();
        return recipientCount > 0 && recipientCount <= MmsConfig.getRecipientLimit() &&
            (mWorkingMessage.hasAttachment() || mWorkingMessage.hasText());
    }
    private int recipientCount() {
        int recipientCount;
        if (isRecipientsEditorVisible()) {
            recipientCount = mRecipientsEditor.getRecipientCount();
        } else {
            recipientCount = getRecipients().size();
        }
        return recipientCount;
    }
    private void sendMessage(boolean bCheckEcmMode) {
        if (bCheckEcmMode) {
            String inEcm = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE);
            if (Boolean.parseBoolean(inEcm)) {
                try {
                    startActivityForResult(
                            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                            REQUEST_CODE_ECM_EXIT_DIALOG);
                    return;
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Cannot find EmergencyCallbackModeExitDialog", e);
                }
            }
        }
        if (!mSendingMessage) {
            removeRecipientsListeners();
            mWorkingMessage.send();
            mSentMessage = true;
            mSendingMessage = true;
            addRecipientsListeners();
        }
        if (mExitOnSent) {
            finish();
        }
    }
    private void resetMessage() {
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            log("resetMessage");
        }
        mAttachmentEditor.hideView();
        showSubjectEditor(false);
        mTextEditor.requestFocus();
        mTextEditor.removeTextChangedListener(mTextEditorWatcher);
        TextKeyListener.clear(mTextEditor.getText());
        mWorkingMessage = WorkingMessage.createEmpty(this);
        mWorkingMessage.setConversation(mConversation);
        hideRecipientEditor();
        drawBottomPanel();
        updateSendButtonState();
        mTextEditor.addTextChangedListener(mTextEditorWatcher);
        if (mIsLandscape) {
            InputMethodManager inputMethodManager =
                (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mTextEditor.getWindowToken(), 0);
        }
        mLastRecipientCount = 0;
        mSendingMessage = false;
   }
    private void updateSendButtonState() {
        boolean enable = false;
        if (isPreparedForSending()) {
            if (!mWorkingMessage.hasSlideshow()) {
                enable = true;
            } else {
                mAttachmentEditor.setCanSend(true);
            }
        } else if (null != mAttachmentEditor){
            mAttachmentEditor.setCanSend(false);
        }
        setSendButtonText(mWorkingMessage.requiresMms());
        mSendButton.setEnabled(enable);
        mSendButton.setFocusable(enable);
    }
    private long getMessageDate(Uri uri) {
        if (uri != null) {
            Cursor cursor = SqliteWrapper.query(this, mContentResolver,
                    uri, new String[] { Mms.DATE }, null, null, null);
            if (cursor != null) {
                try {
                    if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                        return cursor.getLong(0) * 1000L;
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return NO_DATE_FOR_DIALOG;
    }
    private void initActivityState(Bundle bundle, Intent intent) {
        if (bundle != null) {
            String recipients = bundle.getString("recipients");
            mConversation = Conversation.get(this,
                    ContactList.getByNumbers(recipients,
                            false , true ), false);
            addRecipientsListeners();
            mExitOnSent = bundle.getBoolean("exit_on_sent", false);
            mWorkingMessage.readStateFromBundle(bundle);
            return;
        }
        long threadId = intent.getLongExtra("thread_id", 0);
        if (threadId > 0) {
            mConversation = Conversation.get(this, threadId, false);
        } else {
            Uri intentData = intent.getData();
            if (intentData != null) {
                mConversation = Conversation.get(this, intentData, false);
            } else {
                String address = intent.getStringExtra("address");
                if (!TextUtils.isEmpty(address)) {
                    mConversation = Conversation.get(this, ContactList.getByNumbers(address,
                            false , true ), false);
                } else {
                    mConversation = Conversation.createNew(this);
                }
            }
        }
        addRecipientsListeners();
        mExitOnSent = intent.getBooleanExtra("exit_on_sent", false);
        mWorkingMessage.setText(intent.getStringExtra("sms_body"));
        mWorkingMessage.setSubject(intent.getStringExtra("subject"), false);
    }
    private void initFocus() {
        if (!mIsKeyboardOpen) {
            return;
        }
        if (isRecipientsEditorVisible()
                && TextUtils.isEmpty(mRecipientsEditor.getText())
                && !mTextEditor.isFocused()) {
            mRecipientsEditor.requestFocus();
            return;
        }
        mTextEditor.requestFocus();
    }
    private final MessageListAdapter.OnDataSetChangedListener
                    mDataSetChangedListener = new MessageListAdapter.OnDataSetChangedListener() {
        public void onDataSetChanged(MessageListAdapter adapter) {
            mPossiblePendingNotification = true;
        }
        public void onContentChanged(MessageListAdapter adapter) {
            startMsgListQuery();
        }
    };
    private void checkPendingNotification() {
        if (mPossiblePendingNotification && hasWindowFocus()) {
            mConversation.markAsRead();
            mPossiblePendingNotification = false;
        }
    }
    private final class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch(token) {
                case MESSAGE_LIST_QUERY_TOKEN:
                    int newSelectionPos = -1;
                    long targetMsgId = getIntent().getLongExtra("select_id", -1);
                    if (targetMsgId != -1) {
                        cursor.moveToPosition(-1);
                        while (cursor.moveToNext()) {
                            long msgId = cursor.getLong(COLUMN_ID);
                            if (msgId == targetMsgId) {
                                newSelectionPos = cursor.getPosition();
                                break;
                            }
                        }
                    }
                    mMsgListAdapter.changeCursor(cursor);
                    if (newSelectionPos != -1) {
                        mMsgListView.setSelection(newSelectionPos);
                    }
                    if (cursor.getCount() == 0 && !isRecipientsEditorVisible() && !mSentMessage) {
                        initRecipientsEditor();
                    }
                    mTextEditor.requestFocus();
                    mConversation.blockMarkAsRead(false);
                    return;
                case ConversationList.HAVE_LOCKED_MESSAGES_TOKEN:
                    long threadId = (Long)cookie;
                    ConversationList.confirmDeleteThreadDialog(
                            new ConversationList.DeleteThreadListener(threadId,
                                mBackgroundQueryHandler, ComposeMessageActivity.this),
                            threadId == -1,
                            cursor != null && cursor.getCount() > 0,
                            ComposeMessageActivity.this);
                    break;
            }
        }
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            switch(token) {
            case DELETE_MESSAGE_TOKEN:
            case ConversationList.DELETE_CONVERSATION_TOKEN:
                MessagingNotification.nonBlockingUpdateNewMessageIndicator(
                        ComposeMessageActivity.this, false, false);
                updateSendFailedNotification();
                break;
            }
            if (token == ConversationList.DELETE_CONVERSATION_TOKEN) {
                mWorkingMessage.discard();
                Conversation.init(ComposeMessageActivity.this);
                finish();
            }
        }
    }
    private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            int[] icons = SmileyParser.DEFAULT_SMILEY_RES_IDS;
            String[] names = getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_NAMES);
            final String[] texts = getResources().getStringArray(
                    SmileyParser.DEFAULT_SMILEY_TEXTS);
            final int N = names.length;
            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
                boolean added = false;
                for (int j = 0; j < i; j++) {
                    if (icons[i] == icons[j]) {
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    HashMap<String, Object> entry = new HashMap<String, Object>();
                    entry. put("icon", icons[i]);
                    entry. put("name", names[i]);
                    entry.put("text", texts[i]);
                    entries.add(entry);
                }
            }
            final SimpleAdapter a = new SimpleAdapter(
                    this,
                    entries,
                    R.layout.smiley_menu_item,
                    new String[] {"icon", "name", "text"},
                    new int[] {R.id.smiley_icon, R.id.smiley_name, R.id.smiley_text});
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = getResources().getDrawable((Integer)data);
                        ((ImageView)view).setImageDrawable(img);
                        return true;
                    }
                    return false;
                }
            };
            a.setViewBinder(viewBinder);
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle(getString(R.string.menu_insert_smiley));
            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) a.getItem(which);
                    mTextEditor.append((String)item.get("text"));
                    dialog.dismiss();
                }
            });
            mSmileyDialog = b.create();
        }
        mSmileyDialog.show();
    }
    public void onUpdate(final Contact updated) {
        mMessageListItemHandler.post(new Runnable() {
            public void run() {
                ContactList recipients = isRecipientsEditorVisible() ?
                        mRecipientsEditor.constructContactsFromInput() : getRecipients();
                if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
                    log("[CMA] onUpdate contact updated: " + updated);
                    log("[CMA] onUpdate recipients: " + recipients);
                }
                updateTitle(recipients);
                ComposeMessageActivity.this.mMsgListAdapter.notifyDataSetChanged();
                if (mRecipientsEditor != null) {
                    mRecipientsEditor.populate(recipients);
                }
            }
        });
    }
    private void addRecipientsListeners() {
        Contact.addListener(this);
    }
    private void removeRecipientsListeners() {
        Contact.removeListener(this);
    }
    public static Intent createIntent(Context context, long threadId) {
        Intent intent = new Intent(context, ComposeMessageActivity.class);
        if (threadId > 0) {
            intent.setData(Conversation.getUri(threadId));
        }
        return intent;
   }
}
