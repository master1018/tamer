public class JPEGImageReaderSpi extends ImageReaderSpi {
    private static String [] writerSpiNames =
        {"com.sun.imageio.plugins.jpeg.JPEGImageWriterSpi"};
    public JPEGImageReaderSpi() {
        super(JPEG.vendor,
              JPEG.version,
              JPEG.names,
              JPEG.suffixes,
              JPEG.MIMETypes,
              "com.sun.imageio.plugins.jpeg.JPEGImageReader",
              new Class[] { ImageInputStream.class },
              writerSpiNames,
              true,
              JPEG.nativeStreamMetadataFormatName,
              JPEG.nativeStreamMetadataFormatClassName,
              null, null,
              true,
              JPEG.nativeImageMetadataFormatName,
              JPEG.nativeImageMetadataFormatClassName,
              null, null
              );
    }
    public String getDescription(Locale locale) {
        return "Standard JPEG Image Reader";
    }
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream iis = (ImageInputStream) source;
        iis.mark();
        int byte1 = iis.read();
        int byte2 = iis.read();
        iis.reset();
        if ((byte1 == 0xFF) && (byte2 == JPEG.SOI)) {
            return true;
        }
        return false;
    }
    public ImageReader createReaderInstance(Object extension)
        throws IIOException {
        return new JPEGImageReader(this);
    }
}
