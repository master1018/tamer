@TestTargetClass(TypefaceSpan.class)
public class TypefaceSpanTest extends TestCase {
    private static final String FAMILY = "monospace";
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of TypefaceSpan.",
            method = "TypefaceSpan",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of TypefaceSpan.",
            method = "TypefaceSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        TypefaceSpan t = new TypefaceSpan(FAMILY);
        final Parcel p = Parcel.obtain();
        t.writeToParcel(p, 0);
        p.setDataPosition(0);
        new TypefaceSpan(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getFamily().",
        method = "getFamily",
        args = {}
    )
    public void testGetFamily() {
        TypefaceSpan typefaceSpan = new TypefaceSpan(FAMILY);
        assertEquals(FAMILY, typefaceSpan.getFamily());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test updateMeasureState(TextPaint paint).",
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws NullPointerException clause" +
            " into javadoc when input TextPaint is null")
    public void testUpdateMeasureState() {
        TypefaceSpan typefaceSpan = new TypefaceSpan(FAMILY);
        TextPaint tp = new TextPaint();
        assertNull(tp.getTypeface());
        typefaceSpan.updateMeasureState(tp);
        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.NORMAL, tp.getTypeface().getStyle());
        try {
            typefaceSpan.updateMeasureState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test updateDrawState(TextPaint ds).",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws NullPointerException clause" +
            " into javadoc when input TextPaint is null")
    public void testUpdateDrawState() {
        TypefaceSpan typefaceSpan = new TypefaceSpan(FAMILY);
        TextPaint tp = new TextPaint();
        assertNull(tp.getTypeface());
        typefaceSpan.updateDrawState(tp);
        assertNotNull(tp.getTypeface());
        assertEquals(Typeface.NORMAL, tp.getTypeface().getStyle());
        try {
            typefaceSpan.updateDrawState(null);
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
        TypefaceSpan typefaceSpan = new TypefaceSpan(FAMILY);
        typefaceSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        TypefaceSpan typefaceSpan = new TypefaceSpan(FAMILY);
        typefaceSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        TypefaceSpan typefaceSpan = new TypefaceSpan(FAMILY);
        typefaceSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        TypefaceSpan t = new TypefaceSpan(p);
        assertEquals(FAMILY, t.getFamily());
        p.recycle();
    }
}
