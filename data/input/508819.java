@TestTargetClass(SimpleOnGestureListener.class)
public class GestureDetector_SimpleOnGestureListenerTest extends
        ActivityInstrumentationTestCase2<GestureDetectorStubActivity> {
    private GestureDetectorStubActivity mActivity;
    public GestureDetector_SimpleOnGestureListenerTest() {
        super("com.android.cts.stub", GestureDetectorStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test Constructor ",
        method = "GestureDetector.SimpleOnGestureListener",
        args = {}
    )
    public void testConstructor() {
        new SimpleOnGestureListener();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSingleTapUp",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onLongPress",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDown",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onScroll",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onShowPress",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onFling",
            args = {MotionEvent.class, MotionEvent.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDoubleTap",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDoubleTapEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSingleTapConfirmed",
            args = {MotionEvent.class}
        )
    })
    public void testSimpleOnGestureListener() {
        GestureDetectorTestUtil.testGestureDetector(this, mActivity);
    }
}
