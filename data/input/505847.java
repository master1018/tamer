@TestTargetClass(QuoteSpan.class)
public class QuoteSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of QuoteSpan.",
            method = "QuoteSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of QuoteSpan.",
            method = "QuoteSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of QuoteSpan.",
            method = "QuoteSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new QuoteSpan();
        QuoteSpan q = new QuoteSpan(Color.RED);
        final Parcel p = Parcel.obtain();
        q.writeToParcel(p, 0);
        p.setDataPosition(0);
        new QuoteSpan(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getLeadingMargin(boolean first).",
        method = "getLeadingMargin",
        args = {boolean.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetLeadingMargin() {
        QuoteSpan quoteSpan = new QuoteSpan();
        quoteSpan.getLeadingMargin(true);
        quoteSpan.getLeadingMargin(false);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getColor().",
        method = "getColor",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetColor() {
        QuoteSpan quoteSpan = new QuoteSpan(Color.BLACK);
        assertEquals(Color.BLACK, quoteSpan.getColor());
        quoteSpan = new QuoteSpan(Color.BLUE);
        assertEquals(Color.BLUE, quoteSpan.getColor());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top," +
                " int baseline, int bottom, CharSequence text, int start, int end," +
                " boolean first, Layout layout).",
        method = "drawLeadingMargin",
        args = {android.graphics.Canvas.class, android.graphics.Paint.class, int.class,
                int.class, int.class, int.class, int.class, java.lang.CharSequence.class,
                int.class, int.class, boolean.class, android.text.Layout.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc, and have not found a reasonable" +
            " way to test it automatically.")
    public void testDrawLeadingMargin() {
        QuoteSpan quoteSpan = new QuoteSpan();
        Canvas c = new Canvas();
        Paint p = new Paint();
        quoteSpan.drawLeadingMargin(c, p, 0, 0, 0, 0, 0, null, 0, 0, true, null);
        try {
            quoteSpan.drawLeadingMargin(null, null, 0, 0, 0, 0, 0, null, 0, 0, true, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        QuoteSpan quoteSpan = new QuoteSpan(Color.RED);
        quoteSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        QuoteSpan quoteSpan = new QuoteSpan(Color.RED);
        quoteSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        QuoteSpan quoteSpan = new QuoteSpan(Color.RED);
        quoteSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        QuoteSpan q = new QuoteSpan(p);
        assertEquals(Color.RED, q.getColor());
        p.recycle();
        p = Parcel.obtain();
        quoteSpan = new QuoteSpan(Color.MAGENTA);
        quoteSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        q = new QuoteSpan(p);
        assertEquals(Color.MAGENTA, q.getColor());
        p.recycle();
    }
}
