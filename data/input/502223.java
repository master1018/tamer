public class HapticFeedback {
    private static final int VIBRATION_PATTERN_ID =
            com.android.internal.R.array.config_virtualKeyVibePattern;
    private static final long DURATION = 10;  
    private static final int NO_REPEAT = -1;
    private static final String TAG = "HapticFeedback";
    private Context mContext;
    private long[] mHapticPattern;
    private Vibrator mVibrator;
    private boolean mEnabled;
    private Settings.System mSystemSettings;
    private ContentResolver mContentResolver;
    private boolean mSettingEnabled;
    public void init(Context context, boolean enabled) {
        mEnabled = enabled;
        if (enabled) {
            mVibrator = new Vibrator();
            if (!loadHapticSystemPattern(context.getResources())) {
                mHapticPattern = new long[] {0, DURATION, 2 * DURATION, 3 * DURATION};
            }
            mSystemSettings = new Settings.System();
            mContentResolver = context.getContentResolver();
        }
    }
    public void checkSystemSetting() {
        if (!mEnabled) {
            return;
        }
        try {
            int val = mSystemSettings.getInt(mContentResolver, System.HAPTIC_FEEDBACK_ENABLED, 0);
            mSettingEnabled = val != 0;
        } catch (Resources.NotFoundException nfe) {
            Log.e(TAG, "Could not retrieve system setting.", nfe);
            mSettingEnabled = false;
        }
    }
    public void vibrate() {
        if (!mEnabled || !mSettingEnabled) {
            return;
        }
        mVibrator.vibrate(mHapticPattern, NO_REPEAT);
    }
    private boolean loadHapticSystemPattern(Resources r) {
        int[] pattern;
        mHapticPattern = null;
        try {
            pattern = r.getIntArray(VIBRATION_PATTERN_ID);
        } catch (Resources.NotFoundException nfe) {
            Log.e(TAG, "Vibrate pattern missing.", nfe);
            return false;
        }
        if (null == pattern || pattern.length == 0) {
            Log.e(TAG, "Haptic pattern is null or empty.");
            return false;
        }
        mHapticPattern = new long[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            mHapticPattern[i] = pattern[i];
        }
        return true;
    }
}
