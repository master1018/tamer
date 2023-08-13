public class AdvancedPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    public static final String KEY_MY_PREFERENCE = "my_preference";
    public static final String KEY_ADVANCED_CHECKBOX_PREFERENCE = "advanced_checkbox_preference";
    private CheckBoxPreference mCheckBoxPreference;
    private Handler mHandler = new Handler();
    private Runnable mForceCheckBoxRunnable = new Runnable() {
        public void run() {
            if (mCheckBoxPreference != null) {
                mCheckBoxPreference.setChecked(!mCheckBoxPreference.isChecked());
            }
            mHandler.postDelayed(this, 1000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.advanced_preferences);
        mCheckBoxPreference = (CheckBoxPreference)getPreferenceScreen().findPreference(
                KEY_ADVANCED_CHECKBOX_PREFERENCE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mForceCheckBoxRunnable.run();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mHandler.removeCallbacks(mForceCheckBoxRunnable);
    }
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_MY_PREFERENCE)) {
            Toast.makeText(this, "Thanks! You increased my count to "
                    + sharedPreferences.getInt(key, 0), Toast.LENGTH_SHORT).show();
        }
    }
}
