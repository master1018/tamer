@TestTargetClass(Paint.Align.class)
public class Paint_AlignTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Align.LEFT, Align.valueOf("LEFT"));
        assertEquals(Align.CENTER, Align.valueOf("CENTER"));
        assertEquals(Align.RIGHT, Align.valueOf("RIGHT"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setTextAlign",
            args = {android.graphics.Paint.Align.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTextAlign",
            args = {}
        )
    })
    public void testValues() {
        Align[] actual = Align.values();
        assertEquals(3, actual.length);
        assertEquals(Align.LEFT, actual[0]);
        assertEquals(Align.CENTER, actual[1]);
        assertEquals(Align.RIGHT, actual[2]);
        Paint p = new Paint();
        p.setTextAlign(actual[0]);
        assertEquals(Align.LEFT, p.getTextAlign());
        p.setTextAlign(actual[1]);
        assertEquals(Align.CENTER, p.getTextAlign());
        p.setTextAlign(actual[2]);
        assertEquals(Align.RIGHT, p.getTextAlign());
    }
}
