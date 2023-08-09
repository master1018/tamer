public class PowerManagerTest extends AndroidTestCase {
    private PowerManager mPm;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mPm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    }
    @MediumTest
    public void testPreconditions() throws Exception {
        assertNotNull(mPm);
    }
    @MediumTest
    public void testNewWakeLock() throws Exception {
        PowerManager.WakeLock wl = mPm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "FULL_WAKE_LOCK");
        doTestWakeLock(wl);
        wl = mPm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "SCREEN_BRIGHT_WAKE_LOCK");
        doTestWakeLock(wl);
        wl = mPm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SCREEN_DIM_WAKE_LOCK");
        doTestWakeLock(wl);
        wl = mPm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PARTIAL_WAKE_LOCK");
        doTestWakeLock(wl);
        doTestSetBacklightBrightness();
}
    @MediumTest
    public void testBadNewWakeLock() throws Exception {
        final int badFlags = PowerManager.SCREEN_BRIGHT_WAKE_LOCK 
                            | PowerManager.SCREEN_DIM_WAKE_LOCK;
        try {
            PowerManager.WakeLock wl = mPm.newWakeLock(badFlags, "foo");
        } catch (IllegalArgumentException e) {
            return;
        }
        fail("Bad WakeLock flag was not caught.");
    }
    private void doTestWakeLock(PowerManager.WakeLock wl) {
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
    }
    private void doTestSetBacklightBrightness() {
        try {
            mPm.setBacklightBrightness(0);
            fail("setBacklights did not throw SecurityException as expected");
        } catch (SecurityException e) {
        }
    }
}
