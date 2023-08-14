public class AccountUnlockScreen extends RelativeLayout implements KeyguardScreen,
        KeyguardUpdateMonitor.InfoCallback,View.OnClickListener, TextWatcher {
    private static final String LOCK_PATTERN_PACKAGE = "com.android.settings";
    private static final String LOCK_PATTERN_CLASS =
            "com.android.settings.ChooseLockPattern";
    private static final int AWAKE_POKE_MILLIS = 30000;
    private final KeyguardScreenCallback mCallback;
    private final LockPatternUtils mLockPatternUtils;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private TextView mTopHeader;
    private TextView mInstructions;
    private EditText mLogin;
    private EditText mPassword;
    private Button mOk;
    private Button mEmergencyCall;
    private ProgressDialog mCheckingDialog;
    public AccountUnlockScreen(Context context,Configuration configuration,
            KeyguardUpdateMonitor updateMonitor, KeyguardScreenCallback callback,
            LockPatternUtils lockPatternUtils) {
        super(context);
        mCallback = callback;
        mLockPatternUtils = lockPatternUtils;
        LayoutInflater.from(context).inflate(
                R.layout.keyguard_screen_glogin_unlock, this, true);
        mTopHeader = (TextView) findViewById(R.id.topHeader);
        mTopHeader.setText(mLockPatternUtils.isPermanentlyLocked() ?
                R.string.lockscreen_glogin_too_many_attempts :
                R.string.lockscreen_glogin_forgot_pattern);
        mInstructions = (TextView) findViewById(R.id.instructions);
        mLogin = (EditText) findViewById(R.id.login);
        mLogin.setFilters(new InputFilter[] { new LoginFilter.UsernameFilterGeneric() } );
        mLogin.addTextChangedListener(this);
        mPassword = (EditText) findViewById(R.id.password);
        mPassword.addTextChangedListener(this);
        mOk = (Button) findViewById(R.id.ok);
        mOk.setOnClickListener(this);
        mEmergencyCall = (Button) findViewById(R.id.emergencyCall);
        mEmergencyCall.setOnClickListener(this);
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCall);
        mUpdateMonitor = updateMonitor;
        mUpdateMonitor.registerInfoCallback(this);
    }
    public void afterTextChanged(Editable s) {
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mCallback.pokeWakelock(AWAKE_POKE_MILLIS);
    }
    @Override
    protected boolean onRequestFocusInDescendants(int direction,
            Rect previouslyFocusedRect) {
        return mLogin.requestFocus(direction, previouslyFocusedRect);
    }
    public boolean needsInput() {
        return true;
    }
    public void onPause() {
    }
    public void onResume() {
        mLogin.setText("");
        mPassword.setText("");
        mLogin.requestFocus();
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCall);
    }
    public void cleanUp() {
        if (mCheckingDialog != null) {
            mCheckingDialog.hide();
        }
        mUpdateMonitor.removeCallback(this);
    }
    public void onClick(View v) {
        mCallback.pokeWakelock();
        if (v == mOk) {
            asyncCheckPassword();
        }
        if (v == mEmergencyCall) {
            mCallback.takeEmergencyCallAction();
        }
    }
    private void postOnCheckPasswordResult(final boolean success) {
        mLogin.post(new Runnable() {
            public void run() {
                if (success) {
                    mLockPatternUtils.setPermanentlyLocked(false);
                    mLockPatternUtils.setLockPatternEnabled(false);
                    mLockPatternUtils.saveLockPattern(null);
                    Intent intent = new Intent();
                    intent.setClassName(LOCK_PATTERN_PACKAGE, LOCK_PATTERN_CLASS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    mCallback.reportSuccessfulUnlockAttempt();
                    mCallback.keyguardDone(true);
                } else {
                    mInstructions.setText(R.string.lockscreen_glogin_invalid_input);
                    mPassword.setText("");
                    mCallback.reportFailedUnlockAttempt();
                }
            }
        });
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mLockPatternUtils.isPermanentlyLocked()) {
                mCallback.goToLockScreen();
            } else {
                mCallback.forgotPattern(false);
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    private Account findIntendedAccount(String username) {
        Account[] accounts = AccountManager.get(mContext).getAccountsByType("com.google");
        Account bestAccount = null;
        int bestScore = 0;
        for (Account a: accounts) {
            int score = 0;
            if (username.equals(a.name)) {
                score = 4;
            } else if (username.equalsIgnoreCase(a.name)) {
                score = 3;
            } else if (username.indexOf('@') < 0) {
                int i = a.name.indexOf('@');
                if (i >= 0) {
                    String aUsername = a.name.substring(0, i);
                    if (username.equals(aUsername)) {
                        score = 2;
                    } else if (username.equalsIgnoreCase(aUsername)) {
                        score = 1;
                    }
                }
            }
            if (score > bestScore) {
                bestAccount = a;
                bestScore = score;
            } else if (score == bestScore) {
                bestAccount = null;
            }
        }
        return bestAccount;
    }
    private void asyncCheckPassword() {
        mCallback.pokeWakelock(AWAKE_POKE_MILLIS);
        final String login = mLogin.getText().toString();
        final String password = mPassword.getText().toString();
        Account account = findIntendedAccount(login);
        if (account == null) {
            postOnCheckPasswordResult(false);
            return;
        }
        getProgressDialog().show();
        Bundle options = new Bundle();
        options.putString(AccountManager.KEY_PASSWORD, password);
        AccountManager.get(mContext).confirmCredentials(account, options, null ,
                new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    mCallback.pokeWakelock(AWAKE_POKE_MILLIS);
                    final Bundle result = future.getResult();
                    final boolean verified = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
                    postOnCheckPasswordResult(verified);
                } catch (OperationCanceledException e) {
                    postOnCheckPasswordResult(false);
                } catch (IOException e) {
                    postOnCheckPasswordResult(false);
                } catch (AuthenticatorException e) {
                    postOnCheckPasswordResult(false);
                } finally {
                    mLogin.post(new Runnable() {
                        public void run() {
                            getProgressDialog().hide();
                        }
                    });
                }
            }
        }, null );
    }
    private Dialog getProgressDialog() {
        if (mCheckingDialog == null) {
            mCheckingDialog = new ProgressDialog(mContext);
            mCheckingDialog.setMessage(
                    mContext.getString(R.string.lockscreen_glogin_checking_password));
            mCheckingDialog.setIndeterminate(true);
            mCheckingDialog.setCancelable(false);
            mCheckingDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            if (!mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_sf_slowBlur)) {
                mCheckingDialog.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            }
        }
        return mCheckingDialog;
    }
    public void onPhoneStateChanged(String newState) {
        mLockPatternUtils.updateEmergencyCallButtonState(mEmergencyCall);
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
