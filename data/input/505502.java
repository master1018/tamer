public abstract class AbstractThreadedSyncAdapter {
    public static final int LOG_SYNC_DETAILS = 2743;
    private final Context mContext;
    private final AtomicInteger mNumSyncStarts;
    private final ISyncAdapterImpl mISyncAdapterImpl;
    private SyncThread mSyncThread;
    private final Object mSyncThreadLock = new Object();
    private final boolean mAutoInitialize;
    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize) {
        mContext = context;
        mISyncAdapterImpl = new ISyncAdapterImpl();
        mNumSyncStarts = new AtomicInteger(0);
        mSyncThread = null;
        mAutoInitialize = autoInitialize;
    }
    public Context getContext() {
        return mContext;
    }
    private class ISyncAdapterImpl extends ISyncAdapter.Stub {
        public void startSync(ISyncContext syncContext, String authority, Account account,
                Bundle extras) {
            final SyncContext syncContextClient = new SyncContext(syncContext);
            boolean alreadyInProgress;
            synchronized (mSyncThreadLock) {
                if (mSyncThread == null) {
                    if (mAutoInitialize
                            && extras != null
                            && extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)) {
                        if (ContentResolver.getIsSyncable(account, authority) < 0) {
                            ContentResolver.setIsSyncable(account, authority, 1);
                        }
                        syncContextClient.onFinished(new SyncResult());
                        return;
                    }
                    mSyncThread = new SyncThread(
                            "SyncAdapterThread-" + mNumSyncStarts.incrementAndGet(),
                            syncContextClient, authority, account, extras);
                    mSyncThread.start();
                    alreadyInProgress = false;
                } else {
                    alreadyInProgress = true;
                }
            }
            if (alreadyInProgress) {
                syncContextClient.onFinished(SyncResult.ALREADY_IN_PROGRESS);
            }
        }
        public void cancelSync(ISyncContext syncContext) {
            final SyncThread syncThread;
            synchronized (mSyncThreadLock) {
                syncThread = mSyncThread;
            }
            if (syncThread != null
                    && syncThread.mSyncContext.getSyncContextBinder() == syncContext.asBinder()) {
                onSyncCanceled();
            }
        }
        public void initialize(Account account, String authority) throws RemoteException {
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, true);
            startSync(null, authority, account, extras);
        }
    }
    private class SyncThread extends Thread {
        private final SyncContext mSyncContext;
        private final String mAuthority;
        private final Account mAccount;
        private final Bundle mExtras;
        private SyncThread(String name, SyncContext syncContext, String authority,
                Account account, Bundle extras) {
            super(name);
            mSyncContext = syncContext;
            mAuthority = authority;
            mAccount = account;
            mExtras = extras;
        }
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (isCanceled()) {
                return;
            }
            SyncResult syncResult = new SyncResult();
            ContentProviderClient provider = null;
            try {
                provider = mContext.getContentResolver().acquireContentProviderClient(mAuthority);
                if (provider != null) {
                    AbstractThreadedSyncAdapter.this.onPerformSync(mAccount, mExtras,
                            mAuthority, provider, syncResult);
                } else {
                    syncResult.databaseError = true;
                }
            } finally {
                if (provider != null) {
                    provider.release();
                }
                if (!isCanceled()) {
                    mSyncContext.onFinished(syncResult);
                }
                synchronized (mSyncThreadLock) {
                    mSyncThread = null;
                }
            }
        }
        private boolean isCanceled() {
            return Thread.currentThread().isInterrupted();
        }
    }
    public final IBinder getSyncAdapterBinder() {
        return mISyncAdapterImpl.asBinder();
    }
    public abstract void onPerformSync(Account account, Bundle extras,
            String authority, ContentProviderClient provider, SyncResult syncResult);
    public void onSyncCanceled() {
        final SyncThread syncThread;
        synchronized (mSyncThreadLock) {
            syncThread = mSyncThread;
        }
        if (syncThread != null) {
            syncThread.interrupt();
        }
    }
}
