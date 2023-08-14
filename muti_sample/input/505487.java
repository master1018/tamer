public class AudioRecord
{
    public static final int STATE_UNINITIALIZED = 0;
    public static final int STATE_INITIALIZED   = 1;
    public static final int RECORDSTATE_STOPPED = 1;  
    public static final int RECORDSTATE_RECORDING = 3;
    public static final int SUCCESS                 = 0;
    public static final int ERROR                   = -1;
    public static final int ERROR_BAD_VALUE         = -2;
    public static final int ERROR_INVALID_OPERATION = -3;
    private static final int AUDIORECORD_ERROR_SETUP_ZEROFRAMECOUNT      = -16;
    private static final int AUDIORECORD_ERROR_SETUP_INVALIDCHANNELMASK  = -17;
    private static final int AUDIORECORD_ERROR_SETUP_INVALIDFORMAT       = -18;
    private static final int AUDIORECORD_ERROR_SETUP_INVALIDSOURCE       = -19;
    private static final int AUDIORECORD_ERROR_SETUP_NATIVEINITFAILED    = -20;
    private static final int NATIVE_EVENT_MARKER  = 2;
    private static final int NATIVE_EVENT_NEW_POS = 3;
    private final static String TAG = "AudioRecord-Java";
    @SuppressWarnings("unused")
    private int mNativeRecorderInJavaObj;
    @SuppressWarnings("unused")
    private int mNativeCallbackCookie;
    private int mSampleRate = 22050;
    private int mChannelCount = 1;
    private int mChannels = AudioFormat.CHANNEL_IN_MONO;
    private int mChannelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mRecordSource = MediaRecorder.AudioSource.DEFAULT;
    private int mState = STATE_UNINITIALIZED;
    private int mRecordingState = RECORDSTATE_STOPPED;
    private Object mRecordingStateLock = new Object();
    private OnRecordPositionUpdateListener mPositionListener = null;
    private final Object mPositionListenerLock = new Object();
    private NativeEventHandler mEventHandler = null;
    private Looper mInitializationLooper = null;
    private int mNativeBufferSizeInBytes = 0;
    public AudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, 
            int bufferSizeInBytes)
    throws IllegalArgumentException {   
        mState = STATE_UNINITIALIZED;
        mRecordingState = RECORDSTATE_STOPPED;
        if ((mInitializationLooper = Looper.myLooper()) == null) {
            mInitializationLooper = Looper.getMainLooper();
        }
        audioParamCheck(audioSource, sampleRateInHz, channelConfig, audioFormat);
        audioBuffSizeCheck(bufferSizeInBytes);
        int initResult = native_setup( new WeakReference<AudioRecord>(this), 
                mRecordSource, mSampleRate, mChannels, mAudioFormat, mNativeBufferSizeInBytes);
        if (initResult != SUCCESS) {
            loge("Error code "+initResult+" when initializing native AudioRecord object.");
            return; 
        }
        mState = STATE_INITIALIZED;
    }
    private void audioParamCheck(int audioSource, int sampleRateInHz, 
                                 int channelConfig, int audioFormat) {
        if ( (audioSource < MediaRecorder.AudioSource.DEFAULT) ||
             (audioSource > MediaRecorder.getAudioSourceMax()) )  {
            throw (new IllegalArgumentException("Invalid audio source."));
        } else {
            mRecordSource = audioSource;
        }
        if ( (sampleRateInHz < 4000) || (sampleRateInHz > 48000) ) {
            throw (new IllegalArgumentException(sampleRateInHz
                    + "Hz is not a supported sample rate."));
        } else { 
            mSampleRate = sampleRateInHz;
        }
        mChannelConfiguration = channelConfig;
        switch (channelConfig) {
        case AudioFormat.CHANNEL_IN_DEFAULT: 
        case AudioFormat.CHANNEL_IN_MONO:
        case AudioFormat.CHANNEL_CONFIGURATION_MONO:
            mChannelCount = 1;
            mChannels = AudioFormat.CHANNEL_IN_MONO;
            break;
        case AudioFormat.CHANNEL_IN_STEREO:
        case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
            mChannelCount = 2;
            mChannels = AudioFormat.CHANNEL_IN_STEREO;
            break;
        default:
            mChannelCount = 0;
            mChannels = AudioFormat.CHANNEL_INVALID;
            mChannelConfiguration = AudioFormat.CHANNEL_INVALID;
            throw (new IllegalArgumentException("Unsupported channel configuration."));
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
        throw (new IllegalArgumentException("Unsupported sample encoding." 
                + " Should be ENCODING_PCM_8BIT or ENCODING_PCM_16BIT."));
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
    public int getSampleRate() {
        return mSampleRate;
    }
    public int getAudioSource() {
        return mRecordSource;
    }
    public int getAudioFormat() {
        return mAudioFormat;
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
    public int getRecordingState() {
        return mRecordingState;
    }
    public int getNotificationMarkerPosition() {
        return native_get_marker_pos();
    }
    public int getPositionNotificationPeriod() {
        return native_get_pos_update_period();
    }
    static public int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat) {
        int channelCount = 0;
        switch(channelConfig) {
        case AudioFormat.CHANNEL_IN_DEFAULT: 
        case AudioFormat.CHANNEL_IN_MONO:
        case AudioFormat.CHANNEL_CONFIGURATION_MONO:
            channelCount = 1;
            break;
        case AudioFormat.CHANNEL_IN_STEREO:
        case AudioFormat.CHANNEL_CONFIGURATION_STEREO:
            channelCount = 2;
            break;
        case AudioFormat.CHANNEL_INVALID:
        default:
            loge("getMinBufferSize(): Invalid channel configuration.");
            return AudioRecord.ERROR_BAD_VALUE;
        }
        if (audioFormat != AudioFormat.ENCODING_PCM_16BIT) {
            loge("getMinBufferSize(): Invalid audio format.");
            return AudioRecord.ERROR_BAD_VALUE;
        }
        int size = native_get_min_buff_size(sampleRateInHz, channelCount, audioFormat);
        if (size == 0) {
            return AudioRecord.ERROR_BAD_VALUE;
        } 
        else if (size == -1) {
            return AudioRecord.ERROR;
        }
        else {
            return size;
        }
    }
    public void startRecording()
    throws IllegalStateException {
        if (mState != STATE_INITIALIZED) {
            throw(new IllegalStateException("startRecording() called on an "
                    +"uninitialized AudioRecord."));
        }
        synchronized(mRecordingStateLock) {
            if (native_start() == SUCCESS) {
                mRecordingState = RECORDSTATE_RECORDING;
            }
        }
    }
    public void stop()
    throws IllegalStateException {
        if (mState != STATE_INITIALIZED) {
            throw(new IllegalStateException("stop() called on an uninitialized AudioRecord."));
        }
        synchronized(mRecordingStateLock) {
            native_stop();
            mRecordingState = RECORDSTATE_STOPPED;
        }
    }
    public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if ( (audioData == null) || (offsetInBytes < 0 ) || (sizeInBytes < 0) 
                || (offsetInBytes + sizeInBytes > audioData.length)) {
            return ERROR_BAD_VALUE;
        }
        return native_read_in_byte_array(audioData, offsetInBytes, sizeInBytes);
    }
    public int read(short[] audioData, int offsetInShorts, int sizeInShorts) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if ( (audioData == null) || (offsetInShorts < 0 ) || (sizeInShorts < 0) 
                || (offsetInShorts + sizeInShorts > audioData.length)) {
            return ERROR_BAD_VALUE;
        }
        return native_read_in_short_array(audioData, offsetInShorts, sizeInShorts);
    }
    public int read(ByteBuffer audioBuffer, int sizeInBytes) {
        if (mState != STATE_INITIALIZED) {
            return ERROR_INVALID_OPERATION;
        }
        if ( (audioBuffer == null) || (sizeInBytes < 0) ) {
            return ERROR_BAD_VALUE;
        }
        return native_read_in_direct_buffer(audioBuffer, sizeInBytes);
    }
    public void setRecordPositionUpdateListener(OnRecordPositionUpdateListener listener) {
        setRecordPositionUpdateListener(listener, null);
    }
    public void setRecordPositionUpdateListener(OnRecordPositionUpdateListener listener, 
                                                    Handler handler) {
        synchronized (mPositionListenerLock) {
            mPositionListener = listener;
            if (listener != null) {
                if (handler != null) {
                    mEventHandler = new NativeEventHandler(this, handler.getLooper());
                } else {
                    mEventHandler = new NativeEventHandler(this, mInitializationLooper);
                }
            } else {
                mEventHandler = null;
            }
        }
    }
    public int setNotificationMarkerPosition(int markerInFrames) {
        return native_set_marker_pos(markerInFrames);
    }
    public int setPositionNotificationPeriod(int periodInFrames) {
        return native_set_pos_update_period(periodInFrames);
    }
    public interface OnRecordPositionUpdateListener  {
        void onMarkerReached(AudioRecord recorder);
        void onPeriodicNotification(AudioRecord recorder);
    }
    private class NativeEventHandler extends Handler {
        private final AudioRecord mAudioRecord;
        NativeEventHandler(AudioRecord recorder, Looper looper) {
            super(looper);
            mAudioRecord = recorder;
        }
        @Override
        public void handleMessage(Message msg) {
            OnRecordPositionUpdateListener listener = null;
            synchronized (mPositionListenerLock) {
                listener = mAudioRecord.mPositionListener;
            }
            switch(msg.what) {
            case NATIVE_EVENT_MARKER:
                if (listener != null) {
                    listener.onMarkerReached(mAudioRecord);
                }
                break;
            case NATIVE_EVENT_NEW_POS:
                if (listener != null) {
                    listener.onPeriodicNotification(mAudioRecord);
                }
                break;
            default:
                Log.e(TAG, "[ android.media.AudioRecord.NativeEventHandler ] " +
                        "Unknown event type: " + msg.what);
            break;
            }
        }
    };
    @SuppressWarnings("unused")
    private static void postEventFromNative(Object audiorecord_ref,
            int what, int arg1, int arg2, Object obj) {
        AudioRecord recorder = (AudioRecord)((WeakReference)audiorecord_ref).get();
        if (recorder == null) {
            return;
        }
        if (recorder.mEventHandler != null) {
            Message m = 
                recorder.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            recorder.mEventHandler.sendMessage(m);
        }
    }
    private native final int native_setup(Object audiorecord_this, 
            int recordSource, int sampleRate, int nbChannels, int audioFormat, int buffSizeInBytes);
    private native final void native_finalize();
    private native final void native_release();
    private native final int native_start();
    private native final void native_stop();
    private native final int native_read_in_byte_array(byte[] audioData, 
            int offsetInBytes, int sizeInBytes);
    private native final int native_read_in_short_array(short[] audioData, 
            int offsetInShorts, int sizeInShorts);
    private native final int native_read_in_direct_buffer(Object jBuffer, int sizeInBytes);
    private native final int native_set_marker_pos(int marker);
    private native final int native_get_marker_pos();
    private native final int native_set_pos_update_period(int updatePeriod);
    private native final int native_get_pos_update_period();
    static private native final int native_get_min_buff_size(
            int sampleRateInHz, int channelCount, int audioFormat);
    private static void logd(String msg) {
        Log.d(TAG, "[ android.media.AudioRecord ] " + msg);
    }
    private static void loge(String msg) {
        Log.e(TAG, "[ android.media.AudioRecord ] " + msg);
    }
}
