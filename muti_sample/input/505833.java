@TestTargetClass(KeyguardManager.KeyguardLock.class)
public class KeyguardManagerKeyguardLockTest extends InstrumentationTestCase {
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no method to enable the key guard in the emulator",
        method = "disableKeyguard",
        args = {}
    )
    public void testDisableKeyguard() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "There is no method to enable the key guard in the emulator",
        method = "reenableKeyguard",
        args = {}
    )
    public void testReenableKeyguard() {
    }
}
