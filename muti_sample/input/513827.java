public class AppSecurityTests extends DeviceTestCase {
    private static final String SHARED_UI_APK = "CtsSharedUidInstall.apk";
    private static final String SHARED_UI_PKG = "com.android.cts.shareuidinstall";
    private static final String SHARED_UI_DIFF_CERT_APK = "CtsSharedUidInstallDiffCert.apk";
    private static final String SHARED_UI_DIFF_CERT_PKG =
        "com.android.cts.shareuidinstalldiffcert";
    private static final String SIMPLE_APP_APK = "CtsSimpleAppInstall.apk";
    private static final String SIMPLE_APP_PKG = "com.android.cts.simpleappinstall";
    private static final String SIMPLE_APP_DIFF_CERT_APK = "CtsSimpleAppInstallDiffCert.apk";
    private static final String APP_WITH_DATA_APK = "CtsAppWithData.apk";
    private static final String APP_WITH_DATA_PKG = "com.android.cts.appwithdata";
    private static final String APP_ACCESS_DATA_APK = "CtsAppAccessData.apk";
    private static final String APP_ACCESS_DATA_PKG = "com.android.cts.appaccessdata";
    private static final String TARGET_INSTRUMENT_APK = "CtsTargetInstrumentationApp.apk";
    private static final String TARGET_INSTRUMENT_PKG = "com.android.cts.targetinstrumentationapp";
    private static final String INSTRUMENT_DIFF_CERT_APK = "CtsInstrumentationAppDiffCert.apk";
    private static final String INSTRUMENT_DIFF_CERT_PKG =
        "com.android.cts.instrumentationdiffcertapp";
    private static final String DECLARE_PERMISSION_APK = "CtsPermissionDeclareApp.apk";
    private static final String DECLARE_PERMISSION_PKG = "com.android.cts.permissiondeclareapp";
    private static final String PERMISSION_DIFF_CERT_APK = "CtsUsePermissionDiffCert.apk";
    private static final String PERMISSION_DIFF_CERT_PKG =
        "com.android.cts.usespermissiondiffcertapp";
    private static final String LOG_TAG = "AppSecurityTests";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertNotNull(getTestAppPath());
    }
    public void testSharedUidDifferentCerts() throws IOException {
        Log.i(LOG_TAG, "installing apks with shared uid, but different certs");
        try {
            getDevice().uninstallPackage(SHARED_UI_PKG);
            getDevice().uninstallPackage(SHARED_UI_DIFF_CERT_PKG);
            String installResult = getDevice().installPackage(getTestAppFilePath(SHARED_UI_APK),
                    false);
            assertNull("failed to install shared uid app", installResult);
            installResult = getDevice().installPackage(getTestAppFilePath(SHARED_UI_DIFF_CERT_APK),
                    false);
            assertNotNull("shared uid app with different cert than existing app installed " +
                    "successfully", installResult);
            assertEquals("INSTALL_FAILED_SHARED_USER_INCOMPATIBLE", installResult);
        }
        finally {
            getDevice().uninstallPackage(SHARED_UI_PKG);
            getDevice().uninstallPackage(SHARED_UI_DIFF_CERT_PKG);
        }
    }
    public void testAppUpgradeDifferentCerts() throws IOException {
        Log.i(LOG_TAG, "installing app upgrade with different certs");
        try {
            getDevice().uninstallPackage(SIMPLE_APP_PKG);
            String installResult = getDevice().installPackage(getTestAppFilePath(SIMPLE_APP_APK),
                    false);
            assertNull("failed to install simple app", installResult);
            installResult = getDevice().installPackage(getTestAppFilePath(SIMPLE_APP_DIFF_CERT_APK),
                    true );
            assertNotNull("app upgrade with different cert than existing app installed " +
                    "successfully", installResult);
            assertEquals("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES", installResult);
        }
        finally {
            getDevice().uninstallPackage(SIMPLE_APP_PKG);
        }
    }
    public void testAppFailAccessPrivateData() throws IOException {
        Log.i(LOG_TAG, "installing app that attempts to access another app's private data");
        try {
            getDevice().uninstallPackage(APP_WITH_DATA_PKG);
            getDevice().uninstallPackage(APP_ACCESS_DATA_PKG);
            String installResult = getDevice().installPackage(getTestAppFilePath(APP_WITH_DATA_APK),
                    false);
            assertNull("failed to install app with data", installResult);
            assertTrue("failed to create app's private data", runDeviceTests(APP_WITH_DATA_PKG));
            installResult = getDevice().installPackage(getTestAppFilePath(APP_ACCESS_DATA_APK),
                    false);
            assertNull("failed to install app access data", installResult);
            assertTrue("could access app's private data", runDeviceTests(APP_ACCESS_DATA_PKG));
        }
        finally {
            getDevice().uninstallPackage(APP_WITH_DATA_PKG);
            getDevice().uninstallPackage(APP_ACCESS_DATA_PKG);
        }
    }
    public void testInstrumentationDiffCert() throws IOException {
        Log.i(LOG_TAG, "installing app that attempts to instrument another app");
        try {
            getDevice().uninstallPackage(TARGET_INSTRUMENT_PKG);
            getDevice().uninstallPackage(INSTRUMENT_DIFF_CERT_PKG);
            String installResult = getDevice().installPackage(
                    getTestAppFilePath(TARGET_INSTRUMENT_APK), false);
            assertNull("failed to install target instrumentation app", installResult);
            installResult = getDevice().installPackage(getTestAppFilePath(INSTRUMENT_DIFF_CERT_APK),
                    false);
            assertNull("failed to install instrumentation app with diff cert", installResult);
            String runResults = runDeviceTestsWithRunResult(INSTRUMENT_DIFF_CERT_PKG);
            assertNotNull("running instrumentation with diff cert unexpectedly succeeded",
                    runResults);
            String msg = String.format("Unexpected error message result from %s. Received %s",
                    "instrumentation with diff cert. Expected starts with Permission Denial",
                    runResults);
            assertTrue(msg, runResults.startsWith("Permission Denial"));
        }
        finally {
            getDevice().uninstallPackage(TARGET_INSTRUMENT_PKG);
            getDevice().uninstallPackage(INSTRUMENT_DIFF_CERT_PKG);
        }
    }
    public void testPermissionDiffCert() throws IOException {
        Log.i(LOG_TAG, "installing app that attempts to use permission of another app");
        try {
            getDevice().uninstallPackage(DECLARE_PERMISSION_PKG);
            getDevice().uninstallPackage(PERMISSION_DIFF_CERT_PKG);
            String installResult = getDevice().installPackage(
                    getTestAppFilePath(DECLARE_PERMISSION_APK), false);
            assertNull("failed to install declare permission app", installResult);
            installResult = getDevice().installPackage(getTestAppFilePath(PERMISSION_DIFF_CERT_APK),
                    false);
            assertNull("failed to install permission app with diff cert", installResult);
            assertTrue("unexpected result when running permission tests",
                    runDeviceTests(PERMISSION_DIFF_CERT_PKG));
        }
        finally {
            getDevice().uninstallPackage(DECLARE_PERMISSION_PKG);
            getDevice().uninstallPackage(PERMISSION_DIFF_CERT_PKG);
        }
    }
    private String getTestAppFilePath(String fileName) {
        return String.format("%s%s%s", getTestAppPath(), File.separator, fileName);
    }
    private boolean runDeviceTests(String pkgName) {
        CollectingTestRunListener listener = doRunTests(pkgName);
        return listener.didAllTestsPass();
    }
    private CollectingTestRunListener doRunTests(String pkgName) {
        RemoteAndroidTestRunner testRunner = new RemoteAndroidTestRunner(pkgName, getDevice());
        CollectingTestRunListener listener = new CollectingTestRunListener();
        testRunner.run(listener);
        return listener;
    }
    private String runDeviceTestsWithRunResult(String pkgName) {
        CollectingTestRunListener listener = doRunTests(pkgName);
        return listener.getTestRunErrorMessage();
    }
    private static class CollectingTestRunListener implements ITestRunListener {
        private boolean mAllTestsPassed = true;
        private String mTestRunErrorMessage = null;
        public void testEnded(TestIdentifier test) {
        }
        public void testFailed(TestFailure status, TestIdentifier test,
                String trace) {
            Log.w(LOG_TAG, String.format("%s#%s failed: %s", test.getClassName(),
                    test.getTestName(), trace));
            mAllTestsPassed = false;
        }
        public void testRunEnded(long elapsedTime) {
        }
        public void testRunFailed(String errorMessage) {
            Log.w(LOG_TAG, String.format("test run failed: %s", errorMessage));
            mAllTestsPassed = false;
            mTestRunErrorMessage = errorMessage;
        }
        public void testRunStarted(int testCount) {
        }
        public void testRunStopped(long elapsedTime) {
        }
        public void testStarted(TestIdentifier test) {
        }
        boolean didAllTestsPass() {
            return mAllTestsPassed;
        }
        String getTestRunErrorMessage() {
            return mTestRunErrorMessage;
        }
    }
    public static Test suite() {
        return new DeviceTestSuite(AppSecurityTests.class);
    }
}
