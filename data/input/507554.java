@TestTargetClass(android.graphics.drawable.shapes.RoundRectShape.class)
public class RoundRectShapeTest extends TestCase {
    private static final int TEST_WIDTH  = 100;
    private static final int TEST_HEIGHT = 200;
    private static final int TEST_COLOR_1 = 0xFF00FF00;
    private static final int TEST_COLOR_2 = 0xFFFF0000;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RoundRectShape",
        args = {float[].class, android.graphics.RectF.class, float[].class}
    )
    @ToBeFixed(bug = "1695243", explanation = "In some condition, NullPointerException or " +
            "ArrayIndexOutOfBoundsException will be thrown, which is not mentioned in javadoc.")
    public void testConstructor() {
        new RoundRectShape(new float[8], new RectF(), new float[8]);
        new RoundRectShape(new float[9], new RectF(), new float[9]);
        new RoundRectShape(new float[8], null, new float[8]);
        new RoundRectShape(new float[8], new RectF(), null);
        try {
            new RoundRectShape(new float[7], new RectF(), new float[8]);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            new RoundRectShape(null, new RectF(), new float[8]);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new RoundRectShape(new float[8], new RectF(), new float[7]);
            fail("Should throw ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class, android.graphics.Paint.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "onResize",
            args = {float.class, float.class}
        )
    })
    public void testDraw() {
        float[] outerR = new float[] { 12, 12, 0, 0, 0, 0, 0, 0 };
        RectF   inset = new RectF(6, 6, 6, 6);
        float[] innerR = new float[] { 12, 12, 0, 0, 0, 0, 0, 0 };
        RoundRectShape roundRectShape = new RoundRectShape(outerR, inset, innerR);
        Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Style.FILL);
        paint.setColor(TEST_COLOR_1);
        roundRectShape.resize(TEST_WIDTH, TEST_HEIGHT);
        roundRectShape.draw(canvas, paint);
        assertEquals(0, bitmap.getPixel(0, 0));
        assertEquals(TEST_COLOR_1, bitmap.getPixel(TEST_WIDTH / 2, 0));
        paint.setColor(TEST_COLOR_2);
        roundRectShape.draw(canvas, paint);
        assertEquals(0, bitmap.getPixel(0, 0));
        assertEquals(TEST_COLOR_2, bitmap.getPixel(TEST_WIDTH / 2, 0));
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "clone",
        args = {}
    )
    public void testClone() throws CloneNotSupportedException {
        RoundRectShape roundRectShape = new RoundRectShape(new float[8], new RectF(), new float[8]);
        roundRectShape.resize(100f, 200f);
        RoundRectShape clonedShape = roundRectShape.clone();
        assertEquals(100f, roundRectShape.getWidth());
        assertEquals(200f, roundRectShape.getHeight());
        assertNotSame(roundRectShape, clonedShape);
        assertEquals(roundRectShape.getWidth(), clonedShape.getWidth());
        assertEquals(roundRectShape.getHeight(), clonedShape.getHeight());
    }
}
