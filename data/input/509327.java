public class MonkeyThrottleEvent extends MonkeyEvent {
    private long mThrottle; 
    public MonkeyThrottleEvent(long throttle) {
        super(MonkeyEvent.EVENT_TYPE_THROTTLE);
        mThrottle = throttle;
    }  
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 1) {
            System.out.println("Sleeping for " + mThrottle + " milliseconds");
        }
        try {
            Thread.sleep(mThrottle);
        } catch (InterruptedException e1) {
            System.out.println("** Monkey interrupted in sleep.");
            return MonkeyEvent.INJECT_FAIL;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
