public class TestDevice implements DeviceObserver {
    private static final String DEVICE_SETUP_APK = "TestDeviceSetup";
    private static final String DEVICE_SETUP_APP_PACKAGE_NAME = "android.tests.devicesetup";
    private static final String DEFAULT_TEST_RUNNER_NAME =
                                  "android.test.InstrumentationTestRunner";
    private static final String ACTION_INSTALL = "install";
    private static final String ACTION_UNINSTALL = "uninstall";
    private static final String ACTION_GET_DEV_INFO = "getDeviceInfo";
    private static final String sInstrumentResultExpr = "INSTRUMENTATION_RESULT: (\\S+)=(.+)";
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_BUSY = STATUS_IDLE + 1;
    public static final int STATUS_OFFLINE = STATUS_IDLE + 2;
    private static final String STATUS_STR_IDLE = "idle";
    private static final String STATUS_STR_IN_USE = "in use";
    private static final String STATUS_STR_OFFLINE = "offline";
    private static final int REBOOT_POLL_INTERVAL = 5000;
    private static final int REBOOT_POLL_COUNT = 10 * 60 * 1000 / REBOOT_POLL_INTERVAL;
    private static final int GETPROP_TIMEOUT = 5000;
    public static final Pattern INSTRUMENT_RESULT_PATTERN;
    private BatchModeResultParser mBatchModeResultParser;
    private DeviceObserver mDeviceObserver;
    private IDevice mDevice;
    private DeviceParameterCollector mDeviceInfo;
    private SyncService mSyncService;
    private PackageActionObserver mUninstallObserver;
    private int mStatus;
    private static HashMap<Integer, String> mStatusMap;
    private PackageActionTimer mPackageActionTimer;
    private ObjectSync mObjectSync;
    private MultiplexingLogListener logListener = new MultiplexingLogListener();
    private LogReceiver logReceiver = new LogReceiver(logListener);
    private class LogServiceThread extends Thread {
        @Override
        public void run() {
            try {
                mDevice.runLogService("main", logReceiver);
            } catch (IOException e) {
            }
        }
        public void cancelLogService() {
            logReceiver.cancel();
        }
    }
    private LogServiceThread logServiceThread;
    static {
        INSTRUMENT_RESULT_PATTERN = Pattern.compile(sInstrumentResultExpr);
        mStatusMap = new HashMap<Integer, String>();
        mStatusMap.put(STATUS_IDLE, STATUS_STR_IDLE);
        mStatusMap.put(STATUS_BUSY, STATUS_STR_IN_USE);
        mStatusMap.put(STATUS_OFFLINE, STATUS_STR_OFFLINE);
    }
    TestDevice(final String serialNumber) {
        mDeviceInfo = new DeviceParameterCollector();
        mDeviceInfo.setSerialNumber(serialNumber);
    }
    public TestDevice(IDevice device) {
        mDevice = device;
        try {
            mSyncService = mDevice.getSyncService();
        } catch (IOException e) {
        }
        mBatchModeResultParser = null;
        mUninstallObserver = new PackageActionObserver(ACTION_UNINSTALL);
        mStatus = STATUS_IDLE;
        mDeviceInfo = new DeviceParameterCollector();
        mPackageActionTimer = new PackageActionTimer();
        mObjectSync = new ObjectSync();
    }
    public DeviceParameterCollector getDeviceInfo()
                throws DeviceDisconnectedException, InvalidNameSpaceException,
                InvalidApkPathException {
        if (mDeviceInfo.size() == 0) {
            logServiceThread = new LogServiceThread();
            logServiceThread.start();
            genDeviceInfo();
        }
        return mDeviceInfo;
    }
    public void disableKeyguard () throws DeviceDisconnectedException {
        final String commandStr = "am broadcast -a android.tests.util.disablekeyguard";
        Log.d(commandStr);
        executeShellCommand(commandStr, new NullOutputReceiver());
    }
    public IDevice getDevice() {
        return mDevice;
    }
    class RestartPropReceiver extends MultiLineReceiver {
        private boolean mRestarted;
        private boolean mCancelled;
        private boolean mDone;
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.trim().equals("1")) {
                    mRestarted = true;
                }
            }
        }
        @Override
        public void done() {
            synchronized(this) {
                mDone = true;
                this.notifyAll();
            }
        }
        public boolean isCancelled() {
            return mCancelled;
        }
        boolean hasRestarted(long timeout) {
            try {
                synchronized (this) {
                    if (!mDone) {
                        this.wait(timeout);
                    }
                }
            } catch (InterruptedException e) {
            }
            mCancelled = true;
            return mRestarted;
        }
    }
    public boolean waitForBootComplete() throws DeviceDisconnectedException {
        Log.d("probe device status...");
        mDeviceInfo.set(DeviceParameterCollector.SERIAL_NUMBER, getSerialNumber());
        mObjectSync = new ObjectSync();
        DeviceObserver tmpDeviceObserver = mDeviceObserver;
        mDeviceObserver = this;
        int retries = 0;
        boolean success = false;
        while (!success && (retries < REBOOT_POLL_COUNT)) {
            Log.d("Waiting for device to complete boot");
            RestartPropReceiver rpr = new RestartPropReceiver();
            this.executeShellCommand("getprop dev.bootcomplete", rpr);
            success = rpr.hasRestarted(GETPROP_TIMEOUT);
            if (!success) {
                try {
                    Thread.sleep(REBOOT_POLL_INTERVAL);
                } catch (InterruptedException e) {
                }
                retries += 1;
            }
        }
        mDeviceObserver = tmpDeviceObserver;
        if (success) {
            Log.d("Device boot complete");
        }
        return success;
    }
    private void genDeviceInfo() throws DeviceDisconnectedException,
                InvalidNameSpaceException, InvalidApkPathException {
        mDeviceInfo.set(DeviceParameterCollector.SERIAL_NUMBER, getSerialNumber());
        Log.d("run device information collector");
        runDeviceInfoCollectorCommand();
        waitForCommandFinish();
    }
    public void uninstallDeviceSetupApp() throws DeviceDisconnectedException,
            InvalidNameSpaceException {
        DeviceObserver tmpDeviceObserver = mDeviceObserver;
        mDeviceObserver = this;
        Log.d("uninstall get info ...");
        uninstallAPK(DEVICE_SETUP_APP_PACKAGE_NAME);
        waitForCommandFinish();
        Log.d("uninstall device information collector successfully");
        mDeviceObserver = tmpDeviceObserver;
    }
    public void installDeviceSetupApp() throws DeviceDisconnectedException, InvalidApkPathException {
        String apkPath = HostConfig.getInstance().getCaseRepository().getApkPath(DEVICE_SETUP_APK);
        if (!HostUtils.isFileExist(apkPath)) {
            Log.e("File doesn't exist: " + apkPath, null);
            return;
        }
        Log.d("installing " + DEVICE_SETUP_APK + " apk");
        mObjectSync = new ObjectSync();
        DeviceObserver tmpDeviceObserver = mDeviceObserver;
        mDeviceObserver = this;
        Log.d("install get info ...");
        installAPK(apkPath);
        waitForCommandFinish();
        mDeviceObserver = tmpDeviceObserver;
    }
    private void runDeviceInfoCollectorCommand() throws DeviceDisconnectedException {
        final String commandStr = "am instrument -w -e bundle true "
            + String.format("%s/android.tests.getinfo.DeviceInfoInstrument",
                    DEVICE_SETUP_APP_PACKAGE_NAME);
        Log.d(commandStr);
        mPackageActionTimer.start(ACTION_GET_DEV_INFO, this);
        executeShellCommand(commandStr, new DeviceInfoReceiver(mDeviceInfo));
    }
    final class DeviceInfoReceiver extends MultiLineReceiver {
        private ArrayList<String> mResultLines = new ArrayList<String>();
        private DeviceParameterCollector mDeviceParamCollector;
        public DeviceInfoReceiver(DeviceParameterCollector paramCollector) {
            super();
            mDeviceParamCollector = paramCollector;
            setTrimLine(false);
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                mResultLines.add(line);
            }
        }
        public boolean isCancelled() {
            return false;
        }
        @Override
        public void done() {
            super.done();
            String key, value;
            for (String line : mResultLines) {
                Matcher matcher = INSTRUMENT_RESULT_PATTERN.matcher(line);
                if (matcher.matches()) {
                    key = matcher.group(1);
                    value = matcher.group(2);
                    mDeviceParamCollector.set(key, value);
                }
            }
            synchronized(mObjectSync) {
                mObjectSync.sendNotify();
            }
        }
    }
    public static final class DeviceParameterCollector{
        public static final String PRODUCT_NAME = "product_name";
        public static final String BUILD_VERSION = "version_release";
        public static final String BUILD_ID = "build_id";
        public static final String BUILD_FINGERPRINT = "build_fingerprint";
        public static final String BUILD_TAGS = "build_tags";
        public static final String BUILD_TYPE = "build_type";
        public static final String BUILD_MODEL = "build_model";
        public static final String BUILD_BRAND = "build_brand";
        public static final String BUILD_BOARD = "build_board";
        public static final String BUILD_DEVICE = "build_device";
        public static final String BUILD_ABI = "build_abi";
        public static final String BUILD_ABI2 = "build_abi2";
        public static final String SCREEN_HEIGHT = "screen_height";
        public static final String SCREEN_WIDTH = "screen_width";
        public static final String SCREEN_DENSITY = "screen_density";
        public static final String SERIAL_NUMBER = "serialNumber";
        public static final String VERSION_SDK = "version_sdk";
        public static final String LOCALES = "locales";
        public static final String SCREEN_Y_DENSITY = "screen_Y_density";
        public static final String SCREEN_X_DENSITY = "screen_X_density";
        public static final String TOUCH_SCREEN = "touch_screen";
        public static final String NAVIGATION = "navigation";
        public static final String KEYPAD = "keypad";
        public static final String NETWORK = "network";
        public static final String IMEI = "imei";
        public static final String IMSI = "imsi";
        public static final String PHONE_NUMBER = "phoneNumber";
        private HashMap<String, String> mInfoMap;
        public DeviceParameterCollector() {
            mInfoMap = new HashMap<String, String>();
        }
        public void set(final String key, final String value) {
            mInfoMap.put(key, value);
        }
        public int size() {
            return mInfoMap.size();
        }
        public void setBuildFingerPrint(final String buildFingerPrint) {
            mInfoMap.put(BUILD_FINGERPRINT, buildFingerPrint);
        }
        public void setBuildTags(final String buildTags) {
            mInfoMap.put(BUILD_TAGS, buildTags);
        }
        public void setBuildType(final String buildType) {
            mInfoMap.put(BUILD_TYPE, buildType);
        }
        public void setBuildModel(final String buildModel) {
            mInfoMap.put(BUILD_MODEL, buildModel);
        }
        public void setBuildBrand(final String buildBrand) {
            mInfoMap.put(BUILD_BRAND, buildBrand);
        }
        public void setBuildBoard(final String buildBoard) {
            mInfoMap.put(BUILD_BOARD, buildBoard);
        }
        public void setBuildDevice(final String buildDevice) {
            mInfoMap.put(BUILD_DEVICE, buildDevice);
        }
        public void setBuildAbi(final String buildAbi) {
            mInfoMap.put(BUILD_ABI, buildAbi);
        }
        public void setBuildAbi2(final String buildAbi2) {
            mInfoMap.put(BUILD_ABI2, buildAbi2);
        }
        public void setSerialNumber(final String serialNumber) {
            mInfoMap.put(SERIAL_NUMBER, serialNumber);
        }
        public void setBuildId(final String bldId) {
            mInfoMap.put(BUILD_ID, bldId);
        }
        public void setBuildVersion(final String bldVer) {
            mInfoMap.put(BUILD_VERSION, bldVer);
        }
        public void setProductName(final String productName) {
            mInfoMap.put(PRODUCT_NAME, productName);
        }
        public String getBuildFingerPrint() {
            return mInfoMap.get(BUILD_FINGERPRINT);
        }
        public String getBuildTags() {
            return mInfoMap.get(BUILD_TAGS);
        }
        public String getBuildType() {
            return mInfoMap.get(BUILD_TYPE);
        }
        public String getBuildModel() {
            return mInfoMap.get(BUILD_MODEL);
        }
        public String getBuildBrand() {
            return mInfoMap.get(BUILD_BRAND);
        }
        public String getBuildBoard() {
            return mInfoMap.get(BUILD_BOARD);
        }
        public String getBuildDevice() {
            return mInfoMap.get(BUILD_DEVICE);
        }
        public String getBuildAbi() {
            return mInfoMap.get(BUILD_ABI);
        }
        public String getBuildAbi2() {
            return mInfoMap.get(BUILD_ABI2);
        }
        public String getBuildId() {
            return mInfoMap.get(BUILD_ID);
        }
        public String getBuildVersion() {
            return mInfoMap.get(BUILD_VERSION);
        }
        public String getProductName() {
            return mInfoMap.get(PRODUCT_NAME);
        }
        public String getSerialNumber() {
            return mInfoMap.get(SERIAL_NUMBER);
        }
        public String getScreenResolution() {
            return mInfoMap.get(SCREEN_WIDTH) + "x" + mInfoMap.get(SCREEN_HEIGHT);
        }
        public String getAndroidPlatformVersion() {
            return mInfoMap.get(VERSION_SDK);
        }
        public String getLocales() {
            return mInfoMap.get(LOCALES);
        }
        public String getXdpi() {
            return mInfoMap.get(SCREEN_X_DENSITY);
        }
        public String getYdpi() {
            return mInfoMap.get(SCREEN_Y_DENSITY);
        }
        public String getTouchInfo() {
            return mInfoMap.get(TOUCH_SCREEN);
        }
        public String getNavigation() {
            return mInfoMap.get(NAVIGATION);
        }
        public String getKeypad() {
            return mInfoMap.get(KEYPAD);
        }
        public String getNetwork() {
            return mInfoMap.get(NETWORK);
        }
        public String getIMEI() {
            return mInfoMap.get(IMEI);
        }
        public String getIMSI() {
            return mInfoMap.get(IMSI);
        }
        public String getPhoneNumber() {
            return mInfoMap.get(PHONE_NUMBER);
        }
    }
    public String getSerialNumber() {
        if (mDevice == null) {
            return mDeviceInfo.getSerialNumber();
        }
        return mDevice.getSerialNumber();
    }
    public void runTest(Test test) throws DeviceDisconnectedException {
        final String appNameSpace = test.getAppNameSpace();
        String runner = test.getInstrumentationRunner();
        if (runner == null) {
            runner = DEFAULT_TEST_RUNNER_NAME;
        }
        final String testName = test.getFullName().replaceAll("\\$", "\\\\\\$");
        final String commandStr = "am instrument -w -r -e class "
                + testName + " " + appNameSpace + "/" + runner;
        Log.d(commandStr);
        executeShellCommand(commandStr, new IndividualModeResultParser(test));
    }
    public void runInBatchMode(TestPackage testPackage, final String javaPkgName)
                throws DeviceDisconnectedException {
        String appNameSpace = testPackage.getAppNameSpace();
        String runner = testPackage.getInstrumentationRunner();
        if (runner == null) {
            runner = DEFAULT_TEST_RUNNER_NAME;
        }
        String name = testPackage.getAppPackageName();
        if ((javaPkgName != null) && (javaPkgName.length() != 0)) {
            name = javaPkgName;
        }
        String cmdHeader = "am instrument -w -r -e package " + name + " ";
        final String commandStr = cmdHeader + appNameSpace + "/" + runner;
        Log.d(commandStr);
        mBatchModeResultParser = new BatchModeResultParser(testPackage);
        executeShellCommand(commandStr, mBatchModeResultParser);
    }
    public void runTestCaseInBatchMode(TestPackage testPackage, final String javaClassName)
                throws DeviceDisconnectedException {
        if (javaClassName == null) {
            return;
        }
        String appNameSpace = testPackage.getAppNameSpace();
        String runner = testPackage.getInstrumentationRunner();
        if (runner == null) {
            runner = DEFAULT_TEST_RUNNER_NAME;
        }
        String cmdHeader = "am instrument -w -r -e class " + javaClassName + " ";
        final String commandStr = cmdHeader + appNameSpace + "/" + runner;
        Log.d(commandStr);
        mBatchModeResultParser = new BatchModeResultParser(testPackage);
        executeShellCommand(commandStr, mBatchModeResultParser);
    }
    public Client[] getClients() {
        return mDevice.getClients();
    }
    public void pushFile(String localPath, String remotePath) {
        SyncResult result = mSyncService.pushFile(localPath, remotePath,
                new PushMonitor());
        if (result.getCode() != SyncService.RESULT_OK) {
            Log.e("Uploading file failed: " + result.getMessage(), null);
        }
    }
    public void installAPK(final String apkPath) throws DeviceDisconnectedException,
                InvalidApkPathException {
        if ((apkPath == null) || (apkPath.length() == 0) || (!HostUtils.isFileExist(apkPath))) {
            throw new InvalidApkPathException(apkPath);
        }
        final String cmd = DeviceManager.getAdbLocation() + " -s "
                + getSerialNumber() + " install -r " + apkPath;
        Log.d(cmd);
        mPackageActionTimer.start(ACTION_INSTALL, this);
        executeCommand(cmd, new PackageActionObserver(ACTION_INSTALL));
    }
    private void executeCommand(String command, StdOutObserver stdOutReceiver)
                    throws DeviceDisconnectedException {
        if (mStatus != STATUS_OFFLINE) {
            try {
                Process proc = Runtime.getRuntime().exec(command);
                if (stdOutReceiver != null) {
                    stdOutReceiver.setInputStream(proc.getInputStream());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new DeviceDisconnectedException(getSerialNumber());
        }
    }
    interface StdOutObserver {
        public void setInputStream(InputStream is);
        public void processLines() throws IOException;
    }
    public void uninstallAPK(String packageName) throws DeviceDisconnectedException,
                InvalidNameSpaceException {
        if ((packageName == null) || (packageName.length() == 0)) {
            throw new InvalidNameSpaceException(packageName);
        }
        uninstallAPKImpl(packageName, mUninstallObserver);
    }
    private void uninstallAPKImpl(final String packageName, final PackageActionObserver observer)
                throws DeviceDisconnectedException {
        final String cmdStr = DeviceManager.getAdbLocation() + " -s "
                      + getSerialNumber() + " uninstall " + packageName;
        Log.d(cmdStr);
        mPackageActionTimer.start(ACTION_UNINSTALL, this);
        executeCommand(cmdStr, observer);
    }
    class PackageActionTimeoutTask extends TimerTask {
        private String mAction;
        private TestDevice mTargetDevice;
        public PackageActionTimeoutTask(final String action,
                TestDevice testDevice) {
            mAction = action;
            mTargetDevice = testDevice;
        }
        @Override
        public void run() {
            Log.d("PackageActionTimeoutTask.run(): mAction=" + mAction);
            synchronized (mObjectSync) {
                mObjectSync.sendNotify();
            }
            if (mAction.toLowerCase().equals(ACTION_INSTALL)) {
                mDeviceObserver.notifyInstallingTimeout(mTargetDevice);
            } else if (mAction.toLowerCase().equals(ACTION_UNINSTALL)) {
                mDeviceObserver.notifyUninstallingTimeout(mTargetDevice);
            } else if (mAction.toLowerCase().equals(ACTION_GET_DEV_INFO)) {
                Log.e("Get device information timeout", null);
            } else {
                Log.e("Timeout: " + mAction, null);
            }
        }
    }
    class PackageActionTimer {
        private Timer mTimer;
        private void start(final String action, final TestDevice device) {
            start(action, HostConfig.Ints.packageInstallTimeoutMs.value(), device);
        }
        private void start(final String action, final int timeout, final TestDevice device) {
            Log.d("start(), action=" + action + ",mTimer=" + mTimer + ",timeout=" + timeout);
            synchronized (this) {
                if (mTimer != null) {
                    mTimer.cancel();
                }
                mTimer = new Timer();
                mTimer.schedule(new PackageActionTimeoutTask(action, device), timeout);
            }
        }
        private void stop() {
            synchronized (this) {
                Log.d("stop() , mTimer=" + mTimer);
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        }
    }
    final class PackageActionObserver implements StdOutObserver, Runnable {
        private BufferedReader mReader;
        private String mAction;
        public PackageActionObserver(final String action) {
            mAction = action;
        }
        public void run() {
            try {
                processLines();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    mReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void processLines() throws IOException {
            String line = mReader.readLine();
            int statusCode = DeviceObserver.FAIL;
            boolean gotResult = false;
            while (line != null) {
                line = line.toLowerCase();
                if (line.indexOf("success") != -1) {
                    statusCode = DeviceObserver.SUCCESS;
                    gotResult = true;
                } else if (line.indexOf("failure") != -1) {
                    statusCode = DeviceObserver.FAIL;
                    CUIOutputStream.println(mAction.toLowerCase() + " met " + line);
                    gotResult = true;
                } else if (line.indexOf("error") != -1) {
                    CUIOutputStream.println(mAction.toLowerCase() + " met " + line);
                    statusCode = DeviceObserver.FAIL;
                    gotResult = true;
                }
                if (gotResult) {
                    Log.d(mAction + " calls stopPackageActionTimer()");
                    mPackageActionTimer.stop();
                    if (mDeviceObserver != null) {
                        mDeviceObserver.notifyInstallingComplete(statusCode);
                    }
                    break;
                }
                line = mReader.readLine();
            }
        }
        public void setInputStream(InputStream is) {
            mReader = new BufferedReader(new InputStreamReader(is));
            new Thread(this).start();
        }
    }
    abstract class RawModeResultParser extends MultiLineReceiver {
        public final static String EQ_MARK = "=";
        public final static String COMMA_MARK = ":";
        public final static String AT_MARK = "at ";
        public final static String STATUS_STREAM = "INSTRUMENTATION_STATUS: stream=";
        public final static String STATUS_TEST = "INSTRUMENTATION_STATUS: test=";
        public final static String STATUS_CLASS = "INSTRUMENTATION_STATUS: class=";
        public final static String STATUS_CODE = "INSTRUMENTATION_STATUS_CODE:";
        public final static String STATUS_STACK = "INSTRUMENTATION_STATUS: stack=";
        public final static String STATUS_CURRENT = "INSTRUMENTATION_STATUS: current=";
        public final static String STATUS_NUM = "INSTRUMENTATION_STATUS: numtests=";
        public final static String STATUS_ERROR_STR = "INSTRUMENTATION_STATUS: Error=";
        public final static String FAILURE = "Failure in ";
        public final static String ASSERTION = "junit.framework.Assertion";
        public final static String RESULT_STREAM = "INSTRUMENTATION_RESULT: stream=";
        public final static String RESULT_CODE = "INSTRUMENTATION_CODE:";
        public final static String RESULT = "Test results";
        public final static String RESULT_TIME = "Time:";
        public final static String RESULT_SUMMARY = "Tests run:";
        public final static int STATUS_STARTING = 1;
        public final static int STATUS_PASS = 0;
        public final static int STATUS_FAIL = -1;
        public final static int STATUS_ERROR = -2;
        private ArrayList<String> mResultLines;
        public String mStackTrace;
        public String mFailedMsg;
        public int mResultCode;
        public Test mTest;
        public RawModeResultParser(Test test) {
            super();
            setTrimLine(false);
            mTest = test;
            mResultLines = new ArrayList<String>();
            mStackTrace = null;
            mFailedMsg = null;
            mResultCode = CtsTestResult.CODE_PASS;
        }
        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                processNewLine(line.trim());
            }
        }
        abstract public void processNewLine(final String line);
        public ArrayList<String> getResultLines() {
            return mResultLines;
        }
        public String getNamedString(String mark) {
            for (String line : mResultLines) {
                if (line.startsWith(mark)) {
                    String name = line.substring(line.indexOf(EQ_MARK) + 1);
                    return name.trim();
                }
            }
            return null;
        }
        public int parseIntWithMark(String mark) {
            for (String line : mResultLines) {
                if (line.startsWith(mark)) {
                    String code = line.substring(line.indexOf(EQ_MARK) + 1);
                    return Integer.parseInt(code.trim());
                }
            }
            return 0;
        }
        public String getFailedMessage() {
            Iterator<String> iterator = mResultLines.iterator();
            while (iterator.hasNext()) {
                String line = iterator.next();
                if (line.startsWith(STATUS_STACK)) {
                    String failedMsg = line.substring(STATUS_STACK.length());
                    if (iterator.hasNext()) {
                        failedMsg += " " + iterator.next();
                    }
                    return failedMsg;
                }
            }
            return null;
        }
        public String getStackTrace() {
            StringBuilder sb = new StringBuilder();
            for (String line : mResultLines) {
                line = line.trim();
                if (line.startsWith(AT_MARK) && line.endsWith(")")) {
                    sb.append(line + "\n");
                }
            }
            return sb.toString();
        }
        public int getStatusCode(String line) {
            String codeStr = line.substring(line.indexOf(COMMA_MARK) + 1);
            return Integer.parseInt(codeStr.trim());
        }
        public boolean isCancelled() {
            return false;
        }
        @Override
        public void done() {
            super.done();
        }
    }
    final class IndividualModeResultParser extends RawModeResultParser {
        public IndividualModeResultParser(Test test) {
            super(test);
        }
        @Override
        public void processNewLine(final String line) {
            if ((line == null) || (line.trim().length() == 0)) {
                return;
            }
            ArrayList<String> resultLines = getResultLines();
            resultLines.add(line);
            if (line.startsWith(STATUS_CODE)) {
                int statusCode = getStatusCode(line);
                processTestResult(statusCode);
                resultLines.removeAll(resultLines);
            }
        }
        public void processTestResult(int statusCode) {
            String testName = getNamedString(STATUS_TEST);
            String className = getNamedString(STATUS_CLASS);
            String testFullName = className + Test.METHOD_SEPARATOR + testName;
            String errorMessage = getNamedString(STATUS_ERROR_STR);
            mFailedMsg = null;
            mStackTrace = null;
            if ((statusCode == STATUS_FAIL) || (statusCode == STATUS_ERROR)) {
                mFailedMsg = getFailedMessage();
                mStackTrace = getStackTrace();
            }
            if ((errorMessage != null) && (errorMessage.length() != 0)) {
                if (mFailedMsg == null) {
                    mFailedMsg = errorMessage;
                } else {
                    mFailedMsg += " : " + errorMessage;
                }
            }
            Log.d(testFullName + "...(" + statusCode + ")");
            Log.d("errorMessage= " + errorMessage);
            Log.d("mFailedMsg=" + mFailedMsg);
            Log.d("mStackTrace=" + mStackTrace);
            switch (statusCode) {
            case STATUS_STARTING:
                break;
            case STATUS_PASS:
                mResultCode = CtsTestResult.CODE_PASS;
                break;
            case STATUS_FAIL:
            case STATUS_ERROR:
                mResultCode = CtsTestResult.CODE_FAIL;
                break;
            }
        }
        @Override
        public void done() {
            mTest.notifyResult(new CtsTestResult(mResultCode, mFailedMsg, mStackTrace));
            super.done();
        }
    }
    final class BatchModeResultParser extends RawModeResultParser {
        private TestPackage mTestPackage;
        private Collection<Test> mTests;
        public int mCurrentTestNum;
        public int mTotalNum;
        public BatchModeResultParser(TestPackage testPackage) {
            super(null);
            mTestPackage = testPackage;
            if (mTestPackage != null) {
                mTests = mTestPackage.getTests();
            }
        }
        @Override
        public void processNewLine(final String line) {
            if ((line == null) || (line.trim().length() == 0)) {
                return;
            }
            ArrayList<String> resultLines = getResultLines();
            resultLines.add(line);
            if (line.startsWith(STATUS_CODE)) {
                int statusCode = getStatusCode(line);
                processTestResult(statusCode);
                resultLines.removeAll(resultLines);
            } else if (line.startsWith(RESULT_CODE)) {
                int resultCode = getStatusCode(line);
                switch(resultCode) {
                case STATUS_STARTING:
                    break;
                case STATUS_FAIL:
                case STATUS_ERROR:
                    mResultCode = CtsTestResult.CODE_FAIL;
                    break;
                case STATUS_PASS:
                    mResultCode = CtsTestResult.CODE_PASS;
                    break;
                }
                resultLines.removeAll(resultLines);
            }
        }
        public void processTestResult(int statusCode) {
            String testName = getNamedString(STATUS_TEST);
            String className = getNamedString(STATUS_CLASS);
            String testFullName = className + Test.METHOD_SEPARATOR + testName;
            mCurrentTestNum = parseIntWithMark(STATUS_CURRENT);
            mTotalNum = parseIntWithMark(STATUS_NUM);
            mFailedMsg = null;
            mStackTrace = null;
            if ((statusCode == STATUS_FAIL) || ((statusCode == STATUS_ERROR))) {
                mFailedMsg = getFailedMessage();
                mStackTrace = getStackTrace();
            }
            Log.d(testFullName + "...(" + statusCode + ")");
            Log.d("mFailedMsg=" + mFailedMsg);
            Log.d("mStackTrace=" + mStackTrace);
            String status = TestPackage.FINISH;
            if (statusCode == STATUS_STARTING) {
                status = TestPackage.START;
            }
            mTest = searchTest(testFullName);
            if (mTest != null) {
                switch(statusCode) {
                case STATUS_STARTING:
                    status = TestPackage.START;
                    break;
                case STATUS_PASS:
                    mTest.setResult(new CtsTestResult(
                            CtsTestResult.CODE_PASS, null, null));
                    break;
                case STATUS_ERROR:
                case STATUS_FAIL:
                    mTest.setResult(new CtsTestResult(
                            CtsTestResult.CODE_FAIL, mFailedMsg, mStackTrace));
                    break;
                }
            }
            mTestPackage.notifyTestStatus(mTest, status);
        }
        private Test searchTest(String testFullName) {
            for (Test test : mTests) {
                if (testFullName.equals(test.getFullName())) {
                    return test;
                }
            }
            return null;
        }
        @Override
        public void done() {
            mTestPackage.notifyBatchModeFinish();
            super.done();
        }
    }
    public void removeRuntimeListener() {
        mDeviceObserver = null;
    }
    public void setRuntimeListener(DeviceObserver listener) {
        mDeviceObserver = listener;
    }
    class PushMonitor implements ISyncProgressMonitor {
        public PushMonitor() {
        }
        public void advance(int arg0) {
        }
        public boolean isCanceled() {
            return false;
        }
        public void start(int arg0) {
        }
        public void startSubTask(String arg0) {
        }
        public void stop() {
        }
    }
    public void addMainLogListener(ILogListener listener) {
        logListener.addListener(listener);
    }
    public void removeMainLogListener(ILogListener listener) {
        logListener.removeListener(listener);
    }
    public void executeShellCommand(final String cmd,
            final IShellOutputReceiver receiver) throws DeviceDisconnectedException {
        executeShellCommand(cmd, receiver, null);
    }
    public void executeShellCommand(final String cmd,
            final IShellOutputReceiver receiver,
            final LogReceiver logReceiver)
            throws DeviceDisconnectedException {
        if (mStatus == STATUS_OFFLINE) {
            Log.d(String.format("device %s is offline when attempting to execute %s",
                    getSerialNumber(), cmd));
            throw new DeviceDisconnectedException(getSerialNumber());
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    mDevice.executeShellCommand(cmd, receiver);
                } catch (IOException e) {
                    Log.e("", e);
                }
            }
        }.start();
    }
    public void killProcess(String packageName) {
        if (mStatus == STATUS_OFFLINE) {
            return;
        }
        Client[] clients = mDevice.getClients();
        for (Client c : clients) {
            ClientData cd = c.getClientData();
            if (cd.getClientDescription() == null) {
                continue;
            }
            if (cd.getClientDescription().equals(packageName)) {
                c.kill();
                break;
            }
        }
    }
    public void disconnected() {
        CUIOutputStream.println("Device(" + getSerialNumber() + ") disconnected");
        mDevice = null;
        mSyncService = null;
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            mPackageActionTimer.stop();
        }
        if (mStatus == STATUS_BUSY) {
            Log.d("TestDevice.disconnected calls notifyTestingDeviceDisconnected");
            mDeviceObserver.notifyTestingDeviceDisconnected();
        } else {
            if (!TestSession.isADBServerRestartedMode()) {
                CUIOutputStream.printPrompt();
            }
        }
        setStatus(STATUS_OFFLINE);
        if (logServiceThread != null) {
            logServiceThread.cancelLogService();
        }
    }
    public void setStatus(final int statusCode) {
        if (statusCode != STATUS_IDLE && statusCode != STATUS_BUSY
                && statusCode != STATUS_OFFLINE) {
            throw new IllegalArgumentException("Invalid status code");
        }
        mStatus = statusCode;
    }
    public int getStatus() {
        return mStatus;
    }
    public String getStatusAsString() {
        return mStatusMap.get(mStatus);
    }
    public void waitForCommandFinish() {
        synchronized (mObjectSync) {
            try {
                mObjectSync.waitOn();
            } catch (InterruptedException e) {
            }
        }
    }
    void startActionTimer(final String action, final int timeout) {
        mPackageActionTimer.start(action, timeout, this);
    }
    void startActionTimer(String action) {
       mPackageActionTimer.start(action, this);
    }
    void stopActionTimer() {
        mPackageActionTimer.stop();
    }
    void notifyExternalTestComplete() {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
        }
    }
    public void notifyInstallingComplete(int resultCode) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            mPackageActionTimer.stop();
        }
    }
    public void notifyInstallingTimeout(TestDevice testDevice) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
        }
    }
    public void notifyTestingDeviceDisconnected() {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            if (mPackageActionTimer != null) {
                mPackageActionTimer.stop();
            }
        }
    }
    public void notifyUninstallingComplete(int resultCode) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
            mPackageActionTimer.stop();
        }
    }
    public void notifyUninstallingTimeout(TestDevice testDevice) {
        synchronized (mObjectSync) {
            mObjectSync.sendNotify();
        }
    }
    class ObjectSync {
        private boolean mNotifySent = false;
        public void sendNotify() {
            Log.d("ObjectSync.sendNotify() is called, mNotifySent=" + mNotifySent);
            mNotifySent = true;
            notify();
        }
        public void waitOn() throws InterruptedException {
            Log.d("ObjectSync.waitOn() is called, mNotifySent=" + mNotifySent);
            if (!mNotifySent) {
                wait();
            }
            mNotifySent = false;
        }
        public boolean isNotified() {
            return mNotifySent;
        }
    }
    public RawImage getScreenshot() throws IOException {
        return mDevice.getScreenshot();
    }
}
