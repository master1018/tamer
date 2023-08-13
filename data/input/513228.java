@TestTargetClass(TouchDelegate.class)
public class TouchDelegateTest extends ActivityInstrumentationTestCase2<MockActivity> {
    private static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    private static final int ACTION_DOWN = MotionEvent.ACTION_DOWN;
    private static final int ACTION_UP = MotionEvent.ACTION_UP;
    private static final int ACTION_CANCEL = MotionEvent.ACTION_CANCEL;
    private static final int ACTION_MOVE = MotionEvent.ACTION_MOVE;
    private ViewConfiguration mViewConfig;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TouchDelegate mTouchDelegate;
    private Button mButton;
    private Rect mRect;
    private int mXInside;
    private int mYInside;
    private int mXOutside;
    private int mYOutside;
    private int mScaledTouchSlop;
    private MotionEvent mActionDownInside;
    private MotionEvent mActionDownOutside;
    private MotionEvent mActionUpInside;
    private MotionEvent mActionUpOutside;
    private MotionEvent mActionMoveInside;
    private MotionEvent mActionMoveOutside;
    private MotionEvent mActionCancelInside;
    private MotionEvent mActionCancelOutside;
    private Exception mException;
    public TouchDelegateTest() {
        super("com.android.cts.stub", MockActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mViewConfig = ViewConfiguration.get(mActivity);
        mButton = new Button(mActivity);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                try {
                    mActivity.addContentView(mButton, new LinearLayout.LayoutParams(WRAP_CONTENT,
                                                                                    WRAP_CONTENT));
                } catch (Exception e) {
                    mException = e;
                }
            }
        });
        mInstrumentation.waitForIdleSync();
        if(mException != null) {
            throw mException;
        }
        int right = mButton.getRight();
        int bottom = mButton.getBottom();
        mXInside = (mButton.getLeft() + right) / 3;
        mYInside = (mButton.getTop() + bottom) / 3;
        mScaledTouchSlop = mViewConfig.getScaledTouchSlop() << 1;
        mXOutside = right + mScaledTouchSlop;
        mYOutside = bottom + mScaledTouchSlop;
        mRect = new Rect();
        mButton.getHitRect(mRect);
    }
    private void init() {
        mTouchDelegate = new TouchDelegate(mRect, mButton);
        mActionDownInside = MotionEvent.obtain(0, 0, ACTION_DOWN, mXInside, mYInside, 0);
        mActionDownOutside = MotionEvent.obtain(0, 0, ACTION_DOWN, mXOutside, mYOutside, 0);
        mActionUpInside = MotionEvent.obtain(0, 0, ACTION_UP, mXInside, mYInside, 0);
        mActionUpOutside = MotionEvent.obtain(0, 0, ACTION_UP, mXOutside, mYOutside, 0);
        mActionMoveInside = MotionEvent.obtain(0, 0, ACTION_MOVE, mXInside, mYInside, 0);
        mActionMoveOutside = MotionEvent.obtain(0, 0, ACTION_MOVE, mXOutside, mYOutside, 0);
        mActionCancelInside = MotionEvent.obtain(0, 0, ACTION_CANCEL, mXInside, mYInside, 0);
        mActionCancelOutside = MotionEvent.obtain(0, 0, ACTION_CANCEL, mXOutside, mYOutside, 0);
    }
    private void clear() {
        mActionDownInside.recycle();
        mActionDownOutside.recycle();
        mActionUpInside.recycle();
        mActionUpOutside.recycle();
        mActionMoveInside.recycle();
        mActionMoveOutside.recycle();
        mActionCancelInside.recycle();
        mActionCancelOutside.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link TouchDelegate}",
            method = "TouchDelegate",
            args = {Rect.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: MotionEvent",
            method = "onTouchEvent",
            args = {MotionEvent.class}
        )
    })
    @UiThreadTest
    public void testOnTouchEvent() {
        View view = new View(mActivity);
        MockTouchDelegate touchDelegate = new MockTouchDelegate(mRect, mButton);
        view.setTouchDelegate(touchDelegate);
        assertFalse(touchDelegate.mOnTouchEventCalled);
        view.onTouchEvent(MotionEvent.obtain(0, 0, ACTION_DOWN, mXInside, mYInside, 0));
        assertTrue(touchDelegate.mOnTouchEventCalled);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onTouchEvent",
        args = {MotionEvent.class}
    )
    @UiThreadTest
    @BrokenTest("Will fail in batch mode but can pass if only run this TestCase")
    public void testOn() {
        init();
        assertTrue(mTouchDelegate.onTouchEvent(mActionDownInside));
        clear();
        init();
        assertFalse(mTouchDelegate.onTouchEvent(mActionDownOutside));
        clear();
        init();
        assertTrue(mTouchDelegate.onTouchEvent(mActionDownInside));
        assertTrue(mTouchDelegate.onTouchEvent(mActionUpInside));
        clear();
        init();
        assertFalse(mTouchDelegate.onTouchEvent(mActionUpInside));
        clear();
        init();
        assertTrue(mTouchDelegate.onTouchEvent(mActionDownInside));
        assertTrue(mTouchDelegate.onTouchEvent(mActionUpOutside));
        clear();
        init();
        assertFalse(mTouchDelegate.onTouchEvent(mActionMoveInside));
        clear();
        init();
        assertTrue(mTouchDelegate.onTouchEvent(mActionDownInside));
        assertTrue(mTouchDelegate.onTouchEvent(mActionMoveInside));
        clear();
        init();
        assertTrue(mTouchDelegate.onTouchEvent(mActionDownInside));
        assertTrue(mTouchDelegate.onTouchEvent(mActionMoveOutside));
        clear();
        init();
        assertTrue(mTouchDelegate.onTouchEvent(mActionDownInside));
        assertTrue(mTouchDelegate.onTouchEvent(mActionCancelInside));
        assertFalse(mTouchDelegate.onTouchEvent(mActionUpInside));
        clear();
    }
    class MockTouchDelegate extends TouchDelegate {
        private boolean mOnTouchEventCalled;
        public MockTouchDelegate(Rect bounds, View delegateView) {
            super(bounds, delegateView);
        }
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mOnTouchEventCalled = true;
            return true;
        }
    }
}
