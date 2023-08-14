public class TestRunner implements PerformanceTestCase.Intermediates {
    public static final int REGRESSION = 0;
    public static final int PERFORMANCE = 1;
    public static final int PROFILING = 2;
    public static final int CLEARSCREEN = 0;
    private static final String TAG = "TestHarness";
    private Context mContext;
    private int mMode = REGRESSION;
    private List<Listener> mListeners = Lists.newArrayList();
    private int mPassed;
    private int mFailed;
    private int mInternalIterations;
    private long mStartTime;
    private long mEndTime;
    private String mClassName;
    List<IntermediateTime> mIntermediates = null;
    private static Class mRunnableClass;
    private static Class mJUnitClass;
    static {
        try {
            mRunnableClass = Class.forName("java.lang.Runnable", false, null);
            mJUnitClass = Class.forName("junit.framework.TestCase", false, null);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }
    }
    public class JunitTestSuite extends TestSuite implements TestListener {
        boolean mError = false;
        public JunitTestSuite() {
            super();
        }
        @Override
        public void run(TestResult result) {
            result.addListener(this);
            super.run(result);
            result.removeListener(this);
        }
        public void startTest(Test test) {
            started(test.toString());
        }
        public void endTest(Test test) {
            finished(test.toString());
            if (!mError) {
                passed(test.toString());
            }
        }
        public void addError(Test test, Throwable t) {
            mError = true;
            failed(test.toString(), t);
        }
        public void addFailure(Test test, junit.framework.AssertionFailedError t) {
            mError = true;
            failed(test.toString(), t);
        }
    }
    public static class IntermediateTime {
        public IntermediateTime(String name, long timeInNS) {
            this.name = name;
            this.timeInNS = timeInNS;
        }
        public String name;
        public long timeInNS;
    }
    public interface Listener {
        void started(String className);
        void finished(String className);
        void performance(String className,
                long itemTimeNS, int iterations,
                List<IntermediateTime> itermediates);
        void passed(String className);
        void failed(String className, Throwable execption);
    }
    public TestRunner(Context context) {
        mContext = context;
    }
    public void addListener(Listener listener) {
        mListeners.add(listener);
    }
    public void startProfiling() {
        File file = new File("/tmp/trace");
        file.mkdir();
        String base = "/tmp/trace/" + mClassName + ".dmtrace";
        Debug.startMethodTracing(base, 8 * 1024 * 1024);
    }
    public void finishProfiling() {
        Debug.stopMethodTracing();
    }
    private void started(String className) {
        int count = mListeners.size();
        for (int i = 0; i < count; i++) {
            mListeners.get(i).started(className);
        }
    }
    private void finished(String className) {
        int count = mListeners.size();
        for (int i = 0; i < count; i++) {
            mListeners.get(i).finished(className);
        }
    }
    private void performance(String className,
            long itemTimeNS,
            int iterations,
            List<IntermediateTime> intermediates) {
        int count = mListeners.size();
        for (int i = 0; i < count; i++) {
            mListeners.get(i).performance(className,
                    itemTimeNS,
                    iterations,
                    intermediates);
        }
    }
    public void passed(String className) {
        mPassed++;
        int count = mListeners.size();
        for (int i = 0; i < count; i++) {
            mListeners.get(i).passed(className);
        }
    }
    public void failed(String className, Throwable exception) {
        mFailed++;
        int count = mListeners.size();
        for (int i = 0; i < count; i++) {
            mListeners.get(i).failed(className, exception);
        }
    }
    public int passedCount() {
        return mPassed;
    }
    public int failedCount() {
        return mFailed;
    }
    public void run(String[] classes) {
        for (String cl : classes) {
            run(cl);
        }
    }
    public void setInternalIterations(int count) {
        mInternalIterations = count;
    }
    public void startTiming(boolean realTime) {
        if (realTime) {
            mStartTime = System.currentTimeMillis();
        } else {
            mStartTime = SystemClock.currentThreadTimeMillis();
        }
    }
    public void addIntermediate(String name) {
        addIntermediate(name, (System.currentTimeMillis() - mStartTime) * 1000000);
    }
    public void addIntermediate(String name, long timeInNS) {
        mIntermediates.add(new IntermediateTime(name, timeInNS));
    }
    public void finishTiming(boolean realTime) {
        if (realTime) {
            mEndTime = System.currentTimeMillis();
        } else {
            mEndTime = SystemClock.currentThreadTimeMillis();
        }
    }
    public void setPerformanceMode(int mode) {
        mMode = mode;
    }
    private void missingTest(String className, Throwable e) {
        started(className);
        finished(className);
        failed(className, e);
    }
    public void run(String className) {
        try {
            mClassName = className;
            Class clazz = mContext.getClassLoader().loadClass(className);
            Method method = getChildrenMethod(clazz);
            if (method != null) {
                String[] children = getChildren(method);
                run(children);
            } else if (mRunnableClass.isAssignableFrom(clazz)) {
                Runnable test = (Runnable) clazz.newInstance();
                TestCase testcase = null;
                if (test instanceof TestCase) {
                    testcase = (TestCase) test;
                }
                Throwable e = null;
                boolean didSetup = false;
                started(className);
                try {
                    if (testcase != null) {
                        testcase.setUp(mContext);
                        didSetup = true;
                    }
                    if (mMode == PERFORMANCE) {
                        runInPerformanceMode(test, className, false, className);
                    } else if (mMode == PROFILING) {
                        startProfiling();
                        test.run();
                        finishProfiling();
                    } else {
                        test.run();
                    }
                } catch (Throwable ex) {
                    e = ex;
                }
                if (testcase != null && didSetup) {
                    try {
                        testcase.tearDown();
                    } catch (Throwable ex) {
                        e = ex;
                    }
                }
                finished(className);
                if (e == null) {
                    passed(className);
                } else {
                    failed(className, e);
                }
            } else if (mJUnitClass.isAssignableFrom(clazz)) {
                Throwable e = null;
                JunitTestSuite suite = new JunitTestSuite();
                Method[] methods = getAllTestMethods(clazz);
                for (Method m : methods) {
                    junit.framework.TestCase test = (junit.framework.TestCase) clazz.newInstance();
                    test.setName(m.getName());
                    if (test instanceof AndroidTestCase) {
                        AndroidTestCase testcase = (AndroidTestCase) test;
                        try {
                            testcase.setContext(mContext);
                            testcase.setTestContext(mContext);
                        } catch (Exception ex) {
                            Log.i("TestHarness", ex.toString());
                        }
                    }
                    suite.addTest(test);
                }
                if (mMode == PERFORMANCE) {
                    final int testCount = suite.testCount();
                    for (int j = 0; j < testCount; j++) {
                        Test test = suite.testAt(j);
                        started(test.toString());
                        try {
                            runInPerformanceMode(test, className, true, test.toString());
                        } catch (Throwable ex) {
                            e = ex;
                        }
                        finished(test.toString());
                        if (e == null) {
                            passed(test.toString());
                        } else {
                            failed(test.toString(), e);
                        }
                    }
                } else if (mMode == PROFILING) {
                    startProfiling();
                    junit.textui.TestRunner.run(suite);
                    finishProfiling();
                } else {
                    junit.textui.TestRunner.run(suite);
                }
            } else {
                System.out.println("Test wasn't Runnable and didn't have a"
                        + " children method: " + className);
            }
        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFoundException for " + className, e.toString());
            if (isJunitTest(className)) {
                runSingleJunitTest(className);
            } else {
                missingTest(className, e);
            }
        } catch (InstantiationException e) {
            System.out.println("InstantiationException for " + className);
            missingTest(className, e);
        } catch (IllegalAccessException e) {
            System.out.println("IllegalAccessException for " + className);
            missingTest(className, e);
        }
    }
    public void runInPerformanceMode(Object testCase, String className, boolean junitTest,
            String testNameInDb) throws Exception {
        boolean increaseIterations = true;
        int iterations = 1;
        long duration = 0;
        mIntermediates = null;
        mInternalIterations = 1;
        Class clazz = mContext.getClassLoader().loadClass(className);
        Object perftest = clazz.newInstance();
        PerformanceTestCase perftestcase = null;
        if (perftest instanceof PerformanceTestCase) {
            perftestcase = (PerformanceTestCase) perftest;
            if (mMode == REGRESSION && perftestcase.isPerformanceOnly()) return;
        }
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        if (perftestcase != null) {
            mIntermediates = new ArrayList<IntermediateTime>();
            iterations = perftestcase.startPerformance(this);
            if (iterations > 0) {
                increaseIterations = false;
            } else {
                iterations = 1;
            }
        }
        Thread.sleep(1000);
        do {
            mEndTime = 0;
            if (increaseIterations) {
                mStartTime = SystemClock.currentThreadTimeMillis();
            } else {
                mStartTime = 0;
            }
            if (junitTest) {
                for (int i = 0; i < iterations; i++) {
                    junit.textui.TestRunner.run((junit.framework.Test) testCase);
                }
            } else {
                Runnable test = (Runnable) testCase;
                for (int i = 0; i < iterations; i++) {
                    test.run();
                }
            }
            long endTime = mEndTime;
            if (endTime == 0) {
                endTime = SystemClock.currentThreadTimeMillis();
            }
            duration = endTime - mStartTime;
            if (!increaseIterations) {
                break;
            }
            if (duration <= 1) {
                iterations *= 1000;
            } else if (duration <= 10) {
                iterations *= 100;
            } else if (duration < 100) {
                iterations *= 10;
            } else if (duration < 1000) {
                iterations *= (int) ((1000 / duration) + 2);
            } else {
                break;
            }
        } while (true);
        if (duration != 0) {
            iterations *= mInternalIterations;
            performance(testNameInDb, (duration * 1000000) / iterations,
                    iterations, mIntermediates);
        }
    }
    public void runSingleJunitTest(String className) {
        Throwable excep = null;
        int index = className.lastIndexOf('$');
        String testName = "";
        String originalClassName = className;
        if (index >= 0) {
            className = className.substring(0, index);
            testName = originalClassName.substring(index + 1);
        }
        try {
            Class clazz = mContext.getClassLoader().loadClass(className);
            if (mJUnitClass.isAssignableFrom(clazz)) {
                junit.framework.TestCase test = (junit.framework.TestCase) clazz.newInstance();
                JunitTestSuite newSuite = new JunitTestSuite();
                test.setName(testName);
                if (test instanceof AndroidTestCase) {
                    AndroidTestCase testcase = (AndroidTestCase) test;
                    try {
                        testcase.setContext(mContext);
                    } catch (Exception ex) {
                        Log.w(TAG, "Exception encountered while trying to set the context.", ex);
                    }
                }
                newSuite.addTest(test);
                if (mMode == PERFORMANCE) {
                    try {
                        started(test.toString());
                        runInPerformanceMode(test, className, true, test.toString());
                        finished(test.toString());
                        if (excep == null) {
                            passed(test.toString());
                        } else {
                            failed(test.toString(), excep);
                        }
                    } catch (Throwable ex) {
                        excep = ex;
                    }
                } else if (mMode == PROFILING) {
                    startProfiling();
                    junit.textui.TestRunner.run(newSuite);
                    finishProfiling();
                } else {
                    junit.textui.TestRunner.run(newSuite);
                }
            }
        } catch (ClassNotFoundException e) {
            Log.e("TestHarness", "No test case to run", e);
        } catch (IllegalAccessException e) {
            Log.e("TestHarness", "Illegal Access Exception", e);
        } catch (InstantiationException e) {
            Log.e("TestHarness", "Instantiation Exception", e);
        }
    }
    public static Method getChildrenMethod(Class clazz) {
        try {
            return clazz.getMethod("children", (Class[]) null);
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
    public static Method getChildrenMethod(Context c, String className) {
        try {
            return getChildrenMethod(c.getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
        }
        return null;
    }
    public static String[] getChildren(Context c, String className) {
        Method m = getChildrenMethod(c, className);
        String[] testChildren = getTestChildren(c, className);
        if (m == null & testChildren == null) {
            throw new RuntimeException("couldn't get children method for "
                    + className);
        }
        if (m != null) {
            String[] children = getChildren(m);
            if (testChildren != null) {
                String[] allChildren = new String[testChildren.length + children.length];
                System.arraycopy(children, 0, allChildren, 0, children.length);
                System.arraycopy(testChildren, 0, allChildren, children.length, testChildren.length);
                return allChildren;
            } else {
                return children;
            }
        } else {
            if (testChildren != null) {
                return testChildren;
            }
        }
        return null;
    }
    public static String[] getChildren(Method m) {
        try {
            if (!Modifier.isStatic(m.getModifiers())) {
                throw new RuntimeException("children method is not static");
            }
            return (String[]) m.invoke(null, (Object[]) null);
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return new String[0];
    }
    public static String[] getTestChildren(Context c, String className) {
        try {
            Class clazz = c.getClassLoader().loadClass(className);
            if (mJUnitClass.isAssignableFrom(clazz)) {
                return getTestChildren(clazz);
            }
        } catch (ClassNotFoundException e) {
            Log.e("TestHarness", "No class found", e);
        }
        return null;
    }
    public static String[] getTestChildren(Class clazz) {
        Method[] methods = getAllTestMethods(clazz);
        String[] onScreenTestNames = new String[methods.length];
        int index = 0;
        for (Method m : methods) {
            onScreenTestNames[index] = clazz.getName() + "$" + m.getName();
            index++;
        }
        return onScreenTestNames;
    }
    public static Method[] getAllTestMethods(Class clazz) {
        Method[] allMethods = clazz.getDeclaredMethods();
        int numOfMethods = 0;
        for (Method m : allMethods) {
            boolean mTrue = isTestMethod(m);
            if (mTrue) {
                numOfMethods++;
            }
        }
        int index = 0;
        Method[] testMethods = new Method[numOfMethods];
        for (Method m : allMethods) {
            boolean mTrue = isTestMethod(m);
            if (mTrue) {
                testMethods[index] = m;
                index++;
            }
        }
        return testMethods;
    }
    private static boolean isTestMethod(Method m) {
        return m.getName().startsWith("test") &&
                m.getReturnType() == void.class &&
                m.getParameterTypes().length == 0;
    }
    public static int countJunitTests(Class clazz) {
        Method[] allTestMethods = getAllTestMethods(clazz);
        int numberofMethods = allTestMethods.length;
        return numberofMethods;
    }
    public static boolean isTestSuite(Context c, String className) {
        boolean childrenMethods = getChildrenMethod(c, className) != null;
        try {
            Class clazz = c.getClassLoader().loadClass(className);
            if (mJUnitClass.isAssignableFrom(clazz)) {
                int numTests = countJunitTests(clazz);
                if (numTests > 0)
                    childrenMethods = true;
            }
        } catch (ClassNotFoundException e) {
        }
        return childrenMethods;
    }
    public boolean isJunitTest(String className) {
        int index = className.lastIndexOf('$');
        if (index >= 0) {
            className = className.substring(0, index);
        }
        try {
            Class clazz = mContext.getClassLoader().loadClass(className);
            if (mJUnitClass.isAssignableFrom(clazz)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
        }
        return false;
    }
    public static int countTests(Context c, String className) {
        try {
            Class clazz = c.getClassLoader().loadClass(className);
            Method method = getChildrenMethod(clazz);
            if (method != null) {
                String[] children = getChildren(method);
                int rv = 0;
                for (String child : children) {
                    rv += countTests(c, child);
                }
                return rv;
            } else if (mRunnableClass.isAssignableFrom(clazz)) {
                return 1;
            } else if (mJUnitClass.isAssignableFrom(clazz)) {
                return countJunitTests(clazz);
            }
        } catch (ClassNotFoundException e) {
            return 1; 
        }
        return 0;
    }
    public static String getTitle(String className) {
        int indexDot = className.lastIndexOf('.');
        int indexDollar = className.lastIndexOf('$');
        int index = indexDot > indexDollar ? indexDot : indexDollar;
        if (index >= 0) {
            className = className.substring(index + 1);
        }
        return className;
    }
}
