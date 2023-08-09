public class NoNetworkStatePermissionTest extends AndroidTestCase {
    private ConnectivityManager mConnectivityManager;
    private static final int TEST_NETWORK_TYPE = 1;
    private static final int TEST_PREFERENCE = 1;
    private static final String TEST_FEATURE = "feature";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        assertNotNull(mConnectivityManager);
    }
    @SmallTest
    public void testGetNetworkPreference() {
        try {
            mConnectivityManager.getNetworkPreference();
            fail("ConnectivityManager.getNetworkPreference didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetActiveNetworkInfo() {
        try {
            mConnectivityManager.getActiveNetworkInfo();
            fail("ConnectivityManager.getActiveNetworkInfo didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetNetworkInfo() {
        try {
            mConnectivityManager.getNetworkInfo(TEST_NETWORK_TYPE);
            fail("ConnectivityManager.getNetworkInfo didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetAllNetworkInfo() {
        try {
            mConnectivityManager.getAllNetworkInfo();
            fail("ConnectivityManager.getAllNetworkInfo didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetNetworkPreference() {
        try {
            mConnectivityManager.setNetworkPreference(TEST_PREFERENCE);
            fail("ConnectivityManager.setNetworkPreference didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testStartUsingNetworkFeature() {
        try {
            mConnectivityManager.startUsingNetworkFeature(TEST_NETWORK_TYPE, TEST_FEATURE);
            fail("ConnectivityManager.startUsingNetworkFeature didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testStopUsingNetworkFeature() {
        try {
            mConnectivityManager.stopUsingNetworkFeature(TEST_NETWORK_TYPE, TEST_FEATURE);
            fail("ConnectivityManager.stopUsingNetworkFeature didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testRequestRouteToHost() {
        try {
            mConnectivityManager.requestRouteToHost(TEST_NETWORK_TYPE, 0xffffffff);
            fail("ConnectivityManager.requestRouteToHost didn't throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
}
