public class NoAudioPermissionTest extends AndroidTestCase {
    private AudioManager mAudioManager;
    private static final int MODE_COUNT = 3;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        assertNotNull(mAudioManager);
    }
    @SmallTest
    public void testSetMicrophoneMute() {
        boolean muteState = mAudioManager.isMicrophoneMute();
        int originalMode = mAudioManager.getMode();
        mAudioManager.setMicrophoneMute(!muteState);
        assertEquals(muteState, mAudioManager.isMicrophoneMute());
        assertTrue(AudioManager.MODE_NORMAL != AudioManager.MODE_RINGTONE);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        assertEquals(originalMode, mAudioManager.getMode());
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        assertEquals(originalMode, mAudioManager.getMode());
    }
    @SuppressWarnings("deprecation")
    @SmallTest
    public void testRouting() {
        boolean prevState = mAudioManager.isSpeakerphoneOn();
        mAudioManager.setSpeakerphoneOn(!prevState);
        assertEquals(prevState, mAudioManager.isSpeakerphoneOn());
        prevState = mAudioManager.isBluetoothScoOn();
        mAudioManager.setBluetoothScoOn(!prevState);
        assertEquals(prevState, mAudioManager.isBluetoothScoOn());
    }
}
