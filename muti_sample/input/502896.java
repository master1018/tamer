abstract public class RecognizerEngine {
    protected static final String TAG = "RecognizerEngine";
    protected static final String ACTION_RECOGNIZER_RESULT =
            "com.android.voicedialer.ACTION_RECOGNIZER_RESULT";
    public static final String SENTENCE_EXTRA = "sentence";
    public static final String SEMANTIC_EXTRA = "semantic";
    protected final String SREC_DIR = Recognizer.getConfigDir(null);
    protected static final String OPEN_ENTRIES = "openentries.txt";
    protected static final int RESULT_LIMIT = 5;
    protected Activity mActivity;
    protected Recognizer mSrec;
    protected Recognizer.Grammar mSrecGrammar;
    protected RecognizerLogger mLogger;
    protected int mSampleRate;
    public RecognizerEngine() {
        mSampleRate = 0;
    }
    abstract protected void setupGrammar() throws IOException, InterruptedException;
    abstract protected void onRecognitionSuccess(RecognizerClient recognizerClient)
            throws InterruptedException;
    public void recognize(RecognizerClient recognizerClient, Activity activity,
            File micFile, int sampleRate) {
        InputStream mic = null;
        boolean recognizerStarted = false;
        try {
            mActivity = activity;
            mLogger = null;
            if (RecognizerLogger.isEnabled(mActivity)) {
                mLogger = new RecognizerLogger(mActivity);
            }
            if (mSampleRate != sampleRate) {
                if (mSrecGrammar != null) {
                    mSrecGrammar.destroy();
                }
                mSrecGrammar = null;
                mSampleRate = sampleRate;
            }
            if (Config.LOGD) Log.d(TAG, "start new Recognizer");
            if (mSrec == null) {
                String parFilePath = SREC_DIR + "/baseline11k.par";
                if (sampleRate == 8000) {
                    parFilePath = SREC_DIR + "/baseline8k.par";
                }
                mSrec = new Recognizer(parFilePath);
            }
            if (micFile != null) {
                if (Config.LOGD) Log.d(TAG, "using mic file");
                mic = new FileInputStream(micFile);
                WaveHeader hdr = new WaveHeader();
                hdr.read(mic);
            } else {
                if (Config.LOGD) Log.d(TAG, "start new MicrophoneInputStream");
                mic = new MicrophoneInputStream(sampleRate, sampleRate * 15);
            }
            recognizerClient.onMicrophoneStart(mic);
            if (mLogger != null) mic = mLogger.logInputStream(mic, sampleRate);
            setupGrammar();
            if (Config.LOGD) Log.d(TAG, "start mSrec.start");
            mSrec.start();
            recognizerStarted = true;
            while (true) {
                if (Thread.interrupted()) throw new InterruptedException();
                int event = mSrec.advance();
                if (event != Recognizer.EVENT_INCOMPLETE &&
                        event != Recognizer.EVENT_NEED_MORE_AUDIO) {
                    Log.d(TAG, "start advance()=" +
                            Recognizer.eventToString(event) +
                            " avail " + mic.available());
                }
                switch (event) {
                case Recognizer.EVENT_INCOMPLETE:
                case Recognizer.EVENT_STARTED:
                case Recognizer.EVENT_START_OF_VOICING:
                case Recognizer.EVENT_END_OF_VOICING:
                    continue;
                case Recognizer.EVENT_RECOGNITION_RESULT:
                    onRecognitionSuccess(recognizerClient);
                    break;
                case Recognizer.EVENT_NEED_MORE_AUDIO:
                    mSrec.putAudio(mic);
                    continue;
                default:
                    Log.d(TAG, "unknown event " + event);
                    recognizerClient.onRecognitionFailure(Recognizer.eventToString(event));
                    break;
                }
                break;
            }
        } catch (InterruptedException e) {
            if (Config.LOGD) Log.d(TAG, "start interrupted " + e);
            recognizerClient.onRecognitionError(e.toString());
        } catch (IOException e) {
            if (Config.LOGD) Log.d(TAG, "start new Srec failed " + e);
            recognizerClient.onRecognitionError(e.toString());
        } catch (Exception e) {
            if (Config.LOGD) Log.d(TAG, "exception " + e);
            recognizerClient.onRecognitionError(e.toString());
        } finally {
            if (Config.LOGD) Log.d(TAG, "start mSrec.stop");
            if (mSrec != null && recognizerStarted) mSrec.stop();
            try {
                if (mic != null) mic.close();
            }
            catch (IOException ex) {
                if (Config.LOGD) Log.d(TAG, "start - mic.close failed - " + ex);
            }
            mic = null;
            try {
                if (mLogger != null) mLogger.close();
            }
            catch (IOException ex) {
                if (Config.LOGD) Log.d(TAG, "start - mLoggger.close failed - " + ex);
            }
            mLogger = null;
        }
        if (Config.LOGD) Log.d(TAG, "start bye");
    }
    protected static void addIntent(ArrayList<Intent> intents, Intent intent) {
        for (Intent in : intents) {
            if (in.getAction() != null &&
                    in.getAction().equals(intent.getAction()) &&
                    in.getData() != null &&
                    in.getData().equals(intent.getData())) {
                return;
            }
        }
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
        intents.add(intent);
    }
}
