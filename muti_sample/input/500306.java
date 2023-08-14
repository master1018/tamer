abstract class WebSyncManager implements Runnable {
    private static final int SYNC_MESSAGE = 101;
    private static int SYNC_NOW_INTERVAL = 100; 
    private static int SYNC_LATER_INTERVAL = 5 * 60 * 1000; 
    private Thread mSyncThread;
    private String mThreadName;
    protected Handler mHandler;
    protected WebViewDatabase mDataBase;
    private int mStartSyncRefCount;
    protected static final String LOGTAG = "websync";
    private class SyncHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SYNC_MESSAGE) {
                if (DebugFlags.WEB_SYNC_MANAGER) {
                    Log.v(LOGTAG, "*** WebSyncManager sync ***");
                }
                syncFromRamToFlash();
                Message newmsg = obtainMessage(SYNC_MESSAGE);
                sendMessageDelayed(newmsg, SYNC_LATER_INTERVAL);
            }
        }
    }
    protected WebSyncManager(Context context, String name) {
        mThreadName = name;
        if (context != null) {
            mDataBase = WebViewDatabase.getInstance(context);
            mSyncThread = new Thread(this);
            mSyncThread.setName(mThreadName);
            mSyncThread.start();
        } else {
            throw new IllegalStateException(
                    "WebSyncManager can't be created without context");
        }
    }
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("doesn't implement Cloneable");
    }
    public void run() {
        Looper.prepare();
        mHandler = new SyncHandler();
        onSyncInit();
       Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Message msg = mHandler.obtainMessage(SYNC_MESSAGE);
        mHandler.sendMessageDelayed(msg, SYNC_LATER_INTERVAL);
        Looper.loop();
    }
    public void sync() {
        if (DebugFlags.WEB_SYNC_MANAGER) {
            Log.v(LOGTAG, "*** WebSyncManager sync ***");
        }
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(SYNC_MESSAGE);
        Message msg = mHandler.obtainMessage(SYNC_MESSAGE);
        mHandler.sendMessageDelayed(msg, SYNC_NOW_INTERVAL);
    }
    public void resetSync() {
        if (DebugFlags.WEB_SYNC_MANAGER) {
            Log.v(LOGTAG, "*** WebSyncManager resetSync ***");
        }
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(SYNC_MESSAGE);
        Message msg = mHandler.obtainMessage(SYNC_MESSAGE);
        mHandler.sendMessageDelayed(msg, SYNC_LATER_INTERVAL);
    }
    public void startSync() {
        if (DebugFlags.WEB_SYNC_MANAGER) {
            Log.v(LOGTAG, "***  WebSyncManager startSync ***, Ref count:" + 
                    mStartSyncRefCount);
        }
        if (mHandler == null) {
            return;
        }
        if (++mStartSyncRefCount == 1) {
            Message msg = mHandler.obtainMessage(SYNC_MESSAGE);
            mHandler.sendMessageDelayed(msg, SYNC_LATER_INTERVAL);
        }
    }
    public void stopSync() {
        if (DebugFlags.WEB_SYNC_MANAGER) {
            Log.v(LOGTAG, "*** WebSyncManager stopSync ***, Ref count:" + 
                    mStartSyncRefCount);
        }
        if (mHandler == null) {
            return;
        }
        if (--mStartSyncRefCount == 0) {
            mHandler.removeMessages(SYNC_MESSAGE);
        }
    }
    protected void onSyncInit() {
    }
    abstract void syncFromRamToFlash();
}
