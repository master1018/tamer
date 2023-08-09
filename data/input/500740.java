@TestTargetClass(android.graphics.drawable.shapes.PathShape.class)
public class PathShapeTest extends TestCase {
    private static final int TEST_COLOR_1 = 0xFF00FF00;
    private static final int TEST_COLOR_2 = 0xFFFF0000;
    private static final int TOLERANCE = 4;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PathShape",
        args = {android.graphics.Path.class, float.class, float.class}
    )
    public void testConstructor() {
        new PathShape(new Path(), 1f, 5f);
        new PathShape(new Path(), -1f, -1f);
        new PathShape(null, 0f, 0f);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class, android.graphics.Paint.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "onResize",
            args = {float.class, float.class}
        )
    })
    public void testDraw() {
        final int SHAPE_SIZE = 200;
        Path path = new Path();
        path.moveTo(50, 0);
        path.lineTo(0, 50);
        path.lineTo(50, 100);
        path.lineTo(100, 50);
        path.close();
        PathShape pathShape = new PathShape(path, SHAPE_SIZE, SHAPE_SIZE);
        Bitmap bitmap = Bitmap.createBitmap(SHAPE_SIZE, SHAPE_SIZE, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(TEST_COLOR_1);
        pathShape.resize(SHAPE_SIZE, SHAPE_SIZE);
        pathShape.draw(canvas, paint);
        assertEquals(TEST_COLOR_1, bitmap.getPixel(50, 50));
        paint.setColor(TEST_COLOR_2);
        pathShape.resize(SHAPE_SIZE / 2, SHAPE_SIZE / 2);
        pathShape.draw(canvas, paint);
        int horizontal = 0;
        int vertical = 0;
        int diagonal = 0;
        for (int i = 0; i < 50; i++) {
            if (bitmap.getPixel(25, i) == TEST_COLOR_2) {
                vertical += 1;
            }
            if (bitmap.getPixel(i, 25) == TEST_COLOR_2) {
                horizontal += 1;
            }
            if (bitmap.getPixel(i, i) == TEST_COLOR_2) {
                diagonal += 1;
            }
        }
        assertEquals(50, horizontal, TOLERANCE);
        assertEquals(50, vertical, TOLERANCE);
        assertEquals(25, diagonal, TOLERANCE);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "clone",
        args = {}
    )
    public void testClone() throws CloneNotSupportedException {
        PathShape pathShape = new PathShape(new Path(), 1f, 5f);
        pathShape.resize(100f, 200f);
        PathShape clonedShape = pathShape.clone();
        assertEquals(100f, pathShape.getWidth());
        assertEquals(200f, pathShape.getHeight());
        assertNotSame(pathShape, clonedShape);
        assertEquals(pathShape.getWidth(), clonedShape.getWidth());
        assertEquals(pathShape.getHeight(), clonedShape.getHeight());
    }
}
