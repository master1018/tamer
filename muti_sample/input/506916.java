class Timer {
    private long mStart;
    private long mLast;
    public Timer() {
        mStart = mLast = SystemClock.uptimeMillis();
    }
    public void mark(String message) {
        long now = SystemClock.uptimeMillis();
        if (HttpLog.LOGV) {
            HttpLog.v(message + " " + (now - mLast) + " total " + (now - mStart));
        }
        mLast = now;
    }
}
