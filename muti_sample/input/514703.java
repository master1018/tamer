public abstract class DelayedCheck {
    private static final long TIME_SLICE = 200;
    private long mTimeout = 3000;
    public DelayedCheck() {
    }
    public DelayedCheck(long timeout) {
        mTimeout = timeout;
    }
    protected abstract boolean check();
    public void run() {
        long timeout = mTimeout;
        while (timeout > 0) {
            try {
                Thread.sleep(TIME_SLICE);
            } catch (InterruptedException e) {
                Assert.fail("unexpected InterruptedException");
            }
            if (check()) {
                return;
            }
            timeout -= TIME_SLICE;
        }
        Assert.fail("unexpected timeout");
    }
}
