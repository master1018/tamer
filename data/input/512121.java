@TestTargetClass(AccelerateDecelerateInterpolator.class)
public class AccelerateDecelerateInterpolatorTest
        extends ActivityInstrumentationTestCase2<AnimationTestStubActivity> {
    private Activity mActivity;
    private static final float ALPHA_DELTA = 0.001f;
    private static final long ALPHA_DURATION = 2000;
    public AccelerateDecelerateInterpolatorTest() {
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
            method = "AccelerateDecelerateInterpolator",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AccelerateDecelerateInterpolator",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new AccelerateDecelerateInterpolator();
        XmlResourceParser parser = mActivity.getResources().getAnimation(
                R.anim.accelerate_decelerate_alpha);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new AccelerateDecelerateInterpolator(mActivity, attrs);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test case will accelerate then decelerate AlphaAnimation. It will change"
              + " alpha from 0.0 to 1.0, the rate of changing alpha starts out slowly, then"
              + " accelerates to the middle, and then decelerates to the end",
        method = "getInterpolation",
        args = {float.class}
    )
    public void testAccelerateDecelerateInterpolator() {
        final View animWindow = mActivity.findViewById(R.id.anim_window);
        final Animation anim = AnimationUtils.loadAnimation(mActivity,
                R.anim.accelerate_decelerate_alpha);
        assertEquals(ALPHA_DURATION, anim.getDuration());
        assertTrue(anim instanceof AlphaAnimation);
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
        anim.getTransformation(startTime + ALPHA_DURATION, transformation);
        float alpha5 = transformation.getAlpha();
        assertEquals(1.0f, alpha5, ALPHA_DELTA);
        float delta1 = alpha2 - alpha1;
        float delta2 = alpha3 - alpha2;
        float delta3 = alpha4 - alpha3;
        float delta4 = alpha5 - alpha4;
        assertTrue(delta1 < delta2);
        assertTrue(delta3 > delta4);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getInterpolation(float), call it directly.",
        method = "getInterpolation",
        args = {float.class}
    )
    public void testGetInterpolation() {
        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        float alpha1 = interpolator.getInterpolation(0f);
        float alpha2 = interpolator.getInterpolation(0.25f);
        float alpha3 = interpolator.getInterpolation(0.5f);
        float alpha4 = interpolator.getInterpolation(0.75f);
        float alpha5 = interpolator.getInterpolation(1f);
        float delta1 = alpha2 - alpha1;
        float delta2 = alpha3 - alpha2;
        float delta3 = alpha4 - alpha3;
        float delta4 = alpha5 - alpha4;
        assertTrue(delta1 < delta2);
        assertTrue(delta3 > delta4);
    }
}
