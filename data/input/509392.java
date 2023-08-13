public final class Recognizer {
    static {
        System.loadLibrary("srec_jni");
    }
    private static String TAG = "Recognizer";
    public static final String KEY_CONFIDENCE = "conf";
    public static final String KEY_LITERAL = "literal";
    public static final String KEY_MEANING = "meaning";
    private int mVocabulary = 0;
    private int mRecognizer = 0;
    private Grammar mActiveGrammar = null;
    public static String getConfigDir(Locale locale) {
        if (locale == null) locale = Locale.US;
        String dir = "/system/usr/srec/config/" +
                locale.toString().replace('_', '.').toLowerCase();
        if ((new File(dir)).isDirectory()) return dir;
        return null;
    }
    public Recognizer(String configFile) throws IOException {
        PMemInit();
        SR_SessionCreate(configFile);
        mRecognizer = SR_RecognizerCreate();
        SR_RecognizerSetup(mRecognizer);
        mVocabulary = SR_VocabularyLoad();
    }
    public class Grammar {
        private int mGrammar = 0;
        public Grammar(String g2gFileName) throws IOException {
            mGrammar = SR_GrammarLoad(g2gFileName);
            SR_GrammarSetupVocabulary(mGrammar, mVocabulary);
        }
        public void resetAllSlots() {
            SR_GrammarResetAllSlots(mGrammar);
        }
        public void addWordToSlot(String slot, String word, String pron, int weight, String tag) {
            SR_GrammarAddWordToSlot(mGrammar, slot, word, pron, weight, tag); 
        }
        public void compile() {
            SR_GrammarCompile(mGrammar);
        }
        public void setupRecognizer() {
            SR_GrammarSetupRecognizer(mGrammar, mRecognizer);
            mActiveGrammar = this;
        }
        public void save(String g2gFileName) throws IOException {
            SR_GrammarSave(mGrammar, g2gFileName);
        }
        public void destroy() {
            if (mGrammar != 0) {
                SR_GrammarDestroy(mGrammar);
                mGrammar = 0;
            }
        }
        protected void finalize() {
            if (mGrammar != 0) {
                destroy();
                throw new IllegalStateException("someone forgot to destroy Grammar");
            }
        }
    }
    public void start() {
        SR_RecognizerActivateRule(mRecognizer, mActiveGrammar.mGrammar, "trash", 1);
        SR_RecognizerStart(mRecognizer);
    }
    public int advance() {
        return SR_RecognizerAdvance(mRecognizer);
    }
    public int putAudio(byte[] buf, int offset, int length, boolean isLast) {
        return SR_RecognizerPutAudio(mRecognizer, buf, offset, length, isLast);
    }
    public void putAudio(InputStream audio) throws IOException {
        if (mPutAudioBuffer == null) mPutAudioBuffer = new byte[512];
        int nbytes = audio.read(mPutAudioBuffer);
        if (nbytes == -1) {
            SR_RecognizerPutAudio(mRecognizer, mPutAudioBuffer, 0, 0, true);
        }
        else if (nbytes != SR_RecognizerPutAudio(mRecognizer, mPutAudioBuffer, 0, nbytes, false)) {
            throw new IOException("SR_RecognizerPutAudio failed nbytes=" + nbytes);
        }
    }
    private byte[] mPutAudioBuffer = null;
    public int getResultCount() {
        return SR_RecognizerResultGetSize(mRecognizer);
    }
    public String[] getResultKeys(int index) {
        return SR_RecognizerResultGetKeyList(mRecognizer, index);
    }
    public String getResult(int index, String key) {
        return SR_RecognizerResultGetValue(mRecognizer, index, key);
    }
    public void stop() {
        SR_RecognizerStop(mRecognizer);
        SR_RecognizerDeactivateRule(mRecognizer, mActiveGrammar.mGrammar, "trash");
    }
    public void resetAcousticState() {
        SR_AcousticStateReset(mRecognizer);
    }
    public void setAcousticState(String state) {
        SR_AcousticStateSet(mRecognizer, state);
    }
    public String getAcousticState() {
        return SR_AcousticStateGet(mRecognizer);
    }
    public void destroy() {
        try {
            if (mVocabulary != 0) SR_VocabularyDestroy(mVocabulary);
        } finally {
            mVocabulary = 0;
            try {
                if (mRecognizer != 0) SR_RecognizerUnsetup(mRecognizer);
            } finally {
                try {
                    if (mRecognizer != 0) SR_RecognizerDestroy(mRecognizer);
                } finally {
                    mRecognizer = 0;
                    try {
                        SR_SessionDestroy();
                    } finally {
                        PMemShutdown();
                    }
                }
            }
        }
    }
    protected void finalize() throws Throwable {
        if (mVocabulary != 0 || mRecognizer != 0) {
            destroy();
            throw new IllegalStateException("someone forgot to destroy Recognizer");
        }
    }
    private static native void PMemInit();
    private static native void PMemShutdown();
    private static native void SR_SessionCreate(String filename);
    private static native void SR_SessionDestroy();
    public final static int EVENT_INVALID = 0;
    public final static int EVENT_NO_MATCH = 1;
    public final static int EVENT_INCOMPLETE = 2;
    public final static int EVENT_STARTED = 3;
    public final static int EVENT_STOPPED = 4;
    public final static int EVENT_START_OF_VOICING = 5;
    public final static int EVENT_END_OF_VOICING = 6;
    public final static int EVENT_SPOKE_TOO_SOON = 7;
    public final static int EVENT_RECOGNITION_RESULT = 8;
    public final static int EVENT_START_OF_UTTERANCE_TIMEOUT = 9;
    public final static int EVENT_RECOGNITION_TIMEOUT = 10;
    public final static int EVENT_NEED_MORE_AUDIO = 11;
    public final static int EVENT_MAX_SPEECH = 12;
    public static String eventToString(int event) {
        switch (event) {
            case EVENT_INVALID:
                return "EVENT_INVALID";
            case EVENT_NO_MATCH:
                return "EVENT_NO_MATCH";
            case EVENT_INCOMPLETE:
                return "EVENT_INCOMPLETE";
            case EVENT_STARTED:
                return "EVENT_STARTED";
            case EVENT_STOPPED:
                return "EVENT_STOPPED";
            case EVENT_START_OF_VOICING:
                return "EVENT_START_OF_VOICING";
            case EVENT_END_OF_VOICING:
                return "EVENT_END_OF_VOICING";
            case EVENT_SPOKE_TOO_SOON:
                return "EVENT_SPOKE_TOO_SOON";
            case EVENT_RECOGNITION_RESULT:
                return "EVENT_RECOGNITION_RESULT";
            case EVENT_START_OF_UTTERANCE_TIMEOUT:
                return "EVENT_START_OF_UTTERANCE_TIMEOUT";
            case EVENT_RECOGNITION_TIMEOUT:
                return "EVENT_RECOGNITION_TIMEOUT";
            case EVENT_NEED_MORE_AUDIO:
                return "EVENT_NEED_MORE_AUDIO";
            case EVENT_MAX_SPEECH:
                return "EVENT_MAX_SPEECH";
        }
        return "EVENT_" + event;
    }
    private static native void SR_RecognizerStart(int recognizer);
    private static native void SR_RecognizerStop(int recognizer);
    private static native int SR_RecognizerCreate();
    private static native void SR_RecognizerDestroy(int recognizer);
    private static native void SR_RecognizerSetup(int recognizer);
    private static native void SR_RecognizerUnsetup(int recognizer);
    private static native boolean SR_RecognizerIsSetup(int recognizer);
    private static native String SR_RecognizerGetParameter(int recognizer, String key);
    private static native int SR_RecognizerGetSize_tParameter(int recognizer, String key);
    private static native boolean SR_RecognizerGetBoolParameter(int recognizer, String key);
    private static native void SR_RecognizerSetParameter(int recognizer, String key, String value);
    private static native void SR_RecognizerSetSize_tParameter(int recognizer,
            String key, int value);
    private static native void SR_RecognizerSetBoolParameter(int recognizer, String key,
            boolean value);
    private static native void SR_RecognizerSetupRule(int recognizer, int grammar,
            String ruleName);
    private static native boolean SR_RecognizerHasSetupRules(int recognizer);
    private static native void SR_RecognizerActivateRule(int recognizer, int grammar,
            String ruleName, int weight);
    private static native void SR_RecognizerDeactivateRule(int recognizer, int grammar,
            String ruleName);
    private static native void SR_RecognizerDeactivateAllRules(int recognizer);
    private static native boolean SR_RecognizerIsActiveRule(int recognizer, int grammar,
            String ruleName);
    private static native boolean SR_RecognizerCheckGrammarConsistency(int recognizer,
            int grammar);
    private static native int SR_RecognizerPutAudio(int recognizer, byte[] buffer, int offset,
            int length, boolean isLast);
    private static native int SR_RecognizerAdvance(int recognizer);
    private static native boolean SR_RecognizerIsSignalClipping(int recognizer);
    private static native boolean SR_RecognizerIsSignalDCOffset(int recognizer);
    private static native boolean SR_RecognizerIsSignalNoisy(int recognizer);
    private static native boolean SR_RecognizerIsSignalTooQuiet(int recognizer);
    private static native boolean SR_RecognizerIsSignalTooFewSamples(int recognizer);
    private static native boolean SR_RecognizerIsSignalTooManySamples(int recognizer);
    private static native void SR_AcousticStateReset(int recognizer);
    private static native void SR_AcousticStateSet(int recognizer, String state);
    private static native String SR_AcousticStateGet(int recognizer);
    private static native void SR_GrammarCompile(int grammar);
    private static native void SR_GrammarAddWordToSlot(int grammar, String slot,
            String word, String pronunciation, int weight, String tag);
    private static native void SR_GrammarResetAllSlots(int grammar);
    private static native void SR_GrammarSetupVocabulary(int grammar, int vocabulary);
    private static native void SR_GrammarSetupRecognizer(int grammar, int recognizer);
    private static native void SR_GrammarUnsetupRecognizer(int grammar);
    private static native int SR_GrammarCreate();
    private static native void SR_GrammarDestroy(int grammar);
    private static native int SR_GrammarLoad(String filename);
    private static native void SR_GrammarSave(int grammar, String filename);
    private static native void SR_GrammarAllowOnly(int grammar, String transcription);
    private static native void SR_GrammarAllowAll(int grammar);
    private static native int SR_VocabularyLoad();
    private static native void SR_VocabularyDestroy(int vocabulary);
    private static native String SR_VocabularyGetPronunciation(int vocabulary, String word);
    private static native byte[] SR_RecognizerResultGetWaveform(int recognizer);
    private static native int SR_RecognizerResultGetSize(int recognizer);
    private static native int SR_RecognizerResultGetKeyCount(int recognizer, int nbest);
    private static native String[] SR_RecognizerResultGetKeyList(int recognizer, int nbest);
    private static native String SR_RecognizerResultGetValue(int recognizer,
            int nbest, String key);
}
