public class WBMPImageReaderSpi extends ImageReaderSpi {
    private static final int MAX_WBMP_WIDTH = 1024;
    private static final int MAX_WBMP_HEIGHT = 768;
    private static String [] writerSpiNames =
        {"com.sun.imageio.plugins.wbmp.WBMPImageWriterSpi"};
    private static String[] formatNames = {"wbmp", "WBMP"};
    private static String[] entensions = {"wbmp"};
    private static String[] mimeType = {"image/vnd.wap.wbmp"};
    private boolean registered = false;
    public WBMPImageReaderSpi() {
        super("Oracle Corporation",
              "1.0",
              formatNames,
              entensions,
              mimeType,
              "com.sun.imageio.plugins.wbmp.WBMPImageReader",
              new Class[] { ImageInputStream.class },
              writerSpiNames,
              true,
              null, null, null, null,
              true,
              WBMPMetadata.nativeMetadataFormatName,
              "com.sun.imageio.plugins.wbmp.WBMPMetadataFormat",
              null, null);
    }
    public void onRegistration(ServiceRegistry registry,
                               Class<?> category) {
        if (registered) {
            return;
        }
        registered = true;
    }
    public String getDescription(Locale locale) {
        return "Standard WBMP Image Reader";
    }
    public boolean canDecodeInput(Object source) throws IOException {
        if (!(source instanceof ImageInputStream)) {
            return false;
        }
        ImageInputStream stream = (ImageInputStream)source;
        stream.mark();
        int type = stream.readByte();   
        int fixHeaderField = stream.readByte();
        if (type != 0 || fixHeaderField != 0) {
            stream.reset();
            return false;
        }
        int width = ReaderUtil.readMultiByteInteger(stream);
        int height = ReaderUtil.readMultiByteInteger(stream);
        if (width <= 0 || height <= 0) {
            stream.reset();
            return false;
        }
        long dataLength = stream.length();
        if (dataLength == -1) {
            stream.reset();
            return (width < MAX_WBMP_WIDTH) && (height < MAX_WBMP_HEIGHT);
        }
        dataLength -= stream.getStreamPosition();
        stream.reset();
        long scanSize = (width / 8) + ((width % 8) == 0 ? 0 : 1);
        return (dataLength == scanSize * height);
    }
    public ImageReader createReaderInstance(Object extension)
        throws IIOException {
        return new WBMPImageReader(this);
    }
}
