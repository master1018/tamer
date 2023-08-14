public class ChooseLockPassword extends Activity implements OnClickListener, OnEditorActionListener,
        TextWatcher {
    private static final String KEY_FIRST_PIN = "first_pin";
    private static final String KEY_UI_STAGE = "ui_stage";
    private TextView mPasswordEntry;
    private int mPasswordMinLength = 4;
    private int mPasswordMaxLength = 16;
    private LockPatternUtils mLockPatternUtils;
    private int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private com.android.settings.ChooseLockPassword.Stage mUiStage = Stage.Introduction;
    private TextView mHeaderText;
    private String mFirstPin;
    private KeyboardView mKeyboardView;
    private PasswordEntryKeyboardHelper mKeyboardHelper;
    private boolean mIsAlphaMode;
    private Button mCancelButton;
    private Button mNextButton;
    public static final String PASSWORD_MIN_KEY = "lockscreen.password_min";
    public static final String PASSWORD_MAX_KEY = "lockscreen.password_max";
    private static Handler mHandler = new Handler();
    private static final int CONFIRM_EXISTING_REQUEST = 58;
    static final int RESULT_FINISHED = RESULT_FIRST_USER;
    private static final long ERROR_MESSAGE_TIMEOUT = 3000;
    protected enum Stage {
        Introduction(R.string.lockpassword_choose_your_password_header,
                R.string.lockpassword_choose_your_pin_header,
                R.string.lockpassword_continue_label),
        NeedToConfirm(R.string.lockpassword_confirm_your_password_header,
                R.string.lockpassword_confirm_your_pin_header,
                R.string.lockpassword_ok_label),
        ConfirmWrong(R.string.lockpassword_confirm_passwords_dont_match,
                R.string.lockpassword_confirm_pins_dont_match,
                R.string.lockpassword_continue_label);
        Stage(int hintInAlpha, int hintInNumeric, int nextButtonText) {
            this.alphaHint = hintInAlpha;
            this.numericHint = hintInNumeric;
            this.buttonText = nextButtonText;
        }
        public final int alphaHint;
        public final int numericHint;
        public final int buttonText;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        mRequestedQuality = getIntent().getIntExtra(LockPatternUtils.PASSWORD_TYPE_KEY, mRequestedQuality);
        mPasswordMinLength = getIntent().getIntExtra(PASSWORD_MIN_KEY, mPasswordMinLength);
        mPasswordMaxLength = getIntent().getIntExtra(PASSWORD_MAX_KEY, mPasswordMaxLength);
        final boolean confirmCredentials = getIntent().getBooleanExtra("confirm_credentials", true);
        int minMode = mLockPatternUtils.getRequestedPasswordQuality();
        if (mRequestedQuality < minMode) {
            mRequestedQuality = minMode;
        }
        int minLength = mLockPatternUtils.getRequestedMinimumPasswordLength();
        if (mPasswordMinLength < minLength) {
            mPasswordMinLength = minLength;
        }
        initViews();
        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(this);
        if (savedInstanceState == null) {
            updateStage(Stage.Introduction);
            if (confirmCredentials) {
                mChooseLockSettingsHelper.launchConfirmationActivity(CONFIRM_EXISTING_REQUEST,
                        null, null);
            }
        }
    }
    private void initViews() {
        setContentView(R.layout.choose_lock_password);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(this);
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        mKeyboardView = (PasswordEntryKeyboardView) findViewById(R.id.keyboard);
        mPasswordEntry = (TextView) findViewById(R.id.password_entry);
        mPasswordEntry.setOnEditorActionListener(this);
        mPasswordEntry.addTextChangedListener(this);
        mIsAlphaMode = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == mRequestedQuality
            || DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == mRequestedQuality;
        mKeyboardHelper = new PasswordEntryKeyboardHelper(this, mKeyboardView, mPasswordEntry);
        mKeyboardHelper.setKeyboardMode(mIsAlphaMode ?
                PasswordEntryKeyboardHelper.KEYBOARD_MODE_ALPHA
                : PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);
        mHeaderText = (TextView) findViewById(R.id.headerText);
        mKeyboardView.requestFocus();
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateStage(mUiStage);
        mKeyboardView.requestFocus();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_UI_STAGE, mUiStage.name());
        outState.putString(KEY_FIRST_PIN, mFirstPin);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String state = savedInstanceState.getString(KEY_UI_STAGE);
        mFirstPin = savedInstanceState.getString(KEY_FIRST_PIN);
        if (state != null) {
            mUiStage = Stage.valueOf(state);
            updateStage(mUiStage);
        }
    }
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
                break;
        }
    }
    protected void updateStage(Stage stage) {
        mUiStage = stage;
        updateUi();
    }
    private String validatePassword(String password) {
        if (password.length() < mPasswordMinLength) {
            return getString(mIsAlphaMode ?
                    R.string.lockpassword_password_too_short
                    : R.string.lockpassword_pin_too_short, mPasswordMinLength);
        }
        if (password.length() > mPasswordMaxLength) {
            return getString(mIsAlphaMode ?
                    R.string.lockpassword_password_too_long
                    : R.string.lockpassword_pin_too_long, mPasswordMaxLength);
        }
        boolean hasAlpha = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (c <= 32 || c > 127) {
                return getString(R.string.lockpassword_illegal_character);
            }
            if (c >= '0' && c <= '9') {
                hasDigit = true;
            } else if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                hasAlpha = true;
            } else {
                hasSymbol = true;
            }
        }
        if (DevicePolicyManager.PASSWORD_QUALITY_NUMERIC == mRequestedQuality
                && (hasAlpha | hasSymbol)) {
            return getString(R.string.lockpassword_pin_contains_non_digits);
        } else {
            final boolean alphabetic = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC
                    == mRequestedQuality;
            final boolean alphanumeric = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC
                    == mRequestedQuality;
            final boolean symbolic = false; 
            if ((alphabetic || alphanumeric) && !hasAlpha) {
                return getString(R.string.lockpassword_password_requires_alpha);
            }
            if (alphanumeric && !hasDigit) {
                return getString(R.string.lockpassword_password_requires_digit);
            }
            if (symbolic && !hasSymbol) {
                return getString(R.string.lockpassword_password_requires_symbol);
            }
        }
        return null;
    }
    private void handleNext() {
        final String pin = mPasswordEntry.getText().toString();
        if (TextUtils.isEmpty(pin)) {
            return;
        }
        String errorMsg = null;
        if (mUiStage == Stage.Introduction) {
            errorMsg = validatePassword(pin);
            if (errorMsg == null) {
                mFirstPin = pin;
                updateStage(Stage.NeedToConfirm);
                mPasswordEntry.setText("");
            }
        } else if (mUiStage == Stage.NeedToConfirm) {
            if (mFirstPin.equals(pin)) {
                mLockPatternUtils.clearLock();
                mLockPatternUtils.saveLockPassword(pin, mRequestedQuality);
                finish();
            } else {
                updateStage(Stage.ConfirmWrong);
                CharSequence tmp = mPasswordEntry.getText();
                if (tmp != null) {
                    Selection.setSelection((Spannable) tmp, 0, tmp.length());
                }
            }
        }
        if (errorMsg != null) {
            showError(errorMsg, mUiStage);
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button:
                handleNext();
                break;
            case R.id.cancel_button:
                finish();
                break;
        }
    }
    private void showError(String msg, final Stage next) {
        mHeaderText.setText(msg);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateStage(next);
            }
        }, ERROR_MESSAGE_TIMEOUT);
    }
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_NULL) {
            handleNext();
            return true;
        }
        return false;
    }
    private void updateUi() {
        String password = mPasswordEntry.getText().toString();
        final int length = password.length();
        if (mUiStage == Stage.Introduction && length > 0) {
            if (length < mPasswordMinLength) {
                String msg = getString(mIsAlphaMode ? R.string.lockpassword_password_too_short
                        : R.string.lockpassword_pin_too_short, mPasswordMinLength);
                mHeaderText.setText(msg);
                mNextButton.setEnabled(false);
            } else {
                String error = validatePassword(password);
                if (error != null) {
                    mHeaderText.setText(error);
                    mNextButton.setEnabled(false);
                } else {
                    mHeaderText.setText(R.string.lockpassword_press_continue);
                    mNextButton.setEnabled(true);
                }
            }
        } else {
            mHeaderText.setText(mIsAlphaMode ? mUiStage.alphaHint : mUiStage.numericHint);
            mNextButton.setEnabled(length > 0);
        }
        mNextButton.setText(mUiStage.buttonText);
    }
    public void afterTextChanged(Editable s) {
        if (mUiStage == Stage.ConfirmWrong) {
            mUiStage = Stage.NeedToConfirm;
        }
        updateUi();
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
