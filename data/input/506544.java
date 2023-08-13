public class NewLocaleDialog extends Activity implements View.OnClickListener {
    public static final String INTENT_EXTRA_LOCALE = "locale";
    public static final String INTENT_EXTRA_SELECT = "select";
    private static final String TAG = "NewLocale";
    private static final boolean DEBUG = true;
    private Button mButtonAdd;
    private Button mButtonAddSelect;
    private EditText mEditText;
    private boolean mWasEmpty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_locale);
        mEditText = (EditText) findViewById(R.id.value);
        mWasEmpty = true;
        mButtonAdd = (Button) findViewById(R.id.add);
        mButtonAdd.setOnClickListener(this);
        mButtonAddSelect = (Button) findViewById(R.id.add_and_select);
        mButtonAddSelect.setOnClickListener(this);
    }
    public void onClick(View v) {
        String locale = mEditText.getText().toString();
        boolean select = v == mButtonAddSelect;
        if (DEBUG) {
            Log.d(TAG, "New Locale: " + locale + (select ? " + select" : ""));
        }
        Intent data = new Intent(NewLocaleDialog.this, NewLocaleDialog.class);
        data.putExtra(INTENT_EXTRA_LOCALE, locale);
        data.putExtra(INTENT_EXTRA_SELECT, select);
        setResult(RESULT_OK, data);
        finish();
    }
}
