public class EncoderCapabilities
{
    private static final String TAG = "EncoderCapabilities";
    static public class VideoEncoderCap {
        public final int mCodec;                                 
        public final int mMinBitRate, mMaxBitRate;               
        public final int mMinFrameRate, mMaxFrameRate;           
        public final int mMinFrameWidth, mMaxFrameWidth;         
        public final int mMinFrameHeight, mMaxFrameHeight;       
        private VideoEncoderCap(int codec,
                                int minBitRate, int maxBitRate,
                                int minFrameRate, int maxFrameRate,
                                int minFrameWidth, int maxFrameWidth,
                                int minFrameHeight, int maxFrameHeight) {
            mCodec = codec;
            mMinBitRate = minBitRate;
            mMaxBitRate = maxBitRate;
            mMinFrameRate = minFrameRate;
            mMaxFrameRate = maxFrameRate;
            mMinFrameWidth = minFrameWidth;
            mMaxFrameWidth = maxFrameWidth;
            mMinFrameHeight = minFrameHeight;
            mMaxFrameHeight = maxFrameHeight;
        }
    };
    static public class AudioEncoderCap {
        public final int mCodec;                         
        public final int mMinChannels, mMaxChannels;     
        public final int mMinSampleRate, mMaxSampleRate; 
        public final int mMinBitRate, mMaxBitRate;       
        private AudioEncoderCap(int codec,
                                int minBitRate, int maxBitRate,
                                int minSampleRate, int maxSampleRate,
                                int minChannels, int maxChannels) {
           mCodec = codec;
           mMinBitRate = minBitRate;
           mMaxBitRate = maxBitRate;
           mMinSampleRate = minSampleRate;
           mMaxSampleRate = maxSampleRate;
           mMinChannels = minChannels;
           mMaxChannels = maxChannels;
       }
    };
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    public static int[] getOutputFileFormats() {
        int nFormats = native_get_num_file_formats();
        if (nFormats == 0) return null;
        int[] formats = new int[nFormats];
        for (int i = 0; i < nFormats; ++i) {
            formats[i] = native_get_file_format(i);
        }
        return formats;
    }
    public static List<VideoEncoderCap> getVideoEncoders() {
        int nEncoders = native_get_num_video_encoders();
        if (nEncoders == 0) return null;
        List<VideoEncoderCap> encoderList = new ArrayList<VideoEncoderCap>();
        for (int i = 0; i < nEncoders; ++i) {
            encoderList.add(native_get_video_encoder_cap(i));
        }
        return encoderList;
    }
    public static List<AudioEncoderCap> getAudioEncoders() {
        int nEncoders = native_get_num_audio_encoders();
        if (nEncoders == 0) return null;
        List<AudioEncoderCap> encoderList = new ArrayList<AudioEncoderCap>();
        for (int i = 0; i < nEncoders; ++i) {
            encoderList.add(native_get_audio_encoder_cap(i));
        }
        return encoderList;
    }
    private EncoderCapabilities() {}  
    private static native final void native_init();
    private static native final int native_get_num_file_formats();
    private static native final int native_get_file_format(int index);
    private static native final int native_get_num_video_encoders();
    private static native final VideoEncoderCap native_get_video_encoder_cap(int index);
    private static native final int native_get_num_audio_encoders();
    private static native final AudioEncoderCap native_get_audio_encoder_cap(int index);
}
