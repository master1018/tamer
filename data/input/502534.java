public class StubTestBrowserActivity extends TestBrowserActivity {
    private static TestSuite mTestSuite;
    static void setTopTestSuite(TestSuite testSuite) {
        mTestSuite = testSuite;
    }
    @Override
    public TestSuite getTopTestSuite() {
        return mTestSuite;
    }
}
