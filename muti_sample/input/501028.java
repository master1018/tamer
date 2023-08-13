public class WifiConfigInfo extends Activity {
    private static final String TAG = "WifiConfigInfo";
    private TextView mConfigList;
    private WifiManager mWifiManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        setContentView(R.layout.wifi_config_info);
        mConfigList = (TextView) findViewById(R.id.config_list);
    }
    @Override
    protected void onResume() {
        super.onResume();
        final List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();
        StringBuffer configList  = new StringBuffer();
        for (int i = wifiConfigs.size() - 1; i >= 0; i--) {
            configList.append(wifiConfigs.get(i));
        }
        mConfigList.setText(configList);
    }
}
