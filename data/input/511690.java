public class SimpleInputActivity extends Activity {
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_PROMPT = "prompt";
    public static final String EXTRA_DEFAULT_CONTENT = "content";
    public static final String EXTRA_OK_BUTTON_TEXT = "button_ok";
    TextView mPrompt;
    EditText mEdit;
    Button mBtnOk;
    Button mBtnCancel;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Dialog);
        setContentView(R.layout.simple_input_activity);
        Bundle extras = getIntent().getExtras();
        CharSequence title = extras.getCharSequence(EXTRA_TITLE);
        if (title != null) {
            setTitle(title);
        } else {
            setTitle(R.string.default_input_title);
        }
        CharSequence prompt = extras.getCharSequence(EXTRA_PROMPT);
        mPrompt = (TextView) findViewById(R.id.prompt);
        if (prompt != null) {
            mPrompt.setText(prompt);
        } else {
            mPrompt.setVisibility(View.GONE);
        }
        mEdit = (EditText) findViewById(R.id.edit);
        CharSequence defaultText = extras.getCharSequence(EXTRA_DEFAULT_CONTENT);
        if (defaultText != null) {
            mEdit.setText(defaultText);
        }
        mBtnOk = (Button) findViewById(R.id.btnOk);
        mBtnOk.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_OK,
                        (new Intent()).setAction(mEdit.getText().toString()));
                finish();
            }
        });
        CharSequence okText = extras.getCharSequence(EXTRA_OK_BUTTON_TEXT);
        if (okText != null) {
            mBtnOk.setText(okText);
        }
        mBtnCancel = (Button) findViewById(R.id.btnCancel);
        mBtnCancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
