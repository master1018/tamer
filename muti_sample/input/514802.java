@TestTargetClass(DashPathEffect.class)
public class DashPathEffectTest extends TestCase {
    private static final int BITMAP_WIDTH = 200;
    private static final int BITMAP_HEIGHT = 20;
    private static final int START_X = 10;
    private static final int END_X = BITMAP_WIDTH - START_X;
    private static final int COORD_Y = BITMAP_HEIGHT / 2;
    private static final float[] PATTERN = new float[] { 15, 5, 10, 5 };
    private static final int OFFSET = 5;
    private static final int BACKGROUND = Color.TRANSPARENT;
    private static final int FOREGROUND = Color.GREEN;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "DashPathEffect",
        args = {float[].class, float.class}
    )
    @ToBeFixed(bug = "2039296", explanation = "Javadoc for phase parameter is wrong. The phase" +
        " determines the start offset within the pattern and not an initial gap")
    public void testDashPathEffect() {
        PathEffect effect = new DashPathEffect(PATTERN, OFFSET);
        Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Config.ARGB_8888);
        bitmap.eraseColor(BACKGROUND);
        Path path = new Path();
        path.moveTo(START_X, COORD_Y);
        path.lineTo(END_X, COORD_Y);
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(0);
        paint.setColor(FOREGROUND);
        paint.setPathEffect(effect);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPath(path, paint);
        PatternIterator iterator = new PatternIterator(PATTERN, OFFSET);
        for (int y = 0; y < BITMAP_HEIGHT; y++) {
            for (int x = 0; x < BITMAP_WIDTH; x++) {
                try {
                    if (y == COORD_Y && x >= START_X && x < END_X) {
                        if (iterator.next()) {
                            assertEquals(FOREGROUND, bitmap.getPixel(x, y));
                        } else {
                            assertEquals(BACKGROUND, bitmap.getPixel(x, y));
                        }
                    } else {
                        assertEquals(BACKGROUND, bitmap.getPixel(x, y));
                    }
                } catch (Error e) {
                    Log.w(getClass().getName(), "Failed at (" + x + "," + y + ")");
                    throw e;
                }
            }
        }
    }
    private static class PatternIterator {
        private int mPatternOffset;
        private int mLength;
        private final float[] mPattern;
        PatternIterator(final float[] pattern, int offset) {
            mPattern = pattern;
            while (offset-- > 0) {
                next();
            }
        }
        boolean next() {
            int oldPatternOffset = mPatternOffset;
            mLength += 1;
            if (mLength == mPattern[mPatternOffset]) {
                mLength = 0;
                mPatternOffset += 1;
                mPatternOffset %= mPattern.length;
            }
            return (oldPatternOffset & 1) == 0;
        }
    }
}
