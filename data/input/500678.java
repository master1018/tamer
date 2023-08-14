@TestTargetClass(RatingBar.class)
public class RatingBarTest extends ActivityInstrumentationTestCase2<RatingBarStubActivity> {
    private Context mContext;
    private RatingBarStubActivity mActivity;
    public RatingBarTest() {
        super("com.android.cts.stub", RatingBarStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        assertNotNull(mActivity);
        mContext = getInstrumentation().getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link RatingBar}",
            method = "RatingBar",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link RatingBar}",
            method = "RatingBar",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link RatingBar}",
            method = "RatingBar",
            args = {android.content.Context.class, android.util.AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new RatingBar(mContext, null, com.android.internal.R.attr.ratingBarStyle);
        new RatingBar(mContext, null);
        new RatingBar(mContext);
        RatingBar ratingBar = (RatingBar) mActivity.findViewById(R.id.ratingbar_constructor);
        assertNotNull(ratingBar);
        assertFalse(ratingBar.isIndicator());
        assertEquals(50, ratingBar.getNumStars());
        assertEquals(1.2f, ratingBar.getRating());
        assertEquals(0.2f, ratingBar.getStepSize());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access OnRatingBarChangeListener",
            method = "getOnRatingBarChangeListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access OnRatingBarChangeListener",
            method = "setOnRatingBarChangeListener",
            args = {android.widget.RatingBar.OnRatingBarChangeListener.class}
        )
    })
    @UiThreadTest
    public void testAccessOnRatingBarChangeListener() {
        RatingBar ratingBar = (RatingBar)mActivity.findViewById(R.id.ratingbar_constructor);
        MockOnRatingBarChangeListener listener = new MockOnRatingBarChangeListener();
        ratingBar.setOnRatingBarChangeListener(listener);
        assertSame(listener, ratingBar.getOnRatingBarChangeListener());
        ratingBar.setRating(2.2f);
        assertTrue(listener.hasCalledOnRatingChanged());
        listener.reset();
        ratingBar.setOnRatingBarChangeListener(null);
        assertNull(ratingBar.getOnRatingBarChangeListener());
        ratingBar.setRating(1.2f);
        assertFalse(listener.hasCalledOnRatingChanged());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access isIndicator",
            method = "isIndicator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access isIndicator",
            method = "setIsIndicator",
            args = {boolean.class}
        )
    })
    public void testAccessIndicator() {
        RatingBar ratingBar = new RatingBar(mContext);
        ratingBar.setIsIndicator(true);
        assertTrue(ratingBar.isIndicator());
        ratingBar.setIsIndicator(false);
        assertFalse(ratingBar.isIndicator());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access NumStars",
            method = "setNumStars",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access NumStars",
            method = "getNumStars",
            args = {}
        )
    })
    public void testAccessNumStars() {
        MockRatingBar mockRatingBar = new MockRatingBar(mContext);
        assertFalse(mockRatingBar.hasCalledRequestLayout());
        mockRatingBar.setNumStars(20);
        assertTrue(mockRatingBar.hasCalledRequestLayout());
        assertEquals(20, mockRatingBar.getNumStars());
        mockRatingBar.reset();
        mockRatingBar.setNumStars(-10);
        assertFalse(mockRatingBar.hasCalledRequestLayout());
        assertEquals(20, mockRatingBar.getNumStars());
        mockRatingBar.reset();
        mockRatingBar.setNumStars(Integer.MAX_VALUE);
        assertTrue(mockRatingBar.hasCalledRequestLayout());
        assertEquals(Integer.MAX_VALUE, mockRatingBar.getNumStars());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access Rating",
            method = "getRating",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access Rating",
            method = "setRating",
            args = {float.class}
        )
    })
    public void testAccessRating() {
        RatingBar ratingBar = new RatingBar(mContext);
        ratingBar.setRating(2.0f);
        assertEquals(2.0f, ratingBar.getRating());
        ratingBar.setRating(-2.0f);
        assertEquals(0f, ratingBar.getRating());
        ratingBar.setRating(Float.MAX_VALUE);
        assertEquals((float) ratingBar.getNumStars(), ratingBar.getRating());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setMax(int max)",
        method = "setMax",
        args = {int.class}
    )
    public void testSetMax() {
        RatingBar ratingBar = new RatingBar(mContext);
        ratingBar.setMax(10);
        assertEquals(10, ratingBar.getMax());
        ratingBar.setProgress(10);
        ratingBar.setMax(-10);
        assertEquals(10, ratingBar.getMax());
        assertEquals(10, ratingBar.getProgress());
        ratingBar.setMax(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, ratingBar.getMax());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access StepSize",
            method = "getStepSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access StepSize",
            method = "setStepSize",
            args = {float.class}
        )
    })
    public void testAccessStepSize() {
        RatingBar ratingBar = new RatingBar(mContext);
        ratingBar.setStepSize(1.5f);
        float expectedMax = ratingBar.getNumStars() / ratingBar.getStepSize();
        float expectedProgress = expectedMax / ratingBar.getMax() * ratingBar.getProgress();
        assertEquals((int) expectedMax, ratingBar.getMax());
        assertEquals((int) expectedProgress, ratingBar.getProgress());
        assertEquals((float) ratingBar.getNumStars() / (int) (ratingBar.getNumStars() / 1.5f),
                ratingBar.getStepSize());
        int currentMax = ratingBar.getMax();
        int currentProgress = ratingBar.getProgress();
        float currentStepSize = ratingBar.getStepSize();
        ratingBar.setStepSize(-1.5f);
        assertEquals(currentMax, ratingBar.getMax());
        assertEquals(currentProgress, ratingBar.getProgress());
        assertEquals(currentStepSize, ratingBar.getStepSize());
        ratingBar.setStepSize(0f);
        assertEquals(currentMax, ratingBar.getMax());
        assertEquals(currentProgress, ratingBar.getProgress());
        assertEquals(currentStepSize, ratingBar.getStepSize());
        ratingBar.setStepSize(ratingBar.getNumStars() + 0.1f);
        assertEquals(currentMax, ratingBar.getMax());
        assertEquals(currentProgress, ratingBar.getProgress());
        assertEquals(currentStepSize, ratingBar.getStepSize());
        ratingBar.setStepSize(Float.MAX_VALUE);
        assertEquals(currentMax, ratingBar.getMax());
        assertEquals(currentProgress, ratingBar.getProgress());
        assertEquals(currentStepSize, ratingBar.getStepSize());
    }
    private class MockOnRatingBarChangeListener implements OnRatingBarChangeListener {
        private boolean mCalledOnRatingChanged = false;
        boolean hasCalledOnRatingChanged() {
            return mCalledOnRatingChanged;
        }
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromTouch) {
            mCalledOnRatingChanged = true;
        }
        public void reset() {
            mCalledOnRatingChanged = false;
        }
    }
    private class MockRatingBar extends RatingBar {
        public MockRatingBar(Context context) {
            super(context);
        }
        private boolean mCalledOnMeasure = false;
        private boolean mCalledRequestLayout = false;
        public boolean hasCalledOnMeasure() {
            return mCalledOnMeasure;
        }
        @Override
        protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            mCalledOnMeasure = true;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        public void requestLayout() {
            mCalledRequestLayout = true;
            super.requestLayout();
        }
        public boolean hasCalledRequestLayout() {
            return mCalledRequestLayout;
        }
        public void reset() {
            mCalledOnMeasure = false;
            mCalledRequestLayout = false;
        }
    }
}
