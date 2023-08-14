class L2tpIpsecPskEditor extends L2tpEditor {
    private EditTextPreference mPresharedKey;
    private SecretHandler mPskHandler;
    public L2tpIpsecPskEditor(L2tpIpsecPskProfile p) {
        super(p);
    }
    @Override
    protected void loadExtraPreferencesTo(PreferenceGroup subpanel) {
        Context c = subpanel.getContext();
        subpanel.addPreference(createPresharedKeyPreference(c));
        super.loadExtraPreferencesTo(subpanel);
    }
    @Override
    public String validate() {
        String result = super.validate();
        return ((result != null) ? result : mPskHandler.validate());
    }
    private Preference createPresharedKeyPreference(Context c) {
        SecretHandler pskHandler = mPskHandler = new SecretHandler(c,
                R.string.vpn_ipsec_presharedkey_title,
                R.string.vpn_ipsec_presharedkey) {
            @Override
            protected String getSecretFromProfile() {
                return ((L2tpIpsecPskProfile) getProfile()).getPresharedKey();
            }
            @Override
            protected void saveSecretToProfile(String secret) {
                ((L2tpIpsecPskProfile) getProfile()).setPresharedKey(secret);
            }
        };
        return pskHandler.getPreference();
    }
}
