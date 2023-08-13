public final class HeadsetBase {
    private static final String TAG = "Bluetooth HeadsetBase";
    private static final boolean DBG = false;
    public static final int RFCOMM_DISCONNECTED = 1;
    public static final int DIRECTION_INCOMING = 1;
    public static final int DIRECTION_OUTGOING = 2;
    private static int sAtInputCount = 0;  
    private final BluetoothAdapter mAdapter;
    private final BluetoothDevice mRemoteDevice;
    private final String mAddress;  
    private final int mRfcommChannel;
    private int mNativeData;
    private Thread mEventThread;
    private volatile boolean mEventThreadInterrupted;
    private Handler mEventThreadHandler;
    private int mTimeoutRemainingMs;
    private final int mDirection;
    private final long mConnectTimestamp;
    protected AtParser mAtParser;
    private WakeLock mWakeLock;  
    private native static void classInitNative();
    static {
        classInitNative();
    }
    protected void finalize() throws Throwable {
        try {
            cleanupNativeDataNative();
            releaseWakeLock();
        } finally {
            super.finalize();
        }
    }
    private native void cleanupNativeDataNative();
    public HeadsetBase(PowerManager pm, BluetoothAdapter adapter, BluetoothDevice device,
            int rfcommChannel) {
        mDirection = DIRECTION_OUTGOING;
        mConnectTimestamp = System.currentTimeMillis();
        mAdapter = adapter;
        mRemoteDevice = device;
        mAddress = device.getAddress();
        mRfcommChannel = rfcommChannel;
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeadsetBase");
        mWakeLock.setReferenceCounted(false);
        initializeAtParser();
        initializeNativeDataNative(-1);
    }
    public HeadsetBase(PowerManager pm, BluetoothAdapter adapter, BluetoothDevice device,
            int socketFd, int rfcommChannel, Handler handler) {
        mDirection = DIRECTION_INCOMING;
        mConnectTimestamp = System.currentTimeMillis();
        mAdapter = adapter;
        mRemoteDevice = device;
        mAddress = device.getAddress();
        mRfcommChannel = rfcommChannel;
        mEventThreadHandler = handler;
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeadsetBase");
        mWakeLock.setReferenceCounted(false);
        initializeAtParser();
        initializeNativeDataNative(socketFd);
    }
    private native void initializeNativeDataNative(int socketFd);
    protected void handleInput(String input) {
        acquireWakeLock();
        long timestamp;
        synchronized(HeadsetBase.class) {
            if (sAtInputCount == Integer.MAX_VALUE) {
                sAtInputCount = 0;
            } else {
                sAtInputCount++;
            }
        }
        if (DBG) timestamp = System.currentTimeMillis();
        AtCommandResult result = mAtParser.process(input);
        if (DBG) Log.d(TAG, "Processing " + input + " took " +
                       (System.currentTimeMillis() - timestamp) + " ms");
        if (result.getResultCode() == AtCommandResult.ERROR) {
            Log.i(TAG, "Error pocessing <" + input + ">");
        }
        sendURC(result.toString());
        releaseWakeLock();
    }
    protected void initializeAtParser() {
        mAtParser = new AtParser();
    }
    public AtParser getAtParser() {
        return mAtParser;
    }
    public void startEventThread() {
        mEventThread =
            new Thread("HeadsetBase Event Thread") {
                public void run() {
                    int last_read_error;
                    while (!mEventThreadInterrupted) {
                        String input = readNative(500);
                        if (input != null) {
                            handleInput(input);
                        }
                        else {
                            last_read_error = getLastReadStatusNative();
                            if (last_read_error != 0) {
                                Log.i(TAG, "headset read error " + last_read_error);
                                if (mEventThreadHandler != null) {
                                    mEventThreadHandler.obtainMessage(RFCOMM_DISCONNECTED)
                                            .sendToTarget();
                                }
                                disconnectNative();
                                break;
                            }
                        }
                    }
                }
            };
        mEventThreadInterrupted = false;
        mEventThread.start();
    }
    private native String readNative(int timeout_ms);
    private native int getLastReadStatusNative();
    private void stopEventThread() {
        mEventThreadInterrupted = true;
        mEventThread.interrupt();
        try {
            mEventThread.join();
        } catch (java.lang.InterruptedException e) {
        }
        mEventThread = null;
    }
    public boolean connect(Handler handler) {
        if (mEventThread == null) {
            if (!connectNative()) return false;
            mEventThreadHandler = handler;
        }
        return true;
    }
    private native boolean connectNative();
    public boolean connectAsync() {
        int ret = connectAsyncNative();
        return (ret == 0) ? true : false;
    }
    private native int connectAsyncNative();
    public int getRemainingAsyncConnectWaitingTimeMs() {
        return mTimeoutRemainingMs;
    }
    public int waitForAsyncConnect(int timeout_ms, Handler handler) {
        int res = waitForAsyncConnectNative(timeout_ms);
        if (res > 0) {
            mEventThreadHandler = handler;
        }
        return res;
    }
    private native int waitForAsyncConnectNative(int timeout_ms);
    public void disconnect() {
        if (mEventThread != null) {
            stopEventThread();
        }
        disconnectNative();
    }
    private native void disconnectNative();
    public boolean isConnected() {
        return mEventThread != null;
    }
    public BluetoothDevice getRemoteDevice() {
        return mRemoteDevice;
    }
    public int getDirection() {
        return mDirection;
    }
    public long getConnectTimestamp() {
        return mConnectTimestamp;
    }
    public synchronized boolean sendURC(String urc) {
        if (urc.length() > 0) {
            boolean ret = sendURCNative(urc);
            return ret;
        }
        return true;
    }
    private native boolean sendURCNative(String urc);
    private synchronized void acquireWakeLock() {
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }
    private synchronized void releaseWakeLock() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
    public static int getAtInputCount() {
        return sAtInputCount;
    }
    private static void log(String msg) {
        Log.d(TAG, msg);
    }
}
