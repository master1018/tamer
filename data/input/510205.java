public class BluetoothSettings extends PreferenceActivity
        implements LocalBluetoothManager.Callback {
    private static final String TAG = "BluetoothSettings";
    private static final String KEY_BT_CHECKBOX = "bt_checkbox";
    private static final String KEY_BT_DISCOVERABLE = "bt_discoverable";
    private static final String KEY_BT_DEVICE_LIST = "bt_device_list";
    private static final String KEY_BT_NAME = "bt_name";
    private static final String KEY_BT_SCAN = "bt_scan";
    private static final int SCREEN_TYPE_SETTINGS = 0;
    private static final int SCREEN_TYPE_DEVICEPICKER = 1;
    private int mScreenType;
    private int mFilterType;
    private boolean mNeedAuth;
    private String mLaunchPackage;
    private String mLaunchClass;
    private BluetoothDevice mSelectedDevice= null;
    private LocalBluetoothManager mLocalManager;
    private BluetoothEnabler mEnabler;
    private BluetoothDiscoverableEnabler mDiscoverableEnabler;
    private BluetoothNamePreference mNamePreference;
    private ProgressCategory mDeviceList;
    private WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference> mDevicePreferenceMap =
            new WeakHashMap<CachedBluetoothDevice, BluetoothDevicePreference>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                onBluetoothStateChanged(mLocalManager.getBluetoothState());
            } else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                    && mScreenType == SCREEN_TYPE_DEVICEPICKER) {
                int bondState = intent
                        .getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (bondState == BluetoothDevice.BOND_BONDED) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.equals(mSelectedDevice)) {
                        sendDevicePickedIntent(device);
                        finish();
                    }
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocalManager = LocalBluetoothManager.getInstance(this);
        if (mLocalManager == null) finish();
        mFilterType = BluetoothDevicePicker.FILTER_TYPE_ALL;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(BluetoothDevicePicker.ACTION_LAUNCH)) {
            mScreenType = SCREEN_TYPE_DEVICEPICKER;
            mNeedAuth = intent.getBooleanExtra(BluetoothDevicePicker.EXTRA_NEED_AUTH, false);
            mFilterType = intent.getIntExtra(BluetoothDevicePicker.EXTRA_FILTER_TYPE,
                    BluetoothDevicePicker.FILTER_TYPE_ALL);
            mLaunchPackage = intent.getStringExtra(BluetoothDevicePicker.EXTRA_LAUNCH_PACKAGE);
            mLaunchClass = intent.getStringExtra(BluetoothDevicePicker.EXTRA_LAUNCH_CLASS);
            setTitle(getString(R.string.device_picker));
            addPreferencesFromResource(R.xml.device_picker);
        } else {
            addPreferencesFromResource(R.xml.bluetooth_settings);
            mEnabler = new BluetoothEnabler(
                    this,
                    (CheckBoxPreference) findPreference(KEY_BT_CHECKBOX));
            mDiscoverableEnabler = new BluetoothDiscoverableEnabler(
                    this,
                    (CheckBoxPreference) findPreference(KEY_BT_DISCOVERABLE));
            mNamePreference = (BluetoothNamePreference) findPreference(KEY_BT_NAME);
            mDeviceList = (ProgressCategory) findPreference(KEY_BT_DEVICE_LIST);
        }
        mDeviceList = (ProgressCategory) findPreference(KEY_BT_DEVICE_LIST);
        registerForContextMenu(getListView());
    }
    @Override
    protected void onResume() {
        super.onResume();
        mDevicePreferenceMap.clear();
        mDeviceList.removeAll();
        addDevices();
        if (mScreenType == SCREEN_TYPE_SETTINGS) {
            mEnabler.resume();
            mDiscoverableEnabler.resume();
            mNamePreference.resume();
        }
        mLocalManager.registerCallback(this);
        mDeviceList.setProgress(mLocalManager.getBluetoothAdapter().isDiscovering());
        mLocalManager.startScanning(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
        mLocalManager.setForegroundActivity(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mLocalManager.setForegroundActivity(null);
        unregisterReceiver(mReceiver);
        mLocalManager.unregisterCallback(this);
        if (mScreenType == SCREEN_TYPE_SETTINGS) {
            mNamePreference.pause();
            mDiscoverableEnabler.pause();
            mEnabler.pause();
        }
    }
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        mLocalManager.stopScanning();
    }
    private void addDevices() {
        List<CachedBluetoothDevice> cachedDevices =
                mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
            onDeviceAdded(cachedDevice);
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (KEY_BT_SCAN.equals(preference.getKey())) {
            mLocalManager.startScanning(true);
            return true;
        }
        if (preference instanceof BluetoothDevicePreference) {
            BluetoothDevicePreference btPreference = (BluetoothDevicePreference)preference;
            if (mScreenType == SCREEN_TYPE_SETTINGS) {
                btPreference.getCachedDevice().onClicked();
            } else if (mScreenType == SCREEN_TYPE_DEVICEPICKER) {
                CachedBluetoothDevice device = btPreference.getCachedDevice();
                mSelectedDevice = device.getDevice();
                mLocalManager.stopScanning();
                mLocalManager.persistSelectedDeviceInPicker(mSelectedDevice.getAddress());
                if ((device.getBondState() == BluetoothDevice.BOND_BONDED) ||
                        (mNeedAuth == false)) {
                    sendDevicePickedIntent(mSelectedDevice);
                    finish();
                } else {
                    btPreference.getCachedDevice().onClicked();
                }
            }
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (mScreenType != SCREEN_TYPE_SETTINGS) {
            return;
        }
        CachedBluetoothDevice cachedDevice = getDeviceFromMenuInfo(menuInfo);
        if (cachedDevice == null) return;
        cachedDevice.onCreateContextMenu(menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        CachedBluetoothDevice cachedDevice = getDeviceFromMenuInfo(item.getMenuInfo());
        if (cachedDevice == null) return false;
        cachedDevice.onContextItemSelected(item);
        return true;
    }
    private CachedBluetoothDevice getDeviceFromMenuInfo(ContextMenuInfo menuInfo) {
        if ((menuInfo == null) || !(menuInfo instanceof AdapterContextMenuInfo)) {
            return null;
        }
        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        Preference pref = (Preference) getPreferenceScreen().getRootAdapter().getItem(
                adapterMenuInfo.position);
        if (pref == null || !(pref instanceof BluetoothDevicePreference)) {
            return null;
        }
        return ((BluetoothDevicePreference) pref).getCachedDevice();
    }
    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if (mDevicePreferenceMap.get(cachedDevice) != null) {
            throw new IllegalStateException("Got onDeviceAdded, but cachedDevice already exists");
        }
        if (addDevicePreference(cachedDevice)) {
            createDevicePreference(cachedDevice);
        }
     }
    private boolean addDevicePreference(CachedBluetoothDevice cachedDevice) {
        ParcelUuid[] uuids = cachedDevice.getDevice().getUuids();
        BluetoothClass bluetoothClass = cachedDevice.getDevice().getBluetoothClass();
        switch(mFilterType) {
        case BluetoothDevicePicker.FILTER_TYPE_TRANSFER:
            if (uuids != null)
                if (BluetoothUuid.containsAnyUuid(uuids,
                        LocalBluetoothProfileManager.OPP_PROFILE_UUIDS))  return true;
                if (bluetoothClass != null
                        && bluetoothClass.doesClassMatch(BluetoothClass.PROFILE_OPP)) {
                    return true;
                }
            break;
        case BluetoothDevicePicker.FILTER_TYPE_AUDIO:
            if (uuids != null) {
                if (BluetoothUuid.containsAnyUuid(uuids,
                        LocalBluetoothProfileManager.A2DP_PROFILE_UUIDS))  return true;
                if (BluetoothUuid.containsAnyUuid(uuids,
                        LocalBluetoothProfileManager.HEADSET_PROFILE_UUIDS))  return true;
            } else if (bluetoothClass != null) {
                if (bluetoothClass.doesClassMatch(BluetoothClass.PROFILE_A2DP)) return true;
                if (bluetoothClass.doesClassMatch(BluetoothClass.PROFILE_HEADSET)) return true;
            }
            break;
        default:
            return true;
        }
        return false;
    }
    private void createDevicePreference(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = new BluetoothDevicePreference(this, cachedDevice);
        mDeviceList.addPreference(preference);
        mDevicePreferenceMap.put(cachedDevice, preference);
    }
    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        BluetoothDevicePreference preference = mDevicePreferenceMap.remove(cachedDevice);
        if (preference != null) {
            mDeviceList.removePreference(preference);
        }
    }
    public void onScanningStateChanged(boolean started) {
        mDeviceList.setProgress(started);
    }
    private void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == BluetoothAdapter.STATE_ON) {
            mLocalManager.startScanning(false);
        } else if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            mDeviceList.setProgress(false);
        }
    }
    private void sendDevicePickedIntent(BluetoothDevice device) {
        Intent intent = new Intent(BluetoothDevicePicker.ACTION_DEVICE_SELECTED);
        if (mLaunchPackage != null && mLaunchClass != null) {
            intent.setClassName(mLaunchPackage, mLaunchClass);
        }
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        sendBroadcast(intent);
    }
}
