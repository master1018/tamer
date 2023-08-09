public class BluetoothPbapService extends Service {
    private static final String TAG = "BluetoothPbapService";
    public static final boolean DEBUG = false;
    public static final boolean VERBOSE = false;
    public static final String ACCESS_REQUEST_ACTION = "com.android.bluetooth.pbap.accessrequest";
    public static final String ACCESS_ALLOWED_ACTION = "com.android.bluetooth.pbap.accessallowed";
    public static final String ACCESS_DISALLOWED_ACTION =
            "com.android.bluetooth.pbap.accessdisallowed";
    public static final String AUTH_CHALL_ACTION = "com.android.bluetooth.pbap.authchall";
    public static final String AUTH_RESPONSE_ACTION = "com.android.bluetooth.pbap.authresponse";
    public static final String AUTH_CANCELLED_ACTION = "com.android.bluetooth.pbap.authcancelled";
    public static final String USER_CONFIRM_TIMEOUT_ACTION =
            "com.android.bluetooth.pbap.userconfirmtimeout";
    public static final String EXTRA_ALWAYS_ALLOWED = "com.android.bluetooth.pbap.alwaysallowed";
    public static final String EXTRA_SESSION_KEY = "com.android.bluetooth.pbap.sessionkey";
    public static final String THIS_PACKAGE_NAME = "com.android.bluetooth";
    public static final int MSG_SERVERSESSION_CLOSE = 5000;
    public static final int MSG_SESSION_ESTABLISHED = 5001;
    public static final int MSG_SESSION_DISCONNECTED = 5002;
    public static final int MSG_OBEX_AUTH_CHALL = 5003;
    private static final String BLUETOOTH_PERM = android.Manifest.permission.BLUETOOTH;
    private static final String BLUETOOTH_ADMIN_PERM = android.Manifest.permission.BLUETOOTH_ADMIN;
    private static final int START_LISTENER = 1;
    private static final int USER_TIMEOUT = 2;
    private static final int AUTH_TIMEOUT = 3;
    private static final int PORT_NUM = 19;
    private static final int USER_CONFIRM_TIMEOUT_VALUE = 30000;
    private static final int TIME_TO_WAIT_VALUE = 6000;
    private static final int NOTIFICATION_ID_ACCESS = -1000001;
    private static final int NOTIFICATION_ID_AUTH = -1000002;
    private PowerManager.WakeLock mWakeLock = null;
    private BluetoothAdapter mAdapter;
    private SocketAcceptThread mAcceptThread = null;
    private BluetoothPbapAuthenticator mAuth = null;
    private BluetoothPbapObexServer mPbapServer;
    private ServerSession mServerSession = null;
    private BluetoothServerSocket mServerSocket = null;
    private BluetoothSocket mConnSocket = null;
    private BluetoothDevice mRemoteDevice = null;
    private static String sLocalPhoneNum = null;
    private static String sLocalPhoneName = null;
    private static String sRemoteDeviceName = null;
    private boolean mHasStarted = false;
    private volatile boolean mInterrupted;
    private int mState;
    private int mStartId = -1;
    public BluetoothPbapService() {
        mState = BluetoothPbap.STATE_DISCONNECTED;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (VERBOSE) Log.v(TAG, "Pbap Service onCreate");
        mInterrupted = false;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mHasStarted) {
            mHasStarted = true;
            if (VERBOSE) Log.v(TAG, "Starting PBAP service");
            int state = mAdapter.getState();
            if (state == BluetoothAdapter.STATE_ON) {
                mSessionStatusHandler.sendMessageDelayed(mSessionStatusHandler
                        .obtainMessage(START_LISTENER), TIME_TO_WAIT_VALUE);
            }
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (VERBOSE) Log.v(TAG, "Pbap Service onStartCommand");
        int retCode = super.onStartCommand(intent, flags, startId);
        if (retCode == START_STICKY) {
            mStartId = startId;
            if (mAdapter == null) {
                Log.w(TAG, "Stopping BluetoothPbapService: "
                        + "device does not have BT or device is not ready");
                closeService();
            } else {
                if (intent != null) {
                    parseIntent(intent);
                }
            }
        }
        return retCode;
    }
    private void parseIntent(final Intent intent) {
        String action = intent.getStringExtra("action");
        if (VERBOSE) Log.v(TAG, "action: " + action);
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        boolean removeTimeoutMsg = true;
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            removeTimeoutMsg = false;
            if (state == BluetoothAdapter.STATE_OFF) {
                closeService();
            }
        } else if (action.equals(ACCESS_ALLOWED_ACTION)) {
            if (intent.getBooleanExtra(EXTRA_ALWAYS_ALLOWED, false)) {
                boolean result = mRemoteDevice.setTrust(true);
                if (VERBOSE) Log.v(TAG, "setTrust() result=" + result);
            }
            try {
                if (mConnSocket != null) {
                    startObexServerSession();
                } else {
                    stopObexServerSession();
                }
            } catch (IOException ex) {
                Log.e(TAG, "Caught the error: " + ex.toString());
            }
        } else if (action.equals(ACCESS_DISALLOWED_ACTION)) {
            stopObexServerSession();
        } else if (action.equals(AUTH_RESPONSE_ACTION)) {
            String sessionkey = intent.getStringExtra(EXTRA_SESSION_KEY);
            notifyAuthKeyInput(sessionkey);
        } else if (action.equals(AUTH_CANCELLED_ACTION)) {
            notifyAuthCancelled();
        } else {
            removeTimeoutMsg = false;
        }
        if (removeTimeoutMsg) {
            mSessionStatusHandler.removeMessages(USER_TIMEOUT);
        }
    }
    @Override
    public void onDestroy() {
        if (VERBOSE) Log.v(TAG, "Pbap Service onDestroy");
        super.onDestroy();
        setState(BluetoothPbap.STATE_DISCONNECTED, BluetoothPbap.RESULT_CANCELED);
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        closeService();
    }
    @Override
    public IBinder onBind(Intent intent) {
        if (VERBOSE) Log.v(TAG, "Pbap Service onBind");
        return mBinder;
    }
    private void startRfcommSocketListener() {
        if (VERBOSE) Log.v(TAG, "Pbap Service startRfcommSocketListener");
        if (mServerSocket == null) {
            if (!initSocket()) {
                closeService();
                return;
            }
        }
        if (mAcceptThread == null) {
            mAcceptThread = new SocketAcceptThread();
            mAcceptThread.setName("BluetoothPbapAcceptThread");
            mAcceptThread.start();
        }
    }
    private final boolean initSocket() {
        if (VERBOSE) Log.v(TAG, "Pbap Service initSocket");
        boolean initSocketOK = true;
        final int CREATE_RETRY_TIME = 10;
        for (int i = 0; i < CREATE_RETRY_TIME && !mInterrupted; i++) {
            try {
                mServerSocket = mAdapter.listenUsingRfcommOn(PORT_NUM);
            } catch (IOException e) {
                Log.e(TAG, "Error create RfcommServerSocket " + e.toString());
                initSocketOK = false;
            }
            if (!initSocketOK) {
                synchronized (this) {
                    try {
                        if (VERBOSE) Log.v(TAG, "wait 3 seconds");
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "socketAcceptThread thread was interrupted (3)");
                        mInterrupted = true;
                    }
                }
            } else {
                break;
            }
        }
        if (initSocketOK) {
            if (VERBOSE) Log.v(TAG, "Succeed to create listening socket on channel " + PORT_NUM);
        } else {
            Log.e(TAG, "Error to create listening socket after " + CREATE_RETRY_TIME + " try");
        }
        return initSocketOK;
    }
    private final void closeSocket(boolean server, boolean accept) throws IOException {
        if (server == true) {
            mInterrupted = true;
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        }
        if (accept == true) {
            if (mConnSocket != null) {
                mConnSocket.close();
            }
        }
    }
    private final void closeService() {
        if (VERBOSE) Log.v(TAG, "Pbap Service closeService");
        try {
            closeSocket(true, true);
        } catch (IOException ex) {
            Log.e(TAG, "CloseSocket error: " + ex);
        }
        if (mAcceptThread != null) {
            try {
                mAcceptThread.shutdown();
                mAcceptThread.join();
                mAcceptThread = null;
            } catch (InterruptedException ex) {
                Log.w(TAG, "mAcceptThread close error" + ex);
            }
        }
        mServerSocket = null;
        mConnSocket = null;
        if (mServerSession != null) {
            mServerSession.close();
            mServerSession = null;
        }
        mHasStarted = false;
        if (stopSelfResult(mStartId)) {
            if (VERBOSE) Log.v(TAG, "successfully stopped pbap service");
        }
    }
    private final void startObexServerSession() throws IOException {
        if (VERBOSE) Log.v(TAG, "Pbap Service startObexServerSession");
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "StartingObexPbapTransaction");
            mWakeLock.setReferenceCounted(false);
            mWakeLock.acquire();
        }
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            sLocalPhoneNum = tm.getLine1Number();
            sLocalPhoneName = tm.getLine1AlphaTag();
            if (TextUtils.isEmpty(sLocalPhoneName)) {
                sLocalPhoneName = this.getString(R.string.localPhoneName);
            }
        }
        mPbapServer = new BluetoothPbapObexServer(mSessionStatusHandler, this);
        synchronized (this) {
            mAuth = new BluetoothPbapAuthenticator(mSessionStatusHandler);
            mAuth.setChallenged(false);
            mAuth.setCancelled(false);
        }
        BluetoothPbapRfcommTransport transport = new BluetoothPbapRfcommTransport(mConnSocket);
        mServerSession = new ServerSession(transport, mPbapServer, mAuth);
        setState(BluetoothPbap.STATE_CONNECTED);
        if (VERBOSE) {
            Log.v(TAG, "startObexServerSession() success!");
        }
    }
    private void stopObexServerSession() {
        if (VERBOSE) Log.v(TAG, "Pbap Service stopObexServerSession");
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        if (mServerSession != null) {
            mServerSession.close();
            mServerSession = null;
        }
        mAcceptThread = null;
        try {
            closeSocket(false, true);
            mConnSocket = null;
        } catch (IOException e) {
            Log.e(TAG, "closeSocket error: " + e.toString());
        }
        if (mAdapter.isEnabled()) {
            startRfcommSocketListener();
        }
        setState(BluetoothPbap.STATE_DISCONNECTED);
    }
    private void notifyAuthKeyInput(final String key) {
        synchronized (mAuth) {
            if (key != null) {
                mAuth.setSessionKey(key);
            }
            mAuth.setChallenged(true);
            mAuth.notify();
        }
    }
    private void notifyAuthCancelled() {
        synchronized (mAuth) {
            mAuth.setCancelled(true);
            mAuth.notify();
        }
    }
    private class SocketAcceptThread extends Thread {
        private boolean stopped = false;
        @Override
        public void run() {
            while (!stopped) {
                try {
                    mConnSocket = mServerSocket.accept();
                    mRemoteDevice = mConnSocket.getRemoteDevice();
                    if (mRemoteDevice == null) {
                        Log.i(TAG, "getRemoteDevice() = null");
                        break;
                    }
                    sRemoteDeviceName = mRemoteDevice.getName();
                    if (TextUtils.isEmpty(sRemoteDeviceName)) {
                        sRemoteDeviceName = getString(R.string.defaultname);
                    }
                    boolean trust = mRemoteDevice.getTrustState();
                    if (VERBOSE) Log.v(TAG, "GetTrustState() = " + trust);
                    if (trust) {
                        try {
                            if (VERBOSE) Log.v(TAG, "incomming connection accepted from: "
                                + sRemoteDeviceName + " automatically as trusted device");
                            startObexServerSession();
                        } catch (IOException ex) {
                            Log.e(TAG, "catch exception starting obex server session"
                                    + ex.toString());
                        }
                    } else {
                        createPbapNotification(ACCESS_REQUEST_ACTION);
                        if (VERBOSE) Log.v(TAG, "incomming connection accepted from: "
                                + sRemoteDeviceName);
                        mSessionStatusHandler.sendMessageDelayed(mSessionStatusHandler
                                .obtainMessage(USER_TIMEOUT), USER_CONFIRM_TIMEOUT_VALUE);
                    }
                    stopped = true; 
                } catch (IOException ex) {
                    if (stopped) {
                        break;
                    }
                    if (VERBOSE) Log.v(TAG, "Accept exception: " + ex.toString());
                }
            }
        }
        void shutdown() {
            stopped = true;
            interrupt();
        }
    }
    private final Handler mSessionStatusHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (VERBOSE) Log.v(TAG, "Handler(): got msg=" + msg.what);
            CharSequence tmpTxt;
            switch (msg.what) {
                case START_LISTENER:
                    if (mAdapter.isEnabled()) {
                        startRfcommSocketListener();
                    } else {
                        closeService();
                    }
                    break;
                case USER_TIMEOUT:
                    Intent intent = new Intent(USER_CONFIRM_TIMEOUT_ACTION);
                    sendBroadcast(intent);
                    removePbapNotification(NOTIFICATION_ID_ACCESS);
                    stopObexServerSession();
                    break;
                case AUTH_TIMEOUT:
                    Intent i = new Intent(USER_CONFIRM_TIMEOUT_ACTION);
                    sendBroadcast(i);
                    removePbapNotification(NOTIFICATION_ID_AUTH);
                    notifyAuthCancelled();
                    break;
                case MSG_SERVERSESSION_CLOSE:
                    stopObexServerSession();
                    break;
                case MSG_SESSION_ESTABLISHED:
                    break;
                case MSG_SESSION_DISCONNECTED:
                    break;
                case MSG_OBEX_AUTH_CHALL:
                    createPbapNotification(AUTH_CHALL_ACTION);
                    mSessionStatusHandler.sendMessageDelayed(mSessionStatusHandler
                            .obtainMessage(AUTH_TIMEOUT), USER_CONFIRM_TIMEOUT_VALUE);
                    break;
                default:
                    break;
            }
        }
    };
    private void setState(int state) {
        setState(state, BluetoothPbap.RESULT_SUCCESS);
    }
    private synchronized void setState(int state, int result) {
        if (state != mState) {
            if (DEBUG) Log.d(TAG, "Pbap state " + mState + " -> " + state + ", result = "
                    + result);
            Intent intent = new Intent(BluetoothPbap.PBAP_STATE_CHANGED_ACTION);
            intent.putExtra(BluetoothPbap.PBAP_PREVIOUS_STATE, mState);
            mState = state;
            intent.putExtra(BluetoothPbap.PBAP_STATE, mState);
            intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mRemoteDevice);
            sendBroadcast(intent, BLUETOOTH_PERM);
        }
    }
    private void createPbapNotification(String action) {
        NotificationManager nm = (NotificationManager)
            getSystemService(Context.NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent();
        clickIntent.setClass(this, BluetoothPbapActivity.class);
        clickIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickIntent.setAction(action);
        Intent deleteIntent = new Intent();
        deleteIntent.setClass(this, BluetoothPbapReceiver.class);
        Notification notification = null;
        String name = getRemoteDeviceName();
        if (action.equals(ACCESS_REQUEST_ACTION)) {
            deleteIntent.setAction(ACCESS_DISALLOWED_ACTION);
            notification = new Notification(android.R.drawable.stat_sys_data_bluetooth,
                getString(R.string.pbap_notif_ticker), System.currentTimeMillis());
            notification.setLatestEventInfo(this, getString(R.string.pbap_notif_title),
                    getString(R.string.pbap_notif_message, name), PendingIntent
                            .getActivity(this, 0, clickIntent, 0));
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            notification.defaults = Notification.DEFAULT_SOUND;
            notification.deleteIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, 0);
            nm.notify(NOTIFICATION_ID_ACCESS, notification);
        } else if (action.equals(AUTH_CHALL_ACTION)) {
            deleteIntent.setAction(AUTH_CANCELLED_ACTION);
            notification = new Notification(android.R.drawable.stat_sys_data_bluetooth,
                getString(R.string.auth_notif_ticker), System.currentTimeMillis());
            notification.setLatestEventInfo(this, getString(R.string.auth_notif_title),
                    getString(R.string.auth_notif_message, name), PendingIntent
                            .getActivity(this, 0, clickIntent, 0));
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            notification.defaults = Notification.DEFAULT_SOUND;
            notification.deleteIntent = PendingIntent.getBroadcast(this, 0, deleteIntent, 0);
            nm.notify(NOTIFICATION_ID_AUTH, notification);
        }
    }
    private void removePbapNotification(int id) {
        NotificationManager nm = (NotificationManager)
            getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);
    }
    public static String getLocalPhoneNum() {
        return sLocalPhoneNum;
    }
    public static String getLocalPhoneName() {
        return sLocalPhoneName;
    }
    public static String getRemoteDeviceName() {
        return sRemoteDeviceName;
    }
    private final IBluetoothPbap.Stub mBinder = new IBluetoothPbap.Stub() {
        public int getState() {
            if (DEBUG) Log.d(TAG, "getState " + mState);
            enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            return mState;
        }
        public BluetoothDevice getClient() {
            if (DEBUG) Log.d(TAG, "getClient" + mRemoteDevice);
            enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            if (mState == BluetoothPbap.STATE_DISCONNECTED) {
                return null;
            }
            return mRemoteDevice;
        }
        public boolean isConnected(BluetoothDevice device) {
            enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            return mState == BluetoothPbap.STATE_CONNECTED && mRemoteDevice.equals(device);
        }
        public boolean connect(BluetoothDevice device) {
            enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                    "Need BLUETOOTH_ADMIN permission");
            return false;
        }
        public void disconnect() {
            if (DEBUG) Log.d(TAG, "disconnect");
            enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM,
                    "Need BLUETOOTH_ADMIN permission");
            synchronized (BluetoothPbapService.this) {
                switch (mState) {
                    case BluetoothPbap.STATE_CONNECTED:
                        if (mServerSession != null) {
                            mServerSession.close();
                            mServerSession = null;
                        }
                        try {
                            closeSocket(false, true);
                            mConnSocket = null;
                        } catch (IOException ex) {
                            Log.e(TAG, "Caught the error: " + ex);
                        }
                        setState(BluetoothPbap.STATE_DISCONNECTED, BluetoothPbap.RESULT_CANCELED);
                        break;
                    default:
                        break;
                }
            }
        }
    };
}
