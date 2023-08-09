public class ConfirmLockPassword extends Activity implements OnClickListener,
        OnEditorActionListener {
    private static final long ERROR_MESSAGE_TIMEOUT = 3000;
    private TextView mPasswordEntry;
    private LockPatternUtils mLockPatternUtils;
    private TextView mHeaderText;
    private Handler mHandler = new Handler();
    private PasswordEntryKeyboardHelper mKeyboardHelper;
    private PasswordEntryKeyboardView mKeyboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        initViews();
    }
    private void initViews() {
        final int storedQuality = mLockPatternUtils.getKeyguardStoredPasswordQuality();
        setContentView(R.layout.confirm_lock_password);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        findViewById(R.id.cancel_button).setOnClickListener(this);
        findViewById(R.id.next_button).setOnClickListener(this);
        mPasswordEntry = (TextView) findViewById(R.id.password_entry);
        mPasswordEntry.setOnEditorActionListener(this);
        mKeyboardView = (PasswordEntryKeyboardView) findViewById(R.id.keyboard);
        mHeaderText = (TextView) findViewById(R.id.headerText);
        final boolean isAlpha = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == storedQuality
                || DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == storedQuality;
        mHeaderText.setText(isAlpha ? R.string.lockpassword_confirm_your_password_header
                : R.string.lockpassword_confirm_your_pin_header);
        mKeyboardHelper = new PasswordEntryKeyboardHelper(this, mKeyboardView, mPasswordEntry);
        mKeyboardHelper.setKeyboardMode(isAlpha ? PasswordEntryKeyboardHelper.KEYBOARD_MODE_ALPHA
                : PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);
        mKeyboardView.requestFocus();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mKeyboardView.requestFocus();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mKeyboardView.requestFocus();
    }
    private void handleNext() {
        final String pin = mPasswordEntry.getText().toString();
        if (mLockPatternUtils.checkPassword(pin)) {
            setResult(RESULT_OK);
            finish();
        } else {
            showError(R.string.lockpattern_need_to_unlock_wrong);
        }
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_button:
                handleNext();
                break;
            case R.id.cancel_button:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }
    private void showError(int msg) {
        mHeaderText.setText(msg);
        mPasswordEntry.setText(null);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mHeaderText.setText(R.string.lockpassword_confirm_your_password_header);
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
}
