public abstract class ServiceTestCase<T extends Service> extends AndroidTestCase {
    Class<T> mServiceClass;
    private Context mSystemContext;
    private Application mApplication;
    public ServiceTestCase(Class<T> serviceClass) {
        mServiceClass = serviceClass;
    }
    private T mService;
    private boolean mServiceAttached = false;
    private boolean mServiceCreated = false;
    private boolean mServiceStarted = false;
    private boolean mServiceBound = false;
    private Intent mServiceIntent = null;
    private int mServiceId;
    public T getService() {
        return mService;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSystemContext = getContext();
    }
    protected void setupService() {
        mService = null;
        try {
            mService = mServiceClass.newInstance();
        } catch (Exception e) {
            assertNotNull(mService);
        }
        if (getApplication() == null) {
            setApplication(new MockApplication());
        }
        mService.attach(
                getContext(),
                null,               
                mServiceClass.getName(),
                null,               
                getApplication(),
                null                
                );
        assertNotNull(mService);
        mServiceId = new Random().nextInt();
        mServiceAttached = true;
    }
    protected void startService(Intent intent) {
        assertFalse(mServiceStarted);
        assertFalse(mServiceBound);
        if (!mServiceAttached) {
            setupService();
        }
        assertNotNull(mService);
        if (!mServiceCreated) {
            mService.onCreate();
            mServiceCreated = true;
        }
        mService.onStart(intent, mServiceId);
        mServiceStarted = true;
    }
    protected IBinder bindService(Intent intent) {
        assertFalse(mServiceStarted);
        assertFalse(mServiceBound);
        if (!mServiceAttached) {
            setupService();
        }
        assertNotNull(mService);
        if (!mServiceCreated) {
            mService.onCreate();
            mServiceCreated = true;
        }
        mServiceIntent = intent.cloneFilter();
        IBinder result = mService.onBind(intent);
        mServiceBound = true;
        return result;
    }
    protected void shutdownService() {
        if (mServiceStarted) {
            mService.stopSelf();
            mServiceStarted = false;
        } else if (mServiceBound) {
            mService.onUnbind(mServiceIntent);
            mServiceBound = false;
        }
        if (mServiceCreated) {
            mService.onDestroy();
        }
    }
    @Override
    protected void tearDown() throws Exception {
        shutdownService();
        mService = null;
        scrubClass(ServiceTestCase.class);
        super.tearDown();
    }
    public void setApplication(Application application) {
        mApplication = application;
    }
    public Application getApplication() {
        return mApplication;
    }
    public Context getSystemContext() {
        return mSystemContext;
    }
    public void testServiceTestCaseSetUpProperly() throws Exception {
        setupService();
        assertNotNull("service should be launched successfully", mService);
    }
}
