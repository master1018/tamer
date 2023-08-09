public class PNGImageReaderSpi extends ImageReaderSpi {
    static final String PNG_NAMES[] = new String[] {"png", "PNG"};
    static final String PNG_SUFFIXES[] = new String[] {"png"};
    static final String PNG_MIME_TYPES[] = new String[] {"image/png"};
    static final String PNG_READER_CLASS_NAME = "org.apache.harmony.x.imageio.plugins.png.PNGImageReader";
    static final String PNG_READER_SPI_NAMES[] = {"org.apache.harmony.x.imageio.plugins.png.PNGImageReaderSpi"};
    public PNGImageReaderSpi() {
        super(
                JPEGSpiConsts.vendorName, JPEGSpiConsts.version,
                PNG_NAMES, PNG_SUFFIXES,
                PNG_MIME_TYPES, PNG_READER_CLASS_NAME,
                STANDARD_INPUT_TYPE, null,
                false, null,
                null, null,
                null, false, 
                null, null,
                null, null
        );
    }
    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        ImageInputStream markable = (ImageInputStream) source;
        markable.mark();
        byte[] signature = new byte[8];
        markable.seek(0);
        int nBytes = markable.read(signature, 0, 8);
        if(nBytes != 8) markable.read(signature, nBytes, 8-nBytes);
        markable.reset();
        return  (signature[0] & 0xFF) == 137 &&
                (signature[1] & 0xFF) == 80 &&
                (signature[2] & 0xFF) == 78 &&
                (signature[3] & 0xFF) == 71 &&
                (signature[4] & 0xFF) == 13 &&
                (signature[5] & 0xFF) == 10 &&
                (signature[6] & 0xFF) == 26 &&
                (signature[7] & 0xFF) == 10;
    }
    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new PNGImageReader(this);
    }
    @Override
    public String getDescription(Locale locale) {
        return "DRL PNG decoder";
    }
    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
        super.onRegistration(registry, category);
    }
}
