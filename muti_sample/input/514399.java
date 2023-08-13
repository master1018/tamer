public class RemoteImService extends Service {
    private static final String[] ACCOUNT_PROJECTION = {
        Imps.Account._ID,
        Imps.Account.PROVIDER,
        Imps.Account.USERNAME,
        Imps.Account.PASSWORD,
    };
    private static final int ACCOUNT_ID_COLUMN = 0;
    private static final int ACCOUNT_PROVIDER_COLUMN = 1;
    private static final int ACCOUNT_USERNAME_COLUMN = 2;
    private static final int ACCOUNT_PASSOWRD_COLUMN = 3;
    static final String TAG = "ImService";
    private static final int EVENT_SHOW_TOAST = 100;
    private static final int EVENT_NETWORK_STATE_CHANGED = 200;
    private StatusBarNotifier mStatusBarNotifier;
    private Handler mServiceHandler;
    NetworkConnectivityListener mNetworkConnectivityListener;
    private int mNetworkType;
    private boolean mNeedCheckAutoLogin;
    private boolean mBackgroundDataEnabled;
    private SettingsMonitor mSettingsMonitor;
    private ImPluginHelper mPluginHelper;
    Vector<ImConnectionAdapter> mConnections;
    final RemoteCallbackList<IConnectionCreationListener> mRemoteListeners
            = new RemoteCallbackList<IConnectionCreationListener>();
    public RemoteImService() {
        mConnections = new Vector<ImConnectionAdapter>();
    }
    @Override
    public void onCreate() {
        Log.d(TAG, "ImService started");
        mStatusBarNotifier = new StatusBarNotifier(this);
        mServiceHandler = new ServiceHandler();
        mNetworkConnectivityListener = new NetworkConnectivityListener();
        mNetworkConnectivityListener.registerHandler(mServiceHandler, EVENT_NETWORK_STATE_CHANGED);
        mNetworkConnectivityListener.startListening(this);
        mSettingsMonitor = new SettingsMonitor();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED);
        registerReceiver(mSettingsMonitor, intentFilter);
        ConnectivityManager manager
            = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        setBackgroundData(manager.getBackgroundDataSetting());
        mPluginHelper = ImPluginHelper.getInstance(this);
        mPluginHelper.loadAvaiablePlugins();
        AndroidSystemService.getInstance().initialize(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mNeedCheckAutoLogin = intent.getBooleanExtra(ImServiceConstants.EXTRA_CHECK_AUTO_LOGIN, false);
            Log.d(TAG, "ImService.onStart, checkAutoLogin=" + mNeedCheckAutoLogin);
            if (mNeedCheckAutoLogin &&
                    mNetworkConnectivityListener.getState() == State.CONNECTED) {
                mNeedCheckAutoLogin = false;
                autoLogin();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void autoLogin() {
        Log.d(TAG, "Scaning accounts and login automatically");
        ContentResolver resolver = getContentResolver();
        String where = Imps.Account.KEEP_SIGNED_IN + "=1 AND " + Imps.Account.ACTIVE + "=1";
        Cursor cursor = resolver.query(Imps.Account.CONTENT_URI,
                ACCOUNT_PROJECTION, where, null, null);
        if (cursor == null) {
            Log.w(TAG, "Can't query account!");
            return;
        }
        while (cursor.moveToNext()) {
            long accountId = cursor.getLong(ACCOUNT_ID_COLUMN);
            long providerId = cursor.getLong(ACCOUNT_PROVIDER_COLUMN);
            String username = cursor.getString(ACCOUNT_USERNAME_COLUMN);
            String password = cursor.getString(ACCOUNT_PASSOWRD_COLUMN);
            IImConnection conn = createConnection(providerId);
            try {
                conn.login(accountId, username, password, true);
            } catch (RemoteException e) {
                Log.w(TAG, "Logging error while automatically login!");
            }
        }
        cursor.close();
    }
    private Map<String, String> loadProviderSettings(long providerId) {
        ContentResolver cr = getContentResolver();
        Map<String, String> settings = Imps.ProviderSettings.queryProviderSettings(cr, providerId);
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        if ("1".equals(SystemProperties.get("ro.kernel.qemu"))) {
            settings.put(ImpsConfigNames.MSISDN, "15555218135");
        } else if (networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            if (!TextUtils.isEmpty(settings.get(ImpsConfigNames.SMS_ADDR))) {
                settings.put(ImpsConfigNames.SMS_AUTH, "true");
                settings.put(ImpsConfigNames.SECURE_LOGIN, "false");
            } else {
                String msisdn = TelephonyManager.getDefault().getLine1Number();
                if (TextUtils.isEmpty(msisdn)) {
                    Log.w(TAG, "Can not read MSISDN from SIM, use a fake one."
                         + " SMS related feature won't work.");
                    msisdn = "15555218135";
                }
                settings.put(ImpsConfigNames.MSISDN, msisdn);
            }
        }
        return settings;
    }
    @Override
    public void onDestroy() {
        Log.w(TAG, "ImService stopped.");
        for (ImConnectionAdapter conn : mConnections) {
            conn.logout();
        }
        AndroidSystemService.getInstance().shutdown();
        mNetworkConnectivityListener.unregisterHandler(mServiceHandler);
        mNetworkConnectivityListener.stopListening();
        mNetworkConnectivityListener = null;
        unregisterReceiver(mSettingsMonitor);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public void showToast(CharSequence text, int duration) {
        Message msg = Message.obtain(mServiceHandler, EVENT_SHOW_TOAST, duration, 0, text);
        msg.sendToTarget();
    }
    public StatusBarNotifier getStatusBarNotifier() {
        return mStatusBarNotifier;
    }
    public void scheduleReconnect(long delay) {
        if (!isNetworkAvailable()) {
            return;
        }
        mServiceHandler.postDelayed(new Runnable() {
            public void run() {
                reestablishConnections();
            }
        }, delay);
    }
    IImConnection createConnection(long providerId) {
        Map<String, String> settings = loadProviderSettings(providerId);
        String protocol = settings.get(ImConfigNames.PROTOCOL_NAME);
        if(!"IMPS".equals(protocol)) {
            Log.e(TAG, "Unsupported protocol: " + protocol);
            return null;
        }
        ImpsConnectionConfig config = new ImpsConnectionConfig(settings);
        ConnectionFactory factory = ConnectionFactory.getInstance();
        try {
            ImConnection conn = factory.createConnection(config);
            ImConnectionAdapter result = new ImConnectionAdapter(providerId,
                    conn, this);
            mConnections.add(result);
            final int N = mRemoteListeners.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IConnectionCreationListener listener = mRemoteListeners.getBroadcastItem(i);
                try {
                    listener.onConnectionCreated(result);
                } catch (RemoteException e) {
                }
            }
            mRemoteListeners.finishBroadcast();
            return result;
        } catch (ImException e) {
            Log.e(TAG, "Error creating connection", e);
            return null;
        }
    }
    void removeConnection(IImConnection connection) {
        mConnections.remove(connection);
    }
    private boolean isNetworkAvailable() {
        return mNetworkConnectivityListener.getState() == State.CONNECTED;
    }
    private boolean isBackgroundDataEnabled() {
        return mBackgroundDataEnabled;
    }
    private void setBackgroundData(boolean flag) {
        mBackgroundDataEnabled = flag;
    }
    void handleBackgroundDataSettingChange(){
        if (!isBackgroundDataEnabled()) {
            for (ImConnectionAdapter conn : mConnections) {
                conn.logout();
            }
        }
    }
    void networkStateChanged() {
        if (mNetworkConnectivityListener == null) {
            return;
        }
        NetworkInfo networkInfo = mNetworkConnectivityListener.getNetworkInfo();
        NetworkInfo.State state = networkInfo.getState();
        Log.d(TAG, "networkStateChanged:" + state);
        int oldType = mNetworkType;
        mNetworkType = networkInfo.getType();
        if (mNetworkType != oldType
                && isNetworkAvailable()) {
            for (ImConnectionAdapter conn : mConnections) {
                conn.networkTypeChanged();
            }
        }
        switch (state) {
            case CONNECTED:
                if (mNeedCheckAutoLogin) {
                    mNeedCheckAutoLogin = false;
                    autoLogin();
                    break;
                }
                reestablishConnections();
                break;
            case DISCONNECTED:
                if (!isNetworkAvailable()) {
                    suspendConnections();
                }
                break;
        }
    }
    void reestablishConnections() {
        if (!isNetworkAvailable()) {
            return;
        }
        for (ImConnectionAdapter conn : mConnections) {
            int connState = conn.getState();
            if (connState == ImConnection.SUSPENDED) {
                conn.reestablishSession();
            }
        }
    }
    private void suspendConnections() {
        for (ImConnectionAdapter conn : mConnections) {
            if (conn.getState() != ImConnection.LOGGED_IN) {
                continue;
            }
            conn.suspend();
        }
    }
    private final IRemoteImService.Stub mBinder = new IRemoteImService.Stub() {
        public List<ImPluginInfo> getAllPlugins() {
            return new ArrayList<ImPluginInfo>(mPluginHelper.getPluginsInfo());
        }
        public void addConnectionCreatedListener(IConnectionCreationListener listener) {
            if (listener != null) {
                mRemoteListeners.register(listener);
            }
        }
        public void removeConnectionCreatedListener(IConnectionCreationListener listener) {
            if (listener != null) {
                mRemoteListeners.unregister(listener);
            }
        }
        public IImConnection createConnection(long providerId) {
            return RemoteImService.this.createConnection(providerId);
        }
        public List getActiveConnections() {
            ArrayList<IBinder> result = new ArrayList<IBinder>(mConnections.size());
            for(IImConnection conn : mConnections) {
                result.add(conn.asBinder());
            }
            return result;
        }
        public void dismissNotifications(long providerId) {
            mStatusBarNotifier.dismissNotifications(providerId);
        }
        public void dismissChatNotification(long providerId, String username) {
            mStatusBarNotifier.dismissChatNotification(providerId, username);
        }
    };
    private final class SettingsMonitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED.equals(action)) {
                ConnectivityManager manager =
                    (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                setBackgroundData(manager.getBackgroundDataSetting());
                handleBackgroundDataSettingChange();
            }
        }
    }
    private final class ServiceHandler extends Handler {
        public ServiceHandler() {
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_SHOW_TOAST:
                    Toast.makeText(RemoteImService.this,
                            (CharSequence) msg.obj, msg.arg1).show();
                    break;
                case EVENT_NETWORK_STATE_CHANGED:
                    networkStateChanged();
                    break;
                default:
            }
        }
    }
}
