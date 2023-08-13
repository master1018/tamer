public class RebootPermissionTest extends AndroidTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @SmallTest
    public void testBroadcastReboot() {
        try {
            mContext.sendBroadcast(new Intent(Intent.ACTION_REBOOT));
        } catch (SecurityException e) {
        }
    }
}
