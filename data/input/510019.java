@TestTargetClass(GestureStroke.class)
public class GestureStrokeTest extends TestCase {
    private LineGestureStrokeHelper mHelper;
    @Override
    protected void setUp() throws Exception {
        mHelper = new LineGestureStrokeHelper();
    }
    public void testGetPath_empty() {
        GestureStroke emptyStroke = mHelper.createGestureStroke();
        assertNull(emptyStroke.getPath());
    }
    public void testGetPath_singlePoint() {
        GestureStroke emptyStroke = mHelper.createGestureStroke(new GesturePoint(0, 0, 0));
        Path emptyPath = emptyStroke.getPath();
        assertTrue(emptyPath.isEmpty());
    }
    public void testGetPath_line() {
        GestureStroke lineStroke = mHelper.createLineGesture();
        Path linePath = lineStroke.getPath();
        mHelper.assertLineBoundingBox(linePath);
    }
    public void testToPath_line() {
        GestureStroke lineStroke = mHelper.createLineGesture();
        Path linePath = lineStroke.toPath(LineGestureStrokeHelper.LINE_END_POINT,
                LineGestureStrokeHelper.LINE_END_POINT, 2);
        mHelper.assertLineBoundingBox(linePath);
    }
    public void testToPath_boundedLine() {
        GestureStroke lineStroke = mHelper.createLineGesture();
        Path linePath = lineStroke.toPath(LineGestureStrokeHelper.LINE_MIDWAY_POINT,
                LineGestureStrokeHelper.LINE_MIDWAY_POINT, 2);
        RectF bounds = new RectF();
        linePath.computeBounds(bounds, true);
        assertEquals(LineGestureStrokeHelper.LINE_QUARTER_POINT, bounds.bottom);
        assertEquals(LineGestureStrokeHelper.LINE_START_POINT, bounds.left);
        assertEquals(LineGestureStrokeHelper.LINE_QUARTER_POINT, bounds.right);
        assertEquals(LineGestureStrokeHelper.LINE_START_POINT, bounds.top);
    }
    public void testComputeOrientedBoundingBox() {
        GestureStroke line = mHelper.createLineGesture();
        OrientedBoundingBox box = line.computeOrientedBoundingBox();
        assertEquals(LineGestureStrokeHelper.LINE_MIDWAY_POINT, box.centerX);
        assertEquals(LineGestureStrokeHelper.LINE_MIDWAY_POINT, box.centerY);
        assertEquals(LineGestureStrokeHelper.LINE_ANGLE, box.orientation);
    }
    public void testBoundingBox_line() {
        GestureStroke line = mHelper.createLineGesture();
        mHelper.assertLineBoundingBox(line.boundingBox);
    }
}
