public class CoreTestSuite implements Test {
    public static final int RUN_NORMAL_TESTS = 1;
    public static final int RUN_BROKEN_TESTS = 2;
    public static final int RUN_KNOWN_FAILURES = 4;
    public static final int RUN_ANDROID_ONLY = 8;
    public static final int RUN_SIDE_EFFECTS = 16;
    public static final int RUN_ALL_TESTS = 
            RUN_NORMAL_TESTS | RUN_BROKEN_TESTS | 
            RUN_KNOWN_FAILURES | RUN_SIDE_EFFECTS | RUN_ANDROID_ONLY;
    public static final int INVERT_KNOWN_FAILURES = 32;
    public static final int ISOLATE_ALL = 64;
    public static final int ISOLATE_NONE = 128;
    public static final int VERBOSE = 256;
    public static final int REVERSE = 512;
    public static final int DRY_RUN = 1024;
    private final String name;
    protected int fTotalCount;
    protected int fAndroidOnlyCount;
    protected int fBrokenCount;
    protected int fKnownFailureCount;
    protected int fSideEffectCount;
    protected int fNormalCount;
    protected int fIgnoredCount;
    private Vector<Test> fTests = new Vector<Test>();
    private TestCase fVictim;
    private int fStep;
    private int fFlags;
    public CoreTestSuite(Test suite, int flags, int step, TestCase victim) {
        super();
        name = suite.toString();
        fStep = step;
        addAndFlatten(suite, flags);
        fVictim = victim;
        fFlags = flags;
    }
    private void addAndFlatten(Test test, int flags) {
        if (test instanceof TestSuite) {
            TestSuite suite = (TestSuite)test;
            if ((flags & REVERSE) != 0) {
                for (int i = suite.testCount() - 1; i >= 0; i--) {
                    addAndFlatten(suite.testAt(i), flags);
                }
            } else {
                for (int i = 0; i < suite.testCount(); i++) {
                    addAndFlatten(suite.testAt(i), flags);
                }
            }
        } else if (test instanceof TestCase) {
            TestCase testCase = (TestCase)test;
            boolean ignoreMe = false;
            boolean isAndroidOnly = hasAnnotation(testCase, AndroidOnly.class);
            boolean isBrokenTest = hasAnnotation(testCase, BrokenTest.class);
            boolean isKnownFailure = hasAnnotation(testCase, KnownFailure.class);
            boolean isSideEffect = hasAnnotation(testCase, SideEffect.class);
            boolean isNormalTest = 
                    !(isAndroidOnly || isBrokenTest || isKnownFailure ||
                      isSideEffect);
            if (isAndroidOnly) {
                fAndroidOnlyCount++;
            }
            if (isBrokenTest) {
                fBrokenCount++;
            }
            if (isKnownFailure) {
                fKnownFailureCount++;
            }
            if (isNormalTest) {
                fNormalCount++;
            }
            if (isSideEffect) {
                fSideEffectCount++;
            }
            if ((flags & RUN_ANDROID_ONLY) == 0 && isAndroidOnly) { 
                ignoreMe = true;
            }
            if ((flags & RUN_BROKEN_TESTS) == 0 && isBrokenTest) { 
                ignoreMe = true;
            }
            if ((flags & RUN_KNOWN_FAILURES) == 0 && isKnownFailure) {
                ignoreMe = true;
            }
            if (((flags & RUN_NORMAL_TESTS) == 0) && isNormalTest) {
                ignoreMe = true;
            }
            if (((flags & RUN_SIDE_EFFECTS) == 0) && isSideEffect) {
                ignoreMe = true;
            }
            this.fTotalCount++;
            if (!ignoreMe) {
                fTests.add(test);
            } else {
                this.fIgnoredCount++;
            }
        } else {
            System.out.println("Warning: Don't know how to handle " + 
                    test.getClass().getName() + " " + test.toString());
        }
    }
    @SuppressWarnings("unchecked")
    private boolean hasAnnotation(TestCase test, Class clazz) {
        try {
            Method method = test.getClass().getMethod(test.getName());
            return method.getAnnotation(clazz) != null;
        } catch (Exception e) {
        }
        return false;
    }
    public void run(TestResult result) {
        int i = 0;
        while (fTests.size() != 0 && !result.shouldStop()) {
            TestCase test = (TestCase)fTests.elementAt(i);
            Thread.currentThread().setContextClassLoader(
                    test.getClass().getClassLoader());
            test.run(result);
            fTests.remove(i);
            if (fTests.size() != 0) {
                i = (i + fStep - 1) % fTests.size();
            }
        }
        if (result instanceof CoreTestResult) {
            ((CoreTestResult)result).updateStats(
                    fTotalCount, fAndroidOnlyCount, fBrokenCount,
                    fKnownFailureCount, fNormalCount, fIgnoredCount,
                    fSideEffectCount);
        }
    }
    private void cleanup(TestCase test) {
        Field[] fields = test.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (!f.getType().isPrimitive() &&
                    (f.getModifiers() & Modifier.STATIC) == 0) {
                try {
                    f.setAccessible(true);
                    f.set(test, null);
                } catch (Exception ex) {
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    public Enumeration tests() {
        return fTests.elements();
    }
    public int countTestCases() {
        return fTests.size();
    }
    @Override public String toString() {
        return name;
    }
}
