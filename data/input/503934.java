public class SmokeTestSuiteBuilderTest extends TestCase {
    public void testShouldOnlyIncludeSmokeTests() throws Exception {
        TestSuite testSuite = new SmokeTestSuiteBuilder(getClass())
                .includeAllPackagesUnderHere().build();
        List<String> testCaseNames = ListTestCaseNames.getTestCaseNames(testSuite);
        assertEquals("Unexpected number of smoke tests.", 1, testCaseNames.size());
        assertEquals("Unexpected test name", "testSmoke", testCaseNames.get(0));
    }
}
