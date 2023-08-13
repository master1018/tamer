@TestTargetClass(UnderlineSpan.class)
public class UnderlineSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of UnderlineSpan.",
            method = "UnderlineSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of UnderlineSpan.",
            method = "UnderlineSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new UnderlineSpan();
        final Parcel p = Parcel.obtain();
        new UnderlineSpan(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link UnderlineSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws NullPointerException clause" +
        " into javadoc when input TextPaint is null")
    public void testUpdateDrawState() {
        UnderlineSpan underlineSpan = new UnderlineSpan();
        TextPaint tp = new TextPaint();
        tp.setUnderlineText(false);
        assertFalse(tp.isUnderlineText());
        underlineSpan.updateDrawState(tp);
        assertTrue(tp.isUnderlineText());
        try {
            underlineSpan.updateDrawState(null);
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
    public void testDescribeContents() {
        UnderlineSpan underlineSpan = new UnderlineSpan();
        underlineSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        UnderlineSpan underlineSpan = new UnderlineSpan();
        underlineSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test writeToParcel(Parcel dest, int flags).",
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        UnderlineSpan underlineSpan = new UnderlineSpan();
        underlineSpan.writeToParcel(p, 0);
        new UnderlineSpan(p);
        p.recycle();
    }
}
