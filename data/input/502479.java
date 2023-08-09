public class HostTimer {
    private final static int INIT = 0;
    private final static int RUNNING = 1;
    private final static int CANCELLED = 2;
    private final static int TIMEOUT = 3;
    private boolean mIsNotified;
    private int mStatus;
    private int mDelay;
    private TimerTask mTimerTask;
    private Timer mTimer;
    public HostTimer(TimerTask task, int delay) {
        mDelay = delay;
        mTimerTask = task;
        mStatus = INIT;
        mIsNotified = false;
        mTimer = null;
    }
    public void setNotified() {
        mIsNotified = true;
    }
    public boolean isNotified() {
        return mIsNotified;
    }
    public void resetNotified() {
        mIsNotified = false;
    }
    public void waitOn() throws InterruptedException {
        Log.d("HostTimer.waitOn(): mIsNotified=" + mIsNotified + ", this=" + this);
        if (!mIsNotified) {
            wait();
        }
        mIsNotified = false;
    }
    public void setDelay(int delay) {
        mDelay = delay;
    }
    public void setTimerTask(TimerTask task) {
        mTimerTask = task;
    }
    public boolean isTimeOut() {
        return (mStatus == TIMEOUT);
    }
    public void start() {
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, mDelay);
        mStatus = RUNNING;
    }
    public void restart(TimerTask task, int delay) {
        mTimer.cancel();
        mTimerTask = task;
        mDelay = delay;
        start();
    }
    public void sendNotify() {
        Log.d("HostTimer.sendNotify(): mIsNotified=" + mIsNotified + ", this=" + this);
        mIsNotified = true;
        notify();
    }
    public void cancel(boolean timeout) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mStatus == RUNNING) {
            if (timeout) {
                mStatus = TIMEOUT;
            } else {
                mStatus = CANCELLED;
            }
        }
    }
}
