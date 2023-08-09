public class WifiStateTracker extends NetworkStateTracker {
    private static final boolean LOCAL_LOGD = Config.LOGD || false;
    private static final String TAG = "WifiStateTracker";
    private static final int EVENTLOG_NETWORK_STATE_CHANGED = 50021;
    private static final int EVENTLOG_SUPPLICANT_STATE_CHANGED = 50022;
    private static final int EVENTLOG_DRIVER_STATE_CHANGED = 50023;
    private static final int EVENTLOG_INTERFACE_CONFIGURATION_STATE_CHANGED = 50024;
    private static final int EVENTLOG_SUPPLICANT_CONNECTION_STATE_CHANGED = 50025;
    private static final int EVENT_SUPPLICANT_CONNECTION             = 1;
    private static final int EVENT_SUPPLICANT_DISCONNECT             = 2;
    private static final int EVENT_SUPPLICANT_STATE_CHANGED          = 3;
    private static final int EVENT_NETWORK_STATE_CHANGED             = 4;
    private static final int EVENT_SCAN_RESULTS_AVAILABLE            = 5;
    private static final int EVENT_INTERFACE_CONFIGURATION_SUCCEEDED = 6;
    private static final int EVENT_INTERFACE_CONFIGURATION_FAILED    = 7;
    private static final int EVENT_POLL_INTERVAL                     = 8;
    private static final int EVENT_DHCP_START                        = 9;
    private static final int EVENT_DEFERRED_DISCONNECT               = 10;
    private static final int EVENT_DEFERRED_RECONNECT                = 11;
    private static final int EVENT_DRIVER_STATE_CHANGED              = 12;
    private static final int EVENT_PASSWORD_KEY_MAY_BE_INCORRECT     = 13;
    private static final int EVENT_MAYBE_START_SCAN_POST_DISCONNECT  = 14;
    private static final int DRIVER_STARTED                          = 0;
    private static final int DRIVER_STOPPED                          = 1;
    private static final int DRIVER_HUNG                             = 2;
    private static final int POLL_STATUS_INTERVAL_MSECS = 3000;
    private static final int MAX_SUPPLICANT_LOOP_ITERATIONS = 4;
    private static final int DISCONNECT_DELAY_MSECS = 5000;
    private static final int RECONNECT_DELAY_MSECS = 2000;
    private static final int KICKSTART_SCANNING_DELAY_MSECS = 15000;
    private static final int DEFAULT_MAX_DHCP_RETRIES = 9;
    private static final int DRIVER_POWER_MODE_AUTO = 0;
    private static final int DRIVER_POWER_MODE_ACTIVE = 1;
    private SupplicantState mSupplicantLoopState = SupplicantState.DISCONNECTED;
    private int mNumSupplicantLoopIterations = 0;
    private int mNumSupplicantStateChanges = 0;
    private boolean mPasswordKeyMayBeIncorrect = false;
    public static final int SUPPL_SCAN_HANDLING_NORMAL = 1;
    public static final int SUPPL_SCAN_HANDLING_LIST_ONLY = 2;
    private WifiMonitor mWifiMonitor;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mScanResults;
    private WifiManager mWM;
    private boolean mHaveIpAddress;
    private boolean mObtainingIpAddress;
    private boolean mTornDownByConnMgr;
    private boolean mDisconnectPending;
    private boolean mDisconnectExpected;
    private DhcpHandler mDhcpTarget;
    private DhcpInfo mDhcpInfo;
    private int mLastSignalLevel = -1;
    private String mLastBssid;
    private String mLastSsid;
    private int mLastNetworkId = -1;
    private boolean mUseStaticIp = false;
    private int mReconnectCount;
    private int mNumAllowedChannels = 0;
    private static final int ICON_NETWORKS_AVAILABLE =
            com.android.internal.R.drawable.stat_notify_wifi_in_range;
    private final long NOTIFICATION_REPEAT_DELAY_MS;
    private boolean mNotificationEnabled;
    private NotificationEnabledSettingObserver mNotificationEnabledSettingObserver;
    private long mNotificationRepeatTime;
    private Notification mNotification;
    private boolean mNotificationShown;
    private static final int NUM_SCANS_BEFORE_ACTUALLY_SCANNING = 3;
    private int mNumScansSinceNetworkStateChange;
    private SettingsObserver mSettingsObserver;
    private boolean mIsScanModeActive;
    private boolean mEnableRssiPolling;
    private final AtomicInteger mWifiState = new AtomicInteger(WIFI_STATE_UNKNOWN);
    private static final int RUN_STATE_STARTING = 1;
    private static final int RUN_STATE_RUNNING  = 2;
    private static final int RUN_STATE_STOPPING = 3;
    private static final int RUN_STATE_STOPPED  = 4;
    private static final String mRunStateNames[] = {
            "Starting",
            "Running",
            "Stopping",
            "Stopped"
    };
    private int mRunState;
    private final IBatteryStats mBatteryStats;
    private boolean mIsScanOnly;
    private BluetoothA2dp mBluetoothA2dp;
    private String mInterfaceName;
    private static String LS = System.getProperty("line.separator");
    private static String[] sDnsPropNames;
    private static class SupplicantStateChangeResult {
        SupplicantStateChangeResult(int networkId, String BSSID, SupplicantState state) {
            this.state = state;
            this.BSSID = BSSID;
            this.networkId = networkId;
        }
        int networkId;
        String BSSID;
        SupplicantState state;
    }
    private static class NetworkStateChangeResult {
        NetworkStateChangeResult(DetailedState state, String BSSID, int networkId) {
            this.state = state;
            this.BSSID = BSSID;
            this.networkId = networkId;
        }
        DetailedState state;
        String BSSID;
        int networkId;
    }
    public WifiStateTracker(Context context, Handler target) {
        super(context, target, ConnectivityManager.TYPE_WIFI, 0, "WIFI", "");
        mWifiInfo = new WifiInfo();
        mWifiMonitor = new WifiMonitor(this);
        mHaveIpAddress = false;
        mObtainingIpAddress = false;
        setTornDownByConnMgr(false);
        mDisconnectPending = false;
        mScanResults = new ArrayList<ScanResult>();
        mDhcpInfo = new DhcpInfo();
        mRunState = RUN_STATE_STARTING;
        NOTIFICATION_REPEAT_DELAY_MS = Settings.Secure.getInt(context.getContentResolver(), 
                Settings.Secure.WIFI_NETWORKS_AVAILABLE_REPEAT_DELAY, 900) * 1000l;
        mNotificationEnabledSettingObserver = new NotificationEnabledSettingObserver(new Handler());
        mNotificationEnabledSettingObserver.register();
        mSettingsObserver = new SettingsObserver(new Handler());
        mInterfaceName = SystemProperties.get("wifi.interface", "tiwlan0");
        sDnsPropNames = new String[] {
            "dhcp." + mInterfaceName + ".dns1",
            "dhcp." + mInterfaceName + ".dns2"
        };
        mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
    }
    private void setSupplicantState(SupplicantState state) {
        mWifiInfo.setSupplicantState(state);
        updateNetworkInfo();
        checkPollTimer();
    }
    public SupplicantState getSupplicantState() {
        return mWifiInfo.getSupplicantState();
    }
    private void setSupplicantState(String stateName) {
        mWifiInfo.setSupplicantState(stateName);
        updateNetworkInfo();
        checkPollTimer();
    }
    private void setTornDownByConnMgr(boolean flag) {
        mTornDownByConnMgr = flag;
        updateNetworkInfo();
    }
    public String[] getNameServers() {
        return getNameServerList(sDnsPropNames);
    }
    public String getInterfaceName() {
        return mInterfaceName;
    }
    public String getTcpBufferSizesPropName() {
        return "net.tcp.buffersize.wifi";
    }
    public void startMonitoring() {
        mWM = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
    }
    public void startEventLoop() {
        mWifiMonitor.startMonitoring();
    }
    public synchronized boolean isAvailable() {
        SupplicantState suppState = mWifiInfo.getSupplicantState();
        return suppState != SupplicantState.UNINITIALIZED &&
                suppState != SupplicantState.INACTIVE &&
                (mTornDownByConnMgr || !isDriverStopped());
    }
    public int getNetworkSubtype() {
        return 0;
    }
    private void updateNetworkInfo() {
        mNetworkInfo.setIsAvailable(isAvailable());
    }
    public boolean isConnectionCompleted() {
        return mWifiInfo.getSupplicantState() == SupplicantState.COMPLETED;
    }
    public boolean hasIpAddress() {
        return mHaveIpAddress;
    }
    void notifyPasswordKeyMayBeIncorrect() {
        sendEmptyMessage(EVENT_PASSWORD_KEY_MAY_BE_INCORRECT);
    }
    void notifySupplicantConnection() {
        sendEmptyMessage(EVENT_SUPPLICANT_CONNECTION);
    }
    void notifyStateChange(int networkId, String BSSID, SupplicantState newState) {
        Message msg = Message.obtain(
            this, EVENT_SUPPLICANT_STATE_CHANGED,
            new SupplicantStateChangeResult(networkId, BSSID, newState));
        msg.sendToTarget();
    }
    void notifyStateChange(DetailedState newState, String BSSID, int networkId) {
        Message msg = Message.obtain(
            this, EVENT_NETWORK_STATE_CHANGED,
            new NetworkStateChangeResult(newState, BSSID, networkId));
        msg.sendToTarget();
    }
    void notifyScanResultsAvailable() {
        setScanResultHandling(SUPPL_SCAN_HANDLING_NORMAL);
        sendEmptyMessage(EVENT_SCAN_RESULTS_AVAILABLE);
    }
    void notifySupplicantLost() {
        sendEmptyMessage(EVENT_SUPPLICANT_DISCONNECT);
    }
    void notifyDriverStopped() {
        mRunState = RUN_STATE_STOPPED;
        Message.obtain(this, EVENT_DRIVER_STATE_CHANGED, DRIVER_STOPPED, 0).sendToTarget();
    }
    void notifyDriverStarted() {
        Message.obtain(this, EVENT_DRIVER_STATE_CHANGED, DRIVER_STARTED, 0).sendToTarget();
    }
    void notifyDriverHung() {
        Message.obtain(this, EVENT_DRIVER_STATE_CHANGED, DRIVER_HUNG, 0).sendToTarget();
    }
    private synchronized void checkPollTimer() {
        if (mEnableRssiPolling &&
                mWifiInfo.getSupplicantState() == SupplicantState.COMPLETED &&
                !hasMessages(EVENT_POLL_INTERVAL)) {
            sendEmptyMessageDelayed(EVENT_POLL_INTERVAL, POLL_STATUS_INTERVAL_MSECS);
        }
    }
    public synchronized boolean isDriverStopped() {
        return mRunState == RUN_STATE_STOPPED || mRunState == RUN_STATE_STOPPING;
    }
    private void noteRunState() {
        try {
            if (mRunState == RUN_STATE_RUNNING) {
                mBatteryStats.noteWifiRunning();
            } else if (mRunState == RUN_STATE_STOPPED) {
                mBatteryStats.noteWifiStopped();
            }
        } catch (RemoteException ignore) {
        }
    }
    public synchronized void setScanOnlyMode(boolean scanOnlyMode) {
        if (mIsScanOnly != scanOnlyMode) {
            int scanType = (scanOnlyMode ?
                    SUPPL_SCAN_HANDLING_LIST_ONLY : SUPPL_SCAN_HANDLING_NORMAL);
            if (LOCAL_LOGD) Log.v(TAG, "Scan-only mode changing to " + scanOnlyMode + " scanType=" + scanType);
            if (setScanResultHandling(scanType)) {
                mIsScanOnly = scanOnlyMode;
                if (!isDriverStopped()) {
                    if (scanOnlyMode) {
                        disconnect();
                    } else {
                        reconnectCommand();
                    }
                }
            }
        }
    }
    private void checkIsBluetoothPlaying() {
        boolean isBluetoothPlaying = false;
        Set<BluetoothDevice> connected = mBluetoothA2dp.getConnectedSinks();
        for (BluetoothDevice device : connected) {
            if (mBluetoothA2dp.getSinkState(device) == BluetoothA2dp.STATE_PLAYING) {
                isBluetoothPlaying = true;
                break;
            }
        }
        setBluetoothScanMode(isBluetoothPlaying);
    }
    public void enableRssiPolling(boolean enable) {
        if (mEnableRssiPolling != enable) {
            mEnableRssiPolling = enable;
            checkPollTimer();
        }
    }
    @Override
    public void releaseWakeLock() {
    }
    private boolean isSupplicantLooping(SupplicantState newSupplicantState) {
        if (SupplicantState.ASSOCIATING.ordinal() <= newSupplicantState.ordinal()
            && newSupplicantState.ordinal() < SupplicantState.COMPLETED.ordinal()) {
            if (mSupplicantLoopState != newSupplicantState) {
                if (newSupplicantState.ordinal() < mSupplicantLoopState.ordinal()) {
                    ++mNumSupplicantLoopIterations;
                }
                mSupplicantLoopState = newSupplicantState;
            }
        } else if (newSupplicantState == SupplicantState.COMPLETED) {
            resetSupplicantLoopState();
        }
        return mNumSupplicantLoopIterations >= MAX_SUPPLICANT_LOOP_ITERATIONS;
    }
    private void resetSupplicantLoopState() {
        mNumSupplicantLoopIterations = 0;
    }
    @Override
    public void handleMessage(Message msg) {
        Intent intent;
        switch (msg.what) {
            case EVENT_SUPPLICANT_CONNECTION:
                mRunState = RUN_STATE_RUNNING;
                noteRunState();
                checkUseStaticIp();
                resetNotificationTimer();
                HandlerThread dhcpThread = new HandlerThread("DHCP Handler Thread");
                dhcpThread.start();
                mDhcpTarget = new DhcpHandler(dhcpThread.getLooper(), this);
                mIsScanModeActive = true;
                mTornDownByConnMgr = false;
                mLastBssid = null;
                mLastSsid = null;
                requestConnectionInfo();
                SupplicantState supplState = mWifiInfo.getSupplicantState();
                String macaddr = getMacAddress();
                if (macaddr != null) {
                    mWifiInfo.setMacAddress(macaddr);
                }
                if (LOCAL_LOGD) Log.v(TAG, "Connection to supplicant established, state=" +
                    supplState);
                EventLog.writeEvent(EVENTLOG_SUPPLICANT_CONNECTION_STATE_CHANGED, 1);
                if (supplState == SupplicantState.COMPLETED) {
                    mLastBssid = mWifiInfo.getBSSID();
                    mLastSsid = mWifiInfo.getSSID();
                    configureInterface();
                }
                if (ActivityManagerNative.isSystemReady()) {
                    intent = new Intent(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                    intent.putExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, true);
                    mContext.sendBroadcast(intent);
                }
                if (supplState == SupplicantState.COMPLETED && mHaveIpAddress) {
                    setDetailedState(DetailedState.CONNECTED);
                } else {
                    setDetailedState(WifiInfo.getDetailedStateOf(supplState));
                }
                mWM.initializeMulticastFiltering();
                if (mBluetoothA2dp == null) {
                    mBluetoothA2dp = new BluetoothA2dp(mContext);
                }
                checkIsBluetoothPlaying();
                setNumAllowedChannels();
                break;
            case EVENT_SUPPLICANT_DISCONNECT:
                mRunState = RUN_STATE_STOPPED;
                noteRunState();
                boolean died = mWifiState.get() != WIFI_STATE_DISABLED &&
                               mWifiState.get() != WIFI_STATE_DISABLING;
                if (died) {
                    if (LOCAL_LOGD) Log.v(TAG, "Supplicant died unexpectedly");
                } else {
                    if (LOCAL_LOGD) Log.v(TAG, "Connection to supplicant lost");
                }
                EventLog.writeEvent(EVENTLOG_SUPPLICANT_CONNECTION_STATE_CHANGED, died ? 2 : 0);
                closeSupplicantConnection();
                if (died) {
                    resetConnections(true);
                }
                if (mDhcpTarget != null) {
                    mDhcpTarget.getLooper().quit();
                    mDhcpTarget = null;
                }
                mContext.removeStickyBroadcast(new Intent(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                if (ActivityManagerNative.isSystemReady()) {
                    intent = new Intent(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                    intent.putExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                    mContext.sendBroadcast(intent);
                }
                setDetailedState(DetailedState.DISCONNECTED);
                setSupplicantState(SupplicantState.UNINITIALIZED);
                mHaveIpAddress = false;
                mObtainingIpAddress = false;
                if (died) {
                    mWM.setWifiEnabled(false);
                }
                break;
            case EVENT_MAYBE_START_SCAN_POST_DISCONNECT:
                if (mNumSupplicantStateChanges == msg.arg1) {
                    scan(false); 
                }
                break;
            case EVENT_SUPPLICANT_STATE_CHANGED:
                mNumSupplicantStateChanges++;
                SupplicantStateChangeResult supplicantStateResult =
                    (SupplicantStateChangeResult) msg.obj;
                SupplicantState newState = supplicantStateResult.state;
                SupplicantState currentState = mWifiInfo.getSupplicantState();
                int eventLogParam = (newState.ordinal() & 0x3f);
                EventLog.writeEvent(EVENTLOG_SUPPLICANT_STATE_CHANGED, eventLogParam);
                if (LOCAL_LOGD) Log.v(TAG, "Changing supplicant state: "
                                      + currentState +
                                      " ==> " + newState);
                int networkId = supplicantStateResult.networkId;
                if (supplicantStateResult.state == SupplicantState.ASSOCIATING) {
                    mLastBssid = supplicantStateResult.BSSID;
                }
                if (newState == SupplicantState.DISCONNECTED ||
                        newState == SupplicantState.INACTIVE) {
                    sendMessageDelayed(obtainMessage(EVENT_MAYBE_START_SCAN_POST_DISCONNECT,
                            mNumSupplicantStateChanges, 0), KICKSTART_SCANNING_DELAY_MSECS);
                }
                boolean failedToAuthenticate = false;
                if (newState == SupplicantState.DISCONNECTED) {
                    failedToAuthenticate = mPasswordKeyMayBeIncorrect;
                }
                mPasswordKeyMayBeIncorrect = false;
                boolean disabledNetwork = false;
                if (isSupplicantLooping(newState)) {
                    if (LOCAL_LOGD) {
                        Log.v(TAG,
                              "Stop WPA supplicant loop and disable network");
                    }
                    disabledNetwork = wifiManagerDisableNetwork(networkId);
                }
                if (disabledNetwork) {
                    resetSupplicantLoopState();
                } else if (newState != currentState ||
                        (newState == SupplicantState.DISCONNECTED && isDriverStopped())) {
                    setSupplicantState(newState);
                    if (newState == SupplicantState.DORMANT) {
                        DetailedState newDetailedState;
                        Message reconnectMsg = obtainMessage(EVENT_DEFERRED_RECONNECT, mLastBssid);
                        if (mIsScanOnly || mRunState == RUN_STATE_STOPPING) {
                            newDetailedState = DetailedState.IDLE;
                        } else {
                            newDetailedState = DetailedState.FAILED;
                        }
                        handleDisconnectedState(newDetailedState, true);
                        if (mRunState == RUN_STATE_RUNNING && !mIsScanOnly && networkId != -1) {
                            sendMessageDelayed(reconnectMsg, RECONNECT_DELAY_MSECS);
                        } else if (mRunState == RUN_STATE_STOPPING) {
                            stopDriver();
                        } else if (mRunState == RUN_STATE_STARTING && !mIsScanOnly) {
                            reconnectCommand();
                        }
                    } else if (newState == SupplicantState.DISCONNECTED) {
                        mHaveIpAddress = false;
                        if (isDriverStopped() || mDisconnectExpected) {
                            handleDisconnectedState(DetailedState.DISCONNECTED, true);
                        } else {
                            scheduleDisconnect();
                        }
                    } else if (newState != SupplicantState.COMPLETED && !mDisconnectPending) {
                        if (!(currentState == SupplicantState.COMPLETED &&
                               (newState == SupplicantState.ASSOCIATING ||
                                newState == SupplicantState.ASSOCIATED ||
                                newState == SupplicantState.FOUR_WAY_HANDSHAKE ||
                                newState == SupplicantState.GROUP_HANDSHAKE))) {
                            setDetailedState(WifiInfo.getDetailedStateOf(newState));
                        }
                    }
                    mDisconnectExpected = false;
                    intent = new Intent(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                    intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                            | Intent.FLAG_RECEIVER_REPLACE_PENDING);
                    intent.putExtra(WifiManager.EXTRA_NEW_STATE, (Parcelable)newState);
                    if (failedToAuthenticate) {
                        if (LOCAL_LOGD) Log.d(TAG, "Failed to authenticate, disabling network " + networkId);
                        wifiManagerDisableNetwork(networkId);
                        intent.putExtra(
                            WifiManager.EXTRA_SUPPLICANT_ERROR,
                            WifiManager.ERROR_AUTHENTICATING);
                    }
                    mContext.sendStickyBroadcast(intent);
                }
                break;
            case EVENT_NETWORK_STATE_CHANGED:
                NetworkStateChangeResult result =
                    (NetworkStateChangeResult) msg.obj;
                eventLogParam = (result.state.ordinal() & 0x3f);
                EventLog.writeEvent(EVENTLOG_NETWORK_STATE_CHANGED, eventLogParam);
                if (LOCAL_LOGD) Log.v(TAG, "New network state is " + result.state);
                if (mIsScanOnly) {
                    if (LOCAL_LOGD) Log.v(TAG, "Dropping event in scan-only mode");
                    break;
                }
                if (result.state != DetailedState.SCANNING) {
                    mNumScansSinceNetworkStateChange = 0;
                }
                if (result.state == DetailedState.DISCONNECTED) {
                    if (mWifiInfo.getSupplicantState() != SupplicantState.DORMANT) {
                        scheduleDisconnect();
                    }
                    break;
                }
                requestConnectionStatus(mWifiInfo);
                if (!(result.state == DetailedState.CONNECTED &&
                        (!mHaveIpAddress || mDisconnectPending))) {
                    setDetailedState(result.state);
                }
                if (result.state == DetailedState.CONNECTED) {
                    setNotificationVisible(false, 0, false, 0);
                    boolean wasDisconnectPending = mDisconnectPending;
                    cancelDisconnect();
                    if (wasDisconnectPending) {
                        DetailedState saveState = getNetworkInfo().getDetailedState();
                        handleDisconnectedState(DetailedState.DISCONNECTED, false);
                        setDetailedStateInternal(saveState);
                    }
                    configureInterface();
                    mLastBssid = result.BSSID;
                    mLastSsid = mWifiInfo.getSSID();
                    mLastNetworkId = result.networkId;
                    if (mHaveIpAddress) {
                        setDetailedState(DetailedState.CONNECTED);
                    } else {
                        setDetailedState(DetailedState.OBTAINING_IPADDR);
                    }
                }
                sendNetworkStateChangeBroadcast(mWifiInfo.getBSSID());
                break;
            case EVENT_SCAN_RESULTS_AVAILABLE:
                if (ActivityManagerNative.isSystemReady()) {
                    mContext.sendBroadcast(new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                }
                sendScanResultsAvailable();
                setScanMode(false);
                break;
            case EVENT_POLL_INTERVAL:
                if (mWifiInfo.getSupplicantState() != SupplicantState.UNINITIALIZED) {
                    requestPolledInfo(mWifiInfo, true);
                    checkPollTimer();
                }
                break;
            case EVENT_DEFERRED_DISCONNECT:
                if (mWifiInfo.getSupplicantState() != SupplicantState.UNINITIALIZED) {
                    handleDisconnectedState(DetailedState.DISCONNECTED, true);
                }
                break;
            case EVENT_DEFERRED_RECONNECT:
                String BSSID = (msg.obj != null) ? msg.obj.toString() : null;
                if (mWifiInfo.getSupplicantState() != SupplicantState.UNINITIALIZED) {
                    if (++mReconnectCount > getMaxDhcpRetries()) {
                        if (LOCAL_LOGD) {
                            Log.d(TAG, "Failed reconnect count: " +
                                    mReconnectCount + " Disabling " + BSSID);
                        }
                        mWM.disableNetwork(mLastNetworkId);
                    }
                    reconnectCommand();
                }
                break;
            case EVENT_INTERFACE_CONFIGURATION_SUCCEEDED:
                if (mWifiInfo.getSupplicantState() == SupplicantState.UNINITIALIZED) {
                    break;
                }
                mReconnectCount = 0;
                mHaveIpAddress = true;
                mObtainingIpAddress = false;
                mWifiInfo.setIpAddress(mDhcpInfo.ipAddress);
                mLastSignalLevel = -1; 
                if (mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) {
                    setDetailedState(DetailedState.CONNECTED);
                    sendNetworkStateChangeBroadcast(mWifiInfo.getBSSID());
                } else {
                    mTarget.sendEmptyMessage(EVENT_CONFIGURATION_CHANGED);
                }
                if (LOCAL_LOGD) Log.v(TAG, "IP configuration: " + mDhcpInfo);
                EventLog.writeEvent(EVENTLOG_INTERFACE_CONFIGURATION_STATE_CHANGED, 1);
                resetNotificationTimer();
                break;
            case EVENT_INTERFACE_CONFIGURATION_FAILED:
                if (mWifiInfo.getSupplicantState() != SupplicantState.UNINITIALIZED) {
                    EventLog.writeEvent(EVENTLOG_INTERFACE_CONFIGURATION_STATE_CHANGED, 0);
                    mHaveIpAddress = false;
                    mWifiInfo.setIpAddress(0);
                    mObtainingIpAddress = false;
                    disconnect();
                }
                break;
            case EVENT_DRIVER_STATE_CHANGED:
                EventLog.writeEvent(EVENTLOG_DRIVER_STATE_CHANGED, msg.arg1);
                switch (msg.arg1) {
                case DRIVER_STARTED:
                    setNumAllowedChannels();
                    synchronized (this) {
                        if (mRunState == RUN_STATE_STARTING) {
                            mRunState = RUN_STATE_RUNNING;
                            if (!mIsScanOnly) {
                                reconnectCommand();
                            } else {
                                scan(true);
                            }
                        }
                    }
                    break;
                case DRIVER_HUNG:
                    Log.e(TAG, "Wifi Driver reports HUNG - reloading.");
                    mWM.setWifiEnabled(false);
                    mWM.setWifiEnabled(true);
                    break;
                }
                noteRunState();
                break;
            case EVENT_PASSWORD_KEY_MAY_BE_INCORRECT:
                mPasswordKeyMayBeIncorrect = true;
                break;
        }
    }
    private boolean wifiManagerDisableNetwork(int networkId) {
        boolean disabledNetwork = false;
        if (0 <= networkId) {
            disabledNetwork = mWM.disableNetwork(networkId);
            if (LOCAL_LOGD) {
                if (disabledNetwork) {
                    Log.v(TAG, "Disabled network: " + networkId);
                }
            }
        }
        if (LOCAL_LOGD) {
            if (!disabledNetwork) {
                Log.e(TAG, "Failed to disable network:" +
                      " invalid network id: " + networkId);
            }
        }
        return disabledNetwork;
    }
    private void configureInterface() {
        checkPollTimer();
        mLastSignalLevel = -1;
        if (!mUseStaticIp) {
            if (!mHaveIpAddress && !mObtainingIpAddress) {
                mObtainingIpAddress = true;
                mDhcpTarget.sendEmptyMessage(EVENT_DHCP_START);
            }
        } else {
            int event;
            if (NetworkUtils.configureInterface(mInterfaceName, mDhcpInfo)) {
                mHaveIpAddress = true;
                event = EVENT_INTERFACE_CONFIGURATION_SUCCEEDED;
                if (LOCAL_LOGD) Log.v(TAG, "Static IP configuration succeeded");
            } else {
                mHaveIpAddress = false;
                event = EVENT_INTERFACE_CONFIGURATION_FAILED;
                if (LOCAL_LOGD) Log.v(TAG, "Static IP configuration failed");
            }
            sendEmptyMessage(event);
        }
    }
    private void handleDisconnectedState(DetailedState newState, boolean disableInterface) {
        if (mDisconnectPending) {
            cancelDisconnect();
        }
        mDisconnectExpected = false;
        resetConnections(disableInterface);
        setDetailedState(newState);
        sendNetworkStateChangeBroadcast(mLastBssid);
        mWifiInfo.setBSSID(null);
        mLastBssid = null;
        mLastSsid = null;
        mDisconnectPending = false;
    }
    public void resetConnections(boolean disableInterface) {
        if (LOCAL_LOGD) Log.d(TAG, "Reset connections and stopping DHCP");
        mHaveIpAddress = false;
        mObtainingIpAddress = false;
        mWifiInfo.setIpAddress(0);
        NetworkUtils.resetConnections(mInterfaceName);
        if (mDhcpTarget != null) {
            mDhcpTarget.setCancelCallback(true);
            mDhcpTarget.removeMessages(EVENT_DHCP_START);
        }
        if (!NetworkUtils.stopDhcp(mInterfaceName)) {
            Log.e(TAG, "Could not stop DHCP");
        }
        if(disableInterface) {
            if (LOCAL_LOGD) Log.d(TAG, "Disabling interface");
            NetworkUtils.disableInterface(mInterfaceName);
        }
    }
    private void scheduleDisconnect() {
        mDisconnectPending = true;
        if (!hasMessages(EVENT_DEFERRED_DISCONNECT)) {
            sendEmptyMessageDelayed(EVENT_DEFERRED_DISCONNECT, DISCONNECT_DELAY_MSECS);
        }
    }
    private void cancelDisconnect() {
        mDisconnectPending = false;
        removeMessages(EVENT_DEFERRED_DISCONNECT);
    }
    public DhcpInfo getDhcpInfo() {
        return mDhcpInfo;
    }
    public synchronized List<ScanResult> getScanResultsList() {
        return mScanResults;
    }
    public synchronized void setScanResultsList(List<ScanResult> scanList) {
        mScanResults = scanList;
    }
    public WifiInfo requestConnectionInfo() {
        requestConnectionStatus(mWifiInfo);
        requestPolledInfo(mWifiInfo, false);
        return mWifiInfo;
    }
    private void requestConnectionStatus(WifiInfo info) {
        String reply = status();
        if (reply == null) {
            return;
        }
        String SSID = null;
        String BSSID = null;
        String suppState = null;
        int netId = -1;
        String[] lines = reply.split("\n");
        for (String line : lines) {
            String[] prop = line.split(" *= *");
            if (prop.length < 2)
                continue;
            String name = prop[0];
            String value = prop[1];
            if (name.equalsIgnoreCase("id"))
                netId = Integer.parseInt(value);
            else if (name.equalsIgnoreCase("ssid"))
                SSID = value;
            else if (name.equalsIgnoreCase("bssid"))
                BSSID = value;
            else if (name.equalsIgnoreCase("wpa_state"))
                suppState = value;
        }
        info.setNetworkId(netId);
        info.setSSID(SSID);
        info.setBSSID(BSSID);
        if (mWifiInfo.getSupplicantState() == SupplicantState.UNINITIALIZED && suppState != null)
            setSupplicantState(suppState);
    }
    private synchronized void requestPolledInfo(WifiInfo info, boolean polling)
    {
        int newRssi = (polling ? getRssiApprox() : getRssi());
        if (newRssi != -1 && -200 < newRssi && newRssi < 256) { 
            if (newRssi > 0) newRssi -= 256;
            info.setRssi(newRssi);
            int newSignalLevel = WifiManager.calculateSignalLevel(newRssi, 4);
            if (newSignalLevel != mLastSignalLevel) {
                sendRssiChangeBroadcast(newRssi);
            }
            mLastSignalLevel = newSignalLevel;
        } else {
            info.setRssi(-200);
        }
        int newLinkSpeed = getLinkSpeed();
        if (newLinkSpeed != -1) {
            info.setLinkSpeed(newLinkSpeed);
        }
    }
    private void sendRssiChangeBroadcast(final int newRssi) {
        if (ActivityManagerNative.isSystemReady()) {
            Intent intent = new Intent(WifiManager.RSSI_CHANGED_ACTION);
            intent.putExtra(WifiManager.EXTRA_NEW_RSSI, newRssi);
            mContext.sendBroadcast(intent);
        }
    }
    private void sendNetworkStateChangeBroadcast(String bssid) {
        Intent intent = new Intent(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT
                | Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra(WifiManager.EXTRA_NETWORK_INFO, mNetworkInfo);
        if (bssid != null)
            intent.putExtra(WifiManager.EXTRA_BSSID, bssid);
        mContext.sendStickyBroadcast(intent);
    }
    public boolean teardown() {
        if (!mTornDownByConnMgr) {
            if (disconnectAndStop()) {
                setTornDownByConnMgr(true);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    public boolean reconnect() {
        if (mTornDownByConnMgr) {
            if (restart()) {
                setTornDownByConnMgr(false);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
    public synchronized boolean disconnectAndStop() {
        if (mRunState != RUN_STATE_STOPPING && mRunState != RUN_STATE_STOPPED) {
            setNotificationVisible(false, 0, false, 0);
            mRunState = RUN_STATE_STOPPING;
            if (mWifiInfo.getSupplicantState() == SupplicantState.DORMANT) {
                return stopDriver();
            } else {
                return disconnect();
            }
        }
        return true;
    }
    public synchronized boolean restart() {
        if (mRunState == RUN_STATE_STOPPED) {
            mRunState = RUN_STATE_STARTING;
            resetConnections(true);
            return startDriver();
        } else if (mRunState == RUN_STATE_STOPPING) {
            mRunState = RUN_STATE_STARTING;
        }
        return true;
    }
    public int getWifiState() {
        return mWifiState.get();
    }
    public void setWifiState(int wifiState) {
        mWifiState.set(wifiState);
    }
    public synchronized boolean loadDriver() {
        return WifiNative.loadDriver();
    }
    public synchronized boolean unloadDriver() {
        return WifiNative.unloadDriver();
    }
    public synchronized boolean startSupplicant() {
        return WifiNative.startSupplicant();
    }
    public synchronized boolean stopSupplicant() {
        return WifiNative.stopSupplicant();
    }
    public synchronized boolean connectToSupplicant() {
        return WifiNative.connectToSupplicant();
    }
    public synchronized void closeSupplicantConnection() {
        WifiNative.closeSupplicantConnection();
    }
    public synchronized boolean ping() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.pingCommand();
    }
    public synchronized boolean scan(boolean forceActive) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.scanCommand(forceActive);
    }
    public synchronized boolean setScanResultHandling(int mode) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.setScanResultHandlingCommand(mode);
    }
    public synchronized String scanResults() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return null;
        }
        return WifiNative.scanResultsCommand();
    }
    public synchronized boolean setScanMode(boolean isScanModeActive) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        if (mIsScanModeActive != isScanModeActive) {
            return WifiNative.setScanModeCommand(mIsScanModeActive = isScanModeActive);
        }
        return true;
    }
    public synchronized boolean disconnect() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.disconnectCommand();
    }
    public synchronized boolean reconnectCommand() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.reconnectCommand();
    }
    public synchronized int addNetwork() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return -1;
        }
        return WifiNative.addNetworkCommand();
    }
    public synchronized boolean removeNetwork(int networkId) {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return mDisconnectExpected = WifiNative.removeNetworkCommand(networkId);
    }
    public synchronized boolean enableNetwork(int netId, boolean disableOthers) {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.enableNetworkCommand(netId, disableOthers);
    }
    public synchronized boolean disableNetwork(int netId) {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.disableNetworkCommand(netId);
    }
    public synchronized boolean reassociate() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.reassociateCommand();
    }
    public synchronized boolean addToBlacklist(String bssid) {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.addToBlacklistCommand(bssid);
    }
    public synchronized boolean clearBlacklist() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.clearBlacklistCommand();
    }
    public synchronized String listNetworks() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return null;
        }
        return WifiNative.listNetworksCommand();
    }
    public synchronized String getNetworkVariable(int netId, String name) {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return null;
        }
        return WifiNative.getNetworkVariableCommand(netId, name);
    }
    public synchronized boolean setNetworkVariable(int netId, String name, String value) {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.setNetworkVariableCommand(netId, name, value);
    }
    public synchronized String status() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return null;
        }
        return WifiNative.statusCommand();
    }
    public synchronized int getRssi() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return -1;
        }
        return WifiNative.getRssiApproxCommand();
    }
    public synchronized int getRssiApprox() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return -1;
        }
        return WifiNative.getRssiApproxCommand();
    }
    public synchronized int getLinkSpeed() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return -1;
        }
        return WifiNative.getLinkSpeedCommand();
    }
    public synchronized String getMacAddress() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return null;
        }
        return WifiNative.getMacAddressCommand();
    }
    public synchronized boolean startDriver() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.startDriverCommand();
    }
    public synchronized boolean stopDriver() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.stopDriverCommand();
    }
    public synchronized boolean startPacketFiltering() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.startPacketFiltering();
    }
    public synchronized boolean stopPacketFiltering() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.stopPacketFiltering();
    }
    public synchronized boolean setPowerMode(int mode) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.setPowerModeCommand(mode);
    }
    public synchronized boolean setNumAllowedChannels() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        try {
            return setNumAllowedChannels(
                    Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.WIFI_NUM_ALLOWED_CHANNELS));
        } catch (Settings.SettingNotFoundException e) {
            if (mNumAllowedChannels != 0) {
                WifiNative.setNumAllowedChannelsCommand(mNumAllowedChannels);
            }
        }
        return true;
    }
    public synchronized boolean setNumAllowedChannels(int numChannels) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        mNumAllowedChannels = numChannels;
        return WifiNative.setNumAllowedChannelsCommand(numChannels);
    }
    public synchronized int getNumAllowedChannels() {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return -1;
        }
        return WifiNative.getNumAllowedChannelsCommand();
    }
    public synchronized boolean setBluetoothCoexistenceMode(int mode) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return false;
        }
        return WifiNative.setBluetoothCoexistenceModeCommand(mode);
    }
    public synchronized void setBluetoothScanMode(boolean isBluetoothPlaying) {
        if (mWifiState.get() != WIFI_STATE_ENABLED && !isDriverStopped()) {
            return;
        }
        WifiNative.setBluetoothCoexistenceScanModeCommand(isBluetoothPlaying);
    }
    public synchronized boolean saveConfig() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.saveConfigCommand();
    }
    public synchronized boolean reloadConfig() {
        if (mWifiState.get() != WIFI_STATE_ENABLED) {
            return false;
        }
        return WifiNative.reloadConfigCommand();
    }
    public boolean setRadio(boolean turnOn) {
        return mWM.setWifiEnabled(turnOn);
    }
    public int startUsingNetworkFeature(String feature, int callingPid, int callingUid) {
        return -1;
    }
    public int stopUsingNetworkFeature(String feature, int callingPid, int callingUid) {
        return -1;
    }
    @Override
    public void interpretScanResultsAvailable() {
        if (!mNotificationEnabled) return;
        NetworkInfo networkInfo = getNetworkInfo();
        State state = networkInfo.getState();
        if ((state == NetworkInfo.State.DISCONNECTED)
                || (state == NetworkInfo.State.UNKNOWN)) {
            List<ScanResult> scanResults = getScanResultsList();
            if (scanResults != null) {
                int numOpenNetworks = 0;
                for (int i = scanResults.size() - 1; i >= 0; i--) {
                    ScanResult scanResult = scanResults.get(i);
                    if (TextUtils.isEmpty(scanResult.capabilities)) {
                        numOpenNetworks++;
                    }
                }
                if (numOpenNetworks > 0) {
                    if (++mNumScansSinceNetworkStateChange >= NUM_SCANS_BEFORE_ACTUALLY_SCANNING) {
                        setNotificationVisible(true, numOpenNetworks, false, 0);
                    }
                    return;
                }
            }
        }
        setNotificationVisible(false, 0, false, 0);
    }
    public void setNotificationVisible(boolean visible, int numNetworks, boolean force, int delay) {
        if (!visible && !mNotificationShown && !force) {
            return;
        }
        Message message;
        if (visible) {
            if (System.currentTimeMillis() < mNotificationRepeatTime) {
                return;
            }
            if (mNotification == null) {
                mNotification = new Notification();
                mNotification.when = 0;
                mNotification.icon = ICON_NETWORKS_AVAILABLE;
                mNotification.flags = Notification.FLAG_AUTO_CANCEL;
                mNotification.contentIntent = PendingIntent.getActivity(mContext, 0,
                        new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), 0);
            }
            CharSequence title = mContext.getResources().getQuantityText(
                    com.android.internal.R.plurals.wifi_available, numNetworks);
            CharSequence details = mContext.getResources().getQuantityText(
                    com.android.internal.R.plurals.wifi_available_detailed, numNetworks);
            mNotification.tickerText = title;
            mNotification.setLatestEventInfo(mContext, title, details, mNotification.contentIntent);
            mNotificationRepeatTime = System.currentTimeMillis() + NOTIFICATION_REPEAT_DELAY_MS;
            message = mTarget.obtainMessage(EVENT_NOTIFICATION_CHANGED, 1,
                    ICON_NETWORKS_AVAILABLE, mNotification);
        } else {
            mTarget.removeMessages(EVENT_NOTIFICATION_CHANGED, mNotification);
            message = mTarget.obtainMessage(EVENT_NOTIFICATION_CHANGED, 0, ICON_NETWORKS_AVAILABLE);
        }
        mTarget.sendMessageDelayed(message, delay);
        mNotificationShown = visible;
    }
    private void resetNotificationTimer() {
        mNotificationRepeatTime = 0;
        mNumScansSinceNetworkStateChange = 0;
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("interface ").append(mInterfaceName);
        sb.append(" runState=");
        if (mRunState >= 1 && mRunState <= mRunStateNames.length) {
            sb.append(mRunStateNames[mRunState-1]);
        } else {
            sb.append(mRunState);
        }
        sb.append(LS).append(mWifiInfo).append(LS);
        sb.append(mDhcpInfo).append(LS);
        sb.append("haveIpAddress=").append(mHaveIpAddress).
                append(", obtainingIpAddress=").append(mObtainingIpAddress).
                append(", scanModeActive=").append(mIsScanModeActive).append(LS).
                append("lastSignalLevel=").append(mLastSignalLevel).
                append(", explicitlyDisabled=").append(mTornDownByConnMgr);
        return sb.toString();
    }
    private class DhcpHandler extends Handler {
        private Handler mTarget;
        private boolean mCancelCallback;
        private BluetoothHeadset mBluetoothHeadset;
        public DhcpHandler(Looper looper, Handler target) {
            super(looper);
            mTarget = target;
            mBluetoothHeadset = new BluetoothHeadset(mContext, null);
        }
        public void handleMessage(Message msg) {
            int event;
            switch (msg.what) {
                case EVENT_DHCP_START:
                    boolean modifiedBluetoothCoexistenceMode = false;
                    if (shouldDisableCoexistenceMode()) {
                        modifiedBluetoothCoexistenceMode = true;
                        setBluetoothCoexistenceMode(
                                WifiNative.BLUETOOTH_COEXISTENCE_MODE_DISABLED);
                    }
                    setPowerMode(DRIVER_POWER_MODE_ACTIVE);
                    synchronized (this) {
                        mCancelCallback = false;
                    }
                    Log.d(TAG, "DhcpHandler: DHCP request started");
                    if (NetworkUtils.runDhcp(mInterfaceName, mDhcpInfo)) {
                        event = EVENT_INTERFACE_CONFIGURATION_SUCCEEDED;
                        if (LOCAL_LOGD) Log.v(TAG, "DhcpHandler: DHCP request succeeded");
                    } else {
                        event = EVENT_INTERFACE_CONFIGURATION_FAILED;
                        Log.i(TAG, "DhcpHandler: DHCP request failed: " +
                            NetworkUtils.getDhcpError());
                    }
                    setPowerMode(DRIVER_POWER_MODE_AUTO);
                    if (modifiedBluetoothCoexistenceMode) {
                        setBluetoothCoexistenceMode(
                                WifiNative.BLUETOOTH_COEXISTENCE_MODE_SENSE);
                    }
                    synchronized (this) {
                        if (!mCancelCallback) {
                            mTarget.sendEmptyMessage(event);
                        }
                    }
                    break;
            }
        }
        public synchronized void setCancelCallback(boolean cancelCallback) {
            mCancelCallback = cancelCallback;
        }
        private boolean shouldDisableCoexistenceMode() {
            int state = mBluetoothHeadset.getState();
            return state == BluetoothHeadset.STATE_DISCONNECTED;
        }
    }
    private void checkUseStaticIp() {
        mUseStaticIp = false;
        final ContentResolver cr = mContext.getContentResolver();
        try {
            if (Settings.System.getInt(cr, Settings.System.WIFI_USE_STATIC_IP) == 0) {
                return;
            }
        } catch (Settings.SettingNotFoundException e) {
            return;
        }
        try {
            String addr = Settings.System.getString(cr, Settings.System.WIFI_STATIC_IP);
            if (addr != null) {
                mDhcpInfo.ipAddress = stringToIpAddr(addr);
            } else {
                return;
            }
            addr = Settings.System.getString(cr, Settings.System.WIFI_STATIC_GATEWAY);
            if (addr != null) {
                mDhcpInfo.gateway = stringToIpAddr(addr);
            } else {
                return;
            }
            addr = Settings.System.getString(cr, Settings.System.WIFI_STATIC_NETMASK);
            if (addr != null) {
                mDhcpInfo.netmask = stringToIpAddr(addr);
            } else {
                return;
            }
            addr = Settings.System.getString(cr, Settings.System.WIFI_STATIC_DNS1);
            if (addr != null) {
                mDhcpInfo.dns1 = stringToIpAddr(addr);
            } else {
                return;
            }
            addr = Settings.System.getString(cr, Settings.System.WIFI_STATIC_DNS2);
            if (addr != null) {
                mDhcpInfo.dns2 = stringToIpAddr(addr);
            } else {
                mDhcpInfo.dns2 = 0;
            }
        } catch (UnknownHostException e) {
            return;
        }
        mUseStaticIp = true;
    }
    private static int stringToIpAddr(String addrString) throws UnknownHostException {
        try {
            String[] parts = addrString.split("\\.");
            if (parts.length != 4) {
                throw new UnknownHostException(addrString);
            }
            int a = Integer.parseInt(parts[0])      ;
            int b = Integer.parseInt(parts[1]) <<  8;
            int c = Integer.parseInt(parts[2]) << 16;
            int d = Integer.parseInt(parts[3]) << 24;
            return a | b | c | d;
        } catch (NumberFormatException ex) {
            throw new UnknownHostException(addrString);
        }
    }
    private int getMaxDhcpRetries() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                                      Settings.Secure.WIFI_MAX_DHCP_RETRY_COUNT,
                                      DEFAULT_MAX_DHCP_RETRIES);
    }
    private class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
            ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_USE_STATIC_IP), false, this);
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_STATIC_IP), false, this);
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_STATIC_GATEWAY), false, this);
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_STATIC_NETMASK), false, this);
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_STATIC_DNS1), false, this);
            cr.registerContentObserver(Settings.System.getUriFor(
                Settings.System.WIFI_STATIC_DNS2), false, this);
        }
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            boolean wasStaticIp = mUseStaticIp;
            int oIp, oGw, oMsk, oDns1, oDns2;
            oIp = oGw = oMsk = oDns1 = oDns2 = 0;
            if (wasStaticIp) {
                oIp = mDhcpInfo.ipAddress;
                oGw = mDhcpInfo.gateway;
                oMsk = mDhcpInfo.netmask;
                oDns1 = mDhcpInfo.dns1;
                oDns2 = mDhcpInfo.dns2;
            }
            checkUseStaticIp();
            if (mWifiInfo.getSupplicantState() == SupplicantState.UNINITIALIZED) {
                return;
            }
            boolean changed =
                (wasStaticIp != mUseStaticIp) ||
                    (wasStaticIp && (
                        oIp   != mDhcpInfo.ipAddress ||
                        oGw   != mDhcpInfo.gateway ||
                        oMsk  != mDhcpInfo.netmask ||
                        oDns1 != mDhcpInfo.dns1 ||
                        oDns2 != mDhcpInfo.dns2));
            if (changed) {
                resetConnections(true);
                configureInterface();
                if (mUseStaticIp) {
                    mTarget.sendEmptyMessage(EVENT_CONFIGURATION_CHANGED);
                }
            }
        }
    }
    private class NotificationEnabledSettingObserver extends ContentObserver {
        public NotificationEnabledSettingObserver(Handler handler) {
            super(handler);
        }
        public void register() {
            ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(Settings.Secure.getUriFor(
                Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON), true, this);
            mNotificationEnabled = getValue();
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            mNotificationEnabled = getValue();
            if (!mNotificationEnabled) {
                setNotificationVisible(false, 0, true, 0);
            }
            resetNotificationTimer();
        }
        private boolean getValue() {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 1) == 1;
        }
    }
}
