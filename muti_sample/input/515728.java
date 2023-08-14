public class GetPin2Screen extends Activity {
    private static final String LOG_TAG = PhoneApp.LOG_TAG;
    private EditText mPin2Field;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.get_pin2_screen);
        setupView();
    }
    private void setupView() {
        mPin2Field = (EditText) findViewById(R.id.pin);
        if (mPin2Field != null) {
            mPin2Field.setKeyListener(DigitsKeyListener.getInstance());
            mPin2Field.setMovementMethod(null);
            mPin2Field.setOnClickListener(mClicked);
        }
    }
    private String getPin2() {
        return mPin2Field.getText().toString();
    }
    private void returnResult() {
        Bundle map = new Bundle();
        map.putString("pin2", getPin2());
        Intent intent = getIntent();
        Uri uri = intent.getData();
        Intent action = new Intent();
        if (uri != null) action.setAction(uri.toString());
        setResult(RESULT_OK, action.putExtras(map));
        finish();
    }
    private View.OnClickListener mClicked = new View.OnClickListener() {
        public void onClick(View v) {
            if (TextUtils.isEmpty(mPin2Field.getText())) {
                return;
            }
            returnResult();
        }
    };
    private void log(String msg) {
        Log.d(LOG_TAG, "[GetPin2] " + msg);
    }
}
