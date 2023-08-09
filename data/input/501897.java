class L2tpEditor extends VpnProfileEditor {
    private CheckBoxPreference mSecret;
    private SecretHandler mSecretHandler;
    public L2tpEditor(L2tpProfile p) {
        super(p);
    }
    @Override
    protected void loadExtraPreferencesTo(PreferenceGroup subpanel) {
        Context c = subpanel.getContext();
        subpanel.addPreference(createSecretPreference(c));
        subpanel.addPreference(createSecretStringPreference(c));
        L2tpProfile profile = (L2tpProfile) getProfile();
    }
    @Override
    public String validate() {
        String result = super.validate();
        if (!mSecret.isChecked()) return result;
        return ((result != null) ? result : mSecretHandler.validate());
    }
    private Preference createSecretPreference(Context c) {
        final L2tpProfile profile = (L2tpProfile) getProfile();
        CheckBoxPreference secret = mSecret = new CheckBoxPreference(c);
        boolean enabled = profile.isSecretEnabled();
        setCheckBoxTitle(secret, R.string.vpn_l2tp_secret);
        secret.setChecked(enabled);
        setSecretSummary(secret, enabled);
        secret.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        boolean enabled = (Boolean) newValue;
                        profile.setSecretEnabled(enabled);
                        mSecretHandler.getPreference().setEnabled(enabled);
                        setSecretSummary(mSecret, enabled);
                        return true;
                    }
                });
        return secret;
    }
    private Preference createSecretStringPreference(Context c) {
        SecretHandler sHandler = mSecretHandler = new SecretHandler(c,
                R.string.vpn_l2tp_secret_string_title,
                R.string.vpn_l2tp_secret) {
            @Override
            protected String getSecretFromProfile() {
                return ((L2tpProfile) getProfile()).getSecretString();
            }
            @Override
            protected void saveSecretToProfile(String secret) {
                ((L2tpProfile) getProfile()).setSecretString(secret);
            }
        };
        Preference pref = sHandler.getPreference();
        pref.setEnabled(mSecret.isChecked());
        return pref;
    }
    private void setSecretSummary(CheckBoxPreference secret, boolean enabled) {
        Context c = secret.getContext();
        String formatString = c.getString(enabled
                ? R.string.vpn_is_enabled
                : R.string.vpn_is_disabled);
        secret.setSummary(String.format(
                formatString, c.getString(R.string.vpn_l2tp_secret)));
    }
}
