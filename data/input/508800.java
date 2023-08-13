@TestTargetClass(PhoneStateListener.class)
public class PhoneStateListenerTest extends AndroidTestCase {
    public static final long WAIT_TIME = 1000;
    private boolean mOnCallForwardingIndicatorChangedCalled;
    private boolean mOnCallStateChangedCalled;
    private boolean mOnCellLocationChangedCalled;
    private boolean mOnDataActivityCalled;
    private boolean mOnDataConnectionStateChangedCalled;
    private boolean mOnMessageWaitingIndicatorChangedCalled;
    private boolean mOnServiceStateChangedCalled;
    private boolean mOnSignalStrengthChangedCalled;
    private TelephonyManager mTelephonyManager;
    private PhoneStateListener mListener;
    private final Object mLock = new Object();
    private Looper mLooper;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getContext();
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mLooper != null) {
            mLooper.quit();
        }
        if (mListener != null) {
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of {@link PhoneStateListener}",
            method = "PhoneStateListener",
            args = {}
        )
    })
    public void testPhoneStateListener() {
        new PhoneStateListener();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onServiceStateChanged ",
            method = "onServiceStateChanged",
            args = {ServiceState.class}
        )
    })
    public void testOnServiceStateChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onServiceStateChanged(ServiceState serviceState) {
                        synchronized(mLock) {
                            mOnServiceStateChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_SERVICE_STATE);
                Looper.loop();
            }
        });
        assertFalse(mOnServiceStateChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnServiceStateChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnServiceStateChangedCalled);
    }
    private void quitLooper() {
        mLooper.quit();
        mLooper = null;
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onSignalStrengthChanged ",
            method = "onSignalStrengthChanged",
            args = {int.class}
        )
    })
    public void testOnSignalStrengthChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onSignalStrengthChanged(int asu) {
                        synchronized(mLock) {
                            mOnSignalStrengthChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
                Looper.loop();
            }
        });
        assertFalse(mOnSignalStrengthChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnSignalStrengthChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnSignalStrengthChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onMessageWaitingIndicatorChanged ",
            method = "onMessageWaitingIndicatorChanged",
            args = {boolean.class}
        )
    })
    public void testOnMessageWaitingIndicatorChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onMessageWaitingIndicatorChanged(boolean mwi) {
                        synchronized(mLock) {
                            mOnMessageWaitingIndicatorChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(
                        mListener, PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
                Looper.loop();
            }
        });
        assertFalse(mOnMessageWaitingIndicatorChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnMessageWaitingIndicatorChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnMessageWaitingIndicatorChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onCallForwardingIndicatorChanged ",
            method = "onCallForwardingIndicatorChanged",
            args = {boolean.class}
        )
    })
    public void testOnCallForwardingIndicatorChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onCallForwardingIndicatorChanged(boolean cfi) {
                        synchronized(mLock) {
                            mOnCallForwardingIndicatorChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(
                        mListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
                Looper.loop();
            }
        });
        assertFalse(mOnCallForwardingIndicatorChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnCallForwardingIndicatorChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnCallForwardingIndicatorChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onCellLocationChanged ",
            method = "onCellLocationChanged",
            args = {CellLocation.class}
        )
    })
    public void testOnCellLocationChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onCellLocationChanged(CellLocation location) {
                        synchronized(mLock) {
                            mOnCellLocationChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION);
                Looper.loop();
            }
        });
        assertFalse(mOnCellLocationChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnCellLocationChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnCellLocationChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onCallStateChanged ",
            method = "onCallStateChanged",
            args = {int.class, String.class}
        )
    })
    public void testOnCallStateChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        synchronized(mLock) {
                            mOnCallStateChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
                Looper.loop();
            }
        });
        assertFalse(mOnCallStateChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnCallStateChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnCallStateChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onDataConnectionStateChanged ",
            method = "onDataConnectionStateChanged",
            args = {int.class}
        )
    })
    public void testOnDataConnectionStateChanged() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onDataConnectionStateChanged(int state) {
                        synchronized(mLock) {
                            mOnDataConnectionStateChangedCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(
                        mListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
                Looper.loop();
            }
        });
        assertFalse(mOnDataConnectionStateChangedCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnDataConnectionStateChangedCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnDataConnectionStateChangedCalled);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: onDataActivity ",
            method = "onDataActivity",
            args = {int.class}
        )
    })
    public void testOnDataActivity() throws Throwable {
        TestThread t = new TestThread(new Runnable() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mListener = new PhoneStateListener() {
                    @Override
                    public void onDataActivity(int direction) {
                        synchronized(mLock) {
                            mOnDataActivityCalled = true;
                            mLock.notify();
                        }
                    }
                };
                mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_DATA_ACTIVITY);
                Looper.loop();
            }
        });
        assertFalse(mOnDataActivityCalled);
        t.start();
        synchronized (mLock) {
            while(!mOnDataActivityCalled){
                mLock.wait();
            }
        }
        quitLooper();
        t.checkException();
        assertTrue(mOnDataActivityCalled);
    }
}
