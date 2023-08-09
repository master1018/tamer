public class BarrierConsumer<A> implements Consumer<A> {
    private final Lock mLock = new ReentrantLock();
    private final Condition mNotFull = mLock.newCondition();
    private final int mExpectedCount;
    private ArrayList<A> mValues;
    public BarrierConsumer(int expectedCount) {
        mExpectedCount = expectedCount;
        mValues = new ArrayList<A>(expectedCount);
    }
    public ArrayList<A> getValues() {
        mLock.lock();
        try {
            try {
                while (!isFull()) {
                    mNotFull.await();
                }
            } catch (InterruptedException ex) {
            }
            ArrayList<A> values = mValues;
            mValues = null;  
            return values;
        } finally {
            mLock.unlock();
        }
    }
    public boolean consume(A value) {
        mLock.lock();
        try {
            if (mValues == null || isFull()) {
                return false;
            }
            mValues.add(value);
            if (isFull()) {
                mNotFull.signal();
            }
            return true;
        } finally {
            mLock.unlock();
        }
    }
    private boolean isFull() {
        return mValues.size() == mExpectedCount;
    }
}
