public abstract class SingleLaunchActivityTestCase<T extends Activity>
        extends InstrumentationTestCase {
    String mPackage;
    Class<T> mActivityClass;
    private static int sTestCaseCounter = 0;
    private static boolean sActivityLaunchedFlag = false;
    public SingleLaunchActivityTestCase(String pkg, Class<T> activityClass) {
        mPackage = pkg;
        mActivityClass = activityClass;        
        sTestCaseCounter ++;                
    }
    private static Activity sActivity;
    public T getActivity() {
        return (T) sActivity;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (!sActivityLaunchedFlag) {
            getInstrumentation().setInTouchMode(false);
            sActivity = launchActivity(mPackage, mActivityClass, null);
            sActivityLaunchedFlag = true;
        }                        
    }
    @Override
    protected void tearDown() throws Exception {
        sTestCaseCounter --;
        if (sTestCaseCounter == 1) {
            sActivity.finish();
        }        
        super.tearDown();
    }
    public void testActivityTestCaseSetUpProperly() throws Exception {
        assertNotNull("activity should be launched successfully", sActivity);
    }
}
