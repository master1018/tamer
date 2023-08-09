public class PhysicalKeyboardSettings extends PreferenceActivity {
    private final String[] mSettingsUiKey = {
            "auto_caps",
            "auto_replace",
            "auto_punctuate",
    };
    private final String[] mSettingsSystemId = {
            System.TEXT_AUTO_CAPS,
            System.TEXT_AUTO_REPLACE,
            System.TEXT_AUTO_PUNCTUATE,
    };
    private final int[] mSettingsDefault = {
            1,
            1,
            1,
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.keyboard_settings);
    }
    @Override
    protected void onResume() {
        super.onResume();
        ContentResolver resolver = getContentResolver();
        for (int i = 0; i < mSettingsUiKey.length; i++) {
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(mSettingsUiKey[i]);
            pref.setChecked(System.getInt(resolver, mSettingsSystemId[i],
                                          mSettingsDefault[i]) > 0);
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        for (int i = 0; i < mSettingsUiKey.length; i++) {
            if (mSettingsUiKey[i].equals(preference.getKey())) {
                System.putInt(getContentResolver(), mSettingsSystemId[i], 
                        ((CheckBoxPreference)preference).isChecked()? 1 : 0);
                return true;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
