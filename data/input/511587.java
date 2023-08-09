public class HostUnitTestRunner extends BaseTestRunner{
    private static String JAR_SUFFIX = ".jar";
    private TestCase mTestCase;
    private HostSideOnlyTest mTest;
    public HostUnitTestRunner(HostSideOnlyTest test) {
        mTestCase = null;
        mTest = test;
    }
    public TestResult runTest(final String jarPath, final String testPkgName,
            String testClassName, String testMethodName)
            throws ClassNotFoundException, IOException {
        TestResult result = new TestResult();
        loadTestCase(jarPath, testPkgName, testClassName, testMethodName);
        if (mTestCase != null) {
            if (mTestCase instanceof DeviceTest) {
                DeviceTest deviceTest = (DeviceTest) mTestCase;
                deviceTest.setDevice(mTest.mDevice.getDevice());
                deviceTest.setTestAppPath(HostConfig.getInstance().getCaseRepository().getRoot());
            }
            mTestCase.run(result);
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public TestCase loadTestCase(final String jarPath,
            final String testPkgName, final String testClassName,
            final String testMethodName) throws ClassNotFoundException, IOException {
        Log.d("jarPath=" + jarPath + ",testPkgName=" + testPkgName
                + ",testClassName=" + testClassName);
        Class testClass = null;
        if ((jarPath != null) && (jarPath.endsWith(JAR_SUFFIX))) {
            testClass = loadClass(jarPath, testPkgName, testClassName);
        } else {
            testClass = Class.forName(testPkgName + "." + testClassName);
        }
        if ((testMethodName != null) && TestCase.class.isAssignableFrom(testClass)) {
            mTestCase = buildTestMethod(testClass, testMethodName);
        }
        return mTestCase;
    }
    @SuppressWarnings("unchecked")
    public Class loadClass(final String jarPath,
            final String testPkgName, final String testClassName)
            throws ClassNotFoundException, IOException {
        URL urls[] = { new File(jarPath).getCanonicalFile().toURI().toURL() };
        URLClassLoader cl = new URLClassLoader(urls);
        Class testClass = cl.loadClass(testPkgName + "." + testClassName);
        Log.d("succeed in load jarred class: " + jarPath + "." + testPkgName
                + "." + testClassName);
        return testClass;
    }
    @SuppressWarnings("unchecked")
    private TestCase buildTestMethod(Class testClass,
            String testMethodName) {
        try {
            TestCase testCase = (TestCase) testClass.newInstance();
            testCase.setName(testMethodName);
            return testCase;
        } catch (IllegalAccessException e) {
            runFailed("Could not access test class. Class: "
                    + testClass.getName());
        } catch (InstantiationException e) {
            runFailed("Could not instantiate test class. Class: "
                    + testClass.getName());
        }
        return null;
    }
    @Override
    public void testStarted(String testName) {
    }
    @Override
    public void testEnded(String testName) {
    }
    @Override
    public void testFailed(int status, Test test, Throwable t) {
    }
    @Override
    protected void runFailed(String message) {
        throw new RuntimeException(message);
    }
}
