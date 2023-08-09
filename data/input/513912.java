@TestTargetClass(Paint.FontMetricsInt.class)
public class Paint_FontMetricsIntTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Paint.FontMetricsInt",
        args = {}
    )
    public void testConstructor() {
        new Paint.FontMetricsInt();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        int top = 1;
        int ascent = 2;
        int descent = 3;
        int bottom = 4;
        int leading = 5;
        FontMetricsInt fontMetricsInt = new FontMetricsInt();
        fontMetricsInt.top = top;
        fontMetricsInt.ascent = ascent;
        fontMetricsInt.descent = descent;
        fontMetricsInt.bottom = bottom;
        fontMetricsInt.leading = leading;
        assertNotNull(fontMetricsInt.toString());
    }
}
