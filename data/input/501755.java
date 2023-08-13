@TestTargetClass(DisplayMetrics.class)
public class DisplayMetricsTest extends AndroidTestCase {
    private Display initDisplay() {
        WindowManager windowManager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        assertNotNull(windowManager);
        Display display = windowManager.getDefaultDisplay();
        assertNotNull(display);
        return display;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link DisplayMetrics}",
            method = "DisplayMetrics",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: setTo",
            method = "setTo",
            args = {DisplayMetrics.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: setToDefaults",
            method = "setToDefaults",
            args = {}
        )
    })
    public void testDisplayMetricsOp() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        outMetrics.setToDefaults();
        assertEquals(0, outMetrics.widthPixels);
        assertEquals(0, outMetrics.heightPixels);
        assertTrue((0.1 < outMetrics.density) && (outMetrics.density < 3));
        assertTrue((0.1 < outMetrics.scaledDensity) && (outMetrics.scaledDensity < 3));
        assertTrue(0 < outMetrics.xdpi);
        assertTrue(0 < outMetrics.ydpi);
        Display display = initDisplay();
        display.getMetrics(outMetrics);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setTo(outMetrics);
        assertEquals(display.getHeight(), metrics.heightPixels);
        assertEquals(display.getWidth(), metrics.widthPixels);
        assertTrue((0.1 < metrics.density) && (metrics.density < 3));
        assertTrue((0.1 < metrics.scaledDensity) && (metrics.scaledDensity < 3));
        assertTrue(0 < metrics.xdpi);
        assertTrue(0 < metrics.ydpi);
    }
}
