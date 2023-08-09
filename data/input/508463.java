public class DataUsage extends PreferenceActivity {
    private Preference mCurrentUsagePref;
    private Preference mTimeFramePref;
    private Preference mThrottleRatePref;
    private Preference mHelpPref;
    private String mHelpUri;
    private DataUsageListener mDataUsageListener;
    private ThrottleManager mThrottleManager;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mThrottleManager = (ThrottleManager) getSystemService(Context.THROTTLE_SERVICE);
        addPreferencesFromResource(R.xml.data_usage_settings);
        mCurrentUsagePref = findPreference("throttle_current_usage");
        mTimeFramePref = findPreference("throttle_time_frame");
        mThrottleRatePref = findPreference("throttle_rate");
        mHelpPref = findPreference("throttle_help");
        mHelpUri = mThrottleManager.getHelpUri();
        if (mHelpUri == null ) {
            getPreferenceScreen().removePreference(mHelpPref);
        } else {
            mHelpPref.setSummary(getString(R.string.throttle_help_subtext));
        }
        mDataUsageListener = new DataUsageListener(this, mCurrentUsagePref,
                mTimeFramePref, mThrottleRatePref);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mDataUsageListener.resume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mDataUsageListener.pause();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mHelpPref) {
            try {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mHelpUri));
                startActivity(myIntent);
            } catch (Exception e) {
                ;
            }
        }
        return true;
    }
}
