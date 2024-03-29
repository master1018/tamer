@TestTargetClass(SeekBar.class)
public class SeekBarTest extends ActivityInstrumentationTestCase2<SeekBarStubActivity> {
    private SeekBar mSeekBar;
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    public SeekBarTest() {
        super("com.android.cts.stub", SeekBarStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        mSeekBar = (SeekBar) mActivity.findViewById(R.id.seekBar);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SeekBar",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SeekBar",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SeekBar",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new SeekBar(mActivity);
        new SeekBar(mActivity, null);
        new SeekBar(mActivity, null, com.android.internal.R.attr.seekBarStyle);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnSeekBarChangeListener",
        args = {android.widget.SeekBar.OnSeekBarChangeListener.class}
    )
    public void testSetOnSeekBarChangeListener() {
        MockOnSeekBarListener listener = new MockOnSeekBarListener();
        mSeekBar.setOnSeekBarChangeListener(listener);
        listener.reset();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        int seekBarXY[] = new int[2];
        mSeekBar.getLocationInWindow(seekBarXY);
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN,
                seekBarXY[0], seekBarXY[1], 0);
        mInstrumentation.sendPointerSync(event);
        mInstrumentation.waitForIdleSync();
        assertTrue(listener.hasCalledOnStartTrackingTouch());
        assertTrue(listener.hasCalledOnProgressChanged());
        listener.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE,
                seekBarXY[0] + (mSeekBar.getWidth() >> 1), seekBarXY[1], 0);
        mInstrumentation.sendPointerSync(event);
        mInstrumentation.waitForIdleSync();
        assertTrue(listener.hasCalledOnProgressChanged());
        listener.reset();
        downTime = SystemClock.uptimeMillis();
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,
                seekBarXY[0] + (mSeekBar.getWidth() >> 1), seekBarXY[1], 0);
        mInstrumentation.sendPointerSync(event);
        mInstrumentation.waitForIdleSync();
        assertTrue(listener.hasCalledOnStopTrackingTouch());
        mSeekBar.setOnSeekBarChangeListener(null);
    }
    private class MockOnSeekBarListener implements OnSeekBarChangeListener {
        private boolean mHasCalledOnProgressChanged;
        private boolean mHasCalledOnStartTrackingTouch;
        private boolean mHasCalledOnStopTrackingTouch;
        public boolean hasCalledOnProgressChanged() {
            return mHasCalledOnProgressChanged;
        }
        public boolean hasCalledOnStartTrackingTouch() {
            return mHasCalledOnStartTrackingTouch;
        }
        public boolean hasCalledOnStopTrackingTouch() {
            return mHasCalledOnStopTrackingTouch;
        }
        public void reset(){
            mHasCalledOnProgressChanged = false;
            mHasCalledOnStartTrackingTouch = false;
            mHasCalledOnStopTrackingTouch = false;
        }
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            mHasCalledOnProgressChanged = true;
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHasCalledOnStartTrackingTouch = true;
        }
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHasCalledOnStopTrackingTouch = true;
        }
    }
}
