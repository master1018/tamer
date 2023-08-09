@TestTargetClass(GridLayoutAnimationController.class)
public class GridLayoutAnimationControllerTest
    extends ActivityInstrumentationTestCase2<GridLayoutAnimStubActivity> {
    private GridLayoutAnimStubActivity mActivity;
    private Animation mDefaultAnimation;
    private GridLayoutAnimationController mController;
    private GridView mGridView;
    private static final float DEFAULT_DELAY = 0.5f;
    private static final long DEFAULT_MAX_DURATION = 5000;
    private static final float DELTA = 0.1f;
    private static final int INDEX_OF_CHILD1 = 0;
    private static final int INDEX_OF_CHILD2 = 1;
    private static final int INDEX_OF_CHILD3 = 2;
    private static final int INDEX_OF_CHILD4 = 3;
    private static final int INDEX_OF_CHILD5 = 4;
    private static final int INDEX_OF_CHILD6 = 5;
    private static final int INDEX_OF_CHILD7 = 6;
    private static final int INDEX_OF_CHILD8 = 7;
    private static final int INDEX_OF_CHILD9 = 8;
    public GridLayoutAnimationControllerTest() {
        super("com.android.cts.stub", GridLayoutAnimStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mDefaultAnimation = AnimationUtils.loadAnimation(mActivity,
                R.anim.layout_anim_controller_animation);
        mController = new GridLayoutAnimationController(mDefaultAnimation, DEFAULT_DELAY,
                DEFAULT_DELAY);
        mGridView = mActivity.getGridView();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "GridLayoutAnimationController",
            args = {Context.class, AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "GridLayoutAnimationController",
            args = {Animation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "GridLayoutAnimationController",
            args = {Animation.class, float.class, float.class}
        )
    })
    public void testConstructor() {
        XmlResourceParser parser = mActivity.getResources().getAnimation(
                R.anim.accelerate_decelerate_alpha);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new GridLayoutAnimationController(mActivity, attrs);
        GridLayoutAnimationController controller =
                new GridLayoutAnimationController(mDefaultAnimation);
        assertEquals(DEFAULT_DELAY, controller.getRowDelay());
        assertEquals(DEFAULT_DELAY, controller.getColumnDelay());
        new GridLayoutAnimationController(mDefaultAnimation, 0.5f, 0.5f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getColumnDelay",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setColumnDelay",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRowDelay",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRowDelay",
            args = {float.class}
        )
    })
    public void testAccessDelay() throws InterruptedException {
        float delay = 1.5f;
        long maxDuration = 13000;
        mController.setRowDelay(delay);
        assertEquals(delay, mController.getRowDelay());
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, mController,
                maxDuration);
        Animation childAnimation1 = mGridView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation4 = mGridView.getChildAt(INDEX_OF_CHILD4).getAnimation();
        Animation childAnimation7 = mGridView.getChildAt(INDEX_OF_CHILD7).getAnimation();
        assertChildrenDelay(childAnimation1, childAnimation4, childAnimation7);
        mController.setColumnDelay(delay);
        assertEquals(delay, mController.getColumnDelay());
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, mController,
                maxDuration);
        childAnimation1 = mGridView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mGridView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mGridView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        assertChildrenDelay(childAnimation1, childAnimation2, childAnimation3);
    }
    private void assertChildrenDelay(Animation child1, Animation child2, Animation child3) {
        long startTime = child1.getStartTime();
        long offsetTime1 = child1.getStartOffset();
        long offsetTime2 = child2.getStartOffset();
        long offsetTime3 = child3.getStartOffset();
        assertEquals(0, offsetTime1);
        assertEquals(1500, offsetTime2);
        assertEquals(3000, offsetTime3);
        Transformation transformation1 = new Transformation();
        Transformation transformation2 = new Transformation();
        Transformation transformation3 = new Transformation();
        child1.getTransformation(startTime + 500, transformation1);
        child2.getTransformation(startTime + 500, transformation2);
        child3.getTransformation(startTime + 500, transformation3);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        child1.getTransformation(startTime + 1200, transformation1);
        child2.getTransformation(startTime + 1200, transformation2);
        child3.getTransformation(startTime + 1200, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        child1.getTransformation(startTime + 2000, transformation1);
        child2.getTransformation(startTime + 2000, transformation2);
        child3.getTransformation(startTime + 2000, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        child1.getTransformation(startTime + 2700, transformation1);
        child2.getTransformation(startTime + 2700, transformation2);
        child3.getTransformation(startTime + 2700, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertEquals(1.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        child1.getTransformation(startTime + 3500, transformation1);
        child2.getTransformation(startTime + 3500, transformation2);
        child3.getTransformation(startTime + 3500, transformation3);
        assertIsRunningAnimation(transformation3.getAlpha());
    }
    private void assertIsRunningAnimation(float alpha) {
        assertTrue(alpha > 0.0f);
        assertTrue(alpha < 1.0f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDirection",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDirection",
            args = {int.class}
        )
    })
    public void testAccessDirection() throws InterruptedException {
        mController.setDirection(GridLayoutAnimationController.DIRECTION_BOTTOM_TO_TOP);
        assertEquals(GridLayoutAnimationController.DIRECTION_BOTTOM_TO_TOP,
                mController.getDirection());
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, mController,
                DEFAULT_MAX_DURATION);
        Animation childAnimation1 = mGridView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation4 = mGridView.getChildAt(INDEX_OF_CHILD4).getAnimation();
        Animation childAnimation7 = mGridView.getChildAt(INDEX_OF_CHILD7).getAnimation();
        long startTime = childAnimation7.getStartTime();
        assertEquals(1000, childAnimation1.getStartOffset());
        assertEquals(500, childAnimation4.getStartOffset());
        assertEquals(0, childAnimation7.getStartOffset());
        Transformation transformation1 = new Transformation();
        Transformation transformation2 = new Transformation();
        Transformation transformation3 = new Transformation();
        childAnimation1.getTransformation(startTime + 500, transformation1);
        childAnimation4.getTransformation(startTime + 500, transformation2);
        childAnimation7.getTransformation(startTime + 500, transformation3);
        assertEquals(0.0f, transformation1.getAlpha(), DELTA);
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation3.getAlpha());
        childAnimation1.getTransformation(startTime + 1000, transformation1);
        childAnimation4.getTransformation(startTime + 1000, transformation2);
        childAnimation7.getTransformation(startTime + 1000, transformation3);
        assertEquals(0.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        assertEquals(1.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1500, transformation1);
        childAnimation4.getTransformation(startTime + 1500, transformation2);
        childAnimation7.getTransformation(startTime + 1500, transformation3);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(1.0f, transformation2.getAlpha(), DELTA);
        assertEquals(1.0f, transformation3.getAlpha(), DELTA);
        mController.setDirection(GridLayoutAnimationController.DIRECTION_TOP_TO_BOTTOM);
        assertEquals(GridLayoutAnimationController.DIRECTION_TOP_TO_BOTTOM,
                mController.getDirection());
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, mController,
                DEFAULT_MAX_DURATION);
        transformation1 = new Transformation();
        transformation2 = new Transformation();
        transformation3 = new Transformation();
        childAnimation1 = mGridView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        childAnimation4 = mGridView.getChildAt(INDEX_OF_CHILD4).getAnimation();
        childAnimation7 = mGridView.getChildAt(INDEX_OF_CHILD7).getAnimation();
        startTime = childAnimation1.getStartTime();
        assertEquals(0, childAnimation1.getStartOffset());
        assertEquals(500, childAnimation4.getStartOffset());
        assertEquals(1000, childAnimation7.getStartOffset());
        childAnimation1.getTransformation(startTime + 500, transformation1);
        childAnimation4.getTransformation(startTime + 500, transformation2);
        childAnimation7.getTransformation(startTime + 500, transformation3);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1000, transformation1);
        childAnimation4.getTransformation(startTime + 1000, transformation2);
        childAnimation7.getTransformation(startTime + 1000, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1500, transformation1);
        childAnimation4.getTransformation(startTime + 1500, transformation2);
        childAnimation7.getTransformation(startTime + 1500, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertEquals(1.0f, transformation2.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation3.getAlpha());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDelayForView",
        args = {View.class}
    )
    public void testGetDelayForView() throws Throwable {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        animation.setFillAfter(true);
        MyGridLayoutAnimationController controller = new MyGridLayoutAnimationController(animation);
        final AbsListView.LayoutParams layoutParams1 = setAnimationParameters(0);
        final AbsListView.LayoutParams layoutParams2 = setAnimationParameters(1);
        final AbsListView.LayoutParams layoutParams3 = setAnimationParameters(2);
        final AbsListView.LayoutParams layoutParams4 = setAnimationParameters(3);
        final AbsListView.LayoutParams layoutParams5 = setAnimationParameters(4);
        final AbsListView.LayoutParams layoutParams6 = setAnimationParameters(5);
        final AbsListView.LayoutParams layoutParams7 = setAnimationParameters(6);
        final AbsListView.LayoutParams layoutParams8 = setAnimationParameters(7);
        final AbsListView.LayoutParams layoutParams9 = setAnimationParameters(8);
        final View child1 = mGridView.getChildAt(INDEX_OF_CHILD1);
        final View child2 = mGridView.getChildAt(INDEX_OF_CHILD2);
        final View child3 = mGridView.getChildAt(INDEX_OF_CHILD3);
        final View child4 = mGridView.getChildAt(INDEX_OF_CHILD4);
        final View child5 = mGridView.getChildAt(INDEX_OF_CHILD5);
        final View child6 = mGridView.getChildAt(INDEX_OF_CHILD6);
        final View child7 = mGridView.getChildAt(INDEX_OF_CHILD7);
        final View child8 = mGridView.getChildAt(INDEX_OF_CHILD8);
        final View child9 = mGridView.getChildAt(INDEX_OF_CHILD9);
        runTestOnUiThread(new Runnable() {
            public void run() {
                child1.setLayoutParams(layoutParams1);
                child2.setLayoutParams(layoutParams2);
                child3.setLayoutParams(layoutParams3);
                child4.setLayoutParams(layoutParams4);
                child5.setLayoutParams(layoutParams5);
                child6.setLayoutParams(layoutParams6);
                child7.setLayoutParams(layoutParams7);
                child8.setLayoutParams(layoutParams8);
                child9.setLayoutParams(layoutParams9);
            }
        });
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, controller,
                DEFAULT_MAX_DURATION);
        assertEquals(0, controller.getDelayForView(child1));
        assertEquals(1000, controller.getDelayForView(child2));
        assertEquals(2000, controller.getDelayForView(child3));
        assertEquals(1000, controller.getDelayForView(child4));
        assertEquals(2000, controller.getDelayForView(child5));
        assertEquals(3000, controller.getDelayForView(child6));
        assertEquals(2000, controller.getDelayForView(child7));
        assertEquals(3000, controller.getDelayForView(child8));
        assertEquals(4000, controller.getDelayForView(child9));
    }
    private AbsListView.LayoutParams setAnimationParameters(int index) {
        AnimationParameters animationParams = new AnimationParameters();
        animationParams.index = index;
        animationParams.count = 9;
        final AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.layoutAnimationParameters = animationParams;
        return layoutParams;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDirectionPriority",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDirectionPriority",
            args = {int.class}
        )
    })
    public void testAccessDirectionPriority() throws InterruptedException {
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, mController,
                DEFAULT_MAX_DURATION);
        Animation childAnimation1 = mGridView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mGridView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mGridView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        Animation childAnimation7 = mGridView.getChildAt(INDEX_OF_CHILD7).getAnimation();
        Animation childAnimation8 = mGridView.getChildAt(INDEX_OF_CHILD8).getAnimation();
        long startTime = childAnimation1.getStartTime();
        assertEquals(500, childAnimation2.getStartOffset());
        assertEquals(1000, childAnimation3.getStartOffset());
        assertEquals(1000, childAnimation7.getStartOffset());
        assertEquals(1500, childAnimation8.getStartOffset());
        Transformation transformation1 = new Transformation();
        Transformation transformation2 = new Transformation();
        childAnimation2.getTransformation(startTime + 700, transformation1);
        childAnimation7.getTransformation(startTime + 700, transformation2);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        childAnimation3.getTransformation(startTime + 1200, transformation1);
        childAnimation8.getTransformation(startTime + 1200, transformation2);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        mController.setDirectionPriority(GridLayoutAnimationController.PRIORITY_COLUMN);
        assertEquals(GridLayoutAnimationController.PRIORITY_COLUMN,
                mController.getDirectionPriority());
        AnimationTestUtils.assertRunController(getInstrumentation(), mGridView, mController,
                DEFAULT_MAX_DURATION);
        childAnimation1 = mGridView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        childAnimation2 = mGridView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        childAnimation3 = mGridView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        childAnimation7 = mGridView.getChildAt(INDEX_OF_CHILD7).getAnimation();
        childAnimation8 = mGridView.getChildAt(INDEX_OF_CHILD8).getAnimation();
        startTime = childAnimation1.getStartTime();
        assertEquals(1500, childAnimation2.getStartOffset());
        assertEquals(3000, childAnimation3.getStartOffset());
        assertEquals(1000, childAnimation7.getStartOffset());
        assertEquals(2500, childAnimation8.getStartOffset());
        transformation1 = new Transformation();
        transformation2 = new Transformation();
        childAnimation2.getTransformation(startTime + 1200, transformation1);
        childAnimation7.getTransformation(startTime + 1200, transformation2);
        assertEquals(0.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        childAnimation3.getTransformation(startTime + 2700, transformation1);
        childAnimation8.getTransformation(startTime + 2700, transformation2);
        assertEquals(0.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "willOverlap",
        args = {}
    )
    public void testWillOverlap() {
        GridLayoutAnimationController controller = new GridLayoutAnimationController(
                mDefaultAnimation);
        controller.setColumnDelay(0.5f);
        controller.setRowDelay(0.5f);
        assertTrue(controller.willOverlap());
        controller.setColumnDelay(0.5f);
        controller.setRowDelay(1.0f);
        assertTrue(controller.willOverlap());
        controller.setColumnDelay(1.0f);
        controller.setRowDelay(0.5f);
        assertTrue(controller.willOverlap());
        controller.setColumnDelay(1.0f);
        controller.setRowDelay(1.0f);
        assertFalse(controller.willOverlap());
    }
    private class MyGridLayoutAnimationController extends GridLayoutAnimationController {
        public MyGridLayoutAnimationController(Animation animation) {
            super(animation);
        }
        protected long getDelayForView(View view) {
            return super.getDelayForView(view);
        }
    }
}
