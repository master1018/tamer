public abstract class BaseCancelable<T> implements Cancelable<T> {
    private static final int STATE_INITIAL = (1 << 0);
    private static final int STATE_EXECUTING = (1 << 1);
    private static final int STATE_CANCELING = (1 << 2);
    private static final int STATE_CANCELED = (1 << 3);
    private static final int STATE_ERROR = (1 << 4);
    private static final int STATE_COMPLETE = (1 << 5);
    private int mState = STATE_INITIAL;
    private Throwable mError;
    private T mResult;
    private Cancelable<?> mCurrentTask;
    protected abstract T execute() throws Exception;
    protected void freeCanceledResult(T result) {
    }
    private boolean isInStates(int states) {
        return (states & mState) != 0;
    }
    private T handleTerminalStates() throws ExecutionException {
        if (mState == STATE_CANCELED) {
            throw new CancellationException();
        }
        if (mState == STATE_ERROR) {
            throw new ExecutionException(mError);
        }
        if (mState == STATE_COMPLETE)
            return mResult;
        throw new IllegalStateException();
    }
    public synchronized void await() throws InterruptedException {
        while (!isInStates(STATE_COMPLETE | STATE_CANCELED | STATE_ERROR)) {
            wait();
        }
    }
    public final T get() throws InterruptedException, ExecutionException {
        synchronized (this) {
            if (mState != STATE_INITIAL) {
                await();
                return handleTerminalStates();
            }
            mState = STATE_EXECUTING;
        }
        try {
            mResult = execute();
        } catch (CancellationException e) {
            mState = STATE_CANCELED;
        } catch (InterruptedException e) {
            mState = STATE_CANCELED;
        } catch (Throwable error) {
            synchronized (this) {
                if (mState != STATE_CANCELING) {
                    mError = error;
                    mState = STATE_ERROR;
                }
            }
        }
        synchronized (this) {
            if (mState == STATE_CANCELING)
                mState = STATE_CANCELED;
            if (mState == STATE_EXECUTING)
                mState = STATE_COMPLETE;
            notifyAll();
            if (mState == STATE_CANCELED && mResult != null) {
                freeCanceledResult(mResult);
            }
            return handleTerminalStates();
        }
    }
    public synchronized boolean requestCancel() {
        if (mState == STATE_INITIAL) {
            mState = STATE_CANCELED;
            notifyAll();
            return false;
        }
        if (mState == STATE_EXECUTING) {
            if (mCurrentTask != null)
                mCurrentTask.requestCancel();
            mState = STATE_CANCELING;
            return true;
        }
        return false;
    }
}
