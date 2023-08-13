@TestTargetClass(TextToSpeech.class)
public class TextToSpeechTest extends AndroidTestCase {
    private TextToSpeech mTts;
    private static final String UTTERANCE = "utterance";
    private static final String SAMPLE_TEXT = "This is a sample text to speech string";
    private static final String SAMPLE_FILE_NAME = "mytts.wav";
    private static final int TTS_INIT_MAX_WAIT_TIME = 30 * 1000;
    private static final int TTS_SPEECH_MAX_WAIT_TIME = 5 * 1000;
    private static final String LOG_TAG = "TextToSpeechTest";
    private static class InitWaitListener implements OnInitListener {
        private int mStatus = TextToSpeech.ERROR;
        public void onInit(int status) {
            mStatus = status;
            synchronized(this) {
                notify();
            }
        }
        public boolean waitForInit() throws InterruptedException {
            if (mStatus == TextToSpeech.SUCCESS) {
                return true;
            }
            synchronized (this) {
                wait(TTS_INIT_MAX_WAIT_TIME);
            }
            return mStatus == TextToSpeech.SUCCESS;
        }
    }
    private static class UtteranceWaitListener implements OnUtteranceCompletedListener {
        private boolean mIsComplete = false;
        private final String mExpectedUtterance;
        public UtteranceWaitListener(String expectedUtteranceId) {
            mExpectedUtterance = expectedUtteranceId;
        }
        public void onUtteranceCompleted(String utteranceId) {
            if (mExpectedUtterance.equals(utteranceId)) {
                synchronized(this) {
                    mIsComplete = true;
                    notify();
                }
            }
        }
        public boolean waitForComplete() throws InterruptedException {
            if (mIsComplete) {
                return true;
            }
            synchronized (this) {
                wait(TTS_SPEECH_MAX_WAIT_TIME);
                return mIsComplete;
            }
        }
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        InitWaitListener listener = new InitWaitListener();
        mTts = new TextToSpeech(getContext(), listener);
        assertTrue(listener.waitForInit());
        assertTrue(checkAndSetLanguageAvailable());
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mTts.shutdown();
    }
    private boolean  checkAndSetLanguageAvailable() {
        for (Locale locale : Locale.getAvailableLocales()) {
            int availability = mTts.isLanguageAvailable(locale);
            if (availability == TextToSpeech.LANG_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                mTts.setLanguage(locale);
                return true;
            }
        }
        return false;
    }
    public void testSynthesizeToFile() throws Exception {
        File sampleFile = new File(Environment.getExternalStorageDirectory(), SAMPLE_FILE_NAME);
        try {
            assertFalse(sampleFile.exists());
            HashMap<String, String> param = new HashMap<String,String>();
            param.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE);
            UtteranceWaitListener listener = new UtteranceWaitListener(UTTERANCE);
            mTts.setOnUtteranceCompletedListener(listener);
            int result = mTts.synthesizeToFile(SAMPLE_TEXT, param, sampleFile.getPath());
            assertEquals(TextToSpeech.SUCCESS, result);
            assertTrue(listener.waitForComplete());
            assertTrue(sampleFile.exists());
            assertTrue(isMusicFile(sampleFile.getPath()));
        } finally {
            deleteFile(sampleFile);
        }
    }
    private boolean isMusicFile(String filePath) {
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
            mp.stop();
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception while attempting to play music file", e);
            return false;
        }
    }
    private void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
