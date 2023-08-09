@TestTargetClass(AvoidXfermode.class)
public class AvoidXfermodeTest extends TestCase {
    private static final int TOLERANCE = 255;
    private static final int BASE_SIZE = 50;
    private static final int BITMAP_HEIGHT = BASE_SIZE * 2;
    private static final int BITMAP_WIDTH = BASE_SIZE * 2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "AvoidXfermode",
        args = {int.class, int.class, android.graphics.AvoidXfermode.Mode.class}
    )
    @ToBeFixed(bug = "2034547",
               explanation = "AvoidXfermode does not work as expected with tolerance 0.")
    public void testAvoidXfermode() {
        Paint greenPaint;
        Paint redAvoidingGreenPaint;
        Paint blueTargetingGreenPaint;
        greenPaint = new Paint();
        greenPaint.setColor(Color.GREEN);
        AvoidXfermode avoidMode =
            new AvoidXfermode(greenPaint.getColor(), TOLERANCE, AvoidXfermode.Mode.AVOID);
        redAvoidingGreenPaint = new Paint();
        redAvoidingGreenPaint.setColor(Color.RED);
        redAvoidingGreenPaint.setXfermode(avoidMode);
        AvoidXfermode targetMode =
            new AvoidXfermode(greenPaint.getColor(), TOLERANCE, AvoidXfermode.Mode.TARGET);
        blueTargetingGreenPaint = new Paint();
        blueTargetingGreenPaint.setColor(Color.BLUE);
        blueTargetingGreenPaint.setXfermode(targetMode);
        Bitmap b = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        b.eraseColor(Color.BLACK);
        Canvas canvas = new Canvas(b);
        canvas.drawRect(0f, 0f, BASE_SIZE, 2 * BASE_SIZE, greenPaint);
        canvas.drawRect(0f, 0f, 2 * BASE_SIZE, BASE_SIZE, redAvoidingGreenPaint);
        canvas.drawRect(0f, BASE_SIZE, 2 * BASE_SIZE, 2 * BASE_SIZE, blueTargetingGreenPaint);
        assertEquals(Color.GREEN, b.getPixel(BASE_SIZE / 2, BASE_SIZE / 2));
        assertEquals(Color.RED, b.getPixel(BASE_SIZE + BASE_SIZE / 2, BASE_SIZE / 2));
        assertEquals(Color.BLUE, b.getPixel(BASE_SIZE / 2, BASE_SIZE + BASE_SIZE / 2));
        assertEquals(Color.BLACK, b.getPixel(BASE_SIZE + BASE_SIZE / 2, BASE_SIZE + BASE_SIZE / 2));
    }
}
