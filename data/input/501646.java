public class SoundManager {
    private static SoundManager mInstance = null;
    private Context mContext;
    private AudioManager mAudioManager;
    private final float FX_VOLUME = -1.0f;
    private boolean mSilentMode;
    private SoundManager(Context context) {
        mContext = context;
        updateRingerMode();
    }
    public void updateRingerMode() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
        }
        mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
    }
    public static SoundManager getInstance(Context context) {
        if (null == mInstance) {
            if (null != context) {
                mInstance = new SoundManager(context);
            }
        }
        return mInstance;
    }
    public void playKeyDown() {
        if (mAudioManager == null) {
            updateRingerMode();
        }
        if (!mSilentMode) {
            int sound = AudioManager.FX_KEYPRESS_STANDARD;
            mAudioManager.playSoundEffect(sound, FX_VOLUME);
        }
    }
}
