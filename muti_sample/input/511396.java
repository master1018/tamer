public class DebugIntentSender extends Activity {
    private EditText mIntentField;
    private EditText mDataField;
    private EditText mAccountField;
    private EditText mResourceField;
    private Button mSendBroadcastButton;
    private Button mStartActivityButton;
    private View.OnClickListener mClicked = new View.OnClickListener() {
        public void onClick(View v) {
            if ((v == mSendBroadcastButton) ||
                       (v == mStartActivityButton)) {
                String intentAction = mIntentField.getText().toString();
                String intentData = mDataField.getText().toString();
                String account = mAccountField.getText().toString();
                String resource = mResourceField.getText().toString();
                Intent intent = new Intent(intentAction);
                if (!TextUtils.isEmpty(intentData)) {
                    intent.setData(Uri.parse(intentData));
                }
                intent.putExtra("account", account);
                intent.putExtra("resource", resource);
                if (v == mSendBroadcastButton) {
                    sendBroadcast(intent);
                } else {
                    startActivity(intent);
                }
                setResult(RESULT_OK);
                finish();
            }
        }
    };
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.intent_sender);
        mIntentField = (EditText) findViewById(R.id.intent);
        mIntentField.setText(Intent.ACTION_SYNC);
        Selection.selectAll((Spannable) mIntentField.getText());
        mDataField = (EditText) findViewById(R.id.data);
        mDataField.setBackgroundResource(android.R.drawable.editbox_background);
        mAccountField = (EditText) findViewById(R.id.account);
        mResourceField = (EditText) findViewById(R.id.resource);
        mSendBroadcastButton = (Button) findViewById(R.id.sendbroadcast);
        mSendBroadcastButton.setOnClickListener(mClicked);
        mStartActivityButton = (Button) findViewById(R.id.startactivity);
        mStartActivityButton.setOnClickListener(mClicked);
    }
}
