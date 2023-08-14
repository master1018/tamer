@TestTargetClass(Canvas.EdgeType.class)
public class Canvas_EdgeTypeTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(EdgeType.BW, EdgeType.valueOf("BW"));
        assertEquals(EdgeType.AA, EdgeType.valueOf("AA"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues(){
        EdgeType[] edgeType = EdgeType.values();
        assertEquals(2, edgeType.length);
        assertEquals(EdgeType.BW, edgeType[0]);
        assertEquals(EdgeType.AA, edgeType[1]);
        Canvas c = new Canvas();
        c.quickReject(new Path(), EdgeType.AA);
        c.quickReject(new Path(), EdgeType.BW);
        c.quickReject(new RectF(), EdgeType.AA);
        c.quickReject(new RectF(), EdgeType.BW);
        c.quickReject(10, 100, 100, 10, EdgeType.AA);
        c.quickReject(10, 100, 100, 10, EdgeType.BW);
    }
}
