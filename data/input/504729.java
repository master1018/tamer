@TestTargetClass(IconMarginSpan.class)
public class IconMarginSpanTest extends AndroidTestCase {
    private static final int WIDTH = 80;
    private static final int HEIGHT = 120;
    private static final int[] COLOR = new int[WIDTH * HEIGHT];
    private static final Bitmap BITMAP_80X120 =
        Bitmap.createBitmap(COLOR, WIDTH, HEIGHT, Bitmap.Config.RGB_565);
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of IconMarginSpan.",
            method = "IconMarginSpan",
            args = {android.graphics.Bitmap.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of IconMarginSpan.",
            method = "IconMarginSpan",
            args = {android.graphics.Bitmap.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new IconMarginSpan(BITMAP_80X120);
        new IconMarginSpan(BITMAP_80X120, 1);
        new IconMarginSpan(null, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getLeadingMargin(boolean first). And the parameter is never used.",
        method = "getLeadingMargin",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetLeadingMargin() {
        IconMarginSpan iconMarginSpan = new IconMarginSpan(BITMAP_80X120, 1);
        int leadingMargin1 = iconMarginSpan.getLeadingMargin(true);
        iconMarginSpan = new IconMarginSpan(BITMAP_80X120, 2);
        int leadingMargin2 = iconMarginSpan.getLeadingMargin(true);
        assertTrue(leadingMargin2 > leadingMargin1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top," +
                " int baseline, int bottom, CharSequence text, int start, int end," +
                " boolean first, Layout layout). And the following parameters are never" +
                " used in this method: top, baseline, bottom, start, end, first",
        method = "drawLeadingMargin",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class, int.class,
                int.class, int.class, int.class, int.class, java.lang.CharSequence.class,
                int.class, int.class, boolean.class, android.text.Layout.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testDrawLeadingMargin() {
        IconMarginSpan iconMarginSpan = new IconMarginSpan(BITMAP_80X120, 0);
        Canvas c = new Canvas();
        Spanned text = Html.fromHtml("<b>hello</b>");
        TextPaint p = new TextPaint();
        Layout layout = new StaticLayout("cts test.", p, 200, Layout.Alignment.ALIGN_NORMAL,
                1, 0, true);
        iconMarginSpan.drawLeadingMargin(c, p, 0, 0, 0, 0, 0, text, 0, 0, true, layout);
        try {
            iconMarginSpan.chooseHeight(null, 0, 0, 0, 0, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            iconMarginSpan.chooseHeight("cts test.", 0, 0, 0, 0, null);
            fail("When try to use a String as the text, should throw ClassCastException.");
        } catch (ClassCastException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test chooseHeight(CharSequence text, int start, int end, int istartv," +
                " int v, FontMetricsInt fm).",
        method = "chooseHeight",
        args = {java.lang.CharSequence.class, int.class, int.class, int.class, int.class,
                android.graphics.Paint.FontMetricsInt.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc, should add @throws" +
            " NullPointerException clause into javadoc when input null. And when try to" +
            " use a String as the text, there should not be a ClassCastException")
    public void testChooseHeight() {
        IconMarginSpan iconMarginSpan = new IconMarginSpan(BITMAP_80X120, 0);
        Spanned text = Html.fromHtml("cts test.");
        FontMetricsInt fm = new FontMetricsInt();
        assertEquals(0, fm.ascent);
        assertEquals(0, fm.bottom);
        assertEquals(0, fm.descent);
        assertEquals(0, fm.leading);
        assertEquals(0, fm.top);
        iconMarginSpan.chooseHeight(text, 0, -1, 0, 0, fm);
        assertEquals(0, fm.ascent);
        assertEquals(HEIGHT, fm.bottom);
        assertEquals(HEIGHT, fm.descent);
        assertEquals(0, fm.leading);
        assertEquals(0, fm.top);
        try {
            iconMarginSpan.chooseHeight(null, 0, 0, 0, 0, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            iconMarginSpan.chooseHeight("cts test.", 0, 0, 0, 0, null);
            fail("When try to use a String as the text, should throw ClassCastException.");
        } catch (ClassCastException e) {
        }
    }
}
