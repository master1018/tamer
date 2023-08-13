public abstract class ImageWriterSpi extends ImageReaderWriterSpi {
    public static final Class[] STANDARD_OUTPUT_TYPE = new Class[] {
        ImageInputStream.class
    };
    protected Class[] outputTypes;
    protected String[] readerSpiNames;
    protected ImageWriterSpi() {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public ImageWriterSpi(String vendorName, String version, String[] names, String[] suffixes,
            String[] MIMETypes, String pluginClassName, Class[] outputTypes,
            String[] readerSpiNames, boolean supportsStandardStreamMetadataFormat,
            String nativeStreamMetadataFormatName, String nativeStreamMetadataFormatClassName,
            String[] extraStreamMetadataFormatNames, String[] extraStreamMetadataFormatClassNames,
            boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName,
            String nativeImageMetadataFormatClassName, String[] extraImageMetadataFormatNames,
            String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version, names, suffixes, MIMETypes, pluginClassName,
                supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames, supportsStandardImageMetadataFormat,
                nativeImageMetadataFormatName, nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames, extraImageMetadataFormatClassNames);
        if (outputTypes == null || outputTypes.length == 0) {
            throw new NullPointerException("output types array cannot be NULL or empty");
        }
        this.outputTypes = outputTypes;
        this.readerSpiNames = readerSpiNames;
    }
    public boolean isFormatLossless() {
        return true;
    }
    public Class[] getOutputTypes() {
        return outputTypes;
    }
    public abstract boolean canEncodeImage(ImageTypeSpecifier type);
    public boolean canEncodeImage(RenderedImage im) {
        return canEncodeImage(ImageTypeSpecifier.createFromRenderedImage(im));
    }
    public ImageWriter createWriterInstance() throws IOException {
        return createWriterInstance(null);
    }
    public abstract ImageWriter createWriterInstance(Object extension) throws IOException;
    public boolean isOwnWriter(ImageWriter writer) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public String[] getImageReaderSpiNames() {
        return readerSpiNames;
    }
}
