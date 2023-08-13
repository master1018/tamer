class ConnectionThread extends Thread {
    static final int WAIT_TIMEOUT = 5000;
    static final int WAIT_TICK = 1000;
    long mCurrentThreadTime;
    long mTotalThreadTime;
    private boolean mWaiting;
    private volatile boolean mRunning = true;
    private Context mContext;
    private RequestQueue.ConnectionManager mConnectionManager;
    private RequestFeeder mRequestFeeder;
    private int mId;
    Connection mConnection;
    ConnectionThread(Context context,
                     int id,
                     RequestQueue.ConnectionManager connectionManager,
                     RequestFeeder requestFeeder) {
        super();
        mContext = context;
        setName("http" + id);
        mId = id;
        mConnectionManager = connectionManager;
        mRequestFeeder = requestFeeder;
    }
    void requestStop() {
        synchronized (mRequestFeeder) {
            mRunning = false;
            mRequestFeeder.notify();
        }
    }
    public void run() {
        android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_DEFAULT +
                android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
        mCurrentThreadTime = 0;
        mTotalThreadTime = 0;
        while (mRunning) {
            if (mCurrentThreadTime == -1) {
                mCurrentThreadTime = SystemClock.currentThreadTimeMillis();
            }
            Request request;
            request = mRequestFeeder.getRequest();
            if (request == null) {
                synchronized(mRequestFeeder) {
                    if (HttpLog.LOGV) HttpLog.v("ConnectionThread: Waiting for work");
                    mWaiting = true;
                    try {
                        mRequestFeeder.wait();
                    } catch (InterruptedException e) {
                    }
                    mWaiting = false;
                    if (mCurrentThreadTime != 0) {
                        mCurrentThreadTime = SystemClock
                                .currentThreadTimeMillis();
                    }
                }
            } else {
                if (HttpLog.LOGV) HttpLog.v("ConnectionThread: new request " +
                                            request.mHost + " " + request );
                mConnection = mConnectionManager.getConnection(mContext,
                        request.mHost);
                mConnection.processRequests(request);
                if (mConnection.getCanPersist()) {
                    if (!mConnectionManager.recycleConnection(mConnection)) {
                        mConnection.closeConnection();
                    }
                } else {
                    mConnection.closeConnection();
                }
                mConnection = null;
                if (mCurrentThreadTime > 0) {
                    long start = mCurrentThreadTime;
                    mCurrentThreadTime = SystemClock.currentThreadTimeMillis();
                    mTotalThreadTime += mCurrentThreadTime - start;
                }
            }
        }
    }
    public synchronized String toString() {
        String con = mConnection == null ? "" : mConnection.toString();
        String active = mWaiting ? "w" : "a";
        return "cid " + mId + " " + active + " "  + con;
    }
}
