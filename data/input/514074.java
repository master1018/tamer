public class MonkeyEventQueue extends LinkedList<MonkeyEvent> {
    private Random mRandom;
    private long mThrottle;
    private boolean mRandomizeThrottle;
    public MonkeyEventQueue(Random random, long throttle, boolean randomizeThrottle) {
        super();
        mRandom = random;
        mThrottle = throttle;
        mRandomizeThrottle = randomizeThrottle;
    }
    @Override
    public void addLast(MonkeyEvent e) {
        super.add(e);
        if (e.isThrottlable()) {
            long throttle = mThrottle;
            if (mRandomizeThrottle && (mThrottle > 0)) {
                throttle = mRandom.nextLong();
                if (throttle < 0) {
                    throttle = -throttle;
                }
                throttle %= mThrottle;
                ++throttle;
            }
            super.add(new MonkeyThrottleEvent(throttle));
        }
    }
}
