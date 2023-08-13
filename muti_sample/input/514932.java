public final class TestThread extends Thread {
    private Throwable mThrowable;
    private Runnable mTarget;
    public TestThread(Runnable target) {
        mTarget = target;
    }
    @Override
    public final void run() {
        try {
            mTarget.run();
        } catch (Throwable t) {
            mThrowable = t;
        }
    }
    public void runTest(long runTime) throws Throwable {
        start();
        joinAndCheck(runTime);
    }
    public Throwable getThrowable() {
        return mThrowable;
    }
    public void setThrowable(Throwable t) {
        mThrowable = t;
    }
    public void joinAndCheck(long runTime) throws Throwable {
        this.join(runTime);
        if (this.isAlive()) {
            this.interrupt();
            this.join(runTime);
            throw new Exception("Thread did not finish within allotted time.");
        }
        checkException();
    }
    public void checkException() throws Throwable {
        if (mThrowable != null) {
            throw mThrowable;
        }
    }
}
