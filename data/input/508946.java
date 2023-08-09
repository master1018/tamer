@TestTargetClass(Standard.class)
public class AlignmentSpan_StandardTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "AlignmentSpan.Standard",
            args = {android.text.Layout.Alignment.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of Standard.",
            method = "AlignmentSpan.Standard",
            args = {android.os.Parcel.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        new Standard(Alignment.ALIGN_CENTER);
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        final Parcel p = Parcel.obtain();
        standard.writeToParcel(p, 0);
        p.setDataPosition(0);
        new Standard(p);
        p.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getAlignment().",
        method = "getAlignment",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetAlignment() {
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        assertEquals(Alignment.ALIGN_NORMAL, standard.getAlignment());
        standard = new Standard(Alignment.ALIGN_OPPOSITE);
        assertEquals(Alignment.ALIGN_OPPOSITE, standard.getAlignment());
        standard = new Standard(Alignment.ALIGN_CENTER);
        assertEquals(Alignment.ALIGN_CENTER, standard.getAlignment());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test describeContents().",
        method = "describeContents",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testDescribeContents() {
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
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
        Standard standard = new Standard(Alignment.ALIGN_NORMAL);
        standard.getSpanTypeId();
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
        Standard s = new Standard(Alignment.ALIGN_NORMAL);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        Standard standard = new Standard(p);
        assertEquals(Alignment.ALIGN_NORMAL, standard.getAlignment());
        p.recycle();
        s = new Standard(Alignment.ALIGN_OPPOSITE);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        standard = new Standard(p);
        assertEquals(Alignment.ALIGN_OPPOSITE, standard.getAlignment());
        p.recycle();
        s = new Standard(Alignment.ALIGN_CENTER);
        s.writeToParcel(p, 0);
        p.setDataPosition(0);
        standard = new Standard(p);
        assertEquals(Alignment.ALIGN_CENTER, standard.getAlignment());
        p.recycle();
    }
}
