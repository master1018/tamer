@TestTargetClass(DynamicDrawableSpan.class)
public class DynamicDrawableSpanTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of DynamicDrawableSpan.",
            method = "DynamicDrawableSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of DynamicDrawableSpan.",
            method = "DynamicDrawableSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test getVerticalAlignment().",
            method = "getVerticalAlignment",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc for constructor DynamicDrawableSpan()")
    public void testConstructor() {
        DynamicDrawableSpan d = new MyDynamicDrawableSpan();
        assertEquals(DynamicDrawableSpan.ALIGN_BOTTOM, d.getVerticalAlignment());
        d = new MyDynamicDrawableSpan(DynamicDrawableSpan.ALIGN_BASELINE);
        assertEquals(DynamicDrawableSpan.ALIGN_BASELINE, d.getVerticalAlignment());
        d = new MyDynamicDrawableSpan(DynamicDrawableSpan.ALIGN_BOTTOM);
        assertEquals(DynamicDrawableSpan.ALIGN_BOTTOM, d.getVerticalAlignment());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSize(Paint paint, CharSequence text, int start, int end," +
                " FontMetricsInt fm). And the following parameters are never used in" +
                " this method: paint, text, start, end",
        method = "getSize",
        args = {android.graphics.Paint.class, java.lang.CharSequence.class, int.class,
                int.class, android.graphics.Paint.FontMetricsInt.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSize() {
        DynamicDrawableSpan dynamicDrawableSpan = new MyDynamicDrawableSpan();
        FontMetricsInt fm = new FontMetricsInt();
        assertEquals(0, fm.ascent);
        assertEquals(0, fm.bottom);
        assertEquals(0, fm.descent);
        assertEquals(0, fm.leading);
        assertEquals(0, fm.top);
        Rect rect = dynamicDrawableSpan.getDrawable().getBounds();
        assertEquals(rect.right, dynamicDrawableSpan.getSize(null, null, 0, 0, fm));
        assertEquals(-rect.bottom, fm.ascent);
        assertEquals(0, fm.bottom);
        assertEquals(0, fm.descent);
        assertEquals(0, fm.leading);
        assertEquals(-rect.bottom, fm.top);
        assertEquals(rect.right, dynamicDrawableSpan.getSize(null, null, 0, 0, null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test draw(Canvas canvas, CharSequence text, int start, int end, float x," +
                " int top, int y, int bottom, Paint paint). And the following parameters are" +
                " never used in this method: text, start, end, top, y, paint",
        method = "draw",
        args = {android.graphics.Canvas.class, java.lang.CharSequence.class, int.class, int.class,
                float.class, int.class, int.class, int.class, android.graphics.Paint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDraw() {
        DynamicDrawableSpan dynamicDrawableSpan = new MyDynamicDrawableSpan();
        Canvas canvas = new Canvas();
        dynamicDrawableSpan.draw(canvas, null, 0, 0, 1.0f, 0, 0, 1, null);
        try {
            dynamicDrawableSpan.draw(null, null, 0, 0, 1.0f, 0, 0, 1, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    private class MyDynamicDrawableSpan extends DynamicDrawableSpan {
        public MyDynamicDrawableSpan() {
            super();
        }
        protected MyDynamicDrawableSpan(int verticalAlignment) {
            super(verticalAlignment);
        }
        @Override
        public Drawable getDrawable() {
            return getContext().getResources().getDrawable(R.drawable.scenery);
        }
    }
}
