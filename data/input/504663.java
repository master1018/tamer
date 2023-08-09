public class GsmUmtsCallOptions extends PreferenceActivity {
    private static final String LOG_TAG = "GsmUmtsCallOptions";
    private final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.gsm_umts_call_options);
        if (PhoneFactory.getDefaultPhone().getPhoneType() != Phone.PHONE_TYPE_GSM) {
            getPreferenceScreen().setEnabled(false);
        }
    }
}
