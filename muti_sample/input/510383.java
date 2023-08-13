public class BinderThreadPriorityTest extends AndroidTestCase {
    private static final String TAG = "BinderThreadPriorityTest";
    private IBinderThreadPriorityService mService;
    private int mSavedPriority;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (BinderThreadPriorityTest.this) {
                mService = IBinderThreadPriorityService.Stub.asInterface(service);
                BinderThreadPriorityTest.this.notifyAll();
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    private static class ServiceStub extends IBinderThreadPriorityService.Stub {
        public int getThreadPriority() { fail(); return -999; }
        public String getThreadSchedulerGroup() { fail(); return null; }
        public void setPriorityAndCallBack(int p, IBinderThreadPriorityService cb) { fail(); }
        public void callBack(IBinderThreadPriorityService cb) { fail(); }
        private static void fail() { throw new RuntimeException("unimplemented"); }
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getContext().bindService(
                new Intent(getContext(), BinderThreadPriorityService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        synchronized (this) {
            if (mService == null) {
                try {
                    wait(30000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                assertNotNull("Gave up waiting for BinderThreadPriorityService", mService);
            }
        }
        mSavedPriority = Process.getThreadPriority(Process.myTid());
        Process.setThreadPriority(mSavedPriority);  
        assertEquals(expectedSchedulerGroup(mSavedPriority), getSchedulerGroup());
        Log.i(TAG, "Saved priority: " + mSavedPriority);
    }
    @Override
    protected void tearDown() throws Exception {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Process.setThreadPriority(mSavedPriority);
        assertEquals(mSavedPriority, Process.getThreadPriority(Process.myTid()));
        assertEquals(expectedSchedulerGroup(mSavedPriority), getSchedulerGroup());
        getContext().unbindService(mConnection);
        super.tearDown();
    }
    public static String getSchedulerGroup() {
        String fn = "/proc/" + Process.myPid() + "/task/" + Process.myTid() + "/cgroup";
        try {
            String cgroup = FileUtils.readTextFile(new File(fn), 1024, null);
            for (String line : cgroup.split("\n")) {
                String fields[] = line.trim().split(":");
                    if (fields.length == 3 && fields[1].equals("cpu")) return fields[2];
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't read: " + fn, e);
        }
        return null;  
    }
    public static String expectedSchedulerGroup(int prio) {
        return prio < Process.THREAD_PRIORITY_BACKGROUND ? "/" : "/bg_non_interactive";
    }
    public void testPassPriorityToService() throws Exception {
        for (int prio = 19; prio >= -20; prio--) {
            Process.setThreadPriority(prio);
            assertEquals(prio, Process.getThreadPriority(Process.myTid()));
            assertEquals(expectedSchedulerGroup(prio), getSchedulerGroup());
            assertEquals(prio, mService.getThreadPriority());
            assertEquals(expectedSchedulerGroup(prio), mService.getThreadSchedulerGroup());
        }
    }
    public void testCallBackFromServiceWithPriority() throws Exception {
        for (int prio = -20; prio <= 19; prio++) {
            final int expected = prio;
            mService.setPriorityAndCallBack(prio, new ServiceStub() {
                public void callBack(IBinderThreadPriorityService cb) {
                    assertEquals(expected, Process.getThreadPriority(Process.myTid()));
                    assertEquals(expectedSchedulerGroup(expected), getSchedulerGroup());
                }
            });
            assertEquals(mSavedPriority, Process.getThreadPriority(Process.myTid()));
        }
    }
}
