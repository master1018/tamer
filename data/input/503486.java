@TestTargetClass(PorterDuffXfermode.class)
public class PorterDuffXfermodeTest extends TestCase {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 100;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PorterDuffXfermode",
        args = {android.graphics.PorterDuff.Mode.class}
    )
    public void testPorterDuffXfermode() {
        Bitmap target = Bitmap.createBitmap(WIDTH, HEIGHT, Config.ARGB_8888);
        target.eraseColor(Color.TRANSPARENT);
        Bitmap b1 = Bitmap.createBitmap(WIDTH / 2, HEIGHT, Config.ARGB_8888);
        b1.eraseColor(Color.RED);
        Bitmap b2 = Bitmap.createBitmap(WIDTH, HEIGHT / 2, Config.ARGB_8888);
        b2.eraseColor(Color.BLUE);
        Canvas canvas = new Canvas(target);
        Paint p = new Paint();
        canvas.drawBitmap(b1, 0, 0, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        canvas.drawBitmap(b2, 0, HEIGHT / 2, p);
        assertEquals(Color.RED, target.getPixel(WIDTH / 4, HEIGHT / 4));
        assertEquals(Color.BLUE, target.getPixel(WIDTH / 4, HEIGHT * 3 / 4));
        assertEquals(Color.BLUE, target.getPixel(WIDTH * 3 / 4, HEIGHT * 3 / 4));
        target.eraseColor(Color.TRANSPARENT);
        p.setXfermode(null);
        canvas.drawBitmap(b1, 0, 0, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST));
        canvas.drawBitmap(b2, 0, HEIGHT / 2, p);
        assertEquals(Color.RED, target.getPixel(WIDTH / 4, HEIGHT / 4));
        assertEquals(Color.RED, target.getPixel(WIDTH / 4, HEIGHT * 3 / 4));
        assertEquals(Color.TRANSPARENT, target.getPixel(WIDTH * 3 / 4, HEIGHT * 3 / 4));
        target.eraseColor(Color.TRANSPARENT);
        p.setXfermode(null);
        canvas.drawBitmap(b1, 0, 0, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        canvas.drawBitmap(b2, 0, HEIGHT / 2, p);
        assertEquals(Color.RED, target.getPixel(WIDTH / 4, HEIGHT / 4));
        assertEquals(Color.MAGENTA, target.getPixel(WIDTH / 4, HEIGHT * 3 / 4));
        assertEquals(Color.BLUE, target.getPixel(WIDTH * 3 / 4, HEIGHT * 3 / 4));
    }
}
