@TestTargetClass(WifiManager.WifiLock.class)
public class WifiManager_WifiLockTest extends AndroidTestCase {
    private static final String WIFI_TAG = "WifiManager_WifiLockTest";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "acquire",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "finalize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHeld",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setReferenceCounted",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        )
    })
    public void testWifiLock() {
        WifiManager wm = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiLock wl = wm.createWifiLock(WIFI_TAG);
        wl.setReferenceCounted(true);
        assertFalse(wl.isHeld());
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        wl.acquire();
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        assertNotNull(wl.toString());
        try {
            wl.release();
            fail("should throw out exception because release is called"
                    +" a greater number of times than acquire");
        } catch (RuntimeException e) {
        }
        wl = wm.createWifiLock(WIFI_TAG);
        wl.setReferenceCounted(false);
        assertFalse(wl.isHeld());
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        wl.acquire();
        wl.acquire();
        assertTrue(wl.isHeld());
        wl.release();
        assertFalse(wl.isHeld());
        assertNotNull(wl.toString());
        wl.release();
    }
}
