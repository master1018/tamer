public class UnitTests extends TestSuite {
    public static Test suite() {
        return new UnitTestSuiteBuilder(UnitTests.class)
                .includeAllPackagesUnderHere()
                .excludePackages("com.android.camera.stress")
                .named("Camera Unit Tests")
                .build();
    }
}
