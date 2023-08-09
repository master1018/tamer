public class MediaFrameworkTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        suite.addTestSuite(MediaPlayerApiTest.class);
        suite.addTestSuite(SimTonesTest.class);
        suite.addTestSuite(MediaMetadataTest.class);
        suite.addTestSuite(CameraTest.class);
        suite.addTestSuite(MediaRecorderTest.class);
        suite.addTestSuite(MediaAudioTrackTest.class);
        suite.addTestSuite(MediaMimeTest.class);
        suite.addTestSuite(MediaPlayerInvokeTest.class);
        suite.addTestSuite(MediaAudioManagerTest.class);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return MediaFrameworkTestRunner.class.getClassLoader();
    }
}
