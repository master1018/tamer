public class SynthProxy {
    private final static float PICO_FILTER_GAIN = 5.0f; 
    private final static float PICO_FILTER_LOWSHELF_ATTENUATION = -18.0f; 
    private final static float PICO_FILTER_TRANSITION_FREQ = 1100.0f;     
    private final static float PICO_FILTER_SHELF_SLOPE = 1.0f;            
    public SynthProxy(String nativeSoLib, String engineConfig) {
        boolean applyFilter = nativeSoLib.toLowerCase().contains("pico");
        Log.v(TtsService.SERVICE_TAG, "About to load "+ nativeSoLib + ", applyFilter="+applyFilter);
        native_setup(new WeakReference<SynthProxy>(this), nativeSoLib, engineConfig);
        native_setLowShelf(applyFilter, PICO_FILTER_GAIN, PICO_FILTER_LOWSHELF_ATTENUATION,
                PICO_FILTER_TRANSITION_FREQ, PICO_FILTER_SHELF_SLOPE);
    }
    public int stop() {
        return native_stop(mJniData);
    }
    public int stopSync() {
        return native_stopSync(mJniData);
    }
    public int speak(String text, int streamType) {
        if ((streamType > -1) && (streamType < AudioSystem.getNumStreamTypes())) {
            return native_speak(mJniData, text, streamType);
        } else {
            Log.e("SynthProxy", "Trying to speak with invalid stream type " + streamType);
            return native_speak(mJniData, text, AudioManager.STREAM_MUSIC);
        }
    }
    public int synthesizeToFile(String text, String filename) {
        return native_synthesizeToFile(mJniData, text, filename);
    }
    public int isLanguageAvailable(String language, String country, String variant) {
        return native_isLanguageAvailable(mJniData, language, country, variant);
    }
    public int setConfig(String engineConfig) {
        return native_setConfig(mJniData, engineConfig);
    }
    public int setLanguage(String language, String country, String variant) {
        return native_setLanguage(mJniData, language, country, variant);
    }
    public int loadLanguage(String language, String country, String variant) {
        return native_loadLanguage(mJniData, language, country, variant);
    }
    public final int setSpeechRate(int speechRate) {
        return native_setSpeechRate(mJniData, speechRate);
    }
    public final int setPitch(int pitch) {
        return native_setPitch(mJniData, pitch);
    }
    public String[] getLanguage() {
        return native_getLanguage(mJniData);
    }
    public int getRate() {
        return native_getRate(mJniData);
    }
    public void shutdown()  {
        native_shutdown(mJniData);
    }
    protected void finalize() {
        native_finalize(mJniData);
        mJniData = 0;
    }
    static {
        System.loadLibrary("ttssynthproxy");
    }
    private final static String TAG = "SynthProxy";
    private int mJniData = 0;
    private native final int native_setup(Object weak_this, String nativeSoLib,
            String engineConfig);
    private native final int native_setLowShelf(boolean applyFilter, float filterGain,
            float attenuationInDb, float freqInHz, float slope);
    private native final void native_finalize(int jniData);
    private native final int native_stop(int jniData);
    private native final int native_stopSync(int jniData);
    private native final int native_speak(int jniData, String text, int streamType);
    private native final int native_synthesizeToFile(int jniData, String text, String filename);
    private native final int  native_isLanguageAvailable(int jniData, String language,
            String country, String variant);
    private native final int native_setLanguage(int jniData, String language, String country,
            String variant);
    private native final int native_loadLanguage(int jniData, String language, String country,
            String variant);
    private native final int native_setConfig(int jniData, String engineConfig);
    private native final int native_setSpeechRate(int jniData, int speechRate);
    private native final int native_setPitch(int jniData, int speechRate);
    private native final String[] native_getLanguage(int jniData);
    private native final int native_getRate(int jniData);
    private native final void native_shutdown(int jniData);
    @SuppressWarnings("unused")
    private static void postNativeSpeechSynthesizedInJava(Object tts_ref,
            int bufferPointer, int bufferSize) {
        Log.i("TTS plugin debug", "bufferPointer: " + bufferPointer
                + " bufferSize: " + bufferSize);
        SynthProxy nativeTTS = (SynthProxy)((WeakReference)tts_ref).get();
    }
}
