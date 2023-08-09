public class InstrumentationTestRunner extends Instrumentation implements TestSuiteProvider {
    public static final String ARGUMENT_TEST_CLASS = "class";
    public static final String ARGUMENT_TEST_PACKAGE = "package";
    public static final String ARGUMENT_TEST_SIZE_PREDICATE = "size";
    public static final String ARGUMENT_INCLUDE_PERF = "perf";
    public static final String ARGUMENT_DELAY_MSEC = "delay_msec";
    private static final String SMALL_SUITE = "small";
    private static final String MEDIUM_SUITE = "medium";
    private static final String LARGE_SUITE = "large";
    private static final String ARGUMENT_LOG_ONLY = "log";
    static final String ARGUMENT_ANNOTATION = "annotation";
    static final String ARGUMENT_NOT_ANNOTATION = "notAnnotation";
    private static final float SMALL_SUITE_MAX_RUNTIME = 100;
    private static final float MEDIUM_SUITE_MAX_RUNTIME = 1000;
    public static final String REPORT_VALUE_ID = "InstrumentationTestRunner";
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";
    public static final String REPORT_KEY_NUM_CURRENT = "current";
    public static final String REPORT_KEY_NAME_CLASS = "class";
    public static final String REPORT_KEY_NAME_TEST = "test";
    private static final String REPORT_KEY_RUN_TIME = "runtime";
    private static final String REPORT_KEY_SUITE_ASSIGNMENT = "suiteassignment";
    private static final String REPORT_KEY_COVERAGE_PATH = "coverageFilePath";
    public static final int REPORT_VALUE_RESULT_START = 1;
    public static final int REPORT_VALUE_RESULT_OK = 0;
    public static final int REPORT_VALUE_RESULT_ERROR = -1;
    public static final int REPORT_VALUE_RESULT_FAILURE = -2;
    public static final String REPORT_KEY_STACK = "stack";
    private static final String DEFAULT_COVERAGE_FILE_NAME = "coverage.ec";
    private static final String LOG_TAG = "InstrumentationTestRunner";
    private final Bundle mResults = new Bundle();
    private AndroidTestRunner mTestRunner;
    private boolean mDebug;
    private boolean mJustCount;
    private boolean mSuiteAssignmentMode;
    private int mTestCount;
    private String mPackageOfTests;
    private boolean mCoverage;
    private String mCoverageFilePath;
    private int mDelayMsec;
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        String[] apkPaths =
                {getTargetContext().getPackageCodePath(), getContext().getPackageCodePath()};
        ClassPathPackageInfoSource.setApkPaths(apkPaths);
        Predicate<TestMethod> testSizePredicate = null;
        Predicate<TestMethod> testAnnotationPredicate = null;
        Predicate<TestMethod> testNotAnnotationPredicate = null;
        boolean includePerformance = false;
        String testClassesArg = null;
        boolean logOnly = false;
        if (arguments != null) {
            testClassesArg = arguments.getString(ARGUMENT_TEST_CLASS);
            mDebug = getBooleanArgument(arguments, "debug");
            mJustCount = getBooleanArgument(arguments, "count");
            mSuiteAssignmentMode = getBooleanArgument(arguments, "suiteAssignment");
            mPackageOfTests = arguments.getString(ARGUMENT_TEST_PACKAGE);
            testSizePredicate = getSizePredicateFromArg(
                    arguments.getString(ARGUMENT_TEST_SIZE_PREDICATE));
            testAnnotationPredicate = getAnnotationPredicate(
                    arguments.getString(ARGUMENT_ANNOTATION));
            testNotAnnotationPredicate = getNotAnnotationPredicate(
                    arguments.getString(ARGUMENT_NOT_ANNOTATION));
            includePerformance = getBooleanArgument(arguments, ARGUMENT_INCLUDE_PERF);
            logOnly = getBooleanArgument(arguments, ARGUMENT_LOG_ONLY);
            mCoverage = getBooleanArgument(arguments, "coverage");
            mCoverageFilePath = arguments.getString("coverageFile");
            try {
                Object delay = arguments.get(ARGUMENT_DELAY_MSEC);  
                if (delay != null) mDelayMsec = Integer.parseInt(delay.toString());
            } catch (NumberFormatException e) {
                Log.e(LOG_TAG, "Invalid delay_msec parameter", e);
            }
        }
        TestSuiteBuilder testSuiteBuilder = new TestSuiteBuilder(getClass().getName(),
                getTargetContext().getClassLoader());
        if (testSizePredicate != null) {
            testSuiteBuilder.addRequirements(testSizePredicate);
        }
        if (testAnnotationPredicate != null) {
            testSuiteBuilder.addRequirements(testAnnotationPredicate);
        }
        if (testNotAnnotationPredicate != null) {
            testSuiteBuilder.addRequirements(testNotAnnotationPredicate);
        }
        if (!includePerformance) {
            testSuiteBuilder.addRequirements(REJECT_PERFORMANCE);
        }
        if (testClassesArg == null) {
            if (mPackageOfTests != null) {
                testSuiteBuilder.includePackages(mPackageOfTests);
            } else {
                TestSuite testSuite = getTestSuite();
                if (testSuite != null) {
                    testSuiteBuilder.addTestSuite(testSuite);
                } else {
                    testSuiteBuilder.includePackages("");
                }
            }
        } else {
            parseTestClasses(testClassesArg, testSuiteBuilder);
        }
        testSuiteBuilder.addRequirements(getBuilderRequirements());
        mTestRunner = getAndroidTestRunner();
        mTestRunner.setContext(getTargetContext());
        mTestRunner.setInstrumentation(this);
        mTestRunner.setSkipExecution(logOnly);
        mTestRunner.setTest(testSuiteBuilder.build());
        mTestCount = mTestRunner.getTestCases().size();
        if (mSuiteAssignmentMode) {
            mTestRunner.addTestListener(new SuiteAssignmentPrinter());
        } else {
            WatcherResultPrinter resultPrinter = new WatcherResultPrinter(mTestCount);
            mTestRunner.addTestListener(new TestPrinter("TestRunner", false));
            mTestRunner.addTestListener(resultPrinter);
            mTestRunner.setPerformanceResultsWriter(resultPrinter);
        }
        start();
    }
    List<Predicate<TestMethod>> getBuilderRequirements() {
        return new ArrayList<Predicate<TestMethod>>();
    }
    private void parseTestClasses(String testClassArg, TestSuiteBuilder testSuiteBuilder) {
        String[] testClasses = testClassArg.split(",");
        for (String testClass : testClasses) {
            parseTestClass(testClass, testSuiteBuilder);
        }
    }
    private void parseTestClass(String testClassName, TestSuiteBuilder testSuiteBuilder) {
        int methodSeparatorIndex = testClassName.indexOf('#');
        String testMethodName = null;
        if (methodSeparatorIndex > 0) {
            testMethodName = testClassName.substring(methodSeparatorIndex + 1);
            testClassName = testClassName.substring(0, methodSeparatorIndex);
        }
        testSuiteBuilder.addTestClassByName(testClassName, testMethodName, getTargetContext());
    }
    protected AndroidTestRunner getAndroidTestRunner() {
        return new AndroidTestRunner();
    }
    private boolean getBooleanArgument(Bundle arguments, String tag) {
        String tagString = arguments.getString(tag);
        return tagString != null && Boolean.parseBoolean(tagString);
    }
    private Predicate<TestMethod> getSizePredicateFromArg(String sizeArg) {
        if (SMALL_SUITE.equals(sizeArg)) {
            return TestPredicates.SELECT_SMALL;
        } else if (MEDIUM_SUITE.equals(sizeArg)) {
            return TestPredicates.SELECT_MEDIUM;
        } else if (LARGE_SUITE.equals(sizeArg)) {
            return TestPredicates.SELECT_LARGE;
        } else {
            return null;
        }
    }
    private Predicate<TestMethod> getAnnotationPredicate(String annotationClassName) {
        Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
        if (annotationClass != null) {
            return new HasAnnotation(annotationClass);
        }
        return null;
    }
     private Predicate<TestMethod> getNotAnnotationPredicate(String annotationClassName) {
         Class<? extends Annotation> annotationClass = getAnnotationClass(annotationClassName);
         if (annotationClass != null) {
             return Predicates.not(new HasAnnotation(annotationClass));
         }
         return null;
     }
    private Class<? extends Annotation> getAnnotationClass(String annotationClassName) {
        if (annotationClassName == null) {
            return null;
        }
        try {
           Class<?> annotationClass = Class.forName(annotationClassName);
           if (annotationClass.isAnnotation()) {
               return (Class<? extends Annotation>)annotationClass;
           } else {
               Log.e(LOG_TAG, String.format("Provided annotation value %s is not an Annotation",
                       annotationClassName));
           }
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, String.format("Could not find class for specified annotation %s",
                    annotationClassName));
        }
        return null;
    }
    @Override
    public void onStart() {
        Looper.prepare();
        if (mJustCount) {
            mResults.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            mResults.putInt(REPORT_KEY_NUM_TOTAL, mTestCount);
            finish(Activity.RESULT_OK, mResults);
        } else {
            if (mDebug) {
                Debug.waitForDebugger();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream writer = new PrintStream(byteArrayOutputStream);
            try {
                StringResultPrinter resultPrinter = new StringResultPrinter(writer);
                mTestRunner.addTestListener(resultPrinter);
                long startTime = System.currentTimeMillis();
                mTestRunner.runTest();
                long runTime = System.currentTimeMillis() - startTime;
                resultPrinter.print(mTestRunner.getTestResult(), runTime);
            } finally {
                mResults.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                        String.format("\nTest results for %s=%s",
                        mTestRunner.getTestClassName(),
                        byteArrayOutputStream.toString()));
                if (mCoverage) {
                    generateCoverageReport();
                }
                writer.close();
                finish(Activity.RESULT_OK, mResults);
            }
        }
    }
    public TestSuite getTestSuite() {
        return getAllTests();
    }
    public TestSuite getAllTests() {
        return null;
    }
    public ClassLoader getLoader() {
        return null;
    }
    private void generateCoverageReport() {
        String coverageFilePath = getCoverageFilePath();
        java.io.File coverageFile = new java.io.File(coverageFilePath);
        try {
            Class<?> emmaRTClass = Class.forName("com.vladium.emma.rt.RT");
            Method dumpCoverageMethod = emmaRTClass.getMethod("dumpCoverageData",
                    coverageFile.getClass(), boolean.class, boolean.class);
            dumpCoverageMethod.invoke(null, coverageFile, false, false);
            mResults.putString(REPORT_KEY_COVERAGE_PATH, coverageFilePath);
            final String currentStream = mResults.getString(
                    Instrumentation.REPORT_KEY_STREAMRESULT);
            mResults.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                String.format("%s\nGenerated code coverage data to %s", currentStream,
                coverageFilePath));
        } catch (ClassNotFoundException e) {
            reportEmmaError("Is emma jar on classpath?", e);
        } catch (SecurityException e) {
            reportEmmaError(e);
        } catch (NoSuchMethodException e) {
            reportEmmaError(e);
        } catch (IllegalArgumentException e) {
            reportEmmaError(e);
        } catch (IllegalAccessException e) {
            reportEmmaError(e);
        } catch (InvocationTargetException e) {
            reportEmmaError(e);
        }
    }
    private String getCoverageFilePath() {
        if (mCoverageFilePath == null) {
            return getTargetContext().getFilesDir().getAbsolutePath() + File.separator +
                   DEFAULT_COVERAGE_FILE_NAME;
        } else {
            return mCoverageFilePath;
        }
    }
    private void reportEmmaError(Exception e) {
        reportEmmaError("", e);
    }
    private void reportEmmaError(String hint, Exception e) {
        String msg = "Failed to generate emma coverage. " + hint;
        Log.e(LOG_TAG, msg, e);
        mResults.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\nError: " + msg);
    }
    private class StringResultPrinter extends ResultPrinter {
        public StringResultPrinter(PrintStream writer) {
            super(writer);
        }
        synchronized void print(TestResult result, long runTime) {
            printHeader(runTime);
            printFooter(result);
        }
    }
    private class SuiteAssignmentPrinter implements TestListener {
        private Bundle mTestResult;
        private long mStartTime;
        private long mEndTime;
        private boolean mTimingValid;
        public SuiteAssignmentPrinter() {
        }
        public void startTest(Test test) {
            mTimingValid = true;
            mStartTime = System.currentTimeMillis();
        }
        public void addError(Test test, Throwable t) {
            mTimingValid = false;
        }
        public void addFailure(Test test, AssertionFailedError t) {
            mTimingValid = false;
        }
        public void endTest(Test test) {
            float runTime;
            String assignmentSuite;
            mEndTime = System.currentTimeMillis();
            mTestResult = new Bundle();
            if (!mTimingValid || mStartTime < 0) {
                assignmentSuite = "NA";
                runTime = -1;
            } else {
                runTime = mEndTime - mStartTime;
                if (runTime < SMALL_SUITE_MAX_RUNTIME
                        && !InstrumentationTestCase.class.isAssignableFrom(test.getClass())) {
                    assignmentSuite = SMALL_SUITE;
                } else if (runTime < MEDIUM_SUITE_MAX_RUNTIME) {
                    assignmentSuite = MEDIUM_SUITE;
                } else {
                    assignmentSuite = LARGE_SUITE;
                }
            }
            mStartTime = -1;
            mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                    test.getClass().getName() + "#" + ((TestCase) test).getName()
                    + "\nin " + assignmentSuite + " suite\nrunTime: "
                    + String.valueOf(runTime) + "\n");
            mTestResult.putFloat(REPORT_KEY_RUN_TIME, runTime);
            mTestResult.putString(REPORT_KEY_SUITE_ASSIGNMENT, assignmentSuite);
            sendStatus(0, mTestResult);
        }
    }
    private class WatcherResultPrinter implements TestListener, PerformanceResultsWriter {
        private final Bundle mResultTemplate;
        Bundle mTestResult;
        int mTestNum = 0;
        int mTestResultCode = 0;
        String mTestClass = null;
        PerformanceCollector mPerfCollector = new PerformanceCollector();
        boolean mIsTimedTest = false;
        boolean mIncludeDetailedStats = false;
        public WatcherResultPrinter(int numTests) {
            mResultTemplate = new Bundle();
            mResultTemplate.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            mResultTemplate.putInt(REPORT_KEY_NUM_TOTAL, numTests);
        }
        public void startTest(Test test) {
            String testClass = test.getClass().getName();
            String testName = ((TestCase)test).getName();
            mTestResult = new Bundle(mResultTemplate);
            mTestResult.putString(REPORT_KEY_NAME_CLASS, testClass);
            mTestResult.putString(REPORT_KEY_NAME_TEST, testName);
            mTestResult.putInt(REPORT_KEY_NUM_CURRENT, ++mTestNum);
            if (testClass != null && !testClass.equals(mTestClass)) {
                mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                        String.format("\n%s:", testClass));
                mTestClass = testClass;
            } else {
                mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "");
            }
            try {
                if (mTestNum == 1) Thread.sleep(mDelayMsec);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            sendStatus(REPORT_VALUE_RESULT_START, mTestResult);
            mTestResultCode = 0;
            mIsTimedTest = false;
            mIncludeDetailedStats = false;
            try {
                if (test.getClass().getMethod(testName).isAnnotationPresent(TimedTest.class)) {
                    mIsTimedTest = true;
                    mIncludeDetailedStats = test.getClass().getMethod(testName).getAnnotation(
                            TimedTest.class).includeDetailedStats();
                } else if (test.getClass().isAnnotationPresent(TimedTest.class)) {
                    mIsTimedTest = true;
                    mIncludeDetailedStats = test.getClass().getAnnotation(
                            TimedTest.class).includeDetailedStats();
                }
            } catch (SecurityException e) {
                throw new IllegalStateException(e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
            if (mIsTimedTest && mIncludeDetailedStats) {
                mPerfCollector.beginSnapshot("");
            } else if (mIsTimedTest) {
                mPerfCollector.startTiming("");
            }
        }
        public void addError(Test test, Throwable t) {
            mTestResult.putString(REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            mTestResultCode = REPORT_VALUE_RESULT_ERROR;
            mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                String.format("\nError in %s:\n%s",
                    ((TestCase)test).getName(), BaseTestRunner.getFilteredTrace(t)));
        }
        public void addFailure(Test test, AssertionFailedError t) {
            mTestResult.putString(REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            mTestResultCode = REPORT_VALUE_RESULT_FAILURE;
            mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                String.format("\nFailure in %s:\n%s",
                    ((TestCase)test).getName(), BaseTestRunner.getFilteredTrace(t)));
        }
        public void endTest(Test test) {
            if (mIsTimedTest && mIncludeDetailedStats) {
                mTestResult.putAll(mPerfCollector.endSnapshot());
            } else if (mIsTimedTest) {
                writeStopTiming(mPerfCollector.stopTiming(""));
            }
            if (mTestResultCode == 0) {
                mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, ".");
            }
            sendStatus(mTestResultCode, mTestResult);
            try { 
                Thread.sleep(mDelayMsec);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        public void writeBeginSnapshot(String label) {
        }
        public void writeEndSnapshot(Bundle results) {
            mResults.putAll(results);
        }
        public void writeStartTiming(String label) {
        }
        public void writeStopTiming(Bundle results) {
            int i = 0;
            for (Parcelable p :
                    results.getParcelableArrayList(PerformanceCollector.METRIC_KEY_ITERATIONS)) {
                Bundle iteration = (Bundle)p;
                String index = "iteration" + i + ".";
                mTestResult.putString(index + PerformanceCollector.METRIC_KEY_LABEL,
                        iteration.getString(PerformanceCollector.METRIC_KEY_LABEL));
                mTestResult.putLong(index + PerformanceCollector.METRIC_KEY_CPU_TIME,
                        iteration.getLong(PerformanceCollector.METRIC_KEY_CPU_TIME));
                mTestResult.putLong(index + PerformanceCollector.METRIC_KEY_EXECUTION_TIME,
                        iteration.getLong(PerformanceCollector.METRIC_KEY_EXECUTION_TIME));
                i++;
            }
        }
        public void writeMeasurement(String label, long value) {
            mTestResult.putLong(label, value);
        }
        public void writeMeasurement(String label, float value) {
            mTestResult.putFloat(label, value);
        }
        public void writeMeasurement(String label, String value) {
            mTestResult.putString(label, value);
        }
    }
}
