public class ConnectivityManagerTestActivity extends Activity {
    public static final String LOG_TAG = "ConnectivityManagerTestActivity";
    public static final int WAIT_FOR_SCAN_RESULT = 5 * 1000; 
    public static final int WIFI_SCAN_TIMEOUT = 20 * 1000;
    public ConnectivityReceiver mConnectivityReceiver = null;
    public WifiReceiver mWifiReceiver = null;
    public State mState;
    public NetworkInfo mNetworkInfo;
    public NetworkInfo mOtherNetworkInfo;
    public boolean mIsFailOver;
    public String mReason;
    public boolean mScanResultIsAvailable = false;
    public ConnectivityManager mCM;
    public Object wifiObject = new Object();
    public Object connectivityObject = new Object();
    public int mWifiState;
    public NetworkInfo mWifiNetworkInfo;
    public String mBssid;
    public WifiManager mWifiManager;
    public static final int NUM_NETWORK_TYPES = ConnectivityManager.MAX_NETWORK_TYPE + 1;
    NetworkState[] connectivityState = new NetworkState[NUM_NETWORK_TYPES];
    private class ConnectivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(LOG_TAG, "ConnectivityReceiver: onReceive() is called with " + intent);
            String action = intent.getAction();
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.v("ConnectivityReceiver", "onReceive() called with " + intent);
                return;
            }
            boolean noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (noConnectivity) {
                mState = State.DISCONNECTED;
            } else {
                mState = State.CONNECTED;
            }
            mNetworkInfo = (NetworkInfo)
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            mOtherNetworkInfo = (NetworkInfo)
                intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
            mReason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            mIsFailOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            Log.v(LOG_TAG, "mNetworkInfo: " + mNetworkInfo.toString());
            if (mOtherNetworkInfo != null) {
                Log.v(LOG_TAG, "mOtherNetworkInfo: " + mOtherNetworkInfo.toString());
            }
            recordNetworkState(mNetworkInfo.getType(), mNetworkInfo.getState());
            if (mOtherNetworkInfo != null) {
                recordNetworkState(mOtherNetworkInfo.getType(), mOtherNetworkInfo.getState());
            }
            notifyNetworkConnectivityChange();
        }
    }
    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("WifiReceiver", "onReceive() is calleld with " + intent);
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                notifyScanResult();
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                mWifiNetworkInfo =
                    (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.v(LOG_TAG, "mWifiNetworkInfo: " + mWifiNetworkInfo.toString());
                if (mWifiNetworkInfo.getState() == State.CONNECTED) {
                    mBssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                }
                notifyWifiState();
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                                                WifiManager.WIFI_STATE_UNKNOWN);
                notifyWifiState();
            }
            else {
                return;
            }
        }
    }
    public ConnectivityManagerTestActivity() {
        mState = State.UNKNOWN;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate, inst=" + Integer.toHexString(hashCode()));
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        setContentView(contentView);
        setTitle("ConnectivityManagerTestActivity");
        mConnectivityReceiver = new ConnectivityReceiver();
        registerReceiver(mConnectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mWifiReceiver = new WifiReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, mIntentFilter);
        mCM = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifiManager =(WifiManager)getSystemService(Context.WIFI_SERVICE);
        initializeNetworkStates();
        if (mWifiManager.isWifiEnabled()) {
            Log.v(LOG_TAG, "Clear Wifi before we start the test.");
            clearWifi();
        }
     }
    public void initializeNetworkStates() {
        for (int networkType = NUM_NETWORK_TYPES - 1; networkType >=0; networkType--) {
            connectivityState[networkType] =  new NetworkState();
            Log.v(LOG_TAG, "Initialize network state for " + networkType + ": " +
                    connectivityState[networkType].toString());
        }
    }
    public void recordNetworkState(int networkType, State networkState) {
        Log.v(LOG_TAG, "record network state for network " +  networkType +
                ", state is " + networkState);
        connectivityState[networkType].recordState(networkState);
    }
    public void setStateTransitionCriteria(int networkType, State initState,
            int transitionDir, State targetState) {
        connectivityState[networkType].setStateTransitionCriteria(
                initState, transitionDir, targetState);
    }
    public boolean validateNetworkStates(int networkType) {
        Log.v(LOG_TAG, "validate network state for " + networkType + ": ");
        return connectivityState[networkType].validateStateTransition();
    }
    public String getTransitionFailureReason(int networkType) {
        Log.v(LOG_TAG, "get network state transition failure reason for " + networkType + ": " +
                connectivityState[networkType].toString());
        return connectivityState[networkType].getReason();
    }
    private void notifyNetworkConnectivityChange() {
        synchronized(connectivityObject) {
            Log.v(LOG_TAG, "notify network connectivity changed");
            connectivityObject.notifyAll();
        }
    }
    private void notifyScanResult() {
        synchronized (this) {
            Log.v(LOG_TAG, "notify that scan results are available");
            this.notify();
        }
    }
    public void notifyWifiState() {
        synchronized (wifiObject) {
            Log.v(LOG_TAG, "notify wifi state changed");
            wifiObject.notify();
        }
    }
    public boolean isConnectedToMobile() {
        return (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
    }
    public boolean isConnectedToWifi() {
        return (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }
    public boolean enableWifi() {
        return mWifiManager.setWifiEnabled(true);
    }
    public boolean connectToWifi(String knownSSID) {
        if (!mWifiManager.isWifiEnabled()) {
            Log.v(LOG_TAG, "Wifi is not enabled, enable it");
            mWifiManager.setWifiEnabled(true);
        }
        List<ScanResult> netList = mWifiManager.getScanResults();
        if (netList == null) {
            mWifiManager.startScanActive();
            mScanResultIsAvailable = false;
            long startTime = System.currentTimeMillis();
            while (!mScanResultIsAvailable) {
                if ((System.currentTimeMillis() - startTime) > WIFI_SCAN_TIMEOUT) {
                    return false;
                }
                synchronized (this) {
                    try {
                        this.wait(WAIT_FOR_SCAN_RESULT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ((mWifiManager.getScanResults() == null) ||
                            (mWifiManager.getScanResults().size() <= 0)) {
                        continue;
                    }
                    mScanResultIsAvailable = true;
                }
            }
        }
        netList = mWifiManager.getScanResults();
        for (int i = 0; i < netList.size(); i++) {
            ScanResult sr= netList.get(i);
            if (sr.SSID.equals(knownSSID)) {
                Log.v(LOG_TAG, "found " + knownSSID + " in the scan result list");
                WifiConfiguration config = new WifiConfiguration();
                config.SSID = convertToQuotedString(sr.SSID);
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                int networkId = mWifiManager.addNetwork(config);
                mWifiManager.enableNetwork(networkId, true);
                mWifiManager.saveConfiguration();
                mWifiManager.reconnect();
                break;
           }
        }
        List<WifiConfiguration> netConfList = mWifiManager.getConfiguredNetworks();
        if (netConfList.size() <= 0) {
            Log.v(LOG_TAG, knownSSID + " is not available");
            return false;
        }
        return true;
    }
    public boolean disconnectAP() {
        if (mWifiManager.isWifiEnabled()) {
            WifiInfo curWifi = mWifiManager.getConnectionInfo();
            if (curWifi == null) {
                return false;
            }
            int curNetworkId = curWifi.getNetworkId();
            mWifiManager.removeNetwork(curNetworkId);
            mWifiManager.saveConfiguration();
            List<WifiConfiguration> netConfList = mWifiManager.getConfiguredNetworks();
            if (netConfList != null) {
                Log.v(LOG_TAG, "remove configured network ids");
                for (int i = 0; i < netConfList.size(); i++) {
                    WifiConfiguration conf = new WifiConfiguration();
                    conf = netConfList.get(i);
                    mWifiManager.removeNetwork(conf.networkId);
                }
            }
        }
        mWifiManager.saveConfiguration();
        return true;
    }
    public boolean disableWifi() {
        return mWifiManager.setWifiEnabled(false);
    }
    public boolean clearWifi() {
            if (!disconnectAP()) {
                return false;
            }
            if (!mWifiManager.setWifiEnabled(false)) {
                return false;
            }
            try {
                Thread.sleep(5*1000);
            } catch (InterruptedException e) {}
        return true;
    }
    public void setAirplaneMode(Context context, boolean enableAM) {
        Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON,
                enableAM ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", enableAM);
        context.sendBroadcast(intent);
    }
    protected static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectivityReceiver != null) {
            unregisterReceiver(mConnectivityReceiver);
        }
        if (mWifiReceiver != null) {
            unregisterReceiver(mWifiReceiver);
        }
        Log.v(LOG_TAG, "onDestroy, inst=" + Integer.toHexString(hashCode()));
    }
}
