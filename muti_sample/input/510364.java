public class MessageList extends ListActivity implements OnItemClickListener, OnClickListener,
        AnimationListener {
    private static final String EXTRA_ACCOUNT_ID = "com.android.email.activity._ACCOUNT_ID";
    private static final String EXTRA_MAILBOX_TYPE = "com.android.email.activity.MAILBOX_TYPE";
    private static final String EXTRA_MAILBOX_ID = "com.android.email.activity.MAILBOX_ID";
    private static final String STATE_SELECTED_ITEM_TOP =
        "com.android.email.activity.MessageList.selectedItemTop";
    private static final String STATE_SELECTED_POSITION =
        "com.android.email.activity.MessageList.selectedPosition";
    private static final String STATE_CHECKED_ITEMS =
        "com.android.email.activity.MessageList.checkedItems";
    private static final int REQUEST_SECURITY = 0;
    private ListView mListView;
    private View mMultiSelectPanel;
    private Button mReadUnreadButton;
    private Button mFavoriteButton;
    private Button mDeleteButton;
    private View mListFooterView;
    private TextView mListFooterText;
    private View mListFooterProgress;
    private TextView mErrorBanner;
    private static final int LIST_FOOTER_MODE_NONE = 0;
    private static final int LIST_FOOTER_MODE_REFRESH = 1;
    private static final int LIST_FOOTER_MODE_MORE = 2;
    private static final int LIST_FOOTER_MODE_SEND = 3;
    private int mListFooterMode;
    private MessageListAdapter mListAdapter;
    private MessageListHandler mHandler;
    private final Controller mController = Controller.getInstance(getApplication());
    private ControllerResults mControllerCallback;
    private TextView mLeftTitle;
    private ProgressBar mProgressIcon;
    private ContentResolver mResolver;
    private long mMailboxId;
    private LoadMessagesTask mLoadMessagesTask;
    private FindMailboxTask mFindMailboxTask;
    private SetTitleTask mSetTitleTask;
    private SetFooterTask mSetFooterTask;
    public final static String[] MAILBOX_FIND_INBOX_PROJECTION = new String[] {
        EmailContent.RECORD_ID, MailboxColumns.TYPE, MailboxColumns.FLAG_VISIBLE
    };
    private static final int MAILBOX_NAME_COLUMN_ID = 0;
    private static final int MAILBOX_NAME_COLUMN_ACCOUNT_KEY = 1;
    private static final int MAILBOX_NAME_COLUMN_TYPE = 2;
    private static final String[] MAILBOX_NAME_PROJECTION = new String[] {
            MailboxColumns.DISPLAY_NAME, MailboxColumns.ACCOUNT_KEY,
            MailboxColumns.TYPE};
    private static final int ACCOUNT_DISPLAY_NAME_COLUMN_ID = 0;
    private static final String[] ACCOUNT_NAME_PROJECTION = new String[] {
            AccountColumns.DISPLAY_NAME };
    private static final int ACCOUNT_INFO_COLUMN_FLAGS = 0;
    private static final String[] ACCOUNT_INFO_PROJECTION = new String[] {
            AccountColumns.FLAGS };
    private static final String ID_SELECTION = EmailContent.RECORD_ID + "=?";
    private Boolean mPushModeMailbox = null;
    private int mSavedItemTop = 0;
    private int mSavedItemPosition = -1;
    private int mFirstSelectedItemTop = 0;
    private int mFirstSelectedItemPosition = -1;
    private int mFirstSelectedItemHeight = -1;
    private boolean mCanAutoRefresh = false;
     static final String[] MESSAGE_PROJECTION = new String[] {
        EmailContent.RECORD_ID, MessageColumns.MAILBOX_KEY, MessageColumns.ACCOUNT_KEY,
        MessageColumns.DISPLAY_NAME, MessageColumns.SUBJECT, MessageColumns.TIMESTAMP,
        MessageColumns.FLAG_READ, MessageColumns.FLAG_FAVORITE, MessageColumns.FLAG_ATTACHMENT,
        MessageColumns.FLAGS,
    };
    public static void actionHandleMailbox(Context context, long id) {
        context.startActivity(createIntent(context, -1, id, -1));
    }
    public static void actionHandleAccount(Context context, long accountId, int mailboxType) {
        context.startActivity(createIntent(context, accountId, -1, mailboxType));
    }
    public static void actionOpenAccountInboxUuid(Context context, String accountUuid) {
        Intent i = createIntent(context, -1, -1, Mailbox.TYPE_INBOX);
        i.setData(Account.getShortcutSafeUriFromUuid(accountUuid));
        context.startActivity(i);
    }
    public static Intent createIntent(Context context, long accountId, long mailboxId,
            int mailboxType) {
        Intent intent = new Intent(context, MessageList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (accountId != -1) intent.putExtra(EXTRA_ACCOUNT_ID, accountId);
        if (mailboxId != -1) intent.putExtra(EXTRA_MAILBOX_ID, mailboxId);
        if (mailboxType != -1) intent.putExtra(EXTRA_MAILBOX_TYPE, mailboxType);
        return intent;
    }
    public static Intent createAccountIntentForShortcut(Context context, Account account,
            int mailboxType) {
        Intent i = createIntent(context, -1, -1, mailboxType);
        i.setData(account.getShortcutSafeUri());
        return i;
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.message_list);
        mHandler = new MessageListHandler();
        mControllerCallback = new ControllerResults();
        mCanAutoRefresh = true;
        mListView = getListView();
        mMultiSelectPanel = findViewById(R.id.footer_organize);
        mReadUnreadButton = (Button) findViewById(R.id.btn_read_unread);
        mFavoriteButton = (Button) findViewById(R.id.btn_multi_favorite);
        mDeleteButton = (Button) findViewById(R.id.btn_multi_delete);
        mLeftTitle = (TextView) findViewById(R.id.title_left_text);
        mProgressIcon = (ProgressBar) findViewById(R.id.title_progress_icon);
        mErrorBanner = (TextView) findViewById(R.id.connection_error_text);
        mReadUnreadButton.setOnClickListener(this);
        mFavoriteButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        ((Button) findViewById(R.id.account_title_button)).setOnClickListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setItemsCanFocus(false);
        registerForContextMenu(mListView);
        mListAdapter = new MessageListAdapter(this);
        setListAdapter(mListAdapter);
        mResolver = getContentResolver();
        selectAccountAndMailbox(getIntent());
    }
    private void selectAccountAndMailbox(Intent intent) {
        mMailboxId = intent.getLongExtra(EXTRA_MAILBOX_ID, -1);
        if (mMailboxId != -1) {
            mSetTitleTask = new SetTitleTask(mMailboxId);
            mSetTitleTask.execute();
            mLoadMessagesTask = new LoadMessagesTask(mMailboxId, -1);
            mLoadMessagesTask.execute();
            addFooterView(mMailboxId, -1, -1);
        } else {
            int mailboxType = intent.getIntExtra(EXTRA_MAILBOX_TYPE, Mailbox.TYPE_INBOX);
            Uri uri = intent.getData();
            long accountId = (uri == null) ? -1
                    : Account.getAccountIdFromShortcutSafeUri(this, uri);
            if (accountId != -1) {
                mFindMailboxTask = new FindMailboxTask(accountId, mailboxType, false);
                mFindMailboxTask.execute();
            } else {
                accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
                mFindMailboxTask = new FindMailboxTask(accountId, mailboxType, true);
                mFindMailboxTask.execute();
            }
            addFooterView(-1, accountId, mailboxType);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mController.removeResultCallback(mControllerCallback);
    }
    @Override
    public void onResume() {
        super.onResume();
        mController.addResultCallback(mControllerCallback);
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(MailService.NOTIFICATION_ID_NEW_MESSAGES);
        if (Email.getNotifyUiAccountsChanged()) {
            Welcome.actionStart(this);
            finish();
            return;
        }
        restoreListPosition();
        autoRefreshStaleMailbox();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utility.cancelTaskInterrupt(mLoadMessagesTask);
        mLoadMessagesTask = null;
        Utility.cancelTaskInterrupt(mFindMailboxTask);
        mFindMailboxTask = null;
        Utility.cancelTaskInterrupt(mSetTitleTask);
        mSetTitleTask = null;
        Utility.cancelTaskInterrupt(mSetFooterTask);
        mSetFooterTask = null;
        mListAdapter.changeCursor(null);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveListPosition();
        outState.putInt(STATE_SELECTED_POSITION, mSavedItemPosition);
        outState.putInt(STATE_SELECTED_ITEM_TOP, mSavedItemTop);
        Set<Long> checkedset = mListAdapter.getSelectedSet();
        long[] checkedarray = new long[checkedset.size()];
        int i = 0;
        for (Long l : checkedset) {
            checkedarray[i] = l;
            i++;
        }
        outState.putLongArray(STATE_CHECKED_ITEMS, checkedarray);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSavedItemTop = savedInstanceState.getInt(STATE_SELECTED_ITEM_TOP, 0);
        mSavedItemPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION, -1);
        Set<Long> checkedset = mListAdapter.getSelectedSet();
        for (long l: savedInstanceState.getLongArray(STATE_CHECKED_ITEMS)) {
            checkedset.add(l);
        }
    }
    private void saveListPosition() {
        mSavedItemPosition = getListView().getSelectedItemPosition();
        if (mSavedItemPosition >= 0 && getListView().isSelected()) {
            mSavedItemTop = getListView().getSelectedView().getTop();
        } else {
            mSavedItemPosition = getListView().getFirstVisiblePosition();
            if (mSavedItemPosition >= 0) {
                mSavedItemTop = 0;
                View topChild = getListView().getChildAt(0);
                if (topChild != null) {
                    mSavedItemTop = topChild.getTop();
                }
            }
        }
    }
    private void restoreListPosition() {
        if (mSavedItemPosition >= 0 && mSavedItemPosition < getListView().getCount()) {
            getListView().setSelectionFromTop(mSavedItemPosition, mSavedItemTop);
            mSavedItemPosition = -1;
            mSavedItemTop = 0;
        }
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view != mListFooterView) {
            MessageListItem itemView = (MessageListItem) view;
            onOpenMessage(id, itemView.mMailboxId);
        } else {
            doFooterClick();
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_read_unread:
                onMultiToggleRead(mListAdapter.getSelectedSet());
                break;
            case R.id.btn_multi_favorite:
                onMultiToggleFavorite(mListAdapter.getSelectedSet());
                break;
            case R.id.btn_multi_delete:
                onMultiDelete(mListAdapter.getSelectedSet());
                break;
            case R.id.account_title_button:
                onAccounts();
                break;
        }
    }
    public void onAnimationEnd(Animation animation) {
        updateListPosition();
    }
    public void onAnimationRepeat(Animation animation) {
    }
    public void onAnimationStart(Animation animation) {
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mMailboxId < 0) {
            getMenuInflater().inflate(R.menu.message_list_option_smart_folder, menu);
        } else {
            getMenuInflater().inflate(R.menu.message_list_option, menu);
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean showDeselect = mListAdapter.getSelectedSet().size() > 0;
        menu.setGroupVisible(R.id.deselect_all_group, showDeselect);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                onRefresh();
                return true;
            case R.id.folders:
                onFolders();
                return true;
            case R.id.accounts:
                onAccounts();
                return true;
            case R.id.compose:
                onCompose();
                return true;
            case R.id.account_settings:
                onEditAccount();
                return true;
            case R.id.deselect_all:
                onDeselectAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.targetView == mListFooterView) {
            return;
        }
        MessageListItem itemView = (MessageListItem) info.targetView;
        Cursor c = (Cursor) mListView.getItemAtPosition(info.position);
        String messageName = c.getString(MessageListAdapter.COLUMN_SUBJECT);
        menu.setHeaderTitle(messageName);
        Mailbox mailbox = Mailbox.restoreMailboxWithId(this, itemView.mMailboxId);
        if (mailbox == null) {
            return;
        }
        switch (mailbox.mType) {
            case EmailContent.Mailbox.TYPE_DRAFTS:
                getMenuInflater().inflate(R.menu.message_list_context_drafts, menu);
                break;
            case EmailContent.Mailbox.TYPE_OUTBOX:
                getMenuInflater().inflate(R.menu.message_list_context_outbox, menu);
                break;
            case EmailContent.Mailbox.TYPE_TRASH:
                getMenuInflater().inflate(R.menu.message_list_context_trash, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.message_list_context, menu);
                if (itemView.mRead) {
                    menu.findItem(R.id.mark_as_read).setTitle(R.string.mark_as_unread_action);
                }
                break;
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
            (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MessageListItem itemView = (MessageListItem) info.targetView;
        switch (item.getItemId()) {
            case R.id.open:
                onOpenMessage(info.id, itemView.mMailboxId);
                break;
            case R.id.delete:
                onDelete(info.id, itemView.mAccountId);
                break;
            case R.id.reply:
                onReply(itemView.mMessageId);
                break;
            case R.id.reply_all:
                onReplyAll(itemView.mMessageId);
                break;
            case R.id.forward:
                onForward(itemView.mMessageId);
                break;
            case R.id.mark_as_read:
                onSetMessageRead(info.id, !itemView.mRead);
                break;
        }
        return super.onContextItemSelected(item);
    }
    private void onRefresh() {
        if (mMailboxId >= 0) {
            Mailbox mailbox = Mailbox.restoreMailboxWithId(this, mMailboxId);
            if (mailbox != null) {
                mController.updateMailbox(mailbox.mAccountKey, mMailboxId, mControllerCallback);
            }
        }
    }
    private void onFolders() {
        if (mMailboxId >= 0) {
            Mailbox mailbox = Mailbox.restoreMailboxWithId(this, mMailboxId);
            if (mailbox != null) {
                MailboxList.actionHandleAccount(this, mailbox.mAccountKey);
                finish();
            }
        }
    }
    private void onAccounts() {
        AccountFolderList.actionShowAccounts(this);
        finish();
    }
    private long lookupAccountIdFromMailboxId(long mailboxId) {
        if (mailboxId < 0) {
            return -1; 
        }
        EmailContent.Mailbox mailbox =
            EmailContent.Mailbox.restoreMailboxWithId(this, mailboxId);
        if (mailbox == null) {
            return -2;
        }
        return mailbox.mAccountKey;
    }
    private void onCompose() {
        long accountKey = lookupAccountIdFromMailboxId(mMailboxId);
        if (accountKey > -2) {
            MessageCompose.actionCompose(this, accountKey);
        } else {
            finish();
        }
    }
    private void onEditAccount() {
        long accountKey = lookupAccountIdFromMailboxId(mMailboxId);
        if (accountKey > -2) {
            AccountSettings.actionSettings(this, accountKey);
        } else {
            finish();
        }
    }
    private void onDeselectAll() {
        mListAdapter.getSelectedSet().clear();
        mListView.invalidateViews();
        showMultiPanel(false);
    }
    private void onOpenMessage(long messageId, long mailboxId) {
        EmailContent.Mailbox mailbox = EmailContent.Mailbox.restoreMailboxWithId(this, mailboxId);
        if (mailbox == null) {
            return;
        }
        if (mailbox.mType == EmailContent.Mailbox.TYPE_DRAFTS) {
            MessageCompose.actionEditDraft(this, messageId);
        } else {
            final boolean disableReply = (mailbox.mType == EmailContent.Mailbox.TYPE_TRASH);
            MessageView.actionView(this, messageId, mMailboxId, disableReply);
        }
    }
    private void onReply(long messageId) {
        MessageCompose.actionReply(this, messageId, false);
    }
    private void onReplyAll(long messageId) {
        MessageCompose.actionReply(this, messageId, true);
    }
    private void onForward(long messageId) {
        MessageCompose.actionForward(this, messageId);
    }
    private void onLoadMoreMessages() {
        if (mMailboxId >= 0) {
            mController.loadMoreMessages(mMailboxId, mControllerCallback);
        }
    }
    private void onSendPendingMessages() {
        if (mMailboxId == Mailbox.QUERY_ALL_OUTBOX) {
            Cursor c = mResolver.query(Account.CONTENT_URI, Account.ID_PROJECTION,
                    null, null, null);
            try {
                while (c.moveToNext()) {
                    long accountId = c.getLong(Account.ID_PROJECTION_COLUMN);
                    mController.sendPendingMessages(accountId, mControllerCallback);
                }
            } finally {
                c.close();
            }
        } else {
            long accountKey = lookupAccountIdFromMailboxId(mMailboxId);
            if (accountKey > -2) {
                mController.sendPendingMessages(accountKey, mControllerCallback);
            } else {
                finish();
            }
        }
    }
    private void onDelete(long messageId, long accountId) {
        mController.deleteMessage(messageId, accountId);
        Toast.makeText(this, getResources().getQuantityString(
                R.plurals.message_deleted_toast, 1), Toast.LENGTH_SHORT).show();
    }
    private void onSetMessageRead(long messageId, boolean newRead) {
        mController.setMessageRead(messageId, newRead);
    }
    private void onSetMessageFavorite(long messageId, boolean newFavorite) {
        mController.setMessageFavorite(messageId, newFavorite);
    }
    private void onMultiToggleRead(Set<Long> selectedSet) {
        toggleMultiple(selectedSet, new MultiToggleHelper() {
            public boolean getField(long messageId, Cursor c) {
                return c.getInt(MessageListAdapter.COLUMN_READ) == 0;
            }
            public boolean setField(long messageId, Cursor c, boolean newValue) {
                boolean oldValue = getField(messageId, c);
                if (oldValue != newValue) {
                    onSetMessageRead(messageId, !newValue);
                    return true;
                }
                return false;
            }
        });
    }
    private void onMultiToggleFavorite(Set<Long> selectedSet) {
        toggleMultiple(selectedSet, new MultiToggleHelper() {
            public boolean getField(long messageId, Cursor c) {
                return c.getInt(MessageListAdapter.COLUMN_FAVORITE) != 0;
            }
            public boolean setField(long messageId, Cursor c, boolean newValue) {
                boolean oldValue = getField(messageId, c);
                if (oldValue != newValue) {
                    onSetMessageFavorite(messageId, newValue);
                    return true;
                }
                return false;
            }
        });
    }
    private void onMultiDelete(Set<Long> selectedSet) {
        HashSet<Long> cloneSet = new HashSet<Long>(selectedSet);
        for (Long id : cloneSet) {
            mController.deleteMessage(id, -1);
        }
        Toast.makeText(this, getResources().getQuantityString(
                R.plurals.message_deleted_toast, cloneSet.size()), Toast.LENGTH_SHORT).show();
        selectedSet.clear();
        showMultiPanel(false);
    }
    private interface MultiToggleHelper {
        public boolean getField(long messageId, Cursor c);
        public boolean setField(long messageId, Cursor c, boolean newValue);
    }
    private int toggleMultiple(Set<Long> selectedSet, MultiToggleHelper helper) {
        Cursor c = mListAdapter.getCursor();
        boolean anyWereFound = false;
        boolean allWereSet = true;
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            long id = c.getInt(MessageListAdapter.COLUMN_ID);
            if (selectedSet.contains(Long.valueOf(id))) {
                anyWereFound = true;
                if (!helper.getField(id, c)) {
                    allWereSet = false;
                    break;
                }
            }
        }
        int numChanged = 0;
        if (anyWereFound) {
            boolean newValue = !allWereSet;
            c.moveToPosition(-1);
            while (c.moveToNext()) {
                long id = c.getInt(MessageListAdapter.COLUMN_ID);
                if (selectedSet.contains(Long.valueOf(id))) {
                    if (helper.setField(id, c, newValue)) {
                        ++numChanged;
                    }
                }
            }
        }
        return numChanged;
    }
    private boolean testMultiple(Set<Long> selectedSet, int column_id, boolean defaultflag) {
        Cursor c = mListAdapter.getCursor();
        if (c == null || c.isClosed()) {
            return false;
        }
        c.moveToPosition(-1);
        while (c.moveToNext()) {
            long id = c.getInt(MessageListAdapter.COLUMN_ID);
            if (selectedSet.contains(Long.valueOf(id))) {
                if (c.getInt(column_id) == (defaultflag? 1 : 0)) {
                    return true;
                }
            }
        }
        return false;
    }
    private void autoRefreshStaleMailbox() {
        if (!mCanAutoRefresh
                || (mListAdapter.getCursor() == null) 
                || (mPushModeMailbox != null && mPushModeMailbox) 
                || (mMailboxId < 0)) { 
            return;
        }
        mCanAutoRefresh = false;
        if (!Email.mailboxRequiresRefresh(mMailboxId)) {
            return;
        }
        onRefresh();
    }
    private void updateFooterButtonNames () {
        if (testMultiple(mListAdapter.getSelectedSet(), MessageListAdapter.COLUMN_READ, true)) {
            mReadUnreadButton.setText(R.string.unread_action);
        } else {
            mReadUnreadButton.setText(R.string.read_action);
        }
        if (testMultiple(mListAdapter.getSelectedSet(),
                MessageListAdapter.COLUMN_FAVORITE, false)) {
            mFavoriteButton.setText(R.string.set_star_action);
        } else {
            mFavoriteButton.setText(R.string.remove_star_action);
        }
    }
    private void updateListPosition () {
        int listViewHeight = getListView().getHeight();
        if (mListAdapter.getSelectedSet().size() == 1 && mFirstSelectedItemPosition >= 0
                && mFirstSelectedItemPosition < getListView().getCount()
                && listViewHeight < mFirstSelectedItemTop) {
            getListView().setSelectionFromTop(mFirstSelectedItemPosition,
                    listViewHeight - mFirstSelectedItemHeight);
        }
    }
    private void showMultiPanel(boolean show) {
        if (show && mMultiSelectPanel.getVisibility() != View.VISIBLE) {
            mMultiSelectPanel.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.footer_appear);
            animation.setAnimationListener(this);
            mMultiSelectPanel.startAnimation(animation);
        } else if (!show && mMultiSelectPanel.getVisibility() != View.GONE) {
            mMultiSelectPanel.setVisibility(View.GONE);
            mMultiSelectPanel.startAnimation(
                        AnimationUtils.loadAnimation(this, R.anim.footer_disappear));
        }
        if (show) {
            updateFooterButtonNames();
        }
    }
    private void addFooterView(long mailboxId, long accountId, int mailboxType) {
        if (mailboxId == Mailbox.QUERY_ALL_INBOXES
                || mailboxId == Mailbox.QUERY_ALL_UNREAD
                || mailboxId == Mailbox.QUERY_ALL_FAVORITES) {
            finishFooterView(LIST_FOOTER_MODE_REFRESH);
            return;
        }
        if (mailboxId == Mailbox.QUERY_ALL_DRAFTS || mailboxType == Mailbox.TYPE_DRAFTS) {
            finishFooterView(LIST_FOOTER_MODE_NONE);
            return;
        }
        if (mailboxId == Mailbox.QUERY_ALL_OUTBOX || mailboxType == Mailbox.TYPE_OUTBOX) {
            finishFooterView(LIST_FOOTER_MODE_SEND);
            return;
        }
        mSetFooterTask = new SetFooterTask();
        mSetFooterTask.execute(mailboxId, accountId);
    }
    private final static String[] MAILBOX_ACCOUNT_AND_TYPE_PROJECTION =
        new String[] { MailboxColumns.ACCOUNT_KEY, MailboxColumns.TYPE };
    private class SetFooterTask extends AsyncTask<Long, Void, Integer> {
        @Override
        protected Integer doInBackground(Long... params) {
            long mailboxId = params[0];
            long accountId = params[1];
            int mailboxType = -1;
            if (mailboxId != -1) {
                try {
                    Uri uri = ContentUris.withAppendedId(Mailbox.CONTENT_URI, mailboxId);
                    Cursor c = mResolver.query(uri, MAILBOX_ACCOUNT_AND_TYPE_PROJECTION,
                            null, null, null);
                    if (c.moveToFirst()) {
                        try {
                            accountId = c.getLong(0);
                            mailboxType = c.getInt(1);
                        } finally {
                            c.close();
                        }
                    }
                } catch (IllegalArgumentException iae) {
                    return LIST_FOOTER_MODE_NONE;
                }
            }
            switch (mailboxType) {
                case Mailbox.TYPE_OUTBOX:
                    return LIST_FOOTER_MODE_SEND;
                case Mailbox.TYPE_DRAFTS:
                    return LIST_FOOTER_MODE_NONE;
            }
            if (accountId != -1) {
                Account account = Account.restoreAccountWithId(MessageList.this, accountId);
                if (account != null) {
                    mPushModeMailbox = account.mSyncInterval == Account.CHECK_INTERVAL_PUSH;
                    if (MessageList.this.mController.isMessagingController(account)) {
                        return LIST_FOOTER_MODE_MORE;       
                    } else {
                        return LIST_FOOTER_MODE_NONE;    
                    }
                }
            }
            return LIST_FOOTER_MODE_NONE;
        }
        @Override
        protected void onPostExecute(Integer listFooterMode) {
            if (listFooterMode == null) {
                return;
            }
            finishFooterView(listFooterMode);
        }
    }
    private void finishFooterView(int listFooterMode) {
        mListFooterMode = listFooterMode;
        if (mListFooterMode != LIST_FOOTER_MODE_NONE) {
            mListFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.message_list_item_footer, mListView, false);
            getListView().addFooterView(mListFooterView);
            setListAdapter(mListAdapter);
            mListFooterProgress = mListFooterView.findViewById(R.id.progress);
            mListFooterText = (TextView) mListFooterView.findViewById(R.id.main_text);
            setListFooterText(false);
        }
    }
    private void setListFooterText(boolean active) {
        if (mListFooterMode != LIST_FOOTER_MODE_NONE) {
            int footerTextId = 0;
            switch (mListFooterMode) {
                case LIST_FOOTER_MODE_REFRESH:
                    footerTextId = active ? R.string.status_loading_more
                                          : R.string.refresh_action;
                    break;
                case LIST_FOOTER_MODE_MORE:
                    footerTextId = active ? R.string.status_loading_more
                                          : R.string.message_list_load_more_messages_action;
                    break;
                case LIST_FOOTER_MODE_SEND:
                    footerTextId = active ? R.string.status_sending_messages
                                          : R.string.message_list_send_pending_messages_action;
                    break;
            }
            mListFooterText.setText(footerTextId);
        }
    }
    private void doFooterClick() {
        switch (mListFooterMode) {
            case LIST_FOOTER_MODE_NONE:         
                break;
            case LIST_FOOTER_MODE_REFRESH:
                onRefresh();
                break;
            case LIST_FOOTER_MODE_MORE:
                onLoadMoreMessages();
                break;
            case LIST_FOOTER_MODE_SEND:
                onSendPendingMessages();
                break;
        }
    }
    private class FindMailboxTask extends AsyncTask<Void, Void, Long> {
        private final long mAccountId;
        private final int mMailboxType;
        private final boolean mOkToRecurse;
        private boolean showWelcomeActivity;
        private boolean showSecurityActivity;
        public FindMailboxTask(long accountId, int mailboxType, boolean okToRecurse) {
            mAccountId = accountId;
            mMailboxType = mailboxType;
            mOkToRecurse = okToRecurse;
            showWelcomeActivity = false;
            showSecurityActivity = false;
        }
        @Override
        protected Long doInBackground(Void... params) {
            if (mAccountId != -1 && isSecurityHold(mAccountId)) {
                showSecurityActivity = true;
                return Long.valueOf(-1);
            }
            long mailboxId = Mailbox.findMailboxOfType(MessageList.this, mAccountId, mMailboxType);
            if (mailboxId == Mailbox.NO_MAILBOX) {
                final boolean accountExists = Account.isValidId(MessageList.this, mAccountId);
                if (accountExists && mOkToRecurse) {
                    mControllerCallback.presetMailboxListCallback(mMailboxType, mAccountId);
                    mController.updateMailboxList(mAccountId, mControllerCallback);
                } else {
                    showWelcomeActivity = true;
                }
            }
            return mailboxId;
        }
        @Override
        protected void onPostExecute(Long mailboxId) {
            if (showSecurityActivity) {
                Intent i = AccountSecurity.actionUpdateSecurityIntent(
                        MessageList.this, mAccountId);
                MessageList.this.startActivityForResult(i, REQUEST_SECURITY);
                return;
            }
            if (showWelcomeActivity) {
                Welcome.actionStart(MessageList.this);
                finish();
                return;
            }
            if (mailboxId == null || mailboxId == Mailbox.NO_MAILBOX) {
                return;
            }
            mMailboxId = mailboxId;
            mSetTitleTask = new SetTitleTask(mMailboxId);
            mSetTitleTask.execute();
            mLoadMessagesTask = new LoadMessagesTask(mMailboxId, mAccountId);
            mLoadMessagesTask.execute();
        }
    }
    private boolean isSecurityHold(long accountId) {
        Cursor c = MessageList.this.getContentResolver().query(
                ContentUris.withAppendedId(Account.CONTENT_URI, accountId),
                ACCOUNT_INFO_PROJECTION, null, null, null);
        try {
            if (c.moveToFirst()) {
                int flags = c.getInt(ACCOUNT_INFO_COLUMN_FLAGS);
                if ((flags & Account.FLAGS_SECURITY_HOLD) != 0) {
                    return true;
                }
            }
        } finally {
            c.close();
        }
        return false;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SECURITY:
                onAccounts();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private class LoadMessagesTask extends AsyncTask<Void, Void, Cursor> {
        private long mMailboxKey;
        private long mAccountKey;
        public LoadMessagesTask(long mailboxKey, long accountKey) {
            mMailboxKey = mailboxKey;
            mAccountKey = accountKey;
        }
        @Override
        protected Cursor doInBackground(Void... params) {
            String selection =
                Utility.buildMailboxIdSelection(MessageList.this.mResolver, mMailboxKey);
            Cursor c = MessageList.this.managedQuery(
                    EmailContent.Message.CONTENT_URI, MESSAGE_PROJECTION,
                    selection, null, EmailContent.MessageColumns.TIMESTAMP + " DESC");
            return c;
        }
        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor == null || cursor.isClosed()) {
                return;
            }
            MessageList.this.mListAdapter.changeCursor(cursor);
            restoreListPosition();
            autoRefreshStaleMailbox();
            if (mMailboxKey == Mailbox.QUERY_ALL_INBOXES) {
                MailService.resetNewMessageCount(MessageList.this, -1);
            } else if (mMailboxKey >= 0 && mAccountKey != -1) {
                MailService.resetNewMessageCount(MessageList.this, mAccountKey);
            }
        }
    }
    private class SetTitleTask extends AsyncTask<Void, Void, Object[]> {
        private long mMailboxKey;
        public SetTitleTask(long mailboxKey) {
            mMailboxKey = mailboxKey;
        }
        @Override
        protected Object[] doInBackground(Void... params) {
            int resIdSpecialMailbox = 0;
            if (mMailboxKey == Mailbox.QUERY_ALL_INBOXES) {
                resIdSpecialMailbox = R.string.account_folder_list_summary_inbox;
            } else if (mMailboxKey == Mailbox.QUERY_ALL_FAVORITES) {
                resIdSpecialMailbox = R.string.account_folder_list_summary_starred;
            } else if (mMailboxKey == Mailbox.QUERY_ALL_DRAFTS) {
                resIdSpecialMailbox = R.string.account_folder_list_summary_drafts;
            } else if (mMailboxKey == Mailbox.QUERY_ALL_OUTBOX) {
                resIdSpecialMailbox = R.string.account_folder_list_summary_outbox;
            }
            if (resIdSpecialMailbox != 0) {
                return new Object[] {null, getString(resIdSpecialMailbox), 0};
            }
            String accountName = null;
            String mailboxName = null;
            String accountKey = null;
            Cursor c = MessageList.this.mResolver.query(Mailbox.CONTENT_URI,
                    MAILBOX_NAME_PROJECTION, ID_SELECTION,
                    new String[] { Long.toString(mMailboxKey) }, null);
            try {
                if (c.moveToFirst()) {
                    mailboxName = Utility.FolderProperties.getInstance(MessageList.this)
                            .getDisplayName(c.getInt(MAILBOX_NAME_COLUMN_TYPE));
                    if (mailboxName == null) {
                        mailboxName = c.getString(MAILBOX_NAME_COLUMN_ID);
                    }
                    accountKey = c.getString(MAILBOX_NAME_COLUMN_ACCOUNT_KEY);
                }
            } finally {
                c.close();
            }
            if (accountKey != null) {
                c = MessageList.this.mResolver.query(Account.CONTENT_URI,
                        ACCOUNT_NAME_PROJECTION, ID_SELECTION, new String[] { accountKey },
                        null);
                try {
                    if (c.moveToFirst()) {
                        accountName = c.getString(ACCOUNT_DISPLAY_NAME_COLUMN_ID);
                    }
                } finally {
                    c.close();
                }
            }
            int nAccounts = EmailContent.count(MessageList.this, Account.CONTENT_URI, null, null);
            return new Object[] {accountName, mailboxName, nAccounts};
        }
        @Override
        protected void onPostExecute(Object[] result) {
            if (result == null) {
                return;
            }
            final int nAccounts = (Integer) result[2];
            if (result[0] != null) {
                setTitleAccountName((String) result[0], nAccounts > 1);
            }
            if (result[1] != null) {
                mLeftTitle.setText((String) result[1]);
            }
        }
    }
    private void setTitleAccountName(String accountName, boolean showAccountsButton) {
        TextView accountsButton = (TextView) findViewById(R.id.account_title_button);
        TextView textPlain = (TextView) findViewById(R.id.title_right_text);
        if (showAccountsButton) {
            accountsButton.setVisibility(View.VISIBLE);
            textPlain.setVisibility(View.GONE);
            accountsButton.setText(accountName);
        } else {
            accountsButton.setVisibility(View.GONE);
            textPlain.setVisibility(View.VISIBLE);
            textPlain.setText(accountName);
        }
    }
    class MessageListHandler extends Handler {
        private static final int MSG_PROGRESS = 1;
        private static final int MSG_LOOKUP_MAILBOX_TYPE = 2;
        private static final int MSG_ERROR_BANNER = 3;
        private static final int MSG_REQUERY_LIST = 4;
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_PROGRESS:
                    boolean visible = (msg.arg1 != 0);
                    if (visible) {
                        mProgressIcon.setVisibility(View.VISIBLE);
                    } else {
                        mProgressIcon.setVisibility(View.GONE);
                    }
                    if (mListFooterProgress != null) {
                        mListFooterProgress.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }
                    setListFooterText(visible);
                    break;
                case MSG_LOOKUP_MAILBOX_TYPE:
                    if (mFindMailboxTask != null &&
                            mFindMailboxTask.getStatus() != FindMailboxTask.Status.FINISHED) {
                        mFindMailboxTask.cancel(true);
                        mFindMailboxTask = null;
                    }
                    long accountId = ((Long)msg.obj).longValue();
                    int mailboxType = msg.arg1;
                    mFindMailboxTask = new FindMailboxTask(accountId, mailboxType, false);
                    mFindMailboxTask.execute();
                    break;
                case MSG_ERROR_BANNER:
                    String message = (String) msg.obj;
                    boolean isVisible = mErrorBanner.getVisibility() == View.VISIBLE;
                    if (message != null) {
                        mErrorBanner.setText(message);
                        if (!isVisible) {
                            mErrorBanner.setVisibility(View.VISIBLE);
                            mErrorBanner.startAnimation(
                                    AnimationUtils.loadAnimation(
                                            MessageList.this, R.anim.header_appear));
                        }
                    } else {
                        if (isVisible) {
                            mErrorBanner.setVisibility(View.GONE);
                            mErrorBanner.startAnimation(
                                    AnimationUtils.loadAnimation(
                                            MessageList.this, R.anim.header_disappear));
                        }
                    }
                    break;
                case MSG_REQUERY_LIST:
                    mListAdapter.doRequery();
                    if (mMultiSelectPanel.getVisibility() == View.VISIBLE) {
                        updateFooterButtonNames();
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
        public void lookupMailboxType(long accountId, int mailboxType) {
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_LOOKUP_MAILBOX_TYPE;
            msg.arg1 = mailboxType;
            msg.obj = Long.valueOf(accountId);
            sendMessage(msg);
        }
        public void showErrorBanner(String message) {
            android.os.Message msg = android.os.Message.obtain();
            msg.what = MSG_ERROR_BANNER;
            msg.obj = message;
            sendMessage(msg);
        }
        public void requeryList() {
            sendEmptyMessage(MSG_REQUERY_LIST);
        }
    }
    private class ControllerResults implements Controller.Result {
        MessagingException mSendMessageException;
        private int mWaitForMailboxType = -1;
        private long mWaitForMailboxAccount = -1;
        public synchronized void presetMailboxListCallback(int mailboxType, long accountId) {
            mWaitForMailboxType = mailboxType;
            mWaitForMailboxAccount = accountId;
        }
        public synchronized void updateMailboxListCallback(MessagingException result,
                long accountKey, int progress) {
            updateProgress(result, progress);
            if (progress == 100 && accountKey == mWaitForMailboxAccount) {
                mWaitForMailboxAccount = -1;
                mHandler.lookupMailboxType(accountKey, mWaitForMailboxType);
            }
        }
        public void updateMailboxCallback(MessagingException result, long accountKey,
                long mailboxKey, int progress, int numNewMessages) {
            updateBanner(result, progress, mailboxKey);
            if (result != null || progress == 100) {
                Email.updateMailboxRefreshTime(mailboxKey);
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
        }
        public void sendMailCallback(MessagingException result, long accountId, long messageId,
                int progress) {
            if (mListFooterMode == LIST_FOOTER_MODE_SEND) {
                if (messageId == -1 && result == null && progress == 0) {
                    mSendMessageException = null;
                }
                if (result != null && mSendMessageException == null) {
                    mSendMessageException = result;
                }
                if (messageId == -1 && progress == 100) {
                    updateBanner(mSendMessageException, progress, mMailboxId);
                }
                updateProgress(result, progress);
            }
        }
        private void updateProgress(MessagingException result, int progress) {
            if (result != null || progress == 100) {
                mHandler.progress(false);
            } else if (progress == 0) {
                mHandler.progress(true);
            }
        }
        private void updateBanner(MessagingException result, int progress, long mailboxKey) {
            if (mailboxKey != mMailboxId) {
                return;
            }
            if (result != null) {
                int id = R.string.status_network_error;
                if (result instanceof AuthenticationFailedException) {
                    id = R.string.account_setup_failed_dlg_auth_message;
                } else if (result instanceof CertificateValidationException) {
                    id = R.string.account_setup_failed_dlg_certificate_message;
                } else {
                    switch (result.getExceptionType()) {
                        case MessagingException.IOERROR:
                            id = R.string.account_setup_failed_ioerror;
                            break;
                        case MessagingException.TLS_REQUIRED:
                            id = R.string.account_setup_failed_tls_required;
                            break;
                        case MessagingException.AUTH_REQUIRED:
                            id = R.string.account_setup_failed_auth_required;
                            break;
                        case MessagingException.GENERAL_SECURITY:
                            id = R.string.account_setup_failed_security;
                            break;
                        case MessagingException.SECURITY_POLICIES_REQUIRED:
                            id = R.string.account_setup_failed_security;
                            break;
                    }
                }
                mHandler.showErrorBanner(getString(id));
            } else if (progress > 0) {
                mHandler.showErrorBanner(null);
            }
        }
    }
     class MessageListAdapter extends CursorAdapter {
        public static final int COLUMN_ID = 0;
        public static final int COLUMN_MAILBOX_KEY = 1;
        public static final int COLUMN_ACCOUNT_KEY = 2;
        public static final int COLUMN_DISPLAY_NAME = 3;
        public static final int COLUMN_SUBJECT = 4;
        public static final int COLUMN_DATE = 5;
        public static final int COLUMN_READ = 6;
        public static final int COLUMN_FAVORITE = 7;
        public static final int COLUMN_ATTACHMENTS = 8;
        public static final int COLUMN_FLAGS = 9;
        Context mContext;
        private LayoutInflater mInflater;
        private Drawable mAttachmentIcon;
        private Drawable mInvitationIcon;
        private Drawable mFavoriteIconOn;
        private Drawable mFavoriteIconOff;
        private Drawable mSelectedIconOn;
        private Drawable mSelectedIconOff;
        private ColorStateList mTextColorPrimary;
        private ColorStateList mTextColorSecondary;
        private final RefreshTimer mRefreshTimer = new RefreshTimer();
        private long mLastRefreshTime = 0;
        private static final long REFRESH_INTERVAL_MS = 2500;
        private java.text.DateFormat mDateFormat;
        private java.text.DateFormat mTimeFormat;
        private HashSet<Long> mChecked = new HashSet<Long>();
        public MessageListAdapter(Context context) {
            super(context, null, true);
            mContext = context;
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Resources resources = context.getResources();
            mAttachmentIcon = resources.getDrawable(R.drawable.ic_mms_attachment_small);
            mInvitationIcon = resources.getDrawable(R.drawable.ic_calendar_event_small);
            mFavoriteIconOn = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_on);
            mFavoriteIconOff = resources.getDrawable(R.drawable.btn_star_big_buttonless_dark_off);
            mSelectedIconOn = resources.getDrawable(R.drawable.btn_check_buttonless_dark_on);
            mSelectedIconOff = resources.getDrawable(R.drawable.btn_check_buttonless_dark_off);
            Theme theme = context.getTheme();
            TypedArray array;
            array = theme.obtainStyledAttributes(new int[] { android.R.attr.textColorPrimary });
            mTextColorPrimary = resources.getColorStateList(array.getResourceId(0, 0));
            array = theme.obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
            mTextColorSecondary = resources.getColorStateList(array.getResourceId(0, 0));
            mDateFormat = android.text.format.DateFormat.getDateFormat(context);    
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);    
        }
        @Override
        protected synchronized void onContentChanged() {
            final Cursor cursor = getCursor();
            if (cursor != null && !cursor.isClosed()) {
                long sinceRefresh = SystemClock.elapsedRealtime() - mLastRefreshTime;
                mRefreshTimer.schedule(REFRESH_INTERVAL_MS - sinceRefresh);
            }
        }
        public void doRequery() {
            super.onContentChanged();
        }
        class RefreshTimer extends Timer {
            private TimerTask timerTask = null;
            protected void clear() {
                timerTask = null;
            }
            protected synchronized void schedule(long delay) {
                if (timerTask != null) return;
                if (delay < 0) {
                    refreshList();
                } else {
                    timerTask = new RefreshTimerTask();
                    schedule(timerTask, delay);
                }
            }
        }
        class RefreshTimerTask extends TimerTask {
            @Override
            public void run() {
                refreshList();
            }
        }
        private synchronized void refreshList() {
            if (Email.LOGD) {
                Log.d("messageList", "refresh: "
                        + (SystemClock.elapsedRealtime() - mLastRefreshTime) + "ms");
            }
            mHandler.requeryList();
            mLastRefreshTime = SystemClock.elapsedRealtime();
            mRefreshTimer.clear();
        }
        public Set<Long> getSelectedSet() {
            return mChecked;
        }
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            MessageListItem itemView = (MessageListItem) view;
            itemView.bindViewInit(this, true);
            itemView.mMessageId = cursor.getLong(COLUMN_ID);
            itemView.mMailboxId = cursor.getLong(COLUMN_MAILBOX_KEY);
            itemView.mAccountId = cursor.getLong(COLUMN_ACCOUNT_KEY);
            itemView.mRead = cursor.getInt(COLUMN_READ) != 0;
            itemView.mFavorite = cursor.getInt(COLUMN_FAVORITE) != 0;
            itemView.mSelected = mChecked.contains(Long.valueOf(itemView.mMessageId));
            View chipView = view.findViewById(R.id.chip);
            chipView.setBackgroundResource(Email.getAccountColorResourceId(itemView.mAccountId));
            TextView fromView = (TextView) view.findViewById(R.id.from);
            String text = cursor.getString(COLUMN_DISPLAY_NAME);
            fromView.setText(text);
            TextView subjectView = (TextView) view.findViewById(R.id.subject);
            text = cursor.getString(COLUMN_SUBJECT);
            subjectView.setText(text);
            boolean hasInvitation =
                        (cursor.getInt(COLUMN_FLAGS) & Message.FLAG_INCOMING_MEETING_INVITE) != 0;
            boolean hasAttachments = cursor.getInt(COLUMN_ATTACHMENTS) != 0;
            Drawable icon =
                    hasInvitation ? mInvitationIcon
                    : hasAttachments ? mAttachmentIcon : null;
            subjectView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
            TextView dateView = (TextView) view.findViewById(R.id.date);
            long timestamp = cursor.getLong(COLUMN_DATE);
            Date date = new Date(timestamp);
            if (Utility.isDateToday(date)) {
                text = mTimeFormat.format(date);
            } else {
                text = mDateFormat.format(date);
            }
            dateView.setText(text);
            if (itemView.mRead) {
                subjectView.setTypeface(Typeface.DEFAULT);
                fromView.setTypeface(Typeface.DEFAULT);
                fromView.setTextColor(mTextColorSecondary);
                view.setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.message_list_item_background_read));
            } else {
                subjectView.setTypeface(Typeface.DEFAULT_BOLD);
                fromView.setTypeface(Typeface.DEFAULT_BOLD);
                fromView.setTextColor(mTextColorPrimary);
                view.setBackgroundDrawable(context.getResources().getDrawable(
                        R.drawable.message_list_item_background_unread));
            }
            ImageView selectedView = (ImageView) view.findViewById(R.id.selected);
            selectedView.setImageDrawable(itemView.mSelected ? mSelectedIconOn : mSelectedIconOff);
            ImageView favoriteView = (ImageView) view.findViewById(R.id.favorite);
            favoriteView.setImageDrawable(itemView.mFavorite ? mFavoriteIconOn : mFavoriteIconOff);
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.message_list_item, parent, false);
        }
        public void updateSelected(MessageListItem itemView, boolean newSelected) {
            ImageView selectedView = (ImageView) itemView.findViewById(R.id.selected);
            selectedView.setImageDrawable(newSelected ? mSelectedIconOn : mSelectedIconOff);
            Long id = Long.valueOf(itemView.mMessageId);
            if (newSelected) {
                mChecked.add(id);
            } else {
                mChecked.remove(id);
            }
            if (mChecked.size() == 1 && newSelected) {
                mFirstSelectedItemPosition = getListView().getPositionForView(itemView);
                mFirstSelectedItemTop = itemView.getBottom();
                mFirstSelectedItemHeight = itemView.getHeight();
            } else {
                mFirstSelectedItemPosition = -1;
            }
            MessageList.this.showMultiPanel(mChecked.size() > 0);
        }
        public void updateFavorite(MessageListItem itemView, boolean newFavorite) {
            ImageView favoriteView = (ImageView) itemView.findViewById(R.id.favorite);
            favoriteView.setImageDrawable(newFavorite ? mFavoriteIconOn : mFavoriteIconOff);
            onSetMessageFavorite(itemView.mMessageId, newFavorite);
        }
    }
}
