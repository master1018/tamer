public class TestSessionBuilderTests extends CtsTestBase {
    private static final String mTmpPlanFileName = "plan";
    private static final String ATTRIBUTE_DEVICE_ID = "deviceID";
    private static final String ATTRIBUTE_BUILD_ID = "buildID";
    private static final String ATTRIBUTE_BUILD_VERSION = "buildVersion";
    private static final String ATTRIBUTE_BUILD_NAME = "buildName";
    private String mTestPackageBinaryName = "CtsTestPackage";
    @Override
    public void tearDown() {
        HostConfig.getInstance().removeTestPacakges();
        deleteTestPackage(mTestPackageBinaryName);
        super.tearDown();
    }
    public void testBuildSimpleSession() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts" + ".CtsTest";
        final String suiteName = appPackageName;
        final String caseName = "CtsTestHello";
        final String runner = "android.test.InstrumentationTestRunner";
        final String serialNum = "serialNum";
        final String buildID = "buildid";
        final String buildName = "buildname";
        final String buildVersion = "buildVersion";
        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n"
                + "\t<Description>Demo test plan</Description>\n"
                + "\t\t<PlanSettings>\n"
                + "\t\t\t<RequiredDevice amount=\"" + 1 + "\"" + "/>\n"
                + "\t\t</PlanSettings>\n"
                + "\t<Entry uri=\""
                + appPackageName
                + "\"/>\n"
                + "</TestPlan>";
        final String descriptionConfigStr = "<TestPackage name=\""
                + mTestPackageBinaryName+ "\" "
                + "appPackageName=\"" + appPackageName
                + "\" version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"" + runner + "\">\n"
                + " <Description>something extracted from java doc</Description>\n"
                + " <TestSuite name=\"" + suiteName + "\"" + ">\n"
                + "     <TestCase name=\"" + caseName + "\"" + " priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + " </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        String planPath =
            HostConfig.getInstance().getPlanRepository().getPlanPath(mTmpPlanFileName);
        createFile(testPlanConfigStr, planPath);
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals(appPackageName, testPackage.getAppPackageName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        assertEquals(runner, testPackage.getInstrumentationRunner());
        TestSuite testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals(suiteName, testSuite.getFullName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("automatic", test.getType());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        assertEquals(runner, test.getInstrumentationRunner());
        DeviceParameterCollector deviceParam = new DeviceParameterCollector();
        deviceParam.setSerialNumber(serialNum);
        deviceParam.setBuildId(buildID);
        deviceParam.setBuildVersion(buildVersion);
        deviceParam.setProductName(buildName);
        tsl.setDeviceInfo(deviceParam);
        Document doc = tsl.createResultDoc();
        Node buildInfoNode = doc.getElementsByTagName("BuildInfo").item(0);
        assertEquals(serialNum, buildInfoNode.getAttributes().getNamedItem(
                ATTRIBUTE_DEVICE_ID).getNodeValue().trim());
        assertEquals(buildID, buildInfoNode.getAttributes().getNamedItem(
                ATTRIBUTE_BUILD_ID).getNodeValue().trim());
        assertEquals(buildVersion, buildInfoNode.getAttributes().getNamedItem(
                ATTRIBUTE_BUILD_VERSION).getNodeValue().trim());
        assertEquals(buildName, buildInfoNode.getAttributes().getNamedItem(
                ATTRIBUTE_BUILD_NAME).getNodeValue().trim());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testGetAndRemoveEntriesFromPlanFile() throws UnknownCommandException,
                    CommandNotFoundException, Exception {
        final String appPackageName = "com.google.android.cts";
        final String suiteName = appPackageName;
        final String caseName = "CtsTestHello";
        final String runner = "android.test.InstrumentationTestRunner";
        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n"
                + "\t<Description>Demo test plan</Description>\n"
                + "\t\t<PlanSettings>\n"
                + "\t\t\t<RequiredDevice amount=\"" + 1 + "\"" + "/>\n"
                + "\t\t</PlanSettings>\n"
                + "\t<Entry uri=\""
                + appPackageName
                + "\"/>\n"
                + "</TestPlan>";
        final String descriptionConfigStr = "<TestPackage name=\""
                + mTestPackageBinaryName+ "\" "
                + "appPackageName=\"" + appPackageName
                + "\" version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"" + runner + "\">\n"
                + " <Description>something extracted from java doc</Description>\n"
                + " <TestSuite name=\"" + suiteName + "\"" + ">\n"
                + "     <TestCase name=\"" + caseName + "\"" + " priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + " </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        String planPath = HostConfig.getInstance().getPlanRepository()
                .getPlanPath(mTmpPlanFileName);
        createFile(testPlanConfigStr, planPath);
        ArrayList<String> removedPkgList = new ArrayList<String>();
        Collection<String> entries = TestPlan.getEntries(planPath, removedPkgList);
        Iterator<String> it = entries.iterator();
        assertEquals(1, entries.size());
        assertEquals(appPackageName, it.next());
        ConsoleUi cui = new ConsoleUi(TestHost.getInstance());
        String cmdLine = CTSCommand.REMOVE + " " + "-p" + " "
                + appPackageName;
        cui.processCommand(CommandParser.parse(cmdLine));
        entries = TestPlan.getEntries(planPath, removedPkgList);
        assertEquals(1, removedPkgList.size());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testValidatePackageName() throws IOException {
        final String packageName = "com.google.android.testname";
        String testPackageXmlFilePath =
            HostConfig.getInstance().getCaseRepository().getXmlPath(packageName);
        String testpackageAPKFilePath =
            HostConfig.getInstance().getCaseRepository().getApkPath(packageName);
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage("", packageName);
        assertEquals(true, TestPlan.isValidPackageName(packageName));
        deleteFile(testPackageXmlFilePath);
        assertEquals(false, TestPlan.isValidPackageName(packageName));
        deleteFile(testpackageAPKFilePath);
        assertEquals(false, TestPlan.isValidPackageName(packageName));
    }
    public void testBuildSessionWithDependency() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException, NoSuchAlgorithmException {
        final String targetNameSpace = "i don't want to be the target!";
        final String appPackageName = "com.google.android.cts";
        final String suiteName = appPackageName + ".CtsTest";
        final String caseName = "CtsTestHello";
        final String runner = "android.test.InstrumentationTestRunner";
        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n"
                + "\t<Description>Demo test plan</Description>\n"
                + "\t\t<PlanSettings>\n"
                + "\t\t\t<RequiredDevice amount=\"" + 1 + "\"" + "/>\n"
                + "\t\t</PlanSettings>\n"
                + "\t<Entry uri=\""
                + appPackageName
                + "\"/>\n"
                + "</TestPlan>";
        final String descriptionConfigStr = "<TestPackage name=\""
                + mTestPackageBinaryName + "\" "
                + "appPackageName=\"" + appPackageName
                + "\" targetNameSpace=\"" + targetNameSpace
                + "\" version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"" +runner + "\">\n"
                + " <Description>something extracted from java doc</Description>\n"
                + " <TestSuite name=\"" + suiteName + "\"" + ">\n"
                + "     <TestCase name=\"" + caseName + "\"" + " priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + " </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        String planPath =
            HostConfig.getInstance().getPlanRepository().getPlanPath(mTmpPlanFileName);
        createFile(testPlanConfigStr, planPath);
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals(targetNameSpace, testPackage.getTargetNameSpace());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        assertEquals(runner, testPackage.getInstrumentationRunner());
        TestSuite testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals(suiteName, testSuite.getFullName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("automatic", test.getType());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        assertEquals(runner, test.getInstrumentationRunner());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testBuildSessionFromEmbeddedSuites() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String caseName = "CtsTestHello";
        final String testName = "testHello";
        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n"
                + "<Description>Demo test plan</Description>\n"
                + "<PlanSettings>\n"
                + "    <RequiredDevice amount=\"" + 1 + "\"" + "/>\n"
                + "</PlanSettings>\n"
                + "<Entry uri=\"" + appPackageName + "\"/>\n"
                + "</TestPlan>";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestSuite name=\"TestSuiteName\">\n"
                + "         <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "             <Description>" + "something extracted from java doc"
                + "             </Description>\n"
                + "             <!-- Test Methods -->\n"
                + "             <Test name=\"testName\"" + " type=\"automatic\"" + "/>\n"
                + "         </TestCase>\n"
                + "     </TestSuite>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        String planPath =
            HostConfig.getInstance().getPlanRepository().getPlanPath(mTmpPlanFileName);
        createFile(testPlanConfigStr, planPath);
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getSubSuites().size());
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google", testSuite.getFullName());
        assertEquals("com.google", testSuite.getName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("com.google.CtsTestHello#testHello", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(testName, test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        testSuite = testSuite.getSubSuites().iterator().next();
        assertEquals(0, testSuite.getSubSuites().size());
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google.TestSuiteName", testSuite.getFullName());
        assertEquals("TestSuiteName", testSuite.getName());
        testCase = testSuite.getTestCases().iterator().next();
        assertEquals("TestCaseName", testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        test = testCase.getTests().iterator().next();
        assertEquals("com.google.TestSuiteName.TestCaseName#testName", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals("testName", test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingFromXmlFile() throws IOException,
                ParserConfigurationException,
                SAXException,
                TestPlanNotFoundException,
                TestNotFoundException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String caseName = "CtsTestHello";
        final String testName = "testHello";
        final String excludedList = "com.google.TestSuiteName";
        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n"
                + "<Description>Demo test plan</Description>\n"
                + "<PlanSettings>\n"
                + "    <RequiredDevice amount=\"" + 1 + "\"" + "/>\n"
                + "</PlanSettings>\n"
                + "     <Entry uri=\"" + appPackageName
                + "\" " + "exclude=\"" + excludedList + "\"/>\n"
                + "</TestPlan>";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestSuite name=\"TestSuiteName\">\n"
                + "         <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "             <Description>" + "something extracted from java doc"
                + "             </Description>\n"
                + "             <!-- Test Methods -->\n"
                + "             <Test name=\"testName1\"" + " type=\"automatic\"" + "/>\n"
                + "             <Test name=\"testName2\"" + " type=\"automatic\"" + "/>\n"
                + "         </TestCase>\n"
                + "     </TestSuite>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        String planPath =
            HostConfig.getInstance().getPlanRepository().getPlanPath(mTmpPlanFileName);
        createFile(testPlanConfigStr, planPath);
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(0, testSuite.getSubSuites().size());
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google", testSuite.getFullName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("com.google.CtsTestHello#testHello", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(testName, test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingEmbeddedSuite() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String caseName = "CtsTestHello";
        final String testName = "testHello";
        final String excludedList = "com.google.TestSuiteName";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestSuite name=\"TestSuiteName\">\n"
                + "         <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "             <Description>" + "something extracted from java doc"
                + "             </Description>\n"
                + "             <!-- Test Methods -->\n"
                + "             <Test name=\"testName1\"" + " type=\"automatic\"" + "/>\n"
                + "             <Test name=\"testName2\"" + " type=\"automatic\"" + "/>\n"
                + "         </TestCase>\n"
                + "     </TestSuite>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(0, testSuite.getSubSuites().size());
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google", testSuite.getFullName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("com.google.CtsTestHello#testHello", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(testName, test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingTopSuite() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String excludedList = "com.google";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestSuite name=\"TestSuiteName\">\n"
                + "         <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "             <Description>" + "something extracted from java doc"
                + "             </Description>\n"
                + "             <!-- Test Methods -->\n"
                + "             <Test name=\"testName1\"" + " type=\"automatic\"" + "/>\n"
                + "             <Test name=\"testName2\"" + " type=\"automatic\"" + "/>\n"
                + "         </TestCase>\n"
                + "     </TestSuite>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(0, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingTestCase() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String caseName = "CtsTestHello";
        final String testName = "testHello";
        final String excludedList = "com.google.TestCaseName";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testName1\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testName2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google", testSuite.getFullName());
        assertEquals(1, testSuite.getTestCases().size());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("com.google.CtsTestHello#testHello", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(testName, test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingAllTestCases() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String excludedList = "com.google.TestCaseName;com.google.CtsTestHello";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testName1\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testName2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(0, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        assertEquals(0, testPackage.getTestSuites().size());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingTest() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String caseName = "CtsTestHello";
        final String testName = "testHello";
        final String excludedList = "com.google.CtsTestHello#testHello2";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google", testSuite.getFullName());
        assertEquals(1, testSuite.getTestCases().size());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("com.google.CtsTestHello#testHello", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(testName, test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testExcludingAllTests() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String excludedList =
            "com.google.CtsTestHello#testHello;com.google.CtsTestHello#testHello2;";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "         <Test name=\"testHello2\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(0, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testCreatePlanWithExcludedList() throws IOException,
          ParserConfigurationException, SAXException,
          TestPlanNotFoundException, TestNotFoundException,
          TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException{
        final String appPackageName = "com.google.android.cts";
        final String suiteName1 = appPackageName + "." + "SuiteName1";
        final String caseName1 = "CtsTestHello";
        final String testName1 = "testHello";
        final String caseName2 = "CtsTestHello2";
        final String testName2 = "testHello2";
        final String testName3 = "testHello3";
        final String suiteName2 = "com.google.android.cts.CtsTest.SuiteName2";
        final String descriptionConfigStr = "<TestPackage name=\""
              + mTestPackageBinaryName + "\""
              + " appPackageName=\"" + appPackageName + "\""
              + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
              + " runner=\"android.test.InstrumentationTestRunner\" >\n"
              + " <Description>something extracted from java doc</Description>\n"
              + " <TestSuite name=\"" + suiteName1 + "\"" + ">\n"
              + "     <TestCase name=\"" + caseName1 + "\"" + " category=\"mandatory\">\n"
              + "         <Description>" + "something extracted from java doc"
              + "         </Description>\n"
              + "         <!-- Test Methods -->\n"
              + "         <Test name=\"" + testName1 + "\" type=\"automatic\"" + "/>\n"
              + "     </TestCase>\n"
              + " </TestSuite>\n"
              + " <TestSuite name=\"" + suiteName2 + "\"" + ">\n"
              + "     <TestCase name=\"" + caseName2 + "\"" + " priority=\"mandatory\">\n"
              + "         <Test name=\"" + testName2 +"\" type=\"automatic\" />\n"
              + "         <Test name=\"" + testName3 +"\" type=\"automatic\" />\n"
              + "     </TestCase>\n"
              + " </TestSuite>\n"
              + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        String excludedList = suiteName1 + "." + caseName1 + TestPlan.EXCLUDE_SEPARATOR;
        excludedList += suiteName2 + "." + caseName2 + Test.METHOD_SEPARATOR + testName2;
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals(suiteName2, testSuite.getFullName());
        assertEquals(1, testSuite.getTestCases().size());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName2, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        assertEquals(1, testCase.getTests().size());
        Test test = testCase.getTests().iterator().next();
        String testName = suiteName2 + "." + caseName2 + Test.METHOD_SEPARATOR + testName3;
        assertEquals(testName, test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testCreatePlanWithExcludedListForNestedSuite() throws IOException,
          ParserConfigurationException, SAXException,
          TestPlanNotFoundException, TestNotFoundException,
          TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException{
        final String appPackageName = "com.google.android.cts";
        final String fullName = mTestPackageBinaryName + ".CtsTest";
        final String suiteName1 = fullName + "." + "SuiteName1";
        final String caseName1 = "CtsTestHello";
        final String testName1 = "testHello";
        final String caseName2 = "CtsTestHello2";
        final String testName2 = "testHello2";
        final String testName3 = "testHello3";
        final String nestedSuiteName1 = "com.google";
        final String nestedSuiteName2 = "android.cts.CtsTest.SuiteName2";
        final String descriptionConfigStr = "<TestPackage name=\""
            + mTestPackageBinaryName + "\""
            + " appPackageName=\"" + appPackageName + "\""
            + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
              + " runner=\"android.test.InstrumentationTestRunner\" >\n"
              + " <Description>something extracted from java doc</Description>\n"
              + " <TestSuite name=\"" + suiteName1 + "\"" + ">\n"
              + "     <TestCase name=\"" + caseName1 + "\"" + " category=\"mandatory\">\n"
              + "         <Description>" + "something extracted from java doc" + "</Description>\n"
              + "         <!-- Test Methods -->\n"
              + "         <Test name=\"" + testName1 + "\" type=\"automatic\"" + "/>\n"
              + "     </TestCase>\n"
              + " </TestSuite>\n"
              + " <TestSuite name=\"" + nestedSuiteName1 + "\"" + ">\n"
              + "     <TestSuite name=\"" + nestedSuiteName2 + "\"" + ">\n"
              + "         <TestCase name=\"" + caseName2 + "\"" + " priority=\"mandatory\">\n"
              + "             <Test name=\"" + testName2 +"\" type=\"automatic\" />\n"
              + "             <Test name=\"" + testName3 +"\" type=\"automatic\" />\n"
              + "         </TestCase>\n"
              + "     </TestSuite>\n"
              + " </TestSuite>\n"
              + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        ArrayList<String> list = new ArrayList<String>();
        String excludedList = suiteName1 + "." + caseName1 + TestPlan.EXCLUDE_SEPARATOR;
        excludedList += nestedSuiteName1 + "." + nestedSuiteName2
                        + "." + caseName2 + Test.METHOD_SEPARATOR + testName2;
        list.add(excludedList);
        results.put(appPackageName, list);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(0, testSuite.getTestCases().size());
        assertEquals(nestedSuiteName1, testSuite.getFullName());
        assertEquals(1, testSuite.getSubSuites().size());
        TestSuite testSubSuite = testSuite.getSubSuites().iterator().next();
        assertEquals(1, testSubSuite.getTestCases().size());
        String suiteName = nestedSuiteName1 + "." + nestedSuiteName2;
        assertEquals(suiteName, testSubSuite.getFullName());
        assertEquals(1, testSubSuite.getTestCases().size());
        TestCase testCase = testSubSuite.getTestCases().iterator().next();
        assertEquals(caseName2, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        assertEquals(1, testCase.getTests().size());
        Test test = testCase.getTests().iterator().next();
        String testName = nestedSuiteName1 + "." + nestedSuiteName2 + "." +caseName2
                          + Test.METHOD_SEPARATOR + testName3;
        assertEquals(testName, test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testBuildProtocolPlan() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String testPlanConfigStr = "<TestPlan version=\"1.0\">\n"
                + "\t<Description>Demo test plan</Description>\n"
                + "\t\t<PlanSettings>\n"
                + "\t\t\t<RequiredDevice amount=\"" + 2 + "\"" + "/>\n"
                + "\t\t</PlanSettings>\n"
                + "\t<Entry uri=\""
                + appPackageName
                + "\"/>\n"
                + "</TestPlan>";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName +"\" "
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + " <Description>something extracted from java doc</Description>\n"
                + " <TestSuite name=\"com.google.android.cts\"" + ">\n"
                + "     <TestCase name=\"CtsTestHello\"" + " priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + " </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        String planPath = HostConfig.getInstance().getPlanRepository()
                .getPlanPath(mTmpPlanFileName);
        createFile(testPlanConfigStr, planPath);
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(2, ts.getNumOfRequiredDevices());
        TestSessionLog tsl = ts.getSessionLog();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google.android.cts", testSuite.getFullName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        Test test = testCase.getTests().iterator().next();
        assertEquals("testHello", test.getName());
        assertEquals("automatic", test.getType());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testSerialize() throws Exception {
        final String srcStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<TestPlan version=\"1.0\">\n"
            + "<PlanSettings/>\n"
            + "<Entry uri=\"com.google.android.cts.CtsTest\"/>\n"
            + "</TestPlan>";
        final String package1 = "com.google.android.cts.CtsTest";
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(package1);
        HashMap<String, ArrayList<String>> selectedResult =
                      new HashMap<String, ArrayList<String>>();
        selectedResult.put(package1, null);
        HostConfig.getInstance().removeTestPacakges();
        String planName = "plan_test";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, selectedResult);
        File file = new File(planPath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
        FileReader dstInput = new FileReader(file);
        BufferedReader dstBufReader = new BufferedReader(dstInput);
        char[] resChars = new char[1024];
        dstBufReader.read(resChars);
        assertEquals(srcStr, new String(resChars).trim());
        dstBufReader.close();
        deleteFile(planPath);
    }
    public void testLoadPlanWithSubSuite() throws IOException,
            ParserConfigurationException, SAXException, TestPlanNotFoundException,
            TestNotFoundException,
            TransformerFactoryConfigurationError, TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String fullName = mTestPackageBinaryName + ".CtsTest";
        final String caseName = "CtsTestHello";
        final String testName = "testHello";
        final String descriptionConfigStr = "<TestPackage name=\"" + mTestPackageBinaryName + "\""
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" >\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"CtsTestHello\" priority=\"mandatory\">\n"
                + "         <Description>" + "something extracted from java doc"
                + "         </Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testHello\"" + " type=\"automatic\"" + "/>\n"
                + "     </TestCase>\n"
                + "     <TestSuite name=\"TestSuiteName\">\n"
                + "         <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "             <Description>" + "something extracted from java doc"
                + "             </Description>\n"
                + "             <!-- Test Methods -->\n"
                + "             <Test name=\"testName1\"" + " type=\"automatic\"" + "/>\n"
                + "         </TestCase>\n"
                + "     </TestSuite>\n"
                + "  </TestSuite>\n"
                + "</TestPackage>\n";
        HostConfig.getInstance().removeTestPacakges();
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        results.put(appPackageName, null);
        String planName = "plan_test_excluding";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        assertEquals(1, ts.getNumOfRequiredDevices());
        ts.getSessionLog().setStartTime(System.currentTimeMillis());
        TestSessionLog tsl = ts.getSessionLog();
        tsl.createResultDoc();
        assertEquals(1, tsl.getTestPackages().size());
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(1, testPackage.getTestSuites().size());
        assertEquals(mTestPackageBinaryName, testPackage.getAppBinaryName());
        assertEquals("1.0", testPackage.getVersion());
        assertEquals("Android 1.0", testPackage.getAndroidVersion());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        assertEquals(1, testSuite.getSubSuites().size());
        assertEquals(1, testSuite.getTestCases().size());
        assertEquals("com.google", testSuite.getFullName());
        TestCase testCase = testSuite.getTestCases().iterator().next();
        assertEquals(caseName, testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        Test test = testCase.getTests().iterator().next();
        assertEquals("com.google.CtsTestHello#testHello", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals(testName, test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        TestSuite subTestSuite;
        subTestSuite = testSuite.getSubSuites().iterator().next();
        assertEquals(1, subTestSuite.getTestCases().size());
        assertEquals("com.google.TestSuiteName", subTestSuite.getFullName());
        assertEquals("TestSuiteName", subTestSuite.getName());
        testCase = subTestSuite.getTestCases().iterator().next();
        assertEquals("TestCaseName", testCase.getName());
        assertEquals("mandatory", testCase.getPriority());
        test = testCase.getTests().iterator().next();
        assertEquals("com.google.TestSuiteName.TestCaseName#testName1", test.getFullName());
        assertEquals("automatic", test.getType());
        assertEquals("testName1", test.getName());
        assertEquals(CtsTestResult.CODE_NOT_EXECUTED, test.getResult().getResultCode());
        deleteTestPackage(mTestPackageBinaryName);
    }
    public void testValidateTestController() {
        String jarPath = "test.jar";
        String packageName = "com.android.tests";
        String className = "ConsoleTests";
        String methodName = "testMethod";
        TestController controller = new TestController(null, null, null, null);
        assertFalse(controller.isValid());
        controller = new TestController(jarPath, null, null, null);
        assertFalse(controller.isValid());
        controller = new TestController(null, packageName, null, null);
        assertFalse(controller.isValid());
        controller = new TestController(null, null, className, null);
        assertFalse(controller.isValid());
        controller = new TestController(null, null, null, methodName);
        assertFalse(controller.isValid());
        controller = new TestController("", "", "", "");
        assertFalse(controller.isValid());
        controller = new TestController("", packageName, className, methodName);
        assertFalse(controller.isValid());
        controller = new TestController(jarPath, "", className, methodName);
        assertFalse(controller.isValid());
        controller = new TestController(jarPath, packageName, "", methodName);
        assertFalse(controller.isValid());
        controller = new TestController(jarPath, packageName, className, "");
        assertFalse(controller.isValid());
        controller = new TestController(jarPath, packageName, className, methodName);
        assertTrue(controller.isValid());
    }
    public void testLoadPlanWithTestController() throws IOException,
            ParserConfigurationException, SAXException,
            TestPlanNotFoundException, TestNotFoundException,
            TransformerFactoryConfigurationError,
            TransformerException, NoSuchAlgorithmException {
        final String appPackageName = "com.google.android.cts";
        final String jarPath = "test.jar";
        final String controllerPackageName = "com.android.tests";
        final String className = "ConsoleTests";
        final String methodName = "testMethod";
        final String description = controllerPackageName + "." + className
                + Test.METHOD_SEPARATOR + methodName;
        final String descriptionConfigStr = "<TestPackage name=\""
                + mTestPackageBinaryName + "\" "
                + " appPackageName=\"" + appPackageName + "\""
                + " version=\"1.0\" AndroidFramework=\"Android 1.0\""
                + " runner=\"android.test.InstrumentationTestRunner\" jarPath=\"" + jarPath
                + "\">\n"
                + "  <Description>something extracted from java doc</Description>\n"
                + "  <TestSuite name=\"com.google\">\n"
                + "     <TestCase name=\"TestCaseName\" priority=\"mandatory\">\n"
                + "         <Description>"
                + "something extracted from java doc" + "</Description>\n"
                + "         <!-- Test Methods -->\n"
                + "         <Test name=\"testName1\""
                + " type=\"automatic\"" + " HostController=\""
                + description + "\"" + "/>\n" + "     </TestCase>\n"
                + "  </TestSuite>\n" + "</TestPackage>\n";
        createTestPackage(descriptionConfigStr, mTestPackageBinaryName);
        HostConfig.getInstance().loadTestPackages();
        ArrayList<String> packageNames = new ArrayList<String>();
        packageNames.add(appPackageName);
        HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
        results.put(appPackageName, null);
        String planName = "plan_test";
        String planPath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSessionBuilder.getInstance().serialize(planName, packageNames, results);
        TestSession ts = TestSessionBuilder.getInstance().build(planPath);
        ts.getSessionLog().setStartTime(System.currentTimeMillis());
        TestSessionLog tsl = ts.getSessionLog();
        TestPackage testPackage = tsl.getTestPackages().iterator().next();
        assertEquals(jarPath, testPackage.getJarPath());
        TestSuite testSuite;
        testSuite = testPackage.getTestSuites().iterator().next();
        TestCase testCase = testSuite.getTestCases().iterator().next();
        Test test = testCase.getTests().iterator().next();
        assertEquals(controllerPackageName, test.getTestController().getPackageName());
        assertEquals(className, test.getTestController().getClassName());
        assertEquals(methodName, test.getTestController().getMethodName());
        deleteTestPackage(mTestPackageBinaryName);
    }
}
