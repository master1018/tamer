public class TtsService extends Service implements OnCompletionListener {
    private static class SpeechItem {
        public static final int TEXT = 0;
        public static final int EARCON = 1;
        public static final int SILENCE = 2;
        public static final int TEXT_TO_FILE = 3;
        public String mText = "";
        public ArrayList<String> mParams = null;
        public int mType = TEXT;
        public long mDuration = 0;
        public String mFilename = null;
        public String mCallingApp = "";
        public SpeechItem(String source, String text, ArrayList<String> params, int itemType) {
            mText = text;
            mParams = params;
            mType = itemType;
            mCallingApp = source;
        }
        public SpeechItem(String source, long silenceTime, ArrayList<String> params) {
            mDuration = silenceTime;
            mParams = params;
            mType = SILENCE;
            mCallingApp = source;
        }
        public SpeechItem(String source, String text, ArrayList<String> params,
                int itemType, String filename) {
            mText = text;
            mParams = params;
            mType = itemType;
            mFilename = filename;
            mCallingApp = source;
        }
    }
    private static class SoundResource {
        public String mSourcePackageName = null;
        public int mResId = -1;
        public String mFilename = null;
        public SoundResource(String packageName, int id) {
            mSourcePackageName = packageName;
            mResId = id;
            mFilename = null;
        }
        public SoundResource(String file) {
            mSourcePackageName = null;
            mResId = -1;
            mFilename = file;
        }
    }
    private static final int SPEECHQUEUELOCK_TIMEOUT = 5000;
    private static final int MAX_SPEECH_ITEM_CHAR_LENGTH = 4000;
    private static final int MAX_FILENAME_LENGTH = 250;
    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private static final String DEFAULT_SYNTH = "com.svox.pico";
    private static final String ACTION = "android.intent.action.START_TTS_SERVICE";
    private static final String CATEGORY = "android.intent.category.TTS";
    private static final String PKGNAME = "android.tts";
    protected static final String SERVICE_TAG = "TtsService";
    private final RemoteCallbackList<ITtsCallback> mCallbacks
            = new RemoteCallbackList<ITtsCallback>();
    private HashMap<String, ITtsCallback> mCallbacksMap;
    private Boolean mIsSpeaking;
    private Boolean mSynthBusy;
    private ArrayList<SpeechItem> mSpeechQueue;
    private HashMap<String, SoundResource> mEarcons;
    private HashMap<String, SoundResource> mUtterances;
    private MediaPlayer mPlayer;
    private SpeechItem mCurrentSpeechItem;
    private HashMap<SpeechItem, Boolean> mKillList; 
    private TtsService mSelf;
    private ContentResolver mResolver;
    private final ReentrantLock speechQueueLock = new ReentrantLock();
    private final ReentrantLock synthesizerLock = new ReentrantLock();
    private static SynthProxy sNativeSynth = null;
    private String currentSpeechEngineSOFile = "";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("TtsService", "TtsService.onCreate()");
        mResolver = getContentResolver();
        currentSpeechEngineSOFile = "";
        setEngine(getDefaultEngine());
        mSelf = this;
        mIsSpeaking = false;
        mSynthBusy = false;
        mEarcons = new HashMap<String, SoundResource>();
        mUtterances = new HashMap<String, SoundResource>();
        mCallbacksMap = new HashMap<String, android.speech.tts.ITtsCallback>();
        mSpeechQueue = new ArrayList<SpeechItem>();
        mPlayer = null;
        mCurrentSpeechItem = null;
        mKillList = new HashMap<SpeechItem, Boolean>();
        setDefaultSettings();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        killAllUtterances();
        cleanUpPlayer();
        if (sNativeSynth != null) {
            sNativeSynth.shutdown();
        }
        sNativeSynth = null;
        mCallbacks.kill();
        Log.v(SERVICE_TAG, "onDestroy() completed");
    }
    private int setEngine(String enginePackageName) {
        String soFilename = "";
        if (isDefaultEnforced()) {
            enginePackageName = getDefaultEngine();
        }
        if (!enginePackageName.equals(DEFAULT_SYNTH)) {
            String[] enabledEngines = android.provider.Settings.Secure.getString(mResolver,
                    android.provider.Settings.Secure.TTS_ENABLED_PLUGINS).split(" ");
            boolean isEnabled = false;
            for (int i=0; i<enabledEngines.length; i++) {
                if (enabledEngines[i].equals(enginePackageName)) {
                    isEnabled = true;
                    break;
                }
            }
            if (!isEnabled) {
                enginePackageName = DEFAULT_SYNTH;
            }
        }
        if (enginePackageName.equals(DEFAULT_SYNTH)) {
            soFilename = "/system/lib/libttspico.so";
        } else {
            Intent intent = new Intent("android.intent.action.START_TTS_ENGINE");
            intent.setPackage(enginePackageName);
            ResolveInfo[] enginesArray = new ResolveInfo[0];
            PackageManager pm = getPackageManager();
            List <ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
            if ((resolveInfos == null) || resolveInfos.isEmpty()) {
                Log.e(SERVICE_TAG, "Invalid TTS Engine Package: " + enginePackageName);
                return TextToSpeech.ERROR;
            }
            enginesArray = resolveInfos.toArray(enginesArray);
            ActivityInfo aInfo = enginesArray[0].activityInfo;
            soFilename = aInfo.name.replace(aInfo.packageName + ".", "") + ".so";
            soFilename = soFilename.toLowerCase();
            soFilename = "/data/data/" + aInfo.packageName + "/lib/libtts" + soFilename;
        }
        if (currentSpeechEngineSOFile.equals(soFilename)) {
            return TextToSpeech.SUCCESS;
        }
        File f = new File(soFilename);
        if (!f.exists()) {
            Log.e(SERVICE_TAG, "Invalid TTS Binary: " + soFilename);
            return TextToSpeech.ERROR;
        }
        if (sNativeSynth != null) {
            sNativeSynth.stopSync();
            sNativeSynth.shutdown();
            sNativeSynth = null;
        }
        String engineConfig = "";
        Cursor c = getContentResolver().query(Uri.parse("content:
                + ".providers.SettingsProvider"), null, null, null, null);
        if (c != null){
            c.moveToFirst();
            engineConfig = c.getString(0);
            c.close();
        }
        sNativeSynth = new SynthProxy(soFilename, engineConfig);
        currentSpeechEngineSOFile = soFilename;
        return TextToSpeech.SUCCESS;
    }
    private void setDefaultSettings() {
        setLanguage("", this.getDefaultLanguage(), getDefaultCountry(), getDefaultLocVariant());
        setSpeechRate("", getDefaultRate());
    }
    private boolean isDefaultEnforced() {
        return (android.provider.Settings.Secure.getInt(mResolver,
                    android.provider.Settings.Secure.TTS_USE_DEFAULTS,
                    TextToSpeech.Engine.USE_DEFAULTS)
                == 1 );
    }
    private String getDefaultEngine() {
        String defaultEngine = android.provider.Settings.Secure.getString(mResolver,
                android.provider.Settings.Secure.TTS_DEFAULT_SYNTH);
        if (defaultEngine == null) {
            return TextToSpeech.Engine.DEFAULT_SYNTH;
        } else {
            return defaultEngine;
        }
    }
    private int getDefaultRate() {
        return android.provider.Settings.Secure.getInt(mResolver,
                android.provider.Settings.Secure.TTS_DEFAULT_RATE,
                TextToSpeech.Engine.DEFAULT_RATE);
    }
    private int getDefaultPitch() {
        return 100;
    }
    private String getDefaultLanguage() {
        String defaultLang = android.provider.Settings.Secure.getString(mResolver,
                android.provider.Settings.Secure.TTS_DEFAULT_LANG);
        if (defaultLang == null) {
            return Locale.getDefault().getISO3Language();
        } else {
            return defaultLang;
        }
    }
    private String getDefaultCountry() {
        String defaultCountry = android.provider.Settings.Secure.getString(mResolver,
                android.provider.Settings.Secure.TTS_DEFAULT_COUNTRY);
        if (defaultCountry == null) {
            return Locale.getDefault().getISO3Country();
        } else {
            return defaultCountry;
        }
    }
    private String getDefaultLocVariant() {
        String defaultVar = android.provider.Settings.Secure.getString(mResolver,
                android.provider.Settings.Secure.TTS_DEFAULT_VARIANT);
        if (defaultVar == null) {
            return Locale.getDefault().getVariant();
        } else {
            return defaultVar;
        }
    }
    private int setSpeechRate(String callingApp, int rate) {
        int res = TextToSpeech.ERROR;
        try {
            if (isDefaultEnforced()) {
                res = sNativeSynth.setSpeechRate(getDefaultRate());
            } else {
                res = sNativeSynth.setSpeechRate(rate);
            }
        } catch (NullPointerException e) {
            res = TextToSpeech.ERROR;
        }
        return res;
    }
    private int setPitch(String callingApp, int pitch) {
        int res = TextToSpeech.ERROR;
        try {
            res = sNativeSynth.setPitch(pitch);
        } catch (NullPointerException e) {
            res = TextToSpeech.ERROR;
        }
        return res;
    }
    private int isLanguageAvailable(String lang, String country, String variant) {
        int res = TextToSpeech.LANG_NOT_SUPPORTED;
        try {
            res = sNativeSynth.isLanguageAvailable(lang, country, variant);
        } catch (NullPointerException e) {
            res = TextToSpeech.LANG_NOT_SUPPORTED;
        }
        return res;
    }
    private String[] getLanguage() {
        try {
            return sNativeSynth.getLanguage();
        } catch (Exception e) {
            return null;
        }
    }
    private int setLanguage(String callingApp, String lang, String country, String variant) {
        Log.v(SERVICE_TAG, "TtsService.setLanguage(" + lang + ", " + country + ", " + variant + ")");
        int res = TextToSpeech.ERROR;
        try {
            if (isDefaultEnforced()) {
                res = sNativeSynth.setLanguage(getDefaultLanguage(), getDefaultCountry(),
                        getDefaultLocVariant());
            } else {
                res = sNativeSynth.setLanguage(lang, country, variant);
            }
        } catch (NullPointerException e) {
            res = TextToSpeech.ERROR;
        }
        return res;
    }
    private void addSpeech(String callingApp, String text, String packageName, int resId) {
        mUtterances.put(text, new SoundResource(packageName, resId));
    }
    private void addSpeech(String callingApp, String text, String filename) {
        mUtterances.put(text, new SoundResource(filename));
    }
    private void addEarcon(String callingApp, String earcon, String packageName, int resId) {
        mEarcons.put(earcon, new SoundResource(packageName, resId));
    }
    private void addEarcon(String callingApp, String earcon, String filename) {
        mEarcons.put(earcon, new SoundResource(filename));
    }
    private int speak(String callingApp, String text, int queueMode, ArrayList<String> params) {
        Log.v(SERVICE_TAG, "TTS service received " + text);
        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            stop(callingApp);
        } else if (queueMode == 2) {
            stopAll(callingApp);
        }
        mSpeechQueue.add(new SpeechItem(callingApp, text, params, SpeechItem.TEXT));
        if (!mIsSpeaking) {
            processSpeechQueue();
        }
        return TextToSpeech.SUCCESS;
    }
    private int playEarcon(String callingApp, String earcon, int queueMode,
            ArrayList<String> params) {
        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            stop(callingApp);
        } else if (queueMode == 2) {
            stopAll(callingApp);
        }
        mSpeechQueue.add(new SpeechItem(callingApp, earcon, params, SpeechItem.EARCON));
        if (!mIsSpeaking) {
            processSpeechQueue();
        }
        return TextToSpeech.SUCCESS;
    }
    private int stop(String callingApp) {
        int result = TextToSpeech.ERROR;
        boolean speechQueueAvailable = false;
        try{
            speechQueueAvailable =
                    speechQueueLock.tryLock(SPEECHQUEUELOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            if (speechQueueAvailable) {
                Log.i(SERVICE_TAG, "Stopping");
                for (int i = mSpeechQueue.size() - 1; i > -1; i--){
                    if (mSpeechQueue.get(i).mCallingApp.equals(callingApp)){
                        mSpeechQueue.remove(i);
                    }
                }
                if ((mCurrentSpeechItem != null) &&
                     mCurrentSpeechItem.mCallingApp.equals(callingApp)) {
                    try {
                        result = sNativeSynth.stop();
                    } catch (NullPointerException e1) {
                        result = TextToSpeech.ERROR;
                    }
                    mKillList.put(mCurrentSpeechItem, true);
                    if (mPlayer != null) {
                        try {
                            mPlayer.stop();
                        } catch (IllegalStateException e) {
                        }
                    }
                    mIsSpeaking = false;
                    mCurrentSpeechItem = null;
                } else {
                    result = TextToSpeech.SUCCESS;
                }
                Log.i(SERVICE_TAG, "Stopped");
            } else {
                Log.e(SERVICE_TAG, "TTS stop(): queue locked longer than expected");
                result = TextToSpeech.ERROR;
            }
        } catch (InterruptedException e) {
          Log.e(SERVICE_TAG, "TTS stop: tryLock interrupted");
          e.printStackTrace();
        } finally {
            if (speechQueueAvailable) {
                speechQueueLock.unlock();
            }
            return result;
        }
    }
    @SuppressWarnings("finally")
    private int killAllUtterances() {
        int result = TextToSpeech.ERROR;
        boolean speechQueueAvailable = false;
        try {
            speechQueueAvailable = speechQueueLock.tryLock(SPEECHQUEUELOCK_TIMEOUT,
                    TimeUnit.MILLISECONDS);
            if (speechQueueAvailable) {
                mSpeechQueue.clear();
                if (mCurrentSpeechItem != null) {
                    result = sNativeSynth.stopSync();
                    mKillList.put(mCurrentSpeechItem, true);
                    mIsSpeaking = false;
                    if (mCurrentSpeechItem.mType == SpeechItem.TEXT_TO_FILE) {
                        if (mCurrentSpeechItem.mFilename != null) {
                            File tempFile = new File(mCurrentSpeechItem.mFilename);
                            Log.v(SERVICE_TAG, "Leaving behind " + mCurrentSpeechItem.mFilename);
                            if (tempFile.exists()) {
                                Log.v(SERVICE_TAG, "About to delete "
                                        + mCurrentSpeechItem.mFilename);
                                if (tempFile.delete()) {
                                    Log.v(SERVICE_TAG, "file successfully deleted");
                                }
                            }
                        }
                    }
                    mCurrentSpeechItem = null;
                }
            } else {
                Log.e(SERVICE_TAG, "TTS killAllUtterances(): queue locked longer than expected");
                result = TextToSpeech.ERROR;
            }
        } catch (InterruptedException e) {
            Log.e(SERVICE_TAG, "TTS killAllUtterances(): tryLock interrupted");
            result = TextToSpeech.ERROR;
        } finally {
            if (speechQueueAvailable) {
                speechQueueLock.unlock();
            }
            return result;
        }
    }
    private int stopAll(String callingApp) {
        int result = TextToSpeech.ERROR;
        boolean speechQueueAvailable = false;
        try{
            speechQueueAvailable =
                    speechQueueLock.tryLock(SPEECHQUEUELOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            if (speechQueueAvailable) {
                for (int i = mSpeechQueue.size() - 1; i > -1; i--){
                    if (mSpeechQueue.get(i).mType != SpeechItem.TEXT_TO_FILE){
                        mSpeechQueue.remove(i);
                    }
                }
                if ((mCurrentSpeechItem != null) &&
                    ((mCurrentSpeechItem.mType != SpeechItem.TEXT_TO_FILE) ||
                      mCurrentSpeechItem.mCallingApp.equals(callingApp))) {
                    try {
                        result = sNativeSynth.stop();
                    } catch (NullPointerException e1) {
                        result = TextToSpeech.ERROR;
                    }
                    mKillList.put(mCurrentSpeechItem, true);
                    if (mPlayer != null) {
                        try {
                            mPlayer.stop();
                        } catch (IllegalStateException e) {
                        }
                    }
                    mIsSpeaking = false;
                    mCurrentSpeechItem = null;
                } else {
                    result = TextToSpeech.SUCCESS;
                }
                Log.i(SERVICE_TAG, "Stopped all");
            } else {
                Log.e(SERVICE_TAG, "TTS stopAll(): queue locked longer than expected");
                result = TextToSpeech.ERROR;
            }
        } catch (InterruptedException e) {
          Log.e(SERVICE_TAG, "TTS stopAll: tryLock interrupted");
          e.printStackTrace();
        } finally {
            if (speechQueueAvailable) {
                speechQueueLock.unlock();
            }
            return result;
        }
    }
    public void onCompletion(MediaPlayer arg0) {
        SpeechItem currentSpeechItemCopy = mCurrentSpeechItem;
        if (currentSpeechItemCopy != null) {
            String callingApp = currentSpeechItemCopy.mCallingApp;
            ArrayList<String> params = currentSpeechItemCopy.mParams;
            String utteranceId = "";
            if (params != null) {
                for (int i = 0; i < params.size() - 1; i = i + 2) {
                    String param = params.get(i);
                    if (param.equals(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)) {
                        utteranceId = params.get(i + 1);
                    }
                }
            }
            if (utteranceId.length() > 0) {
                dispatchUtteranceCompletedCallback(utteranceId, callingApp);
            }
        }
        processSpeechQueue();
    }
    private int playSilence(String callingApp, long duration, int queueMode,
            ArrayList<String> params) {
        if (queueMode == TextToSpeech.QUEUE_FLUSH) {
            stop(callingApp);
        }
        mSpeechQueue.add(new SpeechItem(callingApp, duration, params));
        if (!mIsSpeaking) {
            processSpeechQueue();
        }
        return TextToSpeech.SUCCESS;
    }
    private void silence(final SpeechItem speechItem) {
        class SilenceThread implements Runnable {
            public void run() {
                String utteranceId = "";
                if (speechItem.mParams != null){
                    for (int i = 0; i < speechItem.mParams.size() - 1; i = i + 2){
                        String param = speechItem.mParams.get(i);
                        if (param.equals(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)){
                            utteranceId = speechItem.mParams.get(i+1);
                        }
                    }
                }
                try {
                    Thread.sleep(speechItem.mDuration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (utteranceId.length() > 0){
                        dispatchUtteranceCompletedCallback(utteranceId, speechItem.mCallingApp);
                    }
                    processSpeechQueue();
                }
            }
        }
        Thread slnc = (new Thread(new SilenceThread()));
        slnc.setPriority(Thread.MIN_PRIORITY);
        slnc.start();
    }
    private void speakInternalOnly(final SpeechItem speechItem) {
        class SynthThread implements Runnable {
            public void run() {
                boolean synthAvailable = false;
                String utteranceId = "";
                try {
                    synthAvailable = synthesizerLock.tryLock();
                    if (!synthAvailable) {
                        mSynthBusy = true;
                        Thread.sleep(100);
                        Thread synth = (new Thread(new SynthThread()));
                        synth.start();
                        mSynthBusy = false;
                        return;
                    }
                    int streamType = DEFAULT_STREAM_TYPE;
                    String language = "";
                    String country = "";
                    String variant = "";
                    String speechRate = "";
                    String engine = "";
                    String pitch = "";
                    if (speechItem.mParams != null){
                        for (int i = 0; i < speechItem.mParams.size() - 1; i = i + 2){
                            String param = speechItem.mParams.get(i);
                            if (param != null) {
                                if (param.equals(TextToSpeech.Engine.KEY_PARAM_RATE)) {
                                    speechRate = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_LANGUAGE)){
                                    language = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_COUNTRY)){
                                    country = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_VARIANT)){
                                    variant = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)){
                                    utteranceId = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_STREAM)) {
                                    try {
                                        streamType
                                                = Integer.parseInt(speechItem.mParams.get(i + 1));
                                    } catch (NumberFormatException e) {
                                        streamType = DEFAULT_STREAM_TYPE;
                                    }
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_ENGINE)) {
                                    engine = speechItem.mParams.get(i + 1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_PITCH)) {
                                    pitch = speechItem.mParams.get(i + 1);
                                }
                            }
                        }
                    }
                    if (mKillList.get(speechItem) == null) {
                        if (engine.length() > 0) {
                            setEngine(engine);
                        } else {
                            setEngine(getDefaultEngine());
                        }
                        if (language.length() > 0){
                            setLanguage("", language, country, variant);
                        } else {
                            setLanguage("", getDefaultLanguage(), getDefaultCountry(),
                                    getDefaultLocVariant());
                        }
                        if (speechRate.length() > 0){
                            setSpeechRate("", Integer.parseInt(speechRate));
                        } else {
                            setSpeechRate("", getDefaultRate());
                        }
                        if (pitch.length() > 0){
                            setPitch("", Integer.parseInt(pitch));
                        } else {
                            setPitch("", getDefaultPitch());
                        }
                        try {
                            sNativeSynth.speak(speechItem.mText, streamType);
                        } catch (NullPointerException e) {
                            Log.v(SERVICE_TAG, " null synth, can't speak");
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(SERVICE_TAG, "TTS speakInternalOnly(): tryLock interrupted");
                    e.printStackTrace();
                } finally {
                    if (utteranceId.length() > 0){
                        dispatchUtteranceCompletedCallback(utteranceId, speechItem.mCallingApp);
                    }
                    if (synthAvailable) {
                        synthesizerLock.unlock();
                        processSpeechQueue();
                    }
                }
            }
        }
        Thread synth = (new Thread(new SynthThread()));
        synth.setPriority(Thread.MAX_PRIORITY);
        synth.start();
    }
    private void synthToFileInternalOnly(final SpeechItem speechItem) {
        class SynthThread implements Runnable {
            public void run() {
                boolean synthAvailable = false;
                String utteranceId = "";
                Log.i(SERVICE_TAG, "Synthesizing to " + speechItem.mFilename);
                try {
                    synthAvailable = synthesizerLock.tryLock();
                    if (!synthAvailable) {
                        synchronized (this) {
                            mSynthBusy = true;
                        }
                        Thread.sleep(100);
                        Thread synth = (new Thread(new SynthThread()));
                        synth.start();
                        synchronized (this) {
                            mSynthBusy = false;
                        }
                        return;
                    }
                    String language = "";
                    String country = "";
                    String variant = "";
                    String speechRate = "";
                    String engine = "";
                    String pitch = "";
                    if (speechItem.mParams != null){
                        for (int i = 0; i < speechItem.mParams.size() - 1; i = i + 2){
                            String param = speechItem.mParams.get(i);
                            if (param != null) {
                                if (param.equals(TextToSpeech.Engine.KEY_PARAM_RATE)) {
                                    speechRate = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_LANGUAGE)){
                                    language = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_COUNTRY)){
                                    country = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_VARIANT)){
                                    variant = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID)){
                                    utteranceId = speechItem.mParams.get(i+1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_ENGINE)) {
                                    engine = speechItem.mParams.get(i + 1);
                                } else if (param.equals(TextToSpeech.Engine.KEY_PARAM_PITCH)) {
                                    pitch = speechItem.mParams.get(i + 1);
                                }
                            }
                        }
                    }
                    if (mKillList.get(speechItem) == null){
                        if (engine.length() > 0) {
                            setEngine(engine);
                        } else {
                            setEngine(getDefaultEngine());
                        }
                        if (language.length() > 0){
                            setLanguage("", language, country, variant);
                        } else {
                            setLanguage("", getDefaultLanguage(), getDefaultCountry(),
                                    getDefaultLocVariant());
                        }
                        if (speechRate.length() > 0){
                            setSpeechRate("", Integer.parseInt(speechRate));
                        } else {
                            setSpeechRate("", getDefaultRate());
                        }
                        if (pitch.length() > 0){
                            setPitch("", Integer.parseInt(pitch));
                        } else {
                            setPitch("", getDefaultPitch());
                        }
                        try {
                            sNativeSynth.synthesizeToFile(speechItem.mText, speechItem.mFilename);
                        } catch (NullPointerException e) {
                            Log.v(SERVICE_TAG, " null synth, can't synthesize to file");
                        }
                    }
                } catch (InterruptedException e) {
                    Log.e(SERVICE_TAG, "TTS synthToFileInternalOnly(): tryLock interrupted");
                    e.printStackTrace();
                } finally {
                    if (utteranceId.length() > 0){
                        dispatchUtteranceCompletedCallback(utteranceId, speechItem.mCallingApp);
                    }
                    if (synthAvailable) {
                        synthesizerLock.unlock();
                        processSpeechQueue();
                    }
                }
            }
        }
        Thread synth = (new Thread(new SynthThread()));
        synth.setPriority(Thread.MAX_PRIORITY);
        synth.start();
    }
    private SoundResource getSoundResource(SpeechItem speechItem) {
        SoundResource sr = null;
        String text = speechItem.mText;
        if (speechItem.mType == SpeechItem.SILENCE) {
        } else if (speechItem.mType == SpeechItem.EARCON) {
            sr = mEarcons.get(text);
        } else {
            sr = mUtterances.get(text);
        }
        return sr;
    }
    private void broadcastTtsQueueProcessingCompleted(){
        Intent i = new Intent(TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
        sendBroadcast(i);
    }
    private void dispatchUtteranceCompletedCallback(String utteranceId, String packageName) {
        ITtsCallback cb = mCallbacksMap.get(packageName);
        if (cb == null){
            return;
        }
        Log.v(SERVICE_TAG, "TTS callback: dispatch started");
        final int N = mCallbacks.beginBroadcast();
        try {
            cb.utteranceCompleted(utteranceId);
        } catch (RemoteException e) {
        }
        mCallbacks.finishBroadcast();
        Log.v(SERVICE_TAG, "TTS callback: dispatch completed to " + N);
    }
    private SpeechItem splitCurrentTextIfNeeded(SpeechItem currentSpeechItem){
        if (currentSpeechItem.mText.length() < MAX_SPEECH_ITEM_CHAR_LENGTH){
            return currentSpeechItem;
        } else {
            String callingApp = currentSpeechItem.mCallingApp;
            ArrayList<SpeechItem> splitItems = new ArrayList<SpeechItem>();
            int start = 0;
            int end = start + MAX_SPEECH_ITEM_CHAR_LENGTH - 1;
            String splitText;
            SpeechItem splitItem;
            while (end < currentSpeechItem.mText.length()){
                splitText = currentSpeechItem.mText.substring(start, end);
                splitItem = new SpeechItem(callingApp, splitText, null, SpeechItem.TEXT);
                splitItems.add(splitItem);
                start = end;
                end = start + MAX_SPEECH_ITEM_CHAR_LENGTH - 1;
            }
            splitText = currentSpeechItem.mText.substring(start);
            splitItem = new SpeechItem(callingApp, splitText, null, SpeechItem.TEXT);
            splitItems.add(splitItem);
            mSpeechQueue.remove(0);
            for (int i = splitItems.size() - 1; i >= 0; i--){
                mSpeechQueue.add(0, splitItems.get(i));
            }
            return mSpeechQueue.get(0);
        }
    }
    private void processSpeechQueue() {
        boolean speechQueueAvailable = false;
        synchronized (this) {
            if (mSynthBusy){
                return;
            }
        }
        try {
            speechQueueAvailable =
                    speechQueueLock.tryLock(SPEECHQUEUELOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!speechQueueAvailable) {
                Log.e(SERVICE_TAG, "processSpeechQueue - Speech queue is unavailable.");
                return;
            }
            if (mSpeechQueue.size() < 1) {
                mIsSpeaking = false;
                mKillList.clear();
                broadcastTtsQueueProcessingCompleted();
                return;
            }
            mCurrentSpeechItem = mSpeechQueue.get(0);
            mIsSpeaking = true;
            SoundResource sr = getSoundResource(mCurrentSpeechItem);
            Log.v(SERVICE_TAG, "TTS processing: " + mCurrentSpeechItem.mText);
            if (sr == null) {
                if (mCurrentSpeechItem.mType == SpeechItem.TEXT) {
                    mCurrentSpeechItem = splitCurrentTextIfNeeded(mCurrentSpeechItem);
                    speakInternalOnly(mCurrentSpeechItem);
                } else if (mCurrentSpeechItem.mType == SpeechItem.TEXT_TO_FILE) {
                    synthToFileInternalOnly(mCurrentSpeechItem);
                } else {
                    silence(mCurrentSpeechItem);
                }
            } else {
                cleanUpPlayer();
                if (sr.mSourcePackageName == PKGNAME) {
                    mPlayer = MediaPlayer.create(this, sr.mResId);
                } else if (sr.mSourcePackageName != null) {
                    Context ctx;
                    try {
                        ctx = this.createPackageContext(sr.mSourcePackageName, 0);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                        mSpeechQueue.remove(0); 
                        mIsSpeaking = false;
                        return;
                    }
                    mPlayer = MediaPlayer.create(ctx, sr.mResId);
                } else {
                    mPlayer = MediaPlayer.create(this, Uri.parse(sr.mFilename));
                }
                if (mPlayer == null) {
                    mSpeechQueue.clear();
                    mIsSpeaking = false;
                    return;
                }
                mPlayer.setOnCompletionListener(this);
                try {
                    mPlayer.setAudioStreamType(getStreamTypeFromParams(mCurrentSpeechItem.mParams));
                    mPlayer.start();
                } catch (IllegalStateException e) {
                    mSpeechQueue.clear();
                    mIsSpeaking = false;
                    cleanUpPlayer();
                    return;
                }
            }
            if (mSpeechQueue.size() > 0) {
                mSpeechQueue.remove(0);
            }
        } catch (InterruptedException e) {
          Log.e(SERVICE_TAG, "TTS processSpeechQueue: tryLock interrupted");
          e.printStackTrace();
        } finally {
            if (speechQueueAvailable) {
                speechQueueLock.unlock();
            }
        }
    }
    private int getStreamTypeFromParams(ArrayList<String> paramList) {
        int streamType = DEFAULT_STREAM_TYPE;
        if (paramList == null) {
            return streamType;
        }
        for (int i = 0; i < paramList.size() - 1; i = i + 2) {
            String param = paramList.get(i);
            if ((param != null) && (param.equals(TextToSpeech.Engine.KEY_PARAM_STREAM))) {
                try {
                    streamType = Integer.parseInt(paramList.get(i + 1));
                } catch (NumberFormatException e) {
                    streamType = DEFAULT_STREAM_TYPE;
                }
            }
        }
        return streamType;
    }
    private void cleanUpPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    private boolean synthesizeToFile(String callingApp, String text, ArrayList<String> params,
            String filename) {
        if (filename.length() > MAX_FILENAME_LENGTH) {
            return false;
        }
        if (text.length() >= MAX_SPEECH_ITEM_CHAR_LENGTH){
            return false;
        }
        try {
            File tempFile = new File(filename);
            if (tempFile.exists()) {
                Log.v("TtsService", "File " + filename + " exists, deleting.");
                tempFile.delete();
            }
            if (!tempFile.createNewFile()) {
                Log.e("TtsService", "Unable to synthesize to file: can't create " + filename);
                return false;
            }
            tempFile.delete();
        } catch (IOException e) {
            Log.e("TtsService", "Can't create " + filename + " due to exception " + e);
            return false;
        }
        mSpeechQueue.add(new SpeechItem(callingApp, text, params, SpeechItem.TEXT_TO_FILE, filename));
        if (!mIsSpeaking) {
            processSpeechQueue();
        }
        return true;
    }
    @Override
    public IBinder onBind(Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            for (String category : intent.getCategories()) {
                if (category.equals(CATEGORY)) {
                    return mBinder;
                }
            }
        }
        return null;
    }
    private final android.speech.tts.ITts.Stub mBinder = new Stub() {
        public int registerCallback(String packageName, ITtsCallback cb) {
            if (cb != null) {
                mCallbacks.register(cb);
                mCallbacksMap.put(packageName, cb);
                return TextToSpeech.SUCCESS;
            }
            return TextToSpeech.ERROR;
        }
        public int unregisterCallback(String packageName, ITtsCallback cb) {
            if (cb != null) {
                mCallbacksMap.remove(packageName);
                mCallbacks.unregister(cb);
                return TextToSpeech.SUCCESS;
            }
            return TextToSpeech.ERROR;
        }
        public int speak(String callingApp, String text, int queueMode, String[] params) {
            ArrayList<String> speakingParams = new ArrayList<String>();
            if (params != null) {
                speakingParams = new ArrayList<String>(Arrays.asList(params));
            }
            return mSelf.speak(callingApp, text, queueMode, speakingParams);
        }
        public int playEarcon(String callingApp, String earcon, int queueMode, String[] params) {
            ArrayList<String> speakingParams = new ArrayList<String>();
            if (params != null) {
                speakingParams = new ArrayList<String>(Arrays.asList(params));
            }
            return mSelf.playEarcon(callingApp, earcon, queueMode, speakingParams);
        }
        public int playSilence(String callingApp, long duration, int queueMode, String[] params) {
            ArrayList<String> speakingParams = new ArrayList<String>();
            if (params != null) {
                speakingParams = new ArrayList<String>(Arrays.asList(params));
            }
            return mSelf.playSilence(callingApp, duration, queueMode, speakingParams);
        }
        public int stop(String callingApp) {
            return mSelf.stop(callingApp);
        }
        public boolean isSpeaking() {
            return (mSelf.mIsSpeaking && (mSpeechQueue.size() < 1));
        }
        public void addSpeech(String callingApp, String text, String packageName, int resId) {
            mSelf.addSpeech(callingApp, text, packageName, resId);
        }
        public void addSpeechFile(String callingApp, String text, String filename) {
            mSelf.addSpeech(callingApp, text, filename);
        }
        public void addEarcon(String callingApp, String earcon, String packageName, int resId) {
            mSelf.addEarcon(callingApp, earcon, packageName, resId);
        }
        public void addEarconFile(String callingApp, String earcon, String filename) {
            mSelf.addEarcon(callingApp, earcon, filename);
        }
        public int setSpeechRate(String callingApp, int speechRate) {
            return mSelf.setSpeechRate(callingApp, speechRate);
        }
        public int setPitch(String callingApp, int pitch) {
            return mSelf.setPitch(callingApp, pitch);
        }
        public int isLanguageAvailable(String lang, String country, String variant,
                String[] params) {
            for (int i = 0; i < params.length - 1; i = i + 2){
                String param = params[i];
                if (param != null) {
                    if (param.equals(TextToSpeech.Engine.KEY_PARAM_ENGINE)) {
                        mSelf.setEngine(params[i + 1]);
                        break;
                    }
                }
            }
            return mSelf.isLanguageAvailable(lang, country, variant);
        }
        public String[] getLanguage() {
            return mSelf.getLanguage();
        }
        public int setLanguage(String callingApp, String lang, String country, String variant) {
            return mSelf.setLanguage(callingApp, lang, country, variant);
        }
        public boolean synthesizeToFile(String callingApp, String text, String[] params,
                String filename) {
            ArrayList<String> speakingParams = new ArrayList<String>();
            if (params != null) {
                speakingParams = new ArrayList<String>(Arrays.asList(params));
            }
            return mSelf.synthesizeToFile(callingApp, text, speakingParams, filename);
        }
        public int setEngineByPackageName(String packageName) {
            return mSelf.setEngine(packageName);
        }
        public String getDefaultEngine() {
            return mSelf.getDefaultEngine();
        }
        public boolean areDefaultsEnforced() {
            return mSelf.isDefaultEnforced();
        }
    };
}
