public class WifiAPITest extends PreferenceActivity implements
Preference.OnPreferenceClickListener {
    private static final String TAG = "WifiAPITest";
    private int netid;
    private static final String KEY_DISCONNECT = "disconnect";
    private static final String KEY_DISABLE_NETWORK = "disable_network";
    private static final String KEY_ENABLE_NETWORK = "enable_network";
    private Preference mWifiDisconnect;
    private Preference mWifiDisableNetwork;
    private Preference mWifiEnableNetwork;
    private WifiManager mWifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreatePreferences();
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    }
    private void onCreatePreferences() {
        addPreferencesFromResource(R.layout.wifi_api_test);
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        mWifiDisconnect = (Preference) preferenceScreen.findPreference(KEY_DISCONNECT);
        mWifiDisconnect.setOnPreferenceClickListener(this);
        mWifiDisableNetwork = (Preference) preferenceScreen.findPreference(KEY_DISABLE_NETWORK);
        mWifiDisableNetwork.setOnPreferenceClickListener(this);
        mWifiEnableNetwork = (Preference) preferenceScreen.findPreference(KEY_ENABLE_NETWORK);
        mWifiEnableNetwork.setOnPreferenceClickListener(this);
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        return false;
    }
    public boolean onPreferenceClick(Preference pref) {
        if (pref == mWifiDisconnect) {
            mWifiManager.disconnect();
        } else if (pref == mWifiDisableNetwork) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    netid = Integer.parseInt(value.toString());
                    mWifiManager.disableNetwork(netid);
                    }
                    });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                    });
            alert.show();
        } else if (pref == mWifiEnableNetwork) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Input");
            alert.setMessage("Enter Network ID");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    Editable value = input.getText();
                    netid =  Integer.parseInt(value.toString());
                    mWifiManager.enableNetwork(netid, false);
                    }
                    });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                    });
            alert.show();
        }
        return true;
    }
}
