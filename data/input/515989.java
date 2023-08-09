public class ConnectivityManagerMobileTest
    extends ActivityInstrumentationTestCase2<ConnectivityManagerTestActivity> {
    private static final String LOG_TAG = "ConnectivityManagerMobileTest";
    private static final String PKG_NAME = "com.android.connectivitymanagertest";
    private static final long STATE_TRANSITION_SHORT_TIMEOUT = 5 * 1000;
    private static final long STATE_TRANSITION_LONG_TIMEOUT = 30 * 1000;
    private String TEST_ACCESS_POINT;
    private ConnectivityManagerTestActivity cmActivity;
    private WakeLock wl;
    public ConnectivityManagerMobileTest() {
        super(PKG_NAME, ConnectivityManagerTestActivity.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        cmActivity = getActivity();
        ConnectivityManagerTestRunner mRunner =
                (ConnectivityManagerTestRunner)getInstrumentation();
        TEST_ACCESS_POINT = mRunner.TEST_SSID;
        PowerManager pm = (PowerManager)getInstrumentation().
                getContext().getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "CMWakeLock");
        wl.acquire();
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        verifyCellularConnection();
    }
    @Override
    public void tearDown() throws Exception {
        cmActivity.finish();
        Log.v(LOG_TAG, "tear down ConnectivityManagerTestActivity");
        wl.release();
        cmActivity.clearWifi();
        super.tearDown();
    }
    public void verifyCellularConnection() {
        NetworkInfo extraNetInfo = cmActivity.mNetworkInfo;
        assertEquals("network type is not MOBILE", ConnectivityManager.TYPE_MOBILE,
            extraNetInfo.getType());
        assertTrue("not connected to cellular network", extraNetInfo.isConnected());
        assertTrue("no data connection", cmActivity.mState.equals(State.CONNECTED));
    }
    private void waitForNetworkState(int networkType, State expectedState, long timeout) {
        long startTime = System.currentTimeMillis();
        if (cmActivity.mCM.getNetworkInfo(networkType).getState() == expectedState) {
            return;
        } else {
            while (true) {
                if ((System.currentTimeMillis() - startTime) > timeout) {
                    assertFalse("Wait for network state timeout", true);
                }
                Log.v(LOG_TAG, "Wait for the connectivity state for network: " + networkType +
                        " to be " + expectedState.toString());
                synchronized (cmActivity.connectivityObject) {
                    try {
                        cmActivity.connectivityObject.wait(STATE_TRANSITION_SHORT_TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ((cmActivity.mNetworkInfo.getType() != networkType) ||
                        (cmActivity.mNetworkInfo.getState() != expectedState)) {
                        Log.v(LOG_TAG, "network state for " + cmActivity.mNetworkInfo.getType() +
                                "is: " + cmActivity.mNetworkInfo.getState());
                        continue;
                    }
                    break;
                }
            }
        }
    }
    private void waitForWifiState(int expectedState, long timeout) {
        long startTime = System.currentTimeMillis();
        if (cmActivity.mWifiState == expectedState) {
            return;
        } else {
            while (true) {
                if ((System.currentTimeMillis() - startTime) > timeout) {
                    assertFalse("Wait for Wifi state timeout", true);
                }
                Log.v(LOG_TAG, "Wait for wifi state to be: " + expectedState);
                synchronized (cmActivity.wifiObject) {
                    try {
                        cmActivity.wifiObject.wait(5*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (cmActivity.mWifiState != expectedState) {
                        Log.v(LOG_TAG, "Wifi state is: " + cmActivity.mWifiNetworkInfo.getState());
                        continue;
                    }
                    break;
                }
            }
        }
    }
    @LargeTest
    public void test3GToWifiNotification() {
        cmActivity.enableWifi();
        try {
            Thread.sleep(2 * STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        cmActivity.disableWifi();
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE, networkInfo.getState(),
                NetworkState.DO_NOTHING, State.CONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                NetworkState.DO_NOTHING, State.DISCONNECTED);
        cmActivity.enableWifi();
        try {
            Thread.sleep(2 * STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "the state for WIFI is changed");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue("state validation fail", false);
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_MOBILE)) {
            Log.v(LOG_TAG, "the state for MOBILE is changed");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_MOBILE));
            assertTrue("state validation fail", false);
        }
        verifyCellularConnection();
    }
    @LargeTest
    public void testConnectToWifi() {
        assertNotNull("SSID is null", TEST_ACCESS_POINT);
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE, networkInfo.getState(),
                NetworkState.TO_DISCONNECTION, State.DISCONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                NetworkState.TO_CONNECTION, State.CONNECTED);
        assertTrue("failed to connect to " + TEST_ACCESS_POINT,
                cmActivity.connectToWifi(TEST_ACCESS_POINT));
        waitForWifiState(WifiManager.WIFI_STATE_ENABLED, STATE_TRANSITION_LONG_TIMEOUT);
        Log.v(LOG_TAG, "wifi state is enabled");
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "Wifi state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue(false);
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_MOBILE)) {
            Log.v(LOG_TAG, "Mobile state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_MOBILE));
            assertTrue(false);
        }
    }
    @LargeTest
    public void testConnectToWifWithKnownAP() {
        assertNotNull("SSID is null", TEST_ACCESS_POINT);
        assertTrue("failed to connect to " + TEST_ACCESS_POINT,
                cmActivity.connectToWifi(TEST_ACCESS_POINT));
        waitForWifiState(WifiManager.WIFI_STATE_ENABLED, STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        try {
            Thread.sleep(STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        Log.v(LOG_TAG, "Disable Wifi");
        if (!cmActivity.disableWifi()) {
            Log.v(LOG_TAG, "disable Wifi failed");
            return;
        }
        waitForWifiState(WifiManager.WIFI_STATE_DISABLED, STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE,
                                              networkInfo.getState(), NetworkState.DO_NOTHING,
                                              State.DISCONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                NetworkState.TO_CONNECTION, State.CONNECTED);
        Log.v(LOG_TAG, "Enable Wifi again");
        cmActivity.enableWifi();
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "Wifi state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue(false);
        }
    }
    @LargeTest
    public void testDisconnectWifi() {
        assertNotNull("SSID is null", TEST_ACCESS_POINT);
        assertTrue("failed to connect to " + TEST_ACCESS_POINT,
                   cmActivity.connectToWifi(TEST_ACCESS_POINT));
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
            STATE_TRANSITION_LONG_TIMEOUT);
        try {
            Thread.sleep(STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE,
                                              networkInfo.getState(),
                                              NetworkState.TO_CONNECTION,
                                              State.CONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                NetworkState.TO_DISCONNECTION, State.DISCONNECTED);
        cmActivity.clearWifi();
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "Wifi state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue(false);
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_MOBILE)) {
            Log.v(LOG_TAG, "Mobile state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_MOBILE));
            assertTrue(false);
        }
    }
    @LargeTest
    public void testDataConnectionWith3GToAmTo3G() {
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE,
                                              networkInfo.getState(),
                                              NetworkState.TO_DISCONNECTION,
                                              State.DISCONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        assertEquals(State.DISCONNECTED, networkInfo.getState());
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                NetworkState.DO_NOTHING, State.DISCONNECTED);
        cmActivity.setAirplaneMode(getInstrumentation().getContext(), true);
        try {
            Thread.sleep(STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "Wifi state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue(false);
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_MOBILE)) {
            Log.v(LOG_TAG, "Mobile state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_MOBILE));
            assertTrue(false);
        }
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE,
                                              networkInfo.getState(),
                                              NetworkState.TO_CONNECTION,
                                              State.CONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                NetworkState.DO_NOTHING, State.DISCONNECTED);
        cmActivity.setAirplaneMode(getInstrumentation().getContext(), false);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_MOBILE)) {
            Log.v(LOG_TAG, "Mobile state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_MOBILE));
            assertTrue(false);
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
          Log.v(LOG_TAG, "Wifi state transition validation failed.");
          Log.v(LOG_TAG, "reason: " +
                  cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
          assertTrue(false);
        }
    }
    @LargeTest
    public void testDataConnectionOverAMWithWifi() {
        assertNotNull("SSID is null", TEST_ACCESS_POINT);
        cmActivity.setAirplaneMode(getInstrumentation().getContext(), true);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_MOBILE,
                                              networkInfo.getState(),
                                              NetworkState.DO_NOTHING,
                                              State.DISCONNECTED);
        networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI, networkInfo.getState(),
                                              NetworkState.TO_CONNECTION, State.CONNECTED);
        assertTrue("failed to connect to " + TEST_ACCESS_POINT,
                   cmActivity.connectToWifi(TEST_ACCESS_POINT));
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                            STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "state validate for Wifi failed");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue("State validation failed", false);
        }
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_MOBILE)) {
            Log.v(LOG_TAG, "state validation for Mobile failed");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_MOBILE));
            assertTrue("state validation failed", false);
        }
        cmActivity.setAirplaneMode(getInstrumentation().getContext(), false);
    }
    @LargeTest
    public void testDataConnectionWithWifiToAMToWifi () {
        assertNotNull("SSID is null", TEST_ACCESS_POINT);
        assertTrue("failed to connect to " + TEST_ACCESS_POINT,
                cmActivity.connectToWifi(TEST_ACCESS_POINT));
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        try {
            Thread.sleep(STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        cmActivity.setAirplaneMode(getInstrumentation().getContext(), true);
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        try {
            Thread.sleep(STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        NetworkInfo networkInfo = cmActivity.mCM.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        assertEquals(State.DISCONNECTED, networkInfo.getState());
        cmActivity.setStateTransitionCriteria(ConnectivityManager.TYPE_WIFI,
                networkInfo.getState(), NetworkState.TO_CONNECTION, State.CONNECTED);
        cmActivity.setAirplaneMode(getInstrumentation().getContext(), false);
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                            STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_MOBILE, State.DISCONNECTED,
                            STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.validateNetworkStates(ConnectivityManager.TYPE_WIFI)) {
            Log.v(LOG_TAG, "Wifi state transition validation failed.");
            Log.v(LOG_TAG, "reason: " +
                    cmActivity.getTransitionFailureReason(ConnectivityManager.TYPE_WIFI));
            assertTrue(false);
        }
    }
    @LargeTest
    public void testWifiStateChange () {
        assertNotNull("SSID is null", TEST_ACCESS_POINT);
        assertTrue("failed to connect to " + TEST_ACCESS_POINT,
                   cmActivity.connectToWifi(TEST_ACCESS_POINT));
        waitForWifiState(WifiManager.WIFI_STATE_ENABLED, STATE_TRANSITION_LONG_TIMEOUT);
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.CONNECTED,
                            STATE_TRANSITION_LONG_TIMEOUT);
        assertNotNull("Not associated with any AP",
                      cmActivity.mWifiManager.getConnectionInfo().getBSSID());
        try {
            Thread.sleep(STATE_TRANSITION_SHORT_TIMEOUT);
        } catch (Exception e) {
            Log.v(LOG_TAG, "exception: " + e.toString());
        }
        Log.v(LOG_TAG, "disconnect from the AP");
        if (!cmActivity.disconnectAP()) {
            Log.v(LOG_TAG, "failed to disconnect from " + TEST_ACCESS_POINT);
        }
        waitForNetworkState(ConnectivityManager.TYPE_WIFI, State.DISCONNECTED,
                STATE_TRANSITION_LONG_TIMEOUT);
        if (!cmActivity.disableWifi()) {
            Log.v(LOG_TAG, "disable Wifi failed");
            return;
        }
        waitForWifiState(WifiManager.WIFI_STATE_DISABLED, STATE_TRANSITION_LONG_TIMEOUT);
    }
}
