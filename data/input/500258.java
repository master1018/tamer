public class TestPackage implements DeviceObserver {
    protected static final String PKG_LOG_SEPARATOR =
              "==============================================================";
    public static final String FINISH = "finish";
    public static final String START = "start";
    private String mName, mVersion, mAndroidVersion;
    private String mTargetNameSpace, mTargetBinaryName, mInstrumentationRunner;
    private Collection<TestSuite> mSuites;
    private String mDigest;
    private String mJarPath;
    private String mAppNameSpace;
    private String mAppPackageName;
    protected TestSuite mCurrentTestSuite;
    protected TestDevice mDevice;
    protected boolean mTestStop;
    private TestSessionThread mTestThread;
    private HostTimer mTimeOutTimer;
    private ProgressObserver mProgressObserver;
    private boolean mIsInBatchMode;
    private Test mCurrentTest;
    public TestPackage(final String instrumentationRunner,
            final String testPkgBinaryName, final String targetNameSpace,
            final String targetBinaryName, final String version,
            final String androidVersion, final String jarPath, final String appNameSpace,
            final String appPackageName) {
        mInstrumentationRunner = instrumentationRunner;
        mName = testPkgBinaryName;
        mTargetNameSpace = targetNameSpace;
        mTargetBinaryName = targetBinaryName;
        mVersion = version;
        mAndroidVersion = androidVersion;
        mSuites = new ArrayList<TestSuite>();
        mJarPath = jarPath;
        mAppNameSpace = appNameSpace;
        mAppPackageName = appPackageName;
        mDevice = null;
        mTestStop = false;
        mTestThread = null;
        mIsInBatchMode = false;
        mCurrentTest = null;
    }
    public String getAppNameSpace() {
        return mAppNameSpace;
    }
    public String getAppPackageName() {
        return mAppPackageName;
    }
    public boolean isHostSideOnly() {
        return false;
    }
    public void addTestSuite(final TestSuite suite) {
        mSuites.add(suite);
    }
    public Collection<TestSuite> getTestSuites() {
        return mSuites;
    }
    public TestSuite getTestSuiteByName(final String suiteFullName) {
        for (TestSuite suite : getAllTestSuites()) {
            if (suite.getFullName().equals(suiteFullName)) {
                return suite;
            }
        }
        return null;
    }
    public TestCase getTestCaseByName(final String testCaseFullName) {
        for (TestCase testCase : getAllTestCases()) {
            if (testCase.getFullName().equals(testCaseFullName)) {
                return testCase;
            }
        }
        return null;
    }
    public Collection<TestSuite> getAllTestSuites() {
        Collection<TestSuite> suites = new ArrayList<TestSuite>();
        for (TestSuite suite : mSuites) {
            suites.addAll(suite.getAllSuites());
        }
        return suites;
    }
    public void getTestSuiteNames(final String expectName,
            List<String> suiteNameList, List<String> caseNameList) {
        for (TestCase testCase : getAllTestCases()) {
            String testCaseName = testCase.getFullName();
            if (testCaseName.startsWith(expectName)) {
                String suiteName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
                if (suiteName.equals(expectName)) {
                    if (!caseNameList.contains(testCaseName)) {
                        caseNameList.add(testCaseName);
                    }
                } else {
                    if (!suiteNameList.contains(suiteName)) {
                        suiteNameList.add(suiteName);
                    }
                }
            }
        }
    }
    public List<String> getAllTestSuiteNames() {
        List<String> suiteNameList = new ArrayList<String>();
        for (TestCase testCase : getAllTestCases()) {
            String testCaseName = testCase.getFullName();
            String suiteName = testCaseName.substring(0, testCaseName.lastIndexOf("."));
            if (!suiteNameList.contains(suiteName)) {
                suiteNameList.add(suiteName);
            }
        }
        return suiteNameList;
    }
    public List<String> getAllTestCaseNames(final String suiteFullName) {
        List<String> caseNameList = new ArrayList<String>();
        TestSuite suite = getTestSuiteByName(suiteFullName);
        if (suite != null) {
            caseNameList.addAll(suite.getAllTestCaseNames());
        }
        return caseNameList;
    }
    public List<String> getAllTestNames(final String testCaseFullName) {
        List<String> testNameList = new ArrayList<String>();
        TestCase testCase = getTestCaseByName(testCaseFullName);
        if (testCase != null) {
            testNameList.addAll(testCase.getAllTestNames());
        }
        return testNameList;
    }
    public void getTestCaseNames(final String expectPackage, List<String> caseList,
            List<String> testList) {
        for (TestCase testCase : getAllTestCases()) {
            String testCaseName = testCase.getFullName();
            if (testCaseName.equals(expectPackage)) {
                for (Test test : testCase.getTests()) {
                    testList.add(test.getFullName());
                }
                return;
            } else if (testCaseName.startsWith(expectPackage)) {
                caseList.add(testCaseName);
            }
        }
    }
    public void getTestNames(final String expectPackage, List<String> testList) {
        for (Test test : getTests()) {
            String testName = test.getFullName();
            if (testName.startsWith(expectPackage)) {
                testList.add(testName);
            }
        }
    }
    public String getAppBinaryName() {
        return mName;
    }
    public String getVersion() {
        return mVersion;
    }
    public String getAndroidVersion() {
        return mAndroidVersion;
    }
    public String getTargetNameSpace() {
        return mTargetNameSpace;
    }
    public String getTargetBinaryName() {
        return mTargetBinaryName;
    }
    public String getInstrumentationRunner() {
        return mInstrumentationRunner;
    }
    public Test searchTest(final String testName) {
        Test test = null;
        for (TestSuite suite : mSuites) {
            test = suite.searchTest(testName);
            if (test != null) {
                break;
            }
        }
        return test;
    }
    public Collection<Test> getTests() {
        List<Test> tests = new ArrayList<Test>();
        for (TestSuite s : mSuites) {
            tests.addAll(s.getTests());
        }
        return tests;
    }
    public Collection<TestCase> getAllTestCases() {
        List<TestCase> testCases = new ArrayList<TestCase>();
        for (TestSuite s : mSuites) {
            testCases.addAll(s.getAllTestCases());
        }
        return testCases;
    }
    private void setMessageDigest(final String digest) {
        mDigest = digest;
    }
    public String getMessageDigest() {
        return mDigest;
    }
    public String getJarPath() {
        return mJarPath;
    }
    public ArrayList<String> getExcludedList(final String resultType) {
        ArrayList<String> excludedList = new ArrayList<String>();
        ArrayList<String> fullNameList = new ArrayList<String>();
        for (TestSuite suite : getTestSuites()) {
            fullNameList.add(suite.getFullName());
            ArrayList<String> list = suite.getExcludedList(resultType);
            if ((list != null) && (list.size() > 0)) {
                excludedList.addAll(list);
            }
        }
        int count = 0;
        for (String fullName : fullNameList) {
            if (excludedList.contains(fullName)) {
                count ++;
            }
        }
        if (count == fullNameList.size()) {
            return null;
        }
        return excludedList;
    }
    protected void println(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.println(msg);
        }
    }
    protected void print(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.print(msg);
        }
    }
    public void notifyBatchModeFinish() {
        Log.d("TestPackage.notifyBatchModeFinish() is called, mTestStop=" + mTestStop);
        if (mTestStop) {
            return;
        }
        if (mIsInBatchMode) {
            if (mCurrentTest != null) {
                handleMissingFinishEvent();
            }
            synchronized (mTimeOutTimer) {
                mTimeOutTimer.sendNotify();
            }
        }
    }
    private void handleMissingFinishEvent() {
        mProgressObserver.stop();
        synchronized (mTimeOutTimer) {
            mTimeOutTimer.cancel(false);
        }
        mCurrentTest.setResult(new CtsTestResult(CtsTestResult.CODE_NOT_EXECUTED, null, null));
        mCurrentTest = null;
    }
    public void notifyTestStatus(final Test test, final String status) {
        if (mTestStop) {
            return;
        }
        if (mIsInBatchMode) {
            if (status.equals(START)) {
                if ((mCurrentTest != null) && (mCurrentTest.getResult().isNotExecuted())) {
                    Log.d("Err: Missing FINISH msg for test " + mCurrentTest.getFullName());
                    handleMissingFinishEvent();
                }
                mCurrentTest = test;
                if (test != null) {
                    print(mCurrentTest.getFullName() + "...");
                    mProgressObserver.start();
                }
            } else {
                mProgressObserver.stop();
                mCurrentTest = null;
            }
            mTimeOutTimer.restart(new TimeOutTask(this), 
                    HostConfig.Ints.testStatusTimeoutMs.value());
        }
    }
    public void notifyInstallingComplete(final int resultCode) {
        Log.d("notifyInstallingComplete() is called with resultCode=" + resultCode);
        sendNotify();
        if (resultCode == FAIL) {
            Log.d("install failed");
        }
    }
    public void notifyUninstallingComplete(final int resultCode) {
        Log.d("notifyUninstallingComplete() is called with resultCode=" + resultCode);
        sendNotify();
        if (resultCode == FAIL) {
            Log.d("uninstall failed");
        }
    }
    private void sendNotify() {
        synchronized (this) {
            notify();
        }
    }
    public void notifyInstallingTimeout(final TestDevice testDevice) {
        Log.d("TestPackage.notifyInstallingTimeout() is called");
        mTestStop = true;
        synchronized (this) {
            notify();
        }
        genPackageActionTimeoutCause(testDevice, "Installing");
    }
    public void notifyUninstallingTimeout(final TestDevice testDevice) {
        Log.d("TestPackage.notifyUninstallingTimeout() is called");
        mTestStop = true;
        synchronized (this) {
            notify();
        }
        genPackageActionTimeoutCause(testDevice, "Uninstalling");
    }
    private void genPackageActionTimeoutCause(final TestDevice testDevice, String type) {
        String cause;
        if (testDevice.getStatus() == TestDevice.STATUS_OFFLINE) {
            cause = testDevice.getSerialNumber() + " is offline.";
        } else {
            cause = "Unknown reason.";
        }
        if (type == null) {
            type = "Unknown timer";
        }
        Log.e(type + " met timeout due to " + cause, null);
    }
    public void notifyTestingDeviceDisconnected() {
        Log.d("busyDeviceDisconnected invoked");
        mTestStop = true;
        synchronized (this) {
            notify();
        }
        cleanUp();
        try {
            CUIOutputStream.println("Test stopped.");
            mTestThread.join();
        } catch (InterruptedException e) {
            Log.e("", e);
        }
    }
    public void setTestDevice(final TestDevice device) {
        mDevice = device;
        device.setRuntimeListener(this);
        device.setStatus(TestDevice.STATUS_BUSY);
    }
    private String getFullPath(String binaryFileName) {
        String packagePath = null;
        if ((binaryFileName != null) && (binaryFileName.length() != 0)) {
            packagePath = HostConfig.getInstance().getCaseRepository()
                .getApkPath(binaryFileName);
        }
        return packagePath;
    }
    private boolean install() throws DeviceDisconnectedException, InvalidApkPathException {
        String packageBinaryName = getAppBinaryName();
        String targetBinaryName = getTargetBinaryName();
        String packagePath = getFullPath(packageBinaryName);
        String targetApkPath = getFullPath(targetBinaryName);
        boolean success = true;
        if (packagePath != null) {
            installAPK(packagePath);
            if ((!mTestStop) && (targetApkPath != null)) {
                installAPK(targetApkPath);
            }
        } else {
            success = false;
            Log.e("The package binary name contains nothing!", null);
        }
        if (mTestStop) {
            success = false;
            println("Install package " + packageBinaryName + "failed");
        }
        return success;
    }
    private void uninstall() throws DeviceDisconnectedException, InvalidNameSpaceException {
        String testPkgBinaryName = getAppBinaryName();
        String appNameSpace = getAppNameSpace();
        String targetNameSpace = getTargetNameSpace();
        String packagePath = getFullPath(testPkgBinaryName);
        String targetApkPath = getFullPath(targetNameSpace);
        if ((packagePath != null) && HostUtils.isFileExist(packagePath)) {
            uninstallAPK(appNameSpace);
            if ((!mTestStop) && (targetNameSpace != null)
                    && ((targetApkPath != null) && (HostUtils.isFileExist(targetApkPath)))) {
                uninstallAPK(targetNameSpace);
            }
        }
    }
    private void uninstallAPK(final String packageName) throws DeviceDisconnectedException,
                InvalidNameSpaceException {
        Log.d("Uninstall: " + packageName);
        mDevice.uninstallAPK(packageName);
        waitPackageActionComplete();
    }
    private void installAPK(final String apkPath) throws DeviceDisconnectedException,
            InvalidApkPathException {
        Log.d("installAPK " + apkPath + " ...");
        mDevice.installAPK(apkPath);
        waitPackageActionComplete();
        Log.d("installAPK " + apkPath + " finish");
    }
    private void waitPackageActionComplete() {
        Log.d("Enter waitPackageActionComplete()");
        synchronized (this) {
            if (!mTestStop) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Log.e("", e);
                }
            }
        }
        try {
            Thread.sleep(HostConfig.Ints.postInstallWaitMs.value());
        } catch (InterruptedException e) {
            Log.e("", e);
        }
        Log.d("Leave waitPackageActionComplete()");
    }
    private String genMessageDigest(final String packagePath) throws IOException {
        final String algorithm = "SHA-1";
        FileInputStream fin = new FileInputStream(packagePath);
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fin.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fin.close();
            return HostUtils.toHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return algorithm + " not found";
        }
    }
    public void setSessionThread(TestSessionThread thread) {
        mTestThread = thread;
    }
    private boolean supportsBatchMode() {
        Collection<Test> tests = getTests();
        if (tests.size() > HostConfig.Ints.maxTestsInBatchMode.value()) {
            return false;
        }
        for (Test test : tests) {
            if (!test.getResult().isNotExecuted()) {
                return false;
            }
            if ((test.getTestController() != null)
                && (test.getTestController().getFullName() != null)) {
                return false;
            }
        }
        return true;
    }
     List<String> getPackageNames() {
        List<String> pkgNames = new ArrayList<String>();
        List<String> suiteNames = getAllTestSuiteNames();
        for (String suiteName : suiteNames) {
            String pkgSeg = suiteName;
            if (suiteName.contains(".")) {
                pkgSeg = suiteName.split("\\.")[0];
            }
            if (!pkgNames.contains(pkgSeg)) {
                pkgNames.add(pkgSeg);
            }
        }
        return pkgNames;
    }
    private void runInBatchMode(final String javaPkgName)
            throws DeviceDisconnectedException {
        mTimeOutTimer = new HostTimer(new TimeOutTask(this),
                HostConfig.Ints.batchStartTimeoutMs.value());
        mTimeOutTimer.start();
        mProgressObserver = new ProgressObserver();
        if ((javaPkgName != null) && (javaPkgName.length() > 0)) {
            runInBatchModeImpl(javaPkgName);
        } else {
            for (String pkgName : getPackageNames()) {
                runInBatchModeImpl(pkgName);
            }
        }
    }
    private void runInBatchModeImpl(String javaPkgName) throws DeviceDisconnectedException {
        mDevice.runInBatchMode(this, javaPkgName);
        synchronized (mTimeOutTimer) {
            if (!mTestStop) {
                try {
                    mTimeOutTimer.waitOn();
                } catch (InterruptedException e) {
                    Log.d("time out object interrupted");
                }
            }
            mProgressObserver.stop();
            if (mTimeOutTimer.isTimeOut()) {
                return;
            } else {
                mTimeOutTimer.cancel(false);
            }
        }
    }
    protected void runInIndividualMode(final String javaPkgName) throws IOException,
                    DeviceDisconnectedException, ADBServerNeedRestartException {
        Iterator<TestSuite> suites = getTestSuites().iterator();
        while (suites.hasNext() && (!mTestStop)) {
            mCurrentTestSuite = suites.next();
            mCurrentTestSuite.run(mDevice, javaPkgName);
        }
    }
    class TimeOutTask extends TimerTask {
        private TestPackage mTestPackage;
        public TimeOutTask(final TestPackage testPackage) {
            mTestPackage = testPackage;
        }
        @Override
        public void run() {
            mProgressObserver.stop();
            synchronized (mTimeOutTimer) {
                mTimeOutTimer.cancel(true);
                mTimeOutTimer.sendNotify();
            }
            if ((mIsInBatchMode) && (mCurrentTest != null)) {
                mCurrentTest.setResult(
                        new CtsTestResult(CtsTestResult.CODE_TIMEOUT, null, null));
                mCurrentTest = null;
            }
            Log.d("mTimeOutTimer timed out");
            killDeviceProcess(mTestPackage.getAppPackageName());
        }
    }
    private void killDeviceProcess(final String packageName) {
        mDevice.killProcess(packageName);
    }
    protected boolean isAllTestsRun(){
        for (Test test : getTests()) {
            if (test.getResult().isNotExecuted()) {
                return false;
            }
        }
        return true;
    }
    protected boolean noTestsExecuted() {
        for (Test test : getTests()) {
            if (!test.getResult().isNotExecuted()) {
                return false;
            }
        }
        return true;
    }
    public void run(final TestDevice device, final String javaPkgName,
                    TestSessionLog sessionLog)
            throws IOException, DeviceDisconnectedException,
            ADBServerNeedRestartException, InvalidApkPathException,
            InvalidNameSpaceException {
        if (isAllTestsRun()) {
            return;
        }
        setup(device, javaPkgName);
        runImpl(javaPkgName);
    }
    protected void runImpl(final String javaPkgName) throws IOException,
            DeviceDisconnectedException, ADBServerNeedRestartException, InvalidApkPathException,
            InvalidNameSpaceException {
        try {
            if (!install()) {
                return;
            }
            if (!mTestStop) {
                Log.d("install " + getAppBinaryName() + " succeed!");
                setMessageDigest(genMessageDigest(HostConfig.getInstance()
                        .getCaseRepository().getApkPath(getAppBinaryName())));
                if (supportsBatchMode()) {
                    mIsInBatchMode = true;
                    Log.d("run in batch mode...");
                    runInBatchMode(javaPkgName);
                    if (!isAllTestsRun()) {
                        mIsInBatchMode = false;
                        Log.d("run in individual mode");
                        runInIndividualMode(javaPkgName);
                    }
                } else {
                    Log.d("run in individual mode...");
                    runInIndividualMode(javaPkgName);
                }
            }
            if (!mTestStop) {
                uninstall();
                if (!TestSession.isADBServerRestartedMode()) {
                    println(PKG_LOG_SEPARATOR);
                }
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }
    protected void setup(final TestDevice device, final String javaPkgName) {
        if (!TestSession.isADBServerRestartedMode() || noTestsExecuted()) {
            println(PKG_LOG_SEPARATOR);
            if ((javaPkgName == null) || (javaPkgName.length() == 0)) {
                println("Test package: " + getAppPackageName());
            } else {
                println("Test java package contained in test package "
                        + getAppPackageName() + ": " + javaPkgName);
            }
        }
        mTestStop = false;
        mIsInBatchMode = false;
        mCurrentTest = null;
        mCurrentTestSuite = null;
        setTestDevice(device);
    }
    public void cleanUp() {
        if (mCurrentTestSuite != null) {
            mCurrentTestSuite.setTestStopped(mTestStop);
            mCurrentTestSuite.notifyTestingDeviceDisconnected();
        }
        if (mProgressObserver != null) {
            mProgressObserver.stop();
        }
        if (mTimeOutTimer != null) {
            mTimeOutTimer.cancel(false);
        }
    }
    public void runTest(final TestDevice device, final Test test)
            throws DeviceDisconnectedException, ADBServerNeedRestartException,
            InvalidApkPathException, InvalidNameSpaceException {
        if (test == null) {
            return;
        }
        mTestStop = false;
        mIsInBatchMode = false;
        println(PKG_LOG_SEPARATOR);
        println("Test package: " + getAppPackageName());
        setTestDevice(device);
        runTestImpl(test);
    }
    protected void runTestImpl(final Test test) throws DeviceDisconnectedException,
            ADBServerNeedRestartException, InvalidApkPathException,
            InvalidNameSpaceException {
        try {
            if (!install()) {
                return;
            }
            if (!mTestStop) {
                Log.d("install " + getAppPackageName() + " succeed!");
                mCurrentTestSuite = test.getTestSuite();
                mCurrentTestSuite.run(mDevice, test);
            }
            if (!mTestStop) {
                uninstall();
                println(PKG_LOG_SEPARATOR);
            }
        } catch (DeviceDisconnectedException e) {
            cleanUp();
            throw e;
        }
    }
}
