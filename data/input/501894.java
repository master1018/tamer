public class OuterTest extends TestCase {
    public void testOuter() {
        assertTrue(true);
    }
    public TestSuite buildTestsUnderHereRecursively() {
        return buildTestsUnderHereWith(new TestSuiteBuilder(getClass()));
    }
    public TestSuite buildTestsUnderHereWith(TestSuiteBuilder testSuiteBuilder) {
        return testSuiteBuilder.includeAllPackagesUnderHere().build();
    }
}
