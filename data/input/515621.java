public class PasswordUnlockScreen extends LinearLayout implements KeyguardScreen,
        View.OnClickListener, KeyguardUpdateMonitor.InfoCallback, OnEditorActionListener {
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final KeyguardScreenCallback mCallback;
    private EditText mPasswordEntry;
    private Button mEmergencyCallButton;
    private LockPatternUtils mLockPatternUtils;
    private PasswordEntryKeyboardView mKeyboardView;
    private PasswordEntryKeyboardHelper mKeyboardHelper;
    private int mCreationOrientation;
    private int mCreationHardKeyboardHidden;
    private CountDownTimer mCountdownTimer;
    private TextView mTitle;
    private static final int MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT = 3;
    public PasswordUnlockScreen(Context context, Configuration configuration,
            LockPatternUtils lockPatternUtils, KeyguardUpdateMonitor updateMonitor,
            KeyguardScreenCallback callback) {
        super(context);
        mCreationHardKeyboardHidden = configuration.hardKeyboardHidden;
        mCreationOrientation = configuration.orientation;
        mUpdateMonitor = updateMonitor;
        mCallback = callback;
        mLockPatternUtils = lockPatternUtils;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (mCreationOrientation != Configuration.ORIENTATION_LANDSCAPE) {
            layoutInflater.inflate(R.layout.keyguard_screen_password_portrait, this, true);
        } else {
            layoutInflater.inflate(R.layout.keyguard_screen_password_landscape, this, true);
        }
        final int quality = lockPatternUtils.getKeyguardStoredPasswordQuality();
        final boolean isAlpha = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == quality
                || DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == quality;
        mKeyboardView = (PasswordEntryKeyboardView) findViewById(R.id.keyboard);
        mPasswordEntry = (EditText) findViewById(R.id.passwordEntry);
        mPasswordEntry.setOnEditorActionListener(this);
        mEmergencyCallButton = (Button) findViewById(R.id.emergencyCall);
        mEmergencyCallButton.setOnClickListener(this);
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
        mTitle = (TextView) findViewById(R.id.enter_password_label);
        mKeyboardHelper = new PasswordEntryKeyboardHelper(context, mKeyboardView, this);
        mKeyboardHelper.setKeyboardMode(isAlpha ? PasswordEntryKeyboardHelper.KEYBOARD_MODE_ALPHA
                : PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);
        mKeyboardView.setVisibility(mCreationHardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO
                ? View.INVISIBLE : View.VISIBLE);
        mPasswordEntry.requestFocus();
        if (isAlpha) {
            mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        } else {
            mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
        }
        mKeyboardHelper.setVibratePattern(mLockPatternUtils.isTactileFeedbackEnabled() ?
                com.android.internal.R.array.config_virtualKeyVibePattern : 0);
    }
    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        return mPasswordEntry.requestFocus(direction, previouslyFocusedRect);
    }
    public boolean needsInput() {
        return false;
    }
    public void onPause() {
    }
    public void onResume() {
        mPasswordEntry.setText("");
        mPasswordEntry.requestFocus();
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
        long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
        if (deadline != 0) {
            handleAttemptLockout(deadline);
        }
    }
    public void cleanUp() {
        mUpdateMonitor.removeCallback(this);
    }
    public void onClick(View v) {
        if (v == mEmergencyCallButton) {
            mCallback.takeEmergencyCallAction();
        }
        mCallback.pokeWakelock();
    }
    private void verifyPasswordAndUnlock() {
        String entry = mPasswordEntry.getText().toString();
        if (mLockPatternUtils.checkPassword(entry)) {
            mCallback.keyguardDone(true);
            mCallback.reportSuccessfulUnlockAttempt();
        } else if (entry.length() > MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT ) {
            mCallback.reportFailedUnlockAttempt();
            if (0 == (mUpdateMonitor.getFailedAttempts()
                    % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                handleAttemptLockout(deadline);
            }
        }
        mPasswordEntry.setText("");
    }
    private void handleAttemptLockout(long elapsedRealtimeDeadline) {
        mPasswordEntry.setEnabled(false);
        mKeyboardView.setEnabled(false);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                String instructions = getContext().getString(
                        R.string.lockscreen_too_many_failed_attempts_countdown,
                        secondsRemaining);
                mTitle.setText(instructions);
            }
            @Override
            public void onFinish() {
                mPasswordEntry.setEnabled(true);
                mTitle.setText(R.string.keyguard_password_enter_password_code);
                mKeyboardView.setEnabled(true);
            }
        }.start();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mCallback.pokeWakelock();
        return false;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Configuration config = getResources().getConfiguration();
        if (config.orientation != mCreationOrientation
                || config.hardKeyboardHidden != mCreationHardKeyboardHidden) {
            mCallback.recreateMe(config);
        }
    }
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mCreationOrientation
                || newConfig.hardKeyboardHidden != mCreationHardKeyboardHidden) {
            mCallback.recreateMe(newConfig);
        }
    }
    public void onKeyboardChange(boolean isKeyboardOpen) {
        mKeyboardView.setVisibility(isKeyboardOpen ? View.INVISIBLE : View.VISIBLE);
    }
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_NULL) {
            verifyPasswordAndUnlock();
            return true;
        }
        return false;
    }
    public void onPhoneStateChanged(String newState) {
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCallButton);
    }
    public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel) {
    }
    public void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn) {
    }
    public void onRingerModeChanged(int state) {
    }
    public void onTimeChanged() {
    }
}
