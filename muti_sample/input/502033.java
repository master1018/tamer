@TestTargetClass(AlphaAnimation.class)
public class AlphaAnimationTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link AlphaAnimation}",
            method = "AlphaAnimation",
            args = {float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link AlphaAnimation}",
            method = "AlphaAnimation",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        XmlResourceParser parser = mContext.getResources().getAnimation(R.anim.alpha);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        new AlphaAnimation(mContext, attrs);
        new AlphaAnimation(0.0f, 1.0f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link AlphaAnimation#willChangeBounds()}. This method always" +
                    " returns false, a alpha animation will not affect the bounds",
            method = "willChangeBounds",
            args = {}
        )
    })
    public void testWillChangeBounds() {
        AlphaAnimation animation = new AlphaAnimation(mContext, null);
        assertFalse(animation.willChangeBounds());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "test {@link AlphaAnimation#willChangeTransformationMatrix()}. This method" +
                " always returns false, a alpha animation will not affect the matrix",
        method = "willChangeTransformationMatrix",
        args = {}
    )
    public void testWillChangeTransformationMatrix() {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 0.5f);
        assertFalse(animation.willChangeTransformationMatrix());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link AlphaAnimation#applyTransformation(float, Transformation)}",
        method = "applyTransformation",
        args = {float.class, android.view.animation.Transformation.class}
    )
    public void testApplyTransformation() {
        MyAlphaAnimation animation = new MyAlphaAnimation(0.0f, 1.0f);
        Transformation transformation = new Transformation();
        assertEquals(1.0f, transformation.getAlpha(), 0.0001f);
        animation.applyTransformation(0.0f, transformation);
        assertEquals(0.0f, transformation.getAlpha(), 0.0001f);
        animation.applyTransformation(0.5f, transformation);
        assertEquals(0.5f, transformation.getAlpha(), 0.0001f);
        animation.applyTransformation(1.0f, transformation);
        assertEquals(1.0f, transformation.getAlpha(), 0.0001f);
        animation = new MyAlphaAnimation(0.2f, 0.9f);
        transformation = new Transformation();
        assertEquals(1.0f, transformation.getAlpha(), 0.0001f);
        animation.applyTransformation(0.0f, transformation);
        assertEquals(0.2f, transformation.getAlpha(), 0.0001f);
        animation.applyTransformation(0.5f, transformation);
        assertEquals(0.55f, transformation.getAlpha(), 0.0001f);
        animation.applyTransformation(1.0f, transformation);
        assertEquals(0.9f, transformation.getAlpha(), 0.0001f);
    }
    private final class MyAlphaAnimation extends AlphaAnimation {
        public MyAlphaAnimation(float fromAlpha, float toAlpha) {
            super(fromAlpha, toAlpha);
        }
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
        }
    }
}
