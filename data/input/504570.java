public class RecognizerIntent {
    public final static String EXTRA_CALLING_PACKAGE = "calling_package";
    private RecognizerIntent() {
    }
    public static final String ACTION_RECOGNIZE_SPEECH = "android.speech.action.RECOGNIZE_SPEECH";
    public static final String ACTION_WEB_SEARCH = "android.speech.action.WEB_SEARCH";
    public static final String EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS =
            "android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS";
    public static final String EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS =
            "android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS";
    public static final String EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS =
            "android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS";
    public static final String EXTRA_LANGUAGE_MODEL = "android.speech.extra.LANGUAGE_MODEL";
    public static final String LANGUAGE_MODEL_FREE_FORM = "free_form";
    public static final String LANGUAGE_MODEL_WEB_SEARCH = "web_search";
    public static final String EXTRA_PROMPT = "android.speech.extra.PROMPT";
    public static final String EXTRA_LANGUAGE = "android.speech.extra.LANGUAGE";
    public static final String EXTRA_MAX_RESULTS = "android.speech.extra.MAX_RESULTS";
    public static final String EXTRA_PARTIAL_RESULTS = "android.speech.extra.PARTIAL_RESULTS";
    public static final String EXTRA_RESULTS_PENDINGINTENT = 
            "android.speech.extra.RESULTS_PENDINGINTENT";
    public static final String EXTRA_RESULTS_PENDINGINTENT_BUNDLE = 
            "android.speech.extra.RESULTS_PENDINGINTENT_BUNDLE";
    public static final int RESULT_NO_MATCH = Activity.RESULT_FIRST_USER;
    public static final int RESULT_CLIENT_ERROR = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_SERVER_ERROR = Activity.RESULT_FIRST_USER + 2;
    public static final int RESULT_NETWORK_ERROR = Activity.RESULT_FIRST_USER + 3;
    public static final int RESULT_AUDIO_ERROR = Activity.RESULT_FIRST_USER + 4;
    public static final String EXTRA_RESULTS = "android.speech.extra.RESULTS";
    public static final Intent getVoiceDetailsIntent(Context context) {
        Intent voiceSearchIntent = new Intent(ACTION_WEB_SEARCH);
        ResolveInfo ri = context.getPackageManager().resolveActivity(
                voiceSearchIntent, PackageManager.GET_META_DATA);
        if (ri == null || ri.activityInfo == null || ri.activityInfo.metaData == null) return null;
        String className = ri.activityInfo.metaData.getString(DETAILS_META_DATA);
        if (className == null) return null;
        Intent detailsIntent = new Intent(ACTION_GET_LANGUAGE_DETAILS);
        detailsIntent.setComponent(new ComponentName(ri.activityInfo.packageName, className));
        return detailsIntent;
    }
    public static final String DETAILS_META_DATA = "android.speech.DETAILS";
    public static final String ACTION_GET_LANGUAGE_DETAILS =
            "android.speech.action.GET_LANGUAGE_DETAILS";
    public static final String EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE =
            "android.speech.extra.ONLY_RETURN_LANGUAGE_PREFERENCE";
    public static final String EXTRA_LANGUAGE_PREFERENCE =
            "android.speech.extra.LANGUAGE_PREFERENCE";
    public static final String EXTRA_SUPPORTED_LANGUAGES =
            "android.speech.extra.SUPPORTED_LANGUAGES";
}
