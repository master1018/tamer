public class InstrumentationCtsTestRunner extends InstrumentationTestRunner {
    private static final String TAG = "InstrumentationCtsTestRunner";
    private boolean singleTest = false;
    @Override
    public void onCreate(Bundle arguments) {
        File cacheDir = getTargetContext().getCacheDir();
        System.setProperty("user.language", "en");
        System.setProperty("user.region", "US");
        System.setProperty("java.home", cacheDir.getAbsolutePath());
        System.setProperty("user.home", cacheDir.getAbsolutePath());
        System.setProperty("java.io.tmpdir", cacheDir.getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStore",
                "/etc/security/cacerts.bks");
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        if (arguments != null) {
            String classArg = arguments.getString(ARGUMENT_TEST_CLASS);
            singleTest = classArg != null && classArg.contains("#");
        }
        if (getContext().checkCallingOrSelfPermission(android.Manifest.permission.DISABLE_KEYGUARD)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Disabling keyguard");
            KeyguardManager keyguardManager =
                (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.newKeyguardLock("cts").disableKeyguard();
        } else {
            Log.i(TAG, "Test lacks permission to disable keyguard. " +
                    "UI based tests may fail if keyguard is up");
        }
        super.onCreate(arguments);
    }
    @Override
    protected AndroidTestRunner getAndroidTestRunner() {
        AndroidTestRunner runner = super.getAndroidTestRunner();
        runner.addTestListener(new TestListener() {
            private Class<?> lastClass;
            private static final int MINIMUM_TIME = 100;
            private long startTime;
            public void startTest(Test test) {
                if (test.getClass() != lastClass) {
                    lastClass = test.getClass();
                    printMemory(test.getClass());
                }
                Thread.currentThread().setContextClassLoader(
                        test.getClass().getClassLoader());
                startTime = System.currentTimeMillis();
            }
            public void endTest(Test test) {
                if (test instanceof TestCase) {
                    cleanup((TestCase)test);
                    long timeTaken = System.currentTimeMillis() - startTime;
                    if (timeTaken < MINIMUM_TIME) {
                        try {
                            Thread.sleep(MINIMUM_TIME - timeTaken);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
            public void addError(Test test, Throwable t) {
            }
            public void addFailure(Test test, AssertionFailedError t) {
            }
            private void printMemory(Class<? extends Test> testClass) {
                Runtime runtime = Runtime.getRuntime();
                long total = runtime.totalMemory();
                long free = runtime.freeMemory();
                long used = total - free;
                Log.d(TAG, "Total memory  : " + total);
                Log.d(TAG, "Used memory   : " + used);
                Log.d(TAG, "Free memory   : " + free);
                Log.d(TAG, "Now executing : " + testClass.getName());
            }
            private void cleanup(TestCase test) {
                Class<?> clazz = test.getClass();
                while (clazz != TestCase.class) {
                    Field[] fields = clazz.getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        Field f = fields[i];
                        if (!f.getType().isPrimitive() &&
                                !Modifier.isStatic(f.getModifiers())) {
                            try {
                                f.setAccessible(true);
                                f.set(test, null);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }
        });
        return runner;
    }
    @Override
    List<Predicate<TestMethod>> getBuilderRequirements() {
        List<Predicate<TestMethod>> builderRequirements =
                super.getBuilderRequirements();
        Predicate<TestMethod> brokenTestPredicate =
                Predicates.not(new HasAnnotation(BrokenTest.class));
        builderRequirements.add(brokenTestPredicate);
        if (!singleTest) {
            Predicate<TestMethod> sideEffectPredicate =
                    Predicates.not(new HasAnnotation(SideEffect.class));
            builderRequirements.add(sideEffectPredicate);
        }
        return builderRequirements;
    }
}
