public class NoWifiStatePermissionTest extends AndroidTestCase {
    private static final int TEST_NET_ID = 1;
    private static final WifiConfiguration TEST_WIFI_CONFIGURATION = new WifiConfiguration();
    private WifiManager mWifiManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        assertNotNull(mWifiManager);
    }
    public void testGetWifiState() {
        try {
            mWifiManager.getWifiState();
            fail("WifiManager.getWifiState didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testGetConfiguredNetworks() {
        try {
            mWifiManager.getConfiguredNetworks();
            fail("WifiManager.getConfiguredNetworks didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testGetConnectionInfo() {
        try {
            mWifiManager.getConnectionInfo();
            fail("WifiManager.getConnectionInfo didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testGetScanResults() {
        try {
            mWifiManager.getScanResults();
            fail("WifiManager.getScanResults didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testGetDhcpInfo() {
        try {
            mWifiManager.getDhcpInfo();
            fail("WifiManager.getDhcpInfo didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testDisconnect() {
        try {
            mWifiManager.disconnect();
            fail("WifiManager.disconnect didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testReconnect() {
        try {
            mWifiManager.reconnect();
            fail("WifiManager.reconnect didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testReassociate() {
        try {
            mWifiManager.reassociate();
            fail("WifiManager.reassociate didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testAddNetwork() {
        try {
            mWifiManager.addNetwork(TEST_WIFI_CONFIGURATION);
            fail("WifiManager.addNetwork didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testUpdateNetwork() {
        TEST_WIFI_CONFIGURATION.networkId = 2;
        try {
            mWifiManager.updateNetwork(TEST_WIFI_CONFIGURATION);
            fail("WifiManager.updateNetwork didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testRemoveNetwork() {
        try {
            mWifiManager.removeNetwork(TEST_NET_ID);
            fail("WifiManager.removeNetwork didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testEnableNetwork() {
        try {
            mWifiManager.enableNetwork(TEST_NET_ID, false);
            fail("WifiManager.enableNetwork didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testDisableNetwork() {
        try {
            mWifiManager.disableNetwork(TEST_NET_ID);
            fail("WifiManager.disableNetwork didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testSaveConfiguration() {
        try {
            mWifiManager.saveConfiguration();
            fail("WifiManager.saveConfiguration didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testPingSupplicant() {
        try {
            mWifiManager.pingSupplicant();
            fail("WifiManager.pingSupplicant didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testStartScan() {
        try {
            mWifiManager.startScan();
            fail("WifiManager.startScan didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testSetWifiEnabled() {
        try {
            mWifiManager.setWifiEnabled(true);
            fail("WifiManager.setWifiEnabled didn't throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}
