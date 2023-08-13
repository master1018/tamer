public class StressTests extends TestSuite {
    public static Test suite() {
        TestSuite result = new TestSuite();
        result.addTestSuite(SwitchPreview.class);
        result.addTestSuite(ImageCapture.class);
        result.addTestSuite(CameraLatency.class);
        result.addTestSuite(CameraStartUp.class);
        return result;
    }
}
