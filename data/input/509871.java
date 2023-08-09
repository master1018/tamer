public class TextToSpeech {
    public static final int SUCCESS                = 0;
    public static final int ERROR                  = -1;
    public static final int QUEUE_FLUSH = 0;
    public static final int QUEUE_ADD = 1;
    public static final int LANG_COUNTRY_VAR_AVAILABLE = 2;
    public static final int LANG_COUNTRY_AVAILABLE = 1;
    public static final int LANG_AVAILABLE = 0;
    public static final int LANG_MISSING_DATA = -1;
    public static final int LANG_NOT_SUPPORTED = -2;
    @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
    public static final String ACTION_TTS_QUEUE_PROCESSING_COMPLETED =
            "android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED";
    public interface OnInitListener {
        public void onInit(int status);
    }
    public interface OnUtteranceCompletedListener {
        public void onUtteranceCompleted(String utteranceId);
    }
    public class Engine {
        public static final int DEFAULT_RATE = 100; 
        public static final int DEFAULT_PITCH = 100;
        public static final int USE_DEFAULTS = 0; 
        public static final String DEFAULT_SYNTH = "com.svox.pico";
        public static final int DEFAULT_STREAM = AudioManager.STREAM_MUSIC;
        public static final int CHECK_VOICE_DATA_PASS = 1;
        public static final int CHECK_VOICE_DATA_FAIL = 0;
        public static final int CHECK_VOICE_DATA_BAD_DATA = -1;
        public static final int CHECK_VOICE_DATA_MISSING_DATA = -2;
        public static final int CHECK_VOICE_DATA_MISSING_VOLUME = -3;
        @SdkConstant(SdkConstantType.ACTIVITY_INTENT_ACTION)
        public static final String ACTION_INSTALL_TTS_DATA =
                "android.speech.tts.engine.INSTALL_TTS_DATA";
        @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
        public static final String ACTION_TTS_DATA_INSTALLED =
                "android.speech.tts.engine.TTS_DATA_INSTALLED";
        @SdkConstant(SdkConstantType.ACTIVITY_INTENT_ACTION)
        public static final String ACTION_CHECK_TTS_DATA =
                "android.speech.tts.engine.CHECK_TTS_DATA";
        public static final String EXTRA_VOICE_DATA_ROOT_DIRECTORY = "dataRoot";
        public static final String EXTRA_VOICE_DATA_FILES = "dataFiles";
        public static final String EXTRA_VOICE_DATA_FILES_INFO = "dataFilesInfo";
        public static final String EXTRA_AVAILABLE_VOICES = "availableVoices";
        public static final String EXTRA_UNAVAILABLE_VOICES = "unavailableVoices";
        public static final String EXTRA_CHECK_VOICE_DATA_FOR = "checkVoiceDataFor";
        public static final String EXTRA_TTS_DATA_INSTALLED = "dataInstalled";
        public static final String KEY_PARAM_RATE = "rate";
        public static final String KEY_PARAM_LANGUAGE = "language";
        public static final String KEY_PARAM_COUNTRY = "country";
        public static final String KEY_PARAM_VARIANT = "variant";
        public static final String KEY_PARAM_ENGINE = "engine";
        public static final String KEY_PARAM_PITCH = "pitch";
        public static final String KEY_PARAM_STREAM = "streamType";
        public static final String KEY_PARAM_UTTERANCE_ID = "utteranceId";
        protected static final int PARAM_POSITION_RATE = 0;
        protected static final int PARAM_POSITION_LANGUAGE = 2;
        protected static final int PARAM_POSITION_COUNTRY = 4;
        protected static final int PARAM_POSITION_VARIANT = 6;
        protected static final int PARAM_POSITION_STREAM = 8;
        protected static final int PARAM_POSITION_UTTERANCE_ID = 10;
        protected static final int PARAM_POSITION_ENGINE = 12;
        protected static final int PARAM_POSITION_PITCH = 14;
        protected static final int NB_CACHED_PARAMS = 8;
    }
    private ServiceConnection mServiceConnection;
    private ITts mITts = null;
    private ITtsCallback mITtscallback = null;
    private Context mContext = null;
    private String mPackageName = "";
    private OnInitListener mInitListener = null;
    private boolean mStarted = false;
    private final Object mStartLock = new Object();
    private String[] mCachedParams;
    public TextToSpeech(Context context, OnInitListener listener) {
        mContext = context;
        mPackageName = mContext.getPackageName();
        mInitListener = listener;
        mCachedParams = new String[2*Engine.NB_CACHED_PARAMS]; 
        mCachedParams[Engine.PARAM_POSITION_RATE] = Engine.KEY_PARAM_RATE;
        mCachedParams[Engine.PARAM_POSITION_LANGUAGE] = Engine.KEY_PARAM_LANGUAGE;
        mCachedParams[Engine.PARAM_POSITION_COUNTRY] = Engine.KEY_PARAM_COUNTRY;
        mCachedParams[Engine.PARAM_POSITION_VARIANT] = Engine.KEY_PARAM_VARIANT;
        mCachedParams[Engine.PARAM_POSITION_STREAM] = Engine.KEY_PARAM_STREAM;
        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID] = Engine.KEY_PARAM_UTTERANCE_ID;
        mCachedParams[Engine.PARAM_POSITION_ENGINE] = Engine.KEY_PARAM_ENGINE;
        mCachedParams[Engine.PARAM_POSITION_PITCH] = Engine.KEY_PARAM_PITCH;
        mCachedParams[Engine.PARAM_POSITION_RATE + 1] = "";
        mCachedParams[Engine.PARAM_POSITION_LANGUAGE + 1] = "";
        mCachedParams[Engine.PARAM_POSITION_COUNTRY + 1] = "";
        mCachedParams[Engine.PARAM_POSITION_VARIANT + 1] = "";
        mCachedParams[Engine.PARAM_POSITION_STREAM + 1] =
                String.valueOf(Engine.DEFAULT_STREAM);
        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID + 1] = "";
        mCachedParams[Engine.PARAM_POSITION_ENGINE + 1] = "";
        mCachedParams[Engine.PARAM_POSITION_PITCH + 1] = "100";
        initTts();
    }
    private void initTts() {
        mStarted = false;
        mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized(mStartLock) {
                    mITts = ITts.Stub.asInterface(service);
                    mStarted = true;
                    setEngineByPackageName(getDefaultEngine());
                    setLanguage(getLanguage());
                    if (mInitListener != null) {
                        mInitListener.onInit(SUCCESS);
                    }
                }
            }
            public void onServiceDisconnected(ComponentName name) {
                synchronized(mStartLock) {
                    mITts = null;
                    mInitListener = null;
                    mStarted = false;
                }
            }
        };
        Intent intent = new Intent("android.intent.action.START_TTS_SERVICE");
        intent.addCategory("android.intent.category.TTS");
        mContext.bindService(intent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }
    public void shutdown() {
        try {
            mContext.unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
        }
    }
    public int addSpeech(String text, String packagename, int resourceId) {
        synchronized(mStartLock) {
            if (!mStarted) {
                return ERROR;
            }
            try {
                mITts.addSpeech(mPackageName, text, packagename, resourceId);
                return SUCCESS;
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - addSpeech", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - addSpeech", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - addSpeech", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return ERROR;
        }
    }
    public int addSpeech(String text, String filename) {
        synchronized (mStartLock) {
            if (!mStarted) {
                return ERROR;
            }
            try {
                mITts.addSpeechFile(mPackageName, text, filename);
                return SUCCESS;
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - addSpeech", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - addSpeech", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - addSpeech", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return ERROR;
        }
    }
    public int addEarcon(String earcon, String packagename, int resourceId) {
        synchronized(mStartLock) {
            if (!mStarted) {
                return ERROR;
            }
            try {
                mITts.addEarcon(mPackageName, earcon, packagename, resourceId);
                return SUCCESS;
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - addEarcon", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - addEarcon", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - addEarcon", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return ERROR;
        }
    }
    public int addEarcon(String earcon, String filename) {
        synchronized (mStartLock) {
            if (!mStarted) {
                return ERROR;
            }
            try {
                mITts.addEarconFile(mPackageName, earcon, filename);
                return SUCCESS;
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - addEarcon", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - addEarcon", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - addEarcon", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return ERROR;
        }
    }
    public int speak(String text, int queueMode, HashMap<String,String> params)
    {
        synchronized (mStartLock) {
            int result = ERROR;
            Log.i("TTS received: ", text);
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.KEY_PARAM_STREAM);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_STREAM + 1] = extra;
                    }
                    extra = params.get(Engine.KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                    extra = params.get(Engine.KEY_PARAM_ENGINE);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_ENGINE + 1] = extra;
                    }
                }
                result = mITts.speak(mPackageName, text, queueMode, mCachedParams);
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - speak", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - speak", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - speak", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                resetCachedParams();
                return result;
            }
        }
    }
    public int playEarcon(String earcon, int queueMode,
            HashMap<String,String> params) {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.KEY_PARAM_STREAM);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_STREAM + 1] = extra;
                    }
                    extra = params.get(Engine.KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                }
                result = mITts.playEarcon(mPackageName, earcon, queueMode, null);
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - playEarcon", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - playEarcon", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - playEarcon", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                resetCachedParams();
                return result;
            }
        }
    }
    public int playSilence(long durationInMs, int queueMode, HashMap<String,String> params) {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                }
                result = mITts.playSilence(mPackageName, durationInMs, queueMode, mCachedParams);
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - playSilence", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - playSilence", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - playSilence", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public boolean isSpeaking() {
        synchronized (mStartLock) {
            if (!mStarted) {
                return false;
            }
            try {
                return mITts.isSpeaking();
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - isSpeaking", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - isSpeaking", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - isSpeaking", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return false;
        }
    }
    public int stop() {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                result = mITts.stop(mPackageName);
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - stop", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - stop", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - stop", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public int setSpeechRate(float speechRate) {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if (speechRate > 0) {
                    int rate = (int)(speechRate*100);
                    mCachedParams[Engine.PARAM_POSITION_RATE + 1] = String.valueOf(rate);
                    if (speechRate > 0.0f) {
                        result = SUCCESS;
                    } else {
                        result = ERROR;
                    }
                }
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - setSpeechRate", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - setSpeechRate", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public int setPitch(float pitch) {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if (pitch > 0) {
                    int p = (int)(pitch*100);
                    mCachedParams[Engine.PARAM_POSITION_PITCH + 1] = String.valueOf(p);
                    result = SUCCESS;
                }
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - setPitch", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - setPitch", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public int setLanguage(Locale loc) {
        synchronized (mStartLock) {
            int result = LANG_NOT_SUPPORTED;
            if (!mStarted) {
                return result;
            }
            try {
                String language = loc.getISO3Language();
                String country = loc.getISO3Country();
                String variant = loc.getVariant();
                result = mITts.isLanguageAvailable(language, country, variant, mCachedParams);
                if (result >= LANG_AVAILABLE){
                    mCachedParams[Engine.PARAM_POSITION_LANGUAGE + 1] = language;
                    if (result >= LANG_COUNTRY_AVAILABLE){
                        mCachedParams[Engine.PARAM_POSITION_COUNTRY + 1] = country;
                    } else {
                        mCachedParams[Engine.PARAM_POSITION_COUNTRY + 1] = "";
                    }
                    if (result >= LANG_COUNTRY_VAR_AVAILABLE){
                        mCachedParams[Engine.PARAM_POSITION_VARIANT + 1] = variant;
                    } else {
                        mCachedParams[Engine.PARAM_POSITION_VARIANT + 1] = "";
                    }
                }
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - setLanguage", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - setLanguage", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - setLanguage", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public Locale getLanguage() {
        synchronized (mStartLock) {
            if (!mStarted) {
                return null;
            }
            try {
                if (mCachedParams[Engine.PARAM_POSITION_LANGUAGE + 1].length() < 1){
                    String[] locStrings = mITts.getLanguage();
                    if ((locStrings != null) && (locStrings.length == 3)) {
                        return new Locale(locStrings[0], locStrings[1], locStrings[2]);
                    } else {
                        return null;
                    }
                } else {
                    return new Locale(mCachedParams[Engine.PARAM_POSITION_LANGUAGE + 1],
                            mCachedParams[Engine.PARAM_POSITION_COUNTRY + 1],
                            mCachedParams[Engine.PARAM_POSITION_VARIANT + 1]);
                }
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - getLanguage", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - getLanguage", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - getLanguage", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            }
            return null;
        }
    }
    public int isLanguageAvailable(Locale loc) {
        synchronized (mStartLock) {
            int result = LANG_NOT_SUPPORTED;
            if (!mStarted) {
                return result;
            }
            try {
                result = mITts.isLanguageAvailable(loc.getISO3Language(),
                        loc.getISO3Country(), loc.getVariant(), mCachedParams);
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - isLanguageAvailable", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - isLanguageAvailable", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - isLanguageAvailable", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public int synthesizeToFile(String text, HashMap<String,String> params,
            String filename) {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                if ((params != null) && (!params.isEmpty())) {
                    String extra = params.get(Engine.KEY_PARAM_UTTERANCE_ID);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID + 1] = extra;
                    }
                    extra = params.get(Engine.KEY_PARAM_ENGINE);
                    if (extra != null) {
                        mCachedParams[Engine.PARAM_POSITION_ENGINE + 1] = extra;
                    }
                }
                result = mITts.synthesizeToFile(mPackageName, text, mCachedParams, filename) ?
                        SUCCESS : ERROR;
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - synthesizeToFile", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - synthesizeToFile", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - synthesizeToFile", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                resetCachedParams();
                return result;
            }
        }
    }
    private void resetCachedParams() {
        mCachedParams[Engine.PARAM_POSITION_STREAM + 1] =
                String.valueOf(Engine.DEFAULT_STREAM);
        mCachedParams[Engine.PARAM_POSITION_UTTERANCE_ID+ 1] = "";
    }
    public int setOnUtteranceCompletedListener(
            final OnUtteranceCompletedListener listener) {
        synchronized (mStartLock) {
            int result = ERROR;
            if (!mStarted) {
                return result;
            }
            mITtscallback = new ITtsCallback.Stub() {
                public void utteranceCompleted(String utteranceId) throws RemoteException {
                    if (listener != null) {
                        listener.onUtteranceCompleted(utteranceId);
                    }
                }
            };
            try {
                result = mITts.registerCallback(mPackageName, mITtscallback);
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - registerCallback", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - registerCallback", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - registerCallback", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public int setEngineByPackageName(String enginePackageName) {
        synchronized (mStartLock) {
            int result = TextToSpeech.ERROR;
            if (!mStarted) {
                return result;
            }
            try {
                result = mITts.setEngineByPackageName(enginePackageName);
                if (result == TextToSpeech.SUCCESS){
                    mCachedParams[Engine.PARAM_POSITION_ENGINE + 1] = enginePackageName;
                }
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - setEngineByPackageName", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - setEngineByPackageName", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - setEngineByPackageName", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return result;
            }
        }
    }
    public String getDefaultEngine() {
        synchronized (mStartLock) {
            String engineName = "";
            if (!mStarted) {
                return engineName;
            }
            try {
                engineName = mITts.getDefaultEngine();
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - setEngineByPackageName", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - setEngineByPackageName", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - setEngineByPackageName", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return engineName;
            }
        }
    }
    public boolean areDefaultsEnforced() {
        synchronized (mStartLock) {
            boolean defaultsEnforced = false;
            if (!mStarted) {
                return defaultsEnforced;
            }
            try {
                defaultsEnforced = mITts.areDefaultsEnforced();
            } catch (RemoteException e) {
                Log.e("TextToSpeech.java - areDefaultsEnforced", "RemoteException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (NullPointerException e) {
                Log.e("TextToSpeech.java - areDefaultsEnforced", "NullPointerException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } catch (IllegalStateException e) {
                Log.e("TextToSpeech.java - areDefaultsEnforced", "IllegalStateException");
                e.printStackTrace();
                mStarted = false;
                initTts();
            } finally {
                return defaultsEnforced;
            }
        }
    }
}
