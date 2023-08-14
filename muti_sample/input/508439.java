class VpnProfileEditor {
    private static final String KEY_VPN_NAME = "vpn_name";
    private EditTextPreference mName;
    private EditTextPreference mServerName;
    private EditTextPreference mDomainSuffices;
    private VpnProfile mProfile;
    public VpnProfileEditor(VpnProfile p) {
        mProfile = p;
    }
    public VpnProfile getProfile() {
        return mProfile;
    }
    public void loadPreferencesTo(PreferenceGroup subpanel) {
        Context c = subpanel.getContext();
        mName = (EditTextPreference) subpanel.findPreference(KEY_VPN_NAME);
        mName.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        setName((String) newValue);
                        return true;
                    }
                });
        setName(getProfile().getName());
        mName.getEditText().setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        subpanel.addPreference(createServerNamePreference(c));
        loadExtraPreferencesTo(subpanel);
        subpanel.addPreference(createDomainSufficesPreference(c));
    }
    protected void loadExtraPreferencesTo(PreferenceGroup subpanel) {
    }
    public String validate() {
        String result = validate(mName, R.string.vpn_a_name);
        return ((result != null)
                ? result
                : validate(mServerName, R.string.vpn_a_vpn_server));
    }
    protected EditTextPreference createDomainSufficesPreference(Context c) {
        EditTextPreference pref = mDomainSuffices = createEditTextPreference(c,
                R.string.vpn_dns_search_list_title,
                R.string.vpn_dns_search_list,
                mProfile.getDomainSuffices(),
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        String v = ((String) newValue).trim();
                        mProfile.setDomainSuffices(v);
                        setSummary(pref, R.string.vpn_dns_search_list, v, false);
                        return true;
                    }
                });
        pref.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        return pref;
    }
    private Preference createServerNamePreference(Context c) {
        EditTextPreference pref = mServerName = createEditTextPreference(c,
                R.string.vpn_vpn_server_title,
                R.string.vpn_vpn_server,
                mProfile.getServerName(),
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        String v = ((String) newValue).trim();
                        mProfile.setServerName(v);
                        setSummary(pref, R.string.vpn_vpn_server, v);
                        return true;
                    }
                });
        pref.getEditText().setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        return pref;
    }
    protected EditTextPreference createEditTextPreference(Context c, int titleId,
            int prefNameId, String value,
            Preference.OnPreferenceChangeListener listener) {
        EditTextPreference pref = new EditTextPreference(c);
        pref.setTitle(titleId);
        pref.setDialogTitle(titleId);
        setSummary(pref, prefNameId, value);
        pref.setText(value);
        pref.setPersistent(true);
        pref.setOnPreferenceChangeListener(listener);
        return pref;
    }
    protected String validate(Preference pref, int fieldNameId) {
        Context c = pref.getContext();
        String value = (pref instanceof EditTextPreference)
                ? ((EditTextPreference) pref).getText()
                : ((ListPreference) pref).getValue();
        String formatString = (pref instanceof EditTextPreference)
                ? c.getString(R.string.vpn_error_miss_entering)
                : c.getString(R.string.vpn_error_miss_selecting);
        return (TextUtils.isEmpty(value)
                ? String.format(formatString, c.getString(fieldNameId))
                : null);
    }
    protected void setSummary(Preference pref, int fieldNameId, String v) {
        setSummary(pref, fieldNameId, v, true);
    }
    protected void setSummary(Preference pref, int fieldNameId, String v,
            boolean required) {
        Context c = pref.getContext();
        String formatString = required
                ? c.getString(R.string.vpn_field_not_set)
                : c.getString(R.string.vpn_field_not_set_optional);
        pref.setSummary(TextUtils.isEmpty(v)
                ? String.format(formatString, c.getString(fieldNameId))
                : v);
    }
    protected void setCheckBoxTitle(CheckBoxPreference pref, int fieldNameId) {
        Context c = pref.getContext();
        String formatString = c.getString(R.string.vpn_enable_field);
        pref.setTitle(String.format(formatString, c.getString(fieldNameId)));
    }
    private void setName(String newName) {
        newName = (newName == null) ? "" : newName.trim();
        mName.setText(newName);
        getProfile().setName(newName);
        setSummary(mName, R.string.vpn_name, newName);
    }
    protected static abstract class SecretHandler {
        private EditTextPreference mPref;
        private int mFieldNameId;
        private boolean mHadSecret;
        protected SecretHandler(Context c, int titleId, int fieldNameId) {
            String value = getSecretFromProfile();
            mHadSecret = !TextUtils.isEmpty(value);
            mFieldNameId = fieldNameId;
            EditTextPreference pref = mPref = new EditTextPreference(c);
            pref.setTitle(titleId);
            pref.setDialogTitle(titleId);
            pref.getEditText().setInputType(
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pref.getEditText().setTransformationMethod(
                    new PasswordTransformationMethod());
            pref.setText("");
            pref.getEditText().setHint(mHadSecret
                    ? R.string.vpn_secret_unchanged
                    : R.string.vpn_secret_not_set);
            setSecretSummary(value);
            pref.setPersistent(true);
            saveSecretToProfile("");
            pref.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        public boolean onPreferenceChange(
                                Preference pref, Object newValue) {
                            saveSecretToProfile((String) newValue);
                            setSecretSummary((String) newValue);
                            return true;
                        }
                    });
        }
        protected EditTextPreference getPreference() {
            return mPref;
        }
        protected String validate() {
            Context c = mPref.getContext();
            String value = mPref.getText();
            return ((TextUtils.isEmpty(value) && !mHadSecret)
                    ? String.format(
                            c.getString(R.string.vpn_error_miss_entering),
                            c.getString(mFieldNameId))
                    : null);
        }
        private void setSecretSummary(String value) {
            EditTextPreference pref = mPref;
            Context c = pref.getContext();
            String formatString = (TextUtils.isEmpty(value) && !mHadSecret)
                    ? c.getString(R.string.vpn_field_not_set)
                    : c.getString(R.string.vpn_field_is_set);
            pref.setSummary(
                    String.format(formatString, c.getString(mFieldNameId)));
        }
        protected abstract String getSecretFromProfile();
        protected abstract void saveSecretToProfile(String secret);
    }
}
