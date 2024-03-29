@TestTargetClass(Transformation.class)
public class TransformationTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Transformation",
        args = {}
    )
    public void testConstructor() {
        new Transformation();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "compose",
        args = {Transformation.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "{@link Transformation#compose(Transformation t)}"
            + "needs to update Javadoc to declare how it composed.")
    public void testCompose() {
        final Transformation t1 = new Transformation();
        final Transformation t2 = new Transformation();
        t1.setAlpha(0.5f);
        t2.setAlpha(0.4f);
        t1.getMatrix().setScale(3, 1);
        t2.getMatrix().setScale(3, 1);
        t1.setTransformationType(Transformation.TYPE_MATRIX);
        t2.setTransformationType(Transformation.TYPE_ALPHA);
        t2.compose(t1);
        Matrix expectedMatrix = new Matrix();
        expectedMatrix.setScale(9, 1);
        assertEquals(expectedMatrix, t2.getMatrix());
        assertEquals(0.4f * 0.5f, t2.getAlpha());
        assertEquals(Transformation.TYPE_ALPHA, t2.getTransformationType());
        t1.setTransformationType(Transformation.TYPE_IDENTITY);
        t2.compose(t1);
        expectedMatrix = new Matrix();
        expectedMatrix.setScale(27, 1);
        assertEquals(expectedMatrix, t2.getMatrix());
        assertEquals(0.4f * 0.5f * 0.5f, t2.getAlpha());
        assertEquals(Transformation.TYPE_ALPHA, t2.getTransformationType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clear",
        args = {}
    )
    public void testClear() {
        final Transformation t1 = new Transformation();
        final Transformation t2 = new Transformation();
        t2.set(t1);
        assertTransformationEquals(t1, t2);
        t2.setAlpha(0.0f);
        t2.getMatrix().setScale(2, 3);
        t2.setTransformationType(Transformation.TYPE_ALPHA);
        assertTransformationNotSame(t1, t2);
        t2.clear();
        assertTransformationEquals(t1, t2);
    }
    private void assertTransformationNotSame(Transformation expected, Transformation actual) {
        assertNotSame(expected.getAlpha(), actual.getAlpha());
        assertFalse(expected.getMatrix().equals(actual.getMatrix()));
        assertNotSame(expected.getTransformationType(), actual.getTransformationType());
    }
    private void assertTransformationEquals(Transformation expected, Transformation actual) {
        assertEquals(expected.getAlpha(), actual.getAlpha());
        assertEquals(expected.getMatrix(), actual.getMatrix());
        assertEquals(expected.getTransformationType(), actual.getTransformationType());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTransformationType",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTransformationType",
            args = {}
        )
    })
    public void testAccessTransformationType() {
        final Transformation transformation = new Transformation();
        assertEquals(Transformation.TYPE_BOTH, transformation.getTransformationType());
        transformation.setTransformationType(Transformation.TYPE_IDENTITY);
        assertEquals(Transformation.TYPE_IDENTITY, transformation.getTransformationType());
        transformation.setTransformationType(Transformation.TYPE_ALPHA);
        assertEquals(Transformation.TYPE_ALPHA, transformation.getTransformationType());
        transformation.setTransformationType(Transformation.TYPE_MATRIX);
        assertEquals(Transformation.TYPE_MATRIX, transformation.getTransformationType());
        transformation.setTransformationType(Transformation.TYPE_BOTH);
        assertEquals(Transformation.TYPE_BOTH, transformation.getTransformationType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {Transformation.class}
    )
    public void testSet() {
        final Transformation t1 = new Transformation();
        t1.setAlpha(0.0f);
        final Transformation t2 = new Transformation();
        t2.set(t1);
        assertTransformationEquals(t1, t2);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setAlpha",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getAlpha",
            args = {}
        )
    })
    public void testAccessAlpha() {
        final Transformation transformation = new Transformation();
        transformation.setAlpha(0.0f);
        assertEquals(0.0f, transformation.getAlpha());
        transformation.setAlpha(0.5f);
        assertEquals(0.5f, transformation.getAlpha());
        transformation.setAlpha(1.0f);
        assertEquals(1.0f, transformation.getAlpha());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toShortString",
            args = {}
        )
    })
    public void testToString() {
        assertNotNull(new Transformation().toString());
        assertNotNull(new Transformation().toShortString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMatrix",
        args = {}
    )
    public void testGetMatrix() {
        final Matrix expected = new Matrix();
        final Transformation transformation = new Transformation();
        assertEquals(expected, transformation.getMatrix());
    }
}
