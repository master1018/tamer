public class AndroidTestRunnerTest extends TestCase {
    private AndroidTestRunner mAndroidTestRunner;
    private StubContext mStubContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStubContext = new StubContext(getClass().getClassLoader());
        mAndroidTestRunner = new AndroidTestRunner();
        mAndroidTestRunner.setContext(mStubContext);
    }
    public void testLoadNoTestCases() throws Exception {
        mAndroidTestRunner.setTestClassName(TestSuite.class.getName(), null);
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        assertNotNull(testCases);
        assertEquals(1, testCases.size());
        assertEquals("warning", testCases.get(0).getName());
        assertEquals(TestSuite.class.getSimpleName(), mAndroidTestRunner.getTestClassName());
    }
    public void testSetTestSuiteWithOneTestCase() throws Exception {
        mAndroidTestRunner.setTestClassName(OneTestTestCase.class.getName(), null);
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        assertNotNull(testCases);
        assertEquals(1, testCases.size());
        assertEquals("testOne", testCases.get(0).getName());
        assertEquals(OneTestTestCase.class.getSimpleName(), mAndroidTestRunner.getTestClassName());
    }
    public void testRunTest() throws Exception {
        mAndroidTestRunner.setTestClassName(OneTestTestCase.class.getName(), null);
        TestListenerStub testListenerStub = new TestListenerStub();
        mAndroidTestRunner.addTestListener(testListenerStub);
        mAndroidTestRunner.runTest();
        assertTrue(testListenerStub.saw("testOne"));
    }
    public void testRunTestWithAndroidTestCase() throws Exception {
        mAndroidTestRunner.setTestClassName(
                OneAndroidTestTestCase.class.getName(), "testOneAndroid");
        TestListenerStub testListenerStub = new TestListenerStub();
        mAndroidTestRunner.addTestListener(testListenerStub);
        assertNull(((AndroidTestCase) mAndroidTestRunner.getTestCases().get(0)).getContext());
        mAndroidTestRunner.runTest();
        assertTrue(testListenerStub.saw("testOneAndroid"));
        assertSame(mStubContext,
                ((AndroidTestCase) mAndroidTestRunner.getTestCases().get(0)).getContext());
    }
    public void testRunTestWithAndroidTestCaseInSuite() throws Exception {
        mAndroidTestRunner.setTestClassName(OneAndroidTestTestCase.class.getName(), null);
        TestListenerStub testListenerStub = new TestListenerStub();
        mAndroidTestRunner.addTestListener(testListenerStub);
        mAndroidTestRunner.runTest();
        assertTrue(testListenerStub.saw("testOneAndroid"));
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        for (TestCase testCase : testCases) {
            assertSame(mStubContext, ((AndroidTestCase) testCase).getContext());
        }
    }
    public void testRunTestWithAndroidTestCaseInNestedSuite() throws Exception {
        mAndroidTestRunner.setTestClassName(AndroidTestCaseTestSuite.class.getName(), null);
        TestListenerStub testListenerStub = new TestListenerStub();
        mAndroidTestRunner.addTestListener(testListenerStub);
        mAndroidTestRunner.runTest();
        assertTrue(testListenerStub.saw("testOneAndroid"));
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        for (TestCase testCase : testCases) {
            assertSame(mStubContext, ((AndroidTestCase) testCase).getContext());
        }
    }
    public void testRunTestWithNullListener() throws Exception {
        mAndroidTestRunner.setTestClassName(OneTestTestCase.class.getName(), null);
        mAndroidTestRunner.addTestListener(null);
        try {
            mAndroidTestRunner.runTest();
        } catch (NullPointerException e) {
            fail("Should not add a null TestListener");
        }
    }
    public void testSetTestClassWithTestSuiteProvider() throws Exception {
        mAndroidTestRunner.setTestClassName(SampleTestSuiteProvider.class.getName(), null);
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        List<String> testNames = Lists.newArrayList();
        for (TestCase testCase : testCases) {
            testNames.add(testCase.getName());
        }
        assertEquals(Arrays.asList("testOne"), testNames);
    }
    public void testSetTestClassWithTestSuite() throws Exception {
        mAndroidTestRunner.setTestClassName(SampleTestSuite.class.getName(), null);
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        List<String> testNames = Lists.newArrayList();
        for (TestCase testCase : testCases) {
            testNames.add(testCase.getName());
        }
        assertEquals(Arrays.asList("testOne", "testOne", "testTwo"), testNames);
    }
    public void testRunSingleTestMethod() throws Exception {
        String testMethodName = "testTwo";
        mAndroidTestRunner.setTestClassName(TwoTestTestCase.class.getName(), testMethodName);
        List<TestCase> testCases = mAndroidTestRunner.getTestCases();
        List<String> testNames = Lists.newArrayList();
        for (TestCase testCase : testCases) {
            testNames.add(testCase.getName());
        }
        assertEquals(Arrays.asList(testMethodName), testNames);
    }
    public void testSetTestClassInvalidClass() throws Exception {
        try {
            mAndroidTestRunner.setTestClassName("class.that.does.not.exist", null);
            fail("expected exception not thrown");
        } catch (RuntimeException e) {
        }
    }
    public void testRunSkipExecution() throws Exception {
        String testMethodName = "testFail";
        mAndroidTestRunner.setTestClassName(
                OnePassOneErrorOneFailTestCase.class.getName(), testMethodName);
        TestListenerStub testListenerStub = new TestListenerStub();
        mAndroidTestRunner.addTestListener(testListenerStub);
        mAndroidTestRunner.runTest();
        assertTrue(testListenerStub.saw("testFail"));
    }
    public static class SampleTestSuiteProvider implements TestSuiteProvider {
        public TestSuite getTestSuite() {
            TestSuite testSuite = new TestSuite();
            testSuite.addTestSuite(OneTestTestCase.class);
            return testSuite;
        }
        public static Test suite() {
            return SampleTestSuite.suite();
        }
    }
    public static class SampleTestSuite {
        public static TestSuite suite() {
            TestSuite testSuite = new TestSuite();
            testSuite.addTestSuite(OneTestTestCase.class);
            testSuite.addTestSuite(TwoTestTestCase.class);
            return testSuite;
        }
    }
    public static class AndroidTestCaseTestSuite {
        public static TestSuite suite() {
            TestSuite testSuite = new TestSuite();
            testSuite.addTestSuite(OneAndroidTestTestCase.class);
            return testSuite;
        }
    }
    public static class OneAndroidTestTestCase extends AndroidTestCase {
        public void testOneAndroid() throws Exception {
        }
    }
    public static class OneTestTestCase extends TestCase {
        public void testOne() throws Exception {
        }
    }
    public static class TwoTestTestCase extends TestCase {
        public void testOne() throws Exception {
        }
        public void testTwo() throws Exception {
        }
    }
    public static class OnePassOneErrorOneFailTestCase extends TestCase {
        public void testPass() throws Exception {
        }
        public void testError() throws Exception {
            throw new Exception();
        }
        public void testFail() throws Exception {
            fail();
        }
    }
    private static class TestListenerStub implements TestListener {
        List<String> testNames = Lists.newArrayList();
        public void addError(Test test, Throwable t) {
        }
        public void addFailure(Test test, AssertionFailedError t) {
        }
        public void endTest(Test test) {
        }
        public void startTest(Test test) {
            if (test instanceof TestCase) {
                testNames.add(((TestCase) test).getName());
            } else if (test instanceof TestSuite) {
                testNames.add(((TestSuite) test).getName());
            }
        }
        public boolean saw(String testName) {
            return testNames.contains(testName);
        }
    }
    private static class StubContext extends MockContext {
        private ClassLoader mClassLoader;
        public StubContext(ClassLoader classLoader) {
            this.mClassLoader = classLoader;
        }
        @Override
        public ClassLoader getClassLoader() {
            return mClassLoader;
        }
    }
}
