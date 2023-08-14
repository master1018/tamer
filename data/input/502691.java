@TestTargetClass(Display.class)
public class DisplayTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDisplayId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "don't know what orientation the default display has",
            method = "getOrientation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMetrics",
            args = {DisplayMetrics.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPixelFormat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRefreshRate",
            args = {}
        )
    })
    @ToBeFixed(bug="1695243", explanation="don't know what orientation the default display has")
    public void testGetDisplayAttrs() {
        Context con = getContext();
        WindowManager windowManager = (WindowManager) con.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        assertEquals(Display.DEFAULT_DISPLAY, display.getDisplayId());
        assertTrue(0 < display.getHeight());
        assertTrue(0 < display.getWidth());
        display.getOrientation();
        assertTrue(0 < display.getPixelFormat());
        assertTrue(0 < display.getRefreshRate());
        DisplayMetrics outMetrics = new DisplayMetrics();
        outMetrics.setToDefaults();
        display.getMetrics(outMetrics);
        assertEquals(display.getHeight(), outMetrics.heightPixels);
        assertEquals(display.getWidth(), outMetrics.widthPixels);
        assertTrue(0.1f <= outMetrics.density && outMetrics.density <= 3.0f);
        assertTrue(0.1f <= outMetrics.scaledDensity && outMetrics.density <= 3.0f);
        assertTrue(0 < outMetrics.xdpi);
        assertTrue(0 < outMetrics.ydpi);
    }
}
