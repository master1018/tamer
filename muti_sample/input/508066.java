@TestTargetClass(Standard.class)
public class LeadingMarginSpan_StandardTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "LeadingMarginSpan.Standard",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "LeadingMarginSpan.Standard",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "LeadingMarginSpan.Standard",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new Standard(1, 2);
        new Standard(3);
        new Standard(-1, -2);
        new Standard(-3);
        Standard standard = new Standard(10, 20);
        final Parcel p = Parcel.obtain();
        standard.writeToParcel(p, 0);
        p.setDataPosition(0);
        new Standard(p);
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
        int first = 4;
        int rest = 5;
        Standard standard = new LeadingMarginSpan.Standard(first, rest);
        assertEquals(first, standard.getLeadingMargin(true));
        assertEquals(rest, standard.getLeadingMargin(false));
        standard = new LeadingMarginSpan.Standard(-1);
        assertEquals(-1, standard.getLeadingMargin(true));
        assertEquals(-1, standard.getLeadingMargin(false));
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
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDrawLeadingMargin() {
        Standard standard = new LeadingMarginSpan.Standard(10);
        standard.drawLeadingMargin(null, null, 0, 0, 0, 0, 0, null, 0, 0, false, null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        Standard standard = new Standard(1);
        standard.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        Standard standard = new Standard(1);
        standard.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        Standard s = new Standard(10, 20);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        Standard standard = new Standard(p);
        assertEquals(10, standard.getLeadingMargin(true));
        assertEquals(20, standard.getLeadingMargin(false));
        p.recycle();
        s = new Standard(3);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        standard = new Standard(p);
        assertEquals(3, standard.getLeadingMargin(true));
        assertEquals(3, standard.getLeadingMargin(false));
        p.recycle();
    }
}
