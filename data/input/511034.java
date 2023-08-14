@TestTargetClass(BitmapShader.class)
public class BitmapShaderTest extends TestCase {
    private static final int TILE_WIDTH = 20;
    private static final int TILE_HEIGHT = 20;
    private static final int BORDER_WIDTH = 5;
    private static final int BORDER_COLOR = Color.BLUE;
    private static final int CENTER_COLOR = Color.RED;
    private static final int NUM_TILES = 4;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "BitmapShader",
        args = {Bitmap.class, TileMode.class, TileMode.class}
    )
    public void testBitmapShader() {
        Bitmap tile = Bitmap.createBitmap(TILE_WIDTH, TILE_HEIGHT, Config.ARGB_8888);
        tile.eraseColor(BORDER_COLOR);
        Canvas c = new Canvas(tile);
        Paint p = new Paint();
        p.setColor(CENTER_COLOR);
        c.drawRect(BORDER_WIDTH, BORDER_WIDTH,
                TILE_WIDTH - BORDER_WIDTH, TILE_HEIGHT - BORDER_WIDTH, p);
        BitmapShader shader = new BitmapShader(tile, Shader.TileMode.REPEAT,
                Shader.TileMode.REPEAT);
        Paint paint = new Paint();
        paint.setShader(shader);
        Bitmap b = Bitmap.createBitmap(NUM_TILES * TILE_WIDTH - TILE_WIDTH / 2,
                NUM_TILES * TILE_HEIGHT - TILE_HEIGHT / 2, Config.ARGB_8888);
        b.eraseColor(Color.BLACK);
        Canvas canvas = new Canvas(b);
        canvas.drawPaint(paint);
        for (int y = 0; y < NUM_TILES; y++) {
            for (int x = 0; x < NUM_TILES; x++) {
                checkTile(b, x * TILE_WIDTH, y * TILE_HEIGHT);
            }
        }
    }
    private void checkTile(Bitmap bitmap, int tileX, int tileY) {
        for (int y = 0; y < TILE_HEIGHT; y++) {
            for (int x = 0; x < TILE_WIDTH; x++) {
                if (x < BORDER_WIDTH || x >= TILE_WIDTH - BORDER_WIDTH ||
                    y < BORDER_WIDTH || y >= TILE_HEIGHT - BORDER_WIDTH) {
                    assertColor(BORDER_COLOR, bitmap, x + tileY, y + tileY);
                } else {
                    assertColor(CENTER_COLOR, bitmap, x + tileY, y + tileY);
                }
            }
        }
    }
    private void assertColor(int color, Bitmap bitmap, int x, int y) {
        if (x < bitmap.getWidth() && y < bitmap.getHeight()) {
            assertEquals(color, bitmap.getPixel(x, y));
        } else {
            return;
        }
    }
}
