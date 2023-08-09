@TestTargetClass(PixelXorXfermode.class)
public class PixelXorXfermodeTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PixelXorXfermode",
        args = {int.class}
    )
    public void testPixelXorXfermode() {
        int width = 100;
        int height = 100;
        Bitmap b1 = Bitmap.createBitmap(width / 2, height, Config.ARGB_8888);
        b1.eraseColor(Color.WHITE);
        Bitmap b2 = Bitmap.createBitmap(width, height / 2, Config.ARGB_8888);
        b2.eraseColor(Color.CYAN);
        Bitmap target = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        target.eraseColor(Color.BLACK);
        Canvas canvas = new Canvas(target);
        Paint p = new Paint();
        canvas.drawBitmap(b1, 0, 0, p);
        p.setXfermode(new PixelXorXfermode(Color.GREEN));
        canvas.drawBitmap(b2, 0, height / 2, p);
        assertEquals(Color.WHITE, target.getPixel(width / 4, height / 4));
        assertEquals(Color.YELLOW, target.getPixel(width / 4, height * 3 / 4));
        assertEquals(Color.BLUE, target.getPixel(width * 3 / 4, height * 3 / 4));
        p.setXfermode(new PixelXorXfermode(alphaColor(Color.GREEN, 25)));
        target.eraseColor(alphaColor(Color.BLACK, 42));
        p.setColor(alphaColor(Color.CYAN, 5));
        canvas.drawPaint(p);
        assertEquals(255, Color.alpha(target.getPixel(0, 0)));
    }
    private int alphaColor(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
