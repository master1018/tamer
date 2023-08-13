public class TestAppWidgetConfigure extends Activity {
    static final String TAG = "TestAppWidgetConfigure";
    public TestAppWidgetConfigure() {
        super();
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.test_appwidget_configure);
        findViewById(R.id.save_button).setOnClickListener(mOnClickListener);
    }
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            String text = ((EditText)findViewById(R.id.edit_text)).getText().toString();
            Log.d(TAG, "text is '" + text + '\'');
            SharedPreferences.Editor prefs = getSharedPreferences(TestAppWidgetProvider.PREFS_NAME, 0)
                    .edit();
            prefs.putString(TestAppWidgetProvider.PREF_PREFIX_KEY, text);
            prefs.commit();
            setResult(RESULT_OK);
            finish();
        }
    };
}
