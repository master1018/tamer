@TestTargetClass(TokenWatcher.class)
public class TokenWatcherTest extends AndroidTestCase {
    private static final String TAG = "TokenWatcherTest";
    private static final String EMPTY_SERVICE = "android.os.REMOTESERVICE";
    private static final int OVERTIME = 2000;
    private static final int DELAY = 500;
    private MockTokenWatcher mMockTokenWatcher;
    private Handler mHandler;
    private ServiceConnection mServiceConnection;
    private Intent mIntent;
    private IEmptyService mEmptyService;
    private Object mSync;
    private boolean mHasConnected;
    private boolean mHasDisconnected;
    private boolean mLooped;
    private Looper mLooper;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        new Thread() {
            public void run() {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mLooped = true;
                Looper.loop();
            }
        }.start();
        while (!mLooped) {
            Thread.sleep(50);
        }
        mHandler = new Handler(mLooper);
        mSync = new Object();
        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                    IBinder service) {
                mEmptyService = IEmptyService.Stub.asInterface(service);
                synchronized (mSync) {
                    mHasConnected = true;
                    mSync.notify();
                }
            }
            public void onServiceDisconnected(ComponentName className) {
                mEmptyService = null;
                synchronized (mSync) {
                    mHasDisconnected = true;
                    mSync.notify();
                }
            }
        };
        mIntent = new Intent(EMPTY_SERVICE);
        getContext().startService(mIntent);
        getContext().bindService(new Intent(IEmptyService.class.getName()),
                mServiceConnection, Context.BIND_AUTO_CREATE);
        synchronized (mSync) {
            if (!mHasConnected) {
                try {
                    mSync.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mLooper.quit();
        if (!mHasDisconnected) {
            getContext().unbindService(mServiceConnection);
        }
        if (mIntent != null) {
            getContext().stopService(mIntent);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor TokenWatcher(Handler h, String tag) throuth mock class",
            method = "TokenWatcher",
            args = {android.os.Handler.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isAcquired",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "acquire",
            args = {android.os.IBinder.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "acquired",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cleanup",
            args = {android.os.IBinder.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {android.os.IBinder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "released",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dump",
            args = {}
        )
    })
    public void testTokenWatcher() throws RemoteException, InterruptedException {
        IBinder token = mEmptyService.getToken();
        mMockTokenWatcher = new MockTokenWatcher(mHandler, TAG);
        assertFalse(mMockTokenWatcher.isAcquired());
        assertFalse(mMockTokenWatcher.isAcquiredCalled);
        assertFalse(mMockTokenWatcher.isReleasedCalled);
        mMockTokenWatcher.acquire(token, TAG);
        assertTrue(mMockTokenWatcher.isAcquired());
        assertTrue(waitUntilAcquired());
        mMockTokenWatcher.isAcquiredCalled = false;
        mMockTokenWatcher.acquire(token, TAG);
        assertTrue(mMockTokenWatcher.isAcquired());
        assertFalse(waitUntilAcquired());
        synchronized (mMockTokenWatcher) {
            mMockTokenWatcher.release(token);
            assertFalse(mMockTokenWatcher.isAcquired());
        }
        assertTrue(waitUntilReleased());
        mMockTokenWatcher.isReleasedCalled =false;
        mMockTokenWatcher.release(token);
        assertFalse(mMockTokenWatcher.isAcquired());
        assertFalse(waitUntilReleased());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "cleanup",
            args = {android.os.IBinder.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dump",
            args = {}
        )
    })
    public void testCleanUp() throws RemoteException, InterruptedException {
        IBinder token = mEmptyService.getToken();
        mMockTokenWatcher = new MockTokenWatcher(mHandler, TAG);
        assertFalse(mMockTokenWatcher.isAcquired());
        assertFalse(mMockTokenWatcher.isAcquiredCalled);
        assertFalse(mMockTokenWatcher.isReleasedCalled);
        mMockTokenWatcher.acquire(token, TAG);
        assertTrue(mMockTokenWatcher.isAcquired());
        assertTrue(waitUntilAcquired());
        mMockTokenWatcher.dump();
        synchronized (mMockTokenWatcher) {
            mMockTokenWatcher.cleanup(token, true);
            assertFalse(mMockTokenWatcher.isAcquired());
        }
        assertTrue(waitUntilReleased());
    }
    private boolean waitUntilAcquired() throws InterruptedException {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < OVERTIME) {
            if (mMockTokenWatcher.isAcquiredCalled) {
                return true;
            }
            Thread.sleep(DELAY);
        }
        return false;
    }
    private boolean waitUntilReleased() throws InterruptedException {
        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() - time < OVERTIME) {
            if (mMockTokenWatcher.isReleasedCalled) {
                return true;
            }
            Thread.sleep(DELAY);
        }
        return false;
    }
    private static class MockTokenWatcher extends TokenWatcher {
        public boolean isAcquiredCalled;
        public boolean isReleasedCalled;
        public MockTokenWatcher(Handler h, String tag) {
            super(h, tag);
        }
        @Override
        public void acquired() {
            isAcquiredCalled = true;
        }
        @Override
        public synchronized void released() {
            isReleasedCalled = true;
        }
    }
}
