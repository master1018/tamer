@TestTargetClass(AsyncTask.class)
public class AsyncTaskTest extends InstrumentationTestCase {
    private static final long COMPUTE_TIME = 1000;
    private static final long RESULT = 1000;
    private static final Integer[] UPDATE_VALUE = { 0, 1, 2 };
    private static final long DURATION = 2000;
    private static final String[] PARAM = { "Test" };
    private static MyAsyncTask mAsyncTask;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "AsyncTask",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "execute",
            args = {Object[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "get",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "doInBackground",
            args = {Object[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPreExecute",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStatus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onProgressUpdate",
            args = {Object[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onPostExecute",
            args = {Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "publishProgress",
            args = {Object[].class}
        )
    })
    public void testAsyncTask() throws Throwable {
        doTestAsyncTask(0);
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                method = "get",
                args = {long.class, TimeUnit.class}
            )
    })
    public void testAsyncTaskWithTimeout() throws Throwable {
        doTestAsyncTask(DURATION);
    }
    private void doTestAsyncTask(final long timeout) throws Throwable {
        startAsyncTask();
        if (timeout > 0) {
            assertEquals(RESULT, mAsyncTask.get(DURATION, TimeUnit.MILLISECONDS).longValue());
        } else {
            assertEquals(RESULT, mAsyncTask.get().longValue());
        }
        new DelayedCheck(DURATION) {
            protected boolean check() {
                return mAsyncTask.getStatus() == AsyncTask.Status.FINISHED;
            }
        }.run();
        assertTrue(mAsyncTask.isOnPreExecuteCalled);
        assert(mAsyncTask.hasRun);
        assertEquals(PARAM.length, mAsyncTask.parameters.length);
        for (int i = 0; i < PARAM.length; i++) {
            assertEquals(PARAM[i], mAsyncTask.parameters[i]);
        }
        assertEquals(RESULT, mAsyncTask.postResult.longValue());
        assertEquals(AsyncTask.Status.FINISHED, mAsyncTask.getStatus());
        if (mAsyncTask.exception != null) {
            throw mAsyncTask.exception;
        }
        new DelayedCheck(DURATION) {
            protected boolean check() {
                return mAsyncTask.updateValue != null;
            }
        }.run();
        assertEquals(UPDATE_VALUE.length, mAsyncTask.updateValue.length);
        for (int i = 0; i < UPDATE_VALUE.length; i++) {
            assertEquals(UPDATE_VALUE[i], mAsyncTask.updateValue[i]);
        }
        runTestOnUiThread(new Runnable() {
            public void run() {
                try {
                    mAsyncTask.execute(PARAM);
                    fail("Failed to throw exception!");
                } catch (IllegalStateException e) {
                }
            }
        });
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "cancel",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "isCancelled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "onCancelled",
            args = {}
        )
    })
    public void testCancelWithInterrupt() throws Throwable {
        startAsyncTask();
        Thread.sleep(COMPUTE_TIME / 2);
        assertTrue(mAsyncTask.cancel(true));
        assertFalse(mAsyncTask.cancel(true));
        Thread.sleep(DURATION);
        assertTrue(mAsyncTask.isCancelled());
        assertTrue(mAsyncTask.isOnCancelledCalled);
        assertNotNull(mAsyncTask.exception);
        assertTrue(mAsyncTask.exception instanceof InterruptedException);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "cancel",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "isCancelled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "onCancelled",
            args = {}
        )
    })
    public void testCancel() throws Throwable {
        startAsyncTask();
        Thread.sleep(COMPUTE_TIME / 2);
        assertTrue(mAsyncTask.cancel(false));
        assertFalse(mAsyncTask.cancel(false));
        Thread.sleep(DURATION);
        assertTrue(mAsyncTask.isCancelled());
        assertTrue(mAsyncTask.isOnCancelledCalled);
        assertNull(mAsyncTask.exception);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "cancel",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "isCancelled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            method = "onCancelled",
            args = {}
        )
    })
    public void testCancelTooLate() throws Throwable {
        startAsyncTask();
        Thread.sleep(DURATION);
        assertFalse(mAsyncTask.cancel(false));
        assertFalse(mAsyncTask.isCancelled());
        assertFalse(mAsyncTask.isOnCancelledCalled);
        assertNull(mAsyncTask.exception);
    }
    private void startAsyncTask() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mAsyncTask = new MyAsyncTask();
                assertEquals(AsyncTask.Status.PENDING, mAsyncTask.getStatus());
                assertEquals(mAsyncTask, mAsyncTask.execute(PARAM));
                assertEquals(AsyncTask.Status.RUNNING, mAsyncTask.getStatus());
            }
        });
    }
    private static class MyAsyncTask extends AsyncTask<String, Integer, Long> {
        public boolean isOnCancelledCalled;
        public boolean isOnPreExecuteCalled;
        public boolean hasRun;
        public Exception exception;
        public Long postResult;
        public Integer[] updateValue;
        public String[] parameters;
        @Override
        protected Long doInBackground(String... params) {
            hasRun = true;
            parameters = params;
            try {
                publishProgress(UPDATE_VALUE);
                Thread.sleep(COMPUTE_TIME);
            } catch (Exception e) {
                exception = e;
            }
            return RESULT;
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
            isOnCancelledCalled = true;
        }
        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            postResult = result;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isOnPreExecuteCalled = true;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            updateValue = values;
        }
    }
}
