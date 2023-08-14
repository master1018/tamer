public class WirelessSettings extends PreferenceActivity {
    private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
    private static final String KEY_TOGGLE_BLUETOOTH = "toggle_bluetooth";
    private static final String KEY_TOGGLE_WIFI = "toggle_wifi";
    private static final String KEY_WIFI_SETTINGS = "wifi_settings";
    private static final String KEY_BT_SETTINGS = "bt_settings";
    private static final String KEY_VPN_SETTINGS = "vpn_settings";
    private static final String KEY_TETHER_SETTINGS = "tether_settings";
    public static final String EXIT_ECM_RESULT = "exit_ecm_result";
    public static final int REQUEST_CODE_EXIT_ECM = 1;
    private AirplaneModeEnabler mAirplaneModeEnabler;
    private CheckBoxPreference mAirplaneModePreference;
    private WifiEnabler mWifiEnabler;
    private BluetoothEnabler mBtEnabler;
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAirplaneModePreference && Boolean.parseBoolean(
                SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            startActivityForResult(
                new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null),
                REQUEST_CODE_EXIT_ECM);
            return true;
        }
        return false;
    }
    public static boolean isRadioAllowed(Context context, String type) {
        if (!AirplaneModeEnabler.isAirplaneModeOn(context)) {
            return true;
        }
        String toggleable = Settings.System.getString(context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        return toggleable != null && toggleable.contains(type);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wireless_settings);
        CheckBoxPreference airplane = (CheckBoxPreference) findPreference(KEY_TOGGLE_AIRPLANE);
        CheckBoxPreference wifi = (CheckBoxPreference) findPreference(KEY_TOGGLE_WIFI);
        CheckBoxPreference bt = (CheckBoxPreference) findPreference(KEY_TOGGLE_BLUETOOTH);
        mAirplaneModeEnabler = new AirplaneModeEnabler(this, airplane);
        mAirplaneModePreference = (CheckBoxPreference) findPreference(KEY_TOGGLE_AIRPLANE);
        mWifiEnabler = new WifiEnabler(this, wifi);
        mBtEnabler = new BluetoothEnabler(this, bt);
        String toggleable = Settings.System.getString(getContentResolver(),
                Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
        if (toggleable == null || !toggleable.contains(Settings.System.RADIO_WIFI)) {
            wifi.setDependency(KEY_TOGGLE_AIRPLANE);
            findPreference(KEY_WIFI_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
            findPreference(KEY_VPN_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
        }
        if (toggleable == null || !toggleable.contains(Settings.System.RADIO_BLUETOOTH)) {
            bt.setDependency(KEY_TOGGLE_AIRPLANE);
            findPreference(KEY_BT_SETTINGS).setDependency(KEY_TOGGLE_AIRPLANE);
        }
        if (ServiceManager.getService(BluetoothAdapter.BLUETOOTH_SERVICE) == null) {
            findPreference(KEY_BT_SETTINGS).setEnabled(false);
        }
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!cm.isTetheringSupported()) {
            getPreferenceScreen().removePreference(findPreference(KEY_TETHER_SETTINGS));
        } else {
            String[] usbRegexs = cm.getTetherableUsbRegexs();
            String[] wifiRegexs = cm.getTetherableWifiRegexs();
            Preference p = findPreference(KEY_TETHER_SETTINGS);
            if (wifiRegexs.length == 0) {
                p.setTitle(R.string.tether_settings_title_usb);
                p.setSummary(R.string.tether_settings_summary_usb);
            } else {
                if (usbRegexs.length == 0) {
                    p.setTitle(R.string.tether_settings_title_wifi);
                    p.setSummary(R.string.tether_settings_summary_wifi);
                } else {
                    p.setTitle(R.string.tether_settings_title_both);
                    p.setSummary(R.string.tether_settings_summary_both);
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAirplaneModeEnabler.resume();
        mWifiEnabler.resume();
        mBtEnabler.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mAirplaneModeEnabler.pause();
        mWifiEnabler.pause();
        mBtEnabler.pause();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EXIT_ECM) {
            Boolean isChoiceYes = data.getBooleanExtra(EXIT_ECM_RESULT, false);
            mAirplaneModeEnabler.setAirplaneModeInECM(isChoiceYes,
                    mAirplaneModePreference.isChecked());
        }
    }
}
