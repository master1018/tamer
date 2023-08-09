public class VoiceRecognitionService extends RecognitionService {
    @Override
    protected void onCancel(Callback listener) {
    }
    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {
        ArrayList<String> results = new ArrayList<String>();
        SharedPreferences prefs = getSharedPreferences(
                VoiceRecognitionSettings.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        String resultType = prefs.getString(
                VoiceRecognitionSettings.PREF_KEY_RESULTS_TYPE,
                String.valueOf(VoiceRecognitionSettings.RESULT_TYPE_LETTERS));
        int resultTypeInt = Integer.parseInt(resultType);
        if (resultTypeInt == VoiceRecognitionSettings.RESULT_TYPE_LETTERS) {
            results.add("a");
            results.add("b");
            results.add("c");            
        } else if (resultTypeInt == VoiceRecognitionSettings.RESULT_TYPE_NUMBERS) {
            results.add("1");
            results.add("2");
            results.add("3");
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION, results);
        try {
            listener.results(bundle);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onStopListening(Callback listener) {
    }
}
