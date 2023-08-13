@TestTargetClass(BackgroundColorSpan.class)
public class BackgroundColorSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of BackgroundColorSpan.",
            method = "BackgroundColorSpan",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of BackgroundColorSpan.",
            method = "BackgroundColorSpan",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        BackgroundColorSpan b = new BackgroundColorSpan(Color.GREEN);
        final Parcel p = Parcel.obtain();
        b.writeToParcel(p, 0);
        p.setDataPosition(0);
        new BackgroundColorSpan(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link BackgroundColorSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "should add @throws clause into javadoc of " +
            "BackgroundColorSpan#updateDrawState(TextPaint) when the input TextPaint is null")
    public void testUpdateDrawState() {
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.BLACK);
        TextPaint tp = new TextPaint();
        backgroundColorSpan.updateDrawState(tp);
        assertEquals(Color.BLACK, tp.bgColor);
        backgroundColorSpan = new BackgroundColorSpan(Color.BLUE);
        backgroundColorSpan.updateDrawState(tp);
        assertEquals(Color.BLUE, tp.bgColor);
        try {
            backgroundColorSpan.updateDrawState(null);
            fail("did not throw NullPointerException when TextPaint is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link BackgroundColorSpan#getBackgroundColor()}",
        method = "getBackgroundColor",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetBackgroundColor() {
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.CYAN);
        assertEquals(Color.CYAN, backgroundColorSpan.getBackgroundColor());
        backgroundColorSpan = new BackgroundColorSpan(Color.GRAY);
        assertEquals(Color.GRAY, backgroundColorSpan.getBackgroundColor());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.RED);
        backgroundColorSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSpanTypeId().",
        method = "getSpanTypeId",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetSpanTypeId() {
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.RED);
        backgroundColorSpan.getSpanTypeId();
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
        BackgroundColorSpan backgroundColorSpan = new BackgroundColorSpan(Color.RED);
        backgroundColorSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        BackgroundColorSpan b = new BackgroundColorSpan(p);
        assertEquals(Color.RED, b.getBackgroundColor());
        p.recycle();
        p = Parcel.obtain();
        backgroundColorSpan = new BackgroundColorSpan(Color.MAGENTA);
        backgroundColorSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        b = new BackgroundColorSpan(p);
        assertEquals(Color.MAGENTA, b.getBackgroundColor());
        p.recycle();
    }
}
