@TestTargetClass(android.graphics.drawable.shapes.ArcShape.class)
public class ArcShapeTest extends TestCase {
    private static final int TEST_WIDTH  = 100;
    private static final int TEST_HEIGHT = 200;
    private static final int TEST_COLOR_1 = 0xFF00FF00;
    private static final int TEST_COLOR_2 = 0xFFFF0000;
    private static final int TOLERANCE = 4; 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ArcShape",
        args = {float.class, float.class}
    )
    public void testConstructor() {
        new ArcShape(1f, 5f);
        new ArcShape(0f, 0f);
        new ArcShape(-1f, -1f);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class}
    )
    public void testDraw() {
        ArcShape arcShape = new ArcShape(0.0f, 360.0f);
        Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(TEST_COLOR_1);
        arcShape.resize(TEST_WIDTH, TEST_HEIGHT);
        arcShape.draw(canvas, paint);
        assertEquals(TEST_COLOR_1, bitmap.getPixel(TEST_WIDTH / 2, TEST_HEIGHT / 2));
        final int SQUARE = Math.min(TEST_WIDTH, TEST_HEIGHT);
        paint.setColor(TEST_COLOR_2);
        arcShape = new ArcShape(0.0f, 180.0f);
        arcShape.resize(SQUARE, SQUARE); 
        arcShape.draw(canvas, paint);
        int count = 0;
        for (int i = 0; i < SQUARE; i++) {
            if (bitmap.getPixel(i, i) == TEST_COLOR_2) {
                count += 1;
            }
        }
        assertEquals((double)SQUARE / 2 / Math.sqrt(2), count, TOLERANCE);
    }
}
