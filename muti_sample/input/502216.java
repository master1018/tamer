abstract class ResettableTimeout
{
    public abstract void on(boolean alreadyOn);
    public abstract void off();
    public void go(long milliseconds)
    {
        synchronized (this) {
            mOffAt = SystemClock.uptimeMillis() + milliseconds;
            boolean alreadyOn;
            if (mThread == null) {
                alreadyOn = false;
                mLock.close();
                mThread = new T();
                mThread.start();
                mLock.block();
                mOffCalled = false;
            } else {
                alreadyOn = true;
                mThread.interrupt();
            }
            on(alreadyOn);
        }
    }
    public void cancel()
    {
        synchronized (this) {
            mOffAt = 0;
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
            if (!mOffCalled) {
                mOffCalled = true;
                off();
            }
        }
    }
    private class T extends Thread
    {
        public void run()
        {
            mLock.open();
            while (true) {
                long diff;
                synchronized (this) {
                    diff = mOffAt - SystemClock.uptimeMillis();
                    if (diff <= 0) {
                        mOffCalled = true;
                        off();
                        mThread = null;
                        break;
                    }
                }
                try {
                    sleep(diff);
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
    private ConditionVariable mLock = new ConditionVariable();
    private volatile long mOffAt;
    private volatile boolean mOffCalled;
    private Thread mThread;
}
