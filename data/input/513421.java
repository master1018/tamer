@TestTargetClass(Ringtone.class)
public class RingtoneTest extends AndroidTestCase {
    private Context mContext;
    private Ringtone mRingtone;
    private AudioManager mAudioManager;
    private int mOriginalVolume;
    private int mOriginalRingerMode;
    private int mOriginalStreamType;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRingtone = RingtoneManager.getRingtone(mContext, Settings.System.DEFAULT_RINGTONE_URI);
        mOriginalRingerMode = mAudioManager.getRingerMode();
        mOriginalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        mOriginalStreamType = mRingtone.getStreamType();
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume / 2,
                AudioManager.FLAG_ALLOW_RINGER_MODES);
    }
    @Override
    protected void tearDown() throws Exception {
        if (mRingtone != null) {
            if (mRingtone.isPlaying()) mRingtone.stop();
            mRingtone.setStreamType(mOriginalStreamType);
        }
        if (mAudioManager != null) {
            mAudioManager.setRingerMode(mOriginalRingerMode);
            mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mOriginalVolume,
                    AudioManager.FLAG_ALLOW_RINGER_MODES);
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isPlaying",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "play",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "stop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStreamType",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStreamType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {Context.class}
        )
    })
    public void testRingtone() {
        assertNotNull(mRingtone.getTitle(mContext));
        assertTrue(mOriginalStreamType >= 0);
        mRingtone.setStreamType(AudioManager.STREAM_MUSIC);
        assertEquals(AudioManager.STREAM_MUSIC, mRingtone.getStreamType());
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
        assertEquals(AudioManager.STREAM_ALARM, mRingtone.getStreamType());
        mRingtone.play();
        assertTrue(mRingtone.isPlaying());
        mRingtone.stop();
        assertFalse(mRingtone.isPlaying());
    }
}
