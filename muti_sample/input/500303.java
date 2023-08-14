@TestTargetClass(DiscretePathEffect.class)
public class DiscretePathEffectTest extends TestCase {
    private static final int BITMAP_WIDTH = 200;
    private static final int BITMAP_HEIGHT = 100;
    private static final int START_X = 10;
    private static final int END_X = BITMAP_WIDTH - START_X;
    private static final int COORD_Y = BITMAP_HEIGHT / 2;
    private static final int SEGMENT_LENGTH = 10; 
    private static final int DEVIATION = 10; 
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "DiscretePathEffect",
        args = {float.class, float.class}
    )
    public void testDiscretePathEffect() {
        DiscretePathEffect effect = new DiscretePathEffect(SEGMENT_LENGTH, DEVIATION);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(0);
        paint.setPathEffect(effect);
        Path path = new Path();
        path.moveTo(START_X, COORD_Y);
        path.lineTo(END_X, COORD_Y);
        Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPath(path, paint);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(0);
        paint.setXfermode(new PorterDuffXfermode(Mode.SCREEN));
        canvas.drawPath(path, paint);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1 + 2 * DEVIATION);
        canvas.drawPath(path, paint);
        int intersect = 0;
        int numGreenPixels = 0;
        int minY = BITMAP_HEIGHT;
        int maxY = 0;
        for (int y = 0; y < BITMAP_HEIGHT; y++) {
            for (int x = 0; x < BITMAP_WIDTH; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (Color.green(pixel) > 0) {
                    numGreenPixels += 1;
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    assertEquals(0xFF, Color.blue(pixel));
                    if (Color.red(pixel) > 0) {
                        intersect += 1;
                    }
                }
            }
        }
        int lineLength = END_X - START_X;
        assertTrue(numGreenPixels >= lineLength);
        assertTrue(maxY - minY > 0);
        assertTrue(maxY - minY <= 1 + 2 * DEVIATION);
        assertTrue(intersect < lineLength);
        assertTrue(intersect >= lineLength / SEGMENT_LENGTH);
    }
}
