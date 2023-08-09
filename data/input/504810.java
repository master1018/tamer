@TestTargetClass(GestureDetector.class)
public class GestureDetectorTest extends
        ActivityInstrumentationTestCase2<GestureDetectorStubActivity> {
    private GestureDetector mGestureDetector;
    private GestureDetectorStubActivity mActivity;
    private Context mContext;
    public GestureDetectorTest() {
        super("com.android.cts.stub", GestureDetectorStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mGestureDetector = mActivity.getGestureDetector();
        mContext = getInstrumentation().getTargetContext();
        mActivity.isDown = false;
        mActivity.isScroll = false;
        mActivity.isFling = false;
        mActivity.isSingleTapUp = false;
        mActivity.onShowPress = false;
        mActivity.onLongPress = false;
        mActivity.onDoubleTap = false;
        mActivity.onDoubleTapEvent = false;
        mActivity.onSingleTapConfirmed = false;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {OnGestureListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {OnGestureListener.class, Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {Context.class, OnGestureListener.class, Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method GestureDetector",
            method = "GestureDetector",
            args = {Context.class, OnGestureListener.class}
         )
    })
    public void testConstructor() {
        new GestureDetector(mContext, new SimpleOnGestureListener(), new Handler());
        new GestureDetector(mContext, new SimpleOnGestureListener());
        new GestureDetector(new SimpleOnGestureListener(), new Handler());
        new GestureDetector(new SimpleOnGestureListener());
        try {
            mGestureDetector = new GestureDetector(null);
            fail("should throw null exception");
        } catch (RuntimeException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method onTouchEvent",
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test method onTouchEvent",
            method = "setOnDoubleTapListener",
            args = {OnDoubleTapListener.class}
        )
    })
    public void testOnTouchEvent() {
        GestureDetectorTestUtil.testGestureDetector(this, mActivity);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setIsLongpressEnabled",
            method = "setIsLongpressEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "test setIsLongpressEnabled",
            method = "isLongpressEnabled",
            args = {}
        )
    })
    public void testLongpressEnabled() {
        mGestureDetector.setIsLongpressEnabled(true);
        assertTrue(mGestureDetector.isLongpressEnabled());
        mGestureDetector.setIsLongpressEnabled(false);
        assertFalse(mGestureDetector.isLongpressEnabled());
    }
}
