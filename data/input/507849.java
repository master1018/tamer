@TestTargetClass(EmbossMaskFilter.class)
public class EmbossMaskFilterTest extends TestCase {
    private static final int BITMAP_WIDTH = 100;
    private static final int BITMAP_HEIGHT = 100;
    private static final int START_X = 10;
    private static final int END_X = BITMAP_WIDTH - START_X;
    private static final int CENTER_X = (START_X + END_X) / 2;
    private static final int CENTER_Y = BITMAP_HEIGHT / 2;
    private static final int STROKE_WIDTH = 10;
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Javadoc is incomplete, cannot test all parameters",
        method = "EmbossMaskFilter",
        args = {float[].class, float.class, float.class, float.class}
    )
    public void testEmbossMaskFilter() {
        EmbossMaskFilter filter = new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.5f, 8, 3);
        Paint paint = new Paint();
        paint.setMaskFilter(filter);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setColor(Color.GRAY);
        Path path = new Path();
        path.moveTo(START_X, CENTER_Y);
        path.lineTo(END_X, CENTER_Y);
        Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        bitmap.eraseColor(Color.BLACK);
        Canvas c = new Canvas(bitmap);
        c.drawPath(path, paint);
        Rect top = new Rect(0, 0, BITMAP_WIDTH, CENTER_Y);
        Rect bottom = new Rect(0, CENTER_Y, BITMAP_WIDTH, BITMAP_HEIGHT);
        Rect left = new Rect(0, 0, CENTER_X, BITMAP_HEIGHT);
        Rect right = new Rect(CENTER_X, 0, BITMAP_WIDTH, BITMAP_HEIGHT);
        assertTrue(brightness(bitmap, top) > brightness(bitmap, bottom));
        assertTrue(brightness(bitmap, left) > brightness(bitmap, right));
        top.bottom = CENTER_Y - STROKE_WIDTH / 2;
        assertEquals(0, brightness(bitmap, top));
        bottom.top = CENTER_Y + STROKE_WIDTH / 2;
        assertEquals(0, brightness(bitmap, bottom));
        left.right = START_X;
        assertEquals(0, brightness(bitmap, left));
        right.left = END_X;
        assertEquals(0, brightness(bitmap, right));
    }
    private long brightness(Bitmap b, Rect rect) {
        long color = 0;
        for (int y = rect.top; y < rect.bottom; y++) {
            for (int x = rect.left; x < rect.right; x++) {
                int pixel = b.getPixel(x, y);
                color += Color.red(pixel) + Color.green(pixel) + Color.blue(pixel);
            }
        }
        return color;
    }
}
