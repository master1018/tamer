public final class ThreadInfo implements IStackTraceInfo {
    private int mThreadId;
    private String mThreadName;
    private int mStatus;
    private int mTid;
    private int mUtime;
    private int mStime;
    private boolean mIsDaemon;
    private StackTraceElement[] mTrace;
    private long mTraceTime;
    ThreadInfo(int threadId, String threadName) {
        mThreadId = threadId;
        mThreadName = threadName;
        mStatus = -1;
    }
    void updateThread(int status, int tid, int utime, int stime, boolean isDaemon) {
        mStatus = status;
        mTid = tid;
        mUtime = utime;
        mStime = stime;
        mIsDaemon = isDaemon;
    }
    void setStackCall(StackTraceElement[] trace) {
        mTrace = trace;
        mTraceTime = System.currentTimeMillis();
    }
    public int getThreadId() {
        return mThreadId;
    }
    public String getThreadName() {
        return mThreadName;
    }
    void setThreadName(String name) {
        mThreadName = name;
    }
    public int getTid() {
        return mTid;
    }
    public int getStatus() {
        return mStatus;
    }
    public int getUtime() {
        return mUtime;
    }
    public int getStime() {
        return mStime;
    }
    public boolean isDaemon() {
        return mIsDaemon;
    }
    public StackTraceElement[] getStackTrace() {
        return mTrace;
    }
    public long getStackCallTime() {
        return mTraceTime;
    }
}
