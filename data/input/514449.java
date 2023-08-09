public class SmallTests extends TestSuite {
    public static Test suite() {
        return new TestSuiteBuilder(SmallTests.class)
                .includeAllPackagesUnderHere()
                .build();
    }
}
