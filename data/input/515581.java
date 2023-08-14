public class WifiSettings extends PreferenceActivity implements DialogInterface.OnClickListener {
    private static final int MENU_ID_SCAN = Menu.FIRST;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 1;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 2;
    private static final int MENU_ID_FORGET = Menu.FIRST + 3;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 4;
    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    private final Scanner mScanner;
    private WifiManager mWifiManager;
    private WifiEnabler mWifiEnabler;
    private CheckBoxPreference mNotifyOpenNetworks;
    private ProgressCategory mAccessPoints;
    private Preference mAddNetwork;
    private DetailedState mLastState;
    private WifiInfo mLastInfo;
    private int mLastPriority;
    private boolean mResetNetworks = false;
    private int mKeyStoreNetworkId = -1;
    private AccessPoint mSelected;
    private WifiDialog mDialog;
    public WifiSettings() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };
        mScanner = new Scanner();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (getIntent().getBooleanExtra("only_access_points", false)) {
            addPreferencesFromResource(R.xml.wifi_access_points);
        } else {
            addPreferencesFromResource(R.xml.wifi_settings);
            mWifiEnabler = new WifiEnabler(this,
                    (CheckBoxPreference) findPreference("enable_wifi"));
            mNotifyOpenNetworks =
                    (CheckBoxPreference) findPreference("notify_open_networks");
            mNotifyOpenNetworks.setChecked(Secure.getInt(getContentResolver(),
                    Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
        }
        mAccessPoints = (ProgressCategory) findPreference("access_points");
        mAccessPoints.setOrderingAsAdded(false);
        mAddNetwork = findPreference("add_network");
        registerForContextMenu(getListView());
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mWifiEnabler != null) {
            mWifiEnabler.resume();
        }
        registerReceiver(mReceiver, mFilter);
        if (mKeyStoreNetworkId != -1 && KeyStore.getInstance().test() == KeyStore.NO_ERROR) {
            connect(mKeyStoreNetworkId);
        }
        mKeyStoreNetworkId = -1;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }
        unregisterReceiver(mReceiver);
        mScanner.pause();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
        if (mResetNetworks) {
            enableNetworks();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_SCAN, 0, R.string.wifi_menu_scan)
                .setIcon(R.drawable.ic_menu_scan_network);
        menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
                .setIcon(android.R.drawable.ic_menu_manage);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SCAN:
                if (mWifiManager.isWifiEnabled()) {
                    mScanner.resume();
                }
                return true;
            case MENU_ID_ADVANCED:
                startActivity(new Intent(this, AdvancedSettings.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        if (info instanceof AdapterContextMenuInfo) {
            Preference preference = (Preference) getListView().getItemAtPosition(
                    ((AdapterContextMenuInfo) info).position);
            if (preference instanceof AccessPoint) {
                mSelected = (AccessPoint) preference;
                menu.setHeaderTitle(mSelected.ssid);
                if (mSelected.getLevel() != -1 && mSelected.getState() == null) {
                    menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
                }
                if (mSelected.networkId != -1) {
                    menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                    if (mSelected.security != AccessPoint.SECURITY_NONE) {
                        menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
                    }
                }
            }
        }
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mSelected == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT:
                if (mSelected.networkId != -1) {
                    if (!requireKeyStore(mSelected.getConfig())) {
                        connect(mSelected.networkId);
                    }
                } else if (mSelected.security == AccessPoint.SECURITY_NONE) {
                    WifiConfiguration config = new WifiConfiguration();
                    config.SSID = AccessPoint.convertToQuotedString(mSelected.ssid);
                    config.allowedKeyManagement.set(KeyMgmt.NONE);
                    int networkId = mWifiManager.addNetwork(config);
                    mWifiManager.enableNetwork(networkId, false);
                    connect(networkId);
                } else {
                    showDialog(mSelected, false);
                }
                return true;
            case MENU_ID_FORGET:
                forget(mSelected.networkId);
                return true;
            case MENU_ID_MODIFY:
                showDialog(mSelected, true);
                return true;
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        if (preference instanceof AccessPoint) {
            mSelected = (AccessPoint) preference;
            showDialog(mSelected, false);
        } else if (preference == mAddNetwork) {
            mSelected = null;
            showDialog(null, true);
        } else if (preference == mNotifyOpenNetworks) {
            Secure.putInt(getContentResolver(),
                    Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    mNotifyOpenNetworks.isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }
    public void onClick(DialogInterface dialogInterface, int button) {
        if (button == WifiDialog.BUTTON_FORGET && mSelected != null) {
            forget(mSelected.networkId);
        } else if (button == WifiDialog.BUTTON_SUBMIT && mDialog != null) {
            WifiConfiguration config = mDialog.getConfig();
            if (config == null) {
                if (mSelected != null && !requireKeyStore(mSelected.getConfig())) {
                    connect(mSelected.networkId);
                }
            } else if (config.networkId != -1) {
                if (mSelected != null) {
                    mWifiManager.updateNetwork(config);
                    saveNetworks();
                }
            } else {
                int networkId = mWifiManager.addNetwork(config);
                if (networkId != -1) {
                    mWifiManager.enableNetwork(networkId, false);
                    config.networkId = networkId;
                    if (mDialog.edit || requireKeyStore(config)) {
                        saveNetworks();
                    } else {
                        connect(networkId);
                    }
                }
            }
        }
    }
    private void showDialog(AccessPoint accessPoint, boolean edit) {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        mDialog = new WifiDialog(this, this, accessPoint, edit);
        mDialog.show();
    }
    private boolean requireKeyStore(WifiConfiguration config) {
        if (WifiDialog.requireKeyStore(config) &&
                KeyStore.getInstance().test() != KeyStore.NO_ERROR) {
            mKeyStoreNetworkId = config.networkId;
            Credentials.getInstance().unlock(this);
            return true;
        }
        return false;
    }
    private void forget(int networkId) {
        mWifiManager.removeNetwork(networkId);
        saveNetworks();
    }
    private void connect(int networkId) {
        if (networkId == -1) {
            return;
        }
        if (mLastPriority > 1000000) {
            for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
                AccessPoint accessPoint = (AccessPoint) mAccessPoints.getPreference(i);
                if (accessPoint.networkId != -1) {
                    WifiConfiguration config = new WifiConfiguration();
                    config.networkId = accessPoint.networkId;
                    config.priority = 0;
                    mWifiManager.updateNetwork(config);
                }
            }
            mLastPriority = 0;
        }
        WifiConfiguration config = new WifiConfiguration();
        config.networkId = networkId;
        config.priority = ++mLastPriority;
        mWifiManager.updateNetwork(config);
        saveNetworks();
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
        mResetNetworks = true;
    }
    private void enableNetworks() {
        for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
            WifiConfiguration config = ((AccessPoint) mAccessPoints.getPreference(i)).getConfig();
            if (config != null && config.status != Status.ENABLED) {
                mWifiManager.enableNetwork(config.networkId, false);
            }
        }
        mResetNetworks = false;
    }
    private void saveNetworks() {
        enableNetworks();
        mWifiManager.saveConfiguration();
        updateAccessPoints();
    }
    private void updateAccessPoints() {
        List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            mLastPriority = 0;
            for (WifiConfiguration config : configs) {
                if (config.priority > mLastPriority) {
                    mLastPriority = config.priority;
                }
                if (config.status == Status.CURRENT) {
                    config.status = Status.ENABLED;
                } else if (mResetNetworks && config.status == Status.DISABLED) {
                    config.status = Status.CURRENT;
                }
                AccessPoint accessPoint = new AccessPoint(this, config);
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
            }
        }
        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0 ||
                        result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (AccessPoint accessPoint : accessPoints) {
                    if (accessPoint.update(result)) {
                        found = true;
                    }
                }
                if (!found) {
                    accessPoints.add(new AccessPoint(this, result));
                }
            }
        }
        mAccessPoints.removeAll();
        for (AccessPoint accessPoint : accessPoints) {
            mAccessPoints.addPreference(accessPoint);
        }
    }
    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
            if (mSelected != null && mSelected.networkId != -1) {
                mSelected = null;
            }
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState)
                    intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            updateConnectionState(((NetworkInfo) intent.getParcelableExtra(
                    WifiManager.EXTRA_NETWORK_INFO)).getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }
    private void updateConnectionState(DetailedState state) {
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }
        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }
        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }
        for (int i = mAccessPoints.getPreferenceCount() - 1; i >= 0; --i) {
            ((AccessPoint) mAccessPoints.getPreference(i)).update(mLastInfo, mLastState);
        }
        if (mResetNetworks && (state == DetailedState.CONNECTED ||
                state == DetailedState.DISCONNECTED || state == DetailedState.FAILED)) {
            updateAccessPoints();
            enableNetworks();
        }
    }
    private void updateWifiState(int state) {
        if (state == WifiManager.WIFI_STATE_ENABLED) {
            mScanner.resume();
            updateAccessPoints();
        } else {
            mScanner.pause();
            mAccessPoints.removeAll();
        }
    }
    private class Scanner extends Handler {
        private int mRetry = 0;
        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }
        void pause() {
            mRetry = 0;
            mAccessPoints.setProgress(false);
            removeMessages(0);
        }
        @Override
        public void handleMessage(Message message) {
            if (mWifiManager.startScanActive()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(WifiSettings.this, R.string.wifi_fail_to_scan,
                        Toast.LENGTH_LONG).show();
                return;
            }
            mAccessPoints.setProgress(mRetry != 0);
            sendEmptyMessageDelayed(0, 6000);
        }
    }
}
