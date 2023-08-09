public class JPEGSpiConsts {
    private JPEGSpiConsts() {}
    public static final String vendorName = "Intel Corporation";
    public static final String version = "0.1 beta";
    static final String readerClassName = "org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageReader";
    static final String writerClassName = "org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriter";
    static final String[] names = {"jpeg", "jpg", "JPEG", "JPG"};
    static final String[] suffixes = {"jpeg", "jpg"};
    static final String[] MIMETypes = {"image/jpeg"};
    static final String[] writerSpiNames = {"org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageWriterSpi"};
    static final String[] readerSpiNames = {"org.apache.harmony.x.imageio.plugins.jpeg.JPEGImageReaderSpi"};
    static final boolean supportsStandardStreamMetadataFormat = false;
    static final String nativeStreamMetadataFormatName = null;
    static final String nativeStreamMetadataFormatClassName = null;
    static final String[] extraStreamMetadataFormatNames = null;
    static final String[] extraStreamMetadataFormatClassNames = null;
    static final boolean supportsStandardImageMetadataFormat = false;
    static final String nativeImageMetadataFormatName =
            "org.apache.harmony.x.imageio.plugins.jpeg.MyFormatMetadata_1.0";
    static final String nativeImageMetadataFormatClassName =
            "org.apache.harmony.x.imageio.plugins.jpeg.MyFormatMetadata";
    static final String[] extraImageMetadataFormatNames = null;
    static final String[] extraImageMetadataFormatClassNames = null;
}
