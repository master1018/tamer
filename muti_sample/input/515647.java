class L2tpIpsecEditor extends L2tpEditor {
    private static final String TAG = L2tpIpsecEditor.class.getSimpleName();
    private KeyStore mKeyStore = KeyStore.getInstance();
    private ListPreference mUserCertificate;
    private ListPreference mCaCertificate;
    private L2tpIpsecProfile mProfile;
    public L2tpIpsecEditor(L2tpIpsecProfile p) {
        super(p);
        mProfile = p;
    }
    @Override
    protected void loadExtraPreferencesTo(PreferenceGroup subpanel) {
        super.loadExtraPreferencesTo(subpanel);
        Context c = subpanel.getContext();
        subpanel.addPreference(createUserCertificatePreference(c));
        subpanel.addPreference(createCaCertificatePreference(c));
    }
    @Override
    public String validate() {
        String result = super.validate();
        if (result == null) {
            result = validate(mUserCertificate, R.string.vpn_a_user_certificate);
        }
        if (result == null) {
            result = validate(mCaCertificate, R.string.vpn_a_ca_certificate);
        }
        return result;
    }
    private Preference createUserCertificatePreference(Context c) {
        mUserCertificate = createListPreference(c,
                R.string.vpn_user_certificate_title,
                mProfile.getUserCertificate(),
                mKeyStore.saw(Credentials.USER_CERTIFICATE),
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        mProfile.setUserCertificate((String) newValue);
                        setSummary(pref, R.string.vpn_user_certificate,
                                (String) newValue);
                        return true;
                    }
                });
        setSummary(mUserCertificate, R.string.vpn_user_certificate,
                mProfile.getUserCertificate());
        return mUserCertificate;
    }
    private Preference createCaCertificatePreference(Context c) {
        mCaCertificate = createListPreference(c,
                R.string.vpn_ca_certificate_title,
                mProfile.getCaCertificate(),
                mKeyStore.saw(Credentials.CA_CERTIFICATE),
                new Preference.OnPreferenceChangeListener() {
                    public boolean onPreferenceChange(
                            Preference pref, Object newValue) {
                        mProfile.setCaCertificate((String) newValue);
                        setSummary(pref, R.string.vpn_ca_certificate,
                                (String) newValue);
                        return true;
                    }
                });
        setSummary(mCaCertificate, R.string.vpn_ca_certificate,
                mProfile.getCaCertificate());
        return mCaCertificate;
    }
    private ListPreference createListPreference(Context c, int titleResId,
            String text, String[] keys,
            Preference.OnPreferenceChangeListener listener) {
        ListPreference pref = new ListPreference(c);
        pref.setTitle(titleResId);
        pref.setDialogTitle(titleResId);
        pref.setPersistent(true);
        pref.setEntries(keys);
        pref.setEntryValues(keys);
        pref.setValue(text);
        pref.setOnPreferenceChangeListener(listener);
        return pref;
    }
}
