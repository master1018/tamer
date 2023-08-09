@TestTargetClass(Paint.Join.class)
public class Paint_JoinTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Join.BEVEL, Join.valueOf("BEVEL"));
        assertEquals(Join.MITER, Join.valueOf("MITER"));
        assertEquals(Join.ROUND, Join.valueOf("ROUND"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStrokeJoin",
            args = {android.graphics.Paint.Join.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStrokeJoin",
            args = {}
        )
    })
    public void testValues() {
        Join[] actual = Join.values();
        assertEquals(3, actual.length);
        assertEquals(Join.MITER, actual[0]);
        assertEquals(Join.ROUND, actual[1]);
        assertEquals(Join.BEVEL, actual[2]);
        Paint p = new Paint();
        p.setStrokeJoin(actual[0]);
        assertEquals(Join.MITER, p.getStrokeJoin());
        p.setStrokeJoin(actual[1]);
        assertEquals(Join.ROUND, p.getStrokeJoin());
        p.setStrokeJoin(actual[2]);
        assertEquals(Join.BEVEL, p.getStrokeJoin());
    }
}
