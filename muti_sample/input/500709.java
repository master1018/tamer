@TestTargetClass(CornerPathEffect.class)
public class CornerPathEffectTest extends TestCase {
    private static final int BITMAP_WIDTH = 100;
    private static final int BITMAP_HEIGHT = 100;
    private static final int PADDING = 10;
    private static final int RADIUS = 20;
    private static final int TOLERANCE = 5;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "CornerPathEffect",
        args = {float.class}
    )
    @ToBeFixed(bug = "2037365", explanation = "CornerPathEffect ends the path prematurely it " +
            "is not closed.")
    public void testCornerPathEffect() {
        Path path = new Path();
        path.moveTo(0, PADDING);
        path.lineTo(BITMAP_WIDTH - PADDING, PADDING);
        path.lineTo(BITMAP_WIDTH - PADDING, BITMAP_HEIGHT);
        PathEffect effect = new CornerPathEffect(RADIUS);
        Paint pathPaint = new Paint();
        pathPaint.setColor(Color.GREEN);
        pathPaint.setStyle(Style.STROKE);
        pathPaint.setStrokeWidth(0);
        pathPaint.setPathEffect(effect);
        Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPath(path, pathPaint);
        Path expectedPath = new Path();
        RectF oval = new RectF(BITMAP_WIDTH - PADDING - 2 * RADIUS, PADDING,
                BITMAP_WIDTH - PADDING, PADDING + 2 * RADIUS);
        expectedPath.moveTo(0, PADDING);
        expectedPath.arcTo(oval, 270, 90);
        expectedPath.lineTo(BITMAP_WIDTH - PADDING, BITMAP_HEIGHT);
        Paint expectedPaint = new Paint();
        expectedPaint.setColor(Color.RED);
        expectedPaint.setStyle(Style.STROKE);
        expectedPaint.setStrokeWidth(TOLERANCE);
        expectedPaint.setXfermode(new PorterDuffXfermode(Mode.SCREEN));
        canvas.drawPath(expectedPath, expectedPaint);
        int numPixels = 0;
        for (int y = 0; y < BITMAP_HEIGHT; y++) {
            for (int x = 0; x < BITMAP_WIDTH; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (Color.green(pixel) > 0) {
                    numPixels += 1;
                    assertEquals(Color.YELLOW, pixel);
                }
            }
        }
        int straightLines = BITMAP_WIDTH - PADDING - RADIUS + BITMAP_HEIGHT - PADDING - RADIUS;
        int cornerPixels = numPixels - straightLines;
        assertTrue(cornerPixels < 2 * RADIUS);
        assertFalse(cornerPixels > RADIUS);
    }
}
