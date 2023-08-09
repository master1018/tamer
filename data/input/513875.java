@TestTargetClass(BlurMaskFilter.class)
public class BlurMaskFilterTest extends TestCase {
    private static final int OFFSET = 10;
    private static final int RADIUS = 5;
    private static final int BITMAP_WIDTH = 100;
    private static final int BITMAP_HEIGHT = 100;
    private static final int CENTER = BITMAP_HEIGHT / 2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "BlurMaskFilter",
        args = {float.class, android.graphics.BlurMaskFilter.Blur.class}
    )
    public void testBlurMaskFilter(){
        BlurMaskFilter filter = new BlurMaskFilter(RADIUS, Blur.NORMAL);
        Paint paint = new Paint();
        paint.setMaskFilter(filter);
        paint.setColor(Color.RED);
        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        b.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(b);
        canvas.drawRect(CENTER - OFFSET, CENTER - OFFSET, CENTER + OFFSET, CENTER + OFFSET, paint);
        for (int x = 0; x < CENTER; x++) {
            for (int y = 0; y < CENTER; y++) {
                if (x < CENTER - OFFSET - RADIUS || y < CENTER - OFFSET - RADIUS) {
                    checkQuadrants(Color.TRANSPARENT, b, x, y, 5);
                } else if (x > CENTER - OFFSET + RADIUS && y > CENTER - OFFSET + RADIUS) {
                    checkQuadrants(Color.RED, b, x, y, 5);
                } else {
                    checkQuadrants(Color.RED, b, x, y, 255);
                }
            }
        }
    }
    private void checkQuadrants(int color, Bitmap bitmap, int x, int y, int alphaTolerance) {
        int right = bitmap.getWidth() - 1;
        int bottom = bitmap.getHeight() - 1;
        try {
            checkColor(color, bitmap.getPixel(x, y), alphaTolerance);
            checkColor(color, bitmap.getPixel(right - x, y), alphaTolerance);
            checkColor(color, bitmap.getPixel(x, bottom - y), alphaTolerance);
            checkColor(color, bitmap.getPixel(right - x, bottom - y), alphaTolerance);
        } catch (Error e) {
            Log.w(getClass().getName(), "Failed for coordinates (" + x + ", " + y + ")");
            throw e;
        }
    }
    private void checkColor(int expected, int actual, int alphaTolerance) {
        assertEquals(Color.red(expected), Color.red(actual));
        assertEquals(Color.green(expected), Color.green(actual));
        assertEquals(Color.blue(expected), Color.blue(actual));
        assertEquals(Color.alpha(expected), Color.alpha(actual), alphaTolerance);
    }
}
