public class CheckVoiceData extends Activity {
    private final static String PICO_LINGWARE_PATH =
            Environment.getExternalStorageDirectory() + "/svox/";
    private final static String PICO_SYSTEM_LINGWARE_PATH = "/system/tts/lang_pico/";
    private final static String[] dataFiles = {
            "de-DE_gl0_sg.bin", "de-DE_ta.bin", "en-GB_kh0_sg.bin", "en-GB_ta.bin",
            "en-US_lh0_sg.bin", "en-US_ta.bin", "es-ES_ta.bin", "es-ES_zl0_sg.bin",
            "fr-FR_nk0_sg.bin", "fr-FR_ta.bin", "it-IT_cm0_sg.bin", "it-IT_ta.bin"
    };
    private final static String[] dataFilesInfo = {
        "deu-DEU", "deu-DEU", "eng-GBR", "eng-GBR", "eng-USA", "eng-USA",
        "spa-ESP", "spa-ESP", "fra-FRA", "fra-FRA", "ita-ITA", "ita-ITA"
    };
    private final static String[] supportedLanguages = {
        "deu-DEU", "eng-GBR", "eng-USA", "spa-ESP", "fra-FRA", "ita-ITA"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int result = TextToSpeech.Engine.CHECK_VOICE_DATA_PASS;
        boolean foundMatch = false;
        ArrayList<String> available = new ArrayList<String>();
        ArrayList<String> unavailable = new ArrayList<String>();
        HashMap<String, Boolean> languageCountry = new HashMap<String, Boolean>();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            ArrayList<String> langCountryVars = bundle.getStringArrayList(
                    TextToSpeech.Engine.EXTRA_CHECK_VOICE_DATA_FOR);
            if (langCountryVars != null){
                for (int i = 0; i < langCountryVars.size(); i++){
                    if (langCountryVars.get(i).length() > 0){
                        languageCountry.put(langCountryVars.get(i), true);
                    }
                }
            }
        }
        for (int i = 0; i < supportedLanguages.length; i++){
            if ((languageCountry.size() < 1) ||
                (languageCountry.containsKey(supportedLanguages[i]))){
                if (!fileExists(dataFiles[2 * i]) ||
                    !fileExists(dataFiles[(2 * i) + 1])){
                    result = TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA;
                    unavailable.add(supportedLanguages[i]);
                } else {
                    available.add(supportedLanguages[i]);
                    foundMatch = true;
                }
            }
        }
        if ((languageCountry.size() > 0) && !foundMatch){
            result = TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL;
        }
        Intent returnData = new Intent();
        returnData.putExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_ROOT_DIRECTORY, PICO_LINGWARE_PATH);
        returnData.putExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES, dataFiles);
        returnData.putExtra(TextToSpeech.Engine.EXTRA_VOICE_DATA_FILES_INFO, dataFilesInfo);
        returnData.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_AVAILABLE_VOICES, available);
        returnData.putStringArrayListExtra(TextToSpeech.Engine.EXTRA_UNAVAILABLE_VOICES, unavailable);
        setResult(result, returnData);
        finish();
    }
    private boolean fileExists(String filename){
        File tempFile = new File(PICO_LINGWARE_PATH + filename);
        File tempFileSys = new File(PICO_SYSTEM_LINGWARE_PATH + filename);
        if ((!tempFile.exists()) && (!tempFileSys.exists())) {
            return false;
        }
        return true;
    }
}
