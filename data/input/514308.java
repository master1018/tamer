public class ConditionVariable
{
    private volatile boolean mCondition;
    public ConditionVariable()
    {
        mCondition = false;
    }
    public ConditionVariable(boolean state)
    {
        mCondition = state;
    }
    public void open()
    {
        synchronized (this) {
            boolean old = mCondition;
            mCondition = true;
            if (!old) {
                this.notifyAll();
            }
        }
    }
    public void close()
    {
        synchronized (this) {
            mCondition = false;
        }
    }
    public void block()
    {
        synchronized (this) {
            while (!mCondition) {
                try {
                    this.wait();
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
    public boolean block(long timeout)
    {
        if (timeout != 0) {
            synchronized (this) {
                long now = System.currentTimeMillis();
                long end = now + timeout;
                while (!mCondition && now < end) {
                    try {
                        this.wait(end-now);
                    }
                    catch (InterruptedException e) {
                    }
                    now = System.currentTimeMillis();
                }
                return mCondition;
            }
        } else {
            this.block();
            return true;
        }
    }
}
