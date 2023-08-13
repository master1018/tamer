public class EditSlideDurationActivity  extends Activity {
    public static final String SLIDE_INDEX = "slide_index";
    public static final String SLIDE_TOTAL = "slide_total";
    public static final String SLIDE_DUR   = "dur";
    private TextView mLabel;
    private Button mDone;
    private EditText mDur;
    private int mCurSlide;
    private int mTotal;
    private Bundle mState;
    private final static String STATE = "state";
    private final static String TAG = "EditSlideDurationActivity";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_slide_duration);
        int dur;
        if (icicle == null) {
            Intent intent = getIntent();
            mCurSlide = intent.getIntExtra(SLIDE_INDEX, 1);
            mTotal = intent.getIntExtra(SLIDE_TOTAL, 1);
            dur = intent.getIntExtra(SLIDE_DUR, 8);
        } else {
            mState = icicle.getBundle(STATE);
            mCurSlide = mState.getInt(SLIDE_INDEX, 1);
            mTotal = mState.getInt(SLIDE_TOTAL, 1);
            dur = mState.getInt(SLIDE_DUR, 8);
        }
        mLabel = (TextView) findViewById(R.id.label);
        mLabel.setText(getString(R.string.duration_selector_title) + " " + (mCurSlide + 1) + "/" + mTotal);
        mDur = (EditText) findViewById(R.id.text);
        mDur.setText(String.valueOf(dur));
        mDur.setOnKeyListener(mOnKeyListener);
        mDone = (Button) findViewById(R.id.done);
        mDone.setOnClickListener(mOnDoneClickListener);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mState = new Bundle();
        mState.putInt(SLIDE_INDEX, mCurSlide);
        mState.putInt(SLIDE_TOTAL, mTotal);
        int durValue;
        try {
            durValue = Integer.parseInt(mDur.getText().toString());
        } catch (NumberFormatException e) {
            durValue = 5;
        }
        mState.putInt(SLIDE_DUR, durValue);
        outState.putBundle(STATE, mState);
    }
    private final OnKeyListener mOnKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    editDone();
                    break;
            }
            return false;
        }
    };
    private final OnClickListener mOnDoneClickListener = new OnClickListener() {
        public void onClick(View v) {
            editDone();
        }
    };
    protected void editDone() {
        String dur = mDur.getText().toString();
        int durValue = 0;
        try {
            durValue = Integer.valueOf(dur);
        } catch (NumberFormatException e) {
            notifyUser(R.string.duration_not_a_number);
            return;
        }
        if (durValue <= 0) {
            notifyUser(R.string.duration_zero);
            return;
        }
        setResult(RESULT_OK, new Intent(mDur.getText().toString()));
        finish();
    }
    private void notifyUser(int msgId) {
        mDur.requestFocus();
        mDur.selectAll();
        Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
        return;
    }
}
