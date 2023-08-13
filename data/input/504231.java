@TestTargetClass(WifiConfiguration.class)
public class WifiConfigurationTest extends AndroidTestCase {
    private  WifiManager mWifiManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWifiManager = (WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "WifiConfiguration",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "toString",
            args = {}
        )
    })
    public void testWifiConfiguration() {
        List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();
        for (int i = 0; i < wifiConfigurations.size(); i++) {
            WifiConfiguration wifiConfiguration = wifiConfigurations.get(i);
            assertNotNull(wifiConfiguration);
            assertNotNull(wifiConfiguration.toString());
        }
    }
}
