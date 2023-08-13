public class AccountFolderList extends ListActivity implements OnItemClickListener {
    private static final int DIALOG_REMOVE_ACCOUNT = 1;
    private static int[] secretKeyCodes = {
            KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_U,
            KeyEvent.KEYCODE_G
    };
    private int mSecretKeyCodeIndex = 0;
    private static final String ICICLE_SELECTED_ACCOUNT = "com.android.email.selectedAccount";
    private EmailContent.Account mSelectedContextAccount;
    private ListView mListView;
    private ProgressBar mProgressIcon;
    private AccountsAdapter mListAdapter;
    private LoadAccountsTask mLoadAccountsTask;
    private DeleteAccountTask mDeleteAccountTask;
    private MessageListHandler mHandler;
    private ControllerResults mControllerCallback;
    public final static int MAILBOX_COLUMN_ID = 0;
    public final static int MAILBOX_DISPLAY_NAME = 1;
    public final static int MAILBOX_ACCOUNT_KEY = 2;
    public final static int MAILBOX_TYPE = 3;
    public final static int MAILBOX_UNREAD_COUNT = 4;
    public final static int MAILBOX_FLAG_VISIBLE = 5;
    public final static int MAILBOX_FLAGS = 6;
    public final static String[] MAILBOX_PROJECTION = new String[] {
        EmailContent.RECORD_ID, MailboxColumns.DISPLAY_NAME,
        MailboxColumns.ACCOUNT_KEY, MailboxColumns.TYPE,
        MailboxColumns.UNREAD_COUNT,
        MailboxColumns.FLAG_VISIBLE, MailboxColumns.FLAGS
    };
    private static final String FAVORITE_COUNT_SELECTION =
        MessageColumns.FLAG_FAVORITE + "= 1";
    private static final String MAILBOX_TYPE_SELECTION =
        MailboxColumns.TYPE + " =?";
    private static final String MAILBOX_ID_SELECTION =
        MessageColumns.MAILBOX_KEY + " =?";
    private static final String[] MAILBOX_SUM_OF_UNREAD_COUNT_PROJECTION = new String [] {
        "sum(" + MailboxColumns.UNREAD_COUNT + ")"
    };
    private static final String MAILBOX_INBOX_SELECTION =
        MailboxColumns.ACCOUNT_KEY + " =?" + " AND " + MailboxColumns.TYPE +" = "
        + Mailbox.TYPE_INBOX;
    private static final int MAILBOX_UNREAD_COUNT_COLUMN_UNREAD_COUNT = 0;
    private static final String[] MAILBOX_UNREAD_COUNT_PROJECTION = new String [] {
        MailboxColumns.UNREAD_COUNT
    };
    public static void actionShowAccounts(Context context) {
        Intent i = new Intent(context, AccountFolderList.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.account_folder_list);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.list_title);
        mHandler = new MessageListHandler();
        mControllerCallback = new ControllerResults();
        mProgressIcon = (ProgressBar) findViewById(R.id.title_progress_icon);
        mListView = getListView();
        mListView.setItemsCanFocus(false);
        mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        mListView.setOnItemClickListener(this);
        mListView.setLongClickable(true);
        registerForContextMenu(mListView);
        if (icicle != null && icicle.containsKey(ICICLE_SELECTED_ACCOUNT)) {
            mSelectedContextAccount = (Account) icicle.getParcelable(ICICLE_SELECTED_ACCOUNT);
        }
        ((TextView) findViewById(R.id.title_left_text)).setText(R.string.app_name);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSelectedContextAccount != null) {
            outState.putParcelable(ICICLE_SELECTED_ACCOUNT, mSelectedContextAccount);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Controller.getInstance(getApplication()).removeResultCallback(mControllerCallback);
    }
    @Override
    public void onResume() {
        super.onResume();
        NotificationManager notifMgr = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notifMgr.cancel(1);
        Controller.getInstance(getApplication()).addResultCallback(mControllerCallback);
        if (Email.getNotifyUiAccountsChanged()) {
            Welcome.actionStart(this);
            finish();
            return;
        }
        updateAccounts();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTaskInterrupt(mLoadAccountsTask);
        mLoadAccountsTask = null;
        Utility.cancelTask(mDeleteAccountTask, false); 
        mDeleteAccountTask = null;
        if (mListAdapter != null) { 
            mListAdapter.changeCursor(null);
        }
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListAdapter.isMailbox(position)) {
            MessageList.actionHandleMailbox(this, id);
        } else if (mListAdapter.isAccount(position)) {
            MessageList.actionHandleAccount(this, id, Mailbox.TYPE_INBOX); 
        }
    }
    private static int getUnreadCountByMailboxType(Context context, int type) {
        int count = 0;
        Cursor c = context.getContentResolver().query(Mailbox.CONTENT_URI,
                MAILBOX_SUM_OF_UNREAD_COUNT_PROJECTION,
                MAILBOX_TYPE_SELECTION,
                new String[] { String.valueOf(type) }, null);
        try {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        } finally {
            c.close();
        }
        return count;
    }
    private static int getCountByMailboxType(Context context, int type) {
        int count = 0;
        Cursor c = context.getContentResolver().query(Mailbox.CONTENT_URI,
                EmailContent.ID_PROJECTION, MAILBOX_TYPE_SELECTION,
                new String[] { String.valueOf(type) }, null);
        try {
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                count += EmailContent.count(context, Message.CONTENT_URI,
                        MAILBOX_ID_SELECTION,
                        new String[] {
                            String.valueOf(c.getLong(EmailContent.ID_PROJECTION_COLUMN)) });
            }
        } finally {
            c.close();
        }
        return count;
    }
    private MatrixCursor getSummaryChildCursor() {
        MatrixCursor childCursor = new MatrixCursor(MAILBOX_PROJECTION);
        int count;
        RowBuilder row;
        count = getUnreadCountByMailboxType(this, Mailbox.TYPE_INBOX);
        row = childCursor.newRow();
        row.add(Long.valueOf(Mailbox.QUERY_ALL_INBOXES));   
        row.add(getString(R.string.account_folder_list_summary_inbox)); 
        row.add(null);                                          
        row.add(Integer.valueOf(Mailbox.TYPE_INBOX));           
        row.add(Integer.valueOf(count));                        
        count = EmailContent.count(this, Message.CONTENT_URI, FAVORITE_COUNT_SELECTION, null);
        if (count > 0) {
            row = childCursor.newRow();
            row.add(Long.valueOf(Mailbox.QUERY_ALL_FAVORITES)); 
            row.add(getString(R.string.account_folder_list_summary_starred));
            row.add(null);                                          
            row.add(Integer.valueOf(Mailbox.TYPE_MAIL));            
            row.add(Integer.valueOf(count));                        
        }
        count = getCountByMailboxType(this, Mailbox.TYPE_DRAFTS);
        if (count > 0) {
            row = childCursor.newRow();
            row.add(Long.valueOf(Mailbox.QUERY_ALL_DRAFTS));    
            row.add(getString(R.string.account_folder_list_summary_drafts));
            row.add(null);                                          
            row.add(Integer.valueOf(Mailbox.TYPE_DRAFTS));          
            row.add(Integer.valueOf(count));                        
        }
        count = getCountByMailboxType(this, Mailbox.TYPE_OUTBOX);
        if (count > 0) {
            row = childCursor.newRow();
            row.add(Long.valueOf(Mailbox.QUERY_ALL_OUTBOX));    
            row.add(getString(R.string.account_folder_list_summary_outbox));
            row.add(null);                                          
            row.add(Integer.valueOf(Mailbox.TYPE_OUTBOX));          
            row.add(Integer.valueOf(count));                        
        }
        return childCursor;
    }
    private class LoadAccountsTask extends AsyncTask<Void, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Void... params) {
            Cursor c1 = getSummaryChildCursor();
            Cursor c2 = getContentResolver().query(
                    EmailContent.Account.CONTENT_URI,
                    EmailContent.Account.CONTENT_PROJECTION, null, null, null);
            Long defaultAccount = Account.getDefaultAccountId(AccountFolderList.this);
            return new Object[] { c1, c2 , defaultAccount};
        }
        @Override
        protected void onPostExecute(Object[] params) {
            if (isCancelled() || params == null || ((Cursor)params[1]).isClosed()) {
                return;
            }
            ListAdapter oldAdapter = mListView.getAdapter();
            if (oldAdapter != null && oldAdapter instanceof CursorAdapter) {
                ((CursorAdapter)oldAdapter).changeCursor(null);
            }
            mListAdapter = AccountsAdapter.getInstance((Cursor)params[0], (Cursor)params[1],
                    AccountFolderList.this, (Long)params[2]);
            mListView.setAdapter(mListAdapter);
        }
    }
    private class DeleteAccountTask extends AsyncTask<Void, Void, Void> {
        private final long mAccountId;
        private final String mAccountUri;
        public DeleteAccountTask(long accountId, String accountUri) {
            mAccountId = accountId;
            mAccountUri = accountUri;
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Store.getInstance(mAccountUri, getApplication(), null).delete();
                Store.removeInstance(mAccountUri);
                Uri uri = ContentUris.withAppendedId(
                        EmailContent.Account.CONTENT_URI, mAccountId);
                AccountFolderList.this.getContentResolver().delete(uri, null, null);
                AccountBackupRestore.backupAccounts(AccountFolderList.this);
                SecurityPolicy.getInstance(AccountFolderList.this).reducePolicies();
            } catch (Exception e) {
            }
            Email.setServicesEnabled(AccountFolderList.this);
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            if (!isCancelled()) {
                updateAccounts();
            }
        }
    }
    private void updateAccounts() {
        Utility.cancelTaskInterrupt(mLoadAccountsTask);
        mLoadAccountsTask = (LoadAccountsTask) new LoadAccountsTask().execute();
    }
    private void onAddNewAccount() {
        AccountSetupBasics.actionNewAccount(this);
    }
    private void onEditAccount(long accountId) {
        AccountSettings.actionSettings(this, accountId);
    }
    private void onRefresh(long accountId) {
        if (accountId == -1) {
            Toast.makeText(this, getString(R.string.account_folder_list_refresh_toast),
                    Toast.LENGTH_LONG).show();
        } else {
            mHandler.progress(true);
            Controller.getInstance(getApplication()).updateMailboxList(
                    accountId, mControllerCallback);
        }
    }
    private void onCompose(long accountId) {
        if (accountId == -1) {
            accountId = Account.getDefaultAccountId(this);
        }
        if (accountId != -1) {
            MessageCompose.actionCompose(this, accountId);
        } else {
            onAddNewAccount();
        }
    }
    private void onDeleteAccount(long accountId) {
        mSelectedContextAccount = Account.restoreAccountWithId(this, accountId);
        showDialog(DIALOG_REMOVE_ACCOUNT);
    }
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_REMOVE_ACCOUNT:
                return createRemoveAccountDialog();
        }
        return super.onCreateDialog(id);
    }
    private Dialog createRemoveAccountDialog() {
        return new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.account_delete_dlg_title)
            .setMessage(getString(R.string.account_delete_dlg_instructions_fmt,
                    mSelectedContextAccount.getDisplayName()))
            .setPositiveButton(R.string.okay_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dismissDialog(DIALOG_REMOVE_ACCOUNT);
                    NotificationManager notificationManager = (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(MailService.NOTIFICATION_ID_NEW_MESSAGES);
                    int numAccounts = EmailContent.count(AccountFolderList.this,
                            Account.CONTENT_URI, null, null);
                    mListAdapter.addOnDeletingAccount(mSelectedContextAccount.mId);
                    mDeleteAccountTask = (DeleteAccountTask) new DeleteAccountTask(
                            mSelectedContextAccount.mId,
                            mSelectedContextAccount.getStoreUri(AccountFolderList.this)).execute();
                    if (numAccounts == 1) {
                        AccountSetupBasics.actionNewAccount(AccountFolderList.this);
                        finish();
                    }
                }
            })
            .setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dismissDialog(DIALOG_REMOVE_ACCOUNT);
                }
            })
            .create();
    }
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_REMOVE_ACCOUNT:
                AlertDialog alert = (AlertDialog) dialog;
                alert.setMessage(getString(R.string.account_delete_dlg_instructions_fmt,
                        mSelectedContextAccount.getDisplayName()));
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo =
            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (mListAdapter.isMailbox(menuInfo.position)) {
            Cursor c = (Cursor) mListView.getItemAtPosition(menuInfo.position);
            long id = c.getLong(MAILBOX_COLUMN_ID);
            switch (item.getItemId()) {
                case R.id.open_folder:
                    MessageList.actionHandleMailbox(this, id);
                    break;
                case R.id.check_mail:
                    onRefresh(-1);
                    break;
            }
            return false;
        } else if (mListAdapter.isAccount(menuInfo.position)) {
            Cursor c = (Cursor) mListView.getItemAtPosition(menuInfo.position);
            long accountId = c.getLong(Account.CONTENT_ID_COLUMN);
            switch (item.getItemId()) {
                case R.id.open_folder:
                    MailboxList.actionHandleAccount(this, accountId);
                    break;
                case R.id.compose:
                    onCompose(accountId);
                    break;
                case R.id.refresh_account:
                    onRefresh(accountId);
                    break;
                case R.id.edit_account:
                    onEditAccount(accountId);
                    break;
                case R.id.delete_account:
                    onDeleteAccount(accountId);
                    break;
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_account:
                onAddNewAccount();
                break;
            case R.id.check_mail:
                onRefresh(-1);
                break;
            case R.id.compose:
                onCompose(-1);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.account_folder_list_option, menu);
        return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
        super.onCreateContextMenu(menu, v, info);
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) info;
        if (mListAdapter.isMailbox(menuInfo.position)) {
            Cursor c = (Cursor) mListView.getItemAtPosition(menuInfo.position);
            String displayName = c.getString(Account.CONTENT_DISPLAY_NAME_COLUMN);
            menu.setHeaderTitle(displayName);
            getMenuInflater().inflate(R.menu.account_folder_list_smart_folder_context, menu);
        } else if (mListAdapter.isAccount(menuInfo.position)) {
            Cursor c = (Cursor) mListView.getItemAtPosition(menuInfo.position);
            String accountName = c.getString(Account.CONTENT_DISPLAY_NAME_COLUMN);
            menu.setHeaderTitle(accountName);
            getMenuInflater().inflate(R.menu.account_folder_list_context, menu);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == secretKeyCodes[mSecretKeyCodeIndex]) {
            mSecretKeyCodeIndex++;
            if (mSecretKeyCodeIndex == secretKeyCodes.length) {
                mSecretKeyCodeIndex = 0;
                startActivity(new Intent(this, Debug.class));
            }
        } else {
            mSecretKeyCodeIndex = 0;
        }
        return super.onKeyDown(keyCode, event);
    }
    private class MessageListHandler extends Handler {
        private static final int MSG_PROGRESS = 1;
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS:
                    boolean showProgress = (msg.arg1 != 0);
                    if (showProgress) {
                        mProgressIcon.setVisibility(View.VISIBLE);
                    } else {
                        mProgressIcon.setVisibility(View.GONE);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        public void progress(boolean progress) {
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_PROGRESS;
            msg.arg1 = progress ? 1 : 0;
            sendMessage(msg);
        }
    }
    private class ControllerResults implements Controller.Result {
        public void updateMailboxListCallback(MessagingException result, long accountKey,
                int progress) {
            updateProgress(result, progress);
        }
        public void updateMailboxCallback(MessagingException result, long accountKey,
                long mailboxKey, int progress, int numNewMessages) {
            if (result != null || progress == 100) {
                Email.updateMailboxRefreshTime(mailboxKey);
            }
            if (progress == 100) {
                updateAccounts();
            }
            updateProgress(result, progress);
        }
        public void loadMessageForViewCallback(MessagingException result, long messageId,
                int progress) {
        }
        public void loadAttachmentCallback(MessagingException result, long messageId,
                long attachmentId, int progress) {
        }
        public void serviceCheckMailCallback(MessagingException result, long accountId,
                long mailboxId, int progress, long tag) {
            updateProgress(result, progress);
        }
        public void sendMailCallback(MessagingException result, long accountId, long messageId,
                int progress) {
            if (progress == 100) {
                updateAccounts();
            }
        }
        private void updateProgress(MessagingException result, int progress) {
            if (result != null || progress == 100) {
                mHandler.progress(false);
            } else if (progress == 0) {
                mHandler.progress(true);
            }
        }
    }
     static class AccountsAdapter extends CursorAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final int mMailboxesCount;
        private final int mSeparatorPosition;
        private final long mDefaultAccountId;
        private final ArrayList<Long> mOnDeletingAccounts = new ArrayList<Long>();
        public static AccountsAdapter getInstance(Cursor mailboxesCursor, Cursor accountsCursor,
                Context context, long defaultAccountId) {
            Cursor[] cursors = new Cursor[] { mailboxesCursor, accountsCursor };
            Cursor mc = new MergeCursor(cursors);
            return new AccountsAdapter(mc, context, mailboxesCursor.getCount(), defaultAccountId);
        }
        public AccountsAdapter(Cursor c, Context context, int mailboxesCount,
                long defaultAccountId) {
            super(context, c, true);
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMailboxesCount = mailboxesCount;
            mSeparatorPosition = mailboxesCount;
            mDefaultAccountId = defaultAccountId;
        }
        public boolean isMailbox(int position) {
            return position < mMailboxesCount;
        }
        public boolean isAccount(int position) {
            return position >= mMailboxesCount;
        }
        public void addOnDeletingAccount(long accountId) {
            mOnDeletingAccounts.add(accountId);
        }
        public boolean isOnDeletingAccountView(long accountId) {
            return mOnDeletingAccounts.contains(accountId);
        }
        public void onClickFolder(AccountFolderListItem itemView) {
            MailboxList.actionHandleAccount(mContext, itemView.mAccountId);
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor.getPosition() < mMailboxesCount) {
                bindMailboxItem(view, context, cursor, false);
            } else {
                bindAccountItem(view, context, cursor, false);
            }
        }
        private void bindMailboxItem(View view, Context context, Cursor cursor, boolean isLastChild)
                {
            AccountFolderListItem itemView = (AccountFolderListItem) view;
            itemView.bindViewInit(this, false);
            view.findViewById(R.id.chip).setVisibility(View.INVISIBLE);
            String text = cursor.getString(MAILBOX_DISPLAY_NAME);
            if (text != null) {
                TextView nameView = (TextView) view.findViewById(R.id.name);
                nameView.setText(text);
            }
            text = null;
            TextView statusView = (TextView) view.findViewById(R.id.status);
            if (text != null) {
                statusView.setText(text);
                statusView.setVisibility(View.VISIBLE);
            } else {
                statusView.setVisibility(View.GONE);
            }
            int count = -1;
            text = cursor.getString(MAILBOX_UNREAD_COUNT);
            if (text != null) {
                count = Integer.valueOf(text);
            }
            TextView unreadCountView = (TextView) view.findViewById(R.id.new_message_count);
            TextView allCountView = (TextView) view.findViewById(R.id.all_message_count);
            int id = cursor.getInt(MAILBOX_COLUMN_ID);
            if (count > 0) {
                if (id == Mailbox.QUERY_ALL_FAVORITES
                        || id == Mailbox.QUERY_ALL_DRAFTS
                        || id == Mailbox.QUERY_ALL_OUTBOX) {
                    unreadCountView.setVisibility(View.GONE);
                    allCountView.setVisibility(View.VISIBLE);
                    allCountView.setText(text);
                } else {
                    allCountView.setVisibility(View.GONE);
                    unreadCountView.setVisibility(View.VISIBLE);
                    unreadCountView.setText(text);
                }
            } else {
                allCountView.setVisibility(View.GONE);
                unreadCountView.setVisibility(View.GONE);
            }
            view.findViewById(R.id.folder_button).setVisibility(View.GONE);
            view.findViewById(R.id.folder_separator).setVisibility(View.GONE);
            view.findViewById(R.id.default_sender).setVisibility(View.GONE);
            view.findViewById(R.id.folder_icon).setVisibility(View.VISIBLE);
            ((ImageView)view.findViewById(R.id.folder_icon)).setImageDrawable(
                    Utility.FolderProperties.getInstance(context).getSummaryMailboxIconIds(id));
        }
        private void bindAccountItem(View view, Context context, Cursor cursor, boolean isExpanded)
                {
            AccountFolderListItem itemView = (AccountFolderListItem) view;
            itemView.bindViewInit(this, true);
            itemView.mAccountId = cursor.getLong(Account.CONTENT_ID_COLUMN);
            long accountId = cursor.getLong(Account.CONTENT_ID_COLUMN);
            View chipView = view.findViewById(R.id.chip);
            chipView.setBackgroundResource(Email.getAccountColorResourceId(accountId));
            chipView.setVisibility(View.VISIBLE);
            String text = cursor.getString(Account.CONTENT_DISPLAY_NAME_COLUMN);
            if (text != null) {
                TextView descriptionView = (TextView) view.findViewById(R.id.name);
                descriptionView.setText(text);
            }
            text = cursor.getString(Account.CONTENT_EMAIL_ADDRESS_COLUMN);
            if (text != null) {
                TextView emailView = (TextView) view.findViewById(R.id.status);
                emailView.setText(text);
                emailView.setVisibility(View.VISIBLE);
            }
            int unreadMessageCount = 0;
            Cursor c = context.getContentResolver().query(Mailbox.CONTENT_URI,
                    MAILBOX_UNREAD_COUNT_PROJECTION,
                    MAILBOX_INBOX_SELECTION,
                    new String[] { String.valueOf(accountId) }, null);
            try {
                if (c.moveToFirst()) {
                    String count = c.getString(MAILBOX_UNREAD_COUNT_COLUMN_UNREAD_COUNT);
                    if (count != null) {
                        unreadMessageCount = Integer.valueOf(count);
                    }
                }
            } finally {
                c.close();
            }
            view.findViewById(R.id.all_message_count).setVisibility(View.GONE);
            TextView unreadCountView = (TextView) view.findViewById(R.id.new_message_count);
            if (unreadMessageCount > 0) {
                unreadCountView.setText(String.valueOf(unreadMessageCount));
                unreadCountView.setVisibility(View.VISIBLE);
            } else {
                unreadCountView.setVisibility(View.GONE);
            }
            view.findViewById(R.id.folder_icon).setVisibility(View.GONE);
            view.findViewById(R.id.folder_button).setVisibility(View.VISIBLE);
            view.findViewById(R.id.folder_separator).setVisibility(View.VISIBLE);
            if (accountId == mDefaultAccountId) {
                view.findViewById(R.id.default_sender).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.default_sender).setVisibility(View.GONE);
            }
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.account_folder_list_item, parent, false);
        }
        @Override
        public int getItemViewType(int position) {
            if (position == mSeparatorPosition) {
                return IGNORE_ITEM_VIEW_TYPE;
            }
            return super.getItemViewType(position);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == mSeparatorPosition) {
                TextView view;
                view = (TextView) mInflater.inflate(R.layout.list_separator, parent, false);
                view.setText(R.string.account_folder_list_separator_accounts);
                return view;
            }
            return super.getView(getRealPosition(position), convertView, parent);
        }
        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }
        @Override
        public boolean isEnabled(int position) {
            if (position == mSeparatorPosition) {
                return false;
            } else if (isAccount(position)) {
                Long id = ((MergeCursor)getItem(position)).getLong(Account.CONTENT_ID_COLUMN);
                return !isOnDeletingAccountView(id);
            } else {
                return true;
            }
        }
        @Override
        public int getCount() {
            int count = super.getCount();
            if (count > 0 && (mSeparatorPosition != ListView.INVALID_POSITION)) {
                count += 1;
            }
            return count;
        }
        private int getRealPosition(int pos) {
            if (mSeparatorPosition == ListView.INVALID_POSITION) {
                return pos;
            } else if (pos <= mSeparatorPosition) {
                return pos;
            } else {
                return pos - 1;
            }
        }
        @Override
        public Object getItem(int pos) {
            return super.getItem(getRealPosition(pos));
        }
        @Override
        public long getItemId(int pos) {
            return super.getItemId(getRealPosition(pos));
        }
    }
}
