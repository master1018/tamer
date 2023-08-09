@TestTargetClass(Paint.Style.class)
public class Paint_StyleTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Style.FILL, Style.valueOf("FILL"));
        assertEquals(Style.STROKE, Style.valueOf("STROKE"));
        assertEquals(Style.FILL_AND_STROKE, Style.valueOf("FILL_AND_STROKE"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStyle",
            args = {android.graphics.Paint.Style.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStyle",
            args = {}
        )
    })
    public void testValues() {
        Style[] actual = Style.values();
        assertEquals(3, actual.length);
        assertEquals(Style.FILL, actual[0]);
        assertEquals(Style.STROKE, actual[1]);
        assertEquals(Style.FILL_AND_STROKE, actual[2]);
        Paint p = new Paint();
        p.setStyle(actual[0]);
        assertEquals(Style.FILL, p.getStyle());
        p.setStyle(actual[1]);
        assertEquals(Style.STROKE, p.getStyle());
        p.setStyle(actual[2]);
        assertEquals(Style.FILL_AND_STROKE, p.getStyle());
    }
}
