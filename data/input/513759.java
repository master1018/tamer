public class RedirectGetter extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redirect_getter);
        Button applyButton = (Button)findViewById(R.id.apply);
        applyButton.setOnClickListener(mApplyListener);
        mText = (TextView)findViewById(R.id.text);
    }
    private final boolean loadPrefs()
    {
        SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
        mTextPref = preferences.getString("text", null);
        if (mTextPref != null) {
            mText.setText(mTextPref);
            return true;
        }
        return false;
    }
    private OnClickListener mApplyListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            SharedPreferences preferences = getSharedPreferences("RedirectData", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("text", mText.getText().toString());
            if (editor.commit()) {
                setResult(RESULT_OK);
            }
            finish();
        }
    };
    private String mTextPref;
    TextView mText;
}
