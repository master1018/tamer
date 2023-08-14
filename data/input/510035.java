@TestTargetClass(Touch.class)
public class TouchTest extends ActivityInstrumentationTestCase2<StubActivity> {
    private Activity mActivity;
    private static final String LONG_TEXT = "Scrolls the specified widget to the specified " +
            "coordinates, except constrains the X scrolling position to the horizontal regions " +
            "of the text that will be visible after scrolling to the specified Y position.";
    private boolean mReturnFromTouchEvent;
    public TouchTest() {
        super("com.android.cts.stub", StubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "scrollTo",
        args = {TextView.class, Layout.class, int.class, int.class}
    )
    public void testScrollTo() throws Throwable {
        final TextView tv = new TextView(mActivity);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.setContentView(tv);
                tv.setSingleLine(true);
                tv.setLines(2);
            }
        });
        getInstrumentation().waitForIdleSync();
        TextPaint paint = tv.getPaint();
        final Layout layout = tv.getLayout();
        runTestOnUiThread(new Runnable() {
            public void run() {
                tv.setText(LONG_TEXT);
            }
        });
        getInstrumentation().waitForIdleSync();
        final int width = getTextWidth(LONG_TEXT, paint);
        runTestOnUiThread(new Runnable() {
            public void run() {
                Touch.scrollTo(tv, layout, width - tv.getWidth() - 1, 0);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(width - tv.getWidth() - 1, tv.getScrollX());
        assertEquals(0, tv.getScrollY());
        runTestOnUiThread(new Runnable() {
            public void run() {
                Touch.scrollTo(tv, layout, width + 100, 5);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(width - tv.getWidth(), tv.getScrollX());
        assertEquals(5, tv.getScrollY());
        runTestOnUiThread(new Runnable() {
            public void run() {
                Touch.scrollTo(tv, layout, width - 10, 5);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertEquals(width - tv.getWidth(), tv.getScrollX());
        assertEquals(5, tv.getScrollY());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInitialScrollX",
            args = {TextView.class, Spannable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInitialScrollY",
            args = {TextView.class, Spannable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onTouchEvent",
            args = {TextView.class, Spannable.class, MotionEvent.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add @throws clause into javadoc.")
    public void testOnTouchEvent() throws Throwable {
        final SpannableString spannable = new SpannableString(LONG_TEXT);
        final TextView tv = new TextView(mActivity);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mActivity.setContentView(tv);
                tv.setSingleLine(true);
                tv.setText(LONG_TEXT);
            }
        });
        getInstrumentation().waitForIdleSync();
        TextPaint paint = tv.getPaint();
        final int width = getTextWidth(LONG_TEXT, paint);
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        int x = width >> 1;
        final MotionEvent event1 = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, 0, 0);
        final MotionEvent event2 = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_MOVE, 0, 0, 0);
        final MotionEvent event3 = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, 0, 0, 0);
        assertEquals(0, tv.getScrollX());
        assertEquals(0, tv.getScrollY());
        mReturnFromTouchEvent = false;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mReturnFromTouchEvent = Touch.onTouchEvent(tv, spannable, event1);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mReturnFromTouchEvent);
        assertEquals(0, tv.getScrollX());
        assertEquals(0, tv.getScrollY());
        assertEquals(0, Touch.getInitialScrollX(tv, spannable));
        assertEquals(0, Touch.getInitialScrollY(tv, spannable));
        mReturnFromTouchEvent = false;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mReturnFromTouchEvent = Touch.onTouchEvent(tv, spannable, event2);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mReturnFromTouchEvent);
        assertEquals(x, tv.getScrollX());
        assertEquals(0, tv.getScrollY());
        assertEquals(0, Touch.getInitialScrollX(tv, spannable));
        assertEquals(0, Touch.getInitialScrollY(tv, spannable));
        mReturnFromTouchEvent = false;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mReturnFromTouchEvent = Touch.onTouchEvent(tv, spannable, event3);
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mReturnFromTouchEvent);
        assertEquals(x, tv.getScrollX());
        assertEquals(0, tv.getScrollY());
        assertEquals(-1, Touch.getInitialScrollX(tv, spannable));
        assertEquals(-1, Touch.getInitialScrollY(tv, spannable));
    }
    private int getTextWidth(String str, TextPaint paint) {
        float totalWidth = 0f;
        float[] widths = new float[str.length()];
        paint.getTextWidths(str, widths);
        for (float f : widths) {
            totalWidth += f;
        }
        return (int) totalWidth;
    }
}
