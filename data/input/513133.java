@TestTargetClass(IntentService.class)
public class IntentServiceTest extends ActivityTestsBase {
    private Intent mIntent;
    private static final int TIMEOUT_MSEC = 5000;
    private boolean mConnected;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IntentServiceStub.reset();
        mIntent = new Intent(mContext, IntentServiceStub.class);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (!IntentServiceStub.onDestroyCalled) {
            mContext.stopService(mIntent);
        }
    }
    public void testIntents() throws Throwable {
        final int value = 42;
        final int adds = 3;
        Intent addIntent = new Intent(mContext, IntentServiceStub.class);
        addIntent.setAction(IntentServiceStub.ISS_ADD);
        addIntent.putExtra(IntentServiceStub.ISS_VALUE, 42);
        for (int i = 0; i < adds; i++) {
            mContext.startService(addIntent);
        }
        IntentServiceStub.waitToFinish(TIMEOUT_MSEC);
        assertEquals(adds, IntentServiceStub.onHandleIntentCalled);
        assertEquals(adds * value, IntentServiceStub.accumulator);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onStart",
            args = {android.content.Intent.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onDestroy",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onHandleIntent",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onBind",
            args = {Intent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCreate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "IntentService",
            args = {String.class}
        )
    })
    public void testIntentServiceLifeCycle() throws Throwable {
        mContext.startService(mIntent);
        new DelayedCheck(TIMEOUT_MSEC) {
            protected boolean check() {
                return IntentServiceStub.onHandleIntentCalled > 0;
            }
        }.run();
        assertTrue(IntentServiceStub.onCreateCalled);
        assertTrue(IntentServiceStub.onStartCalled);
        ServiceConnection conn = new TestConnection();
        mContext.bindService(mIntent, conn, Context.BIND_AUTO_CREATE);
        new DelayedCheck(TIMEOUT_MSEC) {
            protected boolean check() {
                return mConnected;
            }
        }.run();
        assertTrue(IntentServiceStub.onBindCalled);
        mContext.unbindService(conn);
        mContext.stopService(mIntent);
        IntentServiceStub.waitToFinish(TIMEOUT_MSEC);
    }
    private class TestConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            mConnected = true;
        }
        public void onServiceDisconnected(ComponentName name) {
        }
    }
}
