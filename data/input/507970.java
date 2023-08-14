public class IccLockSettings extends PreferenceActivity 
        implements EditPinPreference.OnPinEnteredListener {
    private static final int OFF_MODE = 0;
    private static final int ICC_LOCK_MODE = 1;
    private static final int ICC_OLD_MODE = 2;
    private static final int ICC_NEW_MODE = 3;
    private static final int ICC_REENTER_MODE = 4;
    private static final String PIN_DIALOG = "sim_pin";
    private static final String PIN_TOGGLE = "sim_toggle";
    private static final String DIALOG_STATE = "dialogState";
    private static final String DIALOG_PIN = "dialogPin";
    private static final String DIALOG_ERROR = "dialogError";
    private static final String ENABLE_TO_STATE = "enableState";
    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 8;
    private int mDialogState = OFF_MODE;
    private String mPin;
    private String mOldPin;
    private String mNewPin;
    private String mError;
    private boolean mToState;
    private Phone mPhone;
    private EditPinPreference mPinDialog;
    private CheckBoxPreference mPinToggle;
    private Resources mRes;
    private static final int ENABLE_ICC_PIN_COMPLETE = 100;
    private static final int CHANGE_ICC_PIN_COMPLETE = 101;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case ENABLE_ICC_PIN_COMPLETE:
                    iccLockChanged(ar.exception == null);
                    break;
                case CHANGE_ICC_PIN_COMPLETE:
                    iccPinChanged(ar.exception == null);
                    break;
            }
            return;
        }
    };
    static boolean isIccLockEnabled() {
        return PhoneFactory.getDefaultPhone().getIccCard().getIccLockEnabled();
    }
    static String getSummary(Context context) {
        Resources res = context.getResources();
        String summary = isIccLockEnabled() 
                ? res.getString(R.string.sim_lock_on)
                : res.getString(R.string.sim_lock_off);
        return summary;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }
        addPreferencesFromResource(R.xml.sim_lock_settings);
        mPinDialog = (EditPinPreference) findPreference(PIN_DIALOG);
        mPinToggle = (CheckBoxPreference) findPreference(PIN_TOGGLE);
        if (savedInstanceState != null && savedInstanceState.containsKey(DIALOG_STATE)) {
            mDialogState = savedInstanceState.getInt(DIALOG_STATE);
            mPin = savedInstanceState.getString(DIALOG_PIN);
            mError = savedInstanceState.getString(DIALOG_ERROR);
            mToState = savedInstanceState.getBoolean(ENABLE_TO_STATE);
        }
        mPinDialog.setOnPinEnteredListener(this);
        getPreferenceScreen().setPersistent(false);
        mPhone = PhoneFactory.getDefaultPhone();
        mRes = getResources();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPinToggle.setChecked(mPhone.getIccCard().getIccLockEnabled());
        if (mDialogState != OFF_MODE) {
            showPinDialog();
        } else {
            resetDialogState();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle out) {
        if (mPinDialog.isDialogOpen()) {
            out.putInt(DIALOG_STATE, mDialogState);
            out.putString(DIALOG_PIN, mPinDialog.getEditText().getText().toString());
            out.putString(DIALOG_ERROR, mError);
            out.putBoolean(ENABLE_TO_STATE, mToState);
        } else {
            super.onSaveInstanceState(out);
        }
    }
    private void showPinDialog() {
        if (mDialogState == OFF_MODE) {
            return;
        }
        setDialogValues();
        mPinDialog.showPinDialog();
    }
    private void setDialogValues() {
        mPinDialog.setText(mPin);
        String message = "";
        switch (mDialogState) {
            case ICC_LOCK_MODE:
                message = mRes.getString(R.string.sim_enter_pin);
                mPinDialog.setDialogTitle(mToState 
                        ? mRes.getString(R.string.sim_enable_sim_lock)
                        : mRes.getString(R.string.sim_disable_sim_lock));
                break;
            case ICC_OLD_MODE:
                message = mRes.getString(R.string.sim_enter_old);
                mPinDialog.setDialogTitle(mRes.getString(R.string.sim_change_pin));
                break;
            case ICC_NEW_MODE:
                message = mRes.getString(R.string.sim_enter_new);
                mPinDialog.setDialogTitle(mRes.getString(R.string.sim_change_pin));
                break;
            case ICC_REENTER_MODE:
                message = mRes.getString(R.string.sim_reenter_new);
                mPinDialog.setDialogTitle(mRes.getString(R.string.sim_change_pin));
                break;
        }
        if (mError != null) {
            message = mError + "\n" + message;
            mError = null;
        }
        mPinDialog.setDialogMessage(message);
    }
    public void onPinEntered(EditPinPreference preference, boolean positiveResult) {
        if (!positiveResult) {
            resetDialogState();
            return;
        }
        mPin = preference.getText();
        if (!reasonablePin(mPin)) {
            mError = mRes.getString(R.string.sim_bad_pin);
            showPinDialog();
            return;
        }
        switch (mDialogState) {
            case ICC_LOCK_MODE:
                tryChangeIccLockState();
                break;
            case ICC_OLD_MODE:
                mOldPin = mPin;
                mDialogState = ICC_NEW_MODE;
                mError = null;
                mPin = null;
                showPinDialog();
                break;
            case ICC_NEW_MODE:
                mNewPin = mPin;
                mDialogState = ICC_REENTER_MODE;
                mPin = null;
                showPinDialog();
                break;
            case ICC_REENTER_MODE:
                if (!mPin.equals(mNewPin)) {
                    mError = mRes.getString(R.string.sim_pins_dont_match);
                    mDialogState = ICC_NEW_MODE;
                    mPin = null;
                    showPinDialog();
                } else {
                    mError = null;
                    tryChangePin();
                }
                break;
        }
    }
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPinToggle) {
            mToState = mPinToggle.isChecked();
            mPinToggle.setChecked(!mToState);  
            mDialogState = ICC_LOCK_MODE;
            showPinDialog();
        } else if (preference == mPinDialog) {
            mDialogState = ICC_OLD_MODE;
            return false;
        }
        return true;
    }
    private void tryChangeIccLockState() {
        Message callback = Message.obtain(mHandler, ENABLE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().setIccLockEnabled(mToState, mPin, callback);
    }
    private void iccLockChanged(boolean success) {
        if (success) {
            mPinToggle.setChecked(mToState);
        } else {
            Toast.makeText(this, mRes.getString(R.string.sim_lock_failed), Toast.LENGTH_SHORT)
                    .show();
        }
        resetDialogState();
    }
    private void iccPinChanged(boolean success) {
        if (!success) {
            Toast.makeText(this, mRes.getString(R.string.sim_change_failed),
                    Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this, mRes.getString(R.string.sim_change_succeeded),
                    Toast.LENGTH_SHORT)
                    .show();
        }
        resetDialogState();
    }
    private void tryChangePin() {
        Message callback = Message.obtain(mHandler, CHANGE_ICC_PIN_COMPLETE);
        mPhone.getIccCard().changeIccLockPassword(mOldPin,
                mNewPin, callback);
    }
    private boolean reasonablePin(String pin) {
        if (pin == null || pin.length() < MIN_PIN_LENGTH || pin.length() > MAX_PIN_LENGTH) {
            return false;
        } else {
            return true;
        }
    }
    private void resetDialogState() {
        mError = null;
        mDialogState = ICC_OLD_MODE; 
        mPin = "";
        setDialogValues();
        mDialogState = OFF_MODE;
    }
}
