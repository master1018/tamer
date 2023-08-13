@TestTargetClass(RectF.class)
public class RectFTest extends AndroidTestCase {
    private RectF mRectF;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRectF = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RectF",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RectF",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RectF",
            args = {android.graphics.RectF.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "RectF",
            args = {android.graphics.Rect.class}
        )
    })
    public void testConstructor() {
        mRectF = null;
        mRectF = new RectF();
        mRectF = null;
        mRectF = new RectF(1.5f, 2.5f, 20.3f, 40.9f);
        mRectF = null;
        RectF rectF = new RectF(1.5f, 2.5f, 20.3f, 40.9f);
        mRectF = new RectF(rectF);
        mRectF = null;
        Rect rect = new Rect(0, 0, 10, 10);
        mRectF = new RectF(rect);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "sort",
        args = {}
    )
    public void testSort() {
        mRectF = new RectF(10, 10, 5, 5);
        assertEquals(10.0f, mRectF.left);
        assertEquals(10.0f, mRectF.top);
        assertEquals(5.0f, mRectF.right);
        assertEquals(5.0f, mRectF.bottom);
        mRectF.sort();
        assertEquals(5.0f, mRectF.left);
        assertEquals(5.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testSet1() {
        mRectF = new RectF();
        mRectF.set(1.0f, 2.0f, 3.0f, 4.0f);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.top);
        assertEquals(3.0f, mRectF.right);
        assertEquals(4.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {android.graphics.RectF.class}
    )
    public void testSet2() {
        RectF rectF = new RectF(1.0f, 2.0f, 3.0f, 4.0f);
        mRectF = new RectF();
        mRectF.set(rectF);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.top);
        assertEquals(3.0f, mRectF.right);
        assertEquals(4.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "set",
        args = {android.graphics.Rect.class}
    )
    public void testSet3() {
        Rect rect = new Rect(1, 2, 3, 4);
        mRectF = new RectF();
        mRectF.set(rect);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.top);
        assertEquals(3.0f, mRectF.right);
        assertEquals(4.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersects",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testIntersects1() {
        mRectF = new RectF(0, 0, 10, 10);
        assertTrue(mRectF.intersects(5, 5, 15, 15));
        assertEquals(0.0f, mRectF.left);
        assertEquals(0.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
        mRectF = new RectF(0, 0, 10, 10);
        assertFalse(mRectF.intersects(15, 15, 25, 25));
        assertEquals(0.0f, mRectF.left);
        assertEquals(0.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersects",
        args = {android.graphics.RectF.class, android.graphics.RectF.class}
    )
    public void testIntersects2() {
        RectF rectF1;
        RectF rectF2;
        rectF1 = new RectF(0, 0, 10, 10);
        rectF2 = new RectF(5, 5, 15, 15);
        assertTrue(RectF.intersects(rectF1, rectF2));
        rectF1 = new RectF(0, 0, 10, 10);
        rectF2 = new RectF(15, 15, 25, 25);
        assertFalse(RectF.intersects(rectF1, rectF2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersect",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testIntersect1() {
        mRectF = new RectF(0, 0, 10, 10);
        assertTrue(mRectF.intersect(5, 5, 15, 15));
        assertEquals(5.0f, mRectF.left);
        assertEquals(5.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
        mRectF = new RectF(0, 0, 10, 10);
        assertFalse(mRectF.intersect(15, 15, 25, 25));
        assertEquals(0.0f, mRectF.left);
        assertEquals(0.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "intersect",
        args = {android.graphics.RectF.class}
    )
    public void testIntersect2() {
        RectF rectF;
        mRectF = new RectF(0, 0, 10, 10);
        rectF= new RectF(5, 5, 15, 15);
        assertTrue(mRectF.intersect(rectF));
        assertEquals(5.0f, mRectF.left);
        assertEquals(5.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
        mRectF = new RectF(0, 0, 10, 10);
        rectF= new RectF(15, 15, 25, 25);
        assertFalse(mRectF.intersect(rectF));
        assertEquals(0.0f, mRectF.left);
        assertEquals(0.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "union",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testUnion1() {
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        mRectF.union(1.0f, 1.0f, 2.0f, 2.0f);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(1.0f, 1.0f, 0.0f, 0.0f);
        mRectF.union(1.0f, 1.0f, 2.0f, 2.0f);
        assertEquals(1.0f, mRectF.top);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        mRectF.union(2.0f, 2.0f, 1.5f, 1.5f);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(1.0f, mRectF.right);
        assertEquals(1.0f, mRectF.bottom);
        mRectF = new RectF();
        mRectF.union(1.0f, 1.0f, 2.0f, 2.0f);
        assertEquals(1.0f, mRectF.top);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        mRectF.union(2.0f, 2.0f, 2.0f, 2.0f);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(1.0f, mRectF.right);
        assertEquals(1.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "union",
        args = {android.graphics.RectF.class}
    )
    public void testUnion2() {
        RectF rectF;
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        rectF = new RectF(1.0f, 1.0f, 2.0f, 2.0f);
        mRectF.union(rectF);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(1.0f, 1.0f, 0.0f, 0.0f);
        rectF = new RectF(1.0f, 1.0f, 2.0f, 2.0f);
        mRectF.union(rectF);
        assertEquals(1.0f, mRectF.top);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        rectF = new RectF(2.0f, 2.0f, 1.5f, 1.5f);
        mRectF.union(rectF);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(1.0f, mRectF.right);
        assertEquals(1.0f, mRectF.bottom);
        mRectF = new RectF(0.0f, 0.0f, 0.0f, 0.0f);
        rectF = new RectF(1.0f, 1.0f, 2.0f, 2.0f);
        mRectF.union(rectF);
        assertEquals(1.0f, mRectF.top);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        rectF = new RectF(2.0f, 2.0f, 2.0f, 2.0f);
        mRectF.union(rectF);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(1.0f, mRectF.right);
        assertEquals(1.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "union",
        args = {float.class, float.class}
    )
    public void testUnion3() {
        mRectF = new RectF(0.0f, 0.0f, 1.0f, 1.0f);
        mRectF.union(2.0f, 2.0f);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(1.0f, 1.0f, 2.0f, 2.0f);
        mRectF.union(0.0f, 0.0f);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF(1.0f, 1.0f, 2.0f, 2.0f);
        mRectF.union(1.5f, 1.5f);
        assertEquals(1.0f, mRectF.top);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
        mRectF = new RectF();
        mRectF.union(2.0f, 2.0f);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.left);
        assertEquals(2.0f, mRectF.right);
        assertEquals(2.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "contains",
        args = {float.class, float.class}
    )
    public void testContains1() {
        mRectF = new RectF(1.0f, 1.0f, 20.0f, 20.0f);
        assertFalse(mRectF.contains(0.9f, 0.9f));
        assertTrue(mRectF.contains(1.0f, 1.0f));
        assertTrue(mRectF.contains(19.9f, 19.9f));
        assertFalse(mRectF.contains(20.0f, 20.0f));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "contains",
        args = {float.class, float.class, float.class, float.class}
    )
    public void testContains2() {
        mRectF = new RectF(1.0f, 1.0f, 20.0f, 20.0f);
        assertTrue(mRectF.contains(1.0f, 1.0f, 20.0f, 20.0f));
        assertTrue(mRectF.contains(2.0f, 2.0f, 19.0f, 19.0f));
        assertFalse(mRectF.contains(21.0f, 21.0f, 22.0f, 22.0f));
        assertFalse(mRectF.contains(0.0f, 0.0f, 19.0f, 19.0f));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "contains",
        args = {android.graphics.RectF.class}
    )
    public void testContains3() {
        RectF rectF;
        mRectF = new RectF(1.0f, 1.0f, 20.0f, 20.0f);
        rectF = new RectF(1.0f, 1.0f, 20.0f, 20.0f);
        assertTrue(mRectF.contains(rectF));
        rectF = new RectF(2.0f, 2.0f, 19.0f, 19.0f);
        assertTrue(mRectF.contains(rectF));
        rectF = new RectF(21.0f, 21.0f, 22.0f, 22.0f);
        assertFalse(mRectF.contains(rectF));
        rectF = new RectF(0.0f, 0.0f, 19.0f, 19.0f);
        assertFalse(mRectF.contains(rectF));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offset",
        args = {float.class, float.class}
    )
    public void testOffset() {
       mRectF = new RectF(5, 5, 10, 10);
       mRectF.offset(1.0f, 1.0f);
       assertEquals(6.0f, mRectF.left);
       assertEquals(6.0f, mRectF.top);
       assertEquals(11.0f, mRectF.right);
       assertEquals(11.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inset",
        args = {float.class, float.class}
    )
    public void testInset() {
        mRectF = new RectF(5.0f, 5.0f, 10.0f, 10.0f);
        mRectF.inset(1.0f, 1.0f);
        assertEquals(6.0f, mRectF.left);
        assertEquals(6.0f, mRectF.top);
        assertEquals(9.0f, mRectF.right);
        assertEquals(9.0f, mRectF.bottom);
        mRectF = new RectF(5.0f, 5.0f, 10.0f, 10.0f);
        mRectF.inset(-1.0f, -1.0f);
        assertEquals(4.0f, mRectF.left);
        assertEquals(4.0f, mRectF.top);
        assertEquals(11.0f, mRectF.right);
        assertEquals(11.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "height",
        args = {}
    )
    public void testHeight() {
        mRectF = new RectF(1.0f, 1.0f, 20.5f, 20.5f);
        assertEquals(19.5f, mRectF.height());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "width",
        args = {}
    )
    public void testWidth() {
        mRectF = new RectF(1.0f, 1.0f, 20.5f, 20.5f);
        assertEquals(19.5f, mRectF.width());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "offsetTo",
        args = {float.class, float.class}
    )
    public void testOffsetTo() {
        mRectF = new RectF(5, 5, 10, 10);
        mRectF.offsetTo(1.0f, 1.0f);
        assertEquals(1.0f, mRectF.left);
        assertEquals(1.0f, mRectF.top);
        assertEquals(6.0f, mRectF.right);
        assertEquals(6.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setEmpty",
        args = {}
    )
    public void testSetEmpty() {
        mRectF = new RectF(1, 2, 3, 4);
        assertEquals(1.0f, mRectF.left);
        assertEquals(2.0f, mRectF.top);
        assertEquals(3.0f, mRectF.right);
        assertEquals(4.0f, mRectF.bottom);
        mRectF.setEmpty();
        assertEquals(0.0f, mRectF.left);
        assertEquals(0.0f, mRectF.top);
        assertEquals(0.0f, mRectF.right);
        assertEquals(0.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isEmpty",
        args = {}
    )
    public void testIsEmpty() {
        mRectF = new RectF();
        assertTrue(mRectF.isEmpty());
        mRectF = new RectF(1.0f, 1.0f, 1.0f, 1.0f);
        assertTrue(mRectF.isEmpty());
        mRectF = new RectF(0.0f, 1.0f, 2.0f, 1.0f);
        assertTrue(mRectF.isEmpty());
        mRectF = new RectF(1.0f, 1.0f, 20.0f, 20.0f);
        assertFalse(mRectF.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "centerX",
        args = {}
    )
    public void testCenterX() {
        mRectF = new RectF(10.0f, 10.0f, 20.0f, 20.0f);
        assertEquals(15.0f, mRectF.centerX());
        mRectF = new RectF(10.5f, 10.0f, 20.0f, 20.0f);
        assertEquals(15.25f, mRectF.centerX());
        mRectF = new RectF(10.4f, 10.0f, 20.0f, 20.0f);
        assertEquals(15.2f, mRectF.centerX());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "centerY",
        args = {}
    )
    public void testCenterY() {
        mRectF = new RectF(10.0f, 10.0f, 20.0f, 20.0f);
        assertEquals(15.0f, mRectF.centerY());
        mRectF = new RectF(10.0f, 10.5f, 20.0f, 20.0f);
        assertEquals(15.25f, mRectF.centerY());
        mRectF = new RectF(10.0f, 10.4f, 20.0f, 20.0f);
        assertEquals(15.2f, mRectF.centerY());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        mRectF = new RectF();
        assertNotNull(mRectF.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIntersect",
        args = {android.graphics.RectF.class, android.graphics.RectF.class}
    )
    public void testSetIntersect() {
        RectF rectF1 = new RectF(0, 0, 10, 10);
        RectF rectF2 = new RectF(5, 5, 15, 15);
        mRectF = new RectF();
        assertTrue(mRectF.setIntersect(rectF1, rectF2));
        assertEquals(5.0f, mRectF.left);
        assertEquals(5.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
        mRectF = new RectF(0, 0, 15, 15);
        assertTrue(mRectF.setIntersect(rectF1, rectF2));
        assertEquals(5.0f, mRectF.left);
        assertEquals(5.0f, mRectF.top);
        assertEquals(10.0f, mRectF.right);
        assertEquals(10.0f, mRectF.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "roundOut",
        args = {android.graphics.Rect.class}
    )
    public void testRoundOut() {
        Rect rect = new Rect();
        mRectF = new RectF(1.2f, 1.8f, 5.2f, 5.8f);
        mRectF.roundOut(rect);
        assertEquals(1, rect.left);
        assertEquals(1, rect.top);
        assertEquals(6, rect.right);
        assertEquals(6, rect.bottom);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "round",
        args = {android.graphics.Rect.class}
    )
    public void testRound() {
        Rect rect = new Rect();
        mRectF = new RectF(1.2f, 1.8f, 5.2f, 5.8f);
        mRectF.round(rect);
        assertEquals(1, rect.left);
        assertEquals(2, rect.top);
        assertEquals(5, rect.right);
        assertEquals(6, rect.bottom);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFromParcel",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        )
    })
    public void testWriteReadParcel() {
        RectF rectOut = new RectF(0.0f,0.0f,10.0f,10.0f);
        Parcel out = Parcel.obtain();
        rectOut.writeToParcel(out, 0);
        out.setDataPosition(0);
        RectF rectIn = new RectF(1.0f,1.0f,1.0f,1.0f);
        rectIn.readFromParcel(out);
        assertEquals(rectOut.left, rectIn.left);
        assertEquals(rectOut.bottom, rectIn.bottom);
        assertEquals(rectOut.right, rectIn.right);
        assertEquals(rectOut.top, rectIn.top);
        assertNotNull(rectIn.toString());
        assertEquals(0, rectIn.describeContents());
    }
}
