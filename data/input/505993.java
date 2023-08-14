public class ConnectivityManagerPermissionTest extends AndroidTestCase {
    private ConnectivityManager mConnectivityManager = null;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assertNotNull(mConnectivityManager);
    }
    @SmallTest
    public void testGetNetworkInfo() {
        try {
            mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            fail("Was able to call getNetworkInfo");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetNetworkPreference() {
        try {
            mConnectivityManager.getNetworkPreference();
            fail("Was able to call getNetworkPreference");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testRequestRouteToHost() {
        try {
            mConnectivityManager.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, 1);
            fail("Was able to call requestRouteToHost");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testSetNetworkPreference() {
        try {
            mConnectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
            fail("Was able to call setNetworkPreference");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testStartUsingNetworkPreference() {
        try {
            mConnectivityManager.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
            fail("Was able to call setNetworkPreference");
        } catch (SecurityException e) {
        }
    }
}
