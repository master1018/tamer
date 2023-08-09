@TestTargetClass(RelativeSizeSpan.class)
public class RelativeSizeSpanTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeSizeSpan",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RelativeSizeSpan",
            args = {android.os.Parcel.class}
        )
    })
    public void testConstructor() {
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(1.0f);
        Parcel p = Parcel.obtain();
        relativeSizeSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        new RelativeSizeSpan(p);
        new RelativeSizeSpan(-1.0f);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSizeChange",
        args = {}
    )
    public void testGetSizeChange() {
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(2.0f);
        assertEquals(2.0f, relativeSizeSpan.getSizeChange());
        relativeSizeSpan = new RelativeSizeSpan(-2.0f);
        assertEquals(-2.0f, relativeSizeSpan.getSizeChange());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "updateMeasureState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug="1695243", explanation="miss javadoc")
    public void testUpdateMeasureState() {
        float proportion = 3.0f;
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(proportion);
        TextPaint tp = new TextPaint();
        tp.setTextSize(2.0f);
        float oldSize = tp.getTextSize();
        relativeSizeSpan.updateMeasureState(tp);
        assertEquals(2.0f * proportion, tp.getTextSize());
        tp.setTextSize(-3.0f);
        oldSize = tp.getTextSize();
        relativeSizeSpan.updateMeasureState(tp);
        assertEquals(oldSize * proportion, tp.getTextSize());
        try {
            relativeSizeSpan.updateMeasureState(null);
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
        float proportion = 3.0f;
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(proportion);
        TextPaint tp = new TextPaint();
        tp.setTextSize(2.0f);
        float oldSize = tp.getTextSize();
        relativeSizeSpan.updateDrawState(tp);
        assertEquals(oldSize * proportion, tp.getTextSize());
        tp.setTextSize(-3.0f);
        oldSize = tp.getTextSize();
        relativeSizeSpan.updateDrawState(tp);
        assertEquals(oldSize * proportion, tp.getTextSize());
        try {
            relativeSizeSpan.updateDrawState(null);
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
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(2.0f);
        relativeSizeSpan.describeContents();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSpanTypeId",
        args = {}
    )
    public void testGetSpanTypeId() {
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(2.0f);
        relativeSizeSpan.getSpanTypeId();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel p = Parcel.obtain();
        float proportion = 3.0f;
        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(proportion);
        relativeSizeSpan.writeToParcel(p, 0);
        p.setDataPosition(0);
        RelativeSizeSpan newSpan = new RelativeSizeSpan(p);
        assertEquals(proportion, newSpan.getSizeChange());
        p.recycle();
    }
}
