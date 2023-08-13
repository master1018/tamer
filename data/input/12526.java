public abstract class ImageReaderSpi extends ImageReaderWriterSpi {
    @Deprecated
    public static final Class[] STANDARD_INPUT_TYPE =
        { ImageInputStream.class };
    protected Class[] inputTypes = null;
    protected String[] writerSpiNames = null;
    private Class readerClass = null;
    protected ImageReaderSpi() {
    }
    public ImageReaderSpi(String vendorName,
                          String version,
                          String[] names,
                          String[] suffixes,
                          String[] MIMETypes,
                          String readerClassName,
                          Class[] inputTypes,
                          String[] writerSpiNames,
                          boolean supportsStandardStreamMetadataFormat,
                          String nativeStreamMetadataFormatName,
                          String nativeStreamMetadataFormatClassName,
                          String[] extraStreamMetadataFormatNames,
                          String[] extraStreamMetadataFormatClassNames,
                          boolean supportsStandardImageMetadataFormat,
                          String nativeImageMetadataFormatName,
                          String nativeImageMetadataFormatClassName,
                          String[] extraImageMetadataFormatNames,
                          String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version,
              names, suffixes, MIMETypes, readerClassName,
              supportsStandardStreamMetadataFormat,
              nativeStreamMetadataFormatName,
              nativeStreamMetadataFormatClassName,
              extraStreamMetadataFormatNames,
              extraStreamMetadataFormatClassNames,
              supportsStandardImageMetadataFormat,
              nativeImageMetadataFormatName,
              nativeImageMetadataFormatClassName,
              extraImageMetadataFormatNames,
              extraImageMetadataFormatClassNames);
        if (inputTypes == null) {
            throw new IllegalArgumentException
                ("inputTypes == null!");
        }
        if (inputTypes.length == 0) {
            throw new IllegalArgumentException
                ("inputTypes.length == 0!");
        }
        this.inputTypes = (inputTypes == STANDARD_INPUT_TYPE) ?
            new Class<?>[] { ImageInputStream.class } :
            inputTypes.clone();
        if (writerSpiNames != null && writerSpiNames.length > 0) {
            this.writerSpiNames = (String[])writerSpiNames.clone();
        }
    }
    public Class[] getInputTypes() {
        return (Class[])inputTypes.clone();
    }
    public abstract boolean canDecodeInput(Object source) throws IOException;
    public ImageReader createReaderInstance() throws IOException {
        return createReaderInstance(null);
    }
    public abstract ImageReader createReaderInstance(Object extension)
        throws IOException;
    public boolean isOwnReader(ImageReader reader) {
        if (reader == null) {
            throw new IllegalArgumentException("reader == null!");
        }
        String name = reader.getClass().getName();
        return name.equals(pluginClassName);
    }
    public String[] getImageWriterSpiNames() {
        return writerSpiNames == null ?
            null : (String[])writerSpiNames.clone();
    }
}
