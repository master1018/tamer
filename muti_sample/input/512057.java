public class TestSessionLog extends XMLResourceHandler {
    private static final String EXPR_TEST_FAILED = ".+\\((\\S+):(\\d+)\\)";
    private static Pattern mTestFailedPattern = Pattern.compile(EXPR_TEST_FAILED);
    private static final String ATTRIBUTE_NAME = "name";
    static final String ATTRIBUTE_RESULT = "result";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_DIGEST = "digest";
    private static final String ATTRIBUTE_KNOWN_FAILURE = "KnownFailure";
    public static final String CTS_RESULT_FILE_NAME = "testResult.xml";
    private static final String CTS_RESULT_FILE_VERSION = "1.2";
    static final String ATTRIBUTE_STARTTIME = "starttime";
    static final String ATTRIBUTE_ENDTIME = "endtime";
    static final String ATTRIBUTE_TESTPLAN = "testPlan";
    static final String ATTRIBUTE_RESOLUTION = "resolution";
    static final String ATTRIBUTE_SUBSCRIBER_ID = "subscriberId";
    static final String ATTRIBUTE_DEVICE_ID = "deviceID";
    static final String ATTRIBUTE_BUILD_ID = "buildID";
    static final String ATTRIBUTE_BUILD_VERSION = "buildVersion";
    static final String ATTRIBUTE_ANDROID_PLATFORM_VERSION = "androidPlatformVersion";
    static final String ATTRIBUTE_LOCALES = "locales";
    static final String ATTRIBUTE_XDPI = "Xdpi";
    static final String ATTRIBUTE_YDPI = "Ydpi";
    static final String ATTRIBUTE_TOUCH = "touch";
    static final String ATTRIBUTE_NAVIGATION = "navigation";
    static final String ATTRIBUTE_KEYPAD = "keypad";
    static final String ATTRIBUTE_NETWORK = "network";
    static final String ATTRIBUTE_IMEI = "imei";
    static final String ATTRIBUTE_IMSI = "imsi";
    static final String ATTRIBUTE_BUILD_NAME = "buildName";
    static final String ATTRIBUTE_ARCH = "arch";
    static final String ATTRIBUTE_VALUE = "value";
    static final String ATTRIBUTE_PASS = "pass";
    static final String ATTRIBUTE_FAILED = "failed";
    static final String ATTRIBUTE_TIMEOUT = "timeout";
    static final String ATTRIBUTE_NOT_EXECUTED = "notExecuted";
    static final String TAG_DEVICEINFO = "DeviceInfo";
    static final String TAG_HOSTINFO = "HostInfo";
    static final String TAG_OSINFO = "Os";
    static final String TAG_JAVA = "Java";
    static final String TAG_CTS = "Cts";
    static final String TAG_INTVALUE = "IntValue";
    static final String TAG_SUMMARY = "Summary";
    static final String TAG_SCREEN = "Screen";
    static final String TAG_BUILD_INFO = "BuildInfo";
    static final String TAG_PHONE_SUB_INFO = "PhoneSubInfo";
    static final String TAG_TEST_RESULT = "TestResult";
    static final String TAG_TESTPACKAGE = "TestPackage";
    static final String TAG_TESTSUITE = "TestSuite";
    static final String TAG_TESTCASE = "TestCase";
    static final String TAG_FAILED_SCENE = "FailedScene";
    static final String TAG_STACK_TRACE = "StackTrace";
    static final String TAG_FAILED_MESSAGE = "message";
    private Collection<TestPackage> mTestPackages;
    private Date mSessionStartTime;
    private Date mSessionEndTime;
    private String mResultPath;
    private String mResultDir;
    private String mTestPlanName;
    private ArrayList<DeviceParameterCollector> mDeviceParameterBase;
    public TestSessionLog(final Collection<TestPackage> packages, final String testPlanName) {
        mTestPackages = packages;
        mDeviceParameterBase = new ArrayList<TestDevice.DeviceParameterCollector>();
        mTestPlanName = testPlanName;
        mSessionStartTime = new Date();
        mSessionEndTime = new Date();
    }
    public String getTestPlanName() {
        return mTestPlanName;
    }
    public Collection<Test> getAllResults() {
        if (mTestPackages == null || mTestPackages.size() == 0) {
            return null;
        }
        ArrayList<Test> results = new ArrayList<Test>();
        for (TestPackage p : mTestPackages) {
            results.addAll(p.getTests());
        }
        return results;
    }
    public Collection<Test> getTestList(int resCode) {
        if ((resCode < CtsTestResult.CODE_FIRST)
                || (resCode > CtsTestResult.CODE_LAST)) {
            return null;
        }
        ArrayList<Test> tests = new ArrayList<Test>();
        for (Test test : getAllResults()) {
            if (resCode == test.getResult().getResultCode()) {
                tests.add(test);
            }
        }
        return tests;
    }
    public Date getStartTime() {
        return mSessionStartTime;
    }
    public Date getEndTime() {
        return mSessionEndTime;
    }
    public Collection<TestPackage> getTestPackages() {
        return mTestPackages;
    }
    public String getResultPath() {
        return mResultPath;
    }
    public String getResultDir() {
        return mResultDir;
    }
    public void setStartTime(final long time) {
        mSessionStartTime.setTime(time);
        String startTimeStr = HostUtils.getFormattedTimeString(time, "_", ".", ".");
        mResultDir = HostConfig.getInstance().getResultRepository().getRoot()
            + File.separator + startTimeStr;
        mResultPath =  mResultDir + File.separator + CTS_RESULT_FILE_NAME;
        new File(mResultDir).mkdirs();
    }
    public void setEndTime(final long time) {
        mSessionEndTime.setTime(time);
    }
    public void sessionComplete() {
        try {
            writeToFile(new File(mResultPath), createResultDoc());
            HostUtils.zipUpDirectory(mResultDir,
                    mResultDir + ".zip",
                    new HostUtils.ZipFilenameTransformer() {
                        public String transform(String filename) {
                            if (filename.startsWith(mResultDir)) {
                                return filename.substring(mResultDir.length() + 1);
                            }
                            return filename;
                        }
            });
        } catch (Exception e) {
            Log.e("Got exception when trying to write to result file", e);
        }
        HostConfig.getInstance().extractResultResources(mResultDir);
    }
    protected Document createResultDoc() {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            ProcessingInstruction pr = doc.createProcessingInstruction(
                    "xml-stylesheet", "type=\"text/xsl\"  href=\"cts_result.xsl\"");
            doc.appendChild(pr);
            Node root = doc.createElement(TAG_TEST_RESULT);
            doc.appendChild(root);
            setAttribute(doc, root, ATTRIBUTE_VERSION, CTS_RESULT_FILE_VERSION);
            setAttribute(doc, root, ATTRIBUTE_STARTTIME, HostUtils.dateToString(mSessionStartTime));
            setAttribute(doc, root, ATTRIBUTE_ENDTIME, HostUtils.dateToString(mSessionEndTime));
            setAttribute(doc, root, ATTRIBUTE_TESTPLAN, mTestPlanName);
            for (int i = 0; i < mDeviceParameterBase.size(); i ++) {
                DeviceParameterCollector bldInfo = mDeviceParameterBase.get(i);
                Node deviceSettingNode = doc.createElement(TAG_DEVICEINFO);
                Node screenNode = doc.createElement(TAG_SCREEN);
                setAttribute(doc, screenNode, ATTRIBUTE_RESOLUTION, bldInfo.getScreenResolution());
                deviceSettingNode.appendChild(screenNode);
                Node simCardNode = doc.createElement(TAG_PHONE_SUB_INFO);
                setAttribute(doc, simCardNode, ATTRIBUTE_SUBSCRIBER_ID, bldInfo.getPhoneNumber());
                deviceSettingNode.appendChild(simCardNode);
                root.appendChild(deviceSettingNode);
                Node devInfoNode = doc.createElement(TAG_BUILD_INFO);
                setAttribute(doc, devInfoNode, ATTRIBUTE_DEVICE_ID, bldInfo.getSerialNumber());
                setAttribute(doc, devInfoNode, ATTRIBUTE_BUILD_ID, bldInfo.getBuildId());
                setAttribute(doc, devInfoNode, ATTRIBUTE_BUILD_NAME, bldInfo.getProductName());
                setAttribute(doc, devInfoNode, ATTRIBUTE_BUILD_VERSION,
                             bldInfo.getBuildVersion());
                setAttribute(doc, devInfoNode, ATTRIBUTE_ANDROID_PLATFORM_VERSION,
                             bldInfo.getAndroidPlatformVersion());
                setAttribute(doc, devInfoNode, ATTRIBUTE_LOCALES, bldInfo.getLocales());
                setAttribute(doc, devInfoNode, ATTRIBUTE_XDPI, bldInfo.getXdpi());
                setAttribute(doc, devInfoNode, ATTRIBUTE_YDPI, bldInfo.getYdpi());
                setAttribute(doc, devInfoNode, ATTRIBUTE_TOUCH, bldInfo.getTouchInfo());
                setAttribute(doc, devInfoNode, ATTRIBUTE_NAVIGATION, bldInfo.getNavigation());
                setAttribute(doc, devInfoNode, ATTRIBUTE_KEYPAD, bldInfo.getKeypad());
                setAttribute(doc, devInfoNode, ATTRIBUTE_NETWORK, bldInfo.getNetwork());
                setAttribute(doc, devInfoNode, ATTRIBUTE_IMEI, bldInfo.getIMEI());
                setAttribute(doc, devInfoNode, ATTRIBUTE_IMSI, bldInfo.getIMSI());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_FINGERPRINT, bldInfo.getBuildFingerPrint());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_TYPE, bldInfo.getBuildType());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_MODEL, bldInfo.getBuildModel());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_BRAND, bldInfo.getBuildBrand());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_BOARD, bldInfo.getBuildBoard());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_DEVICE, bldInfo.getBuildDevice());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_ABI, bldInfo.getBuildAbi());
                setAttribute(doc, devInfoNode,
                        DeviceParameterCollector.BUILD_ABI2, bldInfo.getBuildAbi2());
                deviceSettingNode.appendChild(devInfoNode);
            }
            Node hostInfo = doc.createElement(TAG_HOSTINFO);
            root.appendChild(hostInfo);
            String hostName = "";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ignored) {}
            setAttribute(doc, hostInfo, ATTRIBUTE_NAME, hostName);
            Node osInfo = doc.createElement(TAG_OSINFO);
            hostInfo.appendChild(osInfo);
            setAttribute(doc, osInfo, ATTRIBUTE_NAME, System.getProperty("os.name"));
            setAttribute(doc, osInfo, ATTRIBUTE_VERSION, System.getProperty("os.version"));
            setAttribute(doc, osInfo, ATTRIBUTE_ARCH, System.getProperty("os.arch"));
            Node javaInfo = doc.createElement(TAG_JAVA);
            hostInfo.appendChild(javaInfo);
            setAttribute(doc, javaInfo, ATTRIBUTE_NAME, System.getProperty("java.vendor"));
            setAttribute(doc, javaInfo, ATTRIBUTE_VERSION, System.getProperty("java.version"));
            Node ctsInfo = doc.createElement(TAG_CTS);
            hostInfo.appendChild(ctsInfo);
            setAttribute(doc, ctsInfo, ATTRIBUTE_VERSION, Version.asString());
            for (HostConfig.Ints i : HostConfig.Ints.values()) {
                Node intValue = doc.createElement(TAG_INTVALUE);
                ctsInfo.appendChild(intValue);
                setAttribute(doc, intValue, ATTRIBUTE_NAME, i.name());
                setAttribute(doc, intValue, ATTRIBUTE_VALUE, i.value());
            }
            int passNum = getTestList(CtsTestResult.CODE_PASS).size();
            int failNum = getTestList(CtsTestResult.CODE_FAIL).size();
            int notExecutedNum = getTestList(CtsTestResult.CODE_NOT_EXECUTED).size();
            int timeOutNum = getTestList(CtsTestResult.CODE_TIMEOUT).size();
            Node summaryNode = doc.createElement(TAG_SUMMARY);
            root.appendChild(summaryNode);
            setAttribute(doc, summaryNode, ATTRIBUTE_PASS, passNum);
            setAttribute(doc, summaryNode, ATTRIBUTE_FAILED, failNum);
            setAttribute(doc, summaryNode, ATTRIBUTE_NOT_EXECUTED, notExecutedNum);
            setAttribute(doc, summaryNode, ATTRIBUTE_TIMEOUT, timeOutNum);
            for (TestPackage testPackage : mTestPackages) {
                Node testPackageNode = doc.createElement(TAG_TESTPACKAGE);
                setAttribute(doc, testPackageNode, ATTRIBUTE_NAME, testPackage.getAppBinaryName());
                setAttribute(doc, testPackageNode, TestSessionBuilder.ATTRIBUTE_APP_PACKAGE_NAME,
                        testPackage.getAppPackageName());
                setAttribute(doc, testPackageNode, ATTRIBUTE_DIGEST,
                             testPackage.getMessageDigest());
                if (testPackage instanceof SignatureCheckPackage) {
                    setAttribute(doc, testPackageNode,
                              TestSessionBuilder.ATTRIBUTE_SIGNATURE_CHECK, "true");
                }
                for (TestSuite testSuite : testPackage.getTestSuites()) {
                    outputTestSuite(doc, testPackage, testPackageNode, testSuite);
                }
                root.appendChild(testPackageNode);
            }
            return doc;
        } catch (Exception e) {
            Log.e("create result doc failed", e);
        }
        return null;
    }
    private void outputTestSuite(final Document doc,
            final TestPackage testPackage, final Node parentNode,
            TestSuite testSuite) {
        Collection<TestSuite> subSuites = testSuite.getSubSuites();
        Collection<TestCase> testCases = testSuite.getTestCases();
        Node testSuiteNode = doc.createElement(TAG_TESTSUITE);
        setAttribute(doc, testSuiteNode, ATTRIBUTE_NAME, testSuite.getName());
        for (TestCase testCase : testCases) {
            Node testCaseNode = doc.createElement(TAG_TESTCASE);
            testSuiteNode.appendChild(testCaseNode);
            setAttribute(doc, testCaseNode, ATTRIBUTE_NAME, testCase.getName());
            setAttribute(doc, testCaseNode, TestSessionBuilder.ATTRIBUTE_PRIORITY,
                    testCase.getPriority());
            Collection<Test> tests = testCase.getTests();
            for (Test test : tests) {
                Node testNode = doc.createElement(TestSessionBuilder.TAG_TEST);
                testCaseNode.appendChild(testNode);
                if (test.isKnownFailure()) {
                    setAttribute(doc, testNode, ATTRIBUTE_KNOWN_FAILURE, test.getKnownFailure());
                }
                CtsTestResult result = test.getResult();
                setAttribute(doc, testNode, ATTRIBUTE_NAME, test.getName());
                setAttribute(doc, testNode, ATTRIBUTE_RESULT, result.getResultString());
                setAttribute(doc, testNode, ATTRIBUTE_STARTTIME,
                             new Date(test.getStartTime()).toString());
                setAttribute(doc, testNode, ATTRIBUTE_ENDTIME,
                             new Date(test.getEndTime()).toString());
                String failedMessage = result.getFailedMessage();
                if (failedMessage != null) {
                    failedMessage = HostUtils.replaceControlChars(failedMessage);
                    Node failedMessageNode = doc.createElement(TAG_FAILED_SCENE);
                    testNode.appendChild(failedMessageNode);
                    setAttribute(doc, failedMessageNode,TAG_FAILED_MESSAGE, failedMessage);
                    String stackTrace = result.getStackTrace();
                    if (stackTrace != null) {
                        Node stackTraceNode = doc.createElement(TAG_STACK_TRACE);
                        failedMessageNode.appendChild(stackTraceNode);
                        Node stackTraceTextNode = doc.createTextNode(stackTrace);
                        stackTraceNode.appendChild(stackTraceTextNode);
                    }
                }
            }
        }
        for (TestSuite subSuite : subSuites) {
            outputTestSuite(doc, testPackage, testSuiteNode, subSuite);
            parentNode.appendChild(testSuiteNode);
        }
        parentNode.appendChild(testSuiteNode);
    }
    public final static String[] getFailedLineNumber(final String failedResult) {
        Matcher m = mTestFailedPattern.matcher(failedResult);
        if (m.matches()) {
            return new String[]{m.group(1), m.group(2)};
        }
        return null;
    }
    public void setDeviceInfo(final TestDevice.DeviceParameterCollector dInfo) {
        for (DeviceParameterCollector collector : mDeviceParameterBase) {
            if (collector.getSerialNumber().equals(dInfo.getSerialNumber())) {
                mDeviceParameterBase.remove(collector);
                break;
            }
        }
        mDeviceParameterBase.add(dInfo);
    }
}
