public class MessageCompose extends Activity implements OnClickListener, OnFocusChangeListener {
    private static final String ACTION_REPLY = "com.android.email.intent.action.REPLY";
    private static final String ACTION_REPLY_ALL = "com.android.email.intent.action.REPLY_ALL";
    private static final String ACTION_FORWARD = "com.android.email.intent.action.FORWARD";
    private static final String ACTION_EDIT_DRAFT = "com.android.email.intent.action.EDIT_DRAFT";
    private static final String EXTRA_ACCOUNT_ID = "account_id";
    private static final String EXTRA_MESSAGE_ID = "message_id";
    private static final String STATE_KEY_CC_SHOWN =
        "com.android.email.activity.MessageCompose.ccShown";
    private static final String STATE_KEY_BCC_SHOWN =
        "com.android.email.activity.MessageCompose.bccShown";
    private static final String STATE_KEY_QUOTED_TEXT_SHOWN =
        "com.android.email.activity.MessageCompose.quotedTextShown";
    private static final String STATE_KEY_SOURCE_MESSAGE_PROCED =
        "com.android.email.activity.MessageCompose.stateKeySourceMessageProced";
    private static final String STATE_KEY_DRAFT_ID =
        "com.android.email.activity.MessageCompose.draftId";
    private static final int MSG_UPDATE_TITLE = 3;
    private static final int MSG_SKIPPED_ATTACHMENTS = 4;
    private static final int MSG_DISCARDED_DRAFT = 6;
    private static final int ACTIVITY_REQUEST_PICK_ATTACHMENT = 1;
    private static final String[] ATTACHMENT_META_NAME_PROJECTION = {
        OpenableColumns.DISPLAY_NAME
    };
    private static final int ATTACHMENT_META_NAME_COLUMN_DISPLAY_NAME = 0;
    private static final String[] ATTACHMENT_META_SIZE_PROJECTION = {
        OpenableColumns.SIZE
    };
    private static final int ATTACHMENT_META_SIZE_COLUMN_SIZE = 0;
    private static boolean sSaveInProgress = false;
    private static final Object sSaveInProgressCondition = new Object();
    private Account mAccount;
    private Message mDraft = new Message();
    private Message mSource;
    private String mAction;
    private boolean mSourceMessageProcessed = false;
    private MultiAutoCompleteTextView mToView;
    private MultiAutoCompleteTextView mCcView;
    private MultiAutoCompleteTextView mBccView;
    private EditText mSubjectView;
    private EditText mMessageContentView;
    private Button mSendButton;
    private Button mDiscardButton;
    private Button mSaveButton;
    private LinearLayout mAttachments;
    private View mQuotedTextBar;
    private ImageButton mQuotedTextDelete;
    private WebView mQuotedText;
    private TextView mLeftTitle;
    private TextView mRightTitle;
    private Controller mController;
    private Listener mListener;
    private boolean mDraftNeedsSaving;
    private boolean mMessageLoaded;
    private AsyncTask mLoadAttachmentsTask;
    private AsyncTask mSaveMessageTask;
    private AsyncTask mLoadMessageTask;
    private EmailAddressAdapter mAddressAdapterTo;
    private EmailAddressAdapter mAddressAdapterCc;
    private EmailAddressAdapter mAddressAdapterBcc;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_TITLE:
                    updateTitle();
                    break;
                case MSG_SKIPPED_ATTACHMENTS:
                    Toast.makeText(
                            MessageCompose.this,
                            getString(R.string.message_compose_attachments_skipped_toast),
                            Toast.LENGTH_LONG).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };
    public static void actionCompose(Context context, long accountId) {
       try {
           Intent i = new Intent(context, MessageCompose.class);
           i.putExtra(EXTRA_ACCOUNT_ID, accountId);
           context.startActivity(i);
       } catch (ActivityNotFoundException anfe) {
           Email.log(anfe.toString());
       }
    }
    public static boolean actionCompose(Context context, String uriString, long accountId) {
        try {
            Intent i = new Intent(context, MessageCompose.class);
            i.setAction(Intent.ACTION_SEND);
            i.setData(Uri.parse(uriString));
            i.putExtra(EXTRA_ACCOUNT_ID, accountId);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException anfe) {
            Email.log(anfe.toString());
            return false;
        }
    }
    public static void actionReply(Context context, long messageId, boolean replyAll) {
        startActivityWithMessage(context, replyAll ? ACTION_REPLY_ALL : ACTION_REPLY, messageId);
    }
    public static void actionForward(Context context, long messageId) {
        startActivityWithMessage(context, ACTION_FORWARD, messageId);
    }
    public static void actionEditDraft(Context context, long messageId) {
        startActivityWithMessage(context, ACTION_EDIT_DRAFT, messageId);
    }
    private static void startActivityWithMessage(Context context, String action, long messageId) {
        Intent i = new Intent(context, MessageCompose.class);
        i.putExtra(EXTRA_MESSAGE_ID, messageId);
        i.setAction(action);
        context.startActivity(i);
    }
    private void setAccount(Intent intent) {
        long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        if (accountId == -1) {
            accountId = Account.getDefaultAccountId(this);
        }
        if (accountId == -1) {
            AccountFolderList.actionShowAccounts(this);
            finish();
        } else {
            setAccount(Account.restoreAccountWithId(this, accountId));
        }
    }
    private void setAccount(Account account) {
        mAccount = account;
        if (account != null) {
            mRightTitle.setText(account.mDisplayName);
            mAddressAdapterTo.setAccount(account);
            mAddressAdapterCc.setAccount(account);
            mAddressAdapterBcc.setAccount(account);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.message_compose);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.list_title);
        mController = Controller.getInstance(getApplication());
        mListener = new Listener();
        initViews();
        setDraftNeedsSaving(false);
        long draftId = -1;
        if (savedInstanceState != null) {
            mSourceMessageProcessed =
                savedInstanceState.getBoolean(STATE_KEY_SOURCE_MESSAGE_PROCED, false);
            draftId = savedInstanceState.getLong(STATE_KEY_DRAFT_ID, -1);
        }
        Intent intent = getIntent();
        mAction = intent.getAction();
        if (draftId != -1) {
            mAction = ACTION_EDIT_DRAFT;
            mDraft.mId = draftId;
        }
        if (Intent.ACTION_VIEW.equals(mAction)
                || Intent.ACTION_SENDTO.equals(mAction)
                || Intent.ACTION_SEND.equals(mAction)
                || Intent.ACTION_SEND_MULTIPLE.equals(mAction)) {
            setAccount(intent);
            initFromIntent(intent);
            setDraftNeedsSaving(true);
            mMessageLoaded = true;
            mSourceMessageProcessed = true;
        } else {
            long messageId = draftId != -1 ? draftId : intent.getLongExtra(EXTRA_MESSAGE_ID, -1);
            if (messageId != -1) {
                mLoadMessageTask = new LoadMessageTask().execute(messageId);
            } else {
                setAccount(intent);
                mMessageLoaded = true;
                mSourceMessageProcessed = true;
            }
            setInitialComposeText(null, (mAccount != null) ? mAccount.mSignature : null);
        }
        if (ACTION_REPLY.equals(mAction) || ACTION_REPLY_ALL.equals(mAction) ||
                ACTION_FORWARD.equals(mAction) || ACTION_EDIT_DRAFT.equals(mAction)) {
        }
        updateTitle();
    }
    @Override
    public void setIntent(Intent intent) {
        super.setIntent(intent);
        mAction = intent.getAction();
    }
    @Override
    public void onResume() {
        super.onResume();
        mController.addResultCallback(mListener);
        if (Email.getNotifyUiAccountsChanged()) {
            Welcome.actionStart(this);
            finish();
            return;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        saveIfNeeded();
        mController.removeResultCallback(mListener);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mQuotedText.destroy();
        mQuotedText = null;
        Utility.cancelTaskInterrupt(mLoadAttachmentsTask);
        mLoadAttachmentsTask = null;
        Utility.cancelTaskInterrupt(mLoadMessageTask);
        mLoadMessageTask = null;
        mSaveMessageTask = null;
        if (mAddressAdapterTo != null) {
            mAddressAdapterTo.changeCursor(null);
        }
        if (mAddressAdapterCc != null) {
            mAddressAdapterCc.changeCursor(null);
        }
        if (mAddressAdapterBcc != null) {
            mAddressAdapterBcc.changeCursor(null);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long draftId = getOrCreateDraftId();
        if (draftId != -1) {
            outState.putLong(STATE_KEY_DRAFT_ID, draftId);
        }
        outState.putBoolean(STATE_KEY_CC_SHOWN, mCcView.getVisibility() == View.VISIBLE);
        outState.putBoolean(STATE_KEY_BCC_SHOWN, mBccView.getVisibility() == View.VISIBLE);
        outState.putBoolean(STATE_KEY_QUOTED_TEXT_SHOWN,
                mQuotedTextBar.getVisibility() == View.VISIBLE);
        outState.putBoolean(STATE_KEY_SOURCE_MESSAGE_PROCED, mSourceMessageProcessed);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCcView.setVisibility(savedInstanceState.getBoolean(STATE_KEY_CC_SHOWN) ?
                View.VISIBLE : View.GONE);
        mBccView.setVisibility(savedInstanceState.getBoolean(STATE_KEY_BCC_SHOWN) ?
                View.VISIBLE : View.GONE);
        mQuotedTextBar.setVisibility(savedInstanceState.getBoolean(STATE_KEY_QUOTED_TEXT_SHOWN) ?
                View.VISIBLE : View.GONE);
        mQuotedText.setVisibility(savedInstanceState.getBoolean(STATE_KEY_QUOTED_TEXT_SHOWN) ?
                View.VISIBLE : View.GONE);
        setDraftNeedsSaving(false);
    }
    private void setDraftNeedsSaving(boolean needsSaving) {
        mDraftNeedsSaving = needsSaving;
        mSaveButton.setEnabled(needsSaving);
    }
    private void initViews() {
        mToView = (MultiAutoCompleteTextView)findViewById(R.id.to);
        mCcView = (MultiAutoCompleteTextView)findViewById(R.id.cc);
        mBccView = (MultiAutoCompleteTextView)findViewById(R.id.bcc);
        mSubjectView = (EditText)findViewById(R.id.subject);
        mMessageContentView = (EditText)findViewById(R.id.message_content);
        mSendButton = (Button)findViewById(R.id.send);
        mDiscardButton = (Button)findViewById(R.id.discard);
        mSaveButton = (Button)findViewById(R.id.save);
        mAttachments = (LinearLayout)findViewById(R.id.attachments);
        mQuotedTextBar = findViewById(R.id.quoted_text_bar);
        mQuotedTextDelete = (ImageButton)findViewById(R.id.quoted_text_delete);
        mQuotedText = (WebView)findViewById(R.id.quoted_text);
        mLeftTitle = (TextView)findViewById(R.id.title_left_text);
        mRightTitle = (TextView)findViewById(R.id.title_right_text);
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start,
                                          int before, int after) { }
            public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                setDraftNeedsSaving(true);
            }
            public void afterTextChanged(android.text.Editable s) { }
        };
        InputFilter recipientFilter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                    int dstart, int dend) {
                if (end-start != 1 || source.charAt(start) != ' ') {
                    return null;
                }
                int scanBack = dstart;
                boolean dotFound = false;
                while (scanBack > 0) {
                    char c = dest.charAt(--scanBack);
                    switch (c) {
                        case '.':
                            dotFound = true;    
                            break;
                        case ',':
                            return null;
                        case '@':
                            if (!dotFound) {
                                return null;
                            }
                            if (source instanceof Spanned) {
                                SpannableStringBuilder sb = new SpannableStringBuilder(",");
                                sb.append(source);
                                return sb;
                            } else {
                                return ", ";
                            }
                        default:
                    }
                }
                return null;
            }
        };
        InputFilter[] recipientFilters = new InputFilter[] { recipientFilter };
        mToView.addTextChangedListener(watcher);
        mCcView.addTextChangedListener(watcher);
        mBccView.addTextChangedListener(watcher);
        mSubjectView.addTextChangedListener(watcher);
        mMessageContentView.addTextChangedListener(watcher);
        mToView.setFilters(recipientFilters);
        mCcView.setFilters(recipientFilters);
        mBccView.setFilters(recipientFilters);
        mQuotedTextBar.setVisibility(View.GONE);
        mQuotedText.setVisibility(View.GONE);
        mQuotedTextDelete.setOnClickListener(this);
        EmailAddressValidator addressValidator = new EmailAddressValidator();
        setupAddressAdapters();
        mToView.setAdapter(mAddressAdapterTo);
        mToView.setTokenizer(new Rfc822Tokenizer());
        mToView.setValidator(addressValidator);
        mCcView.setAdapter(mAddressAdapterCc);
        mCcView.setTokenizer(new Rfc822Tokenizer());
        mCcView.setValidator(addressValidator);
        mBccView.setAdapter(mAddressAdapterBcc);
        mBccView.setTokenizer(new Rfc822Tokenizer());
        mBccView.setValidator(addressValidator);
        mSendButton.setOnClickListener(this);
        mDiscardButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
        mSubjectView.setOnFocusChangeListener(this);
        mMessageContentView.setOnFocusChangeListener(this);
    }
    @SuppressWarnings("all")
    private void setupAddressAdapters() {
        if (true) {
            mAddressAdapterTo = new GalEmailAddressAdapter(this);
            mAddressAdapterCc = new GalEmailAddressAdapter(this);
            mAddressAdapterBcc = new GalEmailAddressAdapter(this);
        } else {
            mAddressAdapterTo = new EmailAddressAdapter(this);
            mAddressAdapterCc = new EmailAddressAdapter(this);
            mAddressAdapterBcc = new EmailAddressAdapter(this);
        }
    }
    private class LoadMessageTask extends AsyncTask<Long, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Long... messageIds) {
            synchronized (sSaveInProgressCondition) {
                while (sSaveInProgress) {
                    try {
                        sSaveInProgressCondition.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            Message message = Message.restoreMessageWithId(MessageCompose.this, messageIds[0]);
            if (message == null) {
                return new Object[] {null, null};
            }
            long accountId = message.mAccountKey;
            Account account = Account.restoreAccountWithId(MessageCompose.this, accountId);
            try {
                message.mHtml = Body.restoreBodyHtmlWithMessageId(MessageCompose.this, message.mId);
                message.mText = Body.restoreBodyTextWithMessageId(MessageCompose.this, message.mId);
                boolean isEditDraft = ACTION_EDIT_DRAFT.equals(mAction);
                if (isEditDraft) {
                    message.mHtmlReply =
                        Body.restoreReplyHtmlWithMessageId(MessageCompose.this, message.mId);
                    message.mTextReply =
                        Body.restoreReplyTextWithMessageId(MessageCompose.this, message.mId);
                    message.mIntroText =
                        Body.restoreIntroTextWithMessageId(MessageCompose.this, message.mId);
                    message.mSourceKey = Body.restoreBodySourceKey(MessageCompose.this,
                                                                   message.mId);
                } else {
                    message.mHtmlReply = null;
                    message.mTextReply = null;
                    message.mIntroText = null;
                }
            } catch (RuntimeException e) {
                Log.d(Email.LOG_TAG, "Exception while loading message body: " + e);
                return new Object[] {null, null};
            }
            return new Object[]{message, account};
        }
        @Override
        protected void onPostExecute(Object[] messageAndAccount) {
            if (messageAndAccount == null) {
                return;
            }
            final Message message = (Message) messageAndAccount[0];
            final Account account = (Account) messageAndAccount[1];
            if (message == null && account == null) {
                Toast.makeText(MessageCompose.this, R.string.error_loading_message_body,
                               Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            if (ACTION_EDIT_DRAFT.equals(mAction)) {
                mDraft = message;
                mLoadAttachmentsTask = new AsyncTask<Long, Void, Attachment[]>() {
                    @Override
                    protected Attachment[] doInBackground(Long... messageIds) {
                        return Attachment.restoreAttachmentsWithMessageId(MessageCompose.this,
                                messageIds[0]);
                    }
                    @Override
                    protected void onPostExecute(Attachment[] attachments) {
                        if (attachments == null) {
                            return;
                        }
                        for (Attachment attachment : attachments) {
                            addAttachment(attachment);
                        }
                    }
                }.execute(message.mId);
            } else if (ACTION_REPLY.equals(mAction)
                       || ACTION_REPLY_ALL.equals(mAction)
                       || ACTION_FORWARD.equals(mAction)) {
                mSource = message;
            } else if (Email.LOGD) {
                Email.log("Action " + mAction + " has unexpected EXTRA_MESSAGE_ID");
            }
            setAccount(account);
            processSourceMessageGuarded(message, mAccount);
            mMessageLoaded = true;
        }
    }
    private void updateTitle() {
        if (mSubjectView.getText().length() == 0) {
            mLeftTitle.setText(R.string.compose_title);
        } else {
            mLeftTitle.setText(mSubjectView.getText().toString());
        }
    }
    public void onFocusChange(View view, boolean focused) {
        if (!focused) {
            updateTitle();
        } else {
            switch (view.getId()) {
                case R.id.message_content:
                    setMessageContentSelection((mAccount != null) ? mAccount.mSignature : null);
            }
        }
    }
    private void addAddresses(MultiAutoCompleteTextView view, Address[] addresses) {
        if (addresses == null) {
            return;
        }
        for (Address address : addresses) {
            addAddress(view, address.toString());
        }
    }
    private void addAddresses(MultiAutoCompleteTextView view, String[] addresses) {
        if (addresses == null) {
            return;
        }
        for (String oneAddress : addresses) {
            addAddress(view, oneAddress);
        }
    }
    private void addAddress(MultiAutoCompleteTextView view, String address) {
        view.append(address + ", ");
    }
    private String getPackedAddresses(TextView view) {
        Address[] addresses = Address.parse(view.getText().toString().trim());
        return Address.pack(addresses);
    }
    private Address[] getAddresses(TextView view) {
        Address[] addresses = Address.parse(view.getText().toString().trim());
        return addresses;
    }
    private String makeDisplayName(String packedTo, String packedCc, String packedBcc) {
        Address first = null;
        int nRecipients = 0;
        for (String packed: new String[] {packedTo, packedCc, packedBcc}) {
            Address[] addresses = Address.unpack(packed);
            nRecipients += addresses.length;
            if (first == null && addresses.length > 0) {
                first = addresses[0];
            }
        }
        if (nRecipients == 0) {
            return "";
        }
        String friendly = first.toFriendly();
        if (nRecipients == 1) {
            return friendly;
        }
        return this.getString(R.string.message_compose_display_name, friendly, nRecipients - 1);
    }
    private ContentValues getUpdateContentValues(Message message) {
        ContentValues values = new ContentValues();
        values.put(MessageColumns.TIMESTAMP, message.mTimeStamp);
        values.put(MessageColumns.FROM_LIST, message.mFrom);
        values.put(MessageColumns.TO_LIST, message.mTo);
        values.put(MessageColumns.CC_LIST, message.mCc);
        values.put(MessageColumns.BCC_LIST, message.mBcc);
        values.put(MessageColumns.SUBJECT, message.mSubject);
        values.put(MessageColumns.DISPLAY_NAME, message.mDisplayName);
        values.put(MessageColumns.FLAG_READ, message.mFlagRead);
        values.put(MessageColumns.FLAG_LOADED, message.mFlagLoaded);
        values.put(MessageColumns.FLAG_ATTACHMENT, message.mFlagAttachment);
        values.put(MessageColumns.FLAGS, message.mFlags);
        return values;
    }
    private void updateMessage(Message message, Account account, boolean hasAttachments) {
        if (message.mMessageId == null || message.mMessageId.length() == 0) {
            message.mMessageId = Utility.generateMessageId();
        }
        message.mTimeStamp = System.currentTimeMillis();
        message.mFrom = new Address(account.getEmailAddress(), account.getSenderName()).pack();
        message.mTo = getPackedAddresses(mToView);
        message.mCc = getPackedAddresses(mCcView);
        message.mBcc = getPackedAddresses(mBccView);
        message.mSubject = mSubjectView.getText().toString();
        message.mText = mMessageContentView.getText().toString();
        message.mAccountKey = account.mId;
        message.mDisplayName = makeDisplayName(message.mTo, message.mCc, message.mBcc);
        message.mFlagRead = true;
        message.mFlagLoaded = Message.FLAG_LOADED_COMPLETE;
        message.mFlagAttachment = hasAttachments;
        if (mSource != null && mQuotedTextBar.getVisibility() == View.VISIBLE) {
            if (ACTION_REPLY.equals(mAction) || ACTION_REPLY_ALL.equals(mAction)
                    || ACTION_FORWARD.equals(mAction)) {
                message.mSourceKey = mSource.mId;
                message.mHtmlReply = mSource.mHtml;
                message.mTextReply = mSource.mText;
            }
            String fromAsString = Address.unpackToString(mSource.mFrom);
            if (ACTION_FORWARD.equals(mAction)) {
                message.mFlags |= Message.FLAG_TYPE_FORWARD;
                String subject = mSource.mSubject;
                String to = Address.unpackToString(mSource.mTo);
                String cc = Address.unpackToString(mSource.mCc);
                message.mIntroText =
                    getString(R.string.message_compose_fwd_header_fmt, subject, fromAsString,
                            to != null ? to : "", cc != null ? cc : "");
            } else {
                message.mFlags |= Message.FLAG_TYPE_REPLY;
                message.mIntroText =
                    getString(R.string.message_compose_reply_header_fmt, fromAsString);
            }
        }
    }
    private Attachment[] getAttachmentsFromUI() {
        int count = mAttachments.getChildCount();
        Attachment[] attachments = new Attachment[count];
        for (int i = 0; i < count; ++i) {
            attachments[i] = (Attachment) mAttachments.getChildAt(i).getTag();
        }
        return attachments;
    }
    private long getOrCreateDraftId() {
        synchronized (mDraft) {
            if (mDraft.mId > 0) {
                return mDraft.mId;
            }
            if (!mMessageLoaded) {
                return -1;
            }
            final Attachment[] attachments = getAttachmentsFromUI();
            updateMessage(mDraft, mAccount, attachments.length > 0);
            mController.saveToMailbox(mDraft, EmailContent.Mailbox.TYPE_DRAFTS);
            return mDraft.mId;
        }
    }
    private void sendOrSaveMessage(final boolean send) {
        final Attachment[] attachments = getAttachmentsFromUI();
        if (!mMessageLoaded) {
            return;
        }
        updateMessage(mDraft, mAccount, attachments.length > 0);
        synchronized (sSaveInProgressCondition) {
            sSaveInProgress = true;
        }
        mSaveMessageTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                synchronized (mDraft) {
                    if (mDraft.isSaved()) {
                        Uri draftUri =
                            ContentUris.withAppendedId(mDraft.SYNCED_CONTENT_URI, mDraft.mId);
                        getContentResolver().update(draftUri, getUpdateContentValues(mDraft),
                                null, null);
                        ContentValues values = new ContentValues();
                        values.put(BodyColumns.TEXT_CONTENT, mDraft.mText);
                        values.put(BodyColumns.TEXT_REPLY, mDraft.mTextReply);
                        values.put(BodyColumns.HTML_REPLY, mDraft.mHtmlReply);
                        values.put(BodyColumns.INTRO_TEXT, mDraft.mIntroText);
                        values.put(BodyColumns.SOURCE_MESSAGE_KEY, mDraft.mSourceKey);
                        Body.updateBodyWithMessageId(MessageCompose.this, mDraft.mId, values);
                    } else {
                        mController.saveToMailbox(mDraft, EmailContent.Mailbox.TYPE_DRAFTS);
                    }
                    for (Attachment attachment : attachments) {
                        if (!attachment.isSaved()) {
                            attachment.mMessageKey = mDraft.mId;
                            attachment.save(MessageCompose.this);
                        }
                    }
                    if (send) {
                        mController.sendMessage(mDraft.mId, mDraft.mAccountKey);
                    }
                    return null;
                }
            }
            @Override
            protected void onPostExecute(Void dummy) {
                synchronized (sSaveInProgressCondition) {
                    sSaveInProgress = false;
                    sSaveInProgressCondition.notify();
                }
                if (isCancelled()) {
                    return;
                }
                if (!send && (getChangingConfigurations() & ActivityInfo.CONFIG_ORIENTATION) == 0) {
                    Toast.makeText(MessageCompose.this, R.string.message_saved_toast,
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    private void saveIfNeeded() {
        if (!mDraftNeedsSaving) {
            return;
        }
        setDraftNeedsSaving(false);
        sendOrSaveMessage(false);
    }
     boolean isAddressAllValid() {
        for (TextView view : new TextView[]{mToView, mCcView, mBccView}) {
            String addresses = view.getText().toString().trim();
            if (!Address.isAllValid(addresses)) {
                view.setError(getString(R.string.message_compose_error_invalid_email));
                return false;
            }
        }
        return true;
    }
    private void onSend() {
        if (!isAddressAllValid()) {
            Toast.makeText(this, getString(R.string.message_compose_error_invalid_email),
                           Toast.LENGTH_LONG).show();
        } else if (getAddresses(mToView).length == 0 &&
                getAddresses(mCcView).length == 0 &&
                getAddresses(mBccView).length == 0) {
            mToView.setError(getString(R.string.message_compose_error_no_recipients));
            Toast.makeText(this, getString(R.string.message_compose_error_no_recipients),
                    Toast.LENGTH_LONG).show();
        } else {
            sendOrSaveMessage(true);
            setDraftNeedsSaving(false);
            finish();
        }
    }
    private void onDiscard() {
        if (mDraft.mId > 0) {
            mController.deleteMessage(mDraft.mId, mDraft.mAccountKey);
        }
        Toast.makeText(this, getString(R.string.message_discarded_toast), Toast.LENGTH_LONG).show();
        setDraftNeedsSaving(false);
        finish();
    }
    private void onSave() {
        saveIfNeeded();
        finish();
    }
    private void onAddCcBcc() {
        mCcView.setVisibility(View.VISIBLE);
        mBccView.setVisibility(View.VISIBLE);
    }
    private void onAddAttachment() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(Email.ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES[0]);
        startActivityForResult(
                Intent.createChooser(i, getString(R.string.choose_attachment_dialog_title)),
                ACTIVITY_REQUEST_PICK_ATTACHMENT);
    }
    private Attachment loadAttachmentInfo(Uri uri) {
        long size = -1;
        String name = null;
        ContentResolver contentResolver = getContentResolver();
        Cursor metadataCursor = contentResolver.query(uri, ATTACHMENT_META_NAME_PROJECTION,
                null, null, null);
        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    name = metadataCursor.getString(ATTACHMENT_META_NAME_COLUMN_DISPLAY_NAME);
                }
            } finally {
                metadataCursor.close();
            }
        }
        metadataCursor = contentResolver.query(uri, ATTACHMENT_META_SIZE_PROJECTION,
                null, null, null);
        if (metadataCursor != null) {
            try {
                if (metadataCursor.moveToFirst()) {
                    size = metadataCursor.getLong(ATTACHMENT_META_SIZE_COLUMN_SIZE);
                }
            } finally {
                metadataCursor.close();
            }
        }
        if (name == null) {
            name = uri.getLastPathSegment();
        }
        if (size < 0) {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                String path = uri.getPath();
                if (path != null) {
                    File file = new File(path);
                    size = file.length();  
                }
            }
            if (size <= 0) {
                size = Email.MAX_ATTACHMENT_UPLOAD_SIZE + 1;
            }
        }
        String contentType = contentResolver.getType(uri);
        if (contentType == null) {
            contentType = "";
        }
        Attachment attachment = new Attachment();
        attachment.mFileName = name;
        attachment.mContentUri = uri.toString();
        attachment.mSize = size;
        attachment.mMimeType = contentType;
        return attachment;
    }
    private void addAttachment(Attachment attachment) {
        if (attachment.mSize > Email.MAX_ATTACHMENT_UPLOAD_SIZE) {
            Toast.makeText(this, R.string.message_compose_attachment_size, Toast.LENGTH_LONG)
                    .show();
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.message_compose_attachment,
                mAttachments, false);
        TextView nameView = (TextView)view.findViewById(R.id.attachment_name);
        ImageButton delete = (ImageButton)view.findViewById(R.id.attachment_delete);
        nameView.setText(attachment.mFileName);
        delete.setOnClickListener(this);
        delete.setTag(view);
        view.setTag(attachment);
        mAttachments.addView(view);
    }
    private void addAttachment(Uri uri) {
        addAttachment(loadAttachmentInfo(uri));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        addAttachment(data.getData());
        setDraftNeedsSaving(true);
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                onSend();
                break;
            case R.id.save:
                onSave();
                break;
            case R.id.discard:
                onDiscard();
                break;
            case R.id.attachment_delete:
                onDeleteAttachment(view);
                break;
            case R.id.quoted_text_delete:
                mQuotedTextBar.setVisibility(View.GONE);
                mQuotedText.setVisibility(View.GONE);
                mDraft.mIntroText = null;
                mDraft.mTextReply = null;
                mDraft.mHtmlReply = null;
                mDraft.mSourceKey = 0;
                setDraftNeedsSaving(true);
                break;
        }
    }
    private void onDeleteAttachment(View delButtonView) {
        View attachmentView = (View) delButtonView.getTag();
        Attachment attachment = (Attachment) attachmentView.getTag();
        mAttachments.removeView(attachmentView);
        if (attachment.isSaved()) {
            new AsyncTask<Long, Void, Void>() {
                @Override
                protected Void doInBackground(Long... attachmentIds) {
                    mController.deleteAttachment(attachmentIds[0]);
                    return null;
                }
            }.execute(attachment.mId);
        }
        setDraftNeedsSaving(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                onSend();
                break;
            case R.id.save:
                onSave();
                break;
            case R.id.discard:
                onDiscard();
                break;
            case R.id.add_cc_bcc:
                onAddCcBcc();
                break;
            case R.id.add_attachment:
                onAddAttachment();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.message_compose_option, menu);
        return true;
    }
     void setInitialComposeText(CharSequence text, String signature) {
        int textLength = 0;
        if (text != null) {
            mMessageContentView.append(text);
            textLength = text.length();
        }
        if (!TextUtils.isEmpty(signature)) {
            if (textLength == 0 || text.charAt(textLength - 1) != '\n') {
                mMessageContentView.append("\n");
            }
            mMessageContentView.append(signature);
        }
    }
     void initFromIntent(Intent intent) {
        String[] extraStrings = intent.getStringArrayExtra(Intent.EXTRA_EMAIL);
        if (extraStrings != null) {
            addAddresses(mToView, extraStrings);
        }
        extraStrings = intent.getStringArrayExtra(Intent.EXTRA_CC);
        if (extraStrings != null) {
            addAddresses(mCcView, extraStrings);
        }
        extraStrings = intent.getStringArrayExtra(Intent.EXTRA_BCC);
        if (extraStrings != null) {
            addAddresses(mBccView, extraStrings);
        }
        String extraString = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (extraString != null) {
            mSubjectView.setText(extraString);
        }
        final Uri dataUri = intent.getData();
        if (dataUri != null) {
            if ("mailto".equals(dataUri.getScheme())) {
                initializeFromMailTo(dataUri.toString());
            } else {
                String toText = dataUri.getSchemeSpecificPart();
                if (toText != null) {
                    addAddresses(mToView, toText.split(","));
                }
            }
        }
        CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            setInitialComposeText(text, null);
        }
        if (Intent.ACTION_SEND.equals(mAction) && intent.hasExtra(Intent.EXTRA_STREAM)) {
            String type = intent.getType();
            Uri stream = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (stream != null && type != null) {
                if (MimeUtility.mimeTypeMatches(type,
                        Email.ACCEPTABLE_ATTACHMENT_SEND_INTENT_TYPES)) {
                    addAttachment(stream);
                }
            }
        }
        if (Intent.ACTION_SEND_MULTIPLE.equals(mAction)
                && intent.hasExtra(Intent.EXTRA_STREAM)) {
            ArrayList<Parcelable> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (list != null) {
                for (Parcelable parcelable : list) {
                    Uri uri = (Uri) parcelable;
                    if (uri != null) {
                        Attachment attachment = loadAttachmentInfo(uri);
                        if (MimeUtility.mimeTypeMatches(attachment.mMimeType,
                                Email.ACCEPTABLE_ATTACHMENT_SEND_INTENT_TYPES)) {
                            addAttachment(attachment);
                        }
                    }
                }
            }
        }
        if (mCcView.length() > 0) {
            mCcView.setVisibility(View.VISIBLE);
        }
        if (mBccView.length() > 0) {
            mBccView.setVisibility(View.VISIBLE);
        }
        setNewMessageFocus();
        setDraftNeedsSaving(false);
    }
    private void initializeFromMailTo(String mailToString) {
        int index = mailToString.indexOf("?");
        int length = "mailto".length() + 1;
        String to;
        try {
            if (index == -1) {
                to = decode(mailToString.substring(length));
            } else {
                to = decode(mailToString.substring(length, index));
            }
            addAddresses(mToView, to.split(" ,"));
        } catch (UnsupportedEncodingException e) {
            Log.e(Email.LOG_TAG, e.getMessage() + " while decoding '" + mailToString + "'");
        }
        Uri uri = Uri.parse("foo:
        List<String> cc = uri.getQueryParameters("cc");
        addAddresses(mCcView, cc.toArray(new String[cc.size()]));
        List<String> otherTo = uri.getQueryParameters("to");
        addAddresses(mCcView, otherTo.toArray(new String[otherTo.size()]));
        List<String> bcc = uri.getQueryParameters("bcc");
        addAddresses(mBccView, bcc.toArray(new String[bcc.size()]));
        List<String> subject = uri.getQueryParameters("subject");
        if (subject.size() > 0) {
            mSubjectView.setText(subject.get(0));
        }
        List<String> body = uri.getQueryParameters("body");
        if (body.size() > 0) {
            setInitialComposeText(body.get(0), (mAccount != null) ? mAccount.mSignature : null);
        }
    }
    private String decode(String s) throws UnsupportedEncodingException {
        return URLDecoder.decode(s, "UTF-8");
    }
    private void displayQuotedText(String textBody, String htmlBody) {
        boolean plainTextFlag = textBody != null;
        String text = plainTextFlag ? textBody : htmlBody;
        if (text != null) {
            text = plainTextFlag ? EmailHtmlUtil.escapeCharacterToDisplay(text) : text;
            mQuotedTextBar.setVisibility(View.VISIBLE);
            if (mQuotedText != null) {
                mQuotedText.setVisibility(View.VISIBLE);
                mQuotedText.loadDataWithBaseURL("email:
            }
        }
    }
    private boolean safeAddAddresses(String addrs, String ourAddress,
            MultiAutoCompleteTextView view, ArrayList<Address> addrList) {
        boolean added = false;
        for (Address address : Address.unpack(addrs)) {
            if (!address.getAddress().equalsIgnoreCase(ourAddress) && !addrList.contains(address)) {
                addrList.add(address);
                addAddress(view, address.toString());
                added = true;
            }
        }
        return added;
    }
     void setupAddressViews(Message message, Account account,
            MultiAutoCompleteTextView toView, MultiAutoCompleteTextView ccView, boolean replyAll) {
        Address[] replyToAddresses = Address.unpack(message.mReplyTo);
        if (replyToAddresses.length == 0) {
            replyToAddresses = Address.unpack(message.mFrom);
        }
        addAddresses(mToView, replyToAddresses);
        if (replyAll) {
            ArrayList<Address> allAddresses = new ArrayList<Address>();
            String ourAddress = account.mEmailAddress;
            for (Address address: replyToAddresses) {
                allAddresses.add(address);
            }
            safeAddAddresses(message.mTo, ourAddress, mToView, allAddresses);
            if (safeAddAddresses(message.mCc, ourAddress, mCcView, allAddresses)) {
                mCcView.setVisibility(View.VISIBLE);
            }
        }
    }
    void processSourceMessageGuarded(Message message, Account account) {
        if (!mSourceMessageProcessed) {
            processSourceMessage(message, account);
            mSourceMessageProcessed = true;
        }
        if (ACTION_EDIT_DRAFT.equals(mAction)) {
            displayQuotedText(message.mTextReply, message.mHtmlReply);
        }
    }
    void processSourceMessage(Message message, Account account) {
        setDraftNeedsSaving(true);
        final String subject = message.mSubject;
        if (ACTION_REPLY.equals(mAction) || ACTION_REPLY_ALL.equals(mAction)) {
            setupAddressViews(message, account, mToView, mCcView,
                ACTION_REPLY_ALL.equals(mAction));
            if (subject != null && !subject.toLowerCase().startsWith("re:")) {
                mSubjectView.setText("Re: " + subject);
            } else {
                mSubjectView.setText(subject);
            }
            displayQuotedText(message.mText, message.mHtml);
        } else if (ACTION_FORWARD.equals(mAction)) {
            mSubjectView.setText(subject != null && !subject.toLowerCase().startsWith("fwd:") ?
                    "Fwd: " + subject : subject);
            displayQuotedText(message.mText, message.mHtml);
        } else if (ACTION_EDIT_DRAFT.equals(mAction)) {
            mSubjectView.setText(subject);
            addAddresses(mToView, Address.unpack(message.mTo));
            Address[] cc = Address.unpack(message.mCc);
            if (cc.length > 0) {
                addAddresses(mCcView, cc);
                mCcView.setVisibility(View.VISIBLE);
            }
            Address[] bcc = Address.unpack(message.mBcc);
            if (bcc.length > 0) {
                addAddresses(mBccView, bcc);
                mBccView.setVisibility(View.VISIBLE);
            }
            mMessageContentView.setText(message.mText);
            setDraftNeedsSaving(false);
        }
        setNewMessageFocus();
    }
     void setMessageContentSelection(String signature) {
        int selection = mMessageContentView.length();
        if (!TextUtils.isEmpty(signature)) {
            int signatureLength = signature.length();
            int estimatedSelection = selection - signatureLength;
            if (estimatedSelection >= 0) {
                CharSequence text = mMessageContentView.getText();
                int i = 0;
                while (i < signatureLength
                       && text.charAt(estimatedSelection + i) == signature.charAt(i)) {
                    ++i;
                }
                if (i == signatureLength) {
                    selection = estimatedSelection;
                    while (selection > 0 && text.charAt(selection - 1) == '\n') {
                        --selection;
                    }
                }
            }
        }
        mMessageContentView.setSelection(selection, selection);
    }
    private void setNewMessageFocus() {
        if (mToView.length() == 0) {
            mToView.requestFocus();
        } else if (mSubjectView.length() == 0) {
            mSubjectView.requestFocus();
        } else {
            mMessageContentView.requestFocus();
            setMessageContentSelection((mAccount != null) ? mAccount.mSignature : null);
        }
    }
    private class Listener implements Controller.Result {
        public void updateMailboxListCallback(MessagingException result, long accountId,
                int progress) {
        }
        public void updateMailboxCallback(MessagingException result, long accountId,
                long mailboxId, int progress, int numNewMessages) {
            if (result != null || progress == 100) {
                Email.updateMailboxRefreshTime(mailboxId);
            }
        }
        public void loadMessageForViewCallback(MessagingException result, long messageId,
                int progress) {
        }
        public void loadAttachmentCallback(MessagingException result, long messageId,
                long attachmentId, int progress) {
        }
        public void serviceCheckMailCallback(MessagingException result, long accountId,
                long mailboxId, int progress, long tag) {
        }
        public void sendMailCallback(MessagingException result, long accountId, long messageId,
                int progress) {
        }
    }
}
