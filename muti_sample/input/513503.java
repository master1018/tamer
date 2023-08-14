@TestTargetClass(ZoomControls.class)
public class ZoomControlsTest extends InstrumentationTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ZoomControls",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ZoomControls",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new ZoomControls(mContext);
        new ZoomControls(mContext, null);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setOnZoomInClickListener",
        args = {android.view.View.OnClickListener.class},
        notes = "not possible to trigger a zoom button click programmatically using public API"
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testSetOnZoomInClickListener() {
        ZoomControls zoomControls = new ZoomControls(mContext);
        final MockOnClickListener clickListener = new MockOnClickListener();
        zoomControls.setOnZoomInClickListener(clickListener);
        zoomControls.setOnZoomInClickListener(null);
    }
    private class MockOnClickListener implements OnClickListener {
        public void onClick(View v) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setOnZoomOutClickListener",
        args = {android.view.View.OnClickListener.class},
        notes = "not possible to trigger a zoom button click programmatically using public API"
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testSetOnZoomOutClickListener() {
        ZoomControls zoomControls = new ZoomControls(mContext);
        final MockOnClickListener clickListener = new MockOnClickListener();
        zoomControls.setOnZoomOutClickListener(clickListener);
        zoomControls.setOnZoomOutClickListener(null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "setZoomSpeed",
        args = {long.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "how to check zoom speed after set.")
    public void testSetZoomSpeed() {
        ZoomControls zoomControls = new ZoomControls(mContext);
        zoomControls.setZoomSpeed(500);
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "this method always return true",
        method = "onTouchEvent",
        args = {android.view.MotionEvent.class}
    )
    public void testOnTouchEvent() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hide",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testShowAndHide() {
        final ZoomControls zoomControls = new ZoomControls(mContext);
        assertEquals(View.VISIBLE, zoomControls.getVisibility());
        zoomControls.hide();
        assertEquals(View.GONE, zoomControls.getVisibility());
        zoomControls.show();
        assertEquals(View.VISIBLE, zoomControls.getVisibility());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setIsZoomInEnabled",
        args = {boolean.class},
        notes="not feasible to test effect of calling this method"
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testSetIsZoomInEnabled() {
        ZoomControls zoomControls = new ZoomControls(mContext);
        zoomControls.setIsZoomInEnabled(false);
        zoomControls.setIsZoomInEnabled(true);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "setIsZoomOutEnabled",
        args = {boolean.class},
        notes="not feasible to test effect of calling this method"
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testSetIsZoomOutEnabled() {
        ZoomControls zoomControls = new ZoomControls(mContext);
        zoomControls.setIsZoomOutEnabled(false);
        zoomControls.setIsZoomOutEnabled(true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasFocus",
        args = {}
    )
    @UiThreadTest
    public void testHasFocus() {
        ZoomControls zoomControls = new ZoomControls(mContext);
        assertFalse(zoomControls.hasFocus());
        zoomControls.requestFocus();
        assertTrue(zoomControls.hasFocus());
    }
}
