public class MonkeyWaitEvent extends MonkeyEvent {
    private long mWaitTime;
    public MonkeyWaitEvent(long waitTime) {
        super(MonkeyEvent.EVENT_TYPE_THROTTLE);
        mWaitTime = waitTime;
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 1) {
            System.out.println("Wait Event for " + mWaitTime + " milliseconds");
        }
        try {
            Thread.sleep(mWaitTime);
        } catch (InterruptedException e1) {
            System.out.println("** Monkey interrupted in sleep.");
            return MonkeyEvent.INJECT_FAIL;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
