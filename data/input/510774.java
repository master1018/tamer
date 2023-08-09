public class TelephonyManagerPermissionTest extends AndroidTestCase {
    TelephonyManager mTelephonyManager = null;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        assertNotNull(mTelephonyManager);
    }
    @SmallTest
    public void testGetDeviceId() {
        try {
            String id = mTelephonyManager.getDeviceId();
            fail("Got device ID: " + id);
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetLine1Number() {
        try {
            String nmbr = mTelephonyManager.getLine1Number();
            fail("Got line 1 number: " + nmbr);
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testGetSimSerialNumber() {
        try {
            String nmbr = mTelephonyManager.getSimSerialNumber();
            fail("Got SIM serial number: " + nmbr);
        } catch (SecurityException e) {
        }
    }
}
