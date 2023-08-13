public class MediaFrameworkUnitTestRunner extends InstrumentationTestRunner {
    @Override
    public TestSuite getAllTests() {
        TestSuite suite = new InstrumentationTestSuite(this);
        addMediaMetadataRetrieverStateUnitTests(suite);
        addMediaRecorderStateUnitTests(suite);
        addMediaPlayerStateUnitTests(suite);
        return suite;
    }
    @Override
    public ClassLoader getLoader() {
        return MediaFrameworkUnitTestRunner.class.getClassLoader();
    }
    private void addMediaMetadataRetrieverStateUnitTests(TestSuite suite) {
        suite.addTestSuite(MediaMetadataRetrieverTest.class);
    }
    private void addMediaRecorderStateUnitTests(TestSuite suite) {
        suite.addTestSuite(MediaRecorderPrepareStateUnitTest.class);
        suite.addTestSuite(MediaRecorderResetStateUnitTest.class);
        suite.addTestSuite(MediaRecorderSetAudioEncoderStateUnitTest.class);
        suite.addTestSuite(MediaRecorderSetAudioSourceStateUnitTest.class);
        suite.addTestSuite(MediaRecorderSetOutputFileStateUnitTest.class);
        suite.addTestSuite(MediaRecorderSetOutputFormatStateUnitTest.class);
        suite.addTestSuite(MediaRecorderStartStateUnitTest.class);
        suite.addTestSuite(MediaRecorderStopStateUnitTest.class);
    }
    private void addMediaPlayerStateUnitTests(TestSuite suite) {
        suite.addTestSuite(MediaPlayerGetDurationStateUnitTest.class);
        suite.addTestSuite(MediaPlayerSeekToStateUnitTest.class);
        suite.addTestSuite(MediaPlayerGetCurrentPositionStateUnitTest.class);
        suite.addTestSuite(MediaPlayerGetVideoWidthStateUnitTest.class);
        suite.addTestSuite(MediaPlayerGetVideoHeightStateUnitTest.class);
        suite.addTestSuite(MediaPlayerIsPlayingStateUnitTest.class);
        suite.addTestSuite(MediaPlayerResetStateUnitTest.class);
        suite.addTestSuite(MediaPlayerPauseStateUnitTest.class);
        suite.addTestSuite(MediaPlayerStartStateUnitTest.class);
        suite.addTestSuite(MediaPlayerStopStateUnitTest.class);
        suite.addTestSuite(MediaPlayerSetLoopingStateUnitTest.class);
        suite.addTestSuite(MediaPlayerSetAudioStreamTypeStateUnitTest.class);
        suite.addTestSuite(MediaPlayerSetVolumeStateUnitTest.class);
        suite.addTestSuite(MediaPlayerMetadataParserTest.class);
    }
}
