@TestTargetClass(SubscriptSpan.class)
public class SubscriptSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SubscriptSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SubscriptSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        SubscriptSpan subscriptSpan = new SubscriptSpan();
        Parcel p = Parcel.obtain();
        subscriptSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new SubscriptSpan(p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateMeasureState() {
        SubscriptSpan subscriptSpan = new SubscriptSpan();
        TextPaint tp = new TextPaint();
        float ascent = tp.ascent();
        int baselineShift = 100;
        tp.baselineShift = baselineShift;
        subscriptSpan.updateMeasureState(tp);
        assertEquals(baselineShift - (int) (ascent / 2), tp.baselineShift);
        try {
            subscriptSpan.updateMeasureState(null);
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
        SubscriptSpan subscriptSpan = new SubscriptSpan();
        TextPaint tp = new TextPaint();
        float ascent = tp.ascent();
        int baselineShift = 50;
        tp.baselineShift = baselineShift;
        subscriptSpan.updateDrawState(tp);
        assertEquals(baselineShift - (int) (ascent / 2), tp.baselineShift);
        try {
            subscriptSpan.updateDrawState(null);
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
        SubscriptSpan subscriptSpan = new SubscriptSpan();
        subscriptSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        SubscriptSpan subscriptSpan = new SubscriptSpan();
        subscriptSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        SubscriptSpan subscriptSpan = new SubscriptSpan();
        subscriptSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new SubscriptSpan(p);
        p.recycle();
    }
}
