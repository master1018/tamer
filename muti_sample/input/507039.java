public class MediaRecorder
{
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    private final static String TAG = "MediaRecorder";
    @SuppressWarnings("unused")
    private int mNativeContext;
    @SuppressWarnings("unused")
    private Surface mSurface;
    private String mPath;
    private FileDescriptor mFd;
    private EventHandler mEventHandler;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    public MediaRecorder() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
        native_setup(new WeakReference<MediaRecorder>(this));
    }
    public native void setCamera(Camera c);
    public void setPreviewDisplay(Surface sv) {
        mSurface = sv;
    }
    public final class AudioSource {
        private AudioSource() {}
        public static final int DEFAULT = 0;
        public static final int MIC = 1;
        public static final int VOICE_UPLINK = 2;
        public static final int VOICE_DOWNLINK = 3;
        public static final int VOICE_CALL = 4;
        public static final int CAMCORDER = 5;
        public static final int VOICE_RECOGNITION = 6;
    }
    public final class VideoSource {
        private VideoSource() {}
        public static final int DEFAULT = 0;
        public static final int CAMERA = 1;
    }
    public final class OutputFormat {
        private OutputFormat() {}
        public static final int DEFAULT = 0;
        public static final int THREE_GPP = 1;
        public static final int MPEG_4 = 2;
        public static final int RAW_AMR = 3;
        public static final int AMR_NB = 3;
        public static final int AMR_WB = 4;
        public static final int AAC_ADIF = 5;
        public static final int AAC_ADTS = 6;
    };
    public final class AudioEncoder {
        private AudioEncoder() {}
        public static final int DEFAULT = 0;
        public static final int AMR_NB = 1;
        public static final int AMR_WB = 2;
        public static final int AAC = 3;
        public static final int AAC_PLUS = 4;
        public static final int EAAC_PLUS = 5;
    }
    public final class VideoEncoder {
        private VideoEncoder() {}
        public static final int DEFAULT = 0;
        public static final int H263 = 1;
        public static final int H264 = 2;
        public static final int MPEG_4_SP = 3;
    }
    public native void setAudioSource(int audio_source)
            throws IllegalStateException;
    public static final int getAudioSourceMax() { return AudioSource.VOICE_RECOGNITION; }
    public native void setVideoSource(int video_source)
            throws IllegalStateException;
    public void setProfile(CamcorderProfile profile) {
        setOutputFormat(profile.fileFormat);
        setVideoFrameRate(profile.videoFrameRate);
        setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
        setVideoEncodingBitRate(profile.videoBitRate);
        setAudioEncodingBitRate(profile.audioBitRate);
        setAudioChannels(profile.audioChannels);
        setAudioSamplingRate(profile.audioSampleRate);
        setVideoEncoder(profile.videoCodec);
        setAudioEncoder(profile.audioCodec);
    }
    public native void setOutputFormat(int output_format)
            throws IllegalStateException;
    public native void setVideoSize(int width, int height)
            throws IllegalStateException;
    public native void setVideoFrameRate(int rate) throws IllegalStateException;
    public native void setMaxDuration(int max_duration_ms) throws IllegalArgumentException;
    public native void setMaxFileSize(long max_filesize_bytes) throws IllegalArgumentException;
    public native void setAudioEncoder(int audio_encoder)
            throws IllegalStateException;
    public native void setVideoEncoder(int video_encoder)
            throws IllegalStateException;
    public void setAudioSamplingRate(int samplingRate) {
        if (samplingRate <= 0) {
            throw new IllegalArgumentException("Audio sampling rate is not positive");
        }
        setParameter(String.format("audio-param-sampling-rate=%d", samplingRate));
    }
    public void setAudioChannels(int numChannels) {
        if (numChannels <= 0) {
            throw new IllegalArgumentException("Number of channels is not positive");
        }
        setParameter(String.format("audio-param-number-of-channels=%d", numChannels));
    }
    public void setAudioEncodingBitRate(int bitRate) {
        if (bitRate <= 0) {
            throw new IllegalArgumentException("Audio encoding bit rate is not positive");
        }
        setParameter(String.format("audio-param-encoding-bitrate=%d", bitRate));
    }
    public void setVideoEncodingBitRate(int bitRate) {
        if (bitRate <= 0) {
            throw new IllegalArgumentException("Video encoding bit rate is not positive");
        }
        setParameter(String.format("video-param-encoding-bitrate=%d", bitRate));
    }
    public void setOutputFile(FileDescriptor fd) throws IllegalStateException
    {
        mPath = null;
        mFd = fd;
    }
    public void setOutputFile(String path) throws IllegalStateException
    {
        mFd = null;
        mPath = path;
    }
    private native void _setOutputFile(FileDescriptor fd, long offset, long length)
        throws IllegalStateException, IOException;
    private native void _prepare() throws IllegalStateException, IOException;
    public void prepare() throws IllegalStateException, IOException
    {
        if (mPath != null) {
            FileOutputStream fos = new FileOutputStream(mPath);
            try {
                _setOutputFile(fos.getFD(), 0, 0);
            } finally {
                fos.close();
            }
        } else if (mFd != null) {
            _setOutputFile(mFd, 0, 0);
        } else {
            throw new IOException("No valid output file");
        }
        _prepare();
    }
    public native void start() throws IllegalStateException;
    public native void stop() throws IllegalStateException;
    public void reset() {
        native_reset();
        mEventHandler.removeCallbacksAndMessages(null);
    }
    private native void native_reset();
    public native int getMaxAmplitude() throws IllegalStateException;
    public static final int MEDIA_RECORDER_ERROR_UNKNOWN = 1;
    public interface OnErrorListener
    {
        void onError(MediaRecorder mr, int what, int extra);
    }
    public void setOnErrorListener(OnErrorListener l)
    {
        mOnErrorListener = l;
    }
    public static final int MEDIA_RECORDER_INFO_UNKNOWN              = 1;
    public static final int MEDIA_RECORDER_INFO_MAX_DURATION_REACHED = 800;
    public static final int MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED = 801;
    public interface OnInfoListener
    {
        void onInfo(MediaRecorder mr, int what, int extra);
    }
    public void setOnInfoListener(OnInfoListener listener)
    {
        mOnInfoListener = listener;
    }
    private class EventHandler extends Handler
    {
        private MediaRecorder mMediaRecorder;
        public EventHandler(MediaRecorder mr, Looper looper) {
            super(looper);
            mMediaRecorder = mr;
        }
        private static final int MEDIA_RECORDER_EVENT_ERROR = 1;
        private static final int MEDIA_RECORDER_EVENT_INFO  = 2;
        @Override
        public void handleMessage(Message msg) {
            if (mMediaRecorder.mNativeContext == 0) {
                Log.w(TAG, "mediarecorder went away with unhandled events");
                return;
            }
            switch(msg.what) {
            case MEDIA_RECORDER_EVENT_ERROR:
                if (mOnErrorListener != null)
                    mOnErrorListener.onError(mMediaRecorder, msg.arg1, msg.arg2);
                return;
            case MEDIA_RECORDER_EVENT_INFO:
                if (mOnInfoListener != null)
                    mOnInfoListener.onInfo(mMediaRecorder, msg.arg1, msg.arg2);
                return;
            default:
                Log.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }
    private static void postEventFromNative(Object mediarecorder_ref,
                                            int what, int arg1, int arg2, Object obj)
    {
        MediaRecorder mr = (MediaRecorder)((WeakReference)mediarecorder_ref).get();
        if (mr == null) {
            return;
        }
        if (mr.mEventHandler != null) {
            Message m = mr.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mr.mEventHandler.sendMessage(m);
        }
    }
    public native void release();
    private static native final void native_init();
    private native final void native_setup(Object mediarecorder_this) throws IllegalStateException;
    private native final void native_finalize();
    private native void setParameter(String nameValuePair);
    @Override
    protected void finalize() { native_finalize(); }
}
