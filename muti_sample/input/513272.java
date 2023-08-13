public class IccPinUnlockPanel extends IccPanel {
    private static final boolean DBG = false;
    private static final int EVENT_ICC_UNLOCKED_RESULT = 100;
    private enum IccLockState {
        UNLOCKED,
        REQUIRE_PIN,
        REQUIRE_PUK,
        REQUIRE_NEW_PIN,
        VERIFY_NEW_PIN,
        VERIFY_NEW_PIN_FAILED
    };
    private IccLockState mState;
    private String mPUKCode;
    private String mNewPinCode;
    private EditText mEntry;
    private TextView mFailure;
    private TextView mLabel;
    private TextView mStatus;
    private Button mUnlockButton;
    private Button mDismissButton;
    private LinearLayout mUnlockPane;
    private LinearLayout mUnlockInProgressPane;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_ICC_UNLOCKED_RESULT:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    handleUnlockResult(ar);
                    break;
            }
        }
    };
    private class MyTextWatcher implements TextWatcher {
        Context mContext;
        public MyTextWatcher(Context context) {
            mContext = context;
        }
        public void beforeTextChanged(CharSequence buffer,
                                      int start, int olen, int nlen) {
        }
        public void onTextChanged(CharSequence buffer,
                                  int start, int olen, int nlen) {
        }
        public void afterTextChanged(Editable buffer) {
            if (SpecialCharSequenceMgr.handleChars(
                    mContext, buffer.toString())) {
                mEntry.getText().clear();
            }
        }
    }
    public IccPinUnlockPanel(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sim_unlock);
        updateState();
        initView();
        updateView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (updateState()) {
            updateView(); 
        }
    }
    boolean updateState() {
        PhoneApp app = PhoneApp.getInstance();
        IccCard iccCardInterface = app.phone.getIccCard();
        try {
            if (iccCardInterface.getState() == IccCard.State.PUK_REQUIRED) {
                if (getState() != IccLockState.REQUIRE_PUK) {
                    setState(IccLockState.REQUIRE_PUK);
                    return true;
                }
            } else if (getState() != IccLockState.REQUIRE_PIN){
                setState(IccLockState.REQUIRE_PIN);
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }
    void setState(IccLockState state) {
        mState = state;
    }
    IccLockState getState() {
        return mState;
    }
    void initView() {
        mUnlockPane = (LinearLayout) findViewById(R.id.simPINPane);
        mUnlockInProgressPane = (LinearLayout) findViewById(R.id.progress);
        mEntry = (EditText) findViewById(R.id.entry);
        mEntry.setKeyListener(DialerKeyListener.getInstance());
        mEntry.setMovementMethod(null);
        mEntry.setOnClickListener(mUnlockListener);
        CharSequence text = mEntry.getText();
        Spannable span = (Spannable) text;
        span.setSpan(new MyTextWatcher(this.getContext()),
                0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mFailure = (TextView) findViewById(R.id.failure);
        mLabel = (TextView) findViewById(R.id.label);
        mStatus = (TextView) findViewById(R.id.status);
        mUnlockButton = (Button) findViewById(R.id.unlock);
        mUnlockButton.setOnClickListener(mUnlockListener);
        mDismissButton = (Button) findViewById(R.id.dismiss);
        mDismissButton.setOnClickListener(mDismissListener);
    }
    void updateView() {
        Context context = getContext();
        switch (mState) {
            case REQUIRE_PIN:
                mLabel.setText(context.getText(R.string.enterPin));
                break;
            case REQUIRE_PUK:
                mLabel.setText(context.getText(R.string.enterPuk));
                mUnlockButton.setText(
                        context.getText(R.string.buttonTxtContinue));
                break;
            case REQUIRE_NEW_PIN:
                hideIncorrectPinMessage();
                mLabel.setText(context.getText(R.string.enterNewPin));
                break;
            case VERIFY_NEW_PIN:
                mLabel.setText(context.getText(R.string.verifyNewPin));
                break;
            case VERIFY_NEW_PIN_FAILED:
                mLabel.setText(context.getText(R.string.verifyFailed));
                setState(IccLockState.REQUIRE_NEW_PIN);
                break;
        }
        mEntry.getText().clear();
        mEntry.requestFocus(View.FOCUS_FORWARD);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    void handleUnlockResult(AsyncResult ar) {
        if (ar.exception == null) {
            handleSuccess();
            return;
        }
        if (ar.exception instanceof com.android.internal.telephony.CommandException &&
                ((CommandException) ar.exception).getCommandError() ==
                    CommandException.Error.PASSWORD_INCORRECT) {
            hidePinUnlockInProgress();
            handleFailure();
        }
    }
    void handleSuccess() {
        if (DBG) log("unlock successful!");
        showUnlockSuccess();
        PhoneApp.getInstance().setCachedSimPin(mEntry.getText().toString());
    }
    void handleFailure() {
        if (DBG) log("unlock failed");
        showIncorrectPinMessage();
        mEntry.getText().clear();
        updateState();
        updateView();
    }
    void showIncorrectPinMessage() {
        CharSequence msg;
        Context context = getContext();
        if (getState() == IccLockState.REQUIRE_PIN) {
            msg = context.getText(R.string.badPin);
        } else {
            msg = context.getText(R.string.badPuk);
        }
        mFailure.setText(msg);
        mFailure.setVisibility(View.VISIBLE);
    }
    void hideIncorrectPinMessage() {
        mFailure.setVisibility(View.GONE);
    }
    void showUnlockInProgress() {
        mUnlockInProgressPane.setVisibility(View.VISIBLE);
        mUnlockPane.setVisibility(View.GONE);
    }
    void hidePinUnlockInProgress() {
        mUnlockInProgressPane.setVisibility(View.GONE);
        mUnlockPane.setVisibility(View.VISIBLE);
    }
    void showUnlockSuccess() {
        mStatus.setText(getContext().getText(R.string.pinUnlocked));
        mHandler.postDelayed(
                new Runnable() {
                    public void run() {
                        dismiss();
                    }
                }, 1000);
    }
    private boolean verifyNewPin(String pin2) {
        if (mNewPinCode.equals(pin2)) {
            return true;
        }
        return false;
    }
    View.OnClickListener mUnlockListener = new View.OnClickListener() {
        public void onClick(View v) {
            String code = mEntry.getText().toString();
            if (TextUtils.isEmpty(code)) {
                return;
            }
            PhoneApp app = PhoneApp.getInstance();
            IccCard iccCardInterface = app.phone.getIccCard();
            if (iccCardInterface != null) {
                Message callBack = Message.obtain(mHandler,
                        EVENT_ICC_UNLOCKED_RESULT);
                switch (mState) {
                    case REQUIRE_PIN:
                        if (DBG) log("unlock attempt: PIN code entered = " +
                                code);
                        showUnlockInProgress();
                        iccCardInterface.supplyPin(code, callBack);
                        break;
                    case REQUIRE_PUK:
                        if (DBG) log("puk code entered, request for new pin");
                        mPUKCode = code;
                        setState(IccLockState.REQUIRE_NEW_PIN);
                        updateView();
                        break;
                    case REQUIRE_NEW_PIN:
                        if (DBG) log("new pin code entered, verify pin");
                        mNewPinCode = code;
                        setState(IccLockState.VERIFY_NEW_PIN);
                        updateView();
                        break;
                    case VERIFY_NEW_PIN:
                        if (DBG) log("new pin code re-entered");
                        if (verifyNewPin(code)) {
                            showUnlockInProgress();
                            iccCardInterface.supplyPuk(mPUKCode, mNewPinCode,
                                    callBack);
                        } else {
                            setState(IccLockState.VERIFY_NEW_PIN_FAILED);
                            updateView();
                        }
                        break;
                }
            }
        }
    };
    View.OnClickListener mDismissListener = new View.OnClickListener() {
        public void onClick(View v) {
            dismiss();
        }
    };
    private void log(String msg) {
        Log.v(TAG, "[SimPinUnlock] " + msg);
    }
}
