@TestTargetClass(LinearGradient.class)
public class LinearGradientTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LinearGradient",
            args = {float.class, float.class, float.class, float.class, int[].class, float[].class,
                    android.graphics.Shader.TileMode.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "LinearGradient",
            args = {float.class, float.class, float.class, float.class, int.class, int.class,
                    android.graphics.Shader.TileMode.class}
        )
    })
    public void testLinearGradient() {
        Bitmap b;
        LinearGradient lg;
        int[] color = { Color.BLUE, Color.GREEN, Color.RED };
        float[] position = { 0.0f, 1.0f / 3.0f, 2.0f / 3.0f };
        lg = new LinearGradient(0, 0, 0, 40, color, position, TileMode.CLAMP);
        b = drawLinearGradient(lg);
        assertEquals(b.getPixel(10, 10), b.getPixel(20, 10));
        assertTrue(Color.blue(b.getPixel(10, 0)) > Color.blue(b.getPixel(10, 5)));
        assertTrue(Color.blue(b.getPixel(10, 5)) > Color.blue(b.getPixel(10, 10)));
        assertTrue(Color.green(b.getPixel(10, 0)) < Color.green(b.getPixel(10, 5)));
        assertTrue(Color.green(b.getPixel(10, 5)) < Color.green(b.getPixel(10, 10)));
        assertTrue(Color.green(b.getPixel(10, 15)) > Color.green(b.getPixel(10, 20)));
        assertTrue(Color.green(b.getPixel(10, 20)) > Color.green(b.getPixel(10, 25)));
        assertTrue(Color.red(b.getPixel(10, 15)) < Color.red(b.getPixel(10, 20)));
        assertTrue(Color.red(b.getPixel(10, 20)) < Color.red(b.getPixel(10, 25)));
        lg = new LinearGradient(0, 0, 0, 40, Color.RED, Color.BLUE, TileMode.CLAMP);
        b= drawLinearGradient(lg);
        assertEquals(b.getPixel(10, 10), b.getPixel(20, 10));
        assertTrue(Color.red(b.getPixel(10, 0)) > Color.red(b.getPixel(10, 15)));
        assertTrue(Color.red(b.getPixel(10, 15)) > Color.red(b.getPixel(10, 30)));
        assertTrue(Color.blue(b.getPixel(10, 0)) < Color.blue(b.getPixel(10, 15)));
        assertTrue(Color.blue(b.getPixel(10, 15)) < Color.blue(b.getPixel(10, 30)));
    }
    private Bitmap drawLinearGradient(LinearGradient lg) {
        Paint paint = new Paint();
        paint.setShader(lg);
        Bitmap b = Bitmap.createBitmap(40, 40, Config.ARGB_8888);
        b.eraseColor(Color.BLACK);
        Canvas canvas = new Canvas(b);
        canvas.drawPaint(paint);
        return b;
    }
}
