public class FdnSetting extends PreferenceActivity
        implements EditPinPreference.OnPinEnteredListener, DialogInterface.OnCancelListener {
    private Phone mPhone;
    private static final int EVENT_PIN2_ENTRY_COMPLETE = 100;
    private static final int EVENT_PIN2_CHANGE_COMPLETE = 200;
    private static final String BUTTON_FDN_ENABLE_KEY = "button_fdn_enable_key";
    private static final String BUTTON_CHANGE_PIN2_KEY = "button_change_pin2_key";
    private EditPinPreference mButtonEnableFDN;
    private EditPinPreference mButtonChangePin2;
    private String mOldPin;
    private String mNewPin;
    private static final int PIN_CHANGE_OLD = 0;
    private static final int PIN_CHANGE_NEW = 1;
    private static final int PIN_CHANGE_REENTER = 2;
    private static final int PIN_CHANGE_PUK = 3;
    private int mPinChangeState;
    private boolean mSkipOldPin;    
    private static final String SKIP_OLD_PIN_KEY = "skip_old_pin_key";
    private static final String PIN_CHANGE_STATE_KEY = "pin_change_state_key";
    private static final String OLD_PIN_KEY = "old_pin_key";
    private static final String NEW_PIN_KEY = "new_pin_key";
    private static final String DIALOG_MESSAGE_KEY = "dialog_message_key";
    private static final String DIALOG_PIN_ENTRY_KEY = "dialog_pin_entry_key";
    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 8;
    public void onPinEntered(EditPinPreference preference, boolean positiveResult) {
        if (preference == mButtonEnableFDN) {
            toggleFDNEnable(positiveResult);
        } else if (preference == mButtonChangePin2){
            updatePINChangeState(positiveResult);
        }
    }
    private void toggleFDNEnable(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }
        String password = mButtonEnableFDN.getText();
        if (validatePin (password, false)) {
            boolean isEnabled = mPhone.getIccCard().getIccFdnEnabled();
            Message onComplete = mFDNHandler.obtainMessage(EVENT_PIN2_ENTRY_COMPLETE);
            mPhone.getIccCard().setIccFdnEnabled(!isEnabled, password, onComplete);
        } else {
            displayMessage(R.string.invalidPin2);
        }
        mButtonEnableFDN.setText("");
    }
    private void updatePINChangeState(boolean positiveResult) {
        if (!positiveResult) {
            if (!mSkipOldPin) {
                resetPinChangeState();
            } else {
                resetPinChangeStateForPUK2();
            }
            return;
        }
        switch (mPinChangeState) {
            case PIN_CHANGE_OLD:
                mOldPin = mButtonChangePin2.getText();
                mButtonChangePin2.setText("");
                if (validatePin (mOldPin, false)) {
                    mPinChangeState = PIN_CHANGE_NEW;
                    displayPinChangeDialog();
                } else {
                    displayPinChangeDialog(R.string.invalidPin2, true);
                }
                break;
            case PIN_CHANGE_NEW:
                mNewPin = mButtonChangePin2.getText();
                mButtonChangePin2.setText("");
                if (validatePin (mNewPin, false)) {
                    mPinChangeState = PIN_CHANGE_REENTER;
                    displayPinChangeDialog();
                } else {
                    displayPinChangeDialog(R.string.invalidPin2, true);
                }
                break;
            case PIN_CHANGE_REENTER:
                if (!mNewPin.equals(mButtonChangePin2.getText())) {
                    mPinChangeState = PIN_CHANGE_NEW;
                    mButtonChangePin2.setText("");
                    displayPinChangeDialog(R.string.mismatchPin2, true);
                } else {
                    mButtonChangePin2.setText("");
                    if (!mSkipOldPin) {
                        Message onComplete = mFDNHandler.obtainMessage(EVENT_PIN2_CHANGE_COMPLETE);
                        mPhone.getIccCard().changeIccFdnPassword(mOldPin, mNewPin, onComplete);
                    } else {
                        mPinChangeState = PIN_CHANGE_PUK;
                        displayPinChangeDialog();
                    }
                }
                break;
            case PIN_CHANGE_PUK: {
                    String puk2 = mButtonChangePin2.getText();
                    mButtonChangePin2.setText("");
                    if (validatePin (puk2, true)) {
                        Message onComplete = mFDNHandler.obtainMessage(EVENT_PIN2_CHANGE_COMPLETE);
                        mPhone.getIccCard().supplyPuk2(puk2, mNewPin, onComplete);
                    } else {
                        displayPinChangeDialog(R.string.invalidPuk2, true);
                    }
                }
                break;
        }
    }
    private Handler mFDNHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PIN2_ENTRY_COMPLETE: {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            CommandException ce = (CommandException) ar.exception;
                            if (ce.getCommandError() == CommandException.Error.SIM_PUK2) {
                                displayMessage(R.string.fdn_enable_puk2_requested);
                                resetPinChangeStateForPUK2();
                            } else {
                                displayMessage(R.string.pin2_invalid);
                            }
                        }
                        updateEnableFDN();
                    }
                    break;
                case EVENT_PIN2_CHANGE_COMPLETE: {
                        AsyncResult ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            CommandException ce = (CommandException) ar.exception;
                            if (ce.getCommandError() == CommandException.Error.SIM_PUK2) {
                                AlertDialog a = new AlertDialog.Builder(FdnSetting.this)
                                    .setMessage(R.string.puk2_requested)
                                    .setCancelable(true)
                                    .setOnCancelListener(FdnSetting.this)
                                    .create();
                                a.getWindow().addFlags(
                                        WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                a.show();
                            } else {
                                if (mPinChangeState == PIN_CHANGE_PUK) {
                                    displayMessage(R.string.badPuk2);
                                } else {
                                    displayMessage(R.string.badPin2);
                                }
                                if (!mSkipOldPin) {
                                    resetPinChangeState();
                                } else {
                                    resetPinChangeStateForPUK2();
                                }
                            }
                        } else {
                            displayMessage(R.string.pin2_changed);
                            mSkipOldPin = false;
                            resetPinChangeState();
                        }
                    }
                    break;
            }
        }
    };
    public void onCancel(DialogInterface dialog) {
        mPinChangeState = PIN_CHANGE_PUK;
        displayPinChangeDialog(0, true);
    }
    private final void displayMessage(int strId) {
        Toast.makeText(this, getString(strId), Toast.LENGTH_SHORT)
            .show();
    }
    private final void displayPinChangeDialog() {
        displayPinChangeDialog(0, true);
    }
    private final void displayPinChangeDialog(int strId, boolean shouldDisplay) {
        int msgId;
        switch (mPinChangeState) {
            case PIN_CHANGE_OLD:
                msgId = R.string.oldPin2Label;
                break;
            case PIN_CHANGE_NEW:
                msgId = R.string.newPin2Label;
                break;
            case PIN_CHANGE_REENTER:
                msgId = R.string.confirmPin2Label;
                break;
            case PIN_CHANGE_PUK:
            default:
                msgId = R.string.label_puk2_code;
                break;
        }
        if (strId != 0) {
            mButtonChangePin2.setDialogMessage(getText(msgId) + "\n" + getText(strId));
        } else {
            mButtonChangePin2.setDialogMessage(msgId);
        }
        if (shouldDisplay) {
            mButtonChangePin2.showPinDialog();
        }
    }
    private final void resetPinChangeState() {
        mPinChangeState = PIN_CHANGE_OLD;
        displayPinChangeDialog(0, false);
        mOldPin = mNewPin = "";
    }
    private final void resetPinChangeStateForPUK2() {
        mPinChangeState = PIN_CHANGE_NEW;
        displayPinChangeDialog(0, false);
        mOldPin = mNewPin = "";
        mSkipOldPin = true;
    }
    private boolean validatePin(String pin, boolean isPUK) {
        int pinMinimum = isPUK ? MAX_PIN_LENGTH : MIN_PIN_LENGTH;
        if (pin == null || pin.length() < pinMinimum || pin.length() > MAX_PIN_LENGTH) {
            return false;
        } else {
            return true;
        }
    }
    private void updateEnableFDN() {
        if (mPhone.getIccCard().getIccFdnEnabled()) {
            mButtonEnableFDN.setTitle(R.string.enable_fdn_ok);
            mButtonEnableFDN.setSummary(R.string.fdn_enabled);
            mButtonEnableFDN.setDialogTitle(R.string.disable_fdn);
        } else {
            mButtonEnableFDN.setTitle(R.string.disable_fdn_ok);
            mButtonEnableFDN.setSummary(R.string.fdn_disabled);
            mButtonEnableFDN.setDialogTitle(R.string.enable_fdn);
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.fdn_setting);
        mPhone = PhoneFactory.getDefaultPhone();
        PreferenceScreen prefSet = getPreferenceScreen();
        mButtonEnableFDN = (EditPinPreference) prefSet.findPreference(BUTTON_FDN_ENABLE_KEY);
        mButtonChangePin2 = (EditPinPreference) prefSet.findPreference(BUTTON_CHANGE_PIN2_KEY);
        mButtonEnableFDN.setOnPinEnteredListener(this);
        updateEnableFDN();
        mButtonChangePin2.setOnPinEnteredListener(this);
        if (icicle == null) {
            resetPinChangeState();
        } else {
            mSkipOldPin = icicle.getBoolean(SKIP_OLD_PIN_KEY);
            mPinChangeState = icicle.getInt(PIN_CHANGE_STATE_KEY);
            mOldPin = icicle.getString(OLD_PIN_KEY);
            mNewPin = icicle.getString(NEW_PIN_KEY);
            mButtonChangePin2.setDialogMessage(icicle.getString(DIALOG_MESSAGE_KEY));
            mButtonChangePin2.setText(icicle.getString(DIALOG_PIN_ENTRY_KEY));
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPhone = PhoneFactory.getDefaultPhone();
        updateEnableFDN();
    }
    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean(SKIP_OLD_PIN_KEY, mSkipOldPin);
        out.putInt(PIN_CHANGE_STATE_KEY, mPinChangeState);
        out.putString(OLD_PIN_KEY, mOldPin);
        out.putString(NEW_PIN_KEY, mNewPin);
        out.putString(DIALOG_MESSAGE_KEY, mButtonChangePin2.getDialogMessage().toString());
        out.putString(DIALOG_PIN_ENTRY_KEY, mButtonChangePin2.getText());
    }
}
