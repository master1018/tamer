public abstract class AbstractSyncParser extends Parser {
    protected EasSyncService mService;
    protected Mailbox mMailbox;
    protected Account mAccount;
    protected Context mContext;
    protected ContentResolver mContentResolver;
    protected AbstractSyncAdapter mAdapter;
    private boolean mLooping;
    public AbstractSyncParser(InputStream in, AbstractSyncAdapter adapter) throws IOException {
        super(in);
        mAdapter = adapter;
        mService = adapter.mService;
        mContext = mService.mContext;
        mContentResolver = mContext.getContentResolver();
        mMailbox = mService.mMailbox;
        mAccount = mService.mAccount;
    }
    public abstract void commandsParser() throws IOException;
    public abstract void responsesParser() throws IOException;
    public abstract void commit() throws IOException;
    public abstract void wipe();
    public boolean isLooping() {
        return mLooping;
    }
    @Override
    public boolean parse() throws IOException {
        int status;
        boolean moreAvailable = false;
        boolean newSyncKey = false;
        int interval = mMailbox.mSyncInterval;
        mLooping = false;
        if (nextTag(START_DOCUMENT) != Tags.SYNC_SYNC) {
            throw new EasParserException();
        }
        boolean mailboxUpdated = false;
        ContentValues cv = new ContentValues();
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            if (tag == Tags.SYNC_COLLECTION || tag == Tags.SYNC_COLLECTIONS) {
            } else if (tag == Tags.SYNC_STATUS) {
                status = getValueInt();
                if (status != 1) {
                    mService.errorLog("Sync failed: " + status);
                    if (status == 3) {
                        mAdapter.setSyncKey("0", false);
                        mMailbox.mSyncInterval = Mailbox.CHECK_INTERVAL_PUSH;
                        mService.errorLog("Bad sync key; RESET and delete data");
                        wipe();
                        moreAvailable = true;
                    } else if (status == 8) {
                        SyncManager.reloadFolderList(mContext, mAccount.mId, true);
                    }
                }
            } else if (tag == Tags.SYNC_COMMANDS) {
                commandsParser();
            } else if (tag == Tags.SYNC_RESPONSES) {
                responsesParser();
            } else if (tag == Tags.SYNC_MORE_AVAILABLE) {
                moreAvailable = true;
            } else if (tag == Tags.SYNC_SYNC_KEY) {
                if (mAdapter.getSyncKey().equals("0")) {
                    moreAvailable = true;
                }
                String newKey = getValue();
                userLog("Parsed key for ", mMailbox.mDisplayName, ": ", newKey);
                if (!newKey.equals(mMailbox.mSyncKey)) {
                    mAdapter.setSyncKey(newKey, true);
                    cv.put(MailboxColumns.SYNC_KEY, newKey);
                    mailboxUpdated = true;
                    newSyncKey = true;
                }
                if (mMailbox.mSyncInterval == Mailbox.CHECK_INTERVAL_PUSH) {
                    mMailbox.mSyncInterval = Mailbox.CHECK_INTERVAL_PING;
                }
           } else {
                skipTag();
           }
        }
        if (moreAvailable && !newSyncKey) {
            mLooping = true;
        }
        commit();
        boolean abortSyncs = false;
        if (mMailbox.mSyncInterval != interval) {
            cv.put(MailboxColumns.SYNC_INTERVAL, mMailbox.mSyncInterval);
            mailboxUpdated = true;
        } else if (mService.mChangeCount > 0 &&
                mAccount.mSyncInterval == Account.CHECK_INTERVAL_PUSH &&
                mMailbox.mSyncInterval > 0) {
            userLog("Changes found to ping loop mailbox ", mMailbox.mDisplayName, ": will ping.");
            cv.put(MailboxColumns.SYNC_INTERVAL, Mailbox.CHECK_INTERVAL_PING);
            mailboxUpdated = true;
            abortSyncs = true;
        }
        if (mailboxUpdated) {
             synchronized (mService.getSynchronizer()) {
                if (!mService.isStopped()) {
                     mMailbox.update(mContext, cv);
                }
            }
        }
        if (abortSyncs) {
            userLog("Aborting account syncs due to mailbox change to ping...");
            SyncManager.stopAccountSyncs(mAccount.mId);
        }
        userLog("Returning moreAvailable = " + moreAvailable);
        return moreAvailable;
    }
    void userLog(String ...strings) {
        mService.userLog(strings);
    }
    void userLog(String string, int num, String string2) {
        mService.userLog(string, num, string2);
    }
}
