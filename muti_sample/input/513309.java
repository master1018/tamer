@TestTargetClass(KeyguardManager.class)
public class KeyguardManagerTest
        extends ActivityInstrumentationTestCase2<KeyguardManagerActivity> {
    private static final String TAG = "KeyguardManagerTest";
    public KeyguardManagerTest() {
        super("com.android.cts.stub", KeyguardManagerActivity.class);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test newKeyguardLock",
        method = "newKeyguardLock",
        args = {java.lang.String.class}
    )
    public void testNewKeyguardLock() {
        final Context c = getInstrumentation().getContext();
        final KeyguardManager keyguardManager = (KeyguardManager) c.getSystemService(
                Context.KEYGUARD_SERVICE);
        final KeyguardManager.KeyguardLock keyLock = keyguardManager.newKeyguardLock(TAG);
        assertNotNull(keyLock);
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no method to enable the key guard in the emulator",
        method = "inKeyguardRestrictedInputMode",
        args = {}
    )
    public void testInKeyguardRestrictedInputMode() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no method to enable the key guard in the emulator",
        method = "exitKeyguardSecurely",
        args = {android.app.KeyguardManager.OnKeyguardExitResult.class}
    )
    public void testExitKeyguardSecurely() {
    }
}
