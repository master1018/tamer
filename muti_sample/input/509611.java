public abstract class AbstractRetryScheme {
    public static final int OUTGOING = 1;
    public static final int INCOMING = 2;
    protected int mRetriedTimes;
    public AbstractRetryScheme(int retriedTimes) {
        mRetriedTimes = retriedTimes;
    }
    abstract public int getRetryLimit();
    abstract public long getWaitingInterval();
}
