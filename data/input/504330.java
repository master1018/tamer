public class DevicePowerPermissionTest extends AndroidTestCase {
    PowerManager mPowerManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    }
    @LargeTest
    public void testGoToSleep() {
        try {
            mPowerManager.goToSleep(0);
            fail("Was able to call PowerManager.goToSleep without DEVICE_POWER Permission.");
        } catch (SecurityException e) {
        }
    }
    @KnownFailure("will be fixed in future release")
    public void testUserActivity() {
        try {
            mPowerManager.userActivity(0, false);
            fail("Was able to call PowerManager.userActivity without DEVICE_POWER Permission.");
        } catch (SecurityException e) {
        }
    }
}
