public class SearchManagerStubActivity extends Activity {
    private static final String TAG = "SearchManagerStubActivity";
    public static final String TEST_STOP_SEARCH = "stopSearch";
    public static final String TEST_ON_DISMISSLISTENER = "setOnDismissListener";
    public static final String TEST_ON_CANCELLISTENER = "setOnCancelListener";
    private SearchManager mSearchManager;
    private ComponentName mComponentName;
    private static CTSResult sCTSResult;
    private boolean mDismissCalled;
    private boolean mCancelCalled;
    public static void setCTSResult(CTSResult result) {
        sCTSResult = result;
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSearchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mComponentName = getComponentName();
    }
    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        if (action.equals(TEST_STOP_SEARCH)) {
            testStopSearch();
        } else if (action.equals(TEST_ON_DISMISSLISTENER)) {
            testOnDismissListener();
        } else if (action.equals(TEST_ON_CANCELLISTENER)) {
            testOnCancelListener();
        }
    }
    private void testOnCancelListener() {
        mCancelCalled = false;
        mSearchManager.setOnCancelListener(new SearchManager.OnCancelListener() {
            public void onCancel() {
               mCancelCalled = true;
            }
        });
        new TestStepHandler() {
            @Override
            public boolean doStep(int step) throws FailException {
                switch (step) {
                    case 1:
                        startSearch("test", false, mComponentName, null, false);
                        return false;
                    case 2:
                        assertFalse("cancel called", mCancelCalled);
                        stopSearch();
                        return false;
                    case 3:
                        assertTrue("cancel not called", mCancelCalled);
                        return true;
                    default:
                        throw new IllegalArgumentException("Bad step " + step);
                }
            }
        }.start();
    }
    private void testOnDismissListener() {
        mDismissCalled = false;
        mSearchManager.setOnDismissListener(new SearchManager.OnDismissListener() {
            public void onDismiss() {
                mDismissCalled = true;
            }
        });
        new TestStepHandler() {
            @Override
            public boolean doStep(int step) throws FailException {
                switch (step) {
                    case 1:
                        startSearch("test", false, mComponentName, null, false);
                        return false;
                    case 2:
                        if (mDismissCalled) {
                            throw new FailException("dismiss called");
                        } else {
                            stopSearch();
                        }
                        return false;
                    case 3:
                        if (mDismissCalled) {
                            pass();
                        } else {
                            throw new FailException("dismiss not called");
                        }
                        return true;
                    default:
                        throw new IllegalArgumentException("Bad step " + step);
                }
            }
        }.start();
    }
    private void testStopSearch() {
        new TestStepHandler() {
            @Override
            public boolean doStep(int step) throws FailException {
                switch (step) {
                    case 1:
                        startSearch("test", false, mComponentName, null, false);
                        return false;
                    case 2:
                        assertVisible();
                        stopSearch();
                        return false;
                    case 3:
                        assertInVisible();
                        pass();
                        return true;
                    default:
                        throw new IllegalArgumentException("Bad step " + step);
                }
            }
        }.start();
    }
    private void fail(Exception ex) {
        Log.e(TAG, "test failed", ex);
        sCTSResult.setResult(CTSResult.RESULT_FAIL);
        finish();
    }
    private void pass() {
        sCTSResult.setResult(CTSResult.RESULT_OK);
        finish();
    }
    private void assertInVisible() throws FailException {
        if (isVisible()) {
            throw new FailException();
        }
    }
    private void assertVisible() throws FailException {
        if (!isVisible()) {
            throw new FailException();
        }
    }
    private void assertFalse(String message, boolean value) throws FailException {
        assertTrue(message, !value);
    }
    private void assertTrue(String message, boolean value) throws FailException {
        if (!value) {
            throw new FailException(message);
        }
    }
    private void startSearch(String initialQuery, boolean selectInitialQuery,
            ComponentName launchActivity, Bundle appSearchData, boolean globalSearch) {
        mSearchManager.startSearch(initialQuery, selectInitialQuery, launchActivity, appSearchData,
                globalSearch);
    }
    private void stopSearch() {
       mSearchManager.stopSearch();
    }
    private boolean isVisible() {
        return mSearchManager.isVisible();
    }
    private abstract class TestStepHandler extends Handler {
        public void start() {
            sendEmptyMessage(1);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                if (doStep(msg.what)) {
                    pass();
                } else {
                    sendEmptyMessage(msg.what + 1);
                }
            } catch (FailException ex) {
                fail(ex);
            }
        }
        protected abstract boolean doStep(int step) throws FailException;
    }
    private static class FailException extends Exception {
        private static final long serialVersionUID = 1L;
        public FailException() {
            super();
        }
        public FailException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
        public FailException(String detailMessage) {
            super(detailMessage);
        }
        public FailException(Throwable throwable) {
            super(throwable);
        }
    }
}
