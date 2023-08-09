@TestTargetClass(RemoteCallbackList.class)
public class RemoteCallbackListTest extends AndroidTestCase {
    private static final String SERVICE_ACTION = "android.app.REMOTESERVICE";
    private ISecondary mSecondaryService = null;
    private Sync mSync = new Sync();
    private Intent mIntent;
    private Context mContext;
    private ServiceConnection mSecondaryConnection;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mSecondaryConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                mSecondaryService = ISecondary.Stub.asInterface(service);
                synchronized (mSync) {
                    mSync.mIsConnected = true;
                    mSync.notify();
                }
            }
            public void onServiceDisconnected(ComponentName className) {
                mSecondaryService = null;
                synchronized (mSync) {
                    mSync.mIsDisConnected = true;
                    mSync.notify();
                }
            }
        };
        mIntent = new Intent(SERVICE_ACTION);
        assertTrue(mContext.bindService(new Intent(ISecondary.class.getName()),
                mSecondaryConnection, Context.BIND_AUTO_CREATE));
    }
    private static class Sync {
        public boolean mIsConnected;
        public boolean mIsDisConnected;
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (mSecondaryConnection != null) {
            mContext.unbindService(mSecondaryConnection);
        }
        if (mIntent != null) {
            mContext.stopService(mIntent);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onCallbackDied",
            args = {android.os.IInterface.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test register(IInterface), when"
                  + " 1. Register successfully, it will return true;"
                  + " 2. Register null, it will throw NPE.",
            method = "register",
            args = {android.os.IInterface.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unregister",
            args = {android.os.IInterface.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "beginBroadcast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "finishBroadcast",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBroadcastItem",
            args = {int.class}
        )
    })
    public void testRemoteCallbackList() throws Exception {
        MockRemoteCallbackList<IInterface> rc = new MockRemoteCallbackList<IInterface>();
        synchronized (mSync) {
            if (!mSync.mIsConnected) {
                mSync.wait();
            }
        }
        try {
            rc.register(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            rc.unregister(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        int servicePid = mSecondaryService.getPid();
        assertTrue(rc.register(mSecondaryService));
        int index = rc.beginBroadcast();
        assertEquals(1, index);
        IInterface actual = rc.getBroadcastItem(index - 1);
        assertNotNull(actual);
        assertSame(mSecondaryService, actual);
        rc.finishBroadcast();
        assertTrue(rc.unregister(mSecondaryService));
        rc.register(mSecondaryService);
        rc.beginBroadcast();
        android.os.Process.killProcess(servicePid);
        synchronized (mSync) {
            if (!mSync.mIsDisConnected) {
                mSync.wait();
            }
        }
        Thread.sleep(1000);
        assertTrue(rc.isOnCallbackDiedCalled);
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "kill",
        args = {}
    )
    public void testKill() {
        MockRemoteCallbackList<IInterface> rc = new MockRemoteCallbackList<IInterface>();
        synchronized (mSync) {
            if (!mSync.mIsConnected) {
                try {
                    mSync.wait();
                } catch (InterruptedException e) {
                    fail("Throw InterruptedException: " + e.getMessage());
                }
            }
        }
        rc.register(mSecondaryService);
        rc.beginBroadcast();
        rc.finishBroadcast();
        rc.kill();
        assertEquals(0, rc.beginBroadcast());
        assertFalse(rc.register(mSecondaryService));
    }
    private class MockRemoteCallbackList<E extends IInterface> extends RemoteCallbackList<E> {
        public boolean isOnCallbackDiedCalled;
        @Override
        public void onCallbackDied(E callback) {
            isOnCallbackDiedCalled = true;
            super.onCallbackDied(callback);
        }
    }
}
