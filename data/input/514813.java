public class CalendarPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String BUILD_VERSION = "build_version";
    private static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";
    static final String KEY_HIDE_DECLINED = "preferences_hide_declined";
    static final String KEY_ALERTS_TYPE = "preferences_alerts_type";
    static final String KEY_ALERTS_VIBRATE = "preferences_alerts_vibrate";
    static final String KEY_ALERTS_VIBRATE_WHEN = "preferences_alerts_vibrateWhen";
    static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    static final String KEY_DEFAULT_REMINDER = "preferences_default_reminder";
    static final String KEY_START_VIEW = "startView";
    static final String KEY_DETAILED_VIEW = "preferredDetailedView";
    static final String KEY_DEFAULT_CALENDAR = "preference_defaultCalendar";
    static final String ALERT_TYPE_ALERTS = "0";
    static final String ALERT_TYPE_STATUS_BAR = "1";
    static final String ALERT_TYPE_OFF = "2";
    static final String DEFAULT_START_VIEW =
            CalendarApplication.ACTIVITY_NAMES[CalendarApplication.MONTH_VIEW_ID];
    static final String DEFAULT_DETAILED_VIEW =
            CalendarApplication.ACTIVITY_NAMES[CalendarApplication.DAY_VIEW_ID];
    ListPreference mAlertType;
    ListPreference mVibrateWhen;
    RingtonePreference mRingtone;
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }
    public static void setDefaultValues(Context context) {
        PreferenceManager.setDefaultValues(context, SHARED_PREFS_NAME, Context.MODE_PRIVATE,
                R.xml.preferences, false);
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceManager preferenceManager = getPreferenceManager();
        SharedPreferences sharedPreferences = getSharedPreferences(this);
        preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        preferenceScreen.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        mAlertType = (ListPreference) preferenceScreen.findPreference(KEY_ALERTS_TYPE);
        mVibrateWhen = (ListPreference) preferenceScreen.findPreference(KEY_ALERTS_VIBRATE_WHEN);
        mRingtone = (RingtonePreference) preferenceScreen.findPreference(KEY_ALERTS_RINGTONE);
        if (!sharedPreferences.contains(KEY_ALERTS_VIBRATE_WHEN) &&
                sharedPreferences.contains(KEY_ALERTS_VIBRATE)) {
            int stringId = sharedPreferences.getBoolean(KEY_ALERTS_VIBRATE, false) ?
                    R.string.prefDefault_alerts_vibrate_true :
                    R.string.prefDefault_alerts_vibrate_false;
            mVibrateWhen.setValue(getString(stringId));
        }
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            findPreference(BUILD_VERSION).setSummary(packageInfo.versionName);
        } catch (NameNotFoundException e) {
            findPreference(BUILD_VERSION).setSummary("?");
        }
        updateChildPreferences();
    }
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_ALERTS_TYPE)) {
            updateChildPreferences();
        }
    }
    private void updateChildPreferences() {
        if (mAlertType.getValue().equals(ALERT_TYPE_OFF)) {
            mVibrateWhen.setValue(getString(R.string.prefDefault_alerts_vibrate_false));
            mVibrateWhen.setEnabled(false);
            mRingtone.setEnabled(false);
        } else {
            mVibrateWhen.setEnabled(true);
            mRingtone.setEnabled(true);
        }
    }
}
