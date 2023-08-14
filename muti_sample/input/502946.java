@TestTargetClass(android.view.animation.ScaleAnimation.class)
public class ScaleAnimationTest
        extends ActivityInstrumentationTestCase2<AnimationTestStubActivity> {
    private static long DURATION = 1000;
    private static float DELTA = 0.001f;
    private static float FROM_X = 1.0f;
    private static float TO_X = 0.6f;
    private static float FROM_Y = 3.0f;
    private static float TO_Y = 3.6f;
    private static float PIVOT_X = 0.6f;
    private static float PIVOT_Y = 0.6f;
    private static float MID_X = 0.8f;
    private static float MID_Y = 3.3f;
    private AnimationTestStubActivity mActivity;
    public ScaleAnimationTest() {
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
            method = "ScaleAnimation",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ScaleAnimation",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ScaleAnimation",
            args = {float.class, float.class, float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ScaleAnimation",
            args = {float.class, float.class, float.class, float.class, int.class, float.class,
                    int.class, float.class}
        )
    })
    public void testConstructors() {
        final XmlResourceParser parser = mActivity.getResources().getAnimation(
                R.anim.anim_scale);
        final AttributeSet attr = Xml.asAttributeSet(parser);
        assertNotNull(attr);
        new ScaleAnimation(mActivity, attr);
        new ScaleAnimation(FROM_X, TO_X, FROM_Y, TO_Y);
        new ScaleAnimation(FROM_X, TO_X, FROM_Y, TO_Y, Animation.RELATIVE_TO_SELF, PIVOT_X,
                Animation.RELATIVE_TO_SELF, PIVOT_Y);
        new ScaleAnimation(FROM_X, TO_X, FROM_Y, TO_Y, PIVOT_X, PIVOT_Y);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Use applyTransformation directly",
            method = "applyTransformation",
            args = {float.class, android.view.animation.Transformation.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "initialize",
            args = {int.class, int.class, int.class, int.class}
        )
    })
    public void testApplyTransformation() {
        final Transformation transformation = new Transformation();
        transformation.setTransformationType(Transformation.TYPE_MATRIX);
        MyScaleAnimation scaleAnimation = new MyScaleAnimation(FROM_X, TO_X, FROM_Y, TO_Y,
                PIVOT_X, PIVOT_Y);
        float values[] = new float[9];
        scaleAnimation.initialize(50, 50, 100, 100);
        scaleAnimation.applyTransformation(0.0f, transformation);
        transformation.getMatrix().getValues(values);
        assertMatrixValue(FROM_X, FROM_Y, values);
        float trans1X = values[Matrix.MTRANS_X];
        float trans1Y = values[Matrix.MTRANS_Y];
        scaleAnimation.applyTransformation(0.5f, transformation);
        transformation.getMatrix().getValues(values);
        assertMatrixValue(MID_X, MID_Y, values);
        float trans2X = values[Matrix.MTRANS_X];
        float trans2Y = values[Matrix.MTRANS_Y];
        scaleAnimation.applyTransformation(1.0f, transformation);
        transformation.getMatrix().getValues(values);
        assertMatrixValue(TO_X, TO_Y, values);
        float trans3X = values[Matrix.MTRANS_X];
        float trans3Y = values[Matrix.MTRANS_Y];
        assertTrue(Math.abs(trans1X) < Math.abs(trans2X));
        assertTrue(Math.abs(trans2X) < Math.abs(trans3X));
        assertTrue(Math.abs(trans1Y) < Math.abs(trans2Y));
        assertTrue(Math.abs(trans2Y) < Math.abs(trans3Y));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Use applyTransformation indirectly with Animation#getTransformation",
        method = "applyTransformation",
        args = {float.class, android.view.animation.Transformation.class}
    )
    public void testApplyTransformationIndirectly() {
        final View animWindow = mActivity.findViewById(R.id.anim_window);
        final Transformation transformation = new Transformation();
        transformation.setTransformationType(Transformation.TYPE_MATRIX);
        MyScaleAnimation scaleAnimation = new MyScaleAnimation(FROM_X, TO_X, FROM_Y, TO_Y,
                PIVOT_X, PIVOT_Y);
        scaleAnimation.setDuration(DURATION);
        scaleAnimation.initialize(50, 50, 100, 100);
        AnimationTestUtils.assertRunAnimation(getInstrumentation(), animWindow, scaleAnimation);
        float values[] = new float[9];
        long startTime = scaleAnimation.getStartTime();
        scaleAnimation.getTransformation(startTime, transformation);
        assertNotNull(scaleAnimation.getInterpolator());
        transformation.getMatrix().getValues(values);
        assertMatrixValue(FROM_X, FROM_Y, values);
        float trans1X = values[Matrix.MTRANS_X];
        float trans1Y = values[Matrix.MTRANS_Y];
        scaleAnimation.getTransformation(startTime + (DURATION / 2), transformation);
        transformation.getMatrix().getValues(values);
        assertMatrixValue(MID_X, MID_Y, values);
        float trans2X = values[Matrix.MTRANS_X];
        float trans2Y = values[Matrix.MTRANS_Y];
        scaleAnimation.getTransformation(startTime + DURATION, transformation);
        transformation.getMatrix().getValues(values);
        assertMatrixValue(TO_X, TO_Y, values);
        float trans3X = values[Matrix.MTRANS_X];
        float trans3Y = values[Matrix.MTRANS_Y];
        assertTrue(Math.abs(trans1X) < Math.abs(trans2X));
        assertTrue(Math.abs(trans2X) < Math.abs(trans3X));
        assertTrue(Math.abs(trans1Y) < Math.abs(trans2Y));
        assertTrue(Math.abs(trans2Y) < Math.abs(trans3Y));
    }
    private void assertMatrixValue(float expectedX, float expectedY, float[] values) {
        assertEquals(expectedX, values[Matrix.MSCALE_X], DELTA);
        assertEquals(expectedY, values[Matrix.MSCALE_Y], DELTA);
    }
    private class MyScaleAnimation extends ScaleAnimation {
        public MyScaleAnimation(float fromX, float toX, float fromY, float toY,
                float pivotXValue, float pivotYValue) {
            super(fromX, toX, fromY, toY, pivotXValue, pivotYValue);
        }
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
        }
    }
}
