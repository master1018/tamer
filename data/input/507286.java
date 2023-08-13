public class RemoteAndroidTestRunnerTest extends TestCase {
    private RemoteAndroidTestRunner mRunner;
    private MockDevice mMockDevice;
    private static final String TEST_PACKAGE = "com.test";
    private static final String TEST_RUNNER = "com.test.InstrumentationTestRunner";
    @Override
    protected void setUp() throws Exception {
        mMockDevice = new MockDevice();
        mRunner = new RemoteAndroidTestRunner(TEST_PACKAGE, TEST_RUNNER, mMockDevice);
    }
    public void testRun() {
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r %s/%s", TEST_PACKAGE, TEST_RUNNER),
                mMockDevice.getLastShellCommand());
    }
    public void testRunWithLog() {
        mRunner.setLogOnly(true);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r -e log true %s/%s", TEST_PACKAGE,
                TEST_RUNNER), mMockDevice.getLastShellCommand());
    }
    public void testRunWithMethod() {
        final String className = "FooTest";
        final String testName = "fooTest";
        mRunner.setMethodName(className, testName);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r -e class %s#%s %s/%s", className,
                testName, TEST_PACKAGE, TEST_RUNNER), mMockDevice.getLastShellCommand());
    }
    public void testRunWithPackage() {
        final String packageName = "foo.test";
        mRunner.setTestPackageName(packageName);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r -e package %s %s/%s", packageName,
                TEST_PACKAGE, TEST_RUNNER), mMockDevice.getLastShellCommand());
    }
    public void testRunWithAddInstrumentationArg() {
        final String extraArgName = "blah";
        final String extraArgValue = "blahValue";
        mRunner.addInstrumentationArg(extraArgName, extraArgValue);
        mRunner.run(new EmptyListener());
        assertStringsEquals(String.format("am instrument -w -r -e %s %s %s/%s", extraArgName,
                extraArgValue, TEST_PACKAGE, TEST_RUNNER), mMockDevice.getLastShellCommand());
    }
    private void assertStringsEquals(String str1, String str2) {
        String strippedStr1 = str1.replaceAll(" ", "");
        String strippedStr2 = str2.replaceAll(" ", "");
        assertEquals(strippedStr1, strippedStr2);
    }
    private static class MockDevice implements IDevice {
        private String mLastShellCommand;
        public void executeShellCommand(String command,
                IShellOutputReceiver receiver) throws IOException {
            mLastShellCommand = command;
        }
        public String getLastShellCommand() {
            return mLastShellCommand;
        }
        public boolean createForward(int localPort, int remotePort) {
            throw new UnsupportedOperationException();
        }
        public Client getClient(String applicationName) {
            throw new UnsupportedOperationException();
        }
        public String getClientName(int pid) {
            throw new UnsupportedOperationException();
        }
        public Client[] getClients() {
            throw new UnsupportedOperationException();
        }
        public FileListingService getFileListingService() {
            throw new UnsupportedOperationException();
        }
        public Map<String, String> getProperties() {
            throw new UnsupportedOperationException();
        }
        public String getProperty(String name) {
            throw new UnsupportedOperationException();
        }
        public int getPropertyCount() {
            throw new UnsupportedOperationException();
        }
        public String getMountPoint(String name) {
            throw new UnsupportedOperationException();
        }
        public RawImage getScreenshot() throws IOException {
            throw new UnsupportedOperationException();
        }
        public String getSerialNumber() {
            throw new UnsupportedOperationException();
        }
        public DeviceState getState() {
            throw new UnsupportedOperationException();
        }
        public SyncService getSyncService() {
            throw new UnsupportedOperationException();
        }
        public boolean hasClients() {
            throw new UnsupportedOperationException();
        }
        public boolean isBootLoader() {
            throw new UnsupportedOperationException();
        }
        public boolean isEmulator() {
            throw new UnsupportedOperationException();
        }
        public boolean isOffline() {
            throw new UnsupportedOperationException();
        }
        public boolean isOnline() {
            throw new UnsupportedOperationException();
        }
        public boolean removeForward(int localPort, int remotePort) {
            throw new UnsupportedOperationException();
        }
        public void runEventLogService(LogReceiver receiver) throws IOException {
            throw new UnsupportedOperationException();
        }
        public void runLogService(String logname, LogReceiver receiver) throws IOException {
            throw new UnsupportedOperationException();
        }
        public String getAvdName() {
            return "";
        }
        public String installPackage(String packageFilePath, boolean reinstall)
                throws IOException {
            throw new UnsupportedOperationException();
        }
        public String uninstallPackage(String packageName) throws IOException {
            throw new UnsupportedOperationException();
        }
        public String installRemotePackage(String remoteFilePath,
                boolean reinstall) throws IOException {
            throw new UnsupportedOperationException();
        }
        public void removeRemotePackage(String remoteFilePath)
                throws IOException {
            throw new UnsupportedOperationException();
        }
        public String syncPackageToDevice(String localFilePath)
                throws IOException {
            throw new UnsupportedOperationException();
        }
    }
    private static class EmptyListener implements ITestRunListener {
        public void testEnded(TestIdentifier test) {
        }
        public void testFailed(TestFailure status, TestIdentifier test, String trace) {
        }
        public void testRunEnded(long elapsedTime) {
        }
        public void testRunFailed(String errorMessage) {
        }
        public void testRunStarted(int testCount) {
        }
        public void testRunStopped(long elapsedTime) {
        }
        public void testStarted(TestIdentifier test) {
        }
    }
}
