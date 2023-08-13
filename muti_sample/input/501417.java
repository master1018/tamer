public class CamcorderProfile
{
    public static final int QUALITY_LOW  = 0;
    public static final int QUALITY_HIGH = 1;
    public int duration;
    public int quality;
    public int fileFormat;
    public int videoCodec;
    public int videoBitRate;
    public int videoFrameRate;
    public int videoFrameWidth;
    public int videoFrameHeight;
    public int audioCodec;
    public int audioBitRate;
    public int audioSampleRate;
    public int audioChannels;
    public static CamcorderProfile get(int quality) {
        if (quality < QUALITY_LOW || quality > QUALITY_HIGH) {
            String errMessage = "Unsupported quality level: " + quality;
            throw new IllegalArgumentException(errMessage);
        }
        return native_get_camcorder_profile(quality);
    }
    static {
        System.loadLibrary("media_jni");
        native_init();
    }
    private CamcorderProfile(int duration,
                             int quality,
                             int fileFormat,
                             int videoCodec,
                             int videoBitRate,
                             int videoFrameRate,
                             int videoWidth,
                             int videoHeight,
                             int audioCodec,
                             int audioBitRate,
                             int audioSampleRate,
                             int audioChannels) {
        this.duration         = duration;
        this.quality          = quality;
        this.fileFormat       = fileFormat;
        this.videoCodec       = videoCodec;
        this.videoBitRate     = videoBitRate;
        this.videoFrameRate   = videoFrameRate;
        this.videoFrameWidth  = videoWidth;
        this.videoFrameHeight = videoHeight;
        this.audioCodec       = audioCodec;
        this.audioBitRate     = audioBitRate;
        this.audioSampleRate  = audioSampleRate;
        this.audioChannels    = audioChannels;
    }
    private static native final void native_init();
    private static native final CamcorderProfile native_get_camcorder_profile(int quality);
}
