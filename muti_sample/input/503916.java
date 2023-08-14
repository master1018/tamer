public abstract class ApplicationTestCase<T extends Application> extends AndroidTestCase {
    Class<T> mApplicationClass;
    private Context mSystemContext;
    public ApplicationTestCase(Class<T> applicationClass) {
        mApplicationClass = applicationClass;
    }
    private T mApplication;
    private boolean mAttached = false;
    private boolean mCreated = false;
    public T getApplication() {
        return mApplication;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSystemContext = getContext();
    }
    private void setupApplication() {
        mApplication = null;
        try {
            mApplication = (T) Instrumentation.newApplication(mApplicationClass, getContext());
        } catch (Exception e) {
            assertNotNull(mApplication);
        }
        mAttached = true;
    }
    final protected void createApplication() {
        assertFalse(mCreated);
        if (!mAttached) {
            setupApplication();
        }
        assertNotNull(mApplication);
        mApplication.onCreate();
        mCreated = true;
    }
    final protected void terminateApplication() {
        if (mCreated) {
            mApplication.onTerminate();
        }
    }
    @Override
    protected void tearDown() throws Exception {
        terminateApplication();
        mApplication = null;
        scrubClass(ApplicationTestCase.class);
        super.tearDown();
    }
    public Context getSystemContext() {
        return mSystemContext;
    }
    final public void testApplicationTestCaseSetUpProperly() throws Exception {
        setupApplication();
        assertNotNull("Application class could not be instantiated successfully", mApplication);
    }
}
