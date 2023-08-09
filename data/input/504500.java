public class TestSessionLogBuilder extends XMLResourceHandler {
    private static TestSessionLogBuilder sInstance;
    private DocumentBuilder mDocBuilder;
    public static TestSessionLogBuilder getInstance()
            throws ParserConfigurationException {
        if (sInstance == null) {
            sInstance = new TestSessionLogBuilder();
        }
        return sInstance;
    }
    private TestSessionLogBuilder() throws ParserConfigurationException {
        mDocBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    public TestSessionLog build(final String resultFilePath) throws SAXException, IOException,
            TestPlanNotFoundException, TestNotFoundException,
            NoSuchAlgorithmException, ParserConfigurationException {
        File file = new File(resultFilePath);
        if (!file.exists()) {
            throw new TestPlanNotFoundException();
        }
        Document doc = mDocBuilder.parse(file);
        return loadSessionLog(doc);
    }
    private TestSessionLog loadSessionLog(Document doc)
                throws NoSuchAlgorithmException, ParserConfigurationException,
                SAXException, IOException, TestPlanNotFoundException,
                TestNotFoundException {
        ArrayList<TestPackage> pkgsFromResult = new ArrayList<TestPackage>();
        NodeList resultList = doc.getElementsByTagName(TestSessionLog.TAG_TEST_RESULT);
        Node resultNode = resultList.item(0);
        String planName = getStringAttributeValue(resultNode, TestSessionLog.ATTRIBUTE_TESTPLAN);
        String start = getStringAttributeValue(resultNode, TestSessionLog.ATTRIBUTE_STARTTIME);
        String end = getStringAttributeValue(resultNode, TestSessionLog.ATTRIBUTE_ENDTIME);
        String planFilePath = HostConfig.getInstance().getPlanRepository().getPlanPath(planName);
        TestSession sessionFromPlan = TestSessionBuilder.getInstance().build(planFilePath);
        NodeList pkgList = resultNode.getChildNodes();
        for (int i = 0; i < pkgList.getLength(); i++) {
            Node pkgNode = pkgList.item(i);
            if (pkgNode.getNodeType() == Document.ELEMENT_NODE
                    && TestSessionLog.TAG_TESTPACKAGE.equals(pkgNode.getNodeName())) {
                TestPackage pkg = TestSessionBuilder.getInstance().loadPackage(pkgNode, null);
                if (pkg != null) {
                    pkgsFromResult.add(pkg);
                }
            }
        }
        Collection<TestPackage> pkgsFromPlan = sessionFromPlan.getSessionLog().getTestPackages();
        for (TestPackage pkgFromPlan : pkgsFromPlan) {
            for (TestPackage pkgFromResult : pkgsFromResult) {
                if (pkgFromPlan.getAppPackageName().equals(pkgFromResult.getAppPackageName())) {
                    Collection<Test> testsFromPlan = pkgFromPlan.getTests();
                    Collection<Test> testsFromResult = pkgFromResult.getTests();
                    for (Test testFromPlan : testsFromPlan) {
                        for (Test testFromResult : testsFromResult) {
                            if (testFromPlan.getFullName().equals(testFromResult.getFullName())) {
                                CtsTestResult result = testFromResult.getResult();
                                testFromPlan.addResult(testFromResult.getResult());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        TestSessionLog log = new TestSessionLog(pkgsFromPlan, planName);
        try {
            log.setStartTime(HostUtils.dateFromString(start).getTime());
            log.setEndTime(HostUtils.dateFromString(end).getTime());
        } catch (NullPointerException ignored) {
        } catch (ParseException ignored) {
        }
        return log;
    }
}
