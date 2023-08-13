public class TestCaseUtil {
    private TestCaseUtil() {
    }
    @SuppressWarnings("unchecked")
    public static List<String> getTestCaseNames(Test test, boolean flatten) {
        List<Test> tests = (List<Test>) getTests(test, flatten);
        List<String> testCaseNames = Lists.newArrayList();
        for (Test aTest : tests) {
            testCaseNames.add(getTestName(aTest));
        }
        return testCaseNames;
    }
    public static List<? extends Test> getTests(Test test, boolean flatten) {
        return getTests(test, flatten, new HashSet<Class<?>>());
    }
    private static List<? extends Test> getTests(Test test, boolean flatten,
            Set<Class<?>> seen) {
        List<Test> testCases = Lists.newArrayList();
        if (test != null) {
            Test workingTest = null;
            if (test instanceof TestCase &&
                    ((TestCase)test).getName() == null) {
                workingTest = invokeSuiteMethodIfPossible(test.getClass(), 
                        seen);
            }
            if (workingTest == null) {
                workingTest = test;
            }
            if (workingTest instanceof TestSuite) {
                TestSuite testSuite = (TestSuite) workingTest;
                Enumeration enumeration = testSuite.tests();
                while (enumeration.hasMoreElements()) {
                    Test childTest = (Test) enumeration.nextElement();
                    if (flatten) {
                        testCases.addAll(getTests(childTest, flatten, seen));
                    } else {
                        testCases.add(childTest);
                    }
                }
            } else {
                testCases.add(workingTest);
            }
        }
        return testCases;
    }
    private static Test invokeSuiteMethodIfPossible(Class testClass,
            Set<Class<?>> seen) {
        try {
            Method suiteMethod = testClass.getMethod(
                    BaseTestRunner.SUITE_METHODNAME, new Class[0]);
            if (Modifier.isStatic(suiteMethod.getModifiers())
                    && !seen.contains(testClass)) {
                seen.add(testClass);
                try {
                    return (Test) suiteMethod.invoke(null, (Object[]) null);
                } catch (InvocationTargetException e) {
                } catch (IllegalAccessException e) {
                }
            }
        } catch (NoSuchMethodException e) {
        }
        return null;
    }
    public static String getTestName(Test test) {
        if (test instanceof TestCase) {
            TestCase testCase = (TestCase) test;
            return testCase.getName();
        } else if (test instanceof TestSuite) {
            TestSuite testSuite = (TestSuite) test;
            String name = testSuite.getName();
            if (name != null) {
                int index = name.lastIndexOf(".");
                if (index > -1) {
                    return name.substring(index + 1);
                } else {
                    return name;
                }
            }
        }
        return "";
    }
    public static Test getTestAtIndex(TestSuite testSuite, int position) {
        int index = 0;
        Enumeration enumeration = testSuite.tests();
        while (enumeration.hasMoreElements()) {
            Test test = (Test) enumeration.nextElement();
            if (index == position) {
                return test;
            }
            index++;
        }
        return null;
    }
    public static TestSuite createTestSuite(Class<? extends Test> testClass)
            throws InstantiationException, IllegalAccessException {
        Test test = invokeSuiteMethodIfPossible(testClass, 
                new HashSet<Class<?>>());
        if (test == null) {
            return new TestSuite(testClass);
        } else if (TestCase.class.isAssignableFrom(test.getClass())) {
            TestSuite testSuite = new TestSuite(test.getClass().getName());
            testSuite.addTest(test);
            return testSuite;
        }
        return (TestSuite) test;
    }
}
