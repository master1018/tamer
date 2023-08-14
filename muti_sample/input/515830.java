@TestTargetClass(LayoutAnimationController.class)
public class LayoutAnimationControllerTest
        extends ActivityInstrumentationTestCase2<LayoutAnimStubActivity> {
    private ListActivity mActivity;
    private Animation mDefaultAnimation;
    private ListView mListView;
    private LayoutAnimationController mController;
    private static final int DURATION = 1000;
    private static final float DELTA = 0.1f;
    private static final int INDEX_OF_CHILD1 = 0;
    private static final int INDEX_OF_CHILD2 = 1;
    private static final int INDEX_OF_CHILD3 = 2;
    private static final float DEFAULT_DELAY = 0.5f;
    private static final long DEFAULT_MAX_DURATION = 2000;
    public LayoutAnimationControllerTest() {
        super("com.android.cts.stub", LayoutAnimStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mListView = mActivity.getListView();
        mDefaultAnimation = AnimationUtils.loadAnimation(mActivity,
                R.anim.layout_anim_controller_animation);
        mController = new LayoutAnimationController(mDefaultAnimation, DEFAULT_DELAY);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOrder",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOrder",
            args = {int.class}
        )
    })
    public void testAccessOrder() throws InterruptedException {
        mController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        assertEquals(LayoutAnimationController.ORDER_NORMAL, mController.getOrder());
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        Animation childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        long startTime = childAnimation1.getStartTime();
        assertEquals(0, childAnimation1.getStartOffset());
        assertEquals(500, childAnimation2.getStartOffset());
        assertEquals(1000, childAnimation3.getStartOffset());
        Transformation transformation1 = new Transformation();
        Transformation transformation2 = new Transformation();
        Transformation transformation3 = new Transformation();
        childAnimation1.getTransformation(startTime + 500, transformation1);
        childAnimation2.getTransformation(startTime + 500, transformation2);
        childAnimation3.getTransformation(startTime + 500, transformation3);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1000, transformation1);
        childAnimation2.getTransformation(startTime + 1000, transformation2);
        childAnimation3.getTransformation(startTime + 1000, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1500, transformation1);
        childAnimation2.getTransformation(startTime + 1500, transformation2);
        childAnimation3.getTransformation(startTime + 1500, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertEquals(1.0f, transformation2.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation3.getAlpha());
        mController.setOrder(LayoutAnimationController.ORDER_REVERSE);
        assertEquals(LayoutAnimationController.ORDER_REVERSE, mController.getOrder());
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        transformation1 = new Transformation();
        transformation2 = new Transformation();
        transformation3 = new Transformation();
        childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        startTime = childAnimation1.getStartTime();
        assertEquals(1000, childAnimation1.getStartOffset());
        assertEquals(500, childAnimation2.getStartOffset());
        assertEquals(0, childAnimation3.getStartOffset());
        childAnimation1.getTransformation(startTime + 500, transformation1);
        childAnimation2.getTransformation(startTime + 500, transformation2);
        childAnimation3.getTransformation(startTime + 500, transformation3);
        assertEquals(0.0f, transformation1.getAlpha(), DELTA);
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation3.getAlpha());
        childAnimation1.getTransformation(startTime + 1000, transformation1);
        childAnimation2.getTransformation(startTime + 1000, transformation2);
        childAnimation3.getTransformation(startTime + 1000, transformation3);
        assertEquals(0.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        assertEquals(1.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1500, transformation1);
        childAnimation2.getTransformation(startTime + 1500, transformation2);
        childAnimation3.getTransformation(startTime + 1500, transformation3);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(1.0f, transformation2.getAlpha(), DELTA);
        assertEquals(1.0f, transformation3.getAlpha(), DELTA);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDelay",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDelay",
            args = {float.class}
        )
    })
    public void testAccessDelay() throws InterruptedException {
        mController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        float delay = 1.5f;
        mController.setDelay(delay);
        assertEquals(delay, mController.getDelay());
        long maxDuration = (long) (delay * INDEX_OF_CHILD3 * DURATION + DURATION);
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                maxDuration);
        Animation childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        long startTime = childAnimation1.getStartTime();
        long offsetTime1 = childAnimation1.getStartOffset();
        long offsetTime2 = childAnimation2.getStartOffset();
        long offsetTime3 = childAnimation3.getStartOffset();
        assertEquals(0, offsetTime1);
        assertEquals(1500, offsetTime2);
        assertEquals(3000, offsetTime3);
        Transformation transformation1 = new Transformation();
        Transformation transformation2 = new Transformation();
        Transformation transformation3 = new Transformation();
        childAnimation1.getTransformation(startTime + 500, transformation1);
        childAnimation2.getTransformation(startTime + 500, transformation2);
        childAnimation3.getTransformation(startTime + 500, transformation3);
        assertIsRunningAnimation(transformation1.getAlpha());
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 1200, transformation1);
        childAnimation2.getTransformation(startTime + 1200, transformation2);
        childAnimation3.getTransformation(startTime + 1200, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertEquals(0.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 2000, transformation1);
        childAnimation2.getTransformation(startTime + 2000, transformation2);
        childAnimation3.getTransformation(startTime + 2000, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertIsRunningAnimation(transformation2.getAlpha());
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 2700, transformation1);
        childAnimation2.getTransformation(startTime + 2700, transformation2);
        childAnimation3.getTransformation(startTime + 2700, transformation3);
        assertEquals(1.0f, transformation1.getAlpha(), DELTA);
        assertEquals(1.0f, transformation2.getAlpha(), DELTA);
        assertEquals(0.0f, transformation3.getAlpha(), DELTA);
        childAnimation1.getTransformation(startTime + 3500, transformation1);
        childAnimation2.getTransformation(startTime + 3500, transformation2);
        childAnimation3.getTransformation(startTime + 3500, transformation3);
        assertIsRunningAnimation(transformation3.getAlpha());
    }
    private void assertIsRunningAnimation(float alpha) {
        assertTrue(alpha > 0.0f);
        assertTrue(alpha < 1.0f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAnimation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAnimation",
            args = {Animation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAnimation",
            args = {Context.class, int.class}
        )
    })
    public void testAccessAnimation() throws InterruptedException {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        animation.setFillAfter(true);
        long duration = 2000;
        mController.setAnimation(animation);
        assertSame(animation, mController.getAnimation());
        long maxDuration = (long) (DEFAULT_DELAY * INDEX_OF_CHILD3 * duration + duration);
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                maxDuration);
        Animation childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        assertAnimation(childAnimation1, false, duration);
        assertAnimation(childAnimation2, false, duration);
        assertAnimation(childAnimation3, false, duration);
        mController.setAnimation(mActivity, R.anim.layout_anim_controller_animation);
        Animation actualAnimation = mController.getAnimation();
        assertEquals(DURATION, actualAnimation.getDuration());
        assertTrue(actualAnimation.getInterpolator() instanceof AccelerateInterpolator);
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        assertAnimation(childAnimation1, true, DURATION);
        assertAnimation(childAnimation2, true, DURATION);
        assertAnimation(childAnimation3, true, DURATION);
    }
    private void assertAnimation(Animation animation, boolean isAccelerate, long duration) {
        Transformation transformation = new Transformation();
        long baseTime = animation.getStartTime() + animation.getStartOffset();
        animation.getTransformation(baseTime, transformation);
        long step = duration / 4;
        float alpha1 = transformation.getAlpha();
        animation.getTransformation(baseTime + step * 1, transformation);
        float alpha2 = transformation.getAlpha();
        animation.getTransformation(baseTime + step * 2, transformation);
        float alpha3 = transformation.getAlpha();
        animation.getTransformation(baseTime + step * 3, transformation);
        float alpha4 = transformation.getAlpha();
        animation.getTransformation(baseTime + step * 4, transformation);
        float alpha5 = transformation.getAlpha();
        float delta1 = alpha2 - alpha1;
        float delta2 = alpha3 - alpha2;
        float delta3 = alpha4 - alpha3;
        float delta4 = alpha5 - alpha4;
        if (isAccelerate) {
            assertTrue(delta1 < delta2);
            assertTrue(delta2 < delta3);
            assertTrue(delta3 < delta4);
        } else {
            assertTrue(delta1 > delta2);
            assertTrue(delta2 > delta3);
            assertTrue(delta3 > delta4);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInterpolator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInterpolator",
            args = {Interpolator.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInterpolator",
            args = {Context.class, int.class}
        )
    })
    public void testAccessInterpolator() throws InterruptedException {
        DecelerateInterpolator interpolator = new DecelerateInterpolator(1.0f);
        mController.setInterpolator(interpolator);
        assertSame(interpolator, mController.getInterpolator());
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        Animation childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        long delta1 = childAnimation2.getStartOffset() - childAnimation1.getStartOffset();
        long delta2 = childAnimation3.getStartOffset() - childAnimation2.getStartOffset();
        assertTrue(delta2 < delta1);
        mController.setInterpolator(mActivity, android.R.anim.accelerate_interpolator);
        assertTrue(mController.getInterpolator() instanceof AccelerateInterpolator);
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        delta1 = childAnimation2.getStartOffset() - childAnimation1.getStartOffset();
        delta2 = childAnimation3.getStartOffset() - childAnimation2.getStartOffset();
        assertTrue(delta2 > delta1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LayoutAnimationController",
            args = {Context.class, AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LayoutAnimationController",
            args = {Animation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LayoutAnimationController",
            args = {Animation.class, float.class}
        )
    })
    public void testConstructor() {
        XmlResourceParser parser = mActivity.getResources().getAnimation(
                R.anim.accelerate_decelerate_alpha);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new LayoutAnimationController(mActivity, attrs);
        new LayoutAnimationController(mDefaultAnimation, DEFAULT_DELAY);
        LayoutAnimationController controller = new LayoutAnimationController(mDefaultAnimation);
        assertEquals(DEFAULT_DELAY, controller.getDelay());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDelayForView",
        args = {View.class}
    )
    public void testGetDelayForView() throws Throwable {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        animation.setFillAfter(true);
        MyLayoutAnimationController controller = new MyLayoutAnimationController(animation);
        final AbsListView.LayoutParams layoutParams1 = setAnimationParameters(0);
        final AbsListView.LayoutParams layoutParams2 = setAnimationParameters(1);
        final AbsListView.LayoutParams layoutParams3 = setAnimationParameters(2);
        final View child1 = mListView.getChildAt(INDEX_OF_CHILD1);
        final View child2 = mListView.getChildAt(INDEX_OF_CHILD2);
        final View child3 = mListView.getChildAt(INDEX_OF_CHILD3);
        runTestOnUiThread(new Runnable() {
            public void run() {
                child1.setLayoutParams(layoutParams1);
                child2.setLayoutParams(layoutParams2);
                child3.setLayoutParams(layoutParams3);
            }
        });
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, controller,
                DEFAULT_MAX_DURATION);
        assertEquals(0, controller.getDelayForView(child1));
        assertEquals(1000, controller.getDelayForView(child2));
        assertEquals(2000, controller.getDelayForView(child3));
    }
    private AbsListView.LayoutParams setAnimationParameters(int index) {
        AnimationParameters animationParams = new AnimationParameters();
        animationParams.index = index;
        animationParams.count = 3;
        final AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.layoutAnimationParameters = animationParams;
        return layoutParams;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTransformedIndex",
        args = {AnimationParameters.class}
    )
    public void testGetTransformedIndex() {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        animation.setFillAfter(true);
        MyLayoutAnimationController controller = new MyLayoutAnimationController(animation);
        AnimationParameters animationParams = new AnimationParameters();
        animationParams.count = 3;
        animationParams.index = 0;
        assertEquals(0, controller.getTransformedIndex(animationParams));
        animationParams.index = 1;
        assertEquals(1, controller.getTransformedIndex(animationParams));
        animationParams.index = 2;
        assertEquals(2, controller.getTransformedIndex(animationParams));
        controller.setOrder(LayoutAnimationController.ORDER_REVERSE);
        animationParams.index = 0;
        assertEquals(2, controller.getTransformedIndex(animationParams));
        animationParams.index = 1;
        assertEquals(1, controller.getTransformedIndex(animationParams));
        animationParams.index = 2;
        assertEquals(0, controller.getTransformedIndex(animationParams));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "start",
        args = {}
    )
    public void testStart() {
        Animation animation = new ScaleAnimation(0.0f, 10.0f, 0.0f, 20.0f);
        animation.setStartTime(500);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        assertTrue(Animation.START_ON_FIRST_FRAME != controller.getAnimation().getStartTime());
        controller.start();
        assertEquals(Animation.START_ON_FIRST_FRAME, controller.getAnimation().getStartTime());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isDone",
        args = {}
    )
    @ToBeFixed(bug = "1799434", explanation = "isDone() always return true")
    public void testIsDone() throws InterruptedException {
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        assertTrue(mController.isDone());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAnimationForView",
        args = {View.class}
    )
    public void testGetAnimationForView() throws InterruptedException {
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        animation.setFillAfter(true);
        mController.setAnimation(animation);
        AnimationTestUtils.assertRunController(getInstrumentation(), mListView, mController,
                DEFAULT_MAX_DURATION);
        Animation childAnimation1 = mListView.getChildAt(INDEX_OF_CHILD1).getAnimation();
        Animation childAnimation2 = mListView.getChildAt(INDEX_OF_CHILD2).getAnimation();
        Animation childAnimation3 = mListView.getChildAt(INDEX_OF_CHILD3).getAnimation();
        long duration = 2000;
        assertAnimation(childAnimation1, false, duration);
        assertAnimation(childAnimation2, false, duration);
        assertAnimation(childAnimation3, false, duration);
        assertEquals(0, childAnimation1.getStartOffset());
        assertEquals(1000, childAnimation2.getStartOffset());
        assertEquals(2000, childAnimation3.getStartOffset());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "willOverlap",
        args = {}
    )
    public void testWillOverlap() {
        LayoutAnimationController controller = new LayoutAnimationController(mDefaultAnimation);
        controller.setDelay(0.5f);
        assertTrue(controller.willOverlap());
        controller.setDelay(1.0f);
        assertFalse(controller.willOverlap());
        controller.setDelay(1.5f);
        assertFalse(controller.willOverlap());
    }
    private class MyLayoutAnimationController extends LayoutAnimationController {
        public MyLayoutAnimationController(Animation animation) {
            super(animation);
        }
        protected int getTransformedIndex(AnimationParameters params) {
            return super.getTransformedIndex(params);
        }
        protected long getDelayForView(View view) {
            return super.getDelayForView(view);
        }
    }
}
