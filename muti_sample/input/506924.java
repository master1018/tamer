public abstract class ActivityInstrumentationTestCase2<T extends Activity> 
        extends ActivityTestCase {
    Class<T> mActivityClass;
    boolean mInitialTouchMode = false;
    Intent mActivityIntent = null;
    @Deprecated
    public ActivityInstrumentationTestCase2(String pkg, Class<T> activityClass) {
        this(activityClass);
    }
    public ActivityInstrumentationTestCase2(Class<T> activityClass) {
        mActivityClass = activityClass;
    }
    @Override
    public T getActivity() {
        Activity a = super.getActivity();
        if (a == null) {
            getInstrumentation().setInTouchMode(mInitialTouchMode);
            final String targetPackage = getInstrumentation().getTargetContext().getPackageName();
            if (mActivityIntent == null) {
                a = launchActivity(targetPackage, mActivityClass, null);
            } else {
                a = launchActivityWithIntent(targetPackage, mActivityClass, mActivityIntent);
            }
            setActivity(a);
        }
        return (T) a;
    }
    public void setActivityIntent(Intent i) {
        mActivityIntent = i;
    }
    public void setActivityInitialTouchMode(boolean initialTouchMode) {
        mInitialTouchMode = initialTouchMode;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        boolean mInitialTouchMode = false;
        Intent mActivityIntent = null;
    }
    @Override
    protected void tearDown() throws Exception {
        Activity a = super.getActivity();
        if (a != null) {
            a.finish();
            setActivity(null);
        }
        scrubClass(ActivityInstrumentationTestCase2.class);
        super.tearDown();
    }
    @Override
    protected void runTest() throws Throwable {
        try {
            Method method = getClass().getMethod(getName(), (Class[]) null);
            if (method.isAnnotationPresent(UiThreadTest.class)) {
                getActivity();
            }
        } catch (Exception e) {
        }
        super.runTest();
    }
}
