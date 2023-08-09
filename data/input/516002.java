public class CameraProfile
{
    public static final int QUALITY_LOW    = 0;
    public static final int QUALITY_MEDIUM = 1;
    public static final int QUALITY_HIGH   = 2;
    private static final int[] sJpegEncodingQualityParameters;
    public static int getJpegEncodingQualityParameter(int quality) {
        if (quality < QUALITY_LOW || quality > QUALITY_HIGH) {
            throw new IllegalArgumentException("Unsupported quality level: " + quality);
        }
        return sJpegEncodingQualityParameters[quality];
    }
    static {
        System.loadLibrary("media_jni");
        native_init();
        sJpegEncodingQualityParameters = getImageEncodingQualityLevels();
    }
    private static int[] getImageEncodingQualityLevels() {
        int nLevels = native_get_num_image_encoding_quality_levels();
        if (nLevels != QUALITY_HIGH + 1) {
            throw new RuntimeException("Unexpected Jpeg encoding quality levels " + nLevels);
        }
        int[] levels = new int[nLevels];
        for (int i = 0; i < nLevels; ++i) {
            levels[i] = native_get_image_encoding_quality_level(i);
        }
        Arrays.sort(levels);  
        return levels;
    }
    private static native final void native_init();
    private static native final int native_get_num_image_encoding_quality_levels();
    private static native final int native_get_image_encoding_quality_level(int index);
}
