public class ScoSocket {
    private static final String TAG = "ScoSocket";
    private static final boolean DBG = true;
    private static final boolean VDBG = false;  
    public static final int STATE_READY = 1;    
    public static final int STATE_ACCEPT = 2;   
    public static final int STATE_CONNECTING = 3;  
    public static final int STATE_CONNECTED = 4;   
    public static final int STATE_CLOSED = 5;   
    private int mState;
    private int mNativeData;
    private Handler mHandler;
    private int mAcceptedCode;
    private int mConnectedCode;
    private int mClosedCode;
    private WakeLock mWakeLock;  
    static {
        classInitNative();
    }
    private native static void classInitNative();
    public ScoSocket(PowerManager pm, Handler handler, int acceptedCode, int connectedCode,
                     int closedCode) {
        initNative();
        mState = STATE_READY;
        mHandler = handler;
        mAcceptedCode = acceptedCode;
        mConnectedCode = connectedCode;
        mClosedCode = closedCode;
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ScoSocket");
        mWakeLock.setReferenceCounted(false);
        if (VDBG) log(this + " SCO OBJECT CTOR");
    }
    private native void initNative();
    protected void finalize() throws Throwable {
        try {
            if (VDBG) log(this + " SCO OBJECT DTOR");
            destroyNative();
            releaseWakeLockNow();
        } finally {
            super.finalize();
        }
    }
    private native void destroyNative();
    public synchronized boolean connect(String address, String name) {
        if (DBG) log("connect() " + this);
        if (mState != STATE_READY) {
            if (DBG) log("connect(): Bad state");
            return false;
        }
        acquireWakeLock();
        if (connectNative(address, name)) {
            mState = STATE_CONNECTING;
            return true;
        } else {
            mState = STATE_CLOSED;
            releaseWakeLockNow();
            return false;
        }
    }
    private native boolean connectNative(String address, String name);
    public synchronized boolean accept() {
        if (VDBG) log("accept() " + this);
        if (mState != STATE_READY) {
            if (DBG) log("Bad state");
            return false;
        }
        if (acceptNative()) {
            mState = STATE_ACCEPT;
            return true;
        } else {
            mState = STATE_CLOSED;
            return false;
        }
    }
    private native boolean acceptNative();
    public synchronized void close() {
        if (DBG) log(this + " SCO OBJECT close() mState = " + mState);
        acquireWakeLock();
        mState = STATE_CLOSED;
        closeNative();
        releaseWakeLock();
    }
    private native void closeNative();
    public synchronized int getState() {
        return mState;
    }
    private synchronized void onConnected(int result) {
        if (VDBG) log(this + " onConnected() mState = " + mState + " " + this);
        if (mState != STATE_CONNECTING) {
            if (DBG) log("Strange state, closing " + mState + " " + this);
            return;
        }
        if (result >= 0) {
            mState = STATE_CONNECTED;
        } else {
            mState = STATE_CLOSED;
        }
        mHandler.obtainMessage(mConnectedCode, mState, -1, this).sendToTarget();
        releaseWakeLockNow();
    }
    private synchronized void onAccepted(int result) {
        if (VDBG) log("onAccepted() " + this);
        if (mState != STATE_ACCEPT) {
            if (DBG) log("Strange state " + this);
            return;
        }
        if (result >= 0) {
            mState = STATE_CONNECTED;
        } else {
            mState = STATE_CLOSED;
        }
        mHandler.obtainMessage(mAcceptedCode, mState, -1, this).sendToTarget();
    }
    private synchronized void onClosed() {
        if (DBG) log("onClosed() " + this);
        if (mState != STATE_CLOSED) {
            mState = STATE_CLOSED;
            mHandler.obtainMessage(mClosedCode, mState, -1, this).sendToTarget();
            releaseWakeLock();
        }
    }
    private void acquireWakeLock() {
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            if (VDBG) log("mWakeLock.acquire() " + this);
        }
    }
    private void releaseWakeLock() {
        if (mWakeLock.isHeld()) {
            if (VDBG) log("mWakeLock.release() in 2 sec" + this);
            mWakeLock.acquire(2000);
        }
    }
    private void releaseWakeLockNow() {
        if (mWakeLock.isHeld()) {
            if (VDBG) log("mWakeLock.release() now" + this);
            mWakeLock.release();
        }
    }
    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
