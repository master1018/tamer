public class ChooseLockSettingsHelper {
    private LockPatternUtils mLockPatternUtils;
    private Activity mActivity;
    public ChooseLockSettingsHelper(Activity activity) {
        mActivity = activity;
        mLockPatternUtils = new LockPatternUtils(activity);
    }
    public LockPatternUtils utils() {
        return mLockPatternUtils;
    }
    protected boolean launchConfirmationActivity(int request,
            CharSequence message, CharSequence details) {
        boolean launched = false;
        switch (mLockPatternUtils.getKeyguardStoredPasswordQuality()) {
            case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                launched = confirmPattern(request, message, details);
                break;
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
                launched = confirmPassword(request);
                break;
        }
        return launched;
    }
    private boolean confirmPattern(int request, CharSequence message, CharSequence details) {
        if (!mLockPatternUtils.isLockPatternEnabled() || !mLockPatternUtils.savedPatternExists()) {
            return false;
        }
        final Intent intent = new Intent();
        intent.putExtra(ConfirmLockPattern.HEADER_TEXT, message);
        intent.putExtra(ConfirmLockPattern.FOOTER_TEXT, details);
        intent.setClassName("com.android.settings", "com.android.settings.ConfirmLockPattern");
        mActivity.startActivityForResult(intent, request);
        return true;
    }
    private boolean confirmPassword(int request) {
        if (!mLockPatternUtils.isLockPasswordEnabled()) return false;
        final Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.ConfirmLockPassword");
        mActivity.startActivityForResult(intent, request);
        return true;
    }
}
