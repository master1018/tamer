@TestTargetClass(PathMeasure.class)
public class PathMeasureTest extends AndroidTestCase {
    private PathMeasure mPathMeasure;
    private Path mPath;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPath = new Path();
        mPathMeasure = new PathMeasure();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PathMeasure",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PathMeasure",
            args = {android.graphics.Path.class, boolean.class}
        )
    })
    public void testConstructor() {
        mPathMeasure = new PathMeasure();
        Path path = new Path();
        mPathMeasure = new PathMeasure(path, true);
        mPathMeasure = new PathMeasure(path, false);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPosTan",
        args = {float.class, float[].class, float[].class}
    )
    public void testGetPosTan() {
        float distance = 1f;
        float[] pos = { 1f };
        float[] tan = { 1f };
        try {
            mPathMeasure.getPosTan(distance, pos, tan);
            fail("should throw exception");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        float[] pos2 = { 1f, 2f };
        float[] tan2 = { 1f, 3f };
        assertFalse(mPathMeasure.getPosTan(distance, pos2, tan2));
        mPathMeasure.setPath(mPath, true);
        mPath.addRect(1f, 2f, 3f, 4f, Path.Direction.CW);
        mPathMeasure.setPath(mPath, true);
        float[] pos3 = { 1f, 2f, 3f, 4f };
        float[] tan3 = { 1f, 2f, 3f, 4f };
        assertTrue(mPathMeasure.getPosTan(0f, pos3, tan3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "nextContour",
        args = {}
    )
    public void testNextContour() {
        assertFalse(mPathMeasure.nextContour());
        mPath.addRect(1, 2, 3, 4, Path.Direction.CW);
        mPathMeasure.setPath(mPath, true);
        assertTrue(mPathMeasure.nextContour());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLength",
        args = {}
    )
    public void testGetLength() {
        assertEquals(0f, mPathMeasure.getLength());
        mPath.addRect(1, 2, 3, 4, Path.Direction.CW);
        mPathMeasure.setPath(mPath, true);
        assertEquals(8.0f, mPathMeasure.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isClosed",
        args = {}
    )
    @BrokenTest("Flaky test. new PathMeasure().isClosed() does not return consistent result")
    public void testIsClosed() {
        assertTrue(mPathMeasure.isClosed());
        mPathMeasure = null;
        mPathMeasure = new PathMeasure();
        mPathMeasure.setPath(mPath, false);
        assertFalse(mPathMeasure.isClosed());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setPath",
        args = {android.graphics.Path.class, boolean.class}
    )
    public void testSetPath() {
        mPathMeasure.setPath(mPath, true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSegment",
        args = {float.class, float.class, android.graphics.Path.class, boolean.class}
    )
    public void testGetSegment() {
        assertEquals(0f, mPathMeasure.getLength());
        mPath.addRect(1, 2, 3, 4, Path.Direction.CW);
        mPathMeasure.setPath(mPath, true);
        assertEquals(8f, mPathMeasure.getLength());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMatrix",
        args = {float.class, android.graphics.Matrix.class, int.class}
    )
    public void testGetMatrix() {
        Matrix matrix = new Matrix();
        assertFalse(mPathMeasure.getMatrix(1f, matrix, PathMeasure.POSITION_MATRIX_FLAG));
        matrix.setScale(1f, 2f);
        mPath.addRect(1f, 2f, 3f, 4f, Path.Direction.CW);
        mPathMeasure.setPath(mPath, true);
        assertTrue(mPathMeasure.getMatrix(0f, matrix, PathMeasure.TANGENT_MATRIX_FLAG));
    }
}
