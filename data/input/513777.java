class AppErrorResult {
    public void set(int res) {
        synchronized (this) {
            mHasResult = true;
            mResult = res;
            notifyAll();
        }
    }
    public int get() {
        synchronized (this) {
            while (!mHasResult) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return mResult;
    }
    boolean mHasResult = false;
    int mResult;
}
