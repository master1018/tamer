public class VpnTypeSelection extends PreferenceActivity {
    private Map<String, VpnType> mTypeMap = new HashMap<String, VpnType>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.vpn_type);
        initTypeList();
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen ps, Preference pref) {
        setResult(mTypeMap.get(pref.getTitle().toString()));
        finish();
        return true;
    }
    private void initTypeList() {
        PreferenceScreen root = getPreferenceScreen();
        for (VpnType t : VpnManager.getSupportedVpnTypes()) {
            String displayName = t.getDisplayName();
            String message = String.format(
                    getString(R.string.vpn_edit_title_add), displayName);
            mTypeMap.put(message, t);
            Preference pref = new Preference(this);
            pref.setTitle(message);
            pref.setSummary(t.getDescriptionId());
            root.addPreference(pref);
        }
    }
    private void setResult(VpnType type) {
        Intent intent = new Intent(this, VpnSettings.class);
        intent.putExtra(VpnSettings.KEY_VPN_TYPE, type.toString());
        setResult(RESULT_OK, intent);
    }
}
