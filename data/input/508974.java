public class ChooseLockPattern extends Activity implements View.OnClickListener{
    static final int RESULT_FINISHED = RESULT_FIRST_USER;
    public static final int CONFIRM_EXISTING_REQUEST = 55;
    static final int INFORMATION_MSG_TIMEOUT_MS = 3000;
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;
    private static final int ID_EMPTY_MESSAGE = -1;
    protected TextView mHeaderText;
    protected LockPatternView mLockPatternView;
    protected TextView mFooterText;
    private TextView mFooterLeftButton;
    private TextView mFooterRightButton;
    protected List<LockPatternView.Cell> mChosenPattern = null;
    private final List<LockPatternView.Cell> mAnimatePattern =
            Collections.unmodifiableList(Lists.newArrayList(
                    LockPatternView.Cell.of(0, 0),
                    LockPatternView.Cell.of(0, 1),
                    LockPatternView.Cell.of(1, 1),
                    LockPatternView.Cell.of(2, 1)
            ));
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CONFIRM_EXISTING_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    setResult(RESULT_FINISHED);
                    finish();
                }
                updateStage(Stage.Introduction);
                break;
        }
    }
    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {
            public void onPatternStart() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
                patternInProgress();
            }
            public void onPatternCleared() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if (mUiStage == Stage.NeedToConfirm || mUiStage == Stage.ConfirmWrong) {
                    if (mChosenPattern == null) throw new IllegalStateException("null chosen pattern in stage 'need to confirm");
                    if (mChosenPattern.equals(pattern)) {
                        updateStage(Stage.ChoiceConfirmed);
                    } else {
                        updateStage(Stage.ConfirmWrong);
                    }
                } else if (mUiStage == Stage.Introduction || mUiStage == Stage.ChoiceTooShort){
                    if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE) {
                        updateStage(Stage.ChoiceTooShort);
                    } else {
                        mChosenPattern = new ArrayList<LockPatternView.Cell>(pattern);
                        updateStage(Stage.FirstChoiceValid);
                    }
                } else {
                    throw new IllegalStateException("Unexpected stage " + mUiStage + " when "
                            + "entering the pattern.");
                }
            }
            public void onPatternCellAdded(List<Cell> pattern) {
            }
            private void patternInProgress() {
                mHeaderText.setText(R.string.lockpattern_recording_inprogress);
                mFooterText.setText("");
                mFooterLeftButton.setEnabled(false);
                mFooterRightButton.setEnabled(false);
            }
     };
    enum LeftButtonMode {
        Cancel(R.string.cancel, true),
        CancelDisabled(R.string.cancel, false),
        Retry(R.string.lockpattern_retry_button_text, true),
        RetryDisabled(R.string.lockpattern_retry_button_text, false),
        Gone(ID_EMPTY_MESSAGE, false);
        LeftButtonMode(int text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }
        final int text;
        final boolean enabled;
    }
    enum RightButtonMode {
        Continue(R.string.lockpattern_continue_button_text, true),
        ContinueDisabled(R.string.lockpattern_continue_button_text, false),
        Confirm(R.string.lockpattern_confirm_button_text, true),
        ConfirmDisabled(R.string.lockpattern_confirm_button_text, false),
        Ok(android.R.string.ok, true);
        RightButtonMode(int text, boolean enabled) {
            this.text = text;
            this.enabled = enabled;
        }
        final int text;
        final boolean enabled;
    }
    protected enum Stage {
        Introduction(
                R.string.lockpattern_recording_intro_header,
                LeftButtonMode.Cancel, RightButtonMode.ContinueDisabled,
                R.string.lockpattern_recording_intro_footer, true),
        HelpScreen(
                R.string.lockpattern_settings_help_how_to_record,
                LeftButtonMode.Gone, RightButtonMode.Ok, ID_EMPTY_MESSAGE, false),
        ChoiceTooShort(
                R.string.lockpattern_recording_incorrect_too_short,
                LeftButtonMode.Retry, RightButtonMode.ContinueDisabled,
                ID_EMPTY_MESSAGE, true),
        FirstChoiceValid(
                R.string.lockpattern_pattern_entered_header,
                LeftButtonMode.Retry, RightButtonMode.Continue, ID_EMPTY_MESSAGE, false),
        NeedToConfirm(
                R.string.lockpattern_need_to_confirm,
                LeftButtonMode.CancelDisabled, RightButtonMode.ConfirmDisabled,
                ID_EMPTY_MESSAGE, true),
        ConfirmWrong(
                R.string.lockpattern_need_to_unlock_wrong,
                LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled,
                ID_EMPTY_MESSAGE, true),
        ChoiceConfirmed(
                R.string.lockpattern_pattern_confirmed_header,
                LeftButtonMode.Cancel, RightButtonMode.Confirm, ID_EMPTY_MESSAGE, false);
        Stage(int headerMessage,
                LeftButtonMode leftMode,
                RightButtonMode rightMode,
                int footerMessage, boolean patternEnabled) {
            this.headerMessage = headerMessage;
            this.leftMode = leftMode;
            this.rightMode = rightMode;
            this.footerMessage = footerMessage;
            this.patternEnabled = patternEnabled;
        }
        final int headerMessage;
        final LeftButtonMode leftMode;
        final RightButtonMode rightMode;
        final int footerMessage;
        final boolean patternEnabled;
    }
    private Stage mUiStage = Stage.Introduction;
    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private static final String KEY_UI_STAGE = "uiStage";
    private static final String KEY_PATTERN_CHOICE = "chosenPattern";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setupViews();
        final LinearLayoutWithDefaultTouchRecepient topLayout
                = (LinearLayoutWithDefaultTouchRecepient) findViewById(
                R.id.topLayout);
        topLayout.setDefaultTouchRecepient(mLockPatternView);
        final boolean confirmCredentials = getIntent().getBooleanExtra("confirm_credentials", true);
        if (savedInstanceState == null) {
            if (confirmCredentials) {
                updateStage(Stage.NeedToConfirm);
                boolean launchedConfirmationActivity =
                    mChooseLockSettingsHelper.launchConfirmationActivity(CONFIRM_EXISTING_REQUEST,
                            null, null);
                if (!launchedConfirmationActivity) {
                    updateStage(Stage.Introduction);
                }
            } else {
                updateStage(Stage.Introduction);
            }
        } else {
            final String patternString = savedInstanceState.getString(KEY_PATTERN_CHOICE);
            if (patternString != null) {
                mChosenPattern = LockPatternUtils.stringToPattern(patternString);
            }
            updateStage(Stage.values()[savedInstanceState.getInt(KEY_UI_STAGE)]);
        }
    }
    protected void setupViews() {
        setContentView(R.layout.choose_lock_pattern);
        mHeaderText = (TextView) findViewById(R.id.headerText);
        mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mLockPatternView.setTactileFeedbackEnabled(
                mChooseLockSettingsHelper.utils().isTactileFeedbackEnabled());
        mFooterText = (TextView) findViewById(R.id.footerText);
        mFooterLeftButton = (TextView) findViewById(R.id.footerLeftButton);
        mFooterRightButton = (TextView) findViewById(R.id.footerRightButton);
        mFooterLeftButton.setOnClickListener(this);
        mFooterRightButton.setOnClickListener(this);
    }
    public void onClick(View v) {
        if (v == mFooterLeftButton) {
            if (mUiStage.leftMode == LeftButtonMode.Retry) {
                mChosenPattern = null;
                mLockPatternView.clearPattern();
                updateStage(Stage.Introduction);
            } else if (mUiStage.leftMode == LeftButtonMode.Cancel) {
                setResult(RESULT_FINISHED);
                finish();
            } else {
                throw new IllegalStateException("left footer button pressed, but stage of " +
                    mUiStage + " doesn't make sense");
            }
        } else if (v == mFooterRightButton) {
            if (mUiStage.rightMode == RightButtonMode.Continue) {
                if (mUiStage != Stage.FirstChoiceValid) {
                    throw new IllegalStateException("expected ui stage " + Stage.FirstChoiceValid
                            + " when button is " + RightButtonMode.Continue);
                }
                updateStage(Stage.NeedToConfirm);
            } else if (mUiStage.rightMode == RightButtonMode.Confirm) {
                if (mUiStage != Stage.ChoiceConfirmed) {
                    throw new IllegalStateException("expected ui stage " + Stage.ChoiceConfirmed
                            + " when button is " + RightButtonMode.Confirm);
                }
                saveChosenPatternAndFinish();
            } else if (mUiStage.rightMode == RightButtonMode.Ok) {
                if (mUiStage != Stage.HelpScreen) {
                    throw new IllegalStateException("Help screen is only mode with ok button, but " +
                            "stage is " + mUiStage);
                }
                mLockPatternView.clearPattern();
                mLockPatternView.setDisplayMode(DisplayMode.Correct);
                updateStage(Stage.Introduction);
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mUiStage == Stage.HelpScreen) {
                updateStage(Stage.Introduction);
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_MENU && mUiStage == Stage.Introduction) {
            updateStage(Stage.HelpScreen);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_UI_STAGE, mUiStage.ordinal());
        if (mChosenPattern != null) {
            outState.putString(KEY_PATTERN_CHOICE, LockPatternUtils.patternToString(mChosenPattern));
        }
    }
    protected void updateStage(Stage stage) {
        mUiStage = stage;
        if (stage == Stage.ChoiceTooShort) {
            mHeaderText.setText(
                    getResources().getString(
                            stage.headerMessage,
                            LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
        } else {
            mHeaderText.setText(stage.headerMessage);
        }
        if (stage.footerMessage == ID_EMPTY_MESSAGE) {
            mFooterText.setText("");
        } else {
            mFooterText.setText(stage.footerMessage);
        }
        if (stage.leftMode == LeftButtonMode.Gone) {
            mFooterLeftButton.setVisibility(View.GONE);
        } else {
            mFooterLeftButton.setVisibility(View.VISIBLE);
            mFooterLeftButton.setText(stage.leftMode.text);
            mFooterLeftButton.setEnabled(stage.leftMode.enabled);
        }
        mFooterRightButton.setText(stage.rightMode.text);
        mFooterRightButton.setEnabled(stage.rightMode.enabled);
        if (stage.patternEnabled) {
            mLockPatternView.enableInput();
        } else {
            mLockPatternView.disableInput();
        }
        mLockPatternView.setDisplayMode(DisplayMode.Correct);
        switch (mUiStage) {
            case Introduction:
                mLockPatternView.clearPattern();
                break;
            case HelpScreen:
                mLockPatternView.setPattern(DisplayMode.Animate, mAnimatePattern);
                break;
            case ChoiceTooShort:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case FirstChoiceValid:
                break;
            case NeedToConfirm:
                mLockPatternView.clearPattern();
                break;
            case ConfirmWrong:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case ChoiceConfirmed:
                break;
        }
    }
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
    }
    private void saveChosenPatternAndFinish() {
        LockPatternUtils utils = mChooseLockSettingsHelper.utils();
        final boolean lockVirgin = !utils.isPatternEverChosen();
        utils.saveLockPattern(mChosenPattern);
        utils.setLockPatternEnabled(true);
        if (lockVirgin) {
            utils.setVisiblePatternEnabled(true);
            utils.setTactileFeedbackEnabled(false);
        }
        setResult(RESULT_FINISHED);
        finish();
    }
}
