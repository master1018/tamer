public class TestSuiteBuilder {
    private Context context;
    private final TestGrouping testGrouping = new TestGrouping(SORT_BY_FULLY_QUALIFIED_NAME);
    private final Set<Predicate<TestMethod>> predicates = new HashSet<Predicate<TestMethod>>();
    private List<TestCase> testCases;
    private TestSuite rootSuite;
    private TestSuite suiteForCurrentClass;
    private String currentClassname;
    private String suiteName;
    public TestSuiteBuilder(Class clazz) {
        this(clazz.getName(), clazz.getClassLoader());
    }
    public TestSuiteBuilder(String name, ClassLoader classLoader) {
        this.suiteName = name;
        this.testGrouping.setClassLoader(classLoader);
        this.testCases = Lists.newArrayList();
        addRequirements(REJECT_SUPPRESSED);
    }
    public TestSuiteBuilder addTestClassByName(String testClassName, String testMethodName, 
            Context context) {
        AndroidTestRunner atr = new AndroidTestRunner();
        atr.setContext(context);
        atr.setTestClassName(testClassName, testMethodName);
        this.testCases.addAll(atr.getTestCases());
        return this;
    }
    public TestSuiteBuilder addTestSuite(TestSuite testSuite) {
        for (TestCase testCase : (List<TestCase>) TestCaseUtil.getTests(testSuite, true)) {
            this.testCases.add(testCase);
        }
        return this;
    }
    public TestSuiteBuilder includePackages(String... packageNames) {
        testGrouping.addPackagesRecursive(packageNames);
        return this;
    }
    public TestSuiteBuilder excludePackages(String... packageNames) {
        testGrouping.removePackagesRecursive(packageNames);
        return this;
    }
    public TestSuiteBuilder addRequirements(List<Predicate<TestMethod>> predicates) {
        this.predicates.addAll(predicates);
        return this;
    }
    public final TestSuiteBuilder includeAllPackagesUnderHere() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callingClassName = null;
        String thisClassName = TestSuiteBuilder.class.getName();
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            if (thisClassName.equals(element.getClassName())
                    && "includeAllPackagesUnderHere".equals(element.getMethodName())) {
                callingClassName = stackTraceElements[i + 1].getClassName();
                break;
            }
        }
        String packageName = parsePackageNameFromClassName(callingClassName);
        return includePackages(packageName);
    }
    public TestSuiteBuilder named(String newSuiteName) {
        suiteName = newSuiteName;
        return this;
    }
    public final TestSuite build() {
        rootSuite = new TestSuite(getSuiteName());
        currentClassname = null;
        try {
            for (TestMethod test : testGrouping.getTests()) {
                if (satisfiesAllPredicates(test)) {
                    addTest(test);
                }
            }
            if (testCases.size() > 0) {
                for (TestCase testCase : testCases) {
                    if (satisfiesAllPredicates(new TestMethod(testCase))) {
                        addTest(testCase);
                    }
                }
            }
        } catch (Exception exception) {
            Log.i("TestSuiteBuilder", "Failed to create test.", exception);
            TestSuite suite = new TestSuite(getSuiteName());
            suite.addTest(new FailedToCreateTests(exception));
            return suite;
        }
        return rootSuite;
    }
    protected String getSuiteName() {
        return suiteName;
    }
    public final TestSuiteBuilder addRequirements(Predicate<TestMethod>... predicates) {
        ArrayList<Predicate<TestMethod>> list = new ArrayList<Predicate<TestMethod>>();
        Collections.addAll(list, predicates);
        return addRequirements(list);
    }
    public static class FailedToCreateTests extends TestCase {
        private final Exception exception;
        public FailedToCreateTests(Exception exception) {
            super("testSuiteConstructionFailed");
            this.exception = exception;
        }
        public void testSuiteConstructionFailed() {
            throw new RuntimeException("Exception during suite construction", exception);
        }
    }
    protected TestGrouping getTestGrouping() {
        return testGrouping;
    }
    private boolean satisfiesAllPredicates(TestMethod test) {
        for (Predicate<TestMethod> predicate : predicates) {
            if (!predicate.apply(test)) {
                return false;
            }
        }
        return true;
    }
    private void addTest(TestMethod testMethod) throws Exception {
        addSuiteIfNecessary(testMethod.getEnclosingClassname());
        suiteForCurrentClass.addTest(testMethod.createTest());
    }
    private void addTest(Test test) {
        addSuiteIfNecessary(test.getClass().getName());
        suiteForCurrentClass.addTest(test);
    }
    private void addSuiteIfNecessary(String parentClassname) {
        if (!parentClassname.equals(currentClassname)) {
            currentClassname = parentClassname;
            suiteForCurrentClass = new TestSuite(parentClassname);
            rootSuite.addTest(suiteForCurrentClass);
        }
    }
    private static String parsePackageNameFromClassName(String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }
}
