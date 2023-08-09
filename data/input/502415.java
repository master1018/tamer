public abstract class ActivityUnitTestCase<T extends Activity> 
        extends ActivityTestCase {
    private Class<T> mActivityClass;
    private Context mActivityContext;
    private Application mApplication;
    private MockParent mMockParent;
    private boolean mAttached = false;
    private boolean mCreated = false;
    public ActivityUnitTestCase(Class<T> activityClass) {
        mActivityClass = activityClass;
    }
    @Override
    public T getActivity() {
        return (T) super.getActivity();
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
      mActivityContext = getInstrumentation().getTargetContext();
    }
    protected T startActivity(Intent intent, Bundle savedInstanceState,
            Object lastNonConfigurationInstance) {
        assertFalse("Activity already created", mCreated);
        if (!mAttached) {
            assertNotNull(mActivityClass);
            setActivity(null);
            T newActivity = null;
            try {
                IBinder token = null;
                if (mApplication == null) {
                    setApplication(new MockApplication());
                }
                ComponentName cn = new ComponentName(mActivityClass.getPackage().getName(), 
                        mActivityClass.getName());
                intent.setComponent(cn);
                ActivityInfo info = new ActivityInfo();
                CharSequence title = mActivityClass.getName();
                mMockParent = new MockParent();
                String id = null;
                newActivity = (T) getInstrumentation().newActivity(mActivityClass, mActivityContext,
                        token, mApplication, intent, info, title, mMockParent, id,
                        lastNonConfigurationInstance);
            } catch (Exception e) {
                assertNotNull(newActivity);
            }
            assertNotNull(newActivity);
            setActivity(newActivity);
            mAttached = true;
        }
        T result = getActivity();
        if (result != null) {
            getInstrumentation().callActivityOnCreate(getActivity(), savedInstanceState);
            mCreated = true;
        }
        return result;
    }
    @Override
    protected void tearDown() throws Exception {
        setActivity(null);
        scrubClass(ActivityInstrumentationTestCase.class);
        super.tearDown();
    }
    public void setApplication(Application application) {
        mApplication = application;
    }
    public void setActivityContext(Context activityContext) {
        mActivityContext = activityContext;
    }
    public int getRequestedOrientation() {
        if (mMockParent != null) {
            return mMockParent.mRequestedOrientation;
        }
        return 0;
    }
    public Intent getStartedActivityIntent() {
        if (mMockParent != null) {
            return mMockParent.mStartedActivityIntent;
        }
        return null;
    }
    public int getStartedActivityRequest() {
        if (mMockParent != null) {
            return mMockParent.mStartedActivityRequest;
        }
        return 0;
    }
    public boolean isFinishCalled() {
        if (mMockParent != null) {
            return mMockParent.mFinished;
        }
        return false;
    }
    public int getFinishedActivityRequest() {
        if (mMockParent != null) {
            return mMockParent.mFinishedActivityRequest;
        }
        return 0;
    }
    private static class MockParent extends Activity {
        public int mRequestedOrientation = 0;
        public Intent mStartedActivityIntent = null;
        public int mStartedActivityRequest = -1;
        public boolean mFinished = false;
        public int mFinishedActivityRequest = -1;
        @Override
        public void setRequestedOrientation(int requestedOrientation) {
            mRequestedOrientation = requestedOrientation;
        }
        @Override
        public int getRequestedOrientation() {
            return mRequestedOrientation;
        }
        @Override
        public Window getWindow() {
            return null;
        }
        @Override
        public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
            mStartedActivityIntent = intent;
            mStartedActivityRequest = requestCode;
        }
        @Override
        public void finishFromChild(Activity child) {
            mFinished = true;
        }
        @Override
        public void finishActivityFromChild(Activity child, int requestCode) {
            mFinished = true;
            mFinishedActivityRequest = requestCode;
        }
    }
}
