@TestTargetClass(android.graphics.drawable.shapes.OvalShape.class)
public class OvalShapeTest extends TestCase {
    private static final int TEST_WIDTH  = 100;
    private static final int TEST_HEIGHT = 200;
    private static final int TEST_COLOR_1 = 0xFF00FF00;
    private static final int TEST_COLOR_2 = 0xFFFF0000;
    private static final int TOLERANCE = 4; 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "OvalShape",
        args = {}
    )
    public void testConstructor() {
        new OvalShape();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class}
    )
    public void testDraw() {
        OvalShape ovalShape = new OvalShape();
        Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(TEST_COLOR_1);
        ovalShape.resize(TEST_WIDTH, TEST_HEIGHT);
        ovalShape.draw(canvas, paint);
        assertEquals(TEST_COLOR_1, bitmap.getPixel(TEST_WIDTH / 2, TEST_HEIGHT / 2));
        final int SQUARE = Math.min(TEST_WIDTH, TEST_HEIGHT);
        paint.setColor(TEST_COLOR_2);
        ovalShape.resize(SQUARE, SQUARE); 
        ovalShape.draw(canvas, paint);
        int count = 0;
        for (int i = 0; i < SQUARE; i++) {
            if (bitmap.getPixel(i, i) == TEST_COLOR_2) {
                count += 1;
            }
        }
        assertEquals((double)SQUARE / Math.sqrt(2), count, TOLERANCE);
    }
}
