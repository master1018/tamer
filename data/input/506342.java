public abstract class ActivityInstrumentationTestCase<T extends Activity> 
        extends ActivityTestCase {
    String mPackage;
    Class<T> mActivityClass;
    boolean mInitialTouchMode = false;
    public ActivityInstrumentationTestCase(String pkg, Class<T> activityClass) {
        this(pkg, activityClass, false);
    }
    public ActivityInstrumentationTestCase(String pkg, Class<T> activityClass, 
            boolean initialTouchMode) {
        mActivityClass = activityClass;
        mInitialTouchMode = initialTouchMode;
    }
    @Override
    public T getActivity() {
        return (T) super.getActivity();
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getInstrumentation().setInTouchMode(mInitialTouchMode);
        final String targetPackageName = getInstrumentation().getTargetContext().getPackageName();
        setActivity(launchActivity(targetPackageName, mActivityClass, null));
    }
    @Override
    protected void tearDown() throws Exception {
        getActivity().finish();
        setActivity(null);
        scrubClass(ActivityInstrumentationTestCase.class);
        super.tearDown();
    }
    public void testActivityTestCaseSetUpProperly() throws Exception {
        assertNotNull("activity should be launched successfully", getActivity());
    }
}
