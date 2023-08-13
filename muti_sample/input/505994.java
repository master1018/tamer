@TestTargetClass(RasterizerSpan.class)
public class RasterizerSpanTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of {@link RasterizerSpan}",
        method = "RasterizerSpan",
        args = {android.graphics.Rasterizer.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testConstructor() {
        Rasterizer r = new Rasterizer();
        new RasterizerSpan(r);
        new RasterizerSpan(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link RasterizerSpan#getRasterizer()}",
        method = "getRasterizer",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testGetRasterizer() {
        Rasterizer expected = new Rasterizer();
        RasterizerSpan rasterizerSpan = new RasterizerSpan(expected);
        assertSame(expected, rasterizerSpan.getRasterizer());
        rasterizerSpan = new RasterizerSpan(null);
        assertNull(rasterizerSpan.getRasterizer());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link RasterizerSpan#updateDrawState(TextPaint)}",
        method = "updateDrawState",
        args = {android.text.TextPaint.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "miss javadoc")
    public void testUpdateDrawState() {
        Rasterizer rasterizer = new Rasterizer();
        RasterizerSpan rasterizerSpan = new RasterizerSpan(rasterizer);
        TextPaint tp = new TextPaint();
        assertNull(tp.getRasterizer());
        rasterizerSpan.updateDrawState(tp);
        assertSame(rasterizer, tp.getRasterizer());
        try {
            rasterizerSpan.updateDrawState(null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
}
