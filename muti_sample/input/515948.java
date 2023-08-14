public class SettingActivity extends android.preference.PreferenceActivity {
    private static final String KEY_NOTIFICATION_SOUND = "notification-sound";
    private static final String KEY_NOTIFICATION_VIBRATE = "notification-vibrate";
    private static final String KEY_ENABLE_NOTIFICATIONS = "enable-notifications";
    private static final String KEY_HIDE_OFFLINE_CONTACTS = "hide-offline-contacts";
    private long mProviderId;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        Intent intent = getIntent();
        mProviderId = intent.getLongExtra(ImServiceConstants.EXTRA_INTENT_PROVIDER_ID, -1);
        if (mProviderId < 0) {
            Log.e(ImApp.LOG_TAG,"SettingActivity intent requires provider id extra");
            throw new RuntimeException("SettingActivity must be created with an provider id");
        }
        setInitialValues();
    }
    private void setInitialValues() {
        Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                getContentResolver(), mProviderId,
                false , null );
        CheckBoxPreference pref = (CheckBoxPreference) findPreference(KEY_HIDE_OFFLINE_CONTACTS);
        pref.setChecked(settings.getHideOfflineContacts());
        pref = (CheckBoxPreference) findPreference(KEY_ENABLE_NOTIFICATIONS);
        pref.setChecked(settings.getEnableNotification());
        pref = (CheckBoxPreference) findPreference(KEY_NOTIFICATION_VIBRATE);
        pref.setChecked(settings.getVibrate());
        pref = (CheckBoxPreference) findPreference(KEY_NOTIFICATION_SOUND);
        pref.setChecked(settings.getRingtoneURI() != null);
        settings.close();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof CheckBoxPreference) {
            final Imps.ProviderSettings.QueryMap settings = new Imps.ProviderSettings.QueryMap(
                    getContentResolver(), mProviderId,
                    false , null );
            String key = preference.getKey();
            boolean value = ((CheckBoxPreference) preference).isChecked();
            if (key.equals(KEY_HIDE_OFFLINE_CONTACTS)) {
                settings.setHideOfflineContacts(value);
            } else if (key.equals(KEY_ENABLE_NOTIFICATIONS)) {
                settings.setEnableNotification(value);
            } else if (key.equals(KEY_NOTIFICATION_VIBRATE)) {
                settings.setVibrate(value);
            } else if (key.equals(KEY_NOTIFICATION_SOUND)){
                if (!value) {
                    settings.setRingtoneURI(null);
                }
            }
            settings.close();
            return true;
        }
        return false;
    }
}
