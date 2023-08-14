public class VibratorServicePermissionTest extends TestCase {
    private IVibratorService mVibratorService;
    @Override
    protected void setUp() throws Exception {
        mVibratorService = IVibratorService.Stub.asInterface(
                ServiceManager.getService("vibrator"));
    }
    public void testVibrate() throws RemoteException {
        try {
            mVibratorService.vibrate(2000, new Binder());
            fail("vibrate did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testVibratePattern() throws RemoteException {
        try {
            mVibratorService.vibratePattern(new long[] {0}, 0, new Binder());
            fail("vibratePattern did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
    public void testCancelVibrate() throws RemoteException {
        try {
            mVibratorService.cancelVibrate(new Binder());
            fail("cancelVibrate did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}
