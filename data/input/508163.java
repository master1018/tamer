public abstract class ImageReaderSpi extends ImageReaderWriterSpi {
    public static final Class[] STANDARD_INPUT_TYPE = new Class[] {
        ImageInputStream.class
    };
    protected Class[] inputTypes;
    protected String[] writerSpiNames;
    protected ImageReaderSpi() {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public ImageReaderSpi(String vendorName, String version, String[] names, String[] suffixes,
            String[] MIMETypes, String pluginClassName, Class[] inputTypes,
            String[] writerSpiNames, boolean supportsStandardStreamMetadataFormat,
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
        if (inputTypes == null || inputTypes.length == 0) {
            throw new NullPointerException("input types array cannot be NULL or empty");
        }
        this.inputTypes = inputTypes;
        this.writerSpiNames = writerSpiNames;
    }
    public Class[] getInputTypes() {
        return inputTypes;
    }
    public abstract boolean canDecodeInput(Object source) throws IOException;
    public ImageReader createReaderInstance() throws IOException {
        return createReaderInstance(null);
    }
    public abstract ImageReader createReaderInstance(Object extension) throws IOException;
    public boolean isOwnReader(ImageReader reader) {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public String[] getImageWriterSpiNames() {
        throw new UnsupportedOperationException("Not supported yet");
    }
}
