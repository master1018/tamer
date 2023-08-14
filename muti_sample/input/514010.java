public abstract class TimerTask implements Runnable {
    final Object lock = new Object();
    boolean cancelled;
    long when;
    long period;
    boolean fixedRate;
    private long scheduledTime;
    long getWhen() {
        synchronized (lock) {
            return when;
        }
    }
    void setScheduledTime(long time) {
        synchronized (lock) {
            scheduledTime = time;
        }
    }
    boolean isScheduled() {
        synchronized (lock) {
            return when > 0 || scheduledTime > 0;
        }
    }
    protected TimerTask() {
        super();
    }
    public boolean cancel() {
        synchronized (lock) {
            boolean willRun = !cancelled && when > 0;
            cancelled = true;
            return willRun;
        }
    }
    public long scheduledExecutionTime() {
        synchronized (lock) {
            return scheduledTime;
        }
    }
    public abstract void run();
}
