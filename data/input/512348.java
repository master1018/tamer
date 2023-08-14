@TestTargetClass(AudioTrack.class)
public class AudioTrack_ListenerTest extends AndroidTestCase {
    private boolean mOnMarkerReachedCalled;
    private boolean mOnPeriodicNotificationCalled;
    private boolean mIsHandleMessageCalled;
    private final int TEST_SR = 11025;
    private final int TEST_CONF = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final int TEST_FORMAT = AudioFormat.ENCODING_PCM_8BIT;
    private final int TEST_MODE = AudioTrack.MODE_STREAM;
    private final int TEST_STREAM_TYPE1 = AudioManager.STREAM_MUSIC;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mIsHandleMessageCalled = true;
            super.handleMessage(msg);
        }
    };
    private final int mMinBuffSize = AudioTrack.getMinBufferSize(TEST_SR, TEST_CONF, TEST_FORMAT);
    private AudioTrack mAudioTrack = new AudioTrack(TEST_STREAM_TYPE1, TEST_SR, TEST_CONF,
            TEST_FORMAT, 2 * mMinBuffSize, TEST_MODE);
    private OnPlaybackPositionUpdateListener mListener =
                                new MockOnPlaybackPositionUpdateListener();
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMinBufferSize",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPlaybackPositionUpdateListener",
            args = {OnPlaybackPositionUpdateListener.class}
        )
    })
    public void testAudioTrackCallback() throws Exception {
        mAudioTrack.setPlaybackPositionUpdateListener(mListener);
        doTest();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMinBufferSize",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPlaybackPositionUpdateListener",
            args = {OnPlaybackPositionUpdateListener.class, Handler.class}
        )
    })
    @ToBeFixed(explanation="The handler argument is only used to find the correct Looper." +
            "The AudioTrack instance creates its own handler instance internally.")
    public void testAudioTrackCallbackWithHandler() throws Exception {
        mAudioTrack.setPlaybackPositionUpdateListener(mListener, mHandler);
        doTest();
        assertFalse(mIsHandleMessageCalled);
    }
    private void doTest() throws Exception {
        mOnMarkerReachedCalled = false;
        mOnPeriodicNotificationCalled = false;
        byte[] vai = AudioTrackTest.createSoundDataInByteArray(2 * mMinBuffSize, TEST_SR);
        int markerInFrames = vai.length / 4;
        assertEquals(AudioTrack.SUCCESS, mAudioTrack.setNotificationMarkerPosition(markerInFrames));
        int periodInFrames = vai.length / 2;
        assertEquals(AudioTrack.SUCCESS, mAudioTrack.setPositionNotificationPeriod(periodInFrames));
        boolean hasPlayed = false;
        int written = 0;
        while (written < vai.length) {
            written += mAudioTrack.write(vai, written, vai.length - written);
            if (!hasPlayed) {
                mAudioTrack.play();
                hasPlayed = true;
            }
        }
        final int numChannels = (TEST_CONF == AudioFormat.CHANNEL_CONFIGURATION_STEREO) ? 2 : 1;
        final int bytesPerSample = (TEST_FORMAT == AudioFormat.ENCODING_PCM_16BIT) ? 2 : 1;
        final int bytesPerFrame = numChannels * bytesPerSample;
        final int sampleLengthMs = (int)(1000 * ((float)vai.length / TEST_SR / bytesPerFrame));
        Thread.sleep(sampleLengthMs + 1000);
        assertTrue(mOnMarkerReachedCalled);
        assertTrue(mOnPeriodicNotificationCalled);
        mAudioTrack.stop();
    }
    private class MockOnPlaybackPositionUpdateListener
                                        implements OnPlaybackPositionUpdateListener {
        public void onMarkerReached(AudioTrack track) {
            mOnMarkerReachedCalled = true;
        }
        public void onPeriodicNotification(AudioTrack track) {
            mOnPeriodicNotificationCalled = true;
        }
    }
    @Override
    protected void tearDown() throws Exception {
        mAudioTrack.release();
        super.tearDown();
    }
}