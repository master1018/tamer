public class CTSActivityTestCaseBase extends InstrumentationTestCase implements CTSResult {
    private Sync mSync;
    static class Sync {
        public boolean mHasNotify;
        public int mResult;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSync = new Sync();
    }
    public void setResult(int resultCode) {
        synchronized (mSync) {
            mSync.mHasNotify = true;
            mSync.mResult = resultCode;
            mSync.notify();
        }
    }
    protected void waitForResult() throws InterruptedException {
        synchronized (mSync) {
            while (!mSync.mHasNotify) {
                mSync.wait();
            }
            assertEquals(CTSResult.RESULT_OK, mSync.mResult);
        }
    }
}
