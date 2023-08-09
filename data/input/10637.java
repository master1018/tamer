public class JPEG {
    public static final int TEM = 0x01;
    public static final int SOF0 = 0xC0;
    public static final int SOF1 = 0xC1;
    public static final int SOF2 = 0xC2;
    public static final int SOF3 = 0xC3;
    public static final int DHT = 0xC4;
    public static final int SOF5 = 0xC5;
    public static final int SOF6 = 0xC6;
    public static final int SOF7 = 0xC7;
    public static final int JPG = 0xC8;
    public static final int SOF9 = 0xC9;
    public static final int SOF10 = 0xCA;
    public static final int SOF11 = 0xCB;
    public static final int DAC = 0xCC;
    public static final int SOF13 = 0xCD;
    public static final int SOF14 = 0xCE;
    public static final int SOF15 = 0xCF;
    public static final int RST0 = 0xD0;
    public static final int RST1 = 0xD1;
    public static final int RST2 = 0xD2;
    public static final int RST3 = 0xD3;
    public static final int RST4 = 0xD4;
    public static final int RST5 = 0xD5;
    public static final int RST6 = 0xD6;
    public static final int RST7 = 0xD7;
    public static final int RESTART_RANGE = 8;
    public static final int SOI = 0xD8;
    public static final int EOI = 0xD9;
    public static final int SOS = 0xDA;
    public static final int DQT = 0xDB;
    public static final int DNL = 0xDC;
    public static final int DRI = 0xDD;
    public static final int DHP = 0xDE;
    public static final int EXP = 0xDF;
    public static final int APP0 = 0xE0;
    public static final int APP1 = 0xE1;
    public static final int APP2 = 0xE2;
    public static final int APP3 = 0xE3;
    public static final int APP4 = 0xE4;
    public static final int APP5 = 0xE5;
    public static final int APP6 = 0xE6;
    public static final int APP7 = 0xE7;
    public static final int APP8 = 0xE8;
    public static final int APP9 = 0xE9;
    public static final int APP10 = 0xEA;
    public static final int APP11 = 0xEB;
    public static final int APP12 = 0xEC;
    public static final int APP13 = 0xED;
    public static final int APP14 = 0xEE;
    public static final int APP15 = 0xEF;
    public static final int COM = 0xFE;
    public static final int DENSITY_UNIT_ASPECT_RATIO = 0;
    public static final int DENSITY_UNIT_DOTS_INCH    = 1;
    public static final int DENSITY_UNIT_DOTS_CM      = 2;
    public static final int NUM_DENSITY_UNIT = 3;
    public static final int ADOBE_IMPOSSIBLE = -1;
    public static final int ADOBE_UNKNOWN = 0;
    public static final int ADOBE_YCC = 1;
    public static final int ADOBE_YCCK = 2;
    public static final String vendor = "Oracle Corporation";
    public static final String version = "0.5";
    public static final String [] names = {"JPEG", "jpeg", "JPG", "jpg"};
    public static final String [] suffixes = {"jpg", "jpeg"};
    public static final String [] MIMETypes = {"image/jpeg"};
    public static final String nativeImageMetadataFormatName =
        "javax_imageio_jpeg_image_1.0";
    public static final String nativeImageMetadataFormatClassName =
        "com.sun.imageio.plugins.jpeg.JPEGImageMetadataFormat";
    public static final String nativeStreamMetadataFormatName =
        "javax_imageio_jpeg_stream_1.0";
    public static final String nativeStreamMetadataFormatClassName =
        "com.sun.imageio.plugins.jpeg.JPEGStreamMetadataFormat";
    public static final int JCS_UNKNOWN = 0;       
    public static final int JCS_GRAYSCALE = 1;     
    public static final int JCS_RGB = 2;           
    public static final int JCS_YCbCr = 3;         
    public static final int JCS_CMYK = 4;          
    public static final int JCS_YCC = 5;           
    public static final int JCS_RGBA = 6;          
    public static final int JCS_YCbCrA = 7;        
    public static final int JCS_YCCA = 10;         
    public static final int JCS_YCCK = 11;         
    public static final int NUM_JCS_CODES = JCS_YCCK+1;
    public static final int [] [] bandOffsets = {{0},
                                          {0, 1},
                                          {0, 1, 2},
                                          {0, 1, 2, 3}};
    public static final int [] bOffsRGB = { 2, 1, 0 };
    public static class JCS {
        public static final ColorSpace sRGB =
            ColorSpace.getInstance(ColorSpace.CS_sRGB);
        private static ColorSpace YCC = null;
        private static boolean yccInited = false;
        public static ColorSpace getYCC() {
            if (!yccInited) {
                try {
                    YCC = ColorSpace.getInstance(ColorSpace.CS_PYCC);
                } catch (IllegalArgumentException e) {
                } finally {
                    yccInited = true;
                }
            }
            return YCC;
        }
    }
    public static final float DEFAULT_QUALITY = 0.75F;
    static boolean isNonStandardICC(ColorSpace cs) {
        boolean retval = false;
        if ((cs instanceof ICC_ColorSpace)
            && (!cs.isCS_sRGB())
            && (!cs.equals(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ)))
            && (!cs.equals(ColorSpace.getInstance(ColorSpace.CS_GRAY)))
            && (!cs.equals(ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB)))
            && (!cs.equals(ColorSpace.getInstance(ColorSpace.CS_PYCC)))
            ) {
            retval = true;
        }
        return retval;
    }
    static boolean isJFIFcompliant(ImageTypeSpecifier imageType,
                                   boolean input) {
        ColorModel cm = imageType.getColorModel();
        if (cm.hasAlpha()) {
            return false;
        }
        int numComponents = imageType.getNumComponents();
        if (numComponents == 1) {
            return true;
        }
        if (numComponents != 3) {
            return false;
        }
        if (input) {
            if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB) {
                return true;
            }
        } else {
            if (cm.getColorSpace().getType() == ColorSpace.TYPE_YCbCr) {
                return true;
            }
        }
        return false;
    }
    static int transformForType(ImageTypeSpecifier imageType, boolean input) {
        int retval = ADOBE_IMPOSSIBLE;
        ColorModel cm = imageType.getColorModel();
        switch (cm.getColorSpace().getType()) {
        case ColorSpace.TYPE_GRAY:
            retval = ADOBE_UNKNOWN;
            break;
        case ColorSpace.TYPE_RGB:
            retval = input ? ADOBE_YCC : ADOBE_UNKNOWN;
            break;
        case ColorSpace.TYPE_YCbCr:
            retval = ADOBE_YCC;
            break;
        case ColorSpace.TYPE_CMYK:
            retval = input ? ADOBE_YCCK : ADOBE_IMPOSSIBLE;
        }
        return retval;
    }
    static float convertToLinearQuality(float quality) {
        if (quality <= 0.0F) {
            quality = 0.01F;
        }
        if (quality > 1.00F) {
            quality = 1.00F;
        }
        if (quality < 0.5F) {
            quality = 0.5F / quality;
        } else {
            quality = 2.0F - (quality * 2.0F);
        }
        return quality;
    }
    static JPEGQTable [] getDefaultQTables() {
        JPEGQTable [] qTables = new JPEGQTable[2];
        qTables[0] = JPEGQTable.K1Div2Luminance;
        qTables[1] = JPEGQTable.K2Div2Chrominance;
        return qTables;
    }
    static JPEGHuffmanTable [] getDefaultHuffmanTables(boolean wantDC) {
        JPEGHuffmanTable [] tables = new JPEGHuffmanTable[2];
        if (wantDC) {
            tables[0] = JPEGHuffmanTable.StdDCLuminance;
            tables[1] = JPEGHuffmanTable.StdDCChrominance;
        } else {
            tables[0] = JPEGHuffmanTable.StdACLuminance;
            tables[1] = JPEGHuffmanTable.StdACChrominance;
        }
        return tables;
    }
}
