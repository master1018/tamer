@TestTargetClass(StyleSpan.class)
public class StyleSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StyleSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StyleSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        StyleSpan styleSpan = new StyleSpan(2);
        Parcel p = Parcel.obtain();
        styleSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new StyleSpan(p);
        new StyleSpan(-2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getStyle",
        args = {}
    )
    public void testGetStyle() {
        StyleSpan styleSpan = new StyleSpan(2);
        assertEquals(2, styleSpan.getStyle());
        styleSpan = new StyleSpan(-2);
        assertEquals(-2, styleSpan.getStyle());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateMeasureState() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        TextPaint tp = new TextPaint();
        Typeface tf = Typeface.defaultFromStyle(Typeface.NORMAL);
        tp.setTypeface(tf);
        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.NORMAL, tp.getTypeface().getStyle());
        styleSpan.updateMeasureState(tp);
        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.BOLD, tp.getTypeface().getStyle());
        try {
            styleSpan.updateMeasureState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateDrawState() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        TextPaint tp = new TextPaint();
        Typeface tf = Typeface.defaultFromStyle(Typeface.NORMAL);
        tp.setTypeface(tf);
        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.NORMAL, tp.getTypeface().getStyle());
        styleSpan.updateDrawState(tp);
        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.BOLD, tp.getTypeface().getStyle());
        try {
            styleSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "describeContents",
        args = {}
    )
    public void testDescribeContents() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        styleSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        styleSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        styleSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        StyleSpan newSpan = new StyleSpan(p);
        assertEquals(Typeface.BOLD, newSpan.getStyle());
        p.recycle();
    }
}
