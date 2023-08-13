public class VoiceRecognitionSettings extends PreferenceActivity {
    public static final String SHARED_PREFERENCES_NAME = "VoiceRecognitionService";
    public static final String PREF_KEY_RESULTS_TYPE = "results_type";
    public static final int RESULT_TYPE_LETTERS = 0;
    public static final int RESULT_TYPE_NUMBERS = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.preferences);
    }
}
