@TestTargetClass(Paint.Cap.class)
public class Paint_CapTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Cap.BUTT, Cap.valueOf("BUTT"));
        assertEquals(Cap.ROUND, Cap.valueOf("ROUND"));
        assertEquals(Cap.SQUARE, Cap.valueOf("SQUARE"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrokeCap",
            args = {android.graphics.Paint.Cap.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrokeCap",
            args = {}
        )
    })
    public void testValues() {
        Cap[] actual = Cap.values();
        assertEquals(3, actual.length);
        assertEquals(Cap.BUTT, actual[0]);
        assertEquals(Cap.ROUND, actual[1]);
        assertEquals(Cap.SQUARE, actual[2]);
        Paint p = new Paint();
        p.setStrokeCap(actual[0]);
        assertEquals(Cap.BUTT, p.getStrokeCap());
        p.setStrokeCap(actual[1]);
        assertEquals(Cap.ROUND, p.getStrokeCap());
        p.setStrokeCap(actual[2]);
        assertEquals(Cap.SQUARE, p.getStrokeCap());
    }
}
