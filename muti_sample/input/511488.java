@TestTargetClass(Scroller.class)
public class ScrollerTest extends InstrumentationTestCase {
    private Scroller mScroller;
    private Context mTargetContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTargetContext = getInstrumentation().getTargetContext();
        mScroller = new Scroller(mTargetContext);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "Scroller",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructors",
            method = "Scroller",
            args = {android.content.Context.class, android.view.animation.Interpolator.class}
        )
    })
    public void testConstructor() {
        new Scroller(mTargetContext);
        new Scroller(mTargetContext, new LinearInterpolator());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "forceFinished",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFinished",
            args = {}
        )
    })
    public void testIsFinished() {
        mScroller.forceFinished(true);
        assertTrue(mScroller.isFinished());
        mScroller.forceFinished(false);
        assertFalse(mScroller.isFinished());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Scroller#getDuration()}",
        method = "getDuration",
        args = {}
    )
    public void testGetDuration() {
        assertEquals(0, mScroller.getDuration());
        mScroller.startScroll(0, 0, 10000, 100);
        assertTrue(mScroller.getDuration() > 0);
        mScroller.startScroll(0, 0, 10000, 100, 50000);
        assertEquals(50000, mScroller.getDuration());
        mScroller = new Scroller(mTargetContext);
        assertEquals(0, mScroller.getDuration());
        mScroller.fling(0, 0, 10, 4000, 0, 100, 0, 0);
        assertTrue(mScroller.getDuration() > 0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Scroller#getFinalX()} and {@link Scroller#setFinalX(int)}",
            method = "setFinalX",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Scroller#getFinalX()} and {@link Scroller#setFinalX(int)}",
            method = "getFinalX",
            args = {}
        )
    })
    public void testAccessFinalX() {
        assertEquals(0, mScroller.getFinalX());
        assertTrue(mScroller.isFinished());
        mScroller.abortAnimation();
        assertTrue(mScroller.isFinished());
        mScroller.setFinalX(5000);
        assertEquals(5000, mScroller.getFinalX());
        assertFalse(mScroller.isFinished());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Scroller#setFinalY(int)} and {@link Scroller#getFinalY()}",
            method = "setFinalY",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link Scroller#setFinalY(int)} and {@link Scroller#getFinalY()}",
            method = "getFinalY",
            args = {}
        )
    })
    public void testAccessFinalY() {
        assertEquals(0, mScroller.getFinalY());
        assertTrue(mScroller.isFinished());
        mScroller.abortAnimation();
        assertTrue(mScroller.isFinished());
        mScroller.setFinalY(5000);
        assertEquals(5000, mScroller.getFinalY());
        assertFalse(mScroller.isFinished());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startScroll",
            args = {int.class, int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeScrollOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrY",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartY",
            args = {}
        )
    })
    public void testScrollMode() {
        assertEquals(0, mScroller.getFinalX());
        assertEquals(0, mScroller.getFinalY());
        assertEquals(0, mScroller.getDuration());
        assertTrue(mScroller.isFinished());
        mScroller.startScroll(0, 0, 2000, -2000, 5000);
        assertEquals(0, mScroller.getStartX());
        assertEquals(0, mScroller.getStartY());
        assertEquals(2000, mScroller.getFinalX());
        assertEquals(-2000, mScroller.getFinalY());
        assertEquals(5000, mScroller.getDuration());
        assertFalse(mScroller.isFinished());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        assertTrue(mScroller.getCurrX() > 0);
        assertTrue(mScroller.getCurrX() < 2000);
        assertTrue(mScroller.getCurrY() > -2000);
        assertTrue(mScroller.getCurrY() < 0);
        assertFalse(mScroller.isFinished());
        int curX = mScroller.getCurrX();
        int curY = mScroller.getCurrY();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        assertTrue(mScroller.getCurrX() > 0);
        assertTrue(mScroller.getCurrX() < 2000);
        assertTrue(mScroller.getCurrY() > -2000);
        assertTrue(mScroller.getCurrY() < 0);
        assertTrue(mScroller.getCurrX() > curX);
        assertTrue(mScroller.getCurrY() < curY);
        assertFalse(mScroller.isFinished());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        assertEquals(2000, mScroller.getCurrX());
        assertEquals(-2000, mScroller.getCurrY());
        assertTrue(mScroller.isFinished());
        assertFalse(mScroller.computeScrollOffset());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "startScroll",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeScrollOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrY",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartY",
            args = {}
        )
    })
    public void testScrollModeWithDefaultDuration() {
        assertEquals(0, mScroller.getFinalX());
        assertEquals(0, mScroller.getFinalY());
        assertEquals(0, mScroller.getDuration());
        assertTrue(mScroller.isFinished());
        mScroller.startScroll(0, 0, -2000, 2000);
        assertEquals(0, mScroller.getStartX());
        assertEquals(0, mScroller.getStartY());
        assertEquals(-2000, mScroller.getFinalX());
        assertEquals(2000, mScroller.getFinalY());
        int defaultDuration = mScroller.getDuration();
        assertTrue(defaultDuration > 0);
        assertFalse(mScroller.isFinished());
        try {
            Thread.sleep(defaultDuration);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        assertEquals(-2000, mScroller.getCurrX());
        assertEquals(2000, mScroller.getCurrY());
        assertTrue(mScroller.isFinished());
        assertFalse(mScroller.computeScrollOffset());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "fling",
            args = {int.class, int.class, int.class, int.class, int.class, int.class, int.class,
                    int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeScrollOffset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrY",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartX",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStartY",
            args = {}
        )
    })
    public void testFlingMode() {
        assertEquals(0, mScroller.getFinalX());
        assertEquals(0, mScroller.getFinalY());
        assertEquals(0, mScroller.getDuration());
        assertTrue(mScroller.isFinished());
        mScroller.fling(0, 0, - 3000, 4000, Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertEquals(0, mScroller.getStartX());
        assertEquals(0, mScroller.getStartY());
        int duration = mScroller.getDuration();
        assertTrue(mScroller.getFinalX() < 0);
        assertTrue(mScroller.getFinalY() > 0);
        assertTrue(duration > 0);
        assertFalse(mScroller.isFinished());
        try {
            Thread.sleep(duration / 3);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        int currX = mScroller.getCurrX();
        int currY = mScroller.getCurrY();
        assertTrue(currX < 0);
        assertTrue(currX > mScroller.getFinalX());
        assertTrue(currY > 0);
        assertTrue(currY < mScroller.getFinalY());
        assertFalse(mScroller.isFinished());
        try {
            Thread.sleep(duration / 3);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        int previousX = currX;
        int previousY = currY;
        currX = mScroller.getCurrX();
        currY = mScroller.getCurrY();
        assertTrue(currX < previousX);
        assertTrue(currX > mScroller.getFinalX());
        assertTrue(currY > previousY);
        assertTrue(currY < mScroller.getFinalY());
        assertFalse(mScroller.isFinished());
        assertTrue(Math.abs(currX - previousX) < Math.abs(previousX - 0));
        assertTrue(Math.abs(currY - previousY) < Math.abs(previousY - 0));
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.computeScrollOffset());
        assertEquals(mScroller.getFinalX(), mScroller.getCurrX());
        assertEquals(mScroller.getFinalY(), mScroller.getCurrY());
        assertTrue(mScroller.isFinished());
        assertFalse(mScroller.computeScrollOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Scroller#abortAnimation()}",
        method = "abortAnimation",
        args = {}
    )
    public void testAbortAnimation() {
        mScroller.startScroll(0, 0, 2000, -2000, 5000);
        mScroller.computeScrollOffset();
        assertFalse(mScroller.getCurrX() == mScroller.getFinalX());
        assertFalse(mScroller.getCurrY() == mScroller.getFinalY());
        assertFalse(mScroller.isFinished());
        mScroller.abortAnimation();
        assertTrue(mScroller.getCurrX() == mScroller.getFinalX());
        assertTrue(mScroller.getCurrY() == mScroller.getFinalY());
        assertTrue(mScroller.isFinished());
        mScroller.fling(0, 0, - 3000, 4000,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        mScroller.computeScrollOffset();
        assertFalse(mScroller.getCurrX() == mScroller.getFinalX());
        assertFalse(mScroller.getCurrY() == mScroller.getFinalY());
        assertFalse(mScroller.isFinished());
        mScroller.abortAnimation();
        assertTrue(mScroller.getCurrX() == mScroller.getFinalX());
        assertTrue(mScroller.getCurrY() == mScroller.getFinalY());
        assertTrue(mScroller.isFinished());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "extendDuration",
        args = {int.class}
    )
    public void testExtendDuration() {
        mScroller.startScroll(0, 0, 0, 0, 5000);
        assertEquals(5000, mScroller.getDuration());
        mScroller.extendDuration(5000);
        assertTrue(mScroller.getDuration() >= 5000);
        assertFalse(mScroller.isFinished());
        mScroller.startScroll(0, 0, 0, 0, 500);
        assertEquals(500, mScroller.getDuration());
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        mScroller.abortAnimation();
        mScroller.extendDuration(500);
        assertTrue(mScroller.getDuration() >= 2000);
        assertFalse(mScroller.isFinished());
        mScroller = new Scroller(mTargetContext, new LinearInterpolator());
        mScroller.startScroll(0, 0, 5000, 5000, 5000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        mScroller.computeScrollOffset();
        int curX = mScroller.getCurrX();
        int curY = mScroller.getCurrY();
        mScroller.extendDuration(9000);
        mScroller.computeScrollOffset();
        assertTrue(mScroller.getCurrX() - curX < curX - 0);
        assertTrue(mScroller.getCurrY() - curY < curY - 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link Scroller#timePassed()}We can not get the precise time passed.",
        method = "timePassed",
        args = {}
    )
    public void testTimePassed() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.timePassed() > 1000);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("unexpected InterruptedException when sleep");
        }
        assertTrue(mScroller.timePassed() > 3000);
    }
}
