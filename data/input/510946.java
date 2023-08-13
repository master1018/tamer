public class ConfirmLockPattern extends Activity {
    public static final String HEADER_TEXT = "com.android.settings.ConfirmLockPattern.header";
    public static final String FOOTER_TEXT = "com.android.settings.ConfirmLockPattern.footer";
    public static final String HEADER_WRONG_TEXT = "com.android.settings.ConfirmLockPattern.header_wrong";
    public static final String FOOTER_WRONG_TEXT = "com.android.settings.ConfirmLockPattern.footer_wrong";
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;
    private static final String KEY_NUM_WRONG_ATTEMPTS = "num_wrong_attempts";
    private LockPatternView mLockPatternView;
    private LockPatternUtils mLockPatternUtils;
    private int mNumWrongConfirmAttempts;
    private CountDownTimer mCountdownTimer;
    private TextView mHeaderTextView;
    private TextView mFooterTextView;
    private CharSequence mHeaderText;
    private CharSequence mFooterText;
    private CharSequence mHeaderWrongText;
    private CharSequence mFooterWrongText;
    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_lock_pattern);
        mHeaderTextView = (TextView) findViewById(R.id.headerText);
        mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
        mFooterTextView = (TextView) findViewById(R.id.footerText);
        final LinearLayoutWithDefaultTouchRecepient topLayout
                = (LinearLayoutWithDefaultTouchRecepient) findViewById(
                R.id.topLayout);
        topLayout.setDefaultTouchRecepient(mLockPatternView);
        Intent intent = getIntent();
        if (intent != null) {
            mHeaderText = intent.getCharSequenceExtra(HEADER_TEXT);
            mFooterText = intent.getCharSequenceExtra(FOOTER_TEXT);
            mHeaderWrongText = intent.getCharSequenceExtra(HEADER_WRONG_TEXT);
            mFooterWrongText = intent.getCharSequenceExtra(FOOTER_WRONG_TEXT);
        }
        mLockPatternView.setTactileFeedbackEnabled(mLockPatternUtils.isTactileFeedbackEnabled());
        mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
        updateStage(Stage.NeedToUnlock);
        if (savedInstanceState != null) {
            mNumWrongConfirmAttempts = savedInstanceState.getInt(KEY_NUM_WRONG_ATTEMPTS);
        } else {
            if (!mLockPatternUtils.savedPatternExists()) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_NUM_WRONG_ATTEMPTS, mNumWrongConfirmAttempts);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
        if (deadline != 0) {
            handleAttemptLockout(deadline);
        }
    }
    private void updateStage(Stage stage) {
        switch (stage) {
            case NeedToUnlock:
                if (mHeaderText != null) {
                    mHeaderTextView.setText(mHeaderText);
                } else {
                    mHeaderTextView.setText(R.string.lockpattern_need_to_unlock);
                }
                if (mFooterText != null) {
                    mFooterTextView.setText(mFooterText);
                } else {
                    mFooterTextView.setText(R.string.lockpattern_need_to_unlock_footer);
                }
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case NeedToUnlockWrong:
                if (mHeaderWrongText != null) {
                    mHeaderTextView.setText(mHeaderWrongText);
                } else {
                    mHeaderTextView.setText(R.string.lockpattern_need_to_unlock_wrong);
                }
                if (mFooterWrongText != null) {
                    mFooterTextView.setText(mFooterWrongText);
                } else {
                    mFooterTextView.setText(R.string.lockpattern_need_to_unlock_wrong_footer);
                }
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case LockedOut:
                mLockPatternView.clearPattern();
                mLockPatternView.setEnabled(false); 
                break;
        }
    }
    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
    }
    private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener = new LockPatternView.OnPatternListener()  {
        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }
        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }
        public void onPatternCellAdded(List<Cell> pattern) {
        }
        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (mLockPatternUtils.checkPattern(pattern)) {
                setResult(RESULT_OK);
                finish();
            } else {
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL &&
                        ++mNumWrongConfirmAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                    handleAttemptLockout(deadline);
                } else {
                    updateStage(Stage.NeedToUnlockWrong);
                    postClearPatternRunnable();
                }
            }
        }
    };
    private void handleAttemptLockout(long elapsedRealtimeDeadline) {
        updateStage(Stage.LockedOut);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        mCountdownTimer = new CountDownTimer(
                elapsedRealtimeDeadline - elapsedRealtime,
                LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                mHeaderTextView.setText(R.string.lockpattern_too_many_failed_confirmation_attempts_header);
                final int secondsCountdown = (int) (millisUntilFinished / 1000);
                mFooterTextView.setText(getString(
                        R.string.lockpattern_too_many_failed_confirmation_attempts_footer,
                        secondsCountdown));
            }
            @Override
            public void onFinish() {
                mNumWrongConfirmAttempts = 0;
                updateStage(Stage.NeedToUnlock);
            }
        }.start();
    }
}
