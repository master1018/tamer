public class DecoderCapabilities
{
    public enum VideoDecoder {
        VIDEO_DECODER_WMV,
    };
    public enum AudioDecoder {
        AUDIO_DECODER_WMA,
    };
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    public static List<VideoDecoder> getVideoDecoders() {
        List<VideoDecoder> decoderList = new ArrayList<VideoDecoder>();
        int nDecoders = native_get_num_video_decoders();
        for (int i = 0; i < nDecoders; ++i) {
            decoderList.add(VideoDecoder.values()[native_get_video_decoder_type(i)]);
        }
        return decoderList;
    }
    public static List<AudioDecoder> getAudioDecoders() {
        List<AudioDecoder> decoderList = new ArrayList<AudioDecoder>();
        int nDecoders = native_get_num_audio_decoders();
        for (int i = 0; i < nDecoders; ++i) {
            decoderList.add(AudioDecoder.values()[native_get_audio_decoder_type(i)]);
        }
        return decoderList;
    }
    private DecoderCapabilities() {}  
    private static native final void native_init();
    private static native final int native_get_num_video_decoders();
    private static native final int native_get_video_decoder_type(int index);
    private static native final int native_get_num_audio_decoders();
    private static native final int native_get_audio_decoder_type(int index);
}
