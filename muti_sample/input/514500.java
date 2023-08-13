@TestTargetClass(MaskFilterSpan.class)
public class MaskFilterSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link MaskFilterSpan}",
        method = "MaskFilterSpan",
        args = {android.graphics.MaskFilter.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        MaskFilter mf = new MaskFilter();
        new MaskFilterSpan(mf);
        new MaskFilterSpan(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link MaskFilterSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testUpdateDrawState() {
        MaskFilter mf = new MaskFilter();
        MaskFilterSpan maskFilterSpan = new MaskFilterSpan(mf);
        TextPaint tp = new TextPaint();
        assertNull(tp.getMaskFilter());
        maskFilterSpan.updateDrawState(tp);
        assertSame(mf, tp.getMaskFilter());
        try {
            maskFilterSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link MaskFilterSpan#getMaskFilter()}",
        method = "getMaskFilter",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetMaskFilter() {
        MaskFilter expected = new MaskFilter();
        MaskFilterSpan maskFilterSpan = new MaskFilterSpan(expected);
        assertSame(expected, maskFilterSpan.getMaskFilter());
        maskFilterSpan = new MaskFilterSpan(null);
        assertNull(maskFilterSpan.getMaskFilter());
    }
}
