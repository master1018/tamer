public class CdmaOptions extends PreferenceActivity {
    private CdmaRoamingListPreference mButtonCdmaRoam;
    private static final String BUTTON_CDMA_ROAMING_KEY = "cdma_roaming_mode_key";
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.cdma_options);
        PreferenceScreen prefSet = getPreferenceScreen();
        mButtonCdmaRoam =
                (CdmaRoamingListPreference) prefSet.findPreference(BUTTON_CDMA_ROAMING_KEY);
        if (PhoneFactory.getDefaultPhone().getPhoneType() != Phone.PHONE_TYPE_CDMA) {
            mButtonCdmaRoam.setEnabled(false);
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(BUTTON_CDMA_ROAMING_KEY)) {
            return true;
        }
        return false;
    }
}
