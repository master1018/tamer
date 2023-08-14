public abstract class HandlerTester extends Thread {
    public abstract void go();
    public abstract void handleMessage(Message msg);
    public HandlerTester() {
    }
    public void doTest(long timeout) {
        start();
        synchronized (this) {
            try {
                wait(timeout);
                quit();
            }
            catch (InterruptedException e) {
            }
        }
        if (!mDone) {
            throw new RuntimeException("test timed out");
        }
        if (!mSuccess) {
            throw new RuntimeException("test failed");
        }
    }
    public void success() {
        mDone = true;
        mSuccess = true;
    }
    public void failure() {
        mDone = true;
        mSuccess = false;
    }
    public void run() {
        Looper.prepare();
        mLooper = Looper.myLooper();
        go();
        Looper.loop();
    }
    protected class H extends Handler {
        public void handleMessage(Message msg) {
            synchronized (HandlerTester.this) {
                HandlerTester.this.handleMessage(msg);
                if (mDone) {
                    HandlerTester.this.notify();
                    quit();
                }
            }
        }
    }
    private void quit() {
        mLooper.quit();
    }
    private boolean mDone = false;
    private boolean mSuccess = false;
    private Looper mLooper;
}
