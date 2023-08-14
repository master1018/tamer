public class InstrumentationTestSuiteBuilderTest extends TestCase {
    private InstrumentationTestSuiteBuilder instrumentationTestSuiteBuilder;
    protected void setUp() throws Exception {
        super.setUp();
        instrumentationTestSuiteBuilder = new InstrumentationTestSuiteBuilder(getClass());
    }
    public void testShouldIncludeIntrumentationTests() throws Exception {
        instrumentationTestSuiteBuilder.includePackages(packageFor(InstrumentationTest.class));
        SuiteExecutionRecorder recorder = runSuite(instrumentationTestSuiteBuilder);
        assertEquals(1, recorder.testsSeen.size());
        assertTrue(recorder.saw("InstrumentationTest.testInstrumentation"));
    }
    public void testShouldOnlyIncludeIntrumentationTests() throws Exception {
        TestSuite testSuite = new OuterTest()
                .buildTestsUnderHereWith(instrumentationTestSuiteBuilder);
        List<String> testCaseNames = getTestCaseNames(testSuite);
        assertEquals(1, testCaseNames.size());
        assertEquals("testInstrumentation", testCaseNames.get(0));
    }
    private static String packageFor(Class clazz) {
        String className = clazz.getName();
        return className.substring(0, className.lastIndexOf('.'));
    }
    private SuiteExecutionRecorder runSuite(TestSuiteBuilder builder) {
        TestSuite suite = builder.build();
        SuiteExecutionRecorder recorder = new SuiteExecutionRecorder();
        TestResult result = new TestResult();
        result.addListener(recorder);
        suite.run(result);
        return recorder;
    }
    private class SuiteExecutionRecorder implements TestListener {
        private Set<String> failures = new HashSet<String>();
        private Set<String> errors = new HashSet<String>();
        private Set<String> testsSeen = new HashSet<String>();
        public void addError(Test test, Throwable t) {
            errors.add(testName(test));
        }
        public void addFailure(Test test, AssertionFailedError t) {
            failures.add(testName(test));
        }
        public void endTest(Test test) {
        }
        public void startTest(Test test) {
            testsSeen.add(testName(test));
        }
        public boolean saw(String testName) {
            return testsSeen.contains(testName);
        }
        public boolean failed(String testName) {
            return failures.contains(testName);
        }
        public boolean errored(String testName) {
            return errors.contains(testName);
        }
        public boolean passed(String testName) {
            return saw(testName) && !failed(testName) && !errored(testName);
        }
        private String testName(Test test) {
            TestCase testCase = (TestCase) test;
            return testCase.getClass().getSimpleName() + "." + testCase.getName();
        }
    }
}
