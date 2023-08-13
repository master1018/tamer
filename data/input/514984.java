public class AudioTrack
{
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_MAX = 1.0f;
    public static final int PLAYSTATE_STOPPED = 1;  
    public static final int PLAYSTATE_PAUSED  = 2;  
    public static final int PLAYSTATE_PLAYING = 3;  
    public static final int MODE_STATIC = 0;
    public static final int MODE_STREAM = 1;
    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_INITIALIZED   = 1;
    public static final int STATE_NO_STATIC_DATA = 2;
    public  static final int SUCCESS                               = 0;
    public  static final int ERROR                                 = -1;
    public  static final int ERROR_BAD_VALUE                       = -2;
    public  static final int ERROR_INVALID_OPERATION               = -3;
    private static final int ERROR_NATIVESETUP_AUDIOSYSTEM         = -16;
    private static final int ERROR_NATIVESETUP_INVALIDCHANNELMASK  = -17;
    private static final int ERROR_NATIVESETUP_INVALIDFORMAT       = -18;
    private static final int ERROR_NATIVESETUP_INVALIDSTREAMTYPE   = -19;
    private static final int ERROR_NATIVESETUP_NATIVEINITFAILED    = -20;
    private static final int NATIVE_EVENT_MARKER  = 3;
    private static final int NATIVE_EVENT_NEW_POS = 4;
    private final static String TAG = "AudioTrack-Java";
    private int mState = STATE_UNINITIALIZED;
    private int mPlayState = PLAYSTATE_STOPPED;
    private final Object mPlayStateLock = new Object();
    private OnPlaybackPositionUpdateListener mPositionListener = null;
    private final Object mPositionListenerLock = new Object();
    private int mNativeBufferSizeInBytes = 0;
    private NativeEventHandlerDelegate mEventHandlerDelegate = null;
    private Looper mInitializationLooper = null;
    private int mSampleRate = 22050;
    private int mChannelCount = 1;
    private int mChannels = AudioFormat.CHANNEL_OUT_MONO;
    private int mStreamType = AudioManager.STREAM_MUSIC;
    private int mDataLoadMode = MODE_STREAM;
    private int mChannelConfiguration = AudioFormat.CHANNEL_OUT_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    @SuppressWarnings("unused")
    private int mNativeTrackInJavaObj;
    @SuppressWarnings("unused")
    private int mJniData;
    public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat,
            int bufferSizeInBytes, int mode)
    throws IllegalArgumentException {
        mState = STATE_UNINITIALIZED;
        if ((mInitializationLooper = Looper.myLooper()) == null) {
            mInitializationLooper = Looper.getMainLooper();
        }
        audioParamCheck(streamType, sampleRateInHz, channelConfig, audioFormat, mode);
        audioBuffSizeCheck(bufferSizeInBytes);
        int initResult = native_setup(new WeakReference<AudioTrack>(this),
                mStreamType, mSampleRate, mChannels, mAudioFormat,
                mNativeBufferSizeInBytes, mDataLoadMode);
        if (initResult != SUCCESS) {
            loge("Error code "+initResult+" when initializing AudioTrack.");
            return; 
        }
        if (mDataLoadMode == MODE_STATIC) {
            mState = STATE_NO_STATIC_DATA;
        } else {
            mState = STATE_INITIALIZED;
        }
    }
    private void audioParamCheck(int streamType, int sampleRateInHz,
                                 int channelConfig, int audioFormat, int mode) {
        if( (streamType != AudioManager.STREAM_ALARM) && (streamType != AudioManager.STREAM_MUSIC)
           && (streamType != AudioManager.STREAM_RING) && (streamType != AudioManager.STREAM_SYSTEM)
           && (streamType != AudioManager.STREAM_VOICE_CALL)
           && (streamType != AudioManager.STREAM_NOTIFICATION)
           && (streamType != AudioManager.STREAM_BLUETOOTH_SCO)
           && (streamType != AudioManager.STREAM_DTMF)) {
            throw (new IllegalArgumentException("Invalid stream type."));
        } else {
            mStreamType = streamType;
        }
        if ( (sampleRateInHz < 4000) || (sampleRateInHz > 48000) ) {
            throw (new IllegalArgumentException(sampleRateInHz
                    + "Hz is not a supported sample rate."));
        } else {
            mSampleRate = sampleRateInHz;
        }
        mChannelConfiguration = channelConfig;
        switch (channelConfig) {
        case AudioFormat.CHANNEL_OUT_DEFAULT: 
        case AudioFormat.CHANNEL_OUT_MONO:
        case AudioFormat.CHANNEL_CONFIGURATION_MONO:
            mChannelCount = 1;
            mChannels = AudioFormat.CHANNEL_OUT_MONO;
            break;
        case AudioFormat.CHANNEL_OUT_STEREO:
        case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
            mChannelCount = 2;
            mChannels = AudioFormat.CHANNEL_OUT_STEREO;
            break;
        default:
            mChannelCount = 0;
            mChannels = AudioFormat.CHANNEL_INVALID;
            mChannelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_INVALID;
            throw(new IllegalArgumentException("Unsupported channel configuration."));
        }
        switch (audioFormat) {
        case AudioFormat.ENCODING_DEFAULT:
            mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
            break;
        case AudioFormat.ENCODING_PCM_16BIT:
        case AudioFormat.ENCODING_PCM_8BIT:
            mAudioFormat = audioFormat;
            break;
        default:
            mAudioFormat = AudioFormat.ENCODING_INVALID;
            throw(new IllegalArgumentException("Unsupported sample encoding."
                + " Should be ENCODING_PCM_8BIT or ENCODING_PCM_16BIT."));
        }
        if ( (mode != MODE_STREAM) && (mode != MODE_STATIC) ) {
            throw(new IllegalArgumentException("Invalid mode."));
        } else {
            mDataLoadMode = mode;
        }
    }
    private void audioBuffSizeCheck(int audioBufferSize) {
        int frameSizeInBytes = mChannelCount
                * (mAudioFormat == AudioFormat.ENCODING_PCM_8BIT ? 1 : 2);
        if ((audioBufferSize % frameSizeInBytes != 0) || (audioBufferSize < 1)) {
            throw (new IllegalArgumentException("Invalid audio buffer size."));
        }
        mNativeBufferSizeInBytes = audioBufferSize;
    }
    public void release() {
        try {
            stop();
        } catch(IllegalStateException ise) { 
        }
        native_release();
        mState = STATE_UNINITIALIZED;
    }
    @Override
    protected void finalize() {
        native_finalize();
    }
    static public float getMinVolume() {
        return AudioTrack.VOLUME_MIN;
    }
    static public float getMaxVolume() {
        return AudioTrack.VOLUME_MAX;
    }
    public int getSampleRate() {
        return mSampleRate;
    }
    public int getPlaybackRate() {
        return native_get_playback_rate();
    }
    public int getAudioFormat() {
        return mAudioFormat;
    }
    public int getStreamType() {
        return mStreamType;
    }
    public int getChannelConfiguration() {
        return mChannelConfiguration;
    }
    public int getChannelCount() {
        return mChannelCount;
    }
    public int getState() {
        return mState;
    }
    public int getPlayState() {
        return mPlayState;
    }
    protected int getNativeFrameCount() {
        return native_get_native_frame_count();
    }
    public int getNotificationMarkerPosition() {
        return native_get_marker_pos();
    }
    public int getPositionNotificationPeriod() {
        return native_get_pos_update_period();
    }
    public int getPlaybackHeadPosition() {
        return native_get_position();
    }
    static public int getNativeOutputSampleRate(int streamType) {
        return native_get_output_sample_rate(streamType);
    }
    static public int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        int channelCount = 0;
        switch(channelConfig) {
        case AudioFormat.CHANNEL_OUT_MONO:
        case AudioFormat.CHANNEL_CONFIGURATION_MONO:
            channelCount = 1;
            break;
        case AudioFormat.CHANNEL_OUT_STEREO:
        case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
            channelCount = 2;
            break;
        default:
            loge("getMinBufferSize(): Invalid channel configuration.");
            return AudioTrack.ERROR_BAD_VALUE;
        }
        if ((audioFormat != AudioFormat.ENCODING_PCM_16BIT) 
            && (audioFormat != AudioFormat.ENCODING_PCM_8BIT)) {
            loge("getMinBufferSize(): Invalid audio format.");
            return AudioTrack.ERROR_BAD_VALUE;
        }
        if ( (sampleRateInHz < 4000) || (sampleRateInHz > 48000) ) {
            loge("getMinBufferSize(): " + sampleRateInHz +"Hz is not a supported sample rate.");
            return AudioTrack.ERROR_BAD_VALUE;
        }
        int size = native_get_min_buff_size(sampleRateInHz, channelCount, audioFormat);
        if ((size == -1) || (size == 0)) {
            loge("getMinBufferSize(): error querying hardware");
            return AudioTrack.ERROR;
        }
        else {
            return size;
        }
    }
    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener listener) {
        setPlaybackPositionUpdateListener(listener, null);
    }
    public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener listener, 
                                                    Handler handler) {
        synchronized (mPositionListenerLock) {
            mPositionListener = listener;
        }
        if (listener != null) {
            mEventHandlerDelegate = new NativeEventHandlerDelegate(this, handler);
        }
    }
    public int setStereoVolume(float leftVolume, float rightVolume) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if (leftVolume < getMinVolume()) {
            leftVolume = getMinVolume();
        }
        if (leftVolume > getMaxVolume()) {
            leftVolume = getMaxVolume();
        }
        if (rightVolume < getMinVolume()) {
            rightVolume = getMinVolume();
        }
        if (rightVolume > getMaxVolume()) {
            rightVolume = getMaxVolume();
        }
        native_setVolume(leftVolume, rightVolume);
        return SUCCESS;
    }
    public int setPlaybackRate(int sampleRateInHz) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if (sampleRateInHz <= 0) {
            return ERROR_BAD_VALUE;
        }
        return native_set_playback_rate(sampleRateInHz);
    }
    public int setNotificationMarkerPosition(int markerInFrames) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        return native_set_marker_pos(markerInFrames);
    }
    public int setPositionNotificationPeriod(int periodInFrames) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        return native_set_pos_update_period(periodInFrames);
    }
    public int setPlaybackHeadPosition(int positionInFrames) {
        synchronized(mPlayStateLock) {
            if ((mPlayState == PLAYSTATE_STOPPED) || (mPlayState == PLAYSTATE_PAUSED)) {
                return native_set_position(positionInFrames);
            } else {
                return ERROR_INVALID_OPERATION;
            }
        }
    }
    public int setLoopPoints(int startInFrames, int endInFrames, int loopCount) {
        if (mDataLoadMode == MODE_STREAM) {
            return ERROR_INVALID_OPERATION;
        }
        return native_set_loop(startInFrames, endInFrames, loopCount);
    }
    protected void setState(int state) {
        mState = state;
    }
    public void play()
    throws IllegalStateException {
        if (mState != STATE_INITIALIZED) {
            throw(new IllegalStateException("play() called on uninitialized AudioTrack."));
        }
        synchronized(mPlayStateLock) {
            native_start();
            mPlayState = PLAYSTATE_PLAYING;
        }
    }
    public void stop()
    throws IllegalStateException {
        if (mState != STATE_INITIALIZED) {
            throw(new IllegalStateException("stop() called on uninitialized AudioTrack."));
        }
        synchronized(mPlayStateLock) {
            native_stop();
            mPlayState = PLAYSTATE_STOPPED;
        }
    }
    public void pause()
    throws IllegalStateException {
        if (mState != STATE_INITIALIZED) {
            throw(new IllegalStateException("pause() called on uninitialized AudioTrack."));
        }
        synchronized(mPlayStateLock) {
            native_pause();
            mPlayState = PLAYSTATE_PAUSED;
        }
    }
    public void flush() {
        if (mState == STATE_INITIALIZED) {
            native_flush();
        }
    }
    public int write(byte[] audioData,int offsetInBytes, int sizeInBytes) {
        if ((mDataLoadMode == MODE_STATIC)
                && (mState == STATE_NO_STATIC_DATA)
                && (sizeInBytes > 0)) {
            mState = STATE_INITIALIZED;
        }
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if ( (audioData == null) || (offsetInBytes < 0 ) || (sizeInBytes < 0) 
                || (offsetInBytes + sizeInBytes > audioData.length)) {
            return ERROR_BAD_VALUE;
        }
        return native_write_byte(audioData, offsetInBytes, sizeInBytes, mAudioFormat);
    }
    public int write(short[] audioData, int offsetInShorts, int sizeInShorts) {
        if ((mDataLoadMode == MODE_STATIC)
                && (mState == STATE_NO_STATIC_DATA)
                && (sizeInShorts > 0)) {
            mState = STATE_INITIALIZED;
        }
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if ( (audioData == null) || (offsetInShorts < 0 ) || (sizeInShorts < 0) 
                || (offsetInShorts + sizeInShorts > audioData.length)) {
            return ERROR_BAD_VALUE;
        }
        return native_write_short(audioData, offsetInShorts, sizeInShorts, mAudioFormat);
    }
    public int reloadStaticData() {
        if (mDataLoadMode == MODE_STREAM) {
            return ERROR_INVALID_OPERATION;
        }
        return native_reload_static();
    }
    public interface OnPlaybackPositionUpdateListener  {
        void onMarkerReached(AudioTrack track);
        void onPeriodicNotification(AudioTrack track);
    }
    private class NativeEventHandlerDelegate {
        private final AudioTrack mAudioTrack;
        private final Handler mHandler;
        NativeEventHandlerDelegate(AudioTrack track, Handler handler) {
            mAudioTrack = track;
            Looper looper;
            if (handler != null) {
                looper = handler.getLooper();
            } else {
                looper = mInitializationLooper;
            }
            if (looper != null) {
                mHandler = new Handler(looper) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (mAudioTrack == null) {
                            return;
                        }
                        OnPlaybackPositionUpdateListener listener = null;
                        synchronized (mPositionListenerLock) {
                            listener = mAudioTrack.mPositionListener;
                        }
                        switch(msg.what) {
                        case NATIVE_EVENT_MARKER:
                            if (listener != null) {
                                listener.onMarkerReached(mAudioTrack);
                            }
                            break;
                        case NATIVE_EVENT_NEW_POS:
                            if (listener != null) {
                                listener.onPeriodicNotification(mAudioTrack);
                            }
                            break;
                        default:
                            Log.e(TAG, "[ android.media.AudioTrack.NativeEventHandler ] " +
                                    "Unknown event type: " + msg.what);
                            break;
                        }
                    }
                };
            } else {
                mHandler = null;
            } 
        }
        Handler getHandler() {
            return mHandler;
        }
    }
    @SuppressWarnings("unused")
    private static void postEventFromNative(Object audiotrack_ref,
            int what, int arg1, int arg2, Object obj) {
        AudioTrack track = (AudioTrack)((WeakReference)audiotrack_ref).get();
        if (track == null) {
            return;
        }
        if (track.mEventHandlerDelegate != null) {
            Message m = 
                track.mEventHandlerDelegate.getHandler().obtainMessage(what, arg1, arg2, obj);
            track.mEventHandlerDelegate.getHandler().sendMessage(m);
        }
    }
    private native final int native_setup(Object audiotrack_this,
            int streamType, int sampleRate, int nbChannels, int audioFormat,
            int buffSizeInBytes, int mode);
    private native final void native_finalize();
    private native final void native_release();
    private native final void native_start();
    private native final void native_stop();
    private native final void native_pause();
    private native final void native_flush();
    private native final int native_write_byte(byte[] audioData,
                                               int offsetInBytes, int sizeInBytes, int format);
    private native final int native_write_short(short[] audioData,
                                                int offsetInShorts, int sizeInShorts, int format);
    private native final int native_reload_static();
    private native final int native_get_native_frame_count();
    private native final void native_setVolume(float leftVolume, float rightVolume);
    private native final int native_set_playback_rate(int sampleRateInHz);
    private native final int native_get_playback_rate();
    private native final int native_set_marker_pos(int marker);
    private native final int native_get_marker_pos();
    private native final int native_set_pos_update_period(int updatePeriod);
    private native final int native_get_pos_update_period();
    private native final int native_set_position(int position);
    private native final int native_get_position();
    private native final int native_set_loop(int start, int end, int loopCount);
    static private native final int native_get_output_sample_rate(int streamType);
    static private native final int native_get_min_buff_size(
            int sampleRateInHz, int channelConfig, int audioFormat);
    private static void logd(String msg) {
        Log.d(TAG, "[ android.media.AudioTrack ] " + msg);
    }
    private static void loge(String msg) {
        Log.e(TAG, "[ android.media.AudioTrack ] " + msg);
    }
}
