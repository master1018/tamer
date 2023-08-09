public class VoiceInput implements OnClickListener {
    private static final String TAG = "VoiceInput";
    private static final String EXTRA_RECOGNITION_CONTEXT =
            "android.speech.extras.RECOGNITION_CONTEXT";
    private static final String EXTRA_CALLING_PACKAGE = "calling_package";
    private static final String DEFAULT_RECOMMENDED_PACKAGES =
            "com.android.mms " +
            "com.google.android.gm " +
            "com.google.android.talk " +
            "com.google.android.apps.googlevoice " +
            "com.android.email " +
            "com.android.browser ";
    public static boolean ENABLE_WORD_CORRECTIONS = false;
    public static final String DELETE_SYMBOL = " \u00D7 ";  
    private Whitelist mRecommendedList;
    private Whitelist mBlacklist;
    private VoiceInputLogger mLogger;
    private static final String EXTRA_SPEECH_MINIMUM_LENGTH_MILLIS =
            "android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS";
    private static final String EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS =
            "android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS";
    private static final String EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS =
            "android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS";
    private static final String INPUT_COMPLETE_SILENCE_LENGTH_DEFAULT_VALUE_MILLIS = "1000";
    public static final int DEFAULT = 0;
    public static final int LISTENING = 1;
    public static final int WORKING = 2;
    public static final int ERROR = 3;
    private int mAfterVoiceInputDeleteCount = 0;
    private int mAfterVoiceInputInsertCount = 0;
    private int mAfterVoiceInputInsertPunctuationCount = 0;
    private int mAfterVoiceInputCursorPos = 0;
    private int mAfterVoiceInputSelectionSpan = 0;
    private int mState = DEFAULT;
    private final static int MSG_CLOSE_ERROR_DIALOG = 1;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_CLOSE_ERROR_DIALOG) {
                mState = DEFAULT;
                mRecognitionView.finish();
                mUiListener.onCancelVoice();
            }
        }
    };
    public interface UiListener {
        public void onVoiceResults(
            List<String> recognitionResults,
            Map<String, List<CharSequence>> alternatives);
        public void onCancelVoice();
    }
    private SpeechRecognizer mSpeechRecognizer;
    private RecognitionListener mRecognitionListener;
    private RecognitionView mRecognitionView;
    private UiListener mUiListener;
    private Context mContext;
    public VoiceInput(Context context, UiListener uiHandler) {
        mLogger = VoiceInputLogger.getLogger(context);
        mRecognitionListener = new ImeRecognitionListener();
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mUiListener = uiHandler;
        mContext = context;
        newView();
        String recommendedPackages = SettingsUtil.getSettingsString(
                context.getContentResolver(),
                SettingsUtil.LATIN_IME_VOICE_INPUT_RECOMMENDED_PACKAGES,
                DEFAULT_RECOMMENDED_PACKAGES);
        mRecommendedList = new Whitelist();
        for (String recommendedPackage : recommendedPackages.split("\\s+")) {
            mRecommendedList.addApp(recommendedPackage);
        }
        mBlacklist = new Whitelist();
        mBlacklist.addApp("com.android.setupwizard");
    }
    public void setCursorPos(int pos) {
        mAfterVoiceInputCursorPos = pos;
    }
    public int getCursorPos() {
        return mAfterVoiceInputCursorPos;
    }
    public void setSelectionSpan(int span) {
        mAfterVoiceInputSelectionSpan = span;
    }
    public int getSelectionSpan() {
        return mAfterVoiceInputSelectionSpan;
    }
    public void incrementTextModificationDeleteCount(int count){
        mAfterVoiceInputDeleteCount += count;
        if (mAfterVoiceInputInsertCount > 0) {
            logTextModifiedByTypingInsertion(mAfterVoiceInputInsertCount);
            mAfterVoiceInputInsertCount = 0;
        }
        if (mAfterVoiceInputInsertPunctuationCount > 0) {
            logTextModifiedByTypingInsertionPunctuation(mAfterVoiceInputInsertPunctuationCount);
            mAfterVoiceInputInsertPunctuationCount = 0;
        }
    }
    public void incrementTextModificationInsertCount(int count){
        mAfterVoiceInputInsertCount += count;
        if (mAfterVoiceInputSelectionSpan > 0) {
            mAfterVoiceInputDeleteCount += mAfterVoiceInputSelectionSpan;
        }
        if (mAfterVoiceInputDeleteCount > 0) {
            logTextModifiedByTypingDeletion(mAfterVoiceInputDeleteCount);
            mAfterVoiceInputDeleteCount = 0;
        }
        if (mAfterVoiceInputInsertPunctuationCount > 0) {
            logTextModifiedByTypingInsertionPunctuation(mAfterVoiceInputInsertPunctuationCount);
            mAfterVoiceInputInsertPunctuationCount = 0;
        }
    }
    public void incrementTextModificationInsertPunctuationCount(int count){
        mAfterVoiceInputInsertPunctuationCount += 1;
        if (mAfterVoiceInputSelectionSpan > 0) {
            mAfterVoiceInputDeleteCount += mAfterVoiceInputSelectionSpan;
        }
        if (mAfterVoiceInputDeleteCount > 0) {
            logTextModifiedByTypingDeletion(mAfterVoiceInputDeleteCount);
            mAfterVoiceInputDeleteCount = 0;
        }
        if (mAfterVoiceInputInsertCount > 0) {
            logTextModifiedByTypingInsertion(mAfterVoiceInputInsertCount);
            mAfterVoiceInputInsertCount = 0;
        }
    }
    public void flushAllTextModificationCounters() {
        if (mAfterVoiceInputInsertCount > 0) {
            logTextModifiedByTypingInsertion(mAfterVoiceInputInsertCount);
            mAfterVoiceInputInsertCount = 0;
        }
        if (mAfterVoiceInputDeleteCount > 0) {
            logTextModifiedByTypingDeletion(mAfterVoiceInputDeleteCount);
            mAfterVoiceInputDeleteCount = 0;
        }
        if (mAfterVoiceInputInsertPunctuationCount > 0) {
            logTextModifiedByTypingInsertionPunctuation(mAfterVoiceInputInsertPunctuationCount);
            mAfterVoiceInputInsertPunctuationCount = 0;
        }
    }
    public void onConfigurationChanged() {
        mRecognitionView.restoreState();
    }
    public boolean isBlacklistedField(FieldContext context) {
        return mBlacklist.matches(context);
    }
    public boolean isRecommendedField(FieldContext context) {
        return mRecommendedList.matches(context);
    }
    public void startListening(FieldContext context, boolean swipe) {
        mState = DEFAULT;
        Locale locale = Locale.getDefault();
        String localeString = locale.getLanguage() + "-" + locale.getCountry();
        mLogger.start(localeString, swipe);
        mState = LISTENING;
        mRecognitionView.showInitializing();
        startListeningAfterInitialization(context);
    }
    private void startListeningAfterInitialization(FieldContext context) {
        Intent intent = makeIntent();
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "");
        intent.putExtra(EXTRA_RECOGNITION_CONTEXT, context.getBundle());
        intent.putExtra(EXTRA_CALLING_PACKAGE, "VoiceIME");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,
                SettingsUtil.getSettingsInt(
                        mContext.getContentResolver(),
                        SettingsUtil.LATIN_IME_MAX_VOICE_RESULTS,
                        1));
        final ContentResolver cr = mContext.getContentResolver();
        putEndpointerExtra(
                cr,
                intent,
                SettingsUtil.LATIN_IME_SPEECH_MINIMUM_LENGTH_MILLIS,
                EXTRA_SPEECH_MINIMUM_LENGTH_MILLIS,
                null  );
        putEndpointerExtra(
                cr,
                intent,
                SettingsUtil.LATIN_IME_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                INPUT_COMPLETE_SILENCE_LENGTH_DEFAULT_VALUE_MILLIS
                );
        putEndpointerExtra(
                cr,
                intent,
                SettingsUtil.
                        LATIN_IME_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                null  );
        mSpeechRecognizer.startListening(intent);
    }
    private void putEndpointerExtra(ContentResolver cr, Intent i,
            String gservicesKey, String intentExtraKey, String defaultValue) {
        long l = -1;
        String s = SettingsUtil.getSettingsString(cr, gservicesKey, defaultValue);
        if (s != null) {
            try {
                l = Long.valueOf(s);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not parse value for " + gservicesKey + ": " + s);
            }
        }
        if (l != -1) i.putExtra(intentExtraKey, l);
    }
    public void destroy() {
        mSpeechRecognizer.destroy();
    }
    public void newView() {
        mRecognitionView = new RecognitionView(mContext, this);
    }
    public View getView() {
        return mRecognitionView.getView();
    }
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button:
                cancel();
                break;
        }
    }
    public void logTextModifiedByTypingInsertion(int length) {
        mLogger.textModifiedByTypingInsertion(length);
    }
    public void logTextModifiedByTypingInsertionPunctuation(int length) {
        mLogger.textModifiedByTypingInsertionPunctuation(length);
    }
    public void logTextModifiedByTypingDeletion(int length) {
        mLogger.textModifiedByTypingDeletion(length);
    }
    public void logTextModifiedByChooseSuggestion(int length) {
        mLogger.textModifiedByChooseSuggestion(length);
    }
    public void logKeyboardWarningDialogShown() {
        mLogger.keyboardWarningDialogShown();
    }
    public void logKeyboardWarningDialogDismissed() {
        mLogger.keyboardWarningDialogDismissed();
    }
    public void logKeyboardWarningDialogOk() {
        mLogger.keyboardWarningDialogOk();
    }
    public void logKeyboardWarningDialogCancel() {
        mLogger.keyboardWarningDialogCancel();
    }
    public void logSwipeHintDisplayed() {
        mLogger.swipeHintDisplayed();
    }
    public void logPunctuationHintDisplayed() {
        mLogger.punctuationHintDisplayed();
    }
    public void logVoiceInputDelivered(int length) {
        mLogger.voiceInputDelivered(length);
    }
    public void logNBestChoose(int index) {
        mLogger.nBestChoose(index);
    }
    public void logInputEnded() {
        mLogger.inputEnded();
    }
    public void flushLogs() {
        mLogger.flush();
    }
    private static Intent makeIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if (Build.VERSION.RELEASE.equals("1.5")) {
            intent = intent.setClassName(
              "com.google.android.voiceservice",
              "com.google.android.voiceservice.IMERecognitionService");
        } else {
            intent = intent.setClassName(
              "com.google.android.voicesearch",
              "com.google.android.voicesearch.RecognitionService");
        }
        return intent;
    }
    public void cancel() {
        switch (mState) {
        case LISTENING:
            mLogger.cancelDuringListening();
            break;
        case WORKING:
            mLogger.cancelDuringWorking();
            break;
        case ERROR:
            mLogger.cancelDuringError();
            break;
        }
        mState = DEFAULT;
        mHandler.removeMessages(MSG_CLOSE_ERROR_DIALOG);
        mSpeechRecognizer.cancel();
        mUiListener.onCancelVoice();
        mRecognitionView.finish();
    }
    private int getErrorStringId(int errorType, boolean endpointed) {
        switch (errorType) {
            case SpeechRecognizer.ERROR_CLIENT:
                return R.string.voice_not_installed;
            case SpeechRecognizer.ERROR_NETWORK:
                return R.string.voice_network_error;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return endpointed ?
                        R.string.voice_network_error : R.string.voice_too_much_speech;
            case SpeechRecognizer.ERROR_AUDIO:
                return R.string.voice_audio_error;
            case SpeechRecognizer.ERROR_SERVER:
                return R.string.voice_server_error;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return R.string.voice_speech_timeout;
            case SpeechRecognizer.ERROR_NO_MATCH:
                return R.string.voice_no_match;
            default: return R.string.voice_error;
        }
    }
    private void onError(int errorType, boolean endpointed) {
        Log.i(TAG, "error " + errorType);
        mLogger.error(errorType);
        onError(mContext.getString(getErrorStringId(errorType, endpointed)));
    }
    private void onError(String error) {
        mState = ERROR;
        mRecognitionView.showError(error);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_CLOSE_ERROR_DIALOG), 2000);
    }
    private class ImeRecognitionListener implements RecognitionListener {
        final ByteArrayOutputStream mWaveBuffer = new ByteArrayOutputStream();
        int mSpeechStart;
        private boolean mEndpointed = false;
        public void onReadyForSpeech(Bundle noiseParams) {
            mRecognitionView.showListening();
        }
        public void onBeginningOfSpeech() {
            mEndpointed = false;
            mSpeechStart = mWaveBuffer.size();
        }
        public void onRmsChanged(float rmsdB) {
            mRecognitionView.updateVoiceMeter(rmsdB);
        }
        public void onBufferReceived(byte[] buf) {
            try {
                mWaveBuffer.write(buf);
            } catch (IOException e) {}
        }
        public void onEndOfSpeech() {
            mEndpointed = true;
            mState = WORKING;
            mRecognitionView.showWorking(mWaveBuffer, mSpeechStart, mWaveBuffer.size());
        }
        public void onError(int errorType) {
            mState = ERROR;
            VoiceInput.this.onError(errorType, mEndpointed);
        }
        public void onResults(Bundle resultsBundle) {
            List<String> results = resultsBundle
                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            mState = DEFAULT;
            final Map<String, List<CharSequence>> alternatives =
                    new HashMap<String, List<CharSequence>>();
            if (results.size() >= 2 && ENABLE_WORD_CORRECTIONS) {
                final String[][] words = new String[results.size()][];
                for (int i = 0; i < words.length; i++) {
                    words[i] = results.get(i).split(" ");
                }
                for (int key = 0; key < words[0].length; key++) {
                    alternatives.put(words[0][key], new ArrayList<CharSequence>());
                    for (int alt = 1; alt < words.length; alt++) {
                        int keyBegin = key * words[alt].length / words[0].length;
                        int keyEnd = (key + 1) * words[alt].length / words[0].length;
                        for (int i = keyBegin; i < Math.min(words[alt].length, keyEnd); i++) {
                            List<CharSequence> altList = alternatives.get(words[0][key]);
                            if (!altList.contains(words[alt][i]) && altList.size() < 6) {
                                altList.add(words[alt][i]);
                            }
                        }
                    }
                }
            }
            if (results.size() > 5) {
                results = results.subList(0, 5);
            }
            mUiListener.onVoiceResults(results, alternatives);
            mRecognitionView.finish();
        }
        public void onPartialResults(final Bundle partialResults) {
        }
        public void onEvent(int eventType, Bundle params) {
        }
    }
}
