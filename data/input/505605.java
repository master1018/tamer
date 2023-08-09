class PptpEditor extends VpnProfileEditor {
    private CheckBoxPreference mEncryption;
    public PptpEditor(PptpProfile p) {
        super(p);
    }
    @Override
    protected void loadExtraPreferencesTo(PreferenceGroup subpanel) {
        Context c = subpanel.getContext();
        subpanel.addPreference(createEncryptionPreference(c));
        PptpProfile profile = (PptpProfile) getProfile();
    }
    private Preference createEncryptionPreference(Context c) {
        final PptpProfile profile = (PptpProfile) getProfile();
        CheckBoxPreference encryption = mEncryption = new CheckBoxPreference(c);
        boolean enabled = profile.isEncryptionEnabled();
        setCheckBoxTitle(encryption, R.string.vpn_pptp_encryption_title);
        encryption.setChecked(enabled);
        setEncryptionSummary(encryption, enabled);
        encryption.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        boolean enabled = (Boolean) newValue;
                        profile.setEncryptionEnabled(enabled);
                        setEncryptionSummary(mEncryption, enabled);
                        return true;
                    }
                });
        return encryption;
    }
    private void setEncryptionSummary(CheckBoxPreference encryption,
            boolean enabled) {
        Context c = encryption.getContext();
        String formatString = c.getString(enabled
                ? R.string.vpn_is_enabled
                : R.string.vpn_is_disabled);
        encryption.setSummary(String.format(
                formatString, c.getString(R.string.vpn_pptp_encryption)));
    }
}
