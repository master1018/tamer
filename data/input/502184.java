public class UpgradeAccounts extends ListActivity implements OnClickListener {
    private static final boolean DEBUG_FORCE_UPGRADES = false;
    private AccountInfo[] mLegacyAccounts;
    private UIHandler mHandler = new UIHandler();
    private AccountsAdapter mAdapter;
    private ListView mListView;
    private Button mProceedButton;
    private ConversionTask mConversionTask;
    private static final Object sConversionInProgress = new Object();
    private static boolean sConversionHasRun = false;
    private static final String WHERE_ACCOUNT_UUID_IS = AccountColumns.COMPATIBILITY_UUID + "=?";
    public static void actionStart(Context context) {
        Intent i = new Intent(context, UpgradeAccounts.class);
        context.startActivity(i);
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Preferences p = Preferences.getPreferences(this);
        Account[] legacyAccounts = p.getAccounts();
        if (legacyAccounts.length == 0) {
            finish();
            return;
        }
        loadAccountInfoArray(legacyAccounts);
        Log.d(Email.LOG_TAG, "*** Preparing to upgrade " +
                Integer.toString(mLegacyAccounts.length) + " accounts");
        setContentView(R.layout.upgrade_accounts);
        mListView = getListView();
        mProceedButton = (Button) findViewById(R.id.action_button);
        mProceedButton.setEnabled(false);
        mProceedButton.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateList();
        mConversionTask = new ConversionTask(mLegacyAccounts);
        mConversionTask.execute();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTask(mConversionTask, false); 
        mConversionTask = null;
    }
    @Override
    public void onBackPressed() {
        if (!mProceedButton.isEnabled()) {
            finish();
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_button:
                onClickOk();
                break;
        }
    }
    private void onClickOk() {
        Welcome.actionStart(UpgradeAccounts.this);
        finish();
    }
    private void updateList() {
        mAdapter = new AccountsAdapter();
        getListView().setAdapter(mAdapter);
    }
    private static class AccountInfo {
        Account account;
        int maxProgress;
        int progress;
        String errorMessage;    
        boolean isError;        
        public AccountInfo(Account legacyAccount) {
            account = legacyAccount;
            maxProgress = 0;
            progress = 0;
            errorMessage = null;
            isError = false;
        }
    }
    private void loadAccountInfoArray(Account[] legacyAccounts) {
        mLegacyAccounts = new AccountInfo[legacyAccounts.length];
        for (int i = 0; i < legacyAccounts.length; i++) {
            AccountInfo ai = new AccountInfo(legacyAccounts[i]);
            mLegacyAccounts[i] = ai;
        }
    }
    private static class ViewHolder {
        TextView displayName;
        ProgressBar progress;
        TextView errorReport;
    }
    class AccountsAdapter extends BaseAdapter {
        final LayoutInflater mInflater;
        AccountsAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public boolean hasStableIds() {
            return true;
        }
        public int getCount() {
            return mLegacyAccounts.length;
        }
        public Object getItem(int position) {
            return mLegacyAccounts[position];
        }
        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                v = newView(parent);
            } else {
                v = convertView;
            }
            bindView(v, position);
            return v;
        }
        public View newView(ViewGroup parent) {
            View v = mInflater.inflate(R.layout.upgrade_accounts_item, parent, false);
            ViewHolder h = new ViewHolder();
            h.displayName = (TextView) v.findViewById(R.id.name);
            h.progress = (ProgressBar) v.findViewById(R.id.progress);
            h.errorReport = (TextView) v.findViewById(R.id.error);
            v.setTag(h);
            return v;
        }
        public void bindView(View view, int position) {
            ViewHolder vh = (ViewHolder) view.getTag();
            AccountInfo ai = mLegacyAccounts[position];
            vh.displayName.setText(ai.account.getDescription());
            if (ai.errorMessage == null) {
                vh.errorReport.setVisibility(View.GONE);
                vh.progress.setVisibility(View.VISIBLE);
                vh.progress.setMax(ai.maxProgress);
                vh.progress.setProgress(ai.progress);
            } else {
                vh.progress.setVisibility(View.GONE);
                vh.errorReport.setVisibility(View.VISIBLE);
                vh.errorReport.setText(ai.errorMessage);
            }
        }
    }
    class UIHandler extends Handler {
        private static final int MSG_SET_MAX = 1;
        private static final int MSG_SET_PROGRESS = 2;
        private static final int MSG_INC_PROGRESS = 3;
        private static final int MSG_ERROR = 4;
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SET_MAX:
                    mLegacyAccounts[msg.arg1].maxProgress = msg.arg2;
                    mListView.invalidateViews();        
                    break;
                case MSG_SET_PROGRESS:
                    mLegacyAccounts[msg.arg1].progress = msg.arg2;
                    mListView.invalidateViews();        
                    break;
                case MSG_INC_PROGRESS:
                    mLegacyAccounts[msg.arg1].progress += msg.arg2;
                    mListView.invalidateViews();        
                    break;
                case MSG_ERROR:
                    mLegacyAccounts[msg.arg1].errorMessage = (String) msg.obj;
                    mListView.invalidateViews();        
                    mProceedButton.setEnabled(true);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        public void setMaxProgress(int accountNum, int max) {
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_SET_MAX;
            msg.arg1 = accountNum;
            msg.arg2 = max;
            sendMessage(msg);
        }
        public void setProgress(int accountNum, int progress) {
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_SET_PROGRESS;
            msg.arg1 = accountNum;
            msg.arg2 = progress;
            sendMessage(msg);
        }
        public void incProgress(int accountNum) {
            incProgress(accountNum, 1);
        }
        public void incProgress(int accountNum, int incrementBy) {
            if (incrementBy == 0) return;
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_INC_PROGRESS;
            msg.arg1 = accountNum;
            msg.arg2 = incrementBy;
            sendMessage(msg);
        }
        public void error(int accountNum, String error) {
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_ERROR;
            msg.arg1 = accountNum;
            msg.obj = error;
            sendMessage(msg);
        }
    }
    private class ConversionTask extends AsyncTask<Void, Void, Void> {
        UpgradeAccounts.AccountInfo[] mAccountInfo;
        final Context mContext;
        final Preferences mPreferences;
        public ConversionTask(UpgradeAccounts.AccountInfo[] accountInfo) {
            mAccountInfo = accountInfo;
            mContext = UpgradeAccounts.this;
            mPreferences = Preferences.getPreferences(mContext);
        }
        @Override
        protected Void doInBackground(Void... params) {
            synchronized (sConversionInProgress) {
                if (sConversionHasRun) {
                    return null;
                }
                sConversionHasRun = true;
                UIHandler handler = UpgradeAccounts.this.mHandler;
                for (int i = 0; i < mAccountInfo.length; i++) {
                    int estimate = UpgradeAccounts.estimateWork(mContext, mAccountInfo[i].account);
                    if (estimate == -1) {
                        mAccountInfo[i].isError = true;
                        mHandler.error(i, mContext.getString(R.string.upgrade_accounts_error));
                    }
                    UpgradeAccounts.this.mHandler.setMaxProgress(i, estimate);
                }
                for (int i = 0; i < mAccountInfo.length; i++) {
                    if (!mAccountInfo[i].isError) {
                        boolean ok = scrubAccount(mContext, mAccountInfo[i].account, i, handler);
                        if (!ok) {
                            mAccountInfo[i].isError = true;
                            mHandler.error(i, mContext.getString(R.string.upgrade_accounts_error));
                        }
                    }
                }
                for (int i = 0; i < mAccountInfo.length; i++) {
                    AccountInfo info = mAccountInfo[i];
                    copyAndDeleteAccount(info, i, handler, Store.STORE_SCHEME_POP3);
                }
                for (int i = 0; i < mAccountInfo.length; i++) {
                    AccountInfo info = mAccountInfo[i];
                    copyAndDeleteAccount(info, i, handler, Store.STORE_SCHEME_IMAP);
                }
                Email.setServicesEnabled(mContext);
            }
            return null;
        }
        private void copyAndDeleteAccount(AccountInfo info, int i, UIHandler handler, String type) {
            try {
                if (type != null) {
                    String storeUri = info.account.getStoreUri();
                    boolean isType = storeUri.startsWith(type);
                    if (!isType) {
                        return;         
                    }
                }
                if (!info.isError) {
                    copyAccount(mContext, info.account, i, handler);
                }
            } catch (RuntimeException e) {
                Log.d(Email.LOG_TAG, "Exception while copying account " + e);
                mHandler.error(i, mContext.getString(R.string.upgrade_accounts_error));
                info.isError = true;
            }
            try {
                deleteAccountStore(mContext, info.account, i, handler);
                info.account.delete(mPreferences);
            } catch (RuntimeException e) {
                Log.d(Email.LOG_TAG, "Exception while deleting account " + e);
            }
            handler.setProgress(i, Integer.MAX_VALUE);
        }
        @Override
        protected void onPostExecute(Void result) {
            if (!isCancelled()) {
                if (!mProceedButton.isEnabled()) {
                    onClickOk();
                }
            }
        }
    }
     static int estimateWork(Context context, Account account) {
        int estimate = 1;         
        try {
            LocalStore store = LocalStore.newInstance(account.getLocalStoreUri(), context, null);
            Folder[] folders = store.getPersonalNamespaces();
            estimate += folders.length;
            for (int i = 0; i < folders.length; i++) {
                Folder folder = folders[i];
                folder.open(Folder.OpenMode.READ_ONLY, null);
                estimate += folder.getMessageCount();
                folder.close(false);
            }
            estimate += store.getStoredAttachmentCount();
            store.close();
        } catch (MessagingException e) {
            Log.d(Email.LOG_TAG, "Exception while estimating account size " + e);
            return -1;
        } catch (RuntimeException e) {
            Log.d(Email.LOG_TAG, "Exception while estimating account size " + e);
            return -1;
        }
        return estimate;
    }
     static boolean scrubAccount(Context context, Account account, int accountNum,
            UIHandler handler) {
        try {
            String storeUri = account.getStoreUri();
            boolean isImap = storeUri.startsWith(Store.STORE_SCHEME_IMAP);
            LocalStore store = LocalStore.newInstance(account.getLocalStoreUri(), context, null);
            Folder[] folders = store.getPersonalNamespaces();
            for (Folder folder : folders) {
                folder.open(Folder.OpenMode.READ_ONLY, null);
                String folderName = folder.getName();
                if ("drafts".equalsIgnoreCase(folderName)) {
                } else if ("outbox".equalsIgnoreCase(folderName)) {
                } else if ("sent".equalsIgnoreCase(folderName)) {
                } else if (isImap || "trash".equalsIgnoreCase(folderName)) {
                    Log.d(Email.LOG_TAG, "Scrub " + account.getDescription() + "." + folderName);
                    int messageCount = folder.getMessageCount();
                    folder.delete(true);
                    if (handler != null) {
                        handler.incProgress(accountNum, 1 + messageCount);
                    }
                }
                folder.close(false);
            }
            int pruned = store.pruneCachedAttachments();
            if (handler != null) {
                handler.incProgress(accountNum, pruned);
            }
            store.close();
        } catch (MessagingException e) {
            Log.d(Email.LOG_TAG, "Exception while scrubbing account " + e);
            return false;
        } catch (RuntimeException e) {
            Log.d(Email.LOG_TAG, "Exception while scrubbing account " + e);
            return false;
        }
        return true;
    }
    private static class FolderConversion {
        final Folder folder;
        final EmailContent.Mailbox mailbox;
        public FolderConversion(Folder _folder, EmailContent.Mailbox _mailbox) {
            folder = _folder;
            mailbox = _mailbox;
        }
    }
     static void copyAccount(Context context, Account account, int accountNum,
            UIHandler handler) {
        int existCount = EmailContent.count(context, EmailContent.Account.CONTENT_URI,
                WHERE_ACCOUNT_UUID_IS, new String[] { account.getUuid() });
        if (existCount > 0) {
            Log.d(Email.LOG_TAG, "No conversion, account exists: " + account.getDescription());
            if (handler != null) {
                handler.error(accountNum, context.getString(R.string.upgrade_accounts_error));
            }
            return;
        }
        EmailContent.Account newAccount = LegacyConversions.makeAccount(context, account);
        cleanupConnections(context, newAccount, account);
        newAccount.save(context);
        if (handler != null) {
            handler.incProgress(accountNum);
        }
        HashSet<FolderConversion> conversions = new HashSet<FolderConversion>();
        FolderConversion drafts = null;
        FolderConversion outbox = null;
        FolderConversion sent = null;
        LocalStore store = null;
        try {
            store = LocalStore.newInstance(account.getLocalStoreUri(), context, null);
            Folder[] folders = store.getPersonalNamespaces();
            for (Folder folder : folders) {
                String folderName = null;
                try {
                    folder.open(Folder.OpenMode.READ_ONLY, null);
                    folderName = folder.getName();
                    Log.d(Email.LOG_TAG, "Copy " + account.getDescription() + "." + folderName);
                    EmailContent.Mailbox mailbox =
                        LegacyConversions.makeMailbox(context, newAccount, folder);
                    mailbox.save(context);
                    if (handler != null) {
                        handler.incProgress(accountNum);
                    }
                    folder.close(false);
                    FolderConversion conversion = new FolderConversion(folder, mailbox);
                    conversions.add(conversion);
                    switch (mailbox.mType) {
                        case Mailbox.TYPE_DRAFTS:
                            drafts = conversion;
                            break;
                        case Mailbox.TYPE_OUTBOX:
                            outbox = conversion;
                            break;
                        case Mailbox.TYPE_SENT:
                            sent = conversion;
                            break;
                    }
                } catch (MessagingException e) {
                    Log.d(Email.LOG_TAG, "Exception copying folder " + folderName + ": " + e);
                    if (handler != null) {
                        handler.error(accountNum,
                                context.getString(R.string.upgrade_accounts_error));
                    }
                }
            }
            if (outbox != null) {
                copyMessages(context, outbox, true, newAccount, accountNum, handler);
                conversions.remove(outbox);
            }
            if (drafts != null) {
                copyMessages(context, drafts, true, newAccount, accountNum, handler);
                conversions.remove(drafts);
            }
            if (sent != null) {
                copyMessages(context, sent, true, newAccount, accountNum, handler);
                conversions.remove(outbox);
            }
            for (FolderConversion conversion : conversions) {
                copyMessages(context, conversion, false, newAccount, accountNum, handler);
            }
        } catch (MessagingException e) {
            Log.d(Email.LOG_TAG, "Exception while copying folders " + e);
            if (handler != null) {
                handler.error(accountNum, context.getString(R.string.upgrade_accounts_error));
            }
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }
     static void copyMessages(Context context, FolderConversion conversion,
            boolean localAttachments, EmailContent.Account newAccount, int accountNum,
            UIHandler handler) {
        try {
            boolean makeDeleteSentinels = (conversion.mailbox.mType == Mailbox.TYPE_INBOX) &&
                    (newAccount.getDeletePolicy() == EmailContent.Account.DELETE_POLICY_NEVER);
            conversion.folder.open(Folder.OpenMode.READ_ONLY, null);
            Message[] oldMessages = conversion.folder.getMessages(null);
            for (Message oldMessage : oldMessages) {
                Exception e = null;
                try {
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    fp.add(FetchProfile.Item.BODY);
                    conversion.folder.fetch(new Message[] { oldMessage }, fp, null);
                    EmailContent.Message newMessage = new EmailContent.Message();
                    if (makeDeleteSentinels && oldMessage.isSet(Flag.DELETED)) {
                        newMessage.mAccountKey = newAccount.mId;
                        newMessage.mMailboxKey = conversion.mailbox.mId;
                        newMessage.mFlagLoaded = EmailContent.Message.FLAG_LOADED_DELETED;
                        newMessage.mFlagRead = true;
                        newMessage.mServerId = oldMessage.getUid();
                        newMessage.save(context);
                    } else {
                        LegacyConversions.updateMessageFields(newMessage, oldMessage,
                                newAccount.mId, conversion.mailbox.mId);
                        EmailContent.Body newBody = new EmailContent.Body();
                        ArrayList<Part> viewables = new ArrayList<Part>();
                        ArrayList<Part> attachments = new ArrayList<Part>();
                        MimeUtility.collectParts(oldMessage, viewables, attachments);
                        LegacyConversions.updateBodyFields(newBody, newMessage, viewables);
                        newMessage.save(context);
                        newBody.save(context);
                        if (localAttachments) {
                            LegacyConversions.updateAttachments(context, newMessage, attachments,
                                    true);
                        }
                    }
                    if (handler != null) {
                        handler.incProgress(accountNum);
                    }
                } catch (MessagingException me) {
                    e = me;
                } catch (IOException ioe) {
                    e = ioe;
                }
                if (e != null) {
                    Log.d(Email.LOG_TAG, "Exception copying message " + oldMessage.getSubject()
                            + ": "+ e);
                    if (handler != null) {
                        handler.error(accountNum,
                                context.getString(R.string.upgrade_accounts_error));
                    }
                }
            }
        } catch (MessagingException e) {
            Log.d(Email.LOG_TAG, "Exception while copying messages in " +
                    conversion.folder.toString() + ": " + e);
            if (handler != null) {
                handler.error(accountNum, context.getString(R.string.upgrade_accounts_error));
            }
        }
    }
     static void deleteAccountStore(Context context, Account account, int accountNum,
            UIHandler handler) {
        try {
            Store store = LocalStore.newInstance(account.getLocalStoreUri(), context, null);
            store.delete();
        } catch (MessagingException e) {
            Log.d(Email.LOG_TAG, "Exception while deleting account " + e);
            if (handler != null) {
                handler.error(accountNum, context.getString(R.string.upgrade_accounts_error));
            }
        }
    }
     static void cleanupConnections(Context context, EmailContent.Account newAccount,
            Account account) {
        String email = newAccount.mEmailAddress;
        int atSignPos = email.lastIndexOf('@');
        String domain = email.substring(atSignPos + 1);
        Provider p = AccountSettingsUtils.findProviderForDomain(context, domain);
        if (p != null) {
            try {
                URI incomingUriTemplate = p.incomingUriTemplate;
                String incomingUsername = newAccount.mHostAuthRecv.mLogin;
                String incomingPassword = newAccount.mHostAuthRecv.mPassword;
                URI incomingUri = new URI(incomingUriTemplate.getScheme(), incomingUsername + ":"
                        + incomingPassword, incomingUriTemplate.getHost(),
                        incomingUriTemplate.getPort(), incomingUriTemplate.getPath(), null, null);
                newAccount.mHostAuthRecv.setStoreUri(incomingUri.toString());
            } catch (URISyntaxException e) {
            }
            try {
                URI outgoingUriTemplate = p.outgoingUriTemplate;
                String outgoingUsername = newAccount.mHostAuthSend.mLogin;
                String outgoingPassword = newAccount.mHostAuthSend.mPassword;
                URI outgoingUri = new URI(outgoingUriTemplate.getScheme(), outgoingUsername + ":"
                        + outgoingPassword, outgoingUriTemplate.getHost(),
                        outgoingUriTemplate.getPort(), outgoingUriTemplate.getPath(), null, null);
                newAccount.mHostAuthSend.setStoreUri(outgoingUri.toString());
            } catch (URISyntaxException e) {
            }
            Log.d(Email.LOG_TAG, "Rewriting connection details for " + account.getDescription());
            return;
        }
        newAccount.mHostAuthRecv.mFlags |= HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        String receiveUri = account.getStoreUri();
        if (receiveUri.contains("+ssl+")) {
        } else if (receiveUri.contains("+ssl")) {
        } else if (receiveUri.contains("+tls+")) {
        } else if (receiveUri.contains("+tls")) {
        }
        newAccount.mHostAuthSend.mFlags |= HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        String sendUri = account.getSenderUri();
        if (sendUri.contains("+ssl+")) {
        } else if (sendUri.contains("+ssl")) {
        } else if (sendUri.contains("+tls+")) {
        } else if (sendUri.contains("+tls")) {
        }
    }
     static boolean doBulkUpgradeIfNecessary(Context context) {
        if (bulkUpgradesRequired(context, Preferences.getPreferences(context))) {
            UpgradeAccounts.actionStart(context);
            return true;
        }
        return false;
    }
    private static boolean bulkUpgradesRequired(Context context, Preferences preferences) {
        if (DEBUG_FORCE_UPGRADES) {
            Account fake = new Account(context);
            fake.setDescription("Fake Account");
            fake.setEmail("user@gmail.com");
            fake.setName("First Last");
            fake.setSenderUri("smtp:
            fake.setStoreUri("imap:
            fake.save(preferences);
            return true;
        }
        Account[] legacyAccounts = preferences.getAccounts();
        if (legacyAccounts.length == 0) {
            return false;
        }
        if (0 != (legacyAccounts[0].getBackupFlags() & Account.BACKUP_FLAGS_IS_BACKUP)) {
            return false;
        } else {
            return true;
        }
    }
}
