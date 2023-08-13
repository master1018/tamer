@TestTargetClass(android.graphics.drawable.shapes.RectShape.class)
public class RectShapeTest extends TestCase {
    private static final int TEST_WIDTH  = 100;
    private static final int TEST_HEIGHT = 200;
    private static final int TEST_COLOR_1 = 0xFF00FF00;
    private static final int TEST_COLOR_2 = 0xFFFF0000;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RectShape",
        args = {}
    )
    public void testConstructor() {
        new RectShape();
    }
    private void assertDrawSuccessfully(Bitmap bitmap, int width, int height, int color) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                assertEquals(color, bitmap.getPixel(i, j));
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class}
    )
    public void testDraw() {
        RectShape rectShape = new RectShape();
        Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(TEST_COLOR_1);
        rectShape.resize(TEST_WIDTH, TEST_HEIGHT);
        rectShape.draw(canvas, paint);
        assertDrawSuccessfully(bitmap, TEST_WIDTH, TEST_HEIGHT, TEST_COLOR_1);
        paint.setColor(TEST_COLOR_2);
        rectShape.draw(canvas, paint);
        assertDrawSuccessfully(bitmap, TEST_WIDTH, TEST_HEIGHT, TEST_COLOR_2);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "clone",
        args = {}
    )
    public void testClone() throws CloneNotSupportedException {
        RectShape rectShape = new RectShape();
        rectShape.resize(100f, 200f);
        RectShape clonedShape = rectShape.clone();
        assertEquals(100f, rectShape.getWidth());
        assertEquals(200f, rectShape.getHeight());
        assertNotSame(rectShape, clonedShape);
        assertEquals(rectShape.getWidth(), clonedShape.getWidth());
        assertEquals(rectShape.getHeight(), clonedShape.getHeight());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "rect",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onResize",
            args = {float.class, float.class}
        )
    })
    public void testRect() {
        MyRectShape rectShape = new MyRectShape();
        RectF rect = rectShape.myRect();
        assertEquals(0.0f, rect.left);
        assertEquals(0.0f, rect.top);
        assertEquals(0.0f, rect.right);
        assertEquals(0.0f, rect.bottom);
        rectShape.resize(TEST_WIDTH, TEST_HEIGHT);
        rect = rectShape.myRect();
        assertEquals(0.0f, rect.left);
        assertEquals(0.0f, rect.top);
        assertEquals((float) TEST_WIDTH, rect.right);
        assertEquals((float) TEST_HEIGHT, rect.bottom);
    }
    private static class MyRectShape extends RectShape {
        public RectF myRect() {
            return super.rect();
        }
    }
}
