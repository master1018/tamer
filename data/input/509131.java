@TestTargetClass(LinearInterpolator.class)
public class LinearInterpolatorTest extends ActivityInstrumentationTestCase2<AnimationTestStubActivity> {
    private Activity mActivity;
    private static final float ALPHA_DELTA = 0.001f;
    private static final long LINEAR_ALPHA_DURATION = 500;
    private static final long LINEAR_ALPHA_TIME_STEP = LINEAR_ALPHA_DURATION / 5;
    public LinearInterpolatorTest() {
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
            method = "LinearInterpolator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LinearInterpolator",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new LinearInterpolator();
        new LinearInterpolator(mActivity, null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInterpolation",
        args = {float.class}
    )
    public void testGetInterpolation() {
        LinearInterpolator interpolator = new LinearInterpolator();
        final float delta1 = interpolator.getInterpolation(0.1f)
                - interpolator.getInterpolation(0.0f);
        final float delta2 = interpolator.getInterpolation(0.2f)
                - interpolator.getInterpolation(0.1f);
        final float delta3 = interpolator.getInterpolation(0.3f)
                - interpolator.getInterpolation(0.2f);
        assertEquals(delta1, delta2, ALPHA_DELTA);
        assertEquals(delta2, delta3, ALPHA_DELTA);
    }
    public void testLinearInterpolator() {
        final View animWindow = mActivity.findViewById(R.id.anim_window);
        final Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.alpha);
        assertEquals(LINEAR_ALPHA_DURATION, anim.getDuration());
        assertTrue(anim instanceof AlphaAnimation);
        Interpolator interpolator = new LinearInterpolator();
        anim.setInterpolator(interpolator);
        assertFalse(anim.hasStarted());
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, anim);
        Transformation transformation = new Transformation();
        final long startTime = anim.getStartTime();
        anim.getTransformation(startTime, transformation);
        final float alpha1 = transformation.getAlpha();
        assertEquals(0.0f, alpha1, ALPHA_DELTA);
        anim.getTransformation(startTime + LINEAR_ALPHA_TIME_STEP, transformation);
        final float alpha2 = transformation.getAlpha();
        anim.getTransformation(startTime + LINEAR_ALPHA_TIME_STEP * 2, transformation);
        final float alpha3 = transformation.getAlpha();
        anim.getTransformation(startTime + LINEAR_ALPHA_TIME_STEP * 3, transformation);
        final float alpha4 = transformation.getAlpha();
        anim.getTransformation(startTime + LINEAR_ALPHA_TIME_STEP * 4, transformation);
        final float alpha5 = transformation.getAlpha();
        anim.getTransformation(startTime + LINEAR_ALPHA_DURATION, transformation);
        final float alpha6 = transformation.getAlpha();
        assertEquals(1.0f, alpha6, ALPHA_DELTA);
        final float delta1 = alpha2 - alpha1;
        final float delta2 = alpha3 - alpha2;
        final float delta3 = alpha4 - alpha3;
        final float delta4 = alpha5 - alpha4;
        final float delta5 = alpha6 - alpha5;
        assertEquals(delta1, delta2, ALPHA_DELTA);
        assertEquals(delta2, delta3, ALPHA_DELTA);
        assertEquals(delta3, delta4, ALPHA_DELTA);
        assertEquals(delta4, delta5, ALPHA_DELTA);
    }
}
