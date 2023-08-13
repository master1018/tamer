public class RedirectMain extends Activity {
    static final int INIT_TEXT_REQUEST = 0;
    static final int NEW_TEXT_REQUEST = 1;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redirect_main);
        Button clearButton = (Button)findViewById(R.id.clear);
        clearButton.setOnClickListener(mClearListener);
        Button newButton = (Button)findViewById(R.id.newView);
        newButton.setOnClickListener(mNewListener);
        if (!loadPrefs()) {
            Intent intent = new Intent(this, RedirectGetter.class);
            startActivityForResult(intent, INIT_TEXT_REQUEST);
        }
    }
    @Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent data) {
        if (requestCode == INIT_TEXT_REQUEST) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                loadPrefs();
            }
        } else if (requestCode == NEW_TEXT_REQUEST) {
            if (resultCode != RESULT_CANCELED) {
                loadPrefs();
            }
        }
    }
    private final boolean loadPrefs() {
        SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
        mTextPref = preferences.getString("text", null);
        if (mTextPref != null) {
            TextView text = (TextView)findViewById(R.id.text);
            text.setText(mTextPref);
            return true;
        }
        return false;
    }
    private OnClickListener mClearListener = new OnClickListener() {
        public void onClick(View v) {
            SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
            preferences.edit().remove("text").commit();
            finish();
        }
    };
    private OnClickListener mNewListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(RedirectMain.this, RedirectGetter.class);
            startActivityForResult(intent, NEW_TEXT_REQUEST);
        }
    };
    private String mTextPref;
}
