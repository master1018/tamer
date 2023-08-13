@TestTargetClass(TelephonyManager.class)
public class TelephonyManagerTest extends AndroidTestCase {
    private TelephonyManager mTelephonyManager;
    private boolean mOnCellLocationChangedCalled = false;
    private final Object mLock = new Object();
    private static final int TOLERANCE = 1000;
    private Looper mLooper;
    private PhoneStateListener mListener;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTelephonyManager =
            (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
    }
    @Override
    protected void tearDown() throws Exception {
        if (mListener != null) {
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        }
        super.tearDown();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      method = "listen",
      args = {PhoneStateListener.class, int.class}
    )
    public void testListen() throws Throwable {
        if (mTelephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
            return;
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
        mLooper.quit();
        assertTrue(mOnCellLocationChangedCalled);
        t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
                mOnCellLocationChangedCalled = false;
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
                Looper.loop();
            }
        });
        t.start();
        CellLocation.requestLocationUpdate();
        synchronized (mLock) {
            mLock.wait(TOLERANCE);
        }
        mLooper.quit();
        assertFalse(mOnCellLocationChangedCalled);
    }
    @TestTargets({
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNetworkType",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPhoneType",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getVoiceMailNumber",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSimOperatorName",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNetworkCountryIso",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCellLocation",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDeviceSoftwareVersion",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSimState",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSimSerialNumber",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDeviceId",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSimOperator",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNetworkOperatorName",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSubscriberId",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLine1Number",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNetworkOperator",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getSimCountryIso",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDataActivity",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDataState",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCallState",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "isNetworkRoaming",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getVoiceMailAlphaTag",
        args = {}
      ),
      @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getNeighboringCellInfo",
        args = {}
      )
    })
    public void testTelephonyManager() {
        assertTrue(mTelephonyManager.getNetworkType() >= TelephonyManager.NETWORK_TYPE_UNKNOWN);
        assertTrue(mTelephonyManager.getPhoneType() >= TelephonyManager.PHONE_TYPE_NONE);
        assertTrue(mTelephonyManager.getSimState() >= TelephonyManager.SIM_STATE_UNKNOWN);
        assertTrue(mTelephonyManager.getDataActivity() >= TelephonyManager.DATA_ACTIVITY_NONE);
        assertTrue(mTelephonyManager.getDataState() >= TelephonyManager.DATA_DISCONNECTED);
        assertTrue(mTelephonyManager.getCallState() >= TelephonyManager.CALL_STATE_IDLE);
        mTelephonyManager.getVoiceMailNumber();
        mTelephonyManager.getSimOperatorName();
        mTelephonyManager.getNetworkCountryIso();
        mTelephonyManager.getCellLocation();
        mTelephonyManager.getSimSerialNumber();
        mTelephonyManager.getSimOperator();
        mTelephonyManager.getNetworkOperatorName();
        mTelephonyManager.getSubscriberId();
        mTelephonyManager.getLine1Number();
        mTelephonyManager.getNetworkOperator();
        mTelephonyManager.getSimCountryIso();
        mTelephonyManager.getVoiceMailAlphaTag();
        mTelephonyManager.getNeighboringCellInfo();
        mTelephonyManager.isNetworkRoaming();
        mTelephonyManager.getDeviceId();
        mTelephonyManager.getDeviceSoftwareVersion();
    }
}
