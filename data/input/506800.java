public class AndroidManifestParserTest extends TestCase {
    private AndroidManifestParser mManifestTestApp;
    private AndroidManifestParser mManifestInstrumentation;
    private static final String TESTDATA_PATH =
        "com/android/ide/eclipse/testdata/";  
    private static final String INSTRUMENTATION_XML = TESTDATA_PATH +
        "AndroidManifest-instrumentation.xml";  
    private static final String TESTAPP_XML = TESTDATA_PATH +
        "AndroidManifest-testapp.xml";  
    private static final String PACKAGE_NAME =  "com.android.testapp"; 
    private static final String ACTIVITY_NAME = "com.android.testapp.MainActivity"; 
    private static final String LIBRARY_NAME = "android.test.runner"; 
    private static final String INSTRUMENTATION_NAME = "android.test.InstrumentationTestRunner"; 
    private static final String INSTRUMENTATION_TARGET = "com.android.AndroidProject"; 
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String testFilePath = AdtTestData.getInstance().getTestFilePath(TESTAPP_XML);
        mManifestTestApp = AndroidManifestParser.parseForData(testFilePath);
        assertNotNull(mManifestTestApp);
        testFilePath = AdtTestData.getInstance().getTestFilePath(INSTRUMENTATION_XML);
        mManifestInstrumentation = AndroidManifestParser.parseForData(testFilePath);
        assertNotNull(mManifestInstrumentation);
    }
    public void testGetInstrumentationInformation() {
        assertEquals(1, mManifestInstrumentation.getInstrumentations().length);
        assertEquals(INSTRUMENTATION_NAME,
                mManifestInstrumentation.getInstrumentations()[0].getName());
        assertEquals(INSTRUMENTATION_TARGET,
                mManifestInstrumentation.getInstrumentations()[0].getTargetPackage());
    }
    public void testGetPackage() {
        assertEquals(PACKAGE_NAME, mManifestTestApp.getPackage());
    }
    public void testGetActivities() {
        assertEquals(1, mManifestTestApp.getActivities().length);
        AndroidManifestParser.Activity activity = mManifestTestApp.getActivities()[0];
        assertEquals(ACTIVITY_NAME, activity.getName());
        assertTrue(activity.hasAction());
        assertTrue(activity.isHomeActivity());
        assertTrue(activity.hasAction());
        assertEquals(activity, mManifestTestApp.getActivities()[0]);
    }
    public void testGetLauncherActivity() {
        Activity activity = mManifestTestApp.getLauncherActivity();
        assertEquals(ACTIVITY_NAME, activity.getName());
        assertTrue(activity.hasAction());
        assertTrue(activity.isHomeActivity());
    }
    private void assertEquals(Activity lhs, Activity rhs) {
        assertTrue(lhs == rhs || (lhs != null && rhs != null));
        if (lhs != null && rhs != null) {
            assertEquals(lhs.getName(),        rhs.getName());
            assertEquals(lhs.isExported(),     rhs.isExported());
            assertEquals(lhs.hasAction(),      rhs.hasAction());
            assertEquals(lhs.isHomeActivity(), rhs.isHomeActivity());
        }
    }
    public void testGetUsesLibraries() {
        assertEquals(1, mManifestTestApp.getUsesLibraries().length);
        assertEquals(LIBRARY_NAME, mManifestTestApp.getUsesLibraries()[0]);
    }
    public void testGetPackageName() {
        assertEquals(PACKAGE_NAME, mManifestTestApp.getPackage());
    }
}
