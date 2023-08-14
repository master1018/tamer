public class EnableIccPinScreen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final int ENABLE_ICC_PIN_COMPLETE = 100;
    private static final boolean DBG = false;
    private LinearLayout mPinFieldContainer;
    private EditText mPinField;
    private TextView mStatusField;
    private boolean mEnable;
    private Phone mPhone;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_ICC_PIN_COMPLETE:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    handleResult(ar);
                    break;
            }
            return;
        }
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.enable_sim_pin_screen);
        setupView();
        mPhone = PhoneFactory.getDefaultPhone();
        mEnable = !mPhone.getIccCard().getIccLockEnabled();
        int id = mEnable ? R.string.enable_sim_pin : R.string.disable_sim_pin;
        setTitle(getResources().getText(id));
    }
    private void setupView() {
        mPinField = (EditText) findViewById(R.id.pin);
        mPinField.setKeyListener(DigitsKeyListener.getInstance());
        mPinField.setMovementMethod(null);
        mPinField.setOnClickListener(mClicked);
        mPinFieldContainer = (LinearLayout) findViewById(R.id.pinc);
        mStatusField = (TextView) findViewById(R.id.status);
    }
    private void showStatus(CharSequence statusMsg) {
        if (statusMsg != null) {
            mStatusField.setText(statusMsg);
            mStatusField.setVisibility(View.VISIBLE);
            mPinFieldContainer.setVisibility(View.GONE);
        } else {
            mPinFieldContainer.setVisibility(View.VISIBLE);
            mStatusField.setVisibility(View.GONE);
        }
    }
    private String getPin() {
        return mPinField.getText().toString();
    }
    private void enableIccPin() {
        Message callback = Message.obtain(mHandler, ENABLE_ICC_PIN_COMPLETE);
        if (DBG) log("enableIccPin:");
        mPhone.getIccCard().setIccLockEnabled(mEnable, getPin(), callback);
        if (DBG) log("enableIccPin: please wait...");
    }
    private void handleResult(AsyncResult ar) {
        if (ar.exception == null) {
            if (DBG) log("handleResult: success!");
            showStatus(getResources().getText(
                    mEnable ? R.string.enable_pin_ok : R.string.disable_pin_ok));
        } else if (ar.exception instanceof CommandException
                 ) {
            if (DBG) log("handleResult: failed!");
            showStatus(getResources().getText(
                    R.string.pin_failed));
        }
        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 3000);
    }
    private View.OnClickListener mClicked = new View.OnClickListener() {
        public void onClick(View v) {
            if (TextUtils.isEmpty(mPinField.getText())) {
                return;
            }
            showStatus(getResources().getText(
                    R.string.enable_in_progress));
            enableIccPin();
        }
    };
    private void log(String msg) {
        Log.d(LOG_TAG, "[EnableIccPin] " + msg);
    }
}
