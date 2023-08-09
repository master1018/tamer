public class EngineSettings extends PreferenceActivity {
    private final static String MARKET_URI_START = "market:
    private static final int VOICE_DATA_CHECK_CODE = 42;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent();
        i.setClass(this, CheckVoiceData.class);
        startActivityForResult(i, VOICE_DATA_CHECK_CODE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == VOICE_DATA_CHECK_CODE){
            ArrayList<String> available = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES);
            ArrayList<String> unavailable = data.getStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES);
            addPreferencesFromResource(R.xml.voices_list);
            for (int i = 0; i < available.size(); i++){
                Log.e("debug", available.get(i));
                String[] languageCountry = available.get(i).split("-");
                Locale loc = new Locale(languageCountry[0], languageCountry[1]);
                Preference pref = findPreference(available.get(i));
                pref.setTitle(loc.getDisplayLanguage() + " (" + loc.getDisplayCountry() + ")");
                pref.setSummary(R.string.installed);
                pref.setEnabled(false);
            }
            for (int i = 0; i < unavailable.size(); i++){
                final String unavailableLang = unavailable.get(i);
                String[] languageCountry = unavailableLang.split("-");
                Locale loc = new Locale(languageCountry[0], languageCountry[1]);
                Preference pref = findPreference(unavailableLang);
                pref.setTitle(loc.getDisplayLanguage() + " (" + loc.getDisplayCountry() + ")");
                pref.setSummary(R.string.not_installed);
                pref.setEnabled(true);
                pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
                    public boolean onPreferenceClick(Preference preference) {
                        Uri marketUri = Uri.parse(MARKET_URI_START + unavailableLang.toLowerCase().replace("-", "."));
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                        startActivity(marketIntent);
                        return false;
                    }
                });
            }
        }
    }
}
