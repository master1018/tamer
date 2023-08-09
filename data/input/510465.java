@TestTargetClass(TransitionDrawable.class)
public class TransitionDrawableTest extends InstrumentationTestCase {
    private static final int COLOR1 = 0xff0000ff;
    private static final int COLOR0 = 0xffff0000;
    private static final int CANVAS_WIDTH = 10;
    private static final int CANVAS_HEIGHT = 10;
    private TransitionDrawable mTransitionDrawable;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTransitionDrawable = (TransitionDrawable) getInstrumentation().getTargetContext()
                .getResources().getDrawable(R.drawable.transition_test);
        mTransitionDrawable.setBounds(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        mBitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "TransitionDrawable",
        args = {Drawable[].class}
    )
    public void testConstructor() {
        Resources resources = getInstrumentation().getTargetContext().getResources();
        Drawable[] drawables = new Drawable[] {
                resources.getDrawable(R.drawable.testimage),
                resources.getDrawable(R.drawable.levellistdrawable)
        };
        new TransitionDrawable(drawables);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startTransition",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class}
        )
    })
    @ToBeFixed(explanation = "The method should not accept negative duration.")
    public void testStartTransition() {
        MockCallBack cb = new MockCallBack();
        mTransitionDrawable.setCallback(cb);
        cb.reset();
        mTransitionDrawable.startTransition(2000);
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransition(COLOR0, COLOR1, 2000);
        makeTransitionInProgress(2000, 1000);
        cb.reset();
        mTransitionDrawable.startTransition(2000);
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransition(COLOR0, COLOR1, 2000);
        makeReverseTransitionInProgress(2000, 1000);
        cb.reset();
        mTransitionDrawable.startTransition(2000);
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransition(COLOR0, COLOR1, 2000);
        mTransitionDrawable.startTransition(-1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resetTransition",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class}
        )
    })
    public void testResetTransition() {
        MockCallBack cb = new MockCallBack();
        mTransitionDrawable.setCallback(cb);
        cb.reset();
        mTransitionDrawable.resetTransition();
        assertTrue(cb.hasCalledInvalidateDrawable());
        makeTransitionInProgress(2000, 1000);
        cb.reset();
        mTransitionDrawable.resetTransition();
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransitionStart(COLOR0);
        assertTransitionEnd(COLOR0, 2000);
        makeReverseTransitionInProgress(2000, 1000);
        cb.reset();
        mTransitionDrawable.resetTransition();
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransitionStart(COLOR0);
        assertTransitionEnd(COLOR0, 2000);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reverseTransition",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class}
        )
    })
    @ToBeFixed(explanation = "The method should not accept negative duration.")
    public void testReverseTransition() {
        MockCallBack cb = new MockCallBack();
        mTransitionDrawable.setCallback(cb);
        cb.reset();
        mTransitionDrawable.reverseTransition(2000);
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransition(COLOR0, COLOR1, 2000);
        cb.reset();
        mTransitionDrawable.reverseTransition(2000);
        assertTrue(cb.hasCalledInvalidateDrawable());
        assertTransition(COLOR1, COLOR0, 2000);
        makeTransitionInProgress(2000, 1000);
        cb.reset();
        mTransitionDrawable.reverseTransition(20000);
        assertFalse(cb.hasCalledInvalidateDrawable());
        int colorFrom = mBitmap.getPixel(0, 0);
        assertTransition(colorFrom, COLOR0, 1500);
        makeReverseTransitionInProgress(2000, 1000);
        cb.reset();
        mTransitionDrawable.reverseTransition(20000);
        assertFalse(cb.hasCalledInvalidateDrawable());
        colorFrom = mBitmap.getPixel(0, 0);
        assertTransition(colorFrom, COLOR1, 1500);
        mTransitionDrawable.reverseTransition(-1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test the method with null canvas. This method is tested in directly with real "
                + "canvas in other tests of this case.",
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "TransitionDrawable#draw(Canvas) when param canvas is null")
    public void testDrawWithNUllCanvas() {
        try {
            mTransitionDrawable.draw(null);
            fail("The method should check whether the canvas is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCrossFadeEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isCrossFadeEnabled",
            args = {}
        )
    })
    public void testAccessCrossFadeEnabled() {
        assertFalse(mTransitionDrawable.isCrossFadeEnabled());
        mTransitionDrawable.setCrossFadeEnabled(true);
        assertTrue(mTransitionDrawable.isCrossFadeEnabled());
        mTransitionDrawable.setCrossFadeEnabled(false);
        assertFalse(mTransitionDrawable.isCrossFadeEnabled());
    }
    private void assertTransition(int colorFrom, int colorTo, long delay) {
        assertTransitionStart(colorFrom);
        assertTransitionInProgress(colorFrom, colorTo, delay / 2);
        assertTransitionEnd(colorTo, delay);
    }
    private void assertTransitionStart(int colorFrom) {
        mBitmap.eraseColor(0x00000000);
        mTransitionDrawable.draw(mCanvas);
        assertColorFillRect(mBitmap, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, colorFrom);
    }
    private void assertTransitionInProgress(int colorFrom, int colorTo, long delay) {
        drawAfterDelaySync(delay);
        assertColorNotFillRect(mBitmap, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, colorFrom);
        assertColorNotFillRect(mBitmap, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, colorTo);
    }
    private void assertTransitionEnd(int colorTo, long delay) {
        drawAfterDelaySync(delay);
        assertColorFillRect(mBitmap, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, colorTo);
    }
    private void assertColorFillRect(Bitmap bmp, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                assertEquals(color, bmp.getPixel(i, j));
            }
        }
    }
    private void assertColorNotFillRect(Bitmap bmp, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                assertTrue(color != bmp.getPixel(i, j));
            }
        }
    }
    private void makeReverseTransitionInProgress(int duration, int delay) {
        mTransitionDrawable.resetTransition();
        mTransitionDrawable.startTransition(100);
        assertTransition(COLOR0, COLOR1, 100);
        mTransitionDrawable.reverseTransition(duration);
        assertTransitionStart(COLOR1);
        assertTransitionInProgress(COLOR1, COLOR0, delay);
    }
    private void makeTransitionInProgress(int duration, int delay) {
        mTransitionDrawable.resetTransition();
        mTransitionDrawable.startTransition(duration);
        assertTransitionStart(COLOR0);
        assertTransitionInProgress(COLOR0, COLOR1, delay);
    }
    private void drawAfterDelaySync(long delay) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                mBitmap.eraseColor(0x00000000);
                mTransitionDrawable.draw(mCanvas);
            }
        });
        try {
            Thread.sleep(delay);
            t.start();
            t.join();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
    private class MockCallBack implements Callback {
        private boolean mHasCalledInvalidateDrawable;
        public boolean hasCalledInvalidateDrawable() {
            return mHasCalledInvalidateDrawable;
        }
        public void reset() {
            mHasCalledInvalidateDrawable = false;
        }
        public void invalidateDrawable(Drawable who) {
            mHasCalledInvalidateDrawable = true;
        }
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
        }
        public void unscheduleDrawable(Drawable who, Runnable what) {
        }
    }
}
