@TestTargetClass(StrikethroughSpan.class)
public class StrikethroughSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StrikethroughSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "StrikethroughSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        Parcel p = Parcel.obtain();
        strikethroughSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new StrikethroughSpan(p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateDrawState() {
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        TextPaint tp = new TextPaint();
        tp.setStrikeThruText(false);
        assertFalse(tp.isStrikeThruText());
        strikethroughSpan.updateDrawState(tp);
        assertTrue(tp.isStrikeThruText());
        try {
            strikethroughSpan.updateDrawState(null);
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
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        strikethroughSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        strikethroughSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        StrikethroughSpan strikethroughSpan = new StrikethroughSpan();
        strikethroughSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new StrikethroughSpan(p);
        p.recycle();
    }
}
