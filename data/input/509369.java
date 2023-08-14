public class MediaAudioTrackTest extends ActivityInstrumentationTestCase2<MediaFrameworkTest> {    
    private String TAG = "MediaAudioTrackTest";
    public MediaAudioTrackTest() {
        super("com.android.mediaframeworktest", MediaFrameworkTest.class);
    }
    @Override
    protected void setUp() throws Exception {
      super.setUp();
    }
    @Override 
    protected void tearDown() throws Exception {     
        super.tearDown();              
    }
    private static void assumeTrue(String message, boolean cond) {
        assertTrue("(assume)"+message, cond);
    }
    private void log(String testName, String message) {
        Log.v(TAG, "["+testName+"] "+message);
    }
    private void loge(String testName, String message) {
        Log.e(TAG, "["+testName+"] "+message);
    }
    public class TestResults {
        public boolean mResult = false;
        public String  mResultLog = "";
        public TestResults(boolean b, String s) { mResult = b; mResultLog = s; }
    }
    public TestResults constructorTestMultiSampleRate(
                        int _inTest_streamType, int _inTest_mode, 
                        int _inTest_config, int _inTest_format,
                        int _expected_stateForMode) {
        int[] testSampleRates = {8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000};
        String failedRates = "Failure for rate(s): ";
        boolean localRes, finalRes = true;
        for (int i = 0 ; i < testSampleRates.length ; i++) {
            AudioTrack track = null;
            try {
                track = new AudioTrack(
                        _inTest_streamType, 
                        testSampleRates[i], 
                        _inTest_config, 
                        _inTest_format,
                        AudioTrack.getMinBufferSize(testSampleRates[i], 
                                _inTest_config, _inTest_format), 
                        _inTest_mode);
            } catch(IllegalArgumentException iae) {
                Log.e("MediaAudioTrackTest", "[ constructorTestMultiSampleRate ] exception at SR "
                        + testSampleRates[i]+": \n" + iae);
                localRes = false;
            }
            if (track != null) {
                localRes = (track.getState() == _expected_stateForMode);
                track.release();
            }
            else {
                localRes = false;
            }
            if (!localRes) {
                failedRates += Integer.toString(testSampleRates[i]) + "Hz ";
                log("constructorTestMultiSampleRate", "failed to construct "
                        +"AudioTrack(streamType="+_inTest_streamType 
                        +", sampleRateInHz=" + testSampleRates[i]
                        +", channelConfig=" + _inTest_config
                        +", audioFormat=" + _inTest_format  
                        +", bufferSizeInBytes=" + AudioTrack.getMinBufferSize(testSampleRates[i], 
                                _inTest_config, AudioFormat.ENCODING_PCM_16BIT)
                        +", mode="+ _inTest_mode );
                finalRes = false;
            }
        }
        return new TestResults(finalRes, failedRates);
    }
    @LargeTest
    public void testConstructorMono16MusicStream() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STREAM, 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.STATE_INITIALIZED);
        assertTrue("testConstructorMono16MusicStream: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorStereo16MusicStream() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STREAM, 
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.STATE_INITIALIZED);
        assertTrue("testConstructorStereo16MusicStream: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorMono16MusicStatic() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STATIC, 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.STATE_NO_STATIC_DATA);
        assertTrue("testConstructorMono16MusicStatic: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorStereo16MusicStatic() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STATIC, 
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.STATE_NO_STATIC_DATA);
        assertTrue("testConstructorStereo16MusicStatic: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorMono8MusicStream() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STREAM, 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT,
                AudioTrack.STATE_INITIALIZED);
        assertTrue("testConstructorMono8MusicStream: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorStereo8MusicStream() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STREAM, 
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
                AudioTrack.STATE_INITIALIZED);
        assertTrue("testConstructorStereo8MusicStream: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorMono8MusicStatic() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STATIC, 
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT,
                AudioTrack.STATE_NO_STATIC_DATA);
        assertTrue("testConstructorMono8MusicStatic: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorStereo8MusicStatic() throws Exception {
        TestResults res = constructorTestMultiSampleRate(
                AudioManager.STREAM_MUSIC, AudioTrack.MODE_STATIC, 
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
                AudioTrack.STATE_NO_STATIC_DATA);
        assertTrue("testConstructorStereo8MusicStatic: " + res.mResultLog, res.mResult);
    }
    @LargeTest
    public void testConstructorStreamType() throws Exception {
        final int TYPE_TEST_SR = 22050;
        final int TYPE_TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TYPE_TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TYPE_TEST_MODE = AudioTrack.MODE_STREAM;
        final int[] STREAM_TYPES = { AudioManager.STREAM_ALARM, AudioManager.STREAM_BLUETOOTH_SCO, 
                AudioManager.STREAM_MUSIC, AudioManager.STREAM_NOTIFICATION,
                AudioManager.STREAM_RING, AudioManager.STREAM_SYSTEM, 
                AudioManager.STREAM_VOICE_CALL, AudioManager.STREAM_DTMF, };
        final String[] STREAM_NAMES = { "STREAM_ALARM", "STREAM_BLUETOOTH_SCO", "STREAM_MUSIC",
                "STREAM_NOTIFICATION", "STREAM_RING", "STREAM_SYSTEM", "STREAM_VOICE_CALL", "STREAM_DTMF" };
        boolean localTestRes = true;
        AudioTrack track = null;
        for (int i = 0 ; i < STREAM_TYPES.length ; i++)
        {
            try {
                track = new AudioTrack(STREAM_TYPES[i], 
                        TYPE_TEST_SR, TYPE_TEST_CONF, TYPE_TEST_FORMAT,
                        AudioTrack.getMinBufferSize(TYPE_TEST_SR, TYPE_TEST_CONF, TYPE_TEST_FORMAT), 
                        TYPE_TEST_MODE);
            } catch (IllegalArgumentException iae) {
                loge("testConstructorStreamType", "exception for stream type "
                        + STREAM_NAMES[i] + ": "+ iae);
                localTestRes = false;
            }
            if (track != null) {
                if (track.getState() != AudioTrack.STATE_INITIALIZED) {
                    localTestRes = false;
                    Log.e("MediaAudioTrackTest", 
                            "[ testConstructorStreamType ] failed for stream type "+STREAM_NAMES[i]);
                }
                track.release();
            }
            else {
                localTestRes = false;
            }
        }
        assertTrue("testConstructorStreamType", localTestRes);
    }
    @LargeTest
    public void testPlaybackHeadPositionAfterInit() throws Exception {
        final String TEST_NAME = "testPlaybackHeadPositionAfterInit";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT), TEST_MODE);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, track.getPlaybackHeadPosition() == 0);
        track.release();
    }
    @LargeTest
    public void testPlaybackHeadPositionIncrease() throws Exception {
        final String TEST_NAME = "testPlaybackHeadPositionIncrease";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        Thread.sleep(100);
        log(TEST_NAME, "position ="+ track.getPlaybackHeadPosition());
        assertTrue(TEST_NAME, track.getPlaybackHeadPosition() > 0);
        track.release();
    }
    @LargeTest
    public void testPlaybackHeadPositionAfterFlush() throws Exception {
        final String TEST_NAME = "testPlaybackHeadPositionAfterFlush";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        Thread.sleep(100);
        track.stop();
        track.flush();
        log(TEST_NAME, "position ="+ track.getPlaybackHeadPosition());
        assertTrue(TEST_NAME, track.getPlaybackHeadPosition() == 0);
        track.release();
    }
    @LargeTest
    public void testPlaybackHeadPositionAfterStop() throws Exception {
        final String TEST_NAME = "testPlaybackHeadPositionAfterStop";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        Thread.sleep(100);
        track.stop();
        Thread.sleep(100); 
        int pos = track.getPlaybackHeadPosition();
        log(TEST_NAME, "position ="+ pos);
        assertTrue(TEST_NAME, pos == 0);
        track.release();
    }
    @LargeTest
    public void testPlaybackHeadPositionAfterPause() throws Exception {
        final String TEST_NAME = "testPlaybackHeadPositionAfterPause";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        Thread.sleep(100);
        track.pause();
        int pos = track.getPlaybackHeadPosition();
        log(TEST_NAME, "position ="+ pos);
        assertTrue(TEST_NAME, pos > 0);
        track.release();
    }
    @LargeTest
    public void testSetStereoVolumeMax() throws Exception {
        final String TEST_NAME = "testSetStereoVolumeMax";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        float maxVol = AudioTrack.getMaxVolume();
        assertTrue(TEST_NAME, track.setStereoVolume(maxVol, maxVol) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetStereoVolumeMin() throws Exception {
        final String TEST_NAME = "testSetStereoVolumeMin";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        float minVol = AudioTrack.getMinVolume();
        assertTrue(TEST_NAME, track.setStereoVolume(minVol, minVol) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetStereoVolumeMid() throws Exception {
        final String TEST_NAME = "testSetStereoVolumeMid";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        float midVol = (AudioTrack.getMaxVolume() - AudioTrack.getMinVolume()) / 2;
        assertTrue(TEST_NAME, track.setStereoVolume(midVol, midVol) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackRate() throws Exception {
        final String TEST_NAME = "testSetPlaybackRate";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.play();
        assertTrue(TEST_NAME, track.setPlaybackRate((int)(TEST_SR/2)) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackRateZero() throws Exception {
        final String TEST_NAME = "testSetPlaybackRateZero";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, track.setPlaybackRate(0) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackRateTwiceOutputSR() throws Exception {
        final String TEST_NAME = "testSetPlaybackRateTwiceOutputSR";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        int outputSR = AudioTrack.getNativeOutputSampleRate(TEST_STREAM_TYPE);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.play();
        assertTrue(TEST_NAME, track.setPlaybackRate(2*outputSR) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetGetPlaybackRate() throws Exception {
        final String TEST_NAME = "testSetGetPlaybackRate";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_STEREO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize/2];
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.play();
        track.setPlaybackRate((int)(TEST_SR/2));
        assertTrue(TEST_NAME, track.getPlaybackRate() == (int)(TEST_SR/2));
        track.release();
    }
    @LargeTest
    public void testSetPlaybackRateUninit() throws Exception {
        final String TEST_NAME = "testSetPlaybackRateUninit";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STATIC;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_NO_STATIC_DATA);
        assertTrue(TEST_NAME, 
                track.setPlaybackRate(TEST_SR/2) == AudioTrack.ERROR_INVALID_OPERATION);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackHeadPositionPlaying() throws Exception {
        final String TEST_NAME = "testSetPlaybackHeadPositionPlaying";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        assertTrue(TEST_NAME,
                track.setPlaybackHeadPosition(10) == AudioTrack.ERROR_INVALID_OPERATION);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackHeadPositionStopped() throws Exception {
        final String TEST_NAME = "testSetPlaybackHeadPositionStopped";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        track.stop();
        assumeTrue(TEST_NAME, track.getPlayState() == AudioTrack.PLAYSTATE_STOPPED);
        assertTrue(TEST_NAME, track.setPlaybackHeadPosition(10) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackHeadPositionPaused() throws Exception {
        final String TEST_NAME = "testSetPlaybackHeadPositionPaused";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        track.pause();
        assumeTrue(TEST_NAME, track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED);
        assertTrue(TEST_NAME, track.setPlaybackHeadPosition(10) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetPlaybackHeadPositionTooFar() throws Exception {
        final String TEST_NAME = "testSetPlaybackHeadPositionTooFar";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        int frameIndexTooFar = (2*minBuffSize/2) + 77;
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        track.write(data, 0, data.length);
        track.write(data, 0, data.length);
        track.play();
        track.stop();
        assumeTrue(TEST_NAME, track.getPlayState() == AudioTrack.PLAYSTATE_STOPPED);
        assertTrue(TEST_NAME, track.setPlaybackHeadPosition(frameIndexTooFar) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testSetLoopPointsStream() throws Exception {
        final String TEST_NAME = "testSetLoopPointsStream";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, track.setLoopPoints(2, 50, 2) == AudioTrack.ERROR_INVALID_OPERATION);
        track.release();
    }
    @LargeTest
    public void testSetLoopPointsStartAfterEnd() throws Exception {
        final String TEST_NAME = "testSetLoopPointsStartAfterEnd";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STATIC;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, track.setLoopPoints(50, 0, 2) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testSetLoopPointsSuccess() throws Exception {
        final String TEST_NAME = "testSetLoopPointsSuccess";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STATIC;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, track.setLoopPoints(0, 50, 2) == AudioTrack.SUCCESS);
        track.release();
    }
    @LargeTest
    public void testSetLoopPointsLoopTooLong() throws Exception {
        final String TEST_NAME = "testSetLoopPointsLoopTooLong";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STATIC;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        int dataSizeInFrames = minBuffSize/2;
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_NO_STATIC_DATA);
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, 
                track.setLoopPoints(10, dataSizeInFrames+20, 2) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testSetLoopPointsStartTooFar() throws Exception {
        final String TEST_NAME = "testSetLoopPointsStartTooFar";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STATIC;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        int dataSizeInFrames = minBuffSize/2;
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_NO_STATIC_DATA);
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, 
                track.setLoopPoints(dataSizeInFrames+20, dataSizeInFrames+50, 2) 
                    == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testSetLoopPointsEndTooFar() throws Exception {
        final String TEST_NAME = "testSetLoopPointsEndTooFar";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STATIC;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        int dataSizeInFrames = minBuffSize/2;
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_NO_STATIC_DATA);
        track.write(data, 0, data.length);
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME, 
                track.setLoopPoints(dataSizeInFrames-10, dataSizeInFrames+50, 2) 
                    == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteByteOffsetTooBig() throws Exception {
        final String TEST_NAME = "testWriteByteOffsetTooBig";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 10, data.length) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteShortOffsetTooBig() throws Exception {
        final String TEST_NAME = "testWriteShortOffsetTooBig";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        short data[] = new short[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 10, data.length) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteByteSizeTooBig() throws Exception {
        final String TEST_NAME = "testWriteByteSizeTooBig";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, data.length + 10) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteShortSizeTooBig() throws Exception {
        final String TEST_NAME = "testWriteShortSizeTooBig";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        short data[] = new short[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, data.length + 10) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteByteNegativeOffset() throws Exception {
        final String TEST_NAME = "testWriteByteNegativeOffset";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, -10, data.length - 10) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteShortNegativeOffset() throws Exception {
        final String TEST_NAME = "testWriteShortNegativeOffset";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        short data[] = new short[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, -10, data.length - 10) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteByteNegativeSize() throws Exception {
        final String TEST_NAME = "testWriteByteNegativeSize";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, -10) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteShortNegativeSize() throws Exception {
        final String TEST_NAME = "testWriteShortNegativeSize";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        short data[] = new short[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, -10) == AudioTrack.ERROR_BAD_VALUE);
        track.release();
    }
    @LargeTest
    public void testWriteByte() throws Exception {
        final String TEST_NAME = "testWriteByte";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, data.length) == data.length);
        track.release();
    }
    @LargeTest
    public void testWriteShort() throws Exception {
        final String TEST_NAME = "testWriteShort";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        short data[] = new short[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, data.length) == data.length);
        track.release();
    }
    @LargeTest
    public void testWriteByte8bit() throws Exception {
        final String TEST_NAME = "testWriteByte8bit";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        byte data[] = new byte[minBuffSize];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, data.length) == data.length);
        track.release();
    }
    @LargeTest
    public void testWriteShort8bit() throws Exception {
        final String TEST_NAME = "testWriteShort8bit";
        final int TEST_SR = 22050;
        final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
        final int TEST_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
        final int TEST_MODE = AudioTrack.MODE_STREAM;
        final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
        int minBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
        AudioTrack track = new AudioTrack(TEST_STREAM_TYPE, TEST_SR, TEST_CONF, TEST_FORMAT, 
                2*minBuffSize, TEST_MODE);
        short data[] = new short[minBuffSize/2];
        assumeTrue(TEST_NAME, track.getState() == AudioTrack.STATE_INITIALIZED);
        assertTrue(TEST_NAME,
                track.write(data, 0, data.length) == data.length);
        track.release();
    }
    @LargeTest
    public void testGetMinBufferSizeTooLowSR() throws Exception {
      final String TEST_NAME = "testGetMinBufferSizeTooLowSR";
      final int TEST_SR = 3999;
      final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
      final int TEST_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
      final int TEST_MODE = AudioTrack.MODE_STREAM;
      final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
      assertTrue(TEST_NAME, 
          AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT) 
              == AudioTrack.ERROR_BAD_VALUE);
    }    
    @LargeTest
    public void testGetMinBufferSizeTooHighSR() throws Exception {
      final String TEST_NAME = "testGetMinBufferSizeTooHighSR";
      final int TEST_SR = 48001;
      final int TEST_CONF = AudioFormat.CHANNEL_OUT_MONO;
      final int TEST_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
      final int TEST_MODE = AudioTrack.MODE_STREAM;
      final int TEST_STREAM_TYPE = AudioManager.STREAM_MUSIC;
      assertTrue(TEST_NAME, 
          AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT) 
              == AudioTrack.ERROR_BAD_VALUE);
    }    
}
