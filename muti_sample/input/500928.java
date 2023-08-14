public class LaunchingPreferences extends Activity implements OnClickListener {
    private static final int REQUEST_CODE_PREFERENCES = 1;
    private TextView mCounterText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);
        Button launchPreferences = new Button(this);
        launchPreferences.setText(getString(R.string.launch_preference_activity));
        launchPreferences.setOnClickListener(this);
        layout.addView(launchPreferences, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        mCounterText = new TextView(this);
        layout.addView(mCounterText, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        updateCounterText();
    }
    public void onClick(View v) {
        Intent launchPreferencesIntent = new Intent().setClass(this, AdvancedPreferences.class);
        startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PREFERENCES) {
            updateCounterText();
        }
    }
    private void updateCounterText() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final int counter = sharedPref.getInt(AdvancedPreferences.KEY_MY_PREFERENCE, 0);
        mCounterText.setText(getString(R.string.counter_value_is) + " " + counter);
    }
}
