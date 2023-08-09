@TestTargetClass(AudioManager.class)
public class AudioManagerTest extends AndroidTestCase implements CTSResult {
    private final static int MP3_TO_PLAY = R.raw.testmp3;
    private final static long TIME_TO_PLAY = 2000;
    private AudioManager mAudioManager;
    private int mResultCode;
    private Sync mSync = new Sync();
    private static class Sync {
        private boolean notified;
        synchronized void notifyResult() {
            notified = true;
            notify();
        }
        synchronized void waitForResult() throws Exception {
            if (!notified) {
                wait();
            }
        }
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStreamMute",
            args = {int.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStreamSolo",
            args = {int.class, boolean.class}
        )
    })
    @BrokenTest("flaky")
    public void testMuteSolo() throws Exception {
        AudioManagerStub.setCTSResult(this);
        Intent intent = new Intent();
        intent.setClass(mContext, AudioManagerStub.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        mSync.waitForResult();
        assertEquals(CTSResult.RESULT_OK, mResultCode);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMicrophoneMute",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isMicrophoneMute",
            args = {}
        )
    })
    public void testMicrophoneMute() throws Exception {
        mAudioManager.setMicrophoneMute(true);
        assertTrue(mAudioManager.isMicrophoneMute());
        mAudioManager.setMicrophoneMute(false);
        assertFalse(mAudioManager.isMicrophoneMute());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unloadSoundEffects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "playSoundEffect",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "playSoundEffect",
            args = {int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadSoundEffects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testSoundEffects() throws Exception {
        mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        Settings.System.putInt(mContext.getContentResolver(), SOUND_EFFECTS_ENABLED, 1);
        mAudioManager.loadSoundEffects();
        Thread.sleep(TIME_TO_PLAY);
        float volume = 13;
        mAudioManager.playSoundEffect(SoundEffectConstants.CLICK);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT, volume);
        mAudioManager.unloadSoundEffects();
        mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_UP, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_DOWN, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT, volume);
        mAudioManager.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_RIGHT, volume);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isMusicActive",
            args = {}
        )
    })
    public void testMusicActive() throws Exception {
        MediaPlayer mp = MediaPlayer.create(mContext, MP3_TO_PLAY);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.start();
        Thread.sleep(TIME_TO_PLAY);
        assertTrue(mAudioManager.isMusicActive());
        Thread.sleep(TIME_TO_PLAY);
        mp.stop();
        mp.release();
        Thread.sleep(TIME_TO_PLAY);
        assertFalse(mAudioManager.isMusicActive());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMode",
            args = {}
        )
    })
    public void testAccessMode() throws Exception {
        mAudioManager.setMode(MODE_RINGTONE);
        assertEquals(MODE_RINGTONE, mAudioManager.getMode());
        mAudioManager.setMode(MODE_IN_CALL);
        assertEquals(MODE_IN_CALL, mAudioManager.getMode());
        mAudioManager.setMode(MODE_NORMAL);
        assertEquals(MODE_NORMAL, mAudioManager.getMode());
    }
    @SuppressWarnings("deprecation")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBluetoothA2dpOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBluetoothScoOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRouting",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBluetoothA2dpOn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isBluetoothScoOn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "setRouting",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setSpeakerphoneOn",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isSpeakerphoneOn",
            args = {}
        )
    })
    @ToBeFixed(bug="1713090", explanation="setRouting() has not only been deprecated, but is no"
        + " longer having any effect.")
    public void testRouting() throws Exception {
        boolean oldA2DP = mAudioManager.isBluetoothA2dpOn();
        mAudioManager.setBluetoothA2dpOn(true);
        assertEquals(oldA2DP , mAudioManager.isBluetoothA2dpOn());
        mAudioManager.setBluetoothA2dpOn(false);
        assertEquals(oldA2DP , mAudioManager.isBluetoothA2dpOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_RINGTONE));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_NORMAL));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
        mAudioManager.setBluetoothScoOn(true);
        assertTrue(mAudioManager.isBluetoothScoOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_RINGTONE));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_NORMAL));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
        mAudioManager.setBluetoothScoOn(false);
        assertFalse(mAudioManager.isBluetoothScoOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_RINGTONE));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_NORMAL));
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
        mAudioManager.setSpeakerphoneOn(true);
        assertTrue(mAudioManager.isSpeakerphoneOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
        mAudioManager.setSpeakerphoneOn(false);
        assertFalse(mAudioManager.isSpeakerphoneOn());
        assertEquals(AudioManager.MODE_CURRENT, mAudioManager.getRouting(MODE_IN_CALL));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "shouldVibrate",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVibrateSetting",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVibrateSetting",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testVibrateNotification() throws Exception {
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT, mAudioManager
                .getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF, mAudioManager
                .getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_NOTIFICATION, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_NOTIFICATION));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "shouldVibrate",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVibrateSetting",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVibrateSetting",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testVibrateRinger() throws Exception {
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT, mAudioManager
                .getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertFalse(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
        assertTrue(mAudioManager.shouldVibrate(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ON);
        assertEquals(VIBRATE_SETTING_ON, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_OFF);
        assertEquals(VIBRATE_SETTING_OFF, mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
        mAudioManager.setVibrateSetting(VIBRATE_TYPE_RINGER, VIBRATE_SETTING_ONLY_SILENT);
        assertEquals(VIBRATE_SETTING_ONLY_SILENT,
                mAudioManager.getVibrateSetting(VIBRATE_TYPE_RINGER));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unloadSoundEffects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRingerMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setRingerMode",
            args = {int.class}
        )
    })
    public void testAccessRingMode() throws Exception {
        mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
        assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
        mAudioManager.setRingerMode(RINGER_MODE_SILENT);
        assertEquals(RINGER_MODE_SILENT, mAudioManager.getRingerMode());
        mAudioManager.setRingerMode(RINGER_MODE_VIBRATE);
        assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setStreamVolume",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStreamMaxVolume",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getStreamVolume",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
             method = "adjustStreamVolume",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "adjustSuggestedStreamVolume",
            args = {int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "adjustVolume",
            args = {int.class, int.class}
        )
    })
    public void testVolume() throws Exception {
        int[] streams = { AudioManager.STREAM_ALARM,
                          AudioManager.STREAM_MUSIC,
                          AudioManager.STREAM_SYSTEM,
                          AudioManager.STREAM_VOICE_CALL,
                          AudioManager.STREAM_RING };
        mAudioManager.adjustVolume(ADJUST_RAISE, 100);
        mAudioManager.adjustSuggestedStreamVolume(
                ADJUST_LOWER, USE_DEFAULT_STREAM_TYPE, FLAG_SHOW_UI);
        for (int i = 0; i < streams.length; i++) {
            int maxVolume = mAudioManager.getStreamMaxVolume(streams[i]);
            mAudioManager.setStreamVolume(streams[i], 1, FLAG_SHOW_UI);
            assertEquals(1, mAudioManager.getStreamVolume(streams[i]));
            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
            mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_SHOW_UI);
            assertEquals(maxVolume, mAudioManager.getStreamVolume(streams[i]));
            mAudioManager.adjustSuggestedStreamVolume(ADJUST_LOWER, streams[i], FLAG_SHOW_UI);
            assertEquals(maxVolume - 1, mAudioManager.getStreamVolume(streams[i]));
            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
            for (int k = maxVolume; k > 0; k--) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_SHOW_UI);
                assertEquals(k - 1, mAudioManager.getStreamVolume(streams[i]));
            }
            mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
            assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
            mAudioManager.setStreamVolume(streams[i], 1, FLAG_SHOW_UI);
            assertEquals(1, mAudioManager.getStreamVolume(streams[i]));
            if (streams[i] == AudioManager.STREAM_RING) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_SHOW_UI);
                assertEquals(0, mAudioManager.getStreamVolume(streams[i]));
                assertTrue(mAudioManager.getRingerMode() == RINGER_MODE_VIBRATE ||
                        mAudioManager.getRingerMode() == RINGER_MODE_SILENT);
                mAudioManager.setRingerMode(RINGER_MODE_NORMAL);
                assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
                assertEquals(1, mAudioManager.getStreamVolume(streams[i]));
            } else {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_SHOW_UI);
                assertEquals(0, mAudioManager.getStreamVolume(streams[i]));
                assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
                mAudioManager.setStreamVolume(streams[i], 1, FLAG_SHOW_UI);
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_ALLOW_RINGER_MODES);
                assertEquals(RINGER_MODE_VIBRATE, mAudioManager.getRingerMode());
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_LOWER, FLAG_ALLOW_RINGER_MODES);
                assertTrue(mAudioManager.getRingerMode() == RINGER_MODE_VIBRATE ||
                        mAudioManager.getRingerMode() == RINGER_MODE_SILENT);
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_ALLOW_RINGER_MODES);
                assertEquals(RINGER_MODE_NORMAL, mAudioManager.getRingerMode());
            }
            mAudioManager.setStreamVolume(streams[i], 0, FLAG_SHOW_UI);
            for (int k = 0; k < maxVolume; k++) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_RAISE, FLAG_SHOW_UI);
                assertEquals(1 + k, mAudioManager.getStreamVolume(streams[i]));
            }
            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
            for (int k = 0; k < maxVolume; k++) {
                mAudioManager.adjustStreamVolume(streams[i], ADJUST_SAME, FLAG_SHOW_UI);
                assertEquals(maxVolume, mAudioManager.getStreamVolume(streams[i]));
            }
            mAudioManager.setStreamVolume(streams[i], maxVolume, FLAG_SHOW_UI);
        }
        int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_MUSIC);
        mAudioManager.adjustVolume(ADJUST_RAISE, 100);
        MediaPlayer mp = MediaPlayer.create(mContext, MP3_TO_PLAY);
        mp.setAudioStreamType(STREAM_MUSIC);
        mp.setLooping(true);
        mp.start();
        Thread.sleep(TIME_TO_PLAY);
        assertTrue(mAudioManager.isMusicActive());
        for (int k = 0; k < maxVolume; k++) {
            mAudioManager.adjustVolume(ADJUST_SAME, FLAG_SHOW_UI);
            assertEquals(maxVolume, mAudioManager.getStreamVolume(STREAM_MUSIC));
        }
        mAudioManager.setStreamVolume(STREAM_MUSIC, 1, FLAG_SHOW_UI);
        for (int k = 0; k < maxVolume - 1; k++) {
            mAudioManager.adjustVolume(ADJUST_RAISE, FLAG_SHOW_UI);
            assertEquals(2 + k, mAudioManager.getStreamVolume(STREAM_MUSIC));
        }
        mAudioManager.setStreamVolume(STREAM_MUSIC, maxVolume, FLAG_SHOW_UI);
        maxVolume = mAudioManager.getStreamVolume(STREAM_MUSIC);
        mAudioManager.adjustVolume(ADJUST_LOWER, FLAG_SHOW_UI);
        assertEquals(maxVolume - 1, mAudioManager.getStreamVolume(STREAM_MUSIC));
        mp.stop();
        mp.release();
        Thread.sleep(TIME_TO_PLAY);
        assertFalse(mAudioManager.isMusicActive());
    }
    public void setResult(int resultCode) {
        mSync.notifyResult();
        mResultCode = resultCode;
    }
}
