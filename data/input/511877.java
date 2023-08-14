public class TestSession {
    private SessionObserver mSessionObserver;
    private TestSessionLog mSessionLog;
    private TestDevice mDevice;
    private int mId;
    private STATUS mStatus;
    private static int sIdCounter = 0;
    enum STATUS {
        INIT, STARTED, INSTALLING, RUNNING, PAUSED, RESUMED, STOPPED, FINISHED
    }
    private int mRequiredDeviceNumber;
    private boolean mTestStop;
    private TestSessionThread mTestThread;
    private boolean mNeedRestartAdbServer;
    private static boolean mADBServerRestartedMode;
    private static long mTestCount;
    public TestSession(final TestSessionLog sessionLog,
            final int requiredDeviceNum) {
        mStatus = STATUS.INIT;
        mNeedRestartAdbServer = false;
        mADBServerRestartedMode = false;
        mTestCount = 0;
        mSessionLog = sessionLog;
        mDevice = null;
        mRequiredDeviceNumber = requiredDeviceNum;
        mTestStop = false;
        mId = sIdCounter++;
    }
    public static int getLastSessionId() {
        return sIdCounter-1;
    }
    public static void setADBServerRestartedMode() {
        mADBServerRestartedMode = true;
    }
    public static void resetADBServerRestartedMode() {
        mADBServerRestartedMode = false;
    }
    public static boolean isADBServerRestartedMode() {
        return mADBServerRestartedMode;
    }
    public static void incTestCount() {
        mTestCount++;
    }
    public static void resetTestCount() {
        mTestCount = 0;
    }
    public static long getTestCount() {
        return mTestCount;
    }
    public static boolean exceedsMaxCount() {
        final long maxTestCount = HostConfig.getMaxTestCount();
        return (maxTestCount > 0) && (mTestCount >= maxTestCount);
    }
    public STATUS getStatus() {
        return mStatus;
    }
    public String getDeviceId() {
        if (mDevice == null) {
            return null;
        }
        return mDevice.getSerialNumber();
    }
    public TestDevice getDevice() {
        return mDevice;
    }
    public int getNumOfRequiredDevices() {
        return mRequiredDeviceNumber;
    }
    public int getId() {
        return mId;
    }
    public void start(final String testFullName) throws TestNotFoundException,
            IllegalTestNameException, ADBServerNeedRestartException {
        if ((testFullName == null) || (testFullName.length() == 0)) {
            throw new IllegalArgumentException();
        }
        if (!testFullName.matches("(\\w+.)+\\w+")) {
            throw new IllegalTestNameException(testFullName);
        }
        Test test = null;
        TestPackage pkg = null;
        if (-1 != testFullName.indexOf(Test.METHOD_SEPARATOR)) {
            test = searchTest(testFullName);
            if (test == null) {
                throw new TestNotFoundException(
                        "The specific test does not exist: " + testFullName);
            }
            mTestThread = new TestSessionThread(this, test);
            CUIOutputStream.println("start test " + testFullName);
        } else {
            pkg = searchTestPackage(testFullName);
            if (pkg == null) {
                throw new TestNotFoundException(
                        "The specific test package does not exist: " + testFullName);
            }
            mTestThread = new TestSessionThread(this, pkg, testFullName);
            CUIOutputStream.println("start java package " + testFullName);
        }
        mStatus = STATUS.STARTED;
        startImpl();
    }
    private void startImpl() throws ADBServerNeedRestartException {
        String resultPath = mSessionLog.getResultPath();
        if ((resultPath == null) || (resultPath.length() == 0)) {
            mSessionLog.setStartTime(System.currentTimeMillis());
        }
        resetTestCount();
        mTestThread.start();
        try {
            mTestThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mNeedRestartAdbServer && HostConfig.getMaxTestCount() > 0) {
            throw new ADBServerNeedRestartException("Need restart ADB server");
        }
    }
    public void resume() throws ADBServerNeedRestartException {
        mStatus = STATUS.RESUMED;
        mTestThread = new TestSessionThread(this);
        if (!isADBServerRestartedMode()) {
            CUIOutputStream.println("resume test plan " + getSessionLog().getTestPlanName()
                + " (session id = " + mId + ")");
        }
        startImpl();
    }
    private Test searchTest(final String testFullName) {
        Test test = null;
        for (TestPackage pkg : mSessionLog.getTestPackages()) {
            test = pkg.searchTest(testFullName);
            if (test != null) {
                break;
            }
        }
        return test;
    }
    private TestPackage searchTestPackage(String javaPkgName) {
        for (TestPackage pkg : mSessionLog.getTestPackages()) {
            Collection<Test> tests = pkg.getTests();
            for (Test test : tests) {
                String testFullName = test.getFullName();
                if (testFullName.startsWith(javaPkgName)) {
                    if (testFullName.charAt(javaPkgName.length()) != '.') {
                        javaPkgName = javaPkgName.substring(0, javaPkgName.lastIndexOf("."));
                    }
                    return pkg;
                }
            }
        }
        return null;
    }
    public void start() throws ADBServerNeedRestartException {
        mStatus = STATUS.STARTED;
        mSessionLog.setStartTime(System.currentTimeMillis());
        mTestThread = new TestSessionThread(this);
        CUIOutputStream.println("start test plan " + getSessionLog().getTestPlanName());
        startImpl();
    }
    public void setObserver(final SessionObserver so) {
        mSessionObserver = so;
    }
    private void println(final String msg) {
        if (!mTestStop) {
            CUIOutputStream.println(msg);
        }
    }
    public void setTestDevice(final TestDevice device) {
        mDevice = device;
    }
    public TestSessionLog getSessionLog() {
        return mSessionLog;
    }
    public Collection<TestPackage> getTestPackages() {
        return mSessionLog.getTestPackages();
    }
    class TestSessionThread extends Thread {
        private final int MSEC_PER_SECOND = 1000;
        private TestSession mTestSession;
        private Test mTest;
        private TestPackage mTestPackage;
        private String mJavaPackageName;
        private ResultObserver mResultObserver;
        public TestSessionThread(final TestSession ts) {
            mTestSession = ts;
            mResultObserver = ResultObserver.getInstance();
        }
        public TestSessionThread(final TestSession ts, final Test test) {
            mTestSession = ts;
            mResultObserver = ResultObserver.getInstance();
            mTest = test;
        }
        public TestSessionThread(final TestSession ts,
                final TestPackage pkg, final String javaPkgName) {
            mTestSession = ts;
            mResultObserver = ResultObserver.getInstance();
            mTestPackage = pkg;
            mJavaPackageName = javaPkgName;
        }
        @Override
        public void run() {
            Log.d("Start a test session.");
            mNeedRestartAdbServer = false;
            mResultObserver.setTestSessionLog(getSessionLog());
            mResultObserver.start();
            try {
                if (mTest != null) {
                    TestPackage pkg = mTest.getTestPackage();
                    pkg.setSessionThread(this);
                    pkg.runTest(mDevice, mTest);
                } else if (mTestPackage != null) {
                    mTestPackage.setSessionThread(this);
                    mTestPackage.run(mDevice, mJavaPackageName, mSessionLog);
                } else {
                    for (TestPackage pkg : mSessionLog.getTestPackages()) {
                        if (!pkg.isAllTestsRun()) {
                            pkg.setSessionThread(this);
                            pkg.run(mDevice, null, mSessionLog);
                            if (!isAllTestsRun()) {
                                if (HostConfig.getMaxTestCount() > 0) {
                                    markNeedRestartADBServer();
                                    return;
                                }
                            } else {
                                Log.d("All tests have been run.");
                                break;
                            }
                        }
                    }
                    mNeedRestartAdbServer = false;
                    displayTestResultSummary();
                }
            } catch (IOException e) {
                Log.e("Got exception when running the package", e);
            } catch (DeviceDisconnectedException e) {
                Log.e("Device " + e.getMessage() + " disconnected ", null);
            } catch (ADBServerNeedRestartException e) {
                Log.d(e.getMessage());
                if (mTest == null) {
                    markNeedRestartADBServer();
                    return;
                }
            } catch (InvalidApkPathException e) {
                Log.e(e.getMessage(), null);
            } catch (InvalidNameSpaceException e) {
                Log.e(e.getMessage(), null);
            }
            long startTime = getSessionLog().getStartTime().getTime();
            displayTimeInfo(startTime, System.currentTimeMillis());
            mStatus = STATUS.FINISHED;
            mTestSession.getSessionLog().setEndTime(System.currentTimeMillis());
            mSessionObserver.notifyFinished(mTestSession);
            notifyResultObserver();
        }
        private void markNeedRestartADBServer() {
            Log.d("mark mNeedRestartAdbServer to true");
            mNeedRestartAdbServer = true;
            mStatus = STATUS.FINISHED;
            notifyResultObserver();
            return;
        }
        private void notifyResultObserver() {
            mResultObserver.notifyUpdate();
            mResultObserver.finish();
        }
        private boolean isAllTestsRun() {
            Collection<TestPackage> pkgs = getTestPackages();
            for (TestPackage pkg : pkgs) {
                if (!pkg.isAllTestsRun()) {
                    return false;
                }
            }
            return true;
        }
        private void displayTestResultSummary() {
            int passNum = mSessionLog.getTestList(CtsTestResult.CODE_PASS).size();
            int failNum = mSessionLog.getTestList(CtsTestResult.CODE_FAIL).size();
            int notExecutedNum =
                mSessionLog.getTestList(CtsTestResult.CODE_NOT_EXECUTED).size();
            int timeOutNum = mSessionLog.getTestList(CtsTestResult.CODE_TIMEOUT).size();
            int total = passNum + failNum + notExecutedNum + timeOutNum;
            println("Test summary:   pass=" + passNum
                    + "   fail=" + failNum
                    + "   timeOut=" + timeOutNum
                    + "   notExecuted=" + notExecutedNum
                    + "   Total=" + total);
        }
        private void displayTimeInfo(final long startTime, final long endTime) {
            long diff = endTime - startTime;
            long seconds = diff / MSEC_PER_SECOND;
            long millisec = diff % MSEC_PER_SECOND;
            println("Time: " + seconds + "." + millisec + "s\n");
        }
    }
   static class ResultObserver {
        static private boolean mFinished = false;
        static private boolean mNotified = false; 
        static private boolean mNeedUpdate = true;
        static private TestSessionLog mSessionLog;
        static final ResultObserver sInstance = new ResultObserver();
        private Observer mObserver;
        public static final ResultObserver getInstance() {
            return sInstance;
        }
        public void setTestSessionLog(TestSessionLog log) {
            mSessionLog = log;
        }
        public void notifyUpdate() {
            if (mObserver != null) {
                synchronized (mObserver) {
                    mNotified = true;
                    mObserver.notify();
                }
            }
        }
        public void start() {
            mFinished = false;
            mNeedUpdate = true;
            mObserver = new Observer();
            mObserver.start();
        }
        public void finish() {
            mFinished = true;
            mNeedUpdate = false;
            notifyUpdate();
            try {
                mObserver.join();
                mObserver = null;
            } catch (InterruptedException e) {
            }
        }
        class Observer extends Thread {
            @Override
            public void run() {
                while (!mFinished) {
                    try {
                        synchronized (this) {
                            if ((!mNotified) && (!mFinished)) {
                                wait();
                            }
                            mNotified = false;
                        }
                        if (mNeedUpdate && (mSessionLog != null)) {
                            mSessionLog.sessionComplete();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
