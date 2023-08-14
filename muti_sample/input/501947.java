public class ListTestCaseNames {
    public static List<String> getTestCaseNames(TestSuite suite) {
        List<Test> tests = Collections.<Test>list(suite.tests());
        ArrayList<String> testCaseNames = new ArrayList<String>();
        for (Test test : tests) {
            if (test instanceof TestCase) {
                testCaseNames.add(((TestCase) test).getName());
            } else if (test instanceof TestSuite) {
                testCaseNames.addAll(getTestCaseNames((TestSuite) test));
            }
        }
        return testCaseNames;
    }
    public static List<TestDescriptor> getTestNames(TestSuite suite) {
        List<Test> tests = Collections.<Test>list(suite.tests());
        ArrayList<TestDescriptor> testNames = new ArrayList<TestDescriptor>();
        for (Test test : tests) {
            if (test instanceof TestCase) {
                String className = test.getClass().getName();
                String testName = ((TestCase) test).getName();
                testNames.add(new TestDescriptor(className, testName));
            } else if (test instanceof TestSuite) {
                testNames.addAll(getTestNames((TestSuite) test));
            }
        }
        return testNames;
    }
    public static class TestDescriptor {
       private String mClassName;
       private String mTestName;
       public TestDescriptor(String className, String testName) {
           mClassName = className;
           mTestName = testName;
       }
       public String getClassName() {
           return mClassName;
       }
       public String getTestName() {
           return mTestName;
       }
       @Override
       public boolean equals(Object otherObj) {
           if (otherObj instanceof TestDescriptor) {
               TestDescriptor otherDesc = (TestDescriptor)otherObj;
               return otherDesc.getClassName().equals(this.getClassName()) && 
                      otherDesc.getTestName().equals(this.getTestName());
           }
           return false;
       }
       @Override
       public String toString() {
           return getClassName() + "#" + getTestName();
       }
    }
}
