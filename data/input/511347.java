@TestTargetClass(ZoomButton.class)
public class ZoomButtonTest extends ActivityInstrumentationTestCase2<ZoomButtonStubActivity> {
    private ZoomButton mZoomButton;
    private Activity mActivity;
    public ZoomButtonTest() {
        super("com.android.cts.stub", ZoomButtonStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mZoomButton = (ZoomButton) getActivity().findViewById(R.id.zoombutton_test);
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ZoomButton",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ZoomButton",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ZoomButton",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add @throws clause into javadoc.")
    public void testConstructor() {
        new ZoomButton(mActivity);
        new ZoomButton(mActivity, null);
        new ZoomButton(mActivity, null, 0);
        XmlPullParser parser = mActivity.getResources().getXml(R.layout.zoombutton_layout);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        assertNotNull(attrs);
        new ZoomButton(mActivity, attrs);
        new ZoomButton(mActivity, attrs, 0);
        try {
            new ZoomButton(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new ZoomButton(null, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            new ZoomButton(null, null, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setEnabled",
        args = {boolean.class}
    )
    public void testSetEnabled() {
        assertFalse(mZoomButton.isPressed());
        mZoomButton.setEnabled(true);
        assertTrue(mZoomButton.isEnabled());
        assertFalse(mZoomButton.isPressed());
        mZoomButton.setPressed(true);
        assertTrue(mZoomButton.isPressed());
        mZoomButton.setEnabled(true);
        assertTrue(mZoomButton.isEnabled());
        assertTrue(mZoomButton.isPressed());
        mZoomButton.setEnabled(false);
        assertFalse(mZoomButton.isEnabled());
        assertFalse(mZoomButton.isPressed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ZoomButton#dispatchUnhandledMove(View, int)}, " +
                "this function always returns false",
        method = "dispatchUnhandledMove",
        args = {android.view.View.class, int.class}
    )
    @UiThreadTest
    public void testDispatchUnhandledMove() {
        assertFalse(mZoomButton.dispatchUnhandledMove(new ListView(mActivity), View.FOCUS_DOWN));
        assertFalse(mZoomButton.dispatchUnhandledMove(null, View.FOCUS_DOWN));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link ZoomButton#onLongClick(View)}, " +
                "this function always returns true and the parameter 'View v' is ignored.",
        method = "onLongClick",
        args = {android.view.View.class}
    )
    public void testOnLongClick() {
        final MockOnClickListener listener = new MockOnClickListener();
        mZoomButton.setOnClickListener(listener);
        mZoomButton.setEnabled(true);
        long speed = 2000;
        mZoomButton.setZoomSpeed(speed);
        assertFalse(listener.hasOnClickCalled());
        mZoomButton.performLongClick();
        new DelayedCheck(speed + 500) {
            @Override
            protected boolean check() {
                return listener.hasOnClickCalled();
            }
        };
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onTouchEvent",
        args = {android.view.MotionEvent.class}
    )
    public void testOnTouchEvent() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onKeyUp",
        args = {int.class, android.view.KeyEvent.class}
    )
    public void testOnKeyUp() {
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "setZoomSpeed",
        args = {long.class}
    )
    @ToBeFixed(bug = "1400249", explanation = "how to check zoom speed after set.")
    public void testSetZoomSpeed() {
        mZoomButton.setZoomSpeed(100);
        mZoomButton.setZoomSpeed(-1);
    }
    private static class MockOnClickListener implements OnClickListener {
        private boolean mOnClickCalled = false;
        public boolean hasOnClickCalled() {
            return mOnClickCalled;
        }
        public void onClick(View v) {
            mOnClickCalled = true;
        }
    }
}
