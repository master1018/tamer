public class ReferenceAppTestPackage extends TestPackage {
    private static final String ACTION_REFERENCE_APP_TEST = "ReferenceAppTest";
    private final String apkToTestName;
    private final String packageUnderTest;
    private ArrayList<String> testOutputLines = new ArrayList<String>();
    public ReferenceAppTestPackage(String instrumentationRunner,
            String testPkgBinaryName, String targetNameSpace,
            String targetBinaryName, String version,
            String androidVersion, String jarPath,
            String appNameSpace, String appPackageName,
            String apkToTestName, String packageUnderTest) throws NoSuchAlgorithmException {
        super(instrumentationRunner, testPkgBinaryName, targetNameSpace, targetBinaryName, version,
                androidVersion, jarPath, appNameSpace, appPackageName);
        this.apkToTestName = apkToTestName;
        this.packageUnderTest = packageUnderTest;
    }
    @Override
    public void run(final TestDevice device, final String javaPkgName,
                    TestSessionLog testSessionLog) throws DeviceDisconnectedException,
                    InvalidApkPathException, InvalidNameSpaceException {
        Test test = getTests().iterator().next();
        if ((test != null) && (test.getResult().isNotExecuted())) {
            String appToTestApkPath =
                HostConfig.getInstance().getCaseRepository().getApkPath(apkToTestName);
            device.setRuntimeListener(device);
            device.installAPK(appToTestApkPath);
            device.waitForCommandFinish();
            String testApkPath = HostConfig.getInstance().getCaseRepository()
                    .getApkPath(getAppBinaryName());
            device.installAPK(testApkPath);
            device.waitForCommandFinish();
            runTests(device, testSessionLog);
            device.uninstallAPK(getAppPackageName());
            device.waitForCommandFinish();
            device.uninstallAPK(packageUnderTest);
            device.waitForCommandFinish();
            verifyTestResults(test);
        }
    }
    private void verifyTestResults(Test test) {
        boolean testRanOk = false;
        String numberOfTestsRan = "unknown";
        for (String line : testOutputLines) {
            if (line.startsWith("OK")) {
                testRanOk = true;
                int startIndex = 4; 
                int endIndex = line.indexOf(' ', 4);
                numberOfTestsRan = line.substring(4, endIndex);
                break;
            }
        }
        if (!testRanOk) {
            test.setResult(new CtsTestResult(CtsTestResult.CODE_FAIL, null, null));
        } else {
            test.setResult(new CtsTestResult(CtsTestResult.CODE_PASS,
                            numberOfTestsRan + " tests passed", null));
        }
    }
    private static final String REF_APP_COMMAND_COMPONENT = "ReferenceAppTestCase";
    private static final String TAKE_SNAPSHOT_CMD = "takeSnapshot";
    private void runTests(final TestDevice device,
            final TestSessionLog testSessionLog) throws DeviceDisconnectedException {
        Log.i("Running reference tests for " + apkToTestName);
        device.addMainLogListener(new ILogListener() {
            public void newData(byte[] data, int offset, int length) {
            }
            public void newEntry(LogEntry entry) {
                String component = "";
                String msg = "";
                for (int i = 1; i < entry.len; i++) {
                    if (entry.data[i] == 0) {
                        component = new String(entry.data, 1, i - 1);
                        msg = new String(entry.data, i + 1, entry.len - i - 2);
                        if (msg.endsWith("\n")) {
                            msg = msg.substring(0, msg.length() - 1);
                        }
                        break;
                    }
                }
                if (REF_APP_COMMAND_COMPONENT.equals(component)) {
                    String[] parts = msg.split(":", 2);
                    if (parts == null ||
                        parts.length != 2) {
                        Log.e("Got reference app command component with invalid cmd: " + msg,
                                null);
                        return;
                    }
                    String cmd = parts[0];
                    String cmdArgs = parts[1];
                    if (TAKE_SNAPSHOT_CMD.equals(cmd)) {
                        takeSnapshot(device, testSessionLog, cmdArgs);
                    }
                }
            }
            private void takeSnapshot(TestDevice device,
                                      TestSessionLog testSessionLog,
                                      String cmdArgs) {
                try {
                    RawImage rawImage = device.getScreenshot();
                    if (rawImage != null) {
                    String outputFilename = testSessionLog.getResultDir() +
                        File.separator + cmdArgs + ".png";
                    File output = new File(outputFilename);
                    BufferedImage im = HostUtils.convertRawImageToBufferedImage(rawImage);
                    ImageIO.write(im, "png", output);
                    } else {
                        Log.e("getScreenshot returned a null image", null);
                    }
                } catch (IOException e) {
                    Log.e("Error taking snapshot! " + cmdArgs, e);
                }
            }
        });
        final String commandStr = "am instrument -w -e package "+ getAppPackageName() + " "
        + getAppPackageName() + "/" + getInstrumentationRunner();
        Log.d(commandStr);
        device.startActionTimer(ACTION_REFERENCE_APP_TEST);
        device.executeShellCommand(commandStr, new ReferenceAppResultsObserver(device));
        device.waitForCommandFinish();
    }
    class ReferenceAppResultsObserver extends MultiLineReceiver {
        private final TestDevice device;
        public ReferenceAppResultsObserver(TestDevice td) {
            this.device = td;
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                testOutputLines.add(line);
            }
        }
        public boolean isCancelled() {
            return false;
        }
        @Override
        public void done() {
            device.stopActionTimer();
            device.notifyExternalTestComplete();
        }
    }
}
