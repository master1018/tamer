@TestTargetClass(Path.class)
public class PathTest extends TestCase {
    private static final float LEFT = 10.0f;
    private static final float RIGHT = 50.0f;
    private static final float TOP = 10.0f;
    private static final float BOTTOM = 50.0f;
    private static final float XCOORD = 40.0f;
    private static final float YCOORD = 40.0f;
	@TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Path",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Path",
            args = {android.graphics.Path.class}
        )
    })
    public void testConstructor() {
        new Path();
        new Path(new Path());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRect",
            args = {android.graphics.RectF.class, android.graphics.Path.Direction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddRect1() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF rect = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.addRect(rect, Path.Direction.CW);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRect",
            args = {float.class, float.class, float.class, float.class,
                    android.graphics.Path.Direction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddRect2() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.addRect(LEFT, TOP, RIGHT, BOTTOM, Path.Direction.CW);
        assertFalse(path.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "moveTo",
        args = {float.class, float.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "Test moveTo(float x, float y)." +
            "When this method called, the current point will move to the" +
            "appointed coordinate, but there is no more way to get known" +
            "about whether current point is just in that coordinate correctly")
    public void testMoveTo() {
        Path path = new Path();
        path.moveTo(10.0f, 10.0f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "set",
            args = {android.graphics.Path.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testSet() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        Path path1 = new Path();
        setPath(path1);
        path.set(path1);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFillType",
            args = {android.graphics.Path.FillType.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFillType",
            args = {}
        )
    })
    public void testAccessFillType() {
        Path.FillType expected1 = Path.FillType.EVEN_ODD;
        Path.FillType expected2 = Path.FillType.INVERSE_EVEN_ODD;
        Path.FillType expected3 = Path.FillType.INVERSE_WINDING;
        Path.FillType expected4 = Path.FillType.WINDING;
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        assertEquals(expected1, path.getFillType());
        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        assertEquals(expected2, path.getFillType());
        path.setFillType(Path.FillType.INVERSE_WINDING);
        assertEquals(expected3, path.getFillType());
        path.setFillType(Path.FillType.WINDING);
        assertEquals(expected4, path.getFillType());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rQuadTo",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testRQuadTo() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.rQuadTo(5.0f, 5.0f, 10.0f, 10.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "transform",
            args = {android.graphics.Matrix.class, android.graphics.Path.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testTransform1() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        Path dst = new Path();
        setPath(path);
        path.transform(new Matrix(), dst);
        assertFalse(dst.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "transform",
        args = {android.graphics.Matrix.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "The function of this method is" +
              "almost the same as transform(Matrix matrix, Path dst) but no dst" +
              "to show if it works when called, and we can't get any information" +
              "in this method when called" )
    public void testTransform2() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "lineTo",
            args = {float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testLineTo() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.lineTo(XCOORD, YCOORD);
        assertFalse(path.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "close",
        args = {}
    )
    @ToBeFixed(bug = "1451096", explanation = "What does 'close' mean, clear the " +
            "contour or others? If clear, why the path is not empty when" +
            "there is just one path.")
    public void testClose() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        setPath(path);
        path.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "quadTo",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testQuadTo() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.quadTo(20.0f, 20.0f, 40.0f, 40.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addCircle",
            args = {float.class, float.class, float.class, android.graphics.Path.Direction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddCircle() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.addCircle(XCOORD, YCOORD, 10.0f, Path.Direction.CW);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "arcTo",
            args = {android.graphics.RectF.class, float.class, float.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testArcTo1() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF oval = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.arcTo(oval, 0.0f, 30.0f, true);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "arcTo",
            args = {android.graphics.RectF.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testArcTo2() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF oval = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.arcTo(oval, 0.0f, 30.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "computeBounds",
        args = {android.graphics.RectF.class, boolean.class}
    )
    public void testComputeBounds1() {
        RectF expected = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        assertEquals(expected.width(), bounds.width());
        assertEquals(expected.height(), bounds.height());
        path.computeBounds(bounds, false);
        assertEquals(expected.width(), bounds.width());
        assertEquals(expected.height(), bounds.height());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeBounds",
            args = {android.graphics.RectF.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRect",
            args = {android.graphics.RectF.class, android.graphics.Path.Direction.class}
        )
    })
    public void testComputeBounds2() {
        RectF expected = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF bounds = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.addRect(bounds, Path.Direction.CW);
        path.computeBounds(bounds, true);
        assertEquals(expected.width(), bounds.width());
        assertEquals(expected.height(), bounds.height());
        path.computeBounds(bounds, false);
        assertEquals(expected.width(), bounds.width());
        assertEquals(expected.height(), bounds.height());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "rMoveTo",
        args = {float.class, float.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "When this method called, the current" +
            "point will move to the appointed coordinate, but there is no more" +
            "way to get known about whether current point is just in that" +
            "coordinate correctly")
    public void testRMoveTo() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setLastPoint",
        args = {float.class, float.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "When called, we can't" +
              "get any useful information to make sure the point has been" +
              "correctly set")
    public void testSetLastPoint() {
        Path path = new Path();
        path.setLastPoint(10.0f, 10.0f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rLineTo",
            args = {float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testRLineTo() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.rLineTo(10.0f, 10.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isEmpty",
        args = {}
    )
    public void testIsEmpty() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        setPath(path);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rewind",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testRewind() {
        Path.FillType expected = Path.FillType.EVEN_ODD;
        Path path = new Path();
        assertTrue(path.isEmpty());
        setPath(path);
        path.rewind();
        path.setFillType(Path.FillType.EVEN_ODD);
        assertTrue(path.isEmpty());
        assertEquals(expected, path.getFillType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addOval",
        args = {android.graphics.RectF.class, android.graphics.Path.Direction.class}
    )
    public void testAddOval() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF oval = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.addOval(oval, Path.Direction.CW);
        assertFalse(path.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "isRect",
        args = {android.graphics.RectF.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "what does 'specify' in the note of " +
              "this method mean? The return always is false, is it correct?")
    public void testIsRect() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        setPath(path);
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "incReserve",
        args = {int.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "Maybe this method has little" +
              "obvious behavior to test")
    public void testIncReserve() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addPath",
            args = {android.graphics.Path.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddPath1() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        Path src = new Path();
        setPath(src);
        path.addPath(src, 10.0f, 10.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addPath",
            args = {android.graphics.Path.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddPath2() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        Path src = new Path();
        setPath(src);
        path.addPath(src);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addPath",
            args = {android.graphics.Path.class, android.graphics.Matrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddPath3() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        Path src = new Path();
        setPath(src);
        Matrix matrix = new Matrix();
        path.addPath(src, matrix);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRoundRect",
            args = {android.graphics.RectF.class, float.class, float.class,
                    android.graphics.Path.Direction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddRoundRect1() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF rect = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.addRoundRect(rect, XCOORD, YCOORD, Path.Direction.CW);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRoundRect",
            args = {android.graphics.RectF.class, float[].class,
                    android.graphics.Path.Direction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddRoundRect2() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF rect = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        float[] radii = new float[8];
        for (int i = 0; i < 8; i++) {
            radii[i] = 10.0f + i * 5.0f;
        }
        path.addRoundRect(rect, radii, Path.Direction.CW);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isInverseFillType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFillType",
            args = {android.graphics.Path.FillType.class}
        )
    })
    public void testIsInverseFillType() {
        Path path = new Path();
        assertFalse(path.isInverseFillType());
        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        assertTrue(path.isInverseFillType());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offset",
        args = {float.class, float.class, android.graphics.Path.class}
    )
    public void testOffset1() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        setPath(path);
        Path dst = new Path();
        path.offset(XCOORD, YCOORD, dst);
        assertFalse(dst.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offset",
        args = {float.class, float.class}
    )
    @ToBeFixed(bug = "1451096", explanation = "The function of this method is" +
              "almost the same as offset(float dx, float dy, Path dst) but no dst" +
              "to show if it works when called, and we can't get any information" +
              "in this method when called")
    public void testOffset2() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cubicTo",
            args = {float.class, float.class, float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testCubicTo() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.cubicTo(10.0f, 10.0f, 20.0f, 20.0f, 30.0f, 30.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reset",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addRect",
            args = {android.graphics.RectF.class, android.graphics.Path.Direction.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "set",
            args = {android.graphics.Path.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testReset() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        Path path1 = new Path();
        setPath(path1);
        path.set(path1);
        assertFalse(path.isEmpty());
        path.reset();
        assertTrue(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "toggleInverseFillType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isInverseFillType",
            args = {}
        )
    })
    public void testToggleInverseFillType() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.toggleInverseFillType();
        assertTrue(path.isInverseFillType());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addArc",
            args = {android.graphics.RectF.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testAddArc() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        RectF oval = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.addArc(oval, 0.0f, 30.0f);
        assertFalse(path.isEmpty());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rCubicTo",
            args = {float.class, float.class, float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEmpty",
            args = {}
        )
    })
    public void testRCubicTo() {
        Path path = new Path();
        assertTrue(path.isEmpty());
        path.rCubicTo(10.0f, 10.0f, 11.0f, 11.0f, 12.0f, 12.0f);
        assertFalse(path.isEmpty());
    }
    private void setPath(Path path) {
        RectF rect = new RectF(LEFT, TOP, RIGHT, BOTTOM);
        path.addRect(rect, Path.Direction.CW);
    }
}
