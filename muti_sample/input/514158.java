public class JPEGConsts {
    private JPEGConsts() {}
    public static final int SOI = 0xD8;
    public static final int JCS_UNKNOW = 0;
    public static final int JCS_GRAYSCALE = 1;
    public static final int JCS_RGB = 2;
    public static final int JCS_YCbCr = 3;
    public static final int JCS_CMYK = 4;
    public static final int JCS_YCC = 5;
    public static final int JCS_RGBA = 6;
    public static final int JCS_YCbCrA = 7;
    public static final int JCS_YCCA = 10;
    public static final int JCS_YCCK = 11;
    public static int[][] BAND_OFFSETS = {{}, {0}, {0, 1}, {0, 1, 2}, {0, 1, 2, 3}};
    public static final float DEFAULT_JPEG_COMPRESSION_QUALITY = 0.75f;
}
