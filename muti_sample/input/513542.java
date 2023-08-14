class BackupManagerService extends IBackupManager.Stub {
    private static final String TAG = "BackupManagerService";
    private static final boolean DEBUG = false;
    private static final long BACKUP_INTERVAL = AlarmManager.INTERVAL_HOUR;
    private static final int FUZZ_MILLIS = 5 * 60 * 1000;
    private static final long FIRST_BACKUP_INTERVAL = 12 * AlarmManager.INTERVAL_HOUR;
    private static final String RUN_BACKUP_ACTION = "android.app.backup.intent.RUN";
    private static final String RUN_INITIALIZE_ACTION = "android.app.backup.intent.INIT";
    private static final String RUN_CLEAR_ACTION = "android.app.backup.intent.CLEAR";
    private static final int MSG_RUN_BACKUP = 1;
    private static final int MSG_RUN_FULL_BACKUP = 2;
    private static final int MSG_RUN_RESTORE = 3;
    private static final int MSG_RUN_CLEAR = 4;
    private static final int MSG_RUN_INITIALIZE = 5;
    private static final int MSG_RUN_GET_RESTORE_SETS = 6;
    private static final int MSG_TIMEOUT = 7;
    static final long TIMEOUT_INTERVAL = 10 * 1000;
    static final long TIMEOUT_BACKUP_INTERVAL = 30 * 1000;
    static final long TIMEOUT_RESTORE_INTERVAL = 60 * 1000;
    private Context mContext;
    private PackageManager mPackageManager;
    IPackageManager mPackageManagerBinder;
    private IActivityManager mActivityManager;
    private PowerManager mPowerManager;
    private AlarmManager mAlarmManager;
    IBackupManager mBackupManagerBinder;
    boolean mEnabled;   
    boolean mProvisioned;
    boolean mAutoRestore;
    PowerManager.WakeLock mWakelock;
    HandlerThread mHandlerThread = new HandlerThread("backup", Process.THREAD_PRIORITY_BACKGROUND);
    BackupHandler mBackupHandler;
    PendingIntent mRunBackupIntent, mRunInitIntent;
    BroadcastReceiver mRunBackupReceiver, mRunInitReceiver;
    final SparseArray<HashSet<ApplicationInfo>> mBackupParticipants
        = new SparseArray<HashSet<ApplicationInfo>>();
    class BackupRequest {
        public ApplicationInfo appInfo;
        public boolean fullBackup;
        BackupRequest(ApplicationInfo app, boolean isFull) {
            appInfo = app;
            fullBackup = isFull;
        }
        public String toString() {
            return "BackupRequest{app=" + appInfo + " full=" + fullBackup + "}";
        }
    }
    HashMap<ApplicationInfo,BackupRequest> mPendingBackups
            = new HashMap<ApplicationInfo,BackupRequest>();
    static final String PACKAGE_MANAGER_SENTINEL = "@pm@";
    final Object mQueueLock = new Object();
    final Object mAgentConnectLock = new Object();
    IBackupAgent mConnectedAgent;
    volatile boolean mConnecting;
    volatile long mLastBackupPass;
    volatile long mNextBackupPass;
    final Object mClearDataLock = new Object();
    volatile boolean mClearingData;
    final HashMap<String,IBackupTransport> mTransports
            = new HashMap<String,IBackupTransport>();
    String mCurrentTransport;
    IBackupTransport mLocalTransport, mGoogleTransport;
    ActiveRestoreSession mActiveRestoreSession;
    class RestoreGetSetsParams {
        public IBackupTransport transport;
        public ActiveRestoreSession session;
        public IRestoreObserver observer;
        RestoreGetSetsParams(IBackupTransport _transport, ActiveRestoreSession _session,
                IRestoreObserver _observer) {
            transport = _transport;
            session = _session;
            observer = _observer;
        }
    }
    class RestoreParams {
        public IBackupTransport transport;
        public IRestoreObserver observer;
        public long token;
        public PackageInfo pkgInfo;
        public int pmToken; 
        RestoreParams(IBackupTransport _transport, IRestoreObserver _obs,
                long _token, PackageInfo _pkg, int _pmToken) {
            transport = _transport;
            observer = _obs;
            token = _token;
            pkgInfo = _pkg;
            pmToken = _pmToken;
        }
        RestoreParams(IBackupTransport _transport, IRestoreObserver _obs, long _token) {
            transport = _transport;
            observer = _obs;
            token = _token;
            pkgInfo = null;
            pmToken = 0;
        }
    }
    class ClearParams {
        public IBackupTransport transport;
        public PackageInfo packageInfo;
        ClearParams(IBackupTransport _transport, PackageInfo _info) {
            transport = _transport;
            packageInfo = _info;
        }
    }
    static final int OP_PENDING = 0;
    static final int OP_ACKNOWLEDGED = 1;
    static final int OP_TIMEOUT = -1;
    final SparseIntArray mCurrentOperations = new SparseIntArray();
    final Object mCurrentOpLock = new Object();
    final Random mTokenGenerator = new Random();
    File mBaseStateDir;
    File mDataDir;
    File mJournalDir;
    File mJournal;
    private File mEverStored;
    HashSet<String> mEverStoredApps = new HashSet<String>();
    static final int CURRENT_ANCESTRAL_RECORD_VERSION = 1;  
    File mTokenFile;
    Set<String> mAncestralPackages = null;
    long mAncestralToken = 0;
    long mCurrentToken = 0;
    static final String INIT_SENTINEL_FILE_NAME = "_need_init_";
    HashSet<String> mPendingInits = new HashSet<String>();  
    private class BackupHandler extends Handler {
        public BackupHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_RUN_BACKUP:
            {
                mLastBackupPass = System.currentTimeMillis();
                mNextBackupPass = mLastBackupPass + BACKUP_INTERVAL;
                IBackupTransport transport = getTransport(mCurrentTransport);
                if (transport == null) {
                    Slog.v(TAG, "Backup requested but no transport available");
                    mWakelock.release();
                    break;
                }
                ArrayList<BackupRequest> queue = new ArrayList<BackupRequest>();
                File oldJournal = mJournal;
                synchronized (mQueueLock) {
                    if (mPendingBackups.size() > 0) {
                        for (BackupRequest b: mPendingBackups.values()) {
                            queue.add(b);
                        }
                        if (DEBUG) Slog.v(TAG, "clearing pending backups");
                        mPendingBackups.clear();
                        mJournal = null;
                    }
                }
                if (queue.size() > 0) {
                    (new PerformBackupTask(transport, queue, oldJournal)).run();
                } else {
                    Slog.v(TAG, "Backup requested but nothing pending");
                    mWakelock.release();
                }
                break;
            }
            case MSG_RUN_FULL_BACKUP:
                break;
            case MSG_RUN_RESTORE:
            {
                RestoreParams params = (RestoreParams)msg.obj;
                Slog.d(TAG, "MSG_RUN_RESTORE observer=" + params.observer);
                (new PerformRestoreTask(params.transport, params.observer,
                        params.token, params.pkgInfo, params.pmToken)).run();
                break;
            }
            case MSG_RUN_CLEAR:
            {
                ClearParams params = (ClearParams)msg.obj;
                (new PerformClearTask(params.transport, params.packageInfo)).run();
                break;
            }
            case MSG_RUN_INITIALIZE:
            {
                HashSet<String> queue;
                synchronized (mQueueLock) {
                    queue = new HashSet<String>(mPendingInits);
                    mPendingInits.clear();
                }
                (new PerformInitializeTask(queue)).run();
                break;
            }
            case MSG_RUN_GET_RESTORE_SETS:
            {
                RestoreSet[] sets = null;
                RestoreGetSetsParams params = (RestoreGetSetsParams)msg.obj;
                try {
                    sets = params.transport.getAvailableRestoreSets();
                    synchronized (params.session) {
                        params.session.mRestoreSets = sets;
                    }
                    if (sets == null) EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE);
                } catch (Exception e) {
                    Slog.e(TAG, "Error from transport getting set list");
                } finally {
                    if (params.observer != null) {
                        try {
                            params.observer.restoreSetsAvailable(sets);
                        } catch (RemoteException re) {
                            Slog.e(TAG, "Unable to report listing to observer");
                        } catch (Exception e) {
                            Slog.e(TAG, "Restore observer threw", e);
                        }
                    }
                    mWakelock.release();
                }
                break;
            }
            case MSG_TIMEOUT:
            {
                synchronized (mCurrentOpLock) {
                    final int token = msg.arg1;
                    int state = mCurrentOperations.get(token, OP_TIMEOUT);
                    if (state == OP_PENDING) {
                        if (DEBUG) Slog.v(TAG, "TIMEOUT: token=" + token);
                        mCurrentOperations.put(token, OP_TIMEOUT);
                    }
                    mCurrentOpLock.notifyAll();
                }
                break;
            }
            }
        }
    }
    public BackupManagerService(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
        mPackageManagerBinder = ActivityThread.getPackageManager();
        mActivityManager = ActivityManagerNative.getDefault();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mBackupManagerBinder = asInterface(asBinder());
        mHandlerThread = new HandlerThread("backup", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mBackupHandler = new BackupHandler(mHandlerThread.getLooper());
        boolean areEnabled = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_ENABLED, 0) != 0;
        mProvisioned = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_PROVISIONED, 0) != 0;
        mAutoRestore = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.BACKUP_AUTO_RESTORE, 1) != 0;
        mBaseStateDir = new File(Environment.getDataDirectory(), "backup");
        mBaseStateDir.mkdirs();
        mDataDir = Environment.getDownloadCacheDirectory();
        mRunBackupReceiver = new RunBackupReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RUN_BACKUP_ACTION);
        context.registerReceiver(mRunBackupReceiver, filter,
                android.Manifest.permission.BACKUP, null);
        mRunInitReceiver = new RunInitializeReceiver();
        filter = new IntentFilter();
        filter.addAction(RUN_INITIALIZE_ACTION);
        context.registerReceiver(mRunInitReceiver, filter,
                android.Manifest.permission.BACKUP, null);
        Intent backupIntent = new Intent(RUN_BACKUP_ACTION);
        backupIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mRunBackupIntent = PendingIntent.getBroadcast(context, MSG_RUN_BACKUP, backupIntent, 0);
        Intent initIntent = new Intent(RUN_INITIALIZE_ACTION);
        backupIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mRunInitIntent = PendingIntent.getBroadcast(context, MSG_RUN_INITIALIZE, initIntent, 0);
        mJournalDir = new File(mBaseStateDir, "pending");
        mJournalDir.mkdirs();   
        mJournal = null;        
        initPackageTracking();
        synchronized (mBackupParticipants) {
            addPackageParticipantsLocked(null);
        }
        mLocalTransport = new LocalTransport(context);  
        ComponentName localName = new ComponentName(context, LocalTransport.class);
        registerTransport(localName.flattenToShortString(), mLocalTransport);
        mGoogleTransport = null;
        mCurrentTransport = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.BACKUP_TRANSPORT);
        if ("".equals(mCurrentTransport)) {
            mCurrentTransport = null;
        }
        if (DEBUG) Slog.v(TAG, "Starting with transport " + mCurrentTransport);
        ComponentName transportComponent = new ComponentName("com.google.android.backup",
                "com.google.android.backup.BackupTransportService");
        try {
            ApplicationInfo info = mPackageManager.getApplicationInfo(
                    transportComponent.getPackageName(), 0);
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                if (DEBUG) Slog.v(TAG, "Binding to Google transport");
                Intent intent = new Intent().setComponent(transportComponent);
                context.bindService(intent, mGoogleConnection, Context.BIND_AUTO_CREATE);
            } else {
                Slog.w(TAG, "Possible Google transport spoof: ignoring " + info);
            }
        } catch (PackageManager.NameNotFoundException nnf) {
            if (DEBUG) Slog.v(TAG, "Google transport not present");
        }
        parseLeftoverJournals();
        mWakelock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "backup");
        setBackupEnabled(areEnabled);
    }
    private class RunBackupReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (RUN_BACKUP_ACTION.equals(intent.getAction())) {
                synchronized (mQueueLock) {
                    if (mPendingInits.size() > 0) {
                        if (DEBUG) Slog.v(TAG, "Init pending at scheduled backup");
                        try {
                            mAlarmManager.cancel(mRunInitIntent);
                            mRunInitIntent.send();
                        } catch (PendingIntent.CanceledException ce) {
                            Slog.e(TAG, "Run init intent cancelled");
                        }
                    } else {
                        if (mEnabled && mProvisioned) {
                            if (DEBUG) Slog.v(TAG, "Running a backup pass");
                            mWakelock.acquire();
                            Message msg = mBackupHandler.obtainMessage(MSG_RUN_BACKUP);
                            mBackupHandler.sendMessage(msg);
                        } else {
                            Slog.w(TAG, "Backup pass but e=" + mEnabled + " p=" + mProvisioned);
                        }
                    }
                }
            }
        }
    }
    private class RunInitializeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (RUN_INITIALIZE_ACTION.equals(intent.getAction())) {
                synchronized (mQueueLock) {
                    if (DEBUG) Slog.v(TAG, "Running a device init");
                    mWakelock.acquire();
                    Message msg = mBackupHandler.obtainMessage(MSG_RUN_INITIALIZE);
                    mBackupHandler.sendMessage(msg);
                }
            }
        }
    }
    private void initPackageTracking() {
        if (DEBUG) Slog.v(TAG, "Initializing package tracking");
        mTokenFile = new File(mBaseStateDir, "ancestral");
        try {
            RandomAccessFile tf = new RandomAccessFile(mTokenFile, "r");
            int version = tf.readInt();
            if (version == CURRENT_ANCESTRAL_RECORD_VERSION) {
                mAncestralToken = tf.readLong();
                mCurrentToken = tf.readLong();
                int numPackages = tf.readInt();
                if (numPackages >= 0) {
                    mAncestralPackages = new HashSet<String>();
                    for (int i = 0; i < numPackages; i++) {
                        String pkgName = tf.readUTF();
                        mAncestralPackages.add(pkgName);
                    }
                }
            }
        } catch (FileNotFoundException fnf) {
            Slog.v(TAG, "No ancestral data");
        } catch (IOException e) {
            Slog.w(TAG, "Unable to read token file", e);
        }
        mEverStored = new File(mBaseStateDir, "processed");
        File tempProcessedFile = new File(mBaseStateDir, "processed.new");
        if (tempProcessedFile.exists()) {
            tempProcessedFile.delete();
        }
        if (mEverStored.exists()) {
            RandomAccessFile temp = null;
            RandomAccessFile in = null;
            try {
                temp = new RandomAccessFile(tempProcessedFile, "rws");
                in = new RandomAccessFile(mEverStored, "r");
                while (true) {
                    PackageInfo info;
                    String pkg = in.readUTF();
                    try {
                        info = mPackageManager.getPackageInfo(pkg, 0);
                        mEverStoredApps.add(pkg);
                        temp.writeUTF(pkg);
                        if (DEBUG) Slog.v(TAG, "   + " + pkg);
                    } catch (NameNotFoundException e) {
                        if (DEBUG) Slog.v(TAG, "   - " + pkg);
                    }
                }
            } catch (EOFException e) {
                if (!tempProcessedFile.renameTo(mEverStored)) {
                    Slog.e(TAG, "Error renaming " + tempProcessedFile + " to " + mEverStored);
                }
            } catch (IOException e) {
                Slog.e(TAG, "Error in processed file", e);
            } finally {
                try { if (temp != null) temp.close(); } catch (IOException e) {}
                try { if (in != null) in.close(); } catch (IOException e) {}
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mBroadcastReceiver, filter);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        mContext.registerReceiver(mBroadcastReceiver, sdFilter);
    }
    private void parseLeftoverJournals() {
        for (File f : mJournalDir.listFiles()) {
            if (mJournal == null || f.compareTo(mJournal) != 0) {
                RandomAccessFile in = null;
                try {
                    Slog.i(TAG, "Found stale backup journal, scheduling:");
                    in = new RandomAccessFile(f, "r");
                    while (true) {
                        String packageName = in.readUTF();
                        Slog.i(TAG, "    + " + packageName);
                        dataChanged(packageName);
                    }
                } catch (EOFException e) {
                } catch (Exception e) {
                    Slog.e(TAG, "Can't read " + f, e);
                } finally {
                    try { if (in != null) in.close(); } catch (IOException e) {}
                    f.delete();
                }
            }
        }
    }
    void recordInitPendingLocked(boolean isPending, String transportName) {
        if (DEBUG) Slog.i(TAG, "recordInitPendingLocked: " + isPending
                + " on transport " + transportName);
        try {
            IBackupTransport transport = getTransport(transportName);
            String transportDirName = transport.transportDirName();
            File stateDir = new File(mBaseStateDir, transportDirName);
            File initPendingFile = new File(stateDir, INIT_SENTINEL_FILE_NAME);
            if (isPending) {
                mPendingInits.add(transportName);
                try {
                    (new FileOutputStream(initPendingFile)).close();
                } catch (IOException ioe) {
                }
            } else {
                initPendingFile.delete();
                mPendingInits.remove(transportName);
            }
        } catch (RemoteException e) {
        }
    }
    void resetBackupState(File stateFileDir) {
        synchronized (mQueueLock) {
            mEverStoredApps.clear();
            mEverStored.delete();
            mCurrentToken = 0;
            writeRestoreTokens();
            for (File sf : stateFileDir.listFiles()) {
                if (!sf.getName().equals(INIT_SENTINEL_FILE_NAME)) {
                    sf.delete();
                }
            }
            int N = mBackupParticipants.size();
            for (int i=0; i<N; i++) {
                int uid = mBackupParticipants.keyAt(i);
                HashSet<ApplicationInfo> participants = mBackupParticipants.valueAt(i);
                for (ApplicationInfo app: participants) {
                    dataChanged(app.packageName);
                }
            }
        }
    }
    private void registerTransport(String name, IBackupTransport transport) {
        synchronized (mTransports) {
            if (DEBUG) Slog.v(TAG, "Registering transport " + name + " = " + transport);
            if (transport != null) {
                mTransports.put(name, transport);
            } else {
                mTransports.remove(name);
                if ((mCurrentTransport != null) && mCurrentTransport.equals(name)) {
                    mCurrentTransport = null;
                }
                return;
            }
        }
        try {
            String transportName = transport.transportDirName();
            File stateDir = new File(mBaseStateDir, transportName);
            stateDir.mkdirs();
            File initSentinel = new File(stateDir, INIT_SENTINEL_FILE_NAME);
            if (initSentinel.exists()) {
                synchronized (mQueueLock) {
                    mPendingInits.add(transportName);
                    long delay = 1000 * 60; 
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                            System.currentTimeMillis() + delay, mRunInitIntent);
                }
            }
        } catch (RemoteException e) {
        }
    }
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Slog.d(TAG, "Received broadcast " + intent);
            String action = intent.getAction();
            boolean replacing = false;
            boolean added = false;
            Bundle extras = intent.getExtras();
            String pkgList[] = null;
            if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                    Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                Uri uri = intent.getData();
                if (uri == null) {
                    return;
                }
                String pkgName = uri.getSchemeSpecificPart();
                if (pkgName != null) {
                    pkgList = new String[] { pkgName };
                }
                added = Intent.ACTION_PACKAGE_ADDED.equals(action);
                replacing = extras.getBoolean(Intent.EXTRA_REPLACING, false);
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                added = true;
                pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                added = false;
                pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            }
            if (pkgList == null || pkgList.length == 0) {
                return;
            }
            if (added) {
                synchronized (mBackupParticipants) {
                    for (String pkgName : pkgList) {
                        if (replacing) {
                            updatePackageParticipantsLocked(pkgName);
                        } else {
                            addPackageParticipantsLocked(pkgName);
                        }
                    }
                }
            } else {
                if (replacing) {
                } else {
                    synchronized (mBackupParticipants) {
                        for (String pkgName : pkgList) {
                            removePackageParticipantsLocked(pkgName);
                        }
                    }
                }
            }
        }
    };
    ServiceConnection mGoogleConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (DEBUG) Slog.v(TAG, "Connected to Google transport");
            mGoogleTransport = IBackupTransport.Stub.asInterface(service);
            registerTransport(name.flattenToShortString(), mGoogleTransport);
        }
        public void onServiceDisconnected(ComponentName name) {
            if (DEBUG) Slog.v(TAG, "Disconnected from Google transport");
            mGoogleTransport = null;
            registerTransport(name.flattenToShortString(), null);
        }
    };
    void addPackageParticipantsLocked(String packageName) {
        if (DEBUG) Slog.v(TAG, "addPackageParticipantsLocked: " + packageName);
        List<PackageInfo> targetApps = allAgentPackages();
        addPackageParticipantsLockedInner(packageName, targetApps);
    }
    private void addPackageParticipantsLockedInner(String packageName,
            List<PackageInfo> targetPkgs) {
        if (DEBUG) {
            Slog.v(TAG, "Adding " + targetPkgs.size() + " backup participants:");
            for (PackageInfo p : targetPkgs) {
                Slog.v(TAG, "    " + p + " agent=" + p.applicationInfo.backupAgentName
                        + " uid=" + p.applicationInfo.uid
                        + " killAfterRestore="
                        + (((p.applicationInfo.flags & ApplicationInfo.FLAG_KILL_AFTER_RESTORE) != 0) ? "true" : "false")
                        );
            }
        }
        for (PackageInfo pkg : targetPkgs) {
            if (packageName == null || pkg.packageName.equals(packageName)) {
                int uid = pkg.applicationInfo.uid;
                HashSet<ApplicationInfo> set = mBackupParticipants.get(uid);
                if (set == null) {
                    set = new HashSet<ApplicationInfo>();
                    mBackupParticipants.put(uid, set);
                }
                set.add(pkg.applicationInfo);
                if (!mEverStoredApps.contains(pkg.packageName)) {
                    if (DEBUG) Slog.i(TAG, "New app " + pkg.packageName
                            + " never backed up; scheduling");
                    dataChanged(pkg.packageName);
                }
            }
        }
    }
    void removePackageParticipantsLocked(String packageName) {
        if (DEBUG) Slog.v(TAG, "removePackageParticipantsLocked: " + packageName);
        List<PackageInfo> allApps = null;
        if (packageName != null) {
            allApps = new ArrayList<PackageInfo>();
            try {
                int flags = PackageManager.GET_SIGNATURES;
                allApps.add(mPackageManager.getPackageInfo(packageName, flags));
            } catch (Exception e) {
            }
        } else {
            allApps = allAgentPackages();
        }
        removePackageParticipantsLockedInner(packageName, allApps);
    }
    private void removePackageParticipantsLockedInner(String packageName,
            List<PackageInfo> agents) {
        if (DEBUG) {
            Slog.v(TAG, "removePackageParticipantsLockedInner (" + packageName
                    + ") removing " + agents.size() + " entries");
            for (PackageInfo p : agents) {
                Slog.v(TAG, "    - " + p);
            }
        }
        for (PackageInfo pkg : agents) {
            if (packageName == null || pkg.packageName.equals(packageName)) {
                int uid = pkg.applicationInfo.uid;
                HashSet<ApplicationInfo> set = mBackupParticipants.get(uid);
                if (set != null) {
                    for (ApplicationInfo entry: set) {
                        if (entry.packageName.equals(pkg.packageName)) {
                            set.remove(entry);
                            removeEverBackedUp(pkg.packageName);
                            break;
                        }
                    }
                    if (set.size() == 0) {
                        mBackupParticipants.delete(uid);
                    }
                }
            }
        }
    }
    List<PackageInfo> allAgentPackages() {
        int flags = PackageManager.GET_SIGNATURES;
        List<PackageInfo> packages = mPackageManager.getInstalledPackages(flags);
        int N = packages.size();
        for (int a = N-1; a >= 0; a--) {
            PackageInfo pkg = packages.get(a);
            try {
                ApplicationInfo app = pkg.applicationInfo;
                if (((app.flags&ApplicationInfo.FLAG_ALLOW_BACKUP) == 0)
                        || app.backupAgentName == null) {
                    packages.remove(a);
                }
                else {
                    app = mPackageManager.getApplicationInfo(pkg.packageName,
                            PackageManager.GET_SHARED_LIBRARY_FILES);
                    pkg.applicationInfo.sharedLibraryFiles = app.sharedLibraryFiles;
                }
            } catch (NameNotFoundException e) {
                packages.remove(a);
            }
        }
        return packages;
    }
    void updatePackageParticipantsLocked(String packageName) {
        if (packageName == null) {
            Slog.e(TAG, "updatePackageParticipants called with null package name");
            return;
        }
        if (DEBUG) Slog.v(TAG, "updatePackageParticipantsLocked: " + packageName);
        List<PackageInfo> allApps = allAgentPackages();
        removePackageParticipantsLockedInner(packageName, allApps);
        addPackageParticipantsLockedInner(packageName, allApps);
    }
    void logBackupComplete(String packageName) {
        if (packageName.equals(PACKAGE_MANAGER_SENTINEL)) return;
        synchronized (mEverStoredApps) {
            if (!mEverStoredApps.add(packageName)) return;
            RandomAccessFile out = null;
            try {
                out = new RandomAccessFile(mEverStored, "rws");
                out.seek(out.length());
                out.writeUTF(packageName);
            } catch (IOException e) {
                Slog.e(TAG, "Can't log backup of " + packageName + " to " + mEverStored);
            } finally {
                try { if (out != null) out.close(); } catch (IOException e) {}
            }
        }
    }
    void removeEverBackedUp(String packageName) {
        if (DEBUG) Slog.v(TAG, "Removing backed-up knowledge of " + packageName + ", new set:");
        synchronized (mEverStoredApps) {
            File tempKnownFile = new File(mBaseStateDir, "processed.new");
            RandomAccessFile known = null;
            try {
                known = new RandomAccessFile(tempKnownFile, "rws");
                mEverStoredApps.remove(packageName);
                for (String s : mEverStoredApps) {
                    known.writeUTF(s);
                    if (DEBUG) Slog.v(TAG, "    " + s);
                }
                known.close();
                known = null;
                if (!tempKnownFile.renameTo(mEverStored)) {
                    throw new IOException("Can't rename " + tempKnownFile + " to " + mEverStored);
                }
            } catch (IOException e) {
                Slog.w(TAG, "Error rewriting " + mEverStored, e);
                mEverStoredApps.clear();
                tempKnownFile.delete();
                mEverStored.delete();
            } finally {
                try { if (known != null) known.close(); } catch (IOException e) {}
            }
        }
    }
    void writeRestoreTokens() {
        try {
            RandomAccessFile af = new RandomAccessFile(mTokenFile, "rwd");
            af.writeInt(CURRENT_ANCESTRAL_RECORD_VERSION);
            af.writeLong(mAncestralToken);
            af.writeLong(mCurrentToken);
            if (mAncestralPackages == null) {
                af.writeInt(-1);
            } else {
                af.writeInt(mAncestralPackages.size());
                if (DEBUG) Slog.v(TAG, "Ancestral packages:  " + mAncestralPackages.size());
                for (String pkgName : mAncestralPackages) {
                    af.writeUTF(pkgName);
                    if (DEBUG) Slog.v(TAG, "   " + pkgName);
                }
            }
            af.close();
        } catch (IOException e) {
            Slog.w(TAG, "Unable to write token file:", e);
        }
    }
    private IBackupTransport getTransport(String transportName) {
        synchronized (mTransports) {
            IBackupTransport transport = mTransports.get(transportName);
            if (transport == null) {
                Slog.w(TAG, "Requested unavailable transport: " + transportName);
            }
            return transport;
        }
    }
    IBackupAgent bindToAgentSynchronous(ApplicationInfo app, int mode) {
        IBackupAgent agent = null;
        synchronized(mAgentConnectLock) {
            mConnecting = true;
            mConnectedAgent = null;
            try {
                if (mActivityManager.bindBackupAgent(app, mode)) {
                    Slog.d(TAG, "awaiting agent for " + app);
                    long timeoutMark = System.currentTimeMillis() + TIMEOUT_INTERVAL;
                    while (mConnecting && mConnectedAgent == null
                            && (System.currentTimeMillis() < timeoutMark)) {
                        try {
                            mAgentConnectLock.wait(5000);
                        } catch (InterruptedException e) {
                            return null;
                        }
                    }
                    if (mConnecting == true) {
                        Slog.w(TAG, "Timeout waiting for agent " + app);
                        return null;
                    }
                    agent = mConnectedAgent;
                }
            } catch (RemoteException e) {
            }
        }
        return agent;
    }
    void clearApplicationDataSynchronous(String packageName) {
        try {
            PackageInfo info = mPackageManager.getPackageInfo(packageName, 0);
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA) == 0) {
                if (DEBUG) Slog.i(TAG, "allowClearUserData=false so not wiping "
                        + packageName);
                return;
            }
        } catch (NameNotFoundException e) {
            Slog.w(TAG, "Tried to clear data for " + packageName + " but not found");
            return;
        }
        ClearDataObserver observer = new ClearDataObserver();
        synchronized(mClearDataLock) {
            mClearingData = true;
            try {
                mActivityManager.clearApplicationUserData(packageName, observer);
            } catch (RemoteException e) {
            }
            long timeoutMark = System.currentTimeMillis() + TIMEOUT_INTERVAL;
            while (mClearingData && (System.currentTimeMillis() < timeoutMark)) {
                try {
                    mClearDataLock.wait(5000);
                } catch (InterruptedException e) {
                    mClearingData = false;
                }
            }
        }
    }
    class ClearDataObserver extends IPackageDataObserver.Stub {
        public void onRemoveCompleted(String packageName, boolean succeeded) {
            synchronized(mClearDataLock) {
                mClearingData = false;
                mClearDataLock.notifyAll();
            }
        }
    }
    long getAvailableRestoreToken(String packageName) {
        long token = mAncestralToken;
        synchronized (mQueueLock) {
            if (mEverStoredApps.contains(packageName)) {
                token = mCurrentToken;
            }
        }
        return token;
    }
    boolean waitUntilOperationComplete(int token) {
        int finalState = OP_PENDING;
        synchronized (mCurrentOpLock) {
            try {
                while ((finalState = mCurrentOperations.get(token, OP_TIMEOUT)) == OP_PENDING) {
                    try {
                        mCurrentOpLock.wait();
                    } catch (InterruptedException e) {}
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }
        mBackupHandler.removeMessages(MSG_TIMEOUT);
        if (DEBUG) Slog.v(TAG, "operation " + Integer.toHexString(token)
                + " complete: finalState=" + finalState);
        return finalState == OP_ACKNOWLEDGED;
    }
    void prepareOperationTimeout(int token, long interval) {
        if (DEBUG) Slog.v(TAG, "starting timeout: token=" + Integer.toHexString(token)
                + " interval=" + interval);
        mCurrentOperations.put(token, OP_PENDING);
        Message msg = mBackupHandler.obtainMessage(MSG_TIMEOUT, token, 0);
        mBackupHandler.sendMessageDelayed(msg, interval);
    }
    class PerformBackupTask implements Runnable {
        private static final String TAG = "PerformBackupThread";
        IBackupTransport mTransport;
        ArrayList<BackupRequest> mQueue;
        File mStateDir;
        File mJournal;
        public PerformBackupTask(IBackupTransport transport, ArrayList<BackupRequest> queue,
                File journal) {
            mTransport = transport;
            mQueue = queue;
            mJournal = journal;
            try {
                mStateDir = new File(mBaseStateDir, transport.transportDirName());
            } catch (RemoteException e) {
            }
        }
        public void run() {
            int status = BackupConstants.TRANSPORT_OK;
            long startRealtime = SystemClock.elapsedRealtime();
            if (DEBUG) Slog.v(TAG, "Beginning backup of " + mQueue.size() + " targets");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                EventLog.writeEvent(EventLogTags.BACKUP_START, mTransport.transportDirName());
                File pmState = new File(mStateDir, PACKAGE_MANAGER_SENTINEL);
                if (status == BackupConstants.TRANSPORT_OK && pmState.length() <= 0) {
                    Slog.i(TAG, "Initializing (wiping) backup state and transport storage");
                    resetBackupState(mStateDir);  
                    status = mTransport.initializeDevice();
                    if (status == BackupConstants.TRANSPORT_OK) {
                        EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE);
                    } else {
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                        Slog.e(TAG, "Transport error in initializeDevice()");
                    }
                }
                if (status == BackupConstants.TRANSPORT_OK) {
                    PackageManagerBackupAgent pmAgent = new PackageManagerBackupAgent(
                            mPackageManager, allAgentPackages());
                    BackupRequest pmRequest = new BackupRequest(new ApplicationInfo(), false);
                    pmRequest.appInfo.packageName = PACKAGE_MANAGER_SENTINEL;
                    status = processOneBackup(pmRequest,
                            IBackupAgent.Stub.asInterface(pmAgent.onBind()), mTransport);
                }
                if (status == BackupConstants.TRANSPORT_OK) {
                    status = doQueuedBackups(mTransport);
                }
                if (status == BackupConstants.TRANSPORT_OK) {
                    status = mTransport.finishBackup();
                    if (status == BackupConstants.TRANSPORT_OK) {
                        int millis = (int) (SystemClock.elapsedRealtime() - startRealtime);
                        EventLog.writeEvent(EventLogTags.BACKUP_SUCCESS, mQueue.size(), millis);
                    } else {
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(finish)");
                        Slog.e(TAG, "Transport error in finishBackup()");
                    }
                }
                if (status == BackupConstants.TRANSPORT_NOT_INITIALIZED) {
                    EventLog.writeEvent(EventLogTags.BACKUP_RESET, mTransport.transportDirName());
                    resetBackupState(mStateDir);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Error in backup thread", e);
                status = BackupConstants.TRANSPORT_ERROR;
            } finally {
                if ((mCurrentToken == 0) && (status == BackupConstants.TRANSPORT_OK)) {
                    try {
                        mCurrentToken = mTransport.getCurrentRestoreSet();
                    } catch (RemoteException e) {  }
                    writeRestoreTokens();
                }
                if (status != BackupConstants.TRANSPORT_OK) {
                    Slog.w(TAG, "Backup pass unsuccessful, restaging");
                    for (BackupRequest req : mQueue) {
                        dataChanged(req.appInfo.packageName);
                    }
                    try {
                        startBackupAlarmsLocked(mTransport.requestBackupTime());
                    } catch (RemoteException e) {  }
                }
                if (mJournal != null && !mJournal.delete()) {
                    Slog.e(TAG, "Unable to remove backup journal file " + mJournal);
                }
                if (status == BackupConstants.TRANSPORT_NOT_INITIALIZED) {
                    backupNow();
                }
                mWakelock.release();
            }
        }
        private int doQueuedBackups(IBackupTransport transport) {
            for (BackupRequest request : mQueue) {
                Slog.d(TAG, "starting agent for backup of " + request);
                IBackupAgent agent = null;
                int mode = (request.fullBackup)
                        ? IApplicationThread.BACKUP_MODE_FULL
                        : IApplicationThread.BACKUP_MODE_INCREMENTAL;
                try {
                    agent = bindToAgentSynchronous(request.appInfo, mode);
                    if (agent != null) {
                        int result = processOneBackup(request, agent, transport);
                        if (result != BackupConstants.TRANSPORT_OK) return result;
                    }
                } catch (SecurityException ex) {
                    Slog.d(TAG, "error in bind/backup", ex);
                } finally {
                    try {  
                        mActivityManager.unbindBackupAgent(request.appInfo);
                    } catch (RemoteException e) {}
                }
            }
            return BackupConstants.TRANSPORT_OK;
        }
        private int processOneBackup(BackupRequest request, IBackupAgent agent,
                IBackupTransport transport) {
            final String packageName = request.appInfo.packageName;
            if (DEBUG) Slog.d(TAG, "processOneBackup doBackup() on " + packageName);
            File savedStateName = new File(mStateDir, packageName);
            File backupDataName = new File(mDataDir, packageName + ".data");
            File newStateName = new File(mStateDir, packageName + ".new");
            ParcelFileDescriptor savedState = null;
            ParcelFileDescriptor backupData = null;
            ParcelFileDescriptor newState = null;
            PackageInfo packInfo;
            int token = mTokenGenerator.nextInt();
            try {
                if (packageName.equals(PACKAGE_MANAGER_SENTINEL)) {
                    packInfo = new PackageInfo();
                    packInfo.packageName = packageName;
                } else {
                    packInfo = mPackageManager.getPackageInfo(packageName,
                        PackageManager.GET_SIGNATURES);
                }
                if (!request.fullBackup) {
                    savedState = ParcelFileDescriptor.open(savedStateName,
                            ParcelFileDescriptor.MODE_READ_ONLY |
                            ParcelFileDescriptor.MODE_CREATE);  
                }
                backupData = ParcelFileDescriptor.open(backupDataName,
                        ParcelFileDescriptor.MODE_READ_WRITE |
                        ParcelFileDescriptor.MODE_CREATE |
                        ParcelFileDescriptor.MODE_TRUNCATE);
                newState = ParcelFileDescriptor.open(newStateName,
                        ParcelFileDescriptor.MODE_READ_WRITE |
                        ParcelFileDescriptor.MODE_CREATE |
                        ParcelFileDescriptor.MODE_TRUNCATE);
                prepareOperationTimeout(token, TIMEOUT_BACKUP_INTERVAL);
                agent.doBackup(savedState, backupData, newState, token, mBackupManagerBinder);
                boolean success = waitUntilOperationComplete(token);
                if (!success) {
                    throw new RuntimeException("Backup timeout");
                }
                logBackupComplete(packageName);
                if (DEBUG) Slog.v(TAG, "doBackup() success");
            } catch (Exception e) {
                Slog.e(TAG, "Error backing up " + packageName, e);
                EventLog.writeEvent(EventLogTags.BACKUP_AGENT_FAILURE, packageName, e.toString());
                backupDataName.delete();
                newStateName.delete();
                return BackupConstants.TRANSPORT_ERROR;
            } finally {
                try { if (savedState != null) savedState.close(); } catch (IOException e) {}
                try { if (backupData != null) backupData.close(); } catch (IOException e) {}
                try { if (newState != null) newState.close(); } catch (IOException e) {}
                savedState = backupData = newState = null;
                synchronized (mCurrentOpLock) {
                    mCurrentOperations.clear();
                }
            }
            int result = BackupConstants.TRANSPORT_OK;
            try {
                int size = (int) backupDataName.length();
                if (size > 0) {
                    if (result == BackupConstants.TRANSPORT_OK) {
                        backupData = ParcelFileDescriptor.open(backupDataName,
                                ParcelFileDescriptor.MODE_READ_ONLY);
                        result = transport.performBackup(packInfo, backupData);
                    }
                    if (result == BackupConstants.TRANSPORT_OK) {
                        result = transport.finishBackup();
                    }
                } else {
                    if (DEBUG) Slog.i(TAG, "no backup data written; not calling transport");
                }
                if (result == BackupConstants.TRANSPORT_OK) {
                    backupDataName.delete();
                    newStateName.renameTo(savedStateName);
                    EventLog.writeEvent(EventLogTags.BACKUP_PACKAGE, packageName, size);
                } else {
                    EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, packageName);
                }
            } catch (Exception e) {
                Slog.e(TAG, "Transport error backing up " + packageName, e);
                EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, packageName);
                result = BackupConstants.TRANSPORT_ERROR;
            } finally {
                try { if (backupData != null) backupData.close(); } catch (IOException e) {}
            }
            return result;
        }
    }
    private boolean signaturesMatch(Signature[] storedSigs, PackageInfo target) {
        if ((target.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            if (DEBUG) Slog.v(TAG, "System app " + target.packageName + " - skipping sig check");
            return true;
        }
        Signature[] deviceSigs = target.signatures;
        if (DEBUG) Slog.v(TAG, "signaturesMatch(): stored=" + storedSigs
                + " device=" + deviceSigs);
        if ((storedSigs == null || storedSigs.length == 0)
                && (deviceSigs == null || deviceSigs.length == 0)) {
            return true;
        }
        if (storedSigs == null || deviceSigs == null) {
            return false;
        }
        int nStored = storedSigs.length;
        int nDevice = deviceSigs.length;
        for (int i=0; i < nStored; i++) {
            boolean match = false;
            for (int j=0; j < nDevice; j++) {
                if (storedSigs[i].equals(deviceSigs[j])) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }
    class PerformRestoreTask implements Runnable {
        private IBackupTransport mTransport;
        private IRestoreObserver mObserver;
        private long mToken;
        private PackageInfo mTargetPackage;
        private File mStateDir;
        private int mPmToken;
        class RestoreRequest {
            public PackageInfo app;
            public int storedAppVersion;
            RestoreRequest(PackageInfo _app, int _version) {
                app = _app;
                storedAppVersion = _version;
            }
        }
        PerformRestoreTask(IBackupTransport transport, IRestoreObserver observer,
                long restoreSetToken, PackageInfo targetPackage, int pmToken) {
            mTransport = transport;
            mObserver = observer;
            mToken = restoreSetToken;
            mTargetPackage = targetPackage;
            mPmToken = pmToken;
            try {
                mStateDir = new File(mBaseStateDir, transport.transportDirName());
            } catch (RemoteException e) {
            }
        }
        public void run() {
            long startRealtime = SystemClock.elapsedRealtime();
            if (DEBUG) Slog.v(TAG, "Beginning restore process mTransport=" + mTransport
                    + " mObserver=" + mObserver + " mToken=" + Long.toHexString(mToken)
                    + " mTargetPackage=" + mTargetPackage + " mPmToken=" + mPmToken);
            PackageManagerBackupAgent pmAgent = null;
            int error = -1; 
            try {
                EventLog.writeEvent(EventLogTags.RESTORE_START, mTransport.transportDirName(), mToken);
                ArrayList<PackageInfo> restorePackages = new ArrayList<PackageInfo>();
                PackageInfo omPackage = new PackageInfo();
                omPackage.packageName = PACKAGE_MANAGER_SENTINEL;
                restorePackages.add(omPackage);
                List<PackageInfo> agentPackages = allAgentPackages();
                if (mTargetPackage == null) {
                    restorePackages.addAll(agentPackages);
                } else {
                    restorePackages.add(mTargetPackage);
                }
                if (mObserver != null) {
                    try {
                        mObserver.restoreStarting(restorePackages.size());
                    } catch (RemoteException e) {
                        Slog.d(TAG, "Restore observer died at restoreStarting");
                        mObserver = null;
                    }
                }
                if (mTransport.startRestore(mToken, restorePackages.toArray(new PackageInfo[0])) !=
                        BackupConstants.TRANSPORT_OK) {
                    Slog.e(TAG, "Error starting restore operation");
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE);
                    return;
                }
                String packageName = mTransport.nextRestorePackage();
                if (packageName == null) {
                    Slog.e(TAG, "Error getting first restore package");
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE);
                    return;
                } else if (packageName.equals("")) {
                    Slog.i(TAG, "No restore data available");
                    int millis = (int) (SystemClock.elapsedRealtime() - startRealtime);
                    EventLog.writeEvent(EventLogTags.RESTORE_SUCCESS, 0, millis);
                    return;
                } else if (!packageName.equals(PACKAGE_MANAGER_SENTINEL)) {
                    Slog.e(TAG, "Expected restore data for \"" + PACKAGE_MANAGER_SENTINEL
                          + "\", found only \"" + packageName + "\"");
                    EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, PACKAGE_MANAGER_SENTINEL,
                            "Package manager data missing");
                    return;
                }
                pmAgent = new PackageManagerBackupAgent(
                        mPackageManager, agentPackages);
                processOneRestore(omPackage, 0, IBackupAgent.Stub.asInterface(pmAgent.onBind()));
                if (!pmAgent.hasMetadata()) {
                    Slog.e(TAG, "No restore metadata available, so not restoring settings");
                    EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, PACKAGE_MANAGER_SENTINEL,
                            "Package manager restore metadata missing");
                    return;
                }
                int count = 0;
                for (;;) {
                    packageName = mTransport.nextRestorePackage();
                    if (packageName == null) {
                        Slog.e(TAG, "Error getting next restore package");
                        EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE);
                        return;
                    } else if (packageName.equals("")) {
                        if (DEBUG) Slog.v(TAG, "No next package, finishing restore");
                        break;
                    }
                    if (mObserver != null) {
                        try {
                            mObserver.onUpdate(count, packageName);
                        } catch (RemoteException e) {
                            Slog.d(TAG, "Restore observer died in onUpdate");
                            mObserver = null;
                        }
                    }
                    Metadata metaInfo = pmAgent.getRestoredMetadata(packageName);
                    if (metaInfo == null) {
                        Slog.e(TAG, "Missing metadata for " + packageName);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, packageName,
                                "Package metadata missing");
                        continue;
                    }
                    PackageInfo packageInfo;
                    try {
                        int flags = PackageManager.GET_SIGNATURES;
                        packageInfo = mPackageManager.getPackageInfo(packageName, flags);
                    } catch (NameNotFoundException e) {
                        Slog.e(TAG, "Invalid package restoring data", e);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, packageName,
                                "Package missing on device");
                        continue;
                    }
                    if (metaInfo.versionCode > packageInfo.versionCode) {
                        if ((packageInfo.applicationInfo.flags
                                & ApplicationInfo.FLAG_RESTORE_ANY_VERSION) == 0) {
                            String message = "Version " + metaInfo.versionCode
                                    + " > installed version " + packageInfo.versionCode;
                            Slog.w(TAG, "Package " + packageName + ": " + message);
                            EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE,
                                    packageName, message);
                            continue;
                        } else {
                            if (DEBUG) Slog.v(TAG, "Version " + metaInfo.versionCode
                                    + " > installed " + packageInfo.versionCode
                                    + " but restoreAnyVersion");
                        }
                    }
                    if (!signaturesMatch(metaInfo.signatures, packageInfo)) {
                        Slog.w(TAG, "Signature mismatch restoring " + packageName);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, packageName,
                                "Signature mismatch");
                        continue;
                    }
                    if (DEBUG) Slog.v(TAG, "Package " + packageName
                            + " restore version [" + metaInfo.versionCode
                            + "] is compatible with installed version ["
                            + packageInfo.versionCode + "]");
                    IBackupAgent agent = bindToAgentSynchronous(
                            packageInfo.applicationInfo,
                            IApplicationThread.BACKUP_MODE_INCREMENTAL);
                    if (agent == null) {
                        Slog.w(TAG, "Can't find backup agent for " + packageName);
                        EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, packageName,
                                "Restore agent missing");
                        continue;
                    }
                    try {
                        processOneRestore(packageInfo, metaInfo.versionCode, agent);
                        ++count;
                    } finally {
                        mActivityManager.unbindBackupAgent(packageInfo.applicationInfo);
                        if (mTargetPackage == null && (packageInfo.applicationInfo.flags
                                & ApplicationInfo.FLAG_KILL_AFTER_RESTORE) != 0) {
                            if (DEBUG) Slog.d(TAG, "Restore complete, killing host process of "
                                    + packageInfo.applicationInfo.processName);
                            mActivityManager.killApplicationProcess(
                                    packageInfo.applicationInfo.processName,
                                    packageInfo.applicationInfo.uid);
                        }
                    }
                }
                error = 0;
                int millis = (int) (SystemClock.elapsedRealtime() - startRealtime);
                EventLog.writeEvent(EventLogTags.RESTORE_SUCCESS, count, millis);
            } catch (Exception e) {
                Slog.e(TAG, "Error in restore thread", e);
            } finally {
                if (DEBUG) Slog.d(TAG, "finishing restore mObserver=" + mObserver);
                try {
                    mTransport.finishRestore();
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error finishing restore", e);
                }
                if (mObserver != null) {
                    try {
                        mObserver.restoreFinished(error);
                    } catch (RemoteException e) {
                        Slog.d(TAG, "Restore observer died at restoreFinished");
                    }
                }
                if (mTargetPackage == null && pmAgent != null) {
                    mAncestralPackages = pmAgent.getRestoredPackages();
                    mAncestralToken = mToken;
                    writeRestoreTokens();
                }
                if (mPmToken > 0) {
                    if (DEBUG) Slog.v(TAG, "finishing PM token " + mPmToken);
                    try {
                        mPackageManagerBinder.finishPackageInstall(mPmToken);
                    } catch (RemoteException e) {  }
                }
                mWakelock.release();
            }
        }
        void processOneRestore(PackageInfo app, int appVersionCode, IBackupAgent agent) {
            final String packageName = app.packageName;
            if (DEBUG) Slog.d(TAG, "processOneRestore packageName=" + packageName);
            File backupDataName = new File(mDataDir, packageName + ".restore");
            File newStateName = new File(mStateDir, packageName + ".new");
            File savedStateName = new File(mStateDir, packageName);
            ParcelFileDescriptor backupData = null;
            ParcelFileDescriptor newState = null;
            int token = mTokenGenerator.nextInt();
            try {
                backupData = ParcelFileDescriptor.open(backupDataName,
                            ParcelFileDescriptor.MODE_READ_WRITE |
                            ParcelFileDescriptor.MODE_CREATE |
                            ParcelFileDescriptor.MODE_TRUNCATE);
                if (mTransport.getRestoreData(backupData) != BackupConstants.TRANSPORT_OK) {
                    Slog.e(TAG, "Error getting restore data for " + packageName);
                    EventLog.writeEvent(EventLogTags.RESTORE_TRANSPORT_FAILURE);
                    return;
                }
                backupData.close();
                backupData = ParcelFileDescriptor.open(backupDataName,
                            ParcelFileDescriptor.MODE_READ_ONLY);
                newState = ParcelFileDescriptor.open(newStateName,
                            ParcelFileDescriptor.MODE_READ_WRITE |
                            ParcelFileDescriptor.MODE_CREATE |
                            ParcelFileDescriptor.MODE_TRUNCATE);
                prepareOperationTimeout(token, TIMEOUT_RESTORE_INTERVAL);
                agent.doRestore(backupData, appVersionCode, newState, token, mBackupManagerBinder);
                boolean success = waitUntilOperationComplete(token);
                if (!success) {
                    throw new RuntimeException("restore timeout");
                }
                newStateName.delete();                      
                int size = (int) backupDataName.length();
                EventLog.writeEvent(EventLogTags.RESTORE_PACKAGE, packageName, size);
            } catch (Exception e) {
                Slog.e(TAG, "Error restoring data for " + packageName, e);
                EventLog.writeEvent(EventLogTags.RESTORE_AGENT_FAILURE, packageName, e.toString());
                clearApplicationDataSynchronous(packageName);
            } finally {
                backupDataName.delete();
                try { if (backupData != null) backupData.close(); } catch (IOException e) {}
                try { if (newState != null) newState.close(); } catch (IOException e) {}
                backupData = newState = null;
                mCurrentOperations.delete(token);
            }
        }
    }
    class PerformClearTask implements Runnable {
        IBackupTransport mTransport;
        PackageInfo mPackage;
        PerformClearTask(IBackupTransport transport, PackageInfo packageInfo) {
            mTransport = transport;
            mPackage = packageInfo;
        }
        public void run() {
            try {
                File stateDir = new File(mBaseStateDir, mTransport.transportDirName());
                File stateFile = new File(stateDir, mPackage.packageName);
                stateFile.delete();
                mTransport.clearBackupData(mPackage);
            } catch (RemoteException e) {
            } finally {
                try {
                    mTransport.finishBackup();
                } catch (RemoteException e) {
                }
                mWakelock.release();
            }
        }
    }
    class PerformInitializeTask implements Runnable {
        HashSet<String> mQueue;
        PerformInitializeTask(HashSet<String> transportNames) {
            mQueue = transportNames;
        }
        public void run() {
            try {
                for (String transportName : mQueue) {
                    IBackupTransport transport = getTransport(transportName);
                    if (transport == null) {
                        Slog.e(TAG, "Requested init for " + transportName + " but not found");
                        continue;
                    }
                    Slog.i(TAG, "Initializing (wiping) backup transport storage: " + transportName);
                    EventLog.writeEvent(EventLogTags.BACKUP_START, transport.transportDirName());
                    long startRealtime = SystemClock.elapsedRealtime();
                    int status = transport.initializeDevice();
                    if (status == BackupConstants.TRANSPORT_OK) {
                        status = transport.finishBackup();
                    }
                    if (status == BackupConstants.TRANSPORT_OK) {
                        Slog.i(TAG, "Device init successful");
                        int millis = (int) (SystemClock.elapsedRealtime() - startRealtime);
                        EventLog.writeEvent(EventLogTags.BACKUP_INITIALIZE);
                        resetBackupState(new File(mBaseStateDir, transport.transportDirName()));
                        EventLog.writeEvent(EventLogTags.BACKUP_SUCCESS, 0, millis);
                        synchronized (mQueueLock) {
                            recordInitPendingLocked(false, transportName);
                        }
                    } else {
                        Slog.e(TAG, "Transport error in initializeDevice()");
                        EventLog.writeEvent(EventLogTags.BACKUP_TRANSPORT_FAILURE, "(initialize)");
                        synchronized (mQueueLock) {
                            recordInitPendingLocked(true, transportName);
                        }
                        long delay = transport.requestBackupTime();
                        if (DEBUG) Slog.w(TAG, "init failed on "
                                + transportName + " resched in " + delay);
                        mAlarmManager.set(AlarmManager.RTC_WAKEUP,
                                System.currentTimeMillis() + delay, mRunInitIntent);
                    }
                }
            } catch (RemoteException e) {
            } catch (Exception e) {
                Slog.e(TAG, "Unexpected error performing init", e);
            } finally {
                mWakelock.release();
            }
        }
    }
    public void dataChanged(String packageName) {
        EventLog.writeEvent(EventLogTags.BACKUP_DATA_CHANGED, packageName);
        HashSet<ApplicationInfo> targets;
        if ((mContext.checkPermission(android.Manifest.permission.BACKUP, Binder.getCallingPid(),
                Binder.getCallingUid())) == PackageManager.PERMISSION_DENIED) {
            targets = mBackupParticipants.get(Binder.getCallingUid());
        } else {
            targets = new HashSet<ApplicationInfo>();
            int N = mBackupParticipants.size();
            for (int i = 0; i < N; i++) {
                HashSet<ApplicationInfo> s = mBackupParticipants.valueAt(i);
                if (s != null) {
                    targets.addAll(s);
                }
            }
        }
        if (targets != null) {
            synchronized (mQueueLock) {
                for (ApplicationInfo app : targets) {
                    if (app.packageName.equals(packageName)) {
                        BackupRequest req = new BackupRequest(app, false);
                        if (mPendingBackups.put(app, req) == null) {
                            writeToJournalLocked(packageName);
                            if (DEBUG) {
                                int numKeys = mPendingBackups.size();
                                Slog.d(TAG, "Now awaiting backup for " + numKeys + " participants:");
                                for (BackupRequest b : mPendingBackups.values()) {
                                    Slog.d(TAG, "    + " + b + " agent=" + b.appInfo.backupAgentName);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Slog.w(TAG, "dataChanged but no participant pkg='" + packageName + "'"
                    + " uid=" + Binder.getCallingUid());
        }
    }
    private void writeToJournalLocked(String str) {
        RandomAccessFile out = null;
        try {
            if (mJournal == null) mJournal = File.createTempFile("journal", null, mJournalDir);
            out = new RandomAccessFile(mJournal, "rws");
            out.seek(out.length());
            out.writeUTF(str);
        } catch (IOException e) {
            Slog.e(TAG, "Can't write " + str + " to backup journal", e);
            mJournal = null;
        } finally {
            try { if (out != null) out.close(); } catch (IOException e) {}
        }
    }
    public void clearBackupData(String packageName) {
        if (DEBUG) Slog.v(TAG, "clearBackupData() of " + packageName);
        PackageInfo info;
        try {
            info = mPackageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
        } catch (NameNotFoundException e) {
            Slog.d(TAG, "No such package '" + packageName + "' - not clearing backup data");
            return;
        }
        HashSet<ApplicationInfo> apps;
        if ((mContext.checkPermission(android.Manifest.permission.BACKUP, Binder.getCallingPid(),
                Binder.getCallingUid())) == PackageManager.PERMISSION_DENIED) {
            apps = mBackupParticipants.get(Binder.getCallingUid());
        } else {
            if (DEBUG) Slog.v(TAG, "Privileged caller, allowing clear of other apps");
            apps = new HashSet<ApplicationInfo>();
            int N = mBackupParticipants.size();
            for (int i = 0; i < N; i++) {
                HashSet<ApplicationInfo> s = mBackupParticipants.valueAt(i);
                if (s != null) {
                    apps.addAll(s);
                }
            }
        }
        for (ApplicationInfo app : apps) {
            if (app.packageName.equals(packageName)) {
                if (DEBUG) Slog.v(TAG, "Found the app - running clear process");
                synchronized (mQueueLock) {
                    long oldId = Binder.clearCallingIdentity();
                    mWakelock.acquire();
                    Message msg = mBackupHandler.obtainMessage(MSG_RUN_CLEAR,
                            new ClearParams(getTransport(mCurrentTransport), info));
                    mBackupHandler.sendMessage(msg);
                    Binder.restoreCallingIdentity(oldId);
                }
                break;
            }
        }
    }
    public void backupNow() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP, "backupNow");
        if (DEBUG) Slog.v(TAG, "Scheduling immediate backup pass");
        synchronized (mQueueLock) {
            startBackupAlarmsLocked(BACKUP_INTERVAL);
            try {
                mRunBackupIntent.send();
            } catch (PendingIntent.CanceledException e) {
                Slog.e(TAG, "run-backup intent cancelled!");
            }
        }
    }
    public void setBackupEnabled(boolean enable) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                "setBackupEnabled");
        Slog.i(TAG, "Backup enabled => " + enable);
        boolean wasEnabled = mEnabled;
        synchronized (this) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.BACKUP_ENABLED, enable ? 1 : 0);
            mEnabled = enable;
        }
        synchronized (mQueueLock) {
            if (enable && !wasEnabled && mProvisioned) {
                startBackupAlarmsLocked(BACKUP_INTERVAL);
            } else if (!enable) {
                if (DEBUG) Slog.i(TAG, "Opting out of backup");
                mAlarmManager.cancel(mRunBackupIntent);
                if (wasEnabled && mProvisioned) {
                    HashSet<String> allTransports;
                    synchronized (mTransports) {
                        allTransports = new HashSet<String>(mTransports.keySet());
                    }
                    for (String transport : allTransports) {
                        recordInitPendingLocked(true, transport);
                    }
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                            mRunInitIntent);
                }
            }
        }
    }
    public void setAutoRestore(boolean doAutoRestore) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
        "setBackupEnabled");
        Slog.i(TAG, "Auto restore => " + doAutoRestore);
        synchronized (this) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.BACKUP_AUTO_RESTORE, doAutoRestore ? 1 : 0);
            mAutoRestore = doAutoRestore;
        }
    }
    public void setBackupProvisioned(boolean available) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                "setBackupProvisioned");
        boolean wasProvisioned = mProvisioned;
        synchronized (this) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.BACKUP_PROVISIONED, available ? 1 : 0);
            mProvisioned = available;
        }
        synchronized (mQueueLock) {
            if (available && !wasProvisioned && mEnabled) {
                startBackupAlarmsLocked(FIRST_BACKUP_INTERVAL);
            } else if (!available) {
                Slog.w(TAG, "Backup service no longer provisioned");
                mAlarmManager.cancel(mRunBackupIntent);
            }
        }
    }
    private void startBackupAlarmsLocked(long delayBeforeFirstBackup) {
        Random random = new Random();
        long when = System.currentTimeMillis() + delayBeforeFirstBackup +
                random.nextInt(FUZZ_MILLIS);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, when,
                BACKUP_INTERVAL + random.nextInt(FUZZ_MILLIS), mRunBackupIntent);
        mNextBackupPass = when;
    }
    public boolean isBackupEnabled() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP, "isBackupEnabled");
        return mEnabled;    
    }
    public String getCurrentTransport() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                "getCurrentTransport");
        if (DEBUG) Slog.v(TAG, "... getCurrentTransport() returning " + mCurrentTransport);
        return mCurrentTransport;
    }
    public String[] listAllTransports() {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP, "listAllTransports");
        String[] list = null;
        ArrayList<String> known = new ArrayList<String>();
        for (Map.Entry<String, IBackupTransport> entry : mTransports.entrySet()) {
            if (entry.getValue() != null) {
                known.add(entry.getKey());
            }
        }
        if (known.size() > 0) {
            list = new String[known.size()];
            known.toArray(list);
        }
        return list;
    }
    public String selectBackupTransport(String transport) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP, "selectBackupTransport");
        synchronized (mTransports) {
            String prevTransport = null;
            if (mTransports.get(transport) != null) {
                prevTransport = mCurrentTransport;
                mCurrentTransport = transport;
                Settings.Secure.putString(mContext.getContentResolver(),
                        Settings.Secure.BACKUP_TRANSPORT, transport);
                Slog.v(TAG, "selectBackupTransport() set " + mCurrentTransport
                        + " returning " + prevTransport);
            } else {
                Slog.w(TAG, "Attempt to select unavailable transport " + transport);
            }
            return prevTransport;
        }
    }
    public void agentConnected(String packageName, IBinder agentBinder) {
        synchronized(mAgentConnectLock) {
            if (Binder.getCallingUid() == Process.SYSTEM_UID) {
                Slog.d(TAG, "agentConnected pkg=" + packageName + " agent=" + agentBinder);
                IBackupAgent agent = IBackupAgent.Stub.asInterface(agentBinder);
                mConnectedAgent = agent;
                mConnecting = false;
            } else {
                Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid()
                        + " claiming agent connected");
            }
            mAgentConnectLock.notifyAll();
        }
    }
    public void agentDisconnected(String packageName) {
        synchronized(mAgentConnectLock) {
            if (Binder.getCallingUid() == Process.SYSTEM_UID) {
                mConnectedAgent = null;
                mConnecting = false;
            } else {
                Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid()
                        + " claiming agent disconnected");
            }
            mAgentConnectLock.notifyAll();
        }
    }
    public void restoreAtInstall(String packageName, int token) {
        if (Binder.getCallingUid() != Process.SYSTEM_UID) {
            Slog.w(TAG, "Non-system process uid=" + Binder.getCallingUid()
                    + " attemping install-time restore");
            return;
        }
        long restoreSet = getAvailableRestoreToken(packageName);
        if (DEBUG) Slog.v(TAG, "restoreAtInstall pkg=" + packageName
                + " token=" + Integer.toHexString(token));
        if (mAutoRestore && mProvisioned && restoreSet != 0) {
            PackageInfo pkg = new PackageInfo();
            pkg.packageName = packageName;
            mWakelock.acquire();
            Message msg = mBackupHandler.obtainMessage(MSG_RUN_RESTORE);
            msg.obj = new RestoreParams(getTransport(mCurrentTransport), null,
                    restoreSet, pkg, token);
            mBackupHandler.sendMessage(msg);
        } else {
            if (DEBUG) Slog.v(TAG, "No restore set -- skipping restore");
            try {
                mPackageManagerBinder.finishPackageInstall(token);
            } catch (RemoteException e) {  }
        }
    }
    public IRestoreSession beginRestoreSession(String transport) {
        mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP, "beginRestoreSession");
        synchronized(this) {
            if (mActiveRestoreSession != null) {
                Slog.d(TAG, "Restore session requested but one already active");
                return null;
            }
            mActiveRestoreSession = new ActiveRestoreSession(transport);
        }
        return mActiveRestoreSession;
    }
    public void opComplete(int token) {
        synchronized (mCurrentOpLock) {
            if (DEBUG) Slog.v(TAG, "opComplete: " + Integer.toHexString(token));
            mCurrentOperations.put(token, OP_ACKNOWLEDGED);
            mCurrentOpLock.notifyAll();
        }
    }
    class ActiveRestoreSession extends IRestoreSession.Stub {
        private static final String TAG = "RestoreSession";
        private IBackupTransport mRestoreTransport = null;
        RestoreSet[] mRestoreSets = null;
        ActiveRestoreSession(String transport) {
            mRestoreTransport = getTransport(transport);
        }
        public synchronized int getAvailableRestoreSets(IRestoreObserver observer) {
            mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                    "getAvailableRestoreSets");
            if (observer == null) {
                throw new IllegalArgumentException("Observer must not be null");
            }
            long oldId = Binder.clearCallingIdentity();
            try {
                if (mRestoreTransport == null) {
                    Slog.w(TAG, "Null transport getting restore sets");
                    return -1;
                }
                mWakelock.acquire();
                Message msg = mBackupHandler.obtainMessage(MSG_RUN_GET_RESTORE_SETS,
                        new RestoreGetSetsParams(mRestoreTransport, this, observer));
                mBackupHandler.sendMessage(msg);
                return 0;
            } catch (Exception e) {
                Slog.e(TAG, "Error in getAvailableRestoreSets", e);
                return -1;
            } finally {
                Binder.restoreCallingIdentity(oldId);
            }
        }
        public synchronized int restoreAll(long token, IRestoreObserver observer) {
            mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                    "performRestore");
            if (DEBUG) Slog.d(TAG, "performRestore token=" + Long.toHexString(token)
                    + " observer=" + observer);
            if (mRestoreTransport == null || mRestoreSets == null) {
                Slog.e(TAG, "Ignoring performRestore() with no restore set");
                return -1;
            }
            synchronized (mQueueLock) {
                for (int i = 0; i < mRestoreSets.length; i++) {
                    if (token == mRestoreSets[i].token) {
                        long oldId = Binder.clearCallingIdentity();
                        mWakelock.acquire();
                        Message msg = mBackupHandler.obtainMessage(MSG_RUN_RESTORE);
                        msg.obj = new RestoreParams(mRestoreTransport, observer, token);
                        mBackupHandler.sendMessage(msg);
                        Binder.restoreCallingIdentity(oldId);
                        return 0;
                    }
                }
            }
            Slog.w(TAG, "Restore token " + Long.toHexString(token) + " not found");
            return -1;
        }
        public synchronized int restorePackage(String packageName, IRestoreObserver observer) {
            if (DEBUG) Slog.v(TAG, "restorePackage pkg=" + packageName + " obs=" + observer);
            PackageInfo app = null;
            try {
                app = mPackageManager.getPackageInfo(packageName, 0);
            } catch (NameNotFoundException nnf) {
                Slog.w(TAG, "Asked to restore nonexistent pkg " + packageName);
                return -1;
            }
            int perm = mContext.checkPermission(android.Manifest.permission.BACKUP,
                    Binder.getCallingPid(), Binder.getCallingUid());
            if ((perm == PackageManager.PERMISSION_DENIED) &&
                    (app.applicationInfo.uid != Binder.getCallingUid())) {
                Slog.w(TAG, "restorePackage: bad packageName=" + packageName
                        + " or calling uid=" + Binder.getCallingUid());
                throw new SecurityException("No permission to restore other packages");
            }
            if (app.applicationInfo.backupAgentName == null) {
                Slog.w(TAG, "Asked to restore package " + packageName + " with no agent");
                return -1;
            }
            long token = getAvailableRestoreToken(packageName);
            if (token == 0) {
                return -1;
            }
            long oldId = Binder.clearCallingIdentity();
            mWakelock.acquire();
            Message msg = mBackupHandler.obtainMessage(MSG_RUN_RESTORE);
            msg.obj = new RestoreParams(mRestoreTransport, observer, token, app, 0);
            mBackupHandler.sendMessage(msg);
            Binder.restoreCallingIdentity(oldId);
            return 0;
        }
        public synchronized void endRestoreSession() {
            mContext.enforceCallingOrSelfPermission(android.Manifest.permission.BACKUP,
                    "endRestoreSession");
            if (DEBUG) Slog.d(TAG, "endRestoreSession");
            synchronized (this) {
                long oldId = Binder.clearCallingIdentity();
                try {
                    if (mRestoreTransport != null) mRestoreTransport.finishRestore();
                } catch (Exception e) {
                    Slog.e(TAG, "Error in finishRestore", e);
                } finally {
                    mRestoreTransport = null;
                    Binder.restoreCallingIdentity(oldId);
                }
            }
            synchronized (BackupManagerService.this) {
                if (BackupManagerService.this.mActiveRestoreSession == this) {
                    BackupManagerService.this.mActiveRestoreSession = null;
                } else {
                    Slog.e(TAG, "ending non-current restore session");
                }
            }
        }
    }
    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (mQueueLock) {
            pw.println("Backup Manager is " + (mEnabled ? "enabled" : "disabled")
                    + " / " + (!mProvisioned ? "not " : "") + "provisioned / "
                    + (this.mPendingInits.size() == 0 ? "not " : "") + "pending init");
            pw.println("Auto-restore is " + (mAutoRestore ? "enabled" : "disabled"));
            pw.println("Last backup pass: " + mLastBackupPass
                    + " (now = " + System.currentTimeMillis() + ')');
            pw.println("  next scheduled: " + mNextBackupPass);
            pw.println("Available transports:");
            for (String t : listAllTransports()) {
                pw.println((t.equals(mCurrentTransport) ? "  * " : "    ") + t);
                try {
                    File dir = new File(mBaseStateDir, getTransport(t).transportDirName());
                    for (File f : dir.listFiles()) {
                        pw.println("       " + f.getName() + " - " + f.length() + " state bytes");
                    }
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error in transportDirName()", e);
                    pw.println("        Error: " + e);
                }
            }
            pw.println("Pending init: " + mPendingInits.size());
            for (String s : mPendingInits) {
                pw.println("    " + s);
            }
            int N = mBackupParticipants.size();
            pw.println("Participants:");
            for (int i=0; i<N; i++) {
                int uid = mBackupParticipants.keyAt(i);
                pw.print("  uid: ");
                pw.println(uid);
                HashSet<ApplicationInfo> participants = mBackupParticipants.valueAt(i);
                for (ApplicationInfo app: participants) {
                    pw.println("    " + app.packageName);
                }
            }
            pw.println("Ancestral packages: "
                    + (mAncestralPackages == null ? "none" : mAncestralPackages.size()));
            if (mAncestralPackages != null) {
                for (String pkg : mAncestralPackages) {
                    pw.println("    " + pkg);
                }
            }
            pw.println("Ever backed up: " + mEverStoredApps.size());
            for (String pkg : mEverStoredApps) {
                pw.println("    " + pkg);
            }
            pw.println("Pending backup: " + mPendingBackups.size());
            for (BackupRequest req : mPendingBackups.values()) {
                pw.println("    " + req);
            }
        }
    }
}
