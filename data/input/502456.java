@TestTargetClass(DecelerateInterpolator.class)
public class DecelerateInterpolatorTest
        extends ActivityInstrumentationTestCase2<AnimationTestStubActivity> {
    private Activity mActivity;
    private static final float ALPHA_DELTA = 0.001f;
    private static final long DECELERATE_ALPHA_DURATION = 2000;
    public DecelerateInterpolatorTest() {
        super("com.android.cts.stub", AnimationTestStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link DecelerateInterpolator}",
            method = "DecelerateInterpolator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link DecelerateInterpolator}",
            method = "DecelerateInterpolator",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link DecelerateInterpolator}",
            method = "DecelerateInterpolator",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new DecelerateInterpolator();
        new DecelerateInterpolator(1.0f);
        XmlResourceParser parser = mActivity.getResources().getAnimation(R.anim.decelerate_alpha);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new DecelerateInterpolator(mActivity, attrs);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test case will decelerate AlphaAnimation. It will change alpha from 0.0 to"
                + " 1.0, the rate of change alpha starts out quickly and then decelerates.",
        method = "getInterpolation",
        args = {float.class}
    )
    public void testDecelerateInterpolator() {
        final View animWindow = mActivity.findViewById(R.id.anim_window);
        final Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.decelerate_alpha);
        assertEquals(DECELERATE_ALPHA_DURATION, anim.getDuration());
        assertTrue(anim instanceof AlphaAnimation);
        Interpolator interpolator = new DecelerateInterpolator(1.0f);
        anim.setInterpolator(interpolator);
        assertFalse(anim.hasStarted());
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim);
        Transformation transformation = new Transformation();
        long startTime = anim.getStartTime();
        anim.getTransformation(startTime, transformation);
        float alpha1 = transformation.getAlpha();
        assertEquals(0.0f, alpha1, ALPHA_DELTA);
        anim.getTransformation(startTime + 500, transformation);
        float alpha2 = transformation.getAlpha();
        anim.getTransformation(startTime + 1000, transformation);
        float alpha3 = transformation.getAlpha();
        anim.getTransformation(startTime + 1500, transformation);
        float alpha4 = transformation.getAlpha();
        anim.getTransformation(startTime + DECELERATE_ALPHA_DURATION, transformation);
        float alpha5 = transformation.getAlpha();
        assertEquals(1.0f, alpha5, ALPHA_DELTA);
        float delta1 = alpha2 - alpha1;
        float delta2 = alpha3 - alpha2;
        float delta3 = alpha4 - alpha3;
        float delta4 = alpha5 - alpha4;
        assertTrue(delta1 > delta2);
        assertTrue(delta2 > delta3);
        assertTrue(delta3 > delta4);
        interpolator = new DecelerateInterpolator(1.5f);
        anim.setInterpolator(interpolator);
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim);
        transformation = new Transformation();
        startTime = anim.getStartTime();
        anim.getTransformation(startTime, transformation);
        float alpha6 = transformation.getAlpha();
        assertEquals(0.0f, alpha1, ALPHA_DELTA);
        anim.getTransformation(startTime + 500, transformation);
        float alpha7 = transformation.getAlpha();
        anim.getTransformation(startTime + 1000, transformation);
        float alpha8 = transformation.getAlpha();
        anim.getTransformation(startTime + 1500, transformation);
        float alpha9 = transformation.getAlpha();
        anim.getTransformation(startTime + DECELERATE_ALPHA_DURATION, transformation);
        float alpha10 = transformation.getAlpha();
        assertEquals(1.0f, alpha5, ALPHA_DELTA);
        float delta5 = alpha7 - alpha6;
        float delta6 = alpha8 - alpha7;
        float delta7 = alpha9 - alpha8;
        float delta8 = alpha10 - alpha9;
        assertTrue(delta5 > delta6);
        assertTrue(delta6 > delta7);
        assertTrue(delta7 > delta8);
        assertTrue(delta5 > delta1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link DecelerateInterpolator#getInterpolation(float)}.",
        method = "getInterpolation",
        args = {float.class}
    )
    public void testGetInterpolation() {
        final float input = 0.25f;
        Interpolator interpolator1 = new DecelerateInterpolator(1.0f);
        Interpolator interpolator2 = new DecelerateInterpolator(2.0f);
        float delta1 = interpolator1.getInterpolation(input);
        float delta2 = interpolator2.getInterpolation(input);
        assertTrue(delta2 > delta1);
    }
}
