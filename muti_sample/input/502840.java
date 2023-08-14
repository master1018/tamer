public class TestSuiteFactory {
    public static TestSuite createTestSuite(String name) {
        return new TestSuite(name);
    }
    public static TestSuite createTestSuite() {
        return new TestSuite();    
    }
}
