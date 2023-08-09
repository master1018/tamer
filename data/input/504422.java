public class TransactionState {
    public static final int INITIALIZED = 0;
    public static final int SUCCESS = 1;
    public static final int FAILED  = 2;
    private Uri mContentUri;
    private int mState;
    public TransactionState() {
        mState = INITIALIZED;
        mContentUri = null;
    }
    public synchronized int getState() {
        return mState;
    }
    synchronized void setState(int state) {
        if ((state < INITIALIZED) && (state > FAILED)) {
            throw new IllegalArgumentException("Bad state: " + state);
        }
        mState = state;
    }
    public synchronized Uri getContentUri() {
        return mContentUri;
    }
    synchronized void setContentUri(Uri uri) {
        mContentUri = uri;
    }
}
