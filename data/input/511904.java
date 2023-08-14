public class OpenWnnControlPanelJAJP extends PreferenceActivity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OpenWnnJAJP.getInstance() == null) {
            new OpenWnnJAJP(this);
        }
        addPreferencesFromResource(R.xml.openwnn_pref_ja);
    }
}
