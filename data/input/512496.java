@TestTargetClass(RadialGradient.class)
public class RadialGradientTest extends TestCase {
    private static final int SIZE = 200;
    private static final int RADIUS = 80;
    private static final int CENTER = SIZE / 2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RadialGradient",
        args = {float.class, float.class, float.class, int[].class, float[].class,
                TileMode.class}
    )
    public void testRadialGradient() {
        final int[] colors = { Color.BLUE, Color.GREEN, Color.RED };
        final float[] positions = { 0f, 0.3f, 1f };
        int tolerance = (int)(0xFF / (0.3f * RADIUS) * 2);
        RadialGradient rg = new RadialGradient(CENTER, CENTER, RADIUS, colors, positions,
                Shader.TileMode.CLAMP);
        Bitmap b = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint p = new Paint();
        p.setShader(rg);
        canvas.drawRect(0, 0, SIZE, SIZE, p);
        checkPixels(b, colors, positions, tolerance);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RadialGradient",
        args = {float.class, float.class, float.class, int.class, int.class, TileMode.class}
    )
    public void testRadialGradientWithColor() {
        final int[] colors = { Color.BLUE, Color.GREEN };
        final float[] positions = { 0f, 1f };
        int tolerance = (int)(0xFF / RADIUS * 2);
        RadialGradient rg = new RadialGradient(CENTER, CENTER, RADIUS, colors[0], colors[1],
                Shader.TileMode.CLAMP);
        Bitmap b = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        Paint p = new Paint();
        p.setShader(rg);
        canvas.drawRect(0, 0, SIZE, SIZE, p);
        checkPixels(b, colors, positions, tolerance);
    }
    private void checkPixels(Bitmap bitmap, int[] colors, float[] positions, int tolerance) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double dist = dist(x, y, CENTER, CENTER) / RADIUS;
                int idx;
                int color;
                for (idx = 0; idx < positions.length; idx++) {
                    if (positions[idx] > dist) {
                        break;
                    }
                }
                if (idx == 0) {
                    color = colors[0];
                } else if (idx == positions.length) {
                    color = colors[positions.length - 1];
                } else {
                    int i1 = idx - 1; 
                    int i2 = idx; 
                    double delta = (dist - positions[i1]) / (positions[i2] - positions[i1]);
                    int alpha = (int) ((1d - delta) * Color.alpha(colors[i1]) +
                            delta * Color.alpha(colors[i2]));
                    int red = (int) ((1d - delta) * Color.red(colors[i1]) +
                            delta * Color.red(colors[i2]));
                    int green = (int) ((1d - delta) * Color.green(colors[i1]) +
                            delta * Color.green(colors[i2]));
                    int blue = (int) ((1d - delta) * Color.blue(colors[i1]) +
                            delta * Color.blue(colors[i2]));
                    color = Color.argb(alpha, red, green, blue);
                }
                int pixel = bitmap.getPixel(x, y);
                assertEquals(Color.alpha(color), Color.alpha(pixel), tolerance);
                assertEquals(Color.red(color), Color.red(pixel), tolerance);
                assertEquals(Color.green(color), Color.green(pixel), tolerance);
                assertEquals(Color.blue(color), Color.blue(pixel), tolerance);
            }
        }
    }
    private double dist(int x1, int y1, int x2, int y2) {
        int distX = x1 - x2;
        int distY = y1 - y2;
        return Math.sqrt(distX * distX + distY * distY);
    }
}
