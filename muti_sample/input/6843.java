public class WBMPImageWriterSpi extends ImageWriterSpi {
    private static String [] readerSpiNames =
        {"com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi"};
    private static String[] formatNames = {"wbmp", "WBMP"};
    private static String[] entensions = {"wbmp"};
    private static String[] mimeType = {"image/vnd.wap.wbmp"};
    private boolean registered = false;
    public WBMPImageWriterSpi() {
        super("Oracle Corporation",
              "1.0",
              formatNames,
              entensions,
              mimeType,
              "com.sun.imageio.plugins.wbmp.WBMPImageWriter",
              new Class[] { ImageOutputStream.class },
              readerSpiNames,
              true,
              null, null, null, null,
              true,
              null, null, null, null);
    }
    public String getDescription(Locale locale) {
        return "Standard WBMP Image Writer";
    }
    public void onRegistration(ServiceRegistry registry,
                               Class<?> category) {
        if (registered) {
            return;
        }
        registered = true;
    }
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        SampleModel sm = type.getSampleModel();
        if (!(sm instanceof MultiPixelPackedSampleModel))
            return false;
        if (sm.getSampleSize(0) != 1)
            return false;
        return true;
    }
    public ImageWriter createWriterInstance(Object extension)
        throws IIOException {
        return new WBMPImageWriter(this);
    }
}
