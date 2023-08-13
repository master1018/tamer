@TestTargetClass(Region.Op.class)
public class Region_OpTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(Op.DIFFERENCE, Op.valueOf("DIFFERENCE"));
        assertEquals(Op.INTERSECT, Op.valueOf("INTERSECT"));
        assertEquals(Op.UNION, Op.valueOf("UNION"));
        assertEquals(Op.XOR, Op.valueOf("XOR"));
        assertEquals(Op.REVERSE_DIFFERENCE, Op.valueOf("REVERSE_DIFFERENCE"));
        assertEquals(Op.REPLACE, Op.valueOf("REPLACE"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues() {
        Op[] expected = {
                Op.DIFFERENCE,
                Op.INTERSECT,
                Op.UNION,
                Op.XOR,
                Op.REVERSE_DIFFERENCE,
                Op.REPLACE};
        Op[] actual = Op.values();
        assertEquals(expected.length, actual.length);
        assertEquals(expected[0], actual[0]);
        assertEquals(expected[1], actual[1]);
        assertEquals(expected[2], actual[2]);
        assertEquals(expected[3], actual[3]);
        assertEquals(expected[4], actual[4]);
        assertEquals(expected[5], actual[5]);
    }
}
