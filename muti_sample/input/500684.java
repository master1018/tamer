public class ChooseLockGeneric extends PreferenceActivity {
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final String KEY_UNLOCK_SET_NONE = "unlock_set_none";
    private static final String KEY_UNLOCK_SET_PIN = "unlock_set_pin";
    private static final String KEY_UNLOCK_SET_PASSWORD = "unlock_set_password";
    private static final String KEY_UNLOCK_SET_PATTERN = "unlock_set_pattern";
    private static final int CONFIRM_EXISTING_REQUEST = 100;
    private static final String PASSWORD_CONFIRMED = "password_confirmed";
    private static final String CONFIRM_CREDENTIALS = "confirm_credentials";
    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private DevicePolicyManager mDPM;
    private boolean mPasswordConfirmed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(this);
        if (savedInstanceState != null) {
            mPasswordConfirmed = savedInstanceState.getBoolean(PASSWORD_CONFIRMED);
        }
        if (!mPasswordConfirmed) {
            ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
            if (!helper.launchConfirmationActivity(CONFIRM_EXISTING_REQUEST, null, null)) {
                mPasswordConfirmed = true; 
                updatePreferencesOrFinish();
            }
        } else {
            updatePreferencesOrFinish();
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        final String key = preference.getKey();
        boolean handled = true;
        if (KEY_UNLOCK_SET_NONE.equals(key)) {
            updateUnlockMethodAndFinish(DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        } else if (KEY_UNLOCK_SET_PATTERN.equals(key)) {
            updateUnlockMethodAndFinish(DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
        } else if (KEY_UNLOCK_SET_PIN.equals(key)) {
            updateUnlockMethodAndFinish(DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
        } else if (KEY_UNLOCK_SET_PASSWORD.equals(key)) {
            updateUnlockMethodAndFinish(DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
        } else {
            handled = false;
        }
        return handled;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_EXISTING_REQUEST && resultCode == RESULT_OK) {
            mPasswordConfirmed = true;
            updatePreferencesOrFinish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PASSWORD_CONFIRMED, mPasswordConfirmed);
    }
    private void updatePreferencesOrFinish() {
        int quality = getIntent().getIntExtra(LockPatternUtils.PASSWORD_TYPE_KEY, -1);
        if (quality == -1) {
            quality = mChooseLockSettingsHelper.utils().getKeyguardStoredPasswordQuality();
            final PreferenceScreen prefScreen = getPreferenceScreen();
            if (prefScreen != null) {
                prefScreen.removeAll();
            }
            addPreferencesFromResource(R.xml.security_settings_picker);
            disableUnusablePreferences(mDPM.getPasswordQuality(null));
        } else {
            updateUnlockMethodAndFinish(quality);
        }
    }
    private void disableUnusablePreferences(final int quality) {
        final Preference picker = getPreferenceScreen().findPreference("security_picker_category");
        final PreferenceCategory cat = (PreferenceCategory) picker;
        final int preferenceCount = cat.getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            Preference pref = cat.getPreference(i);
            if (pref instanceof PreferenceScreen) {
                final String key = ((PreferenceScreen) pref).getKey();
                boolean enabled = true;
                if (KEY_UNLOCK_SET_NONE.equals(key)) {
                    enabled = quality <= DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
                } else if (KEY_UNLOCK_SET_PATTERN.equals(key)) {
                    enabled = quality <= DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;
                } else if (KEY_UNLOCK_SET_PIN.equals(key)) {
                    enabled = quality <= DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
                } else if (KEY_UNLOCK_SET_PASSWORD.equals(key)) {
                    enabled = quality <= DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
                }
                if (!enabled) {
                    pref.setSummary(R.string.unlock_set_unlock_disabled_summary);
                    pref.setEnabled(false);
                }
            }
        }
    }
    void updateUnlockMethodAndFinish(int quality) {
        if (!mPasswordConfirmed) {
            throw new IllegalStateException("Tried to update password without confirming first");
        }
        int minQuality = mDPM.getPasswordQuality(null);
        if (quality < minQuality) {
            quality = minQuality;
        }
        if (quality >= DevicePolicyManager.PASSWORD_QUALITY_NUMERIC) {
            int minLength = mDPM.getPasswordMinimumLength(null);
            if (minLength < MIN_PASSWORD_LENGTH) {
                minLength = MIN_PASSWORD_LENGTH;
            }
            final int maxLength = mDPM.getPasswordMaximumLength(quality);
            Intent intent = new Intent().setClass(this, ChooseLockPassword.class);
            intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
            intent.putExtra(ChooseLockPassword.PASSWORD_MIN_KEY, minLength);
            intent.putExtra(ChooseLockPassword.PASSWORD_MAX_KEY, maxLength);
            intent.putExtra(CONFIRM_CREDENTIALS, false);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
        } else if (quality == DevicePolicyManager.PASSWORD_QUALITY_SOMETHING) {
            boolean showTutorial = !mChooseLockSettingsHelper.utils().isPatternEverChosen();
            Intent intent = new Intent();
            intent.setClass(this, showTutorial
                    ? ChooseLockPatternTutorial.class
                    : ChooseLockPattern.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            intent.putExtra("key_lock_method", "pattern");
            intent.putExtra(CONFIRM_CREDENTIALS, false);
            startActivity(intent);
        } else if (quality == DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED) {
            mChooseLockSettingsHelper.utils().clearLock();
            setResult(RESULT_OK);
        }
        finish();
    }
}
