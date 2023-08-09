public class NoKeyPermissionTest extends AndroidTestCase {
    KeyguardManager  mKeyManager;
    KeyguardManager.KeyguardLock mKeyLock;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mKeyManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyLock = mKeyManager.newKeyguardLock("testTag");
    }
    @SmallTest
    public void testDisableKeyguard() {
        try {
            mKeyLock.disableKeyguard();
            fail("KeyguardManager.KeyguardLock.disableKeyguard did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testReenableKeyguard() {
        try {
            mKeyLock.reenableKeyguard();
            fail("KeyguardManager.KeyguardLock.reenableKeyguard did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
    @SmallTest
    public void testExitKeyguardSecurely() {
        try {
            mKeyManager.exitKeyguardSecurely(null);
            fail("KeyguardManager.exitKeyguardSecurely did not throw SecurityException as"
                    + " expected");
        } catch (SecurityException e) {
        }
    }
}
