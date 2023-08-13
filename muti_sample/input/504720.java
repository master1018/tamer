@TestTargetClass(SuperscriptSpan.class)
public class SuperscriptSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SuperscriptSpan",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "SuperscriptSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        Parcel p = Parcel.obtain();
        superscriptSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new SuperscriptSpan(p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateMeasureState() {
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        TextPaint tp = new TextPaint();
        float ascent = tp.ascent();
        int baselineShift = 100;
        tp.baselineShift = baselineShift;
        superscriptSpan.updateMeasureState(tp);
        assertEquals(baselineShift + (int) (ascent / 2), tp.baselineShift);
        try {
            superscriptSpan.updateMeasureState(null);
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
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        TextPaint tp = new TextPaint();
        float ascent = tp.ascent();
        int baselineShift = 50;
        tp.baselineShift = baselineShift;
        superscriptSpan.updateDrawState(tp);
        assertEquals(baselineShift + (int) (ascent / 2), tp.baselineShift);
        try {
            superscriptSpan.updateDrawState(null);
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
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        superscriptSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        superscriptSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        SuperscriptSpan superscriptSpan = new SuperscriptSpan();
        superscriptSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new SuperscriptSpan(p);
        p.recycle();
    }
}
