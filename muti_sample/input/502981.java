public abstract class AbstractSyncAdapter {
    public static final int SECONDS = 1000;
    public static final int MINUTES = SECONDS*60;
    public static final int HOURS = MINUTES*60;
    public static final int DAYS = HOURS*24;
    public static final int WEEKS = DAYS*7;
    public Mailbox mMailbox;
    public EasSyncService mService;
    public Context mContext;
    public Account mAccount;
    public final android.accounts.Account mAccountManagerAccount;
    public abstract boolean sendLocalChanges(Serializer s)
        throws IOException;
    public abstract boolean parse(InputStream is)
        throws IOException;
    public abstract String getCollectionName();
    public abstract void cleanup();
    public abstract boolean isSyncable();
    public boolean isLooping() {
        return false;
    }
    public AbstractSyncAdapter(Mailbox mailbox, EasSyncService service) {
        mMailbox = mailbox;
        mService = service;
        mContext = service.mContext;
        mAccount = service.mAccount;
        mAccountManagerAccount = new android.accounts.Account(mAccount.mEmailAddress,
                Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
    }
    public void userLog(String ...strings) {
        mService.userLog(strings);
    }
    public void incrementChangeCount() {
        mService.mChangeCount++;
    }
    public String getSyncKey() throws IOException {
        if (mMailbox.mSyncKey == null) {
            userLog("Reset SyncKey to 0");
            mMailbox.mSyncKey = "0";
        }
        return mMailbox.mSyncKey;
    }
    public void setSyncKey(String syncKey, boolean inCommands) throws IOException {
        mMailbox.mSyncKey = syncKey;
    }
}
