@TestTargetClass(VelocityTracker.class)
public class VelocityTrackerTest extends AndroidTestCase {
    private static final float ERROR_TOLERANCE = 0.0001f;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "obtain",
        args = {}
    )
    public void testObtain() {
        VelocityTracker vt = VelocityTracker.obtain();
        assertNotNull(vt);
        vt.recycle();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "recycle",
        args = {}
    )
    public void testRecycle() {
        VelocityTracker vt = VelocityTracker.obtain();
        assertNotNull(vt);
        vt.recycle();
        VelocityTracker vt2 = VelocityTracker.obtain();
        assertEquals(vt, vt2);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "computeCurrentVelocity",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getXVelocity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getYVelocity",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "addMovement",
            args = {android.view.MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clear",
            args = {}
        )
    })
    public void testComputeCurrentVelocity() {
        float XVelocity;
        float YVelocity;
        VelocityTracker vt = VelocityTracker.obtain();
        assertNotNull(vt);
        MotionEvent me = MotionEvent.obtain(0L, 1, 1, .0f, .0f, 0);
        vt.clear();
        me.addBatch(2L, 2, 2, .0f, .0f, 0);
        vt.addMovement(me);
        vt.computeCurrentVelocity(1);
        XVelocity = 2.0f;
        YVelocity = 2.0f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);
        vt.computeCurrentVelocity(10);
        XVelocity = 20.0f;
        YVelocity = 20.0f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);
        for (int i = 3; i < 10; i++) {
            me.addBatch((long)i, (float)i, (float)i, .0f, .0f, 0);
        }
        vt.clear();
        vt.addMovement(me);
        vt.computeCurrentVelocity(1);
        XVelocity = 1.1875744f;
        YVelocity = 1.1875744f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);
        vt.clear();
        me.addBatch(10L, 10, 10, .0f, .0f, 0);
        vt.addMovement(me);
        vt.computeCurrentVelocity(1);
        XVelocity = 1.1562872f;
        YVelocity = 1.1562872f;
        assertEquals(XVelocity, vt.getXVelocity(), ERROR_TOLERANCE);
        assertEquals(YVelocity, vt.getYVelocity(), ERROR_TOLERANCE);
        vt.recycle();
    }
}
