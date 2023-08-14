@TestTargetClass(CellLocation.class)
public class CellLocationTest extends AndroidTestCase {
    private boolean mOnCellLocationChangedCalled;
    private final Object mLock = new Object();
    private TelephonyManager mTelephonyManager;
    private Looper mLooper;
    private PhoneStateListener mListener;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTelephonyManager =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
    }
    @Override
    protected void tearDown() throws Exception {
        if (mLooper != null) {
            mLooper.quit();
        }
        if (mListener != null) {
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        }
        super.tearDown();
    }
    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getEmpty",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestLocationUpdate",
        args = {}
      )
    })
    public void testCellLocation() throws Throwable {
        CellLocation cl = CellLocation.getEmpty();
        if (cl instanceof GsmCellLocation) {
            GsmCellLocation gcl = (GsmCellLocation) cl;
            assertNotNull(gcl);
            assertEquals(-1, gcl.getCid());
            assertEquals(-1, gcl.getLac());
        }
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onCellLocationChanged(CellLocation location) {
                        synchronized (mLock) {
                            mOnCellLocationChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION);
                Looper.loop();
            }
        });
        t.start();
        CellLocation.requestLocationUpdate();
        synchronized (mLock) {
            while (!mOnCellLocationChangedCalled) {
                mLock.wait();
            }
        }
        Thread.sleep(1000);
        assertTrue(mOnCellLocationChangedCalled);
        t.checkException();
    }
}
