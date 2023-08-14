public class ConnectSpecificProfilesActivity extends PreferenceActivity
        implements CachedBluetoothDevice.Callback, Preference.OnPreferenceChangeListener {
    private static final String TAG = "ConnectSpecificProfilesActivity";
    private static final String KEY_ONLINE_MODE = "online_mode";
    private static final String KEY_TITLE = "title";
    private static final String KEY_PROFILE_CONTAINER = "profile_container";
    public static final String EXTRA_DEVICE = "device";
    private LocalBluetoothManager mManager;
    private CachedBluetoothDevice mCachedDevice;
    private PreferenceGroup mProfileContainer;
    private CheckBoxPreference mOnlineModePreference;
    private boolean mOnlineMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothDevice device;
        if (savedInstanceState != null) {
            device = savedInstanceState.getParcelable(EXTRA_DEVICE);
        } else {
            Intent intent = getIntent();
            device = intent.getParcelableExtra(EXTRA_DEVICE);
        }
        if (device == null) {
            Log.w(TAG, "Activity started without a remote blueototh device");
            finish();
        }
        mManager = LocalBluetoothManager.getInstance(this);
        mCachedDevice = mManager.getCachedDeviceManager().findDevice(device);
        if (mCachedDevice == null) {
            Log.w(TAG, "Device not found, cannot connect to it");
            finish();
        }
        addPreferencesFromResource(R.xml.bluetooth_device_advanced);
        mProfileContainer = (PreferenceGroup) findPreference(KEY_PROFILE_CONTAINER);
        findPreference(KEY_TITLE).setTitle(
                getString(R.string.bluetooth_device_advanced_title, mCachedDevice.getName()));
        mOnlineModePreference = (CheckBoxPreference) findPreference(KEY_ONLINE_MODE);
        mOnlineModePreference.setOnPreferenceChangeListener(this);
        addPreferencesForProfiles();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_DEVICE, mCachedDevice.getDevice());
    }
    @Override
    protected void onResume() {
        super.onResume();
        mManager.setForegroundActivity(this);
        mCachedDevice.registerCallback(this);
        refresh();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mCachedDevice.unregisterCallback(this);
        mManager.setForegroundActivity(null);
    }
    private void addPreferencesForProfiles() {
        for (Profile profile : mCachedDevice.getConnectableProfiles()) {
            Preference pref = createProfilePreference(profile);
            mProfileContainer.addPreference(pref);
        }
    }
    private CheckBoxPreference createProfilePreference(Profile profile) {
        CheckBoxPreference pref = new CheckBoxPreference(this);
        pref.setKey(profile.toString());
        pref.setTitle(profile.localizedString);
        pref.setPersistent(false);
        pref.setOnPreferenceChangeListener(this);
        LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                .getProfileManager(mManager, profile);
        pref.setEnabled(!mCachedDevice.isBusy());
        refreshProfilePreference(pref, profile);
        return pref;
    }
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (TextUtils.isEmpty(key) || newValue == null) return true;
        if (key.equals(KEY_ONLINE_MODE)) {
            onOnlineModeCheckedStateChanged((Boolean) newValue);
        } else {
            Profile profile = getProfileOf(preference);
            if (profile == null) return false;
            onProfileCheckedStateChanged(profile, (Boolean) newValue);
        }
        return true;
    }
    private void onOnlineModeCheckedStateChanged(boolean checked) {
        setOnlineMode(checked, true);
    }
    private void onProfileCheckedStateChanged(Profile profile, boolean checked) {
        if (mOnlineMode) {
            if (checked) {
                mCachedDevice.connect(profile);
            } else {
                mCachedDevice.disconnect(profile);
            }
        }
        LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                .getProfileManager(mManager, profile);
        profileManager.setPreferred(mCachedDevice.getDevice(), checked);
    }
    public void onDeviceAttributesChanged(CachedBluetoothDevice cachedDevice) {
        refresh();
    }
    private void refresh() {
        setOnlineMode(mCachedDevice.isConnected() || mCachedDevice.isBusy(), false);
        refreshProfiles();
    }
    private void setOnlineMode(boolean onlineMode, boolean takeAction) {
        mOnlineMode = onlineMode;
        if (takeAction) {
            if (onlineMode) {
                mCachedDevice.connect();
            } else {
                mCachedDevice.disconnect();
            }
        }
        refreshOnlineModePreference();
    }
    private void refreshOnlineModePreference() {
        mOnlineModePreference.setChecked(mOnlineMode);
        mOnlineModePreference.setEnabled(!mCachedDevice.isBusy());
        mOnlineModePreference.setSummary(mOnlineMode ? mCachedDevice.getSummary()
                : R.string.bluetooth_device_advanced_online_mode_summary);
    }
    private void refreshProfiles() {
        for (Profile profile : mCachedDevice.getConnectableProfiles()) {
            CheckBoxPreference profilePref =
                    (CheckBoxPreference) findPreference(profile.toString());
            if (profilePref == null) {
                profilePref = createProfilePreference(profile);
                mProfileContainer.addPreference(profilePref);
            } else {
                refreshProfilePreference(profilePref, profile);
            }
        }
    }
    private void refreshProfilePreference(CheckBoxPreference profilePref, Profile profile) {
        BluetoothDevice device = mCachedDevice.getDevice();
        LocalBluetoothProfileManager profileManager = LocalBluetoothProfileManager
                .getProfileManager(mManager, profile);
        int connectionStatus = profileManager.getConnectionStatus(device);
        profilePref.setEnabled(!mCachedDevice.isBusy());
        profilePref.setSummary(getProfileSummary(profileManager, profile, device,
                connectionStatus, mOnlineMode));
        profilePref.setChecked(profileManager.isPreferred(device));
    }
    private Profile getProfileOf(Preference pref) {
        if (!(pref instanceof CheckBoxPreference)) return null;
        String key = pref.getKey();
        if (TextUtils.isEmpty(key)) return null;
        try {
            return Profile.valueOf(pref.getKey());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    private static int getProfileSummary(LocalBluetoothProfileManager profileManager,
            Profile profile, BluetoothDevice device, int connectionStatus, boolean onlineMode) {
        if (!onlineMode || connectionStatus == SettingsBtStatus.CONNECTION_STATUS_DISCONNECTED) {
            return getProfileSummaryForSettingPreference(profile);
        } else {
            return profileManager.getSummary(device);
        }
    }
    private static final int getProfileSummaryForSettingPreference(Profile profile) {
        switch (profile) {
            case A2DP:
                return R.string.bluetooth_a2dp_profile_summary_use_for;
            case HEADSET:
                return R.string.bluetooth_headset_profile_summary_use_for;
            default:
                return 0;
        }
    }
}
