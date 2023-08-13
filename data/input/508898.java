@TestTargetClass(Point.class)
public class PointTest extends AndroidTestCase {
    private Point mPoint;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPoint = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Point",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Point",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Point",
            args = {android.graphics.Point.class}
        )
    })
    public void testConstructor() {
        mPoint = new Point();
        mPoint = new Point(10, 10);
        Point point = new Point(10, 10);
        mPoint = new Point(point);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {int.class, int.class}
    )
    public void testSet() {
        mPoint = new Point();
        mPoint.set(3, 4);
        assertEquals(3, mPoint.x);
        assertEquals(4, mPoint.y);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {int.class, int.class}
    )
    public void testEquals1() {
        mPoint = new Point(3, 4);
        assertTrue(mPoint.equals(3, 4));
        assertFalse(mPoint.equals(4, 3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals2() {
        mPoint = new Point(3, 4);
        Point point = new Point(3, 4);
        assertTrue(mPoint.equals(point));
        point = new Point(4, 3);
        assertFalse(mPoint.equals(point));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        mPoint = new Point(10, 10);
        Point p = new Point(100, 10);
        assertTrue(p.hashCode() != mPoint.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        mPoint = new Point();
        assertNotNull(mPoint.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offset",
        args = {int.class, int.class}
    )
    public void testOffset() {
        mPoint = new Point(10, 10);
        mPoint.offset(1, 1);
        assertEquals(11, mPoint.x);
        assertEquals(11, mPoint.y);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "negate",
        args = {}
    )
    public void testNegate() {
        mPoint = new Point(10, 10);
        mPoint.negate();
        assertEquals(-10, mPoint.x);
        assertEquals(-10, mPoint.y);
    }
}
