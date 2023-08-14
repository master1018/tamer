@TestTargetClass(PowerManager.WakeLock.class)
public class PowerManager_WakeLockTest extends AndroidTestCase {
    private static final String TAG = "PowerManager_WakeLockTest";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: acquire",
            method = "acquire",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: setReferenceCounted",
            method = "setReferenceCounted",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: isHeld",
            method = "isHeld",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: toString",
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Test method: release",
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Test method: acquire",
            method = "acquire",
            args = {long.class}
        )
    })
    public void testPowerManagerWakeLock() throws InterruptedException {
        PowerManager pm = (PowerManager)  getContext().getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        assertNotNull(wl.toString());
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        wl.setReferenceCounted(true);
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        wl.setReferenceCounted(false);
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        wl.acquire(PowerManagerTest.TIME);
        assertTrue(wl.isHeld());
        Thread.sleep(PowerManagerTest.TIME + PowerManagerTest.MORE_TIME);
        assertFalse(wl.isHeld());
    }
}
