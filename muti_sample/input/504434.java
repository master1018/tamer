public class SyncManager implements OnAccountsUpdateListener {
    private static final String TAG = "SyncManager";
    private static final long LOCAL_SYNC_DELAY;
    private static final long MAX_TIME_PER_SYNC;
    static {
        String localSyncDelayString = SystemProperties.get("sync.local_sync_delay");
        long localSyncDelay = 30 * 1000; 
        if (localSyncDelayString != null) {
            try {
                localSyncDelay = Long.parseLong(localSyncDelayString);
            } catch (NumberFormatException nfe) {
            }
        }
        LOCAL_SYNC_DELAY = localSyncDelay;
        String maxTimePerSyncString = SystemProperties.get("sync.max_time_per_sync");
        long maxTimePerSync = 5 * 60 * 1000; 
        if (maxTimePerSyncString != null) {
            try {
                maxTimePerSync = Long.parseLong(maxTimePerSyncString);
            } catch (NumberFormatException nfe) {
            }
        }
        MAX_TIME_PER_SYNC = maxTimePerSync;
    }
    private static final long SYNC_NOTIFICATION_DELAY = 30 * 1000; 
    private static final long INITIAL_SYNC_RETRY_TIME_IN_MS = 30 * 1000; 
    private static final long DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS = 60 * 60; 
    private static final int DELAY_RETRY_SYNC_IN_PROGRESS_IN_SECONDS = 10;
    private static final long ERROR_NOTIFICATION_DELAY_MS = 1000 * 60 * 10; 
    private static final int INITIALIZATION_UNBIND_DELAY_MS = 5000;
    private static final String SYNC_WAKE_LOCK = "SyncManagerSyncWakeLock";
    private static final String HANDLE_SYNC_ALARM_WAKE_LOCK = "SyncManagerHandleSyncAlarmWakeLock";
    private Context mContext;
    private volatile Account[] mAccounts = INITIAL_ACCOUNTS_ARRAY;
    volatile private PowerManager.WakeLock mSyncWakeLock;
    volatile private PowerManager.WakeLock mHandleAlarmWakeLock;
    volatile private boolean mDataConnectionIsConnected = false;
    volatile private boolean mStorageIsLow = false;
    private final NotificationManager mNotificationMgr;
    private AlarmManager mAlarmService = null;
    private final SyncStorageEngine mSyncStorageEngine;
    public final SyncQueue mSyncQueue;
    private ActiveSyncContext mActiveSyncContext = null;
    private boolean mNeedSyncErrorNotification = false;
    private boolean mNeedSyncActiveNotification = false;
    private final PendingIntent mSyncAlarmIntent;
    private ConnectivityManager mConnManagerDoNotUseDirectly;
    private final SyncAdaptersCache mSyncAdapters;
    private BroadcastReceiver mStorageIntentReceiver =
            new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "Internal storage is low.");
                        }
                        mStorageIsLow = true;
                        cancelActiveSync(null , null );
                    } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "Internal storage is ok.");
                        }
                        mStorageIsLow = false;
                        sendCheckAlarmsMessage();
                    }
                }
            };
    private BroadcastReceiver mBootCompletedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            mSyncHandler.onBootCompleted();
        }
    };
    private BroadcastReceiver mBackgroundDataSettingChanged = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (getConnectivityManager().getBackgroundDataSetting()) {
                scheduleSync(null , null , new Bundle(), 0 ,
                        false );
            }
        }
    };
    private static final Account[] INITIAL_ACCOUNTS_ARRAY = new Account[0];
    public void onAccountsUpdated(Account[] accounts) {
        final boolean justBootedUp = mAccounts == INITIAL_ACCOUNTS_ARRAY;
        mAccounts = accounts;
        ActiveSyncContext activeSyncContext = mActiveSyncContext;
        if (activeSyncContext != null) {
            if (!ArrayUtils.contains(accounts, activeSyncContext.mSyncOperation.account)) {
                Log.d(TAG, "canceling sync since the account has been removed");
                sendSyncFinishedOrCanceledMessage(activeSyncContext,
                        null );
            }
        }
        sendCheckAlarmsMessage();
        if (mBootCompleted) {
            mSyncStorageEngine.doDatabaseCleanup(accounts);
        }
        if (accounts.length > 0) {
            boolean onlyThoseWithUnkownSyncableState = justBootedUp;
            scheduleSync(null, null, null, 0 , onlyThoseWithUnkownSyncableState);
        }
    }
    private BroadcastReceiver mConnectivityIntentReceiver =
            new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NetworkInfo networkInfo =
                    intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = (networkInfo == null ? NetworkInfo.State.UNKNOWN :
                    networkInfo.getState());
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "received connectivity action.  network info: " + networkInfo);
            }
            switch (state) {
                case CONNECTED:
                    mDataConnectionIsConnected = true;
                    break;
                case DISCONNECTED:
                    if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                        mDataConnectionIsConnected = false;
                    } else {
                        mDataConnectionIsConnected = true;
                    }
                    break;
                default:
            }
            if (mDataConnectionIsConnected) {
                sendCheckAlarmsMessage();
            }
        }
    };
    private BroadcastReceiver mShutdownIntentReceiver =
            new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.w(TAG, "Writing sync state before shutdown...");
            getSyncStorageEngine().writeAllState();
        }
    };
    private static final String ACTION_SYNC_ALARM = "android.content.syncmanager.SYNC_ALARM";
    private final SyncHandler mSyncHandler;
    private final Handler mMainHandler;
    private volatile boolean mBootCompleted = false;
    private ConnectivityManager getConnectivityManager() {
        synchronized (this) {
            if (mConnManagerDoNotUseDirectly == null) {
                mConnManagerDoNotUseDirectly = (ConnectivityManager)mContext.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
            }
            return mConnManagerDoNotUseDirectly;
        }
    }
    public SyncManager(Context context, boolean factoryTest) {
        SyncStorageEngine.init(context);
        mSyncStorageEngine = SyncStorageEngine.getSingleton();
        mSyncQueue = new SyncQueue(mSyncStorageEngine);
        mContext = context;
        HandlerThread syncThread = new HandlerThread("SyncHandlerThread",
                Process.THREAD_PRIORITY_BACKGROUND);
        syncThread.start();
        mSyncHandler = new SyncHandler(syncThread.getLooper());
        mMainHandler = new Handler(mContext.getMainLooper());
        mSyncAdapters = new SyncAdaptersCache(mContext);
        mSyncAdapters.setListener(new RegisteredServicesCacheListener<SyncAdapterType>() {
            public void onServiceChanged(SyncAdapterType type, boolean removed) {
                if (!removed) {
                    scheduleSync(null, type.authority, null, 0 ,
                            false );
                }
            }
        }, mSyncHandler);
        mSyncAlarmIntent = PendingIntent.getBroadcast(
                mContext, 0 , new Intent(ACTION_SYNC_ALARM), 0);
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mConnectivityIntentReceiver, intentFilter);
        if (!factoryTest) {
            intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
            context.registerReceiver(mBootCompletedReceiver, intentFilter);
        }
        intentFilter = new IntentFilter(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
        context.registerReceiver(mBackgroundDataSettingChanged, intentFilter);
        intentFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
        intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        context.registerReceiver(mStorageIntentReceiver, intentFilter);
        intentFilter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        intentFilter.setPriority(100);
        context.registerReceiver(mShutdownIntentReceiver, intentFilter);
        if (!factoryTest) {
            mNotificationMgr = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
            context.registerReceiver(new SyncAlarmIntentReceiver(),
                    new IntentFilter(ACTION_SYNC_ALARM));
        } else {
            mNotificationMgr = null;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mSyncWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SYNC_WAKE_LOCK);
        mSyncWakeLock.setReferenceCounted(false);
        mHandleAlarmWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                HANDLE_SYNC_ALARM_WAKE_LOCK);
        mHandleAlarmWakeLock.setReferenceCounted(false);
        mSyncStorageEngine.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, new ISyncStatusObserver.Stub() {
            public void onStatusChanged(int which) {
                sendCheckAlarmsMessage();
            }
        });
        if (!factoryTest) {
            AccountManager.get(mContext).addOnAccountsUpdatedListener(SyncManager.this,
                mSyncHandler, false );
            onAccountsUpdated(AccountManager.get(mContext).getAccounts());
        }
    }
    private long jitterize(long minValue, long maxValue) {
        Random random = new Random(SystemClock.elapsedRealtime());
        long spread = maxValue - minValue;
        if (spread > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("the difference between the maxValue and the "
                    + "minValue must be less than " + Integer.MAX_VALUE);
        }
        return minValue + random.nextInt((int)spread);
    }
    public SyncStorageEngine getSyncStorageEngine() {
        return mSyncStorageEngine;
    }
    private void ensureAlarmService() {
        if (mAlarmService == null) {
            mAlarmService = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        }
    }
    private void initializeSyncAdapter(Account account, String authority) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "initializeSyncAdapter: " + account + ", authority " + authority);
        }
        SyncAdapterType syncAdapterType = SyncAdapterType.newKey(authority, account.type);
        RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo =
                mSyncAdapters.getServiceInfo(syncAdapterType);
        if (syncAdapterInfo == null) {
            Log.w(TAG, "can't find a sync adapter for " + syncAdapterType + ", removing");
            mSyncStorageEngine.removeAuthority(account, authority);
            return;
        }
        Intent intent = new Intent();
        intent.setAction("android.content.SyncAdapter");
        intent.setComponent(syncAdapterInfo.componentName);
        if (!mContext.bindService(intent, new InitializerServiceConnection(account, authority, mContext,
                mMainHandler),
                Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND)) {
            Log.w(TAG, "initializeSyncAdapter: failed to bind to " + intent);
        }
    }
    private static class InitializerServiceConnection implements ServiceConnection {
        private final Account mAccount;
        private final String mAuthority;
        private final Handler mHandler;
        private volatile Context mContext;
        private volatile boolean mInitialized;
        public InitializerServiceConnection(Account account, String authority, Context context,
                Handler handler) {
            mAccount = account;
            mAuthority = authority;
            mContext = context;
            mHandler = handler;
            mInitialized = false;
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                if (!mInitialized) {
                    mInitialized = true;
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log.v(TAG, "calling initialize: " + mAccount + ", authority " + mAuthority);
                    }
                    ISyncAdapter.Stub.asInterface(service).initialize(mAccount, mAuthority);
                }
            } catch (RemoteException e) {
                Log.d(TAG, "error while initializing: " + mAccount + ", authority " + mAuthority,
                        e);
            } finally {
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (mContext != null) {
                            mContext.unbindService(InitializerServiceConnection.this);
                            mContext = null;
                        }
                    }
                }, INITIALIZATION_UNBIND_DELAY_MS);
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            if (mContext != null) {
                mContext.unbindService(InitializerServiceConnection.this);
                mContext = null;
            }
        }
    }
    public void scheduleSync(Account requestedAccount, String requestedAuthority,
            Bundle extras, long delay, boolean onlyThoseWithUnkownSyncableState) {
        boolean isLoggable = Log.isLoggable(TAG, Log.VERBOSE);
        final boolean backgroundDataUsageAllowed = !mBootCompleted ||
                getConnectivityManager().getBackgroundDataSetting();
        if (extras == null) extras = new Bundle();
        Boolean expedited = extras.getBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
        if (expedited) {
            delay = -1; 
        }
        Account[] accounts;
        if (requestedAccount != null) {
            accounts = new Account[]{requestedAccount};
        } else {
            accounts = mAccounts;
            if (accounts.length == 0) {
                if (isLoggable) {
                    Log.v(TAG, "scheduleSync: no accounts configured, dropping");
                }
                return;
            }
        }
        final boolean uploadOnly = extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false);
        final boolean manualSync = extras.getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
        if (manualSync) {
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, true);
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true);
        }
        final boolean ignoreSettings =
                extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, false);
        int source;
        if (uploadOnly) {
            source = SyncStorageEngine.SOURCE_LOCAL;
        } else if (manualSync) {
            source = SyncStorageEngine.SOURCE_USER;
        } else if (requestedAuthority == null) {
            source = SyncStorageEngine.SOURCE_POLL;
        } else {
            source = SyncStorageEngine.SOURCE_SERVER;
        }
        final HashSet<String> syncableAuthorities = new HashSet<String>();
        for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapter :
                mSyncAdapters.getAllServices()) {
            syncableAuthorities.add(syncAdapter.type.authority);
        }
        if (requestedAuthority != null) {
            final boolean hasSyncAdapter = syncableAuthorities.contains(requestedAuthority);
            syncableAuthorities.clear();
            if (hasSyncAdapter) syncableAuthorities.add(requestedAuthority);
        }
        final boolean masterSyncAutomatically = mSyncStorageEngine.getMasterSyncAutomatically();
        for (String authority : syncableAuthorities) {
            for (Account account : accounts) {
                int isSyncable = mSyncStorageEngine.getIsSyncable(account, authority);
                if (isSyncable == 0) {
                    continue;
                }
                if (onlyThoseWithUnkownSyncableState && isSyncable >= 0) {
                    continue;
                }
                final RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo =
                        mSyncAdapters.getServiceInfo(
                                SyncAdapterType.newKey(authority, account.type));
                if (syncAdapterInfo != null) {
                    if (!syncAdapterInfo.type.supportsUploading() && uploadOnly) {
                        continue;
                    }
                    boolean syncAllowed =
                            (isSyncable < 0)
                            || ignoreSettings
                            || (backgroundDataUsageAllowed && masterSyncAutomatically
                                && mSyncStorageEngine.getSyncAutomatically(account, authority));
                    if (!syncAllowed) {
                        if (isLoggable) {
                            Log.d(TAG, "scheduleSync: sync of " + account + ", " + authority
                                    + " is not allowed, dropping request");
                        }
                        continue;
                    }
                    if (isLoggable) {
                        Log.v(TAG, "scheduleSync:"
                                + " delay " + delay
                                + ", source " + source
                                + ", account " + account
                                + ", authority " + authority
                                + ", extras " + extras);
                    }
                    scheduleSyncOperation(
                            new SyncOperation(account, source, authority, extras, delay));
                }
            }
        }
    }
    public void scheduleLocalSync(Account account, String authority) {
        final Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
        scheduleSync(account, authority, extras, LOCAL_SYNC_DELAY,
                false );
    }
    public SyncAdapterType[] getSyncAdapterTypes() {
        final Collection<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> serviceInfos =
                mSyncAdapters.getAllServices();
        SyncAdapterType[] types = new SyncAdapterType[serviceInfos.size()];
        int i = 0;
        for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo : serviceInfos) {
            types[i] = serviceInfo.type;
            ++i;
        }
        return types;
    }
    private void sendSyncAlarmMessage() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "sending MESSAGE_SYNC_ALARM");
        mSyncHandler.sendEmptyMessage(SyncHandler.MESSAGE_SYNC_ALARM);
    }
    private void sendCheckAlarmsMessage() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "sending MESSAGE_CHECK_ALARMS");
        mSyncHandler.sendEmptyMessage(SyncHandler.MESSAGE_CHECK_ALARMS);
    }
    private void sendSyncFinishedOrCanceledMessage(ActiveSyncContext syncContext,
            SyncResult syncResult) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) Log.v(TAG, "sending MESSAGE_SYNC_FINISHED");
        Message msg = mSyncHandler.obtainMessage();
        msg.what = SyncHandler.MESSAGE_SYNC_FINISHED;
        msg.obj = new SyncHandlerMessagePayload(syncContext, syncResult);
        mSyncHandler.sendMessage(msg);
    }
    class SyncHandlerMessagePayload {
        public final ActiveSyncContext activeSyncContext;
        public final SyncResult syncResult;
        SyncHandlerMessagePayload(ActiveSyncContext syncContext, SyncResult syncResult) {
            this.activeSyncContext = syncContext;
            this.syncResult = syncResult;
        }
    }
    class SyncAlarmIntentReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            mHandleAlarmWakeLock.acquire();
            sendSyncAlarmMessage();
        }
    }
    private void clearBackoffSetting(SyncOperation op) {
        mSyncStorageEngine.setBackoff(op.account, op.authority,
                SyncStorageEngine.NOT_IN_BACKOFF_MODE, SyncStorageEngine.NOT_IN_BACKOFF_MODE);
    }
    private void increaseBackoffSetting(SyncOperation op) {
        final long now = SystemClock.elapsedRealtime();
        final Pair<Long, Long> previousSettings =
                mSyncStorageEngine.getBackoff(op.account, op.authority);
        long newDelayInMs;
        if (previousSettings == null || previousSettings.second <= 0) {
            newDelayInMs = jitterize(INITIAL_SYNC_RETRY_TIME_IN_MS,
                    (long)(INITIAL_SYNC_RETRY_TIME_IN_MS * 1.1));
        } else {
            newDelayInMs = previousSettings.second * 2;
        }
        long maxSyncRetryTimeInSeconds = Settings.Secure.getLong(mContext.getContentResolver(),
                Settings.Secure.SYNC_MAX_RETRY_DELAY_IN_SECONDS,
                DEFAULT_MAX_SYNC_RETRY_TIME_IN_SECONDS);
        if (newDelayInMs > maxSyncRetryTimeInSeconds * 1000) {
            newDelayInMs = maxSyncRetryTimeInSeconds * 1000;
        }
        mSyncStorageEngine.setBackoff(op.account, op.authority,
                now + newDelayInMs, newDelayInMs);
    }
    private void setDelayUntilTime(SyncOperation op, long delayUntilSeconds) {
        final long delayUntil = delayUntilSeconds * 1000;
        final long absoluteNow = System.currentTimeMillis();
        long newDelayUntilTime;
        if (delayUntil > absoluteNow) {
            newDelayUntilTime = SystemClock.elapsedRealtime() + (delayUntil - absoluteNow);
        } else {
            newDelayUntilTime = 0;
        }
        mSyncStorageEngine.setDelayUntilTime(op.account, op.authority, newDelayUntilTime);
    }
    public void cancelActiveSync(Account account, String authority) {
        ActiveSyncContext activeSyncContext = mActiveSyncContext;
        if (activeSyncContext != null) {
            if (account != null) {
                if (!account.equals(activeSyncContext.mSyncOperation.account)) {
                    return;
                }
            }
            if (authority != null) {
                if (!authority.equals(activeSyncContext.mSyncOperation.authority)) {
                    return;
                }
            }
            sendSyncFinishedOrCanceledMessage(activeSyncContext,
                    null );
        }
    }
    public void scheduleSyncOperation(SyncOperation syncOperation) {
        final ActiveSyncContext activeSyncContext = mActiveSyncContext;
        if (syncOperation.expedited && activeSyncContext != null) {
            final boolean hasSameKey =
                    activeSyncContext.mSyncOperation.key.equals(syncOperation.key);
            if (!activeSyncContext.mSyncOperation.expedited && !hasSameKey) {
                scheduleSyncOperation(new SyncOperation(activeSyncContext.mSyncOperation));
                sendSyncFinishedOrCanceledMessage(activeSyncContext,
                        null );
            }
        }
        boolean queueChanged;
        synchronized (mSyncQueue) {
            queueChanged = mSyncQueue.add(syncOperation);
        }
        if (queueChanged) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "scheduleSyncOperation: enqueued " + syncOperation);
            }
            sendCheckAlarmsMessage();
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "scheduleSyncOperation: dropping duplicate sync operation "
                        + syncOperation);
            }
        }
    }
    public void clearScheduledSyncOperations(Account account, String authority) {
        mSyncStorageEngine.setBackoff(account, authority,
                SyncStorageEngine.NOT_IN_BACKOFF_MODE, SyncStorageEngine.NOT_IN_BACKOFF_MODE);
        synchronized (mSyncQueue) {
            mSyncQueue.remove(account, authority);
        }
    }
    void maybeRescheduleSync(SyncResult syncResult, SyncOperation operation) {
        boolean isLoggable = Log.isLoggable(TAG, Log.DEBUG);
        if (isLoggable) {
            Log.d(TAG, "encountered error(s) during the sync: " + syncResult + ", " + operation);
        }
        operation = new SyncOperation(operation);
        if (operation.extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF, false)) {
            operation.extras.remove(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF);
        }
        if (operation.extras.getBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false)) {
            Log.d(TAG, "not retrying sync operation because SYNC_EXTRAS_DO_NOT_RETRY was specified "
                    + operation);
        } else if (operation.extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, false)) {
            operation.extras.remove(ContentResolver.SYNC_EXTRAS_UPLOAD);
            Log.d(TAG, "retrying sync operation as a two-way sync because an upload-only sync "
                    + "encountered an error: " + operation);
            scheduleSyncOperation(operation);
        } else if (syncResult.tooManyRetries) {
            Log.d(TAG, "not retrying sync operation because it retried too many times: "
                    + operation);
        } else if (syncResult.madeSomeProgress()) {
            if (isLoggable) {
                Log.d(TAG, "retrying sync operation because even though it had an error "
                        + "it achieved some success");
            }
            scheduleSyncOperation(operation);
        } else if (syncResult.syncAlreadyInProgress) {
            if (isLoggable) {
                Log.d(TAG, "retrying sync operation that failed because there was already a "
                        + "sync in progress: " + operation);
            }
            scheduleSyncOperation(new SyncOperation(operation.account, operation.syncSource,
                    operation.authority, operation.extras,
                    DELAY_RETRY_SYNC_IN_PROGRESS_IN_SECONDS * 1000));
        } else if (syncResult.hasSoftError()) {
            if (isLoggable) {
                Log.d(TAG, "retrying sync operation because it encountered a soft error: "
                        + operation);
            }
            scheduleSyncOperation(operation);
        } else {
            Log.d(TAG, "not retrying sync operation because the error is a hard error: "
                    + operation);
        }
    }
    class ActiveSyncContext extends ISyncContext.Stub implements ServiceConnection {
        final SyncOperation mSyncOperation;
        final long mHistoryRowId;
        ISyncAdapter mSyncAdapter;
        final long mStartTime;
        long mTimeoutStartTime;
        boolean mBound;
        public ActiveSyncContext(SyncOperation syncOperation,
                long historyRowId) {
            super();
            mSyncOperation = syncOperation;
            mHistoryRowId = historyRowId;
            mSyncAdapter = null;
            mStartTime = SystemClock.elapsedRealtime();
            mTimeoutStartTime = mStartTime;
        }
        public void sendHeartbeat() {
        }
        public void onFinished(SyncResult result) {
            sendSyncFinishedOrCanceledMessage(this, result);
        }
        public void toString(StringBuilder sb) {
            sb.append("startTime ").append(mStartTime)
                    .append(", mTimeoutStartTime ").append(mTimeoutStartTime)
                    .append(", mHistoryRowId ").append(mHistoryRowId)
                    .append(", syncOperation ").append(mSyncOperation);
        }
        public void onServiceConnected(ComponentName name, IBinder service) {
            Message msg = mSyncHandler.obtainMessage();
            msg.what = SyncHandler.MESSAGE_SERVICE_CONNECTED;
            msg.obj = new ServiceConnectionData(this, ISyncAdapter.Stub.asInterface(service));
            mSyncHandler.sendMessage(msg);
        }
        public void onServiceDisconnected(ComponentName name) {
            Message msg = mSyncHandler.obtainMessage();
            msg.what = SyncHandler.MESSAGE_SERVICE_DISCONNECTED;
            msg.obj = new ServiceConnectionData(this, null);
            mSyncHandler.sendMessage(msg);
        }
        boolean bindToSyncAdapter(RegisteredServicesCache.ServiceInfo info) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "bindToSyncAdapter: " + info.componentName + ", connection " + this);
            }
            Intent intent = new Intent();
            intent.setAction("android.content.SyncAdapter");
            intent.setComponent(info.componentName);
            intent.putExtra(Intent.EXTRA_CLIENT_LABEL,
                    com.android.internal.R.string.sync_binding_label);
            intent.putExtra(Intent.EXTRA_CLIENT_INTENT, PendingIntent.getActivity(
                    mContext, 0, new Intent(Settings.ACTION_SYNC_SETTINGS), 0));
            mBound = true;
            final boolean bindResult = mContext.bindService(intent, this,
                    Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND);
            if (!bindResult) {
                mBound = false;
            }
            return bindResult;
        }
        protected void close() {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.d(TAG, "unBindFromSyncAdapter: connection " + this);
            }
            if (mBound) {
                mBound = false;
                mContext.unbindService(this);
            }
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
    }
    protected void dump(FileDescriptor fd, PrintWriter pw) {
        StringBuilder sb = new StringBuilder();
        dumpSyncState(pw, sb);
        dumpSyncHistory(pw, sb);
        pw.println();
        pw.println("SyncAdapters:");
        for (RegisteredServicesCache.ServiceInfo info : mSyncAdapters.getAllServices()) {
            pw.println("  " + info);
        }
    }
    static String formatTime(long time) {
        Time tobj = new Time();
        tobj.set(time);
        return tobj.format("%Y-%m-%d %H:%M:%S");
    }
    protected void dumpSyncState(PrintWriter pw, StringBuilder sb) {
        pw.print("data connected: "); pw.println(mDataConnectionIsConnected);
        pw.print("memory low: "); pw.println(mStorageIsLow);
        final Account[] accounts = mAccounts;
        pw.print("accounts: ");
        if (accounts != INITIAL_ACCOUNTS_ARRAY) {
            pw.println(accounts.length);
        } else {
            pw.println("not known yet");
        }
        final long now = SystemClock.elapsedRealtime();
        pw.print("now: "); pw.print(now);
        pw.println(" (" + formatTime(System.currentTimeMillis()) + ")");
        pw.print("uptime: "); pw.print(DateUtils.formatElapsedTime(now/1000));
                pw.println(" (HH:MM:SS)");
        pw.print("time spent syncing: ");
                pw.print(DateUtils.formatElapsedTime(
                        mSyncHandler.mSyncTimeTracker.timeSpentSyncing() / 1000));
                pw.print(" (HH:MM:SS), sync ");
                pw.print(mSyncHandler.mSyncTimeTracker.mLastWasSyncing ? "" : "not ");
                pw.println("in progress");
        if (mSyncHandler.mAlarmScheduleTime != null) {
            pw.print("next alarm time: "); pw.print(mSyncHandler.mAlarmScheduleTime);
                    pw.print(" (");
                    pw.print(DateUtils.formatElapsedTime((mSyncHandler.mAlarmScheduleTime-now)/1000));
                    pw.println(" (HH:MM:SS) from now)");
        } else {
            pw.println("no alarm is scheduled (there had better not be any pending syncs)");
        }
        final SyncManager.ActiveSyncContext activeSyncContext = mActiveSyncContext;
        pw.print("active sync: "); pw.println(activeSyncContext);
        pw.print("notification info: ");
        sb.setLength(0);
        mSyncHandler.mSyncNotificationInfo.toString(sb);
        pw.println(sb.toString());
        synchronized (mSyncQueue) {
            pw.print("sync queue: ");
            sb.setLength(0);
            mSyncQueue.dump(sb);
            pw.println(sb.toString());
        }
        SyncInfo active = mSyncStorageEngine.getCurrentSync();
        if (active != null) {
            SyncStorageEngine.AuthorityInfo authority
                    = mSyncStorageEngine.getAuthority(active.authorityId);
            final long durationInSeconds = (now - active.startTime) / 1000;
            pw.print("Active sync: ");
                    pw.print(authority != null ? authority.account : "<no account>");
                    pw.print(" ");
                    pw.print(authority != null ? authority.authority : "<no account>");
                    if (activeSyncContext != null) {
                        pw.print(" ");
                        pw.print(SyncStorageEngine.SOURCES[
                                activeSyncContext.mSyncOperation.syncSource]);
                    }
                    pw.print(", duration is ");
                    pw.println(DateUtils.formatElapsedTime(durationInSeconds));
        } else {
            pw.println("No sync is in progress.");
        }
        ArrayList<SyncStorageEngine.PendingOperation> ops
                = mSyncStorageEngine.getPendingOperations();
        if (ops != null && ops.size() > 0) {
            pw.println();
            pw.println("Pending Syncs");
            final int N = ops.size();
            for (int i=0; i<N; i++) {
                SyncStorageEngine.PendingOperation op = ops.get(i);
                pw.print("  #"); pw.print(i); pw.print(": account=");
                pw.print(op.account.name); pw.print(":");
                pw.print(op.account.type); pw.print(" authority=");
                pw.print(op.authority); pw.print(" expedited=");
                pw.println(op.expedited);
                if (op.extras != null && op.extras.size() > 0) {
                    sb.setLength(0);
                    SyncOperation.extrasToStringBuilder(op.extras, sb, false );
                    pw.print("    extras: "); pw.println(sb.toString());
                }
            }
        }
        pw.println();
        pw.println("Sync Status");
        for (Account account : accounts) {
            pw.print("  Account "); pw.print(account.name);
                    pw.print(" "); pw.print(account.type);
                    pw.println(":");
            for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterType :
                    mSyncAdapters.getAllServices()) {
                if (!syncAdapterType.type.accountType.equals(account.type)) {
                    continue;
                }
                SyncStorageEngine.AuthorityInfo settings = mSyncStorageEngine.getOrCreateAuthority(
                        account, syncAdapterType.type.authority);
                SyncStatusInfo status = mSyncStorageEngine.getOrCreateSyncStatus(settings);
                pw.print("    "); pw.print(settings.authority);
                pw.println(":");
                pw.print("      settings:");
                pw.print(" " + (settings.syncable > 0
                        ? "syncable"
                        : (settings.syncable == 0 ? "not syncable" : "not initialized")));
                pw.print(", " + (settings.enabled ? "enabled" : "disabled"));
                if (settings.delayUntil > now) {
                    pw.print(", delay for "
                            + ((settings.delayUntil - now) / 1000) + " sec");
                }
                if (settings.backoffTime > now) {
                    pw.print(", backoff for "
                            + ((settings.backoffTime - now) / 1000) + " sec");
                }
                if (settings.backoffDelay > 0) {
                    pw.print(", the backoff increment is " + settings.backoffDelay / 1000
                                + " sec");
                }
                pw.println();
                for (int periodicIndex = 0;
                        periodicIndex < settings.periodicSyncs.size();
                        periodicIndex++) {
                    Pair<Bundle, Long> info = settings.periodicSyncs.get(periodicIndex);
                    long lastPeriodicTime = status.getPeriodicSyncTime(periodicIndex);
                    long nextPeriodicTime = lastPeriodicTime + info.second * 1000;
                    pw.println("      periodic period=" + info.second
                            + ", extras=" + info.first
                            + ", next=" + formatTime(nextPeriodicTime));
                }
                pw.print("      count: local="); pw.print(status.numSourceLocal);
                pw.print(" poll="); pw.print(status.numSourcePoll);
                pw.print(" periodic="); pw.print(status.numSourcePeriodic);
                pw.print(" server="); pw.print(status.numSourceServer);
                pw.print(" user="); pw.print(status.numSourceUser);
                pw.print(" total="); pw.print(status.numSyncs);
                pw.println();
                pw.print("      total duration: ");
                pw.println(DateUtils.formatElapsedTime(status.totalElapsedTime/1000));
                if (status.lastSuccessTime != 0) {
                    pw.print("      SUCCESS: source=");
                    pw.print(SyncStorageEngine.SOURCES[status.lastSuccessSource]);
                    pw.print(" time=");
                    pw.println(formatTime(status.lastSuccessTime));
                }
                if (status.lastFailureTime != 0) {
                    pw.print("      FAILURE: source=");
                    pw.print(SyncStorageEngine.SOURCES[
                            status.lastFailureSource]);
                    pw.print(" initialTime=");
                    pw.print(formatTime(status.initialFailureTime));
                    pw.print(" lastTime=");
                    pw.println(formatTime(status.lastFailureTime));
                    pw.print("      message: "); pw.println(status.lastFailureMesg);
                }
            }
        }
    }
    private void dumpTimeSec(PrintWriter pw, long time) {
        pw.print(time/1000); pw.print('.'); pw.print((time/100)%10);
        pw.print('s');
    }
    private void dumpDayStatistic(PrintWriter pw, SyncStorageEngine.DayStats ds) {
        pw.print("Success ("); pw.print(ds.successCount);
        if (ds.successCount > 0) {
            pw.print(" for "); dumpTimeSec(pw, ds.successTime);
            pw.print(" avg="); dumpTimeSec(pw, ds.successTime/ds.successCount);
        }
        pw.print(") Failure ("); pw.print(ds.failureCount);
        if (ds.failureCount > 0) {
            pw.print(" for "); dumpTimeSec(pw, ds.failureTime);
            pw.print(" avg="); dumpTimeSec(pw, ds.failureTime/ds.failureCount);
        }
        pw.println(")");
    }
    protected void dumpSyncHistory(PrintWriter pw, StringBuilder sb) {
        SyncStorageEngine.DayStats dses[] = mSyncStorageEngine.getDayStatistics();
        if (dses != null && dses[0] != null) {
            pw.println();
            pw.println("Sync Statistics");
            pw.print("  Today:  "); dumpDayStatistic(pw, dses[0]);
            int today = dses[0].day;
            int i;
            SyncStorageEngine.DayStats ds;
            for (i=1; i<=6 && i < dses.length; i++) {
                ds = dses[i];
                if (ds == null) break;
                int delta = today-ds.day;
                if (delta > 6) break;
                pw.print("  Day-"); pw.print(delta); pw.print(":  ");
                dumpDayStatistic(pw, ds);
            }
            int weekDay = today;
            while (i < dses.length) {
                SyncStorageEngine.DayStats aggr = null;
                weekDay -= 7;
                while (i < dses.length) {
                    ds = dses[i];
                    if (ds == null) {
                        i = dses.length;
                        break;
                    }
                    int delta = weekDay-ds.day;
                    if (delta > 6) break;
                    i++;
                    if (aggr == null) {
                        aggr = new SyncStorageEngine.DayStats(weekDay);
                    }
                    aggr.successCount += ds.successCount;
                    aggr.successTime += ds.successTime;
                    aggr.failureCount += ds.failureCount;
                    aggr.failureTime += ds.failureTime;
                }
                if (aggr != null) {
                    pw.print("  Week-"); pw.print((today-weekDay)/7); pw.print(": ");
                    dumpDayStatistic(pw, aggr);
                }
            }
        }
        ArrayList<SyncStorageEngine.SyncHistoryItem> items
                = mSyncStorageEngine.getSyncHistory();
        if (items != null && items.size() > 0) {
            pw.println();
            pw.println("Recent Sync History");
            final int N = items.size();
            for (int i=0; i<N; i++) {
                SyncStorageEngine.SyncHistoryItem item = items.get(i);
                SyncStorageEngine.AuthorityInfo authority
                        = mSyncStorageEngine.getAuthority(item.authorityId);
                pw.print("  #"); pw.print(i+1); pw.print(": ");
                        if (authority != null) {
                            pw.print(authority.account.name);
                            pw.print(":");
                            pw.print(authority.account.type);
                            pw.print(" ");
                            pw.print(authority.authority);
                        } else {
                            pw.print("<no account>");
                        }
                Time time = new Time();
                time.set(item.eventTime);
                pw.print(" "); pw.print(SyncStorageEngine.SOURCES[item.source]);
                        pw.print(" @ ");
                        pw.print(formatTime(item.eventTime));
                        pw.print(" for ");
                        dumpTimeSec(pw, item.elapsedTime);
                        pw.println();
                if (item.event != SyncStorageEngine.EVENT_STOP
                        || item.upstreamActivity !=0
                        || item.downstreamActivity != 0) {
                    pw.print("    event="); pw.print(item.event);
                            pw.print(" upstreamActivity="); pw.print(item.upstreamActivity);
                            pw.print(" downstreamActivity="); pw.println(item.downstreamActivity);
                }
                if (item.mesg != null
                        && !SyncStorageEngine.MESG_SUCCESS.equals(item.mesg)) {
                    pw.print("    mesg="); pw.println(item.mesg);
                }
            }
        }
    }
    private class SyncTimeTracker {
        boolean mLastWasSyncing = false;
        long mWhenSyncStarted = 0;
        private long mTimeSpentSyncing;
        public synchronized void update() {
            final boolean isSyncInProgress = mActiveSyncContext != null;
            if (isSyncInProgress == mLastWasSyncing) return;
            final long now = SystemClock.elapsedRealtime();
            if (isSyncInProgress) {
                mWhenSyncStarted = now;
            } else {
                mTimeSpentSyncing += now - mWhenSyncStarted;
            }
            mLastWasSyncing = isSyncInProgress;
        }
        public synchronized long timeSpentSyncing() {
            if (!mLastWasSyncing) return mTimeSpentSyncing;
            final long now = SystemClock.elapsedRealtime();
            return mTimeSpentSyncing + (now - mWhenSyncStarted);
        }
    }
    class ServiceConnectionData {
        public final ActiveSyncContext activeSyncContext;
        public final ISyncAdapter syncAdapter;
        ServiceConnectionData(ActiveSyncContext activeSyncContext, ISyncAdapter syncAdapter) {
            this.activeSyncContext = activeSyncContext;
            this.syncAdapter = syncAdapter;
        }
    }
    class SyncHandler extends Handler {
        private static final int MESSAGE_SYNC_FINISHED = 1;
        private static final int MESSAGE_SYNC_ALARM = 2;
        private static final int MESSAGE_CHECK_ALARMS = 3;
        private static final int MESSAGE_SERVICE_CONNECTED = 4;
        private static final int MESSAGE_SERVICE_DISCONNECTED = 5;
        public final SyncNotificationInfo mSyncNotificationInfo = new SyncNotificationInfo();
        private Long mAlarmScheduleTime = null;
        public final SyncTimeTracker mSyncTimeTracker = new SyncTimeTracker();
        private boolean mErrorNotificationInstalled = false;
        private volatile CountDownLatch mReadyToRunLatch = new CountDownLatch(1);
        public void onBootCompleted() {
            mBootCompleted = true;
            mSyncStorageEngine.doDatabaseCleanup(AccountManager.get(mContext).getAccounts());
            if (mReadyToRunLatch != null) {
                mReadyToRunLatch.countDown();
            }
        }
        private void waitUntilReadyToRun() {
            CountDownLatch latch = mReadyToRunLatch;
            if (latch != null) {
                while (true) {
                    try {
                        latch.await();
                        mReadyToRunLatch = null;
                        return;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        class SyncNotificationInfo {
            public Account account;
            public String authority;
            public boolean isActive = false;
            public Long startTime = null;
            public void toString(StringBuilder sb) {
                sb.append("account ").append(account)
                        .append(", authority ").append(authority)
                        .append(", isActive ").append(isActive)
                        .append(", startTime ").append(startTime);
            }
            @Override
            public String toString() {
                StringBuilder sb = new StringBuilder();
                toString(sb);
                return sb.toString();
            }
        }
        public SyncHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            Long earliestFuturePollTime = null;
            try {
                waitUntilReadyToRun();
                earliestFuturePollTime = scheduleReadyPeriodicSyncs();
                switch (msg.what) {
                    case SyncHandler.MESSAGE_SYNC_FINISHED:
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "handleSyncHandlerMessage: MESSAGE_SYNC_FINISHED");
                        }
                        SyncHandlerMessagePayload payload = (SyncHandlerMessagePayload)msg.obj;
                        if (mActiveSyncContext != payload.activeSyncContext) {
                            Log.d(TAG, "handleSyncHandlerMessage: sync context doesn't match, "
                                    + "dropping: mActiveSyncContext " + mActiveSyncContext
                                    + " != " + payload.activeSyncContext);
                            return;
                        }
                        runSyncFinishedOrCanceled(payload.syncResult);
                        runStateIdle();
                        break;
                    case SyncHandler.MESSAGE_SERVICE_CONNECTED: {
                        ServiceConnectionData msgData = (ServiceConnectionData)msg.obj;
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.d(TAG, "handleSyncHandlerMessage: MESSAGE_SERVICE_CONNECTED: "
                                    + msgData.activeSyncContext
                                    + " active is " + mActiveSyncContext);
                        }
                        if (mActiveSyncContext == msgData.activeSyncContext) {
                            runBoundToSyncAdapter(msgData.syncAdapter);
                        }
                        break;
                    }
                    case SyncHandler.MESSAGE_SERVICE_DISCONNECTED: {
                        ServiceConnectionData msgData = (ServiceConnectionData)msg.obj;
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.d(TAG, "handleSyncHandlerMessage: MESSAGE_SERVICE_DISCONNECTED: "
                                    + msgData.activeSyncContext
                                    + " active is " + mActiveSyncContext);
                        }
                        if (mActiveSyncContext == msgData.activeSyncContext) {
                            if (mActiveSyncContext.mSyncAdapter != null) {
                                try {
                                    mActiveSyncContext.mSyncAdapter.cancelSync(mActiveSyncContext);
                                } catch (RemoteException e) {
                                }
                            }
                            SyncResult syncResult = new SyncResult();
                            syncResult.stats.numIoExceptions++;
                            runSyncFinishedOrCanceled(syncResult);
                            runStateIdle();
                        }
                        break;
                    }
                    case SyncHandler.MESSAGE_SYNC_ALARM: {
                        boolean isLoggable = Log.isLoggable(TAG, Log.VERBOSE);
                        if (isLoggable) {
                            Log.v(TAG, "handleSyncHandlerMessage: MESSAGE_SYNC_ALARM");
                        }
                        mAlarmScheduleTime = null;
                        try {
                            if (mActiveSyncContext != null) {
                                if (isLoggable) {
                                    Log.v(TAG, "handleSyncHandlerMessage: sync context is active");
                                }
                                runStateSyncing();
                            }
                            if (mActiveSyncContext == null) {
                                if (isLoggable) {
                                    Log.v(TAG, "handleSyncHandlerMessage: "
                                            + "sync context is not active");
                                }
                                runStateIdle();
                            }
                        } finally {
                            mHandleAlarmWakeLock.release();
                        }
                        break;
                    }
                    case SyncHandler.MESSAGE_CHECK_ALARMS:
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "handleSyncHandlerMessage: MESSAGE_CHECK_ALARMS");
                        }
                        break;
                }
            } finally {
                final boolean isSyncInProgress = mActiveSyncContext != null;
                if (!isSyncInProgress) {
                    mSyncWakeLock.release();
                }
                manageSyncNotification();
                manageErrorNotification();
                manageSyncAlarm(earliestFuturePollTime);
                mSyncTimeTracker.update();
            }
        }
        private Long scheduleReadyPeriodicSyncs() {
            final boolean backgroundDataUsageAllowed =
                    getConnectivityManager().getBackgroundDataSetting();
            Long earliestFuturePollTime = null;
            if (!backgroundDataUsageAllowed || !mSyncStorageEngine.getMasterSyncAutomatically()) {
                return earliestFuturePollTime;
            }
            final long nowAbsolute = System.currentTimeMillis();
            ArrayList<SyncStorageEngine.AuthorityInfo> infos = mSyncStorageEngine.getAuthorities();
            for (SyncStorageEngine.AuthorityInfo info : infos) {
                if (!ArrayUtils.contains(mAccounts, info.account)) {
                    continue;
                }
                if (!mSyncStorageEngine.getSyncAutomatically(info.account, info.authority)) {
                    continue;
                }
                if (mSyncStorageEngine.getIsSyncable(info.account, info.authority) == 0) {
                    continue;
                }
                SyncStatusInfo status = mSyncStorageEngine.getOrCreateSyncStatus(info);
                for (int i = 0, N = info.periodicSyncs.size(); i < N; i++) {
                    final Bundle extras = info.periodicSyncs.get(i).first;
                    final Long periodInSeconds = info.periodicSyncs.get(i).second;
                    final long lastPollTimeAbsolute = status.getPeriodicSyncTime(i);
                    long nextPollTimeAbsolute = lastPollTimeAbsolute + periodInSeconds * 1000;
                    if (nextPollTimeAbsolute <= nowAbsolute) {
                        scheduleSyncOperation(
                                new SyncOperation(info.account, SyncStorageEngine.SOURCE_PERIODIC,
                                        info.authority, extras, 0 ));
                        status.setPeriodicSyncTime(i, nowAbsolute);
                    } else {
                        if (earliestFuturePollTime == null
                                || nextPollTimeAbsolute < earliestFuturePollTime) {
                            earliestFuturePollTime = nextPollTimeAbsolute;
                        }
                    }
                }
            }
            if (earliestFuturePollTime == null) {
                return null;
            }
            return SystemClock.elapsedRealtime()
                    + ((earliestFuturePollTime < nowAbsolute)
                      ? 0
                      : (earliestFuturePollTime - nowAbsolute));
        }
        private void runStateSyncing() {
            ActiveSyncContext activeSyncContext = mActiveSyncContext;
            final long now = SystemClock.elapsedRealtime();
            if (now > activeSyncContext.mTimeoutStartTime + MAX_TIME_PER_SYNC) {
                Pair<SyncOperation, Long> nextOpAndRunTime;
                synchronized (mSyncQueue) {
                    nextOpAndRunTime = mSyncQueue.nextOperation();
                }
                if (nextOpAndRunTime != null && nextOpAndRunTime.second <= now) {
                    Log.d(TAG, "canceling and rescheduling sync because it ran too long: "
                            + activeSyncContext.mSyncOperation);
                    scheduleSyncOperation(new SyncOperation(activeSyncContext.mSyncOperation));
                    sendSyncFinishedOrCanceledMessage(activeSyncContext,
                            null );
                } else {
                    activeSyncContext.mTimeoutStartTime = now + MAX_TIME_PER_SYNC;
                }
            }
        }
        private void runStateIdle() {
            boolean isLoggable = Log.isLoggable(TAG, Log.VERBOSE);
            if (isLoggable) Log.v(TAG, "runStateIdle");
            if (!mDataConnectionIsConnected) {
                if (isLoggable) {
                    Log.v(TAG, "runStateIdle: no data connection, skipping");
                }
                return;
            }
            if (mStorageIsLow) {
                if (isLoggable) {
                    Log.v(TAG, "runStateIdle: memory low, skipping");
                }
                return;
            }
            Account[] accounts = mAccounts;
            if (accounts == INITIAL_ACCOUNTS_ARRAY) {
                if (isLoggable) {
                    Log.v(TAG, "runStateIdle: accounts not known, skipping");
                }
                return;
            }
            SyncOperation op;
            int syncableState;
            final boolean backgroundDataUsageAllowed =
                    getConnectivityManager().getBackgroundDataSetting();
            final boolean masterSyncAutomatically = mSyncStorageEngine.getMasterSyncAutomatically();
            synchronized (mSyncQueue) {
                final long now = SystemClock.elapsedRealtime();
                while (true) {
                    Pair<SyncOperation, Long> nextOpAndRunTime = mSyncQueue.nextOperation();
                    if (nextOpAndRunTime == null || nextOpAndRunTime.second > now) {
                        if (isLoggable) {
                            Log.v(TAG, "runStateIdle: no more ready sync operations, returning");
                        }
                        return;
                    }
                    op = nextOpAndRunTime.first;
                    mSyncQueue.remove(op);
                    if (!ArrayUtils.contains(mAccounts, op.account)) {
                        continue;
                    }
                    syncableState = mSyncStorageEngine.getIsSyncable(op.account, op.authority);
                    if (syncableState == 0) {
                        continue;
                    }
                    if (!op.extras.getBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, false)
                            && (syncableState > 0)
                            && (!masterSyncAutomatically
                                || !backgroundDataUsageAllowed
                                || !mSyncStorageEngine.getSyncAutomatically(
                                       op.account, op.authority))) {
                        continue;
                    }
                    break;
                }
                if (isLoggable) {
                    Log.v(TAG, "runStateIdle: we are going to sync " + op);
                }
            }
            final boolean initializeIsSet =
                    op.extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);
            if (syncableState < 0 && !initializeIsSet) {
                op.extras.putBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, true);
                op = new SyncOperation(op);
            } else if (syncableState > 0 && initializeIsSet) {
                op.extras.putBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false);
                op = new SyncOperation(op);
            }
            SyncAdapterType syncAdapterType = SyncAdapterType.newKey(op.authority, op.account.type);
            RegisteredServicesCache.ServiceInfo<SyncAdapterType> syncAdapterInfo =
                    mSyncAdapters.getServiceInfo(syncAdapterType);
            if (syncAdapterInfo == null) {
                Log.d(TAG, "can't find a sync adapter for " + syncAdapterType
                        + ", removing settings for it");
                mSyncStorageEngine.removeAuthority(op.account, op.authority);
                runStateIdle();
                return;
            }
            ActiveSyncContext activeSyncContext =
                    new ActiveSyncContext(op, insertStartSyncEvent(op));
            mActiveSyncContext = activeSyncContext;
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "runStateIdle: setting mActiveSyncContext to " + mActiveSyncContext);
            }
            mSyncStorageEngine.setActiveSync(mActiveSyncContext);
            if (!activeSyncContext.bindToSyncAdapter(syncAdapterInfo)) {
                Log.e(TAG, "Bind attempt failed to " + syncAdapterInfo);
                mActiveSyncContext.close();
                mActiveSyncContext = null;
                mSyncStorageEngine.setActiveSync(mActiveSyncContext);
                runStateIdle();
                return;
            }
            mSyncWakeLock.acquire();
        }
        private void runBoundToSyncAdapter(ISyncAdapter syncAdapter) {
            mActiveSyncContext.mSyncAdapter = syncAdapter;
            final SyncOperation syncOperation = mActiveSyncContext.mSyncOperation;
            try {
                syncAdapter.startSync(mActiveSyncContext, syncOperation.authority,
                        syncOperation.account, syncOperation.extras);
            } catch (RemoteException remoteExc) {
                Log.d(TAG, "runStateIdle: caught a RemoteException, rescheduling", remoteExc);
                mActiveSyncContext.close();
                mActiveSyncContext = null;
                mSyncStorageEngine.setActiveSync(mActiveSyncContext);
                increaseBackoffSetting(syncOperation);
                scheduleSyncOperation(new SyncOperation(syncOperation));
            } catch (RuntimeException exc) {
                mActiveSyncContext.close();
                mActiveSyncContext = null;
                mSyncStorageEngine.setActiveSync(mActiveSyncContext);
                Log.e(TAG, "Caught RuntimeException while starting the sync " + syncOperation, exc);
            }
        }
        private void runSyncFinishedOrCanceled(SyncResult syncResult) {
            boolean isLoggable = Log.isLoggable(TAG, Log.VERBOSE);
            final ActiveSyncContext activeSyncContext = mActiveSyncContext;
            mActiveSyncContext = null;
            mSyncStorageEngine.setActiveSync(mActiveSyncContext);
            final SyncOperation syncOperation = activeSyncContext.mSyncOperation;
            final long elapsedTime = SystemClock.elapsedRealtime() - activeSyncContext.mStartTime;
            String historyMessage;
            int downstreamActivity;
            int upstreamActivity;
            if (syncResult != null) {
                if (isLoggable) {
                    Log.v(TAG, "runSyncFinishedOrCanceled [finished]: "
                            + syncOperation + ", result " + syncResult);
                }
                if (!syncResult.hasError()) {
                    historyMessage = SyncStorageEngine.MESG_SUCCESS;
                    downstreamActivity = 0;
                    upstreamActivity = 0;
                    clearBackoffSetting(syncOperation);
                    if (syncOperation.extras.getBoolean(
                                ContentResolver.SYNC_EXTRAS_INITIALIZE, false)
                            && mSyncStorageEngine.getIsSyncable(
                                syncOperation.account, syncOperation.authority) > 0) {
                        scheduleSyncOperation(new SyncOperation(syncOperation));
                    }
                } else {
                    Log.d(TAG, "failed sync operation " + syncOperation + ", " + syncResult);
                    if (!syncResult.syncAlreadyInProgress) {
                        increaseBackoffSetting(syncOperation);
                    }
                    maybeRescheduleSync(syncResult, syncOperation);
                    historyMessage = Integer.toString(syncResultToErrorNumber(syncResult));
                    downstreamActivity = 0;
                    upstreamActivity = 0;
                }
                setDelayUntilTime(syncOperation, syncResult.delayUntil);
            } else {
                if (isLoggable) {
                    Log.v(TAG, "runSyncFinishedOrCanceled [canceled]: " + syncOperation);
                }
                if (activeSyncContext.mSyncAdapter != null) {
                    try {
                        activeSyncContext.mSyncAdapter.cancelSync(activeSyncContext);
                    } catch (RemoteException e) {
                    }
                }
                historyMessage = SyncStorageEngine.MESG_CANCELED;
                downstreamActivity = 0;
                upstreamActivity = 0;
            }
            stopSyncEvent(activeSyncContext.mHistoryRowId, syncOperation, historyMessage,
                    upstreamActivity, downstreamActivity, elapsedTime);
            activeSyncContext.close();
            if (syncResult != null && syncResult.tooManyDeletions) {
                installHandleTooManyDeletesNotification(syncOperation.account,
                        syncOperation.authority, syncResult.stats.numDeletes);
            } else {
                mNotificationMgr.cancel(
                        syncOperation.account.hashCode() ^ syncOperation.authority.hashCode());
            }
            if (syncResult != null && syncResult.fullSyncRequested) {
                scheduleSyncOperation(new SyncOperation(syncOperation.account,
                        syncOperation.syncSource, syncOperation.authority, new Bundle(), 0));
            }
        }
        private int syncResultToErrorNumber(SyncResult syncResult) {
            if (syncResult.syncAlreadyInProgress)
                return ContentResolver.SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS;
            if (syncResult.stats.numAuthExceptions > 0)
                return ContentResolver.SYNC_ERROR_AUTHENTICATION;
            if (syncResult.stats.numIoExceptions > 0)
                return ContentResolver.SYNC_ERROR_IO;
            if (syncResult.stats.numParseExceptions > 0)
                return ContentResolver.SYNC_ERROR_PARSE;
            if (syncResult.stats.numConflictDetectedExceptions > 0)
                return ContentResolver.SYNC_ERROR_CONFLICT;
            if (syncResult.tooManyDeletions)
                return ContentResolver.SYNC_ERROR_TOO_MANY_DELETIONS;
            if (syncResult.tooManyRetries)
                return ContentResolver.SYNC_ERROR_TOO_MANY_RETRIES;
            if (syncResult.databaseError)
                return ContentResolver.SYNC_ERROR_INTERNAL;
            throw new IllegalStateException("we are not in an error state, " + syncResult);
        }
        private void manageSyncNotification() {
            boolean shouldCancel;
            boolean shouldInstall;
            if (mActiveSyncContext == null) {
                mSyncNotificationInfo.startTime = null;
                shouldCancel = mSyncNotificationInfo.isActive;
                shouldInstall = false;
            } else {
                final SyncOperation syncOperation = mActiveSyncContext.mSyncOperation;
                final long now = SystemClock.elapsedRealtime();
                if (mSyncNotificationInfo.startTime == null) {
                    mSyncNotificationInfo.startTime = now;
                }
                shouldCancel = mSyncNotificationInfo.isActive &&
                        (!syncOperation.authority.equals(mSyncNotificationInfo.authority)
                        || !syncOperation.account.equals(mSyncNotificationInfo.account));
                if (mSyncNotificationInfo.isActive) {
                    shouldInstall = shouldCancel;
                } else {
                    final boolean timeToShowNotification =
                            now > mSyncNotificationInfo.startTime + SYNC_NOTIFICATION_DELAY;
                    final boolean manualSync = syncOperation.extras
                            .getBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);
                    shouldInstall = timeToShowNotification || manualSync;
                }
            }
            if (shouldCancel && !shouldInstall) {
                mNeedSyncActiveNotification = false;
                sendSyncStateIntent();
                mSyncNotificationInfo.isActive = false;
            }
            if (shouldInstall) {
                SyncOperation syncOperation = mActiveSyncContext.mSyncOperation;
                mNeedSyncActiveNotification = true;
                sendSyncStateIntent();
                mSyncNotificationInfo.isActive = true;
                mSyncNotificationInfo.account = syncOperation.account;
                mSyncNotificationInfo.authority = syncOperation.authority;
            }
        }
        private void manageErrorNotification() {
            long when = mSyncStorageEngine.getInitialSyncFailureTime();
            if ((when > 0) && (when + ERROR_NOTIFICATION_DELAY_MS < System.currentTimeMillis())) {
                if (!mErrorNotificationInstalled) {
                    mNeedSyncErrorNotification = true;
                    sendSyncStateIntent();
                }
                mErrorNotificationInstalled = true;
            } else {
                if (mErrorNotificationInstalled) {
                    mNeedSyncErrorNotification = false;
                    sendSyncStateIntent();
                }
                mErrorNotificationInstalled = false;
            }
        }
        private void manageSyncAlarm(Long earliestFuturePollElapsedTime) {
            if (!mDataConnectionIsConnected) return;
            if (mStorageIsLow) return;
            final long now = SystemClock.elapsedRealtime();
            Long alarmTime;
            ActiveSyncContext activeSyncContext = mActiveSyncContext;
            if (activeSyncContext == null) {
                synchronized (mSyncQueue) {
                    final Pair<SyncOperation, Long> candidate = mSyncQueue.nextOperation();
                    if (earliestFuturePollElapsedTime == null && candidate == null) {
                        alarmTime = null;
                    } else if (earliestFuturePollElapsedTime == null) {
                        alarmTime = candidate.second;
                    } else if (candidate == null) {
                        alarmTime = earliestFuturePollElapsedTime;
                    } else {
                        alarmTime = Math.min(earliestFuturePollElapsedTime, candidate.second);
                    }
                }
            } else {
                final long notificationTime =
                        mSyncHandler.mSyncNotificationInfo.startTime + SYNC_NOTIFICATION_DELAY;
                final long timeoutTime =
                        mActiveSyncContext.mTimeoutStartTime + MAX_TIME_PER_SYNC;
                if (mSyncHandler.mSyncNotificationInfo.isActive) {
                    alarmTime = timeoutTime;
                } else {
                    alarmTime = Math.min(notificationTime, timeoutTime);
                }
            }
            if (!mErrorNotificationInstalled) {
                long when = mSyncStorageEngine.getInitialSyncFailureTime();
                if (when > 0) {
                    when += ERROR_NOTIFICATION_DELAY_MS;
                    long delay = when - System.currentTimeMillis();
                    when = now + delay;
                    alarmTime = alarmTime != null ? Math.min(alarmTime, when) : when;
                }
            }
            boolean shouldSet = false;
            boolean shouldCancel = false;
            final boolean alarmIsActive = mAlarmScheduleTime != null;
            final boolean needAlarm = alarmTime != null;
            if (needAlarm) {
                if (!alarmIsActive || alarmTime < mAlarmScheduleTime) {
                    shouldSet = true;
                }
            } else {
                shouldCancel = alarmIsActive;
            }
            ensureAlarmService();
            if (shouldSet) {
                mAlarmScheduleTime = alarmTime;
                mAlarmService.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTime,
                        mSyncAlarmIntent);
            } else if (shouldCancel) {
                mAlarmScheduleTime = null;
                mAlarmService.cancel(mSyncAlarmIntent);
            }
        }
        private void sendSyncStateIntent() {
            Intent syncStateIntent = new Intent(Intent.ACTION_SYNC_STATE_CHANGED);
            syncStateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
            syncStateIntent.putExtra("active", mNeedSyncActiveNotification);
            syncStateIntent.putExtra("failing", mNeedSyncErrorNotification);
            mContext.sendBroadcast(syncStateIntent);
        }
        private void installHandleTooManyDeletesNotification(Account account, String authority,
                long numDeletes) {
            if (mNotificationMgr == null) return;
            final ProviderInfo providerInfo = mContext.getPackageManager().resolveContentProvider(
                    authority, 0 );
            if (providerInfo == null) {
                return;
            }
            CharSequence authorityName = providerInfo.loadLabel(mContext.getPackageManager());
            Intent clickIntent = new Intent();
            clickIntent.setClassName("com.android.providers.subscribedfeeds",
                    "com.android.settings.SyncActivityTooManyDeletes");
            clickIntent.putExtra("account", account);
            clickIntent.putExtra("authority", authority);
            clickIntent.putExtra("provider", authorityName.toString());
            clickIntent.putExtra("numDeletes", numDeletes);
            if (!isActivityAvailable(clickIntent)) {
                Log.w(TAG, "No activity found to handle too many deletes.");
                return;
            }
            final PendingIntent pendingIntent = PendingIntent
                    .getActivity(mContext, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            CharSequence tooManyDeletesDescFormat = mContext.getResources().getText(
                    R.string.contentServiceTooManyDeletesNotificationDesc);
            Notification notification =
                new Notification(R.drawable.stat_notify_sync_error,
                        mContext.getString(R.string.contentServiceSync),
                        System.currentTimeMillis());
            notification.setLatestEventInfo(mContext,
                    mContext.getString(R.string.contentServiceSyncNotificationTitle),
                    String.format(tooManyDeletesDescFormat.toString(), authorityName),
                    pendingIntent);
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            mNotificationMgr.notify(account.hashCode() ^ authority.hashCode(), notification);
        }
        private boolean isActivityAvailable(Intent intent) {
            PackageManager pm = mContext.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                        != 0) {
                    return true;
                }
            }
            return false;
        }
        public long insertStartSyncEvent(SyncOperation syncOperation) {
            final int source = syncOperation.syncSource;
            final long now = System.currentTimeMillis();
            EventLog.writeEvent(2720, syncOperation.authority,
                                SyncStorageEngine.EVENT_START, source,
                                syncOperation.account.name.hashCode());
            return mSyncStorageEngine.insertStartSyncEvent(
                    syncOperation.account, syncOperation.authority, now, source);
        }
        public void stopSyncEvent(long rowId, SyncOperation syncOperation, String resultMessage,
                int upstreamActivity, int downstreamActivity, long elapsedTime) {
            EventLog.writeEvent(2720, syncOperation.authority,
                                SyncStorageEngine.EVENT_STOP, syncOperation.syncSource,
                                syncOperation.account.name.hashCode());
            mSyncStorageEngine.stopSyncEvent(rowId, elapsedTime,
                    resultMessage, downstreamActivity, upstreamActivity);
        }
    }
}
