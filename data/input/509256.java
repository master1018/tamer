@TestTargetClass(ProgressBar.class)
public class ProgressBarTest extends InstrumentationTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ProgressBar",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ProgressBar",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ProgressBar",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new ProgressBar(mContext);
        new ProgressBar(mContext, null);
        new ProgressBar(mContext, null, com.android.internal.R.attr.progressBarStyle);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIndeterminate",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isIndeterminate",
            args = {}
        )
    })
    public void testSetIndeterminate() {
        ProgressBar progressBar = new ProgressBar(mContext);
        assertTrue(progressBar.isIndeterminate());
        progressBar.setIndeterminate(true);
        assertTrue(progressBar.isIndeterminate());
        progressBar.setIndeterminate(false);
        assertTrue(progressBar.isIndeterminate());
        progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        assertFalse(progressBar.isIndeterminate());
        progressBar.setIndeterminate(true);
        assertTrue(progressBar.isIndeterminate());
        progressBar.setIndeterminate(false);
        assertFalse(progressBar.isIndeterminate());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIndeterminateDrawable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setIndeterminateDrawable",
            args = {android.graphics.drawable.Drawable.class}
        )
    })
    public void testAccessIndeterminateDrawable() {
        ProgressBar progressBar = new ProgressBar(mContext);
        MockDrawable mockDrawable = new MockDrawable();
        progressBar.setIndeterminateDrawable(mockDrawable);
        assertSame(mockDrawable, progressBar.getIndeterminateDrawable());
        assertFalse(mockDrawable.hasCalledDraw());
        progressBar.draw(new Canvas());
        assertTrue(mockDrawable.hasCalledDraw());
        progressBar.setIndeterminateDrawable(null);
        assertNull(progressBar.getIndeterminateDrawable());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProgressDrawable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgressDrawable",
            args = {android.graphics.drawable.Drawable.class}
        )
    })
    public void testAccessProgressDrawable() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                        com.android.internal.R.attr.progressBarStyleHorizontal);
        MockDrawable mockDrawable = new MockDrawable();
        progressBar.setProgressDrawable(mockDrawable);
        assertSame(mockDrawable, progressBar.getProgressDrawable());
        assertFalse(mockDrawable.hasCalledDraw());
        progressBar.draw(new Canvas());
        assertTrue(mockDrawable.hasCalledDraw());
        progressBar.setProgressDrawable(null);
        assertNull(progressBar.getProgressDrawable());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProgress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProgress",
            args = {int.class}
        )
    })
    public void testAccessProgress() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        assertEquals(0, progressBar.getProgress());
        final int maxProgress = progressBar.getMax();
        progressBar.setProgress(maxProgress >> 1);
        assertEquals(maxProgress >> 1, progressBar.getProgress());
        progressBar.setProgress(-1);
        assertEquals(0, progressBar.getProgress());
        progressBar.setProgress(maxProgress + 1);
        assertEquals(maxProgress, progressBar.getProgress());
        progressBar.setProgress(Integer.MAX_VALUE);
        assertEquals(maxProgress, progressBar.getProgress());
        progressBar.setIndeterminate(true);
        progressBar.setProgress(maxProgress >> 1);
        assertEquals(0, progressBar.getProgress());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSecondaryProgress",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSecondaryProgress",
            args = {int.class}
        )
    })
    public void testAccessSecondaryProgress() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        assertEquals(0, progressBar.getSecondaryProgress());
        final int maxProgress = progressBar.getMax();
        progressBar.setSecondaryProgress(maxProgress >> 1);
        assertEquals(maxProgress >> 1, progressBar.getSecondaryProgress());
        progressBar.setSecondaryProgress(-1);
        assertEquals(0, progressBar.getSecondaryProgress());
        progressBar.setSecondaryProgress(maxProgress + 1);
        assertEquals(maxProgress, progressBar.getSecondaryProgress());
        progressBar.setSecondaryProgress(Integer.MAX_VALUE);
        assertEquals(maxProgress, progressBar.getSecondaryProgress());
        progressBar.setIndeterminate(true);
        progressBar.setSecondaryProgress(maxProgress >> 1);
        assertEquals(0, progressBar.getSecondaryProgress());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "incrementProgressBy",
        args = {int.class}
    )
    public void testIncrementProgressBy() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        int increment = 1;
        int oldProgress = progressBar.getProgress();
        progressBar.incrementProgressBy(increment);
        assertEquals(oldProgress + increment, progressBar.getProgress());
        increment = progressBar.getMax() >> 1;
        oldProgress = progressBar.getProgress();
        progressBar.incrementProgressBy(increment);
        assertEquals(oldProgress + increment, progressBar.getProgress());
        progressBar.setProgress(0);
        progressBar.incrementProgressBy(Integer.MAX_VALUE);
        assertEquals(progressBar.getMax(), progressBar.getProgress());
        progressBar.setProgress(0);
        progressBar.incrementProgressBy(Integer.MIN_VALUE);
        assertEquals(0, progressBar.getProgress());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "incrementSecondaryProgressBy",
        args = {int.class}
    )
    public void testIncrementSecondaryProgressBy() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        int increment = 1;
        int oldSecondaryProgress = progressBar.getSecondaryProgress();
        progressBar.incrementSecondaryProgressBy(increment);
        assertEquals(oldSecondaryProgress + increment, progressBar.getSecondaryProgress());
        increment = progressBar.getMax() >> 1;
        oldSecondaryProgress = progressBar.getSecondaryProgress();
        progressBar.incrementSecondaryProgressBy(increment);
        assertEquals(oldSecondaryProgress + increment, progressBar.getSecondaryProgress());
        progressBar.setSecondaryProgress(0);
        progressBar.incrementSecondaryProgressBy(Integer.MAX_VALUE);
        assertEquals(progressBar.getMax(), progressBar.getSecondaryProgress());
        progressBar.setSecondaryProgress(0);
        progressBar.incrementSecondaryProgressBy(Integer.MIN_VALUE);
        assertEquals(0, progressBar.getSecondaryProgress());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "setInterpolator",
            args = {android.view.animation.Interpolator.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "getInterpolator",
            args = {}
        )
    })
    public void testAccessInterpolator() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyle);
        assertTrue(progressBar.getInterpolator() instanceof LinearInterpolator);
        Interpolator i = new AccelerateDecelerateInterpolator();
        progressBar.setInterpolator(i);
        assertEquals(i, progressBar.getInterpolator());
        progressBar.setInterpolator(null);
        assertNull(progressBar.getInterpolator());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInterpolator",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInterpolator",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setInterpolator() is incomplete." +
            "1. not clear what is supposed to happen if context or resID is exceptional.")
    @BrokenTest("Initial setInterpolator() call occasionally fails with NPE. context null?")
    public void testAccessInterpolatorContext() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyle);
        assertTrue(progressBar.getInterpolator() instanceof LinearInterpolator);
        progressBar.setInterpolator(mContext.getApplicationContext(), R.anim.move_cycle);
        assertTrue(progressBar.getInterpolator() instanceof CycleInterpolator);
        try {
            progressBar.setInterpolator(null, R.anim.move_ani);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            progressBar.setInterpolator(mContext.getApplicationContext(), -1);
            fail("Should throw NotFoundException");
        } catch (NotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setVisibility",
        args = {int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setVisibility() is incomplete." +
            "1. not clear what is supposed result if visibility isn't VISIBLE, INVISIBLE or GONE.")
    public void testSetVisibility() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        int visibility = View.VISIBLE;
        progressBar.setVisibility(visibility);
        assertEquals(visibility, progressBar.getVisibility());
        visibility = View.GONE;
        progressBar.setVisibility(visibility);
        assertEquals(visibility, progressBar.getVisibility());
        visibility = 0xfffffff5; 
        int mask = 0x0000000C; 
        int expected = (progressBar.getVisibility() & ~mask) | (visibility & mask);
        progressBar.setVisibility(visibility);
        assertEquals(expected, progressBar.getVisibility());
        visibility = 0x7fffffff; 
        expected = (progressBar.getVisibility() & ~mask) | (visibility & mask);
        progressBar.setVisibility(Integer.MAX_VALUE);
        assertEquals(expected, progressBar.getVisibility());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "invalidateDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for invalidateDrawable() is incomplete." +
            "1. not clear what is supposed to happen if drawable is null.")
    public void testInvalidateDrawable() {
        MockProgressBar mockProgressBar = new MockProgressBar(mContext);
        MockDrawable mockDrawable1 = new MockDrawable();
        MockDrawable mockDrawable2 = new MockDrawable();
        mockProgressBar.setBackgroundDrawable(mockDrawable1);
        mockProgressBar.invalidateDrawable(mockDrawable1);
        assertTrue(mockProgressBar.hasCalledInvalidate());
        mockProgressBar.reset();
        mockProgressBar.invalidateDrawable(mockDrawable2);
        assertFalse(mockProgressBar.hasCalledInvalidate());
        try {
            mockProgressBar.invalidateDrawable(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        mockProgressBar.setIndeterminateDrawable(mockDrawable1);
        mockProgressBar.setProgressDrawable(mockDrawable2);
        mockProgressBar.invalidateDrawable(null);
        assertFalse(mockProgressBar.hasCalledInvalidate());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "postInvalidate",
        args = {}
    )
    public void testPostInvalidate() {
        MockProgressBar mockProgressBar = new MockProgressBar(mContext);
        mockProgressBar.postInvalidate();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMax",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMax",
            args = {int.class}
        )
    })
    public void testAccessMax() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        int progress = 10;
        progressBar.setProgress(progress);
        int max = progress + 1;
        progressBar.setMax(max);
        assertEquals(max, progressBar.getMax());
        assertEquals(progress, progressBar.getProgress());
        max = progress - 1;
        progressBar.setMax(max);
        assertEquals(max, progressBar.getMax());
        assertEquals(max, progressBar.getProgress());
        progressBar.setMax(-1);
        assertEquals(0, progressBar.getMax());
        assertEquals(0, progressBar.getProgress());
        progressBar.setMax(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, progressBar.getMax());
        assertEquals(0, progressBar.getProgress());
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onDraw",
        args = {android.graphics.Canvas.class}
    )
    public void testOnDraw() {
    }
    private class MockDrawable extends Drawable {
        private boolean mCalledDraw = false;
        @Override
        public void draw(Canvas canvas) {
            mCalledDraw = true;
        }
        @Override
        public int getOpacity() {
            return 0;
        }
        @Override
        public void setAlpha(int alpha) {
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
        }
        public boolean hasCalledDraw() {
            return mCalledDraw;
        }
        public void reset() {
            mCalledDraw = false;
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onMeasure",
        args = {int.class, int.class}
    )
    public void testOnMeasure() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onSizeChanged",
        args = {int.class, int.class, int.class, int.class}
    )
    public void testOnSizeChange() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "verifyDrawable",
        args = {android.graphics.drawable.Drawable.class}
    )
    public void testVerifyDrawable() {
        MockProgressBar mockProgressBar = new MockProgressBar(mContext);
        assertTrue(mockProgressBar.verifyDrawable(null));
        Drawable d1 = mContext.getResources().getDrawable(R.drawable.blue);
        Drawable d2 = mContext.getResources().getDrawable(R.drawable.red);
        Drawable d3 = mContext.getResources().getDrawable(R.drawable.yellow);
        mockProgressBar.setBackgroundDrawable(d1);
        assertTrue(mockProgressBar.verifyDrawable(null));
        assertTrue(mockProgressBar.verifyDrawable(d1));
        assertFalse(mockProgressBar.verifyDrawable(d2));
        assertFalse(mockProgressBar.verifyDrawable(d3));
        mockProgressBar.setIndeterminateDrawable(d2);
        assertTrue(mockProgressBar.verifyDrawable(null));
        assertTrue(mockProgressBar.verifyDrawable(d1));
        assertTrue(mockProgressBar.verifyDrawable(d2));
        assertFalse(mockProgressBar.verifyDrawable(d3));
        mockProgressBar.setProgressDrawable(d3);
        assertFalse(mockProgressBar.verifyDrawable(null));
        assertTrue(mockProgressBar.verifyDrawable(d1));
        assertTrue(mockProgressBar.verifyDrawable(d2));
        assertTrue(mockProgressBar.verifyDrawable(d3));
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "drawableStateChanged",
        args = {}
    )
    public void testDrawableStateChanged() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onRestoreInstanceState",
            args = {android.os.Parcelable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onSaveInstanceState",
            args = {}
        )
    })
    public void testOnSaveAndRestoreInstanceState() {
        ProgressBar progressBar = new ProgressBar(mContext, null,
                com.android.internal.R.attr.progressBarStyleHorizontal);
        int oldProgress = 1;
        int oldSecondaryProgress = progressBar.getMax() - 1;
        progressBar.setProgress(oldProgress);
        progressBar.setSecondaryProgress(oldSecondaryProgress);
        assertEquals(oldProgress, progressBar.getProgress());
        assertEquals(oldSecondaryProgress, progressBar.getSecondaryProgress());
        Parcelable state = progressBar.onSaveInstanceState();
        int newProgress = 2;
        int newSecondaryProgress = progressBar.getMax() - 2;
        progressBar.setProgress(newProgress);
        progressBar.setSecondaryProgress(newSecondaryProgress);
        assertEquals(newProgress, progressBar.getProgress());
        assertEquals(newSecondaryProgress, progressBar.getSecondaryProgress());
        progressBar.onRestoreInstanceState(state);
        assertEquals(oldProgress, progressBar.getProgress());
        assertEquals(oldSecondaryProgress, progressBar.getSecondaryProgress());
    }
    private class MockProgressBar extends ProgressBar {
        private boolean mCalledInvalidate = false;
        public MockProgressBar(Context context) {
            super(context);
        }
        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who);
        }
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }
        @Override
        protected synchronized void onMeasure(int widthMeasureSpec,
                int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        @Override
        protected synchronized void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }
        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
        }
        public void invalidate(int l, int t, int r, int b) {
            mCalledInvalidate = true;
            super.invalidate(l, t, r, b);
        }
        public void invalidate() {
            mCalledInvalidate = true;
            super.invalidate();
        }
        public boolean hasCalledInvalidate() {
            return mCalledInvalidate;
        }
        public void reset() {
            mCalledInvalidate = false;
        }
    }
}
