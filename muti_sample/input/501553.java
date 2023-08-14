public class EnableFdnScreen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private static final boolean DBG = false;
    private static final int ENABLE_FDN_COMPLETE = 100;
    private LinearLayout mPinFieldContainer;
    private EditText mPin2Field;
    private TextView mStatusField;
    private boolean mEnable;
    private Phone mPhone;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ENABLE_FDN_COMPLETE:
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
        setContentView(R.layout.enable_fdn_screen);
        setupView();
        mPhone = PhoneFactory.getDefaultPhone();
        mEnable = !mPhone.getIccCard().getIccFdnEnabled();
        int id = mEnable ? R.string.enable_fdn : R.string.disable_fdn;
        setTitle(getResources().getText(id));
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPhone = PhoneFactory.getDefaultPhone();
    }
    private void setupView() {
        mPin2Field = (EditText) findViewById(R.id.pin);
        mPin2Field.setKeyListener(DigitsKeyListener.getInstance());
        mPin2Field.setMovementMethod(null);
        mPin2Field.setOnClickListener(mClicked);
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
    private String getPin2() {
        return mPin2Field.getText().toString();
    }
    private void enableFdn() {
        Message callback = Message.obtain(mHandler, ENABLE_FDN_COMPLETE);
        mPhone.getIccCard().setIccFdnEnabled(mEnable, getPin2(), callback);
        if (DBG) log("enableFdn: please wait...");
    }
    private void handleResult(AsyncResult ar) {
        if (ar.exception == null) {
            if (DBG) log("handleResult: success!");
            showStatus(getResources().getText(mEnable ?
                            R.string.enable_fdn_ok : R.string.disable_fdn_ok));
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
            if (TextUtils.isEmpty(mPin2Field.getText())) {
                return;
            }
            showStatus(getResources().getText(
                    R.string.enable_in_progress));
            enableFdn();
        }
    };
    private void log(String msg) {
        Log.d(LOG_TAG, "[EnableSimPin] " + msg);
    }
}
