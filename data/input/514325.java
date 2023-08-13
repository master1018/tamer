@TestTargetClass(PaintFlagsDrawFilter.class)
public class PaintFlagsDrawFilterTest extends AndroidTestCase {
    private static final float TEXT_SIZE = 20;
    private static final float TEXT_X = 50;
    private static final float TEXT_Y = 50;
    private static final String TEXT = "Test";
    private static final int BITMAP_WIDTH = 100;
    private static final int BITMAP_HEIGHT = 100;
    private float mTextWidth;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PaintFlagsDrawFilter",
        args = {int.class, int.class}
    )
    public void testPaintFlagsDrawFilter() {
        Bitmap bitmapWithoutFilter = drawText(null);
        PaintFlagsDrawFilter filter = new PaintFlagsDrawFilter(Paint.UNDERLINE_TEXT_FLAG, 0);
        Bitmap bitmapWithFilter = drawText(filter);
        Bitmap combined = delta(bitmapWithoutFilter, bitmapWithFilter);
        assertUnderline(combined);
    }
    private Bitmap drawText(PaintFlagsDrawFilter filter) {
        Paint p = new Paint(Paint.UNDERLINE_TEXT_FLAG);
        p.setColor(Color.RED);
        p.setTextSize(TEXT_SIZE);
        p.setTextAlign(Align.CENTER);
        mTextWidth = p.measureText(TEXT);
        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.setDrawFilter(filter);
        c.drawColor(Color.BLACK);
        c.drawText(TEXT, TEXT_X, TEXT_Y, p);
        return b;
    }
    private Bitmap delta(Bitmap bitmapWithoutFilter, Bitmap bitmapWithFilter) {
        Bitmap combinedBitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        combinedBitmap.eraseColor(Color.BLACK);
        int pixelWithoutFilter;
        int pixelWithFilter;
        for (int i = 0; i < BITMAP_WIDTH; i++) {
            for (int j = 0; j < BITMAP_HEIGHT; j++) {
                pixelWithoutFilter = bitmapWithoutFilter.getPixel(i, j);
                pixelWithFilter = bitmapWithFilter.getPixel(i, j);
                if (pixelWithoutFilter != pixelWithFilter) {
                    assertEquals(Color.RED, pixelWithoutFilter);
                    assertEquals(Color.BLACK, pixelWithFilter);
                    combinedBitmap.setPixel(i, j, Color.RED);
                }
            }
        }
        return combinedBitmap;
    }
    private void assertUnderline(Bitmap bitmap) {
        Rect rect = new Rect(BITMAP_WIDTH, BITMAP_HEIGHT, 0, 0);
        for (int y = 0; y < BITMAP_HEIGHT; y++) {
            for (int x = 0; x < BITMAP_WIDTH; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel == Color.RED) {
                    rect.left = Math.min(rect.left, x);
                    rect.right = Math.max(rect.right, x);
                    rect.top = Math.min(rect.top, y);
                    rect.bottom = Math.max(rect.bottom, y);
                }
            }
        }
        assertTrue(rect.top <= rect.bottom);
        assertEquals(mTextWidth, rect.right - rect.left, mTextWidth * 0.05);
        assertTrue(rect.top >= TEXT_Y);
    }
}
