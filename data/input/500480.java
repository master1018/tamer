public class PackageManagerHostTestUtils extends Assert {
    private static final String LOG_TAG = "PackageManagerHostTests";
    private IDevice mDevice = null;
    private static final String APP_PRIVATE_PATH = "/data/app-private/";
    private static final String DEVICE_APP_PATH = "/data/app/";
    private static final String SDCARD_APP_PATH = "/mnt/secure/asec/";
    private static final int MAX_WAIT_FOR_DEVICE_TIME = 120 * 1000;
    private static final int WAIT_FOR_DEVICE_POLL_TIME = 10 * 1000;
    private static final int MAX_WAIT_FOR_APP_LAUNCH_TIME = 60 * 1000;
    private static final int WAIT_FOR_APP_LAUNCH_POLL_TIME = 5 * 1000;
    public static enum InstallLocPreference {
        AUTO,
        INTERNAL,
        EXTERNAL
    }
    public static enum InstallLocation {
        DEVICE,
        SDCARD
    }
    public PackageManagerHostTestUtils(IDevice device)
    {
          mDevice = device;
    }
    private PackageManagerHostTestUtils() {}
    public static String getAppPrivatePath() {
        return APP_PRIVATE_PATH;
    }
    public static String getDeviceAppPath() {
        return DEVICE_APP_PATH;
    }
    public static String getSDCardAppPath() {
        return SDCARD_APP_PATH;
    }
    private CollectingTestRunListener doRunTests(String pkgName) throws IOException {
        RemoteAndroidTestRunner testRunner = new RemoteAndroidTestRunner(
                pkgName, mDevice);
        CollectingTestRunListener listener = new CollectingTestRunListener();
        testRunner.run(listener);
        return listener;
    }
    public boolean runDeviceTestsDidAllTestsPass(String pkgName) throws IOException {
        CollectingTestRunListener listener = doRunTests(pkgName);
        return listener.didAllTestsPass();
    }
    public void pushFile(final String localFilePath, final String destFilePath)
            throws IOException {
        SyncResult result = mDevice.getSyncService().pushFile(
                localFilePath, destFilePath, new NullSyncProgressMonitor());
        assertEquals(SyncService.RESULT_OK, result.getCode());
    }
    public void installFile(final String localFilePath, final boolean replace) throws IOException {
        String result = mDevice.installPackage(localFilePath, replace);
        assertEquals(null, result);
    }
    public String installFileFail(final String localFilePath, final boolean replace)
            throws IOException {
        String result = mDevice.installPackage(localFilePath, replace);
        assertNotNull(result);
        return result;
    }
    public String installFileForwardLocked(final String localFilePath, final boolean replace)
            throws IOException {
        String remoteFilePath = mDevice.syncPackageToDevice(localFilePath);
        InstallReceiver receiver = new InstallReceiver();
        String cmd = String.format(replace ? "pm install -r -l \"%1$s\"" :
                "pm install -l \"%1$s\"", remoteFilePath);
        mDevice.executeShellCommand(cmd, receiver);
        mDevice.removeRemotePackage(remoteFilePath);
        return receiver.getErrorMessage();
    }
    public boolean doesRemoteFileExist(String destPath) throws IOException {
        String lsGrep = executeShellCommand(String.format("ls %s", destPath));
        return !lsGrep.contains("No such file or directory");
    }
    public boolean doesRemoteFileExistContainingString(String destPath, String searchString)
            throws IOException {
        String lsResult = executeShellCommand(String.format("ls %s", destPath));
        return lsResult.contains(searchString);
    }
    public boolean doesPackageExist(String packageName) throws IOException {
        String pkgGrep = executeShellCommand(String.format("pm path %s", packageName));
        return pkgGrep.contains("package:");
    }
    public boolean doesAppExistOnDevice(String packageName) throws IOException {
        return doesRemoteFileExistContainingString(DEVICE_APP_PATH, packageName);
    }
    public boolean doesAppExistOnSDCard(String packageName) throws IOException {
        return doesRemoteFileExistContainingString(SDCARD_APP_PATH, packageName);
    }
    public boolean doesAppExistAsForwardLocked(String packageName) throws IOException {
        return doesRemoteFileExistContainingString(APP_PRIVATE_PATH, packageName);
    }
    public void waitForPackageManager() throws InterruptedException, IOException {
        Log.i(LOG_TAG, "waiting for device");
        int currentWaitTime = 0;
        while (!doesPackageExist("android")) {
            Thread.sleep(WAIT_FOR_DEVICE_POLL_TIME);
            currentWaitTime += WAIT_FOR_DEVICE_POLL_TIME;
            if (currentWaitTime > MAX_WAIT_FOR_DEVICE_TIME) {
                Log.e(LOG_TAG, "time out waiting for device");
                throw new InterruptedException();
            }
        }
    }
    private boolean deviceIsOnline() {
        AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();
        IDevice[] devices = bridge.getDevices();
        for (IDevice device : devices) {
            if ((mDevice != null) &&
                    mDevice.getSerialNumber().equals(device.getSerialNumber()) &&
                    device.isOnline()) {
                return true;
            }
        }
        return false;
    }
    public void waitForDeviceToComeOnline() throws InterruptedException, IOException {
        Log.i(LOG_TAG, "waiting for device to be online");
        int currentWaitTime = 0;
        while (!deviceIsOnline()) {
            Thread.sleep(WAIT_FOR_DEVICE_POLL_TIME);
            currentWaitTime += WAIT_FOR_DEVICE_POLL_TIME;
            if (currentWaitTime > MAX_WAIT_FOR_DEVICE_TIME) {
                Log.e(LOG_TAG, "time out waiting for device");
                throw new InterruptedException();
            }
        }
        Thread.sleep(WAIT_FOR_DEVICE_POLL_TIME);
    }
    public void waitForApp(String packageName) throws InterruptedException, IOException {
        Log.i(LOG_TAG, "waiting for app to launch");
        int currentWaitTime = 0;
        while (!doesPackageExist(packageName)) {
            Thread.sleep(WAIT_FOR_APP_LAUNCH_POLL_TIME);
            currentWaitTime += WAIT_FOR_APP_LAUNCH_POLL_TIME;
            if (currentWaitTime > MAX_WAIT_FOR_APP_LAUNCH_TIME) {
                Log.e(LOG_TAG, "time out waiting for app to launch: " + packageName);
                throw new InterruptedException();
            }
        }
    }
    public String executeShellCommand(String command) throws IOException {
        Log.i(LOG_TAG, String.format("adb shell %s", command));
        CollectingOutputReceiver receiver = new CollectingOutputReceiver();
        mDevice.executeShellCommand(command, receiver);
        String output = receiver.getOutput();
        Log.i(LOG_TAG, String.format("Result: %s", output));
        return output;
    }
    public void runAdbRoot() throws IOException, InterruptedException {
        Log.i(LOG_TAG, "adb root");
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec("adb root"); 
        BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String nextLine = null;
        while (null != (nextLine = output.readLine())) {
            Log.i(LOG_TAG, nextLine);
        }
        process.waitFor();
        waitForDeviceToComeOnline();
        waitForPackageManager(); 
    }
    public void rebootDevice() throws IOException, InterruptedException {
        String command = "reboot"; 
        Log.i(LOG_TAG, command);
        CollectingOutputReceiver receiver = new CollectingOutputReceiver();
        mDevice.executeShellCommand(command, receiver);
        String output = receiver.getOutput();
        Log.i(LOG_TAG, String.format("Result: %s", output));
        waitForDeviceToComeOnline(); 
        runAdbRoot();
    }
    private class CollectingOutputReceiver extends MultiLineReceiver {
        private StringBuffer mOutputBuffer = new StringBuffer();
        public String getOutput() {
            return mOutputBuffer.toString();
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line: lines) {
                mOutputBuffer.append(line);
                mOutputBuffer.append("\n");
            }
        }
        public boolean isCancelled() {
            return false;
        }
    }
    private class NullSyncProgressMonitor implements ISyncProgressMonitor {
        public void advance(int work) {
        }
        public boolean isCanceled() {
            return false;
        }
        public void start(int totalWork) {
        }
        public void startSubTask(String name) {
        }
        public void stop() {
        }
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
    private static final class InstallReceiver extends MultiLineReceiver {
        private static final String SUCCESS_OUTPUT = "Success"; 
        private static final Pattern FAILURE_PATTERN = Pattern.compile("Failure\\s+\\[(.*)\\]"); 
        private String mErrorMessage = null;
        public InstallReceiver() {
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.length() > 0) {
                    if (line.startsWith(SUCCESS_OUTPUT)) {
                        mErrorMessage = null;
                    } else {
                        Matcher m = FAILURE_PATTERN.matcher(line);
                        if (m.matches()) {
                            mErrorMessage = m.group(1);
                        }
                    }
                }
            }
        }
        public boolean isCancelled() {
            return false;
        }
        public String getErrorMessage() {
            return mErrorMessage;
        }
    }
    public void installAppAndVerifyExistsOnSDCard(String apkPath, String pkgName, boolean overwrite)
            throws IOException, InterruptedException {
        if (!overwrite) {
            mDevice.uninstallPackage(pkgName);
            assertFalse(doesPackageExist(pkgName));
        }
        installFile(apkPath, overwrite);
        assertTrue(doesAppExistOnSDCard(pkgName));
        assertFalse(doesAppExistOnDevice(pkgName));
        waitForPackageManager();
        assertTrue(doesPackageExist(pkgName));
    }
    public void installAppAndVerifyExistsOnDevice(String apkPath, String pkgName, boolean overwrite)
            throws IOException, InterruptedException {
        if (!overwrite) {
            mDevice.uninstallPackage(pkgName);
            assertFalse(doesPackageExist(pkgName));
        }
        installFile(apkPath, overwrite);
        assertFalse(doesAppExistOnSDCard(pkgName));
        assertTrue(doesAppExistOnDevice(pkgName));
        waitForPackageManager();
        assertTrue(doesPackageExist(pkgName));
    }
    public void installFwdLockedAppAndVerifyExists(String apkPath,
            String pkgName, boolean overwrite) throws IOException, InterruptedException {
        if (!overwrite) {
            mDevice.uninstallPackage(pkgName);
            assertFalse(doesPackageExist(pkgName));
        }
        String result = installFileForwardLocked(apkPath, overwrite);
        assertEquals(null, result);
        assertTrue(doesAppExistAsForwardLocked(pkgName));
        assertFalse(doesAppExistOnSDCard(pkgName));
        waitForPackageManager();
        assertTrue(doesPackageExist(pkgName));
    }
    public void uninstallApp(String pkgName) throws IOException, InterruptedException {
        mDevice.uninstallPackage(pkgName);
        assertFalse(doesPackageExist(pkgName));
    }
    public void wipeNonSystemApps() throws IOException {
      String allInstalledPackages = executeShellCommand("pm list packages -f");
      BufferedReader outputReader = new BufferedReader(new StringReader(allInstalledPackages));
      String currentLine = null;
      while ((currentLine = outputReader.readLine()) != null) {
          if (currentLine.contains("/system/")) {
              continue;
          }
          String packageName = currentLine.substring(currentLine.indexOf('=') + 1);
          mDevice.uninstallPackage(packageName);
      }
      executeShellCommand(String.format("rm %s*", SDCARD_APP_PATH, "*"));
      executeShellCommand(String.format("rm %s*", DEVICE_APP_PATH, "*"));
      executeShellCommand(String.format("rm %s*", APP_PRIVATE_PATH, "*"));
    }
    public void setDevicePreferredInstallLocation(InstallLocPreference pref) throws IOException {
        String command = "pm setInstallLocation %d";
        int locValue = 0;
        switch (pref) {
            case INTERNAL:
                locValue = 1;
                break;
            case EXTERNAL:
                locValue = 2;
                break;
            default: 
                locValue = 0;
                break;
        }
        executeShellCommand(String.format(command, locValue));
    }
    public InstallLocPreference getDevicePreferredInstallLocation() throws IOException {
        String result = executeShellCommand("pm getInstallLocation");
        if (result.indexOf('0') != -1) {
            return InstallLocPreference.AUTO;
        }
        else if (result.indexOf('1') != -1) {
            return InstallLocPreference.INTERNAL;
        }
        else {
            return InstallLocPreference.EXTERNAL;
        }
    }
}
