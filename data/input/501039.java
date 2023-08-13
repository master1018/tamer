public class TestIdentifier {
    private final String mClassName;
    private final String mTestName;
    public TestIdentifier(String className, String testName) {
        if (className == null || testName == null) {
            throw new IllegalArgumentException("className and testName must " +
                    "be non-null");
        }
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
    public boolean equals(Object other) {
        if (!(other instanceof TestIdentifier)) {
            return false;
        }
        TestIdentifier otherTest = (TestIdentifier)other;
        return getClassName().equals(otherTest.getClassName())  &&
                getTestName().equals(otherTest.getTestName());
    }
    @Override
    public int hashCode() {
        return getClassName().hashCode() * 31 + getTestName().hashCode();
    }
    @Override
    public String toString() {
        return String.format("%s#%s", getClassName(), getTestName());
    }
}
