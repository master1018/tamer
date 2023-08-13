public class LatencyTracker {
    private long mStartTime;
    public LatencyTracker() {
        mStartTime = SystemClock.uptimeMillis();
    }
    public void reset() {
        mStartTime = SystemClock.uptimeMillis();
    }
    public int getLatency() {
        long now = SystemClock.uptimeMillis();
        return (int) (now - mStartTime);
    }
}
