public abstract class ImageReaderWriterSpi extends IIOServiceProvider implements
        RegisterableService {
    protected String[] names;
    protected String[] suffixes;
    protected String[] MIMETypes;
    protected String pluginClassName;
    protected boolean supportsStandardStreamMetadataFormat;
    protected String nativeStreamMetadataFormatName;
    protected String nativeStreamMetadataFormatClassName;
    protected String[] extraStreamMetadataFormatNames;
    protected String[] extraStreamMetadataFormatClassNames;
    protected boolean supportsStandardImageMetadataFormat;
    protected String nativeImageMetadataFormatName;
    protected String nativeImageMetadataFormatClassName;
    protected String[] extraImageMetadataFormatNames;
    protected String[] extraImageMetadataFormatClassNames;
    public ImageReaderWriterSpi(String vendorName, String version, String[] names,
            String[] suffixes, String[] MIMETypes, String pluginClassName,
            boolean supportsStandardStreamMetadataFormat, String nativeStreamMetadataFormatName,
            String nativeStreamMetadataFormatClassName, String[] extraStreamMetadataFormatNames,
            String[] extraStreamMetadataFormatClassNames,
            boolean supportsStandardImageMetadataFormat, String nativeImageMetadataFormatName,
            String nativeImageMetadataFormatClassName, String[] extraImageMetadataFormatNames,
            String[] extraImageMetadataFormatClassNames) {
        super(vendorName, version);
        if (names == null || names.length == 0) {
            throw new NullPointerException("format names array cannot be NULL or empty");
        }
        if (pluginClassName == null) {
            throw new NullPointerException("Plugin class name cannot be NULL");
        }
        this.names = names.clone();
        this.suffixes = suffixes == null ? null : suffixes.clone();
        this.MIMETypes = MIMETypes == null ? null : MIMETypes.clone();
        this.pluginClassName = pluginClassName;
        this.supportsStandardStreamMetadataFormat = supportsStandardStreamMetadataFormat;
        this.nativeStreamMetadataFormatName = nativeStreamMetadataFormatName;
        this.nativeStreamMetadataFormatClassName = nativeStreamMetadataFormatClassName;
        this.extraStreamMetadataFormatNames = extraStreamMetadataFormatNames == null ? null
                : extraStreamMetadataFormatNames.clone();
        this.extraStreamMetadataFormatClassNames = extraStreamMetadataFormatClassNames == null ? null
                : extraStreamMetadataFormatClassNames.clone();
        this.supportsStandardImageMetadataFormat = supportsStandardImageMetadataFormat;
        this.nativeImageMetadataFormatName = nativeImageMetadataFormatName;
        this.nativeImageMetadataFormatClassName = nativeImageMetadataFormatClassName;
        this.extraImageMetadataFormatNames = extraImageMetadataFormatNames == null ? null
                : extraImageMetadataFormatNames.clone();
        this.extraImageMetadataFormatClassNames = extraImageMetadataFormatClassNames == null ? null
                : extraImageMetadataFormatClassNames.clone();
    }
    public ImageReaderWriterSpi() {
    }
    public String[] getFormatNames() {
        return names.clone();
    }
    public String[] getFileSuffixes() {
        return suffixes == null ? null : suffixes.clone();
    }
    public String[] getExtraImageMetadataFormatNames() {
        return extraImageMetadataFormatNames == null ? null : extraImageMetadataFormatNames.clone();
    }
    public String[] getExtraStreamMetadataFormatNames() {
        return extraStreamMetadataFormatNames == null ? null : extraStreamMetadataFormatNames
                .clone();
    }
    public IIOMetadataFormat getImageMetadataFormat(String formatName) {
        return IIOMetadataUtils.instantiateMetadataFormat(formatName,
                supportsStandardImageMetadataFormat, nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName, extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames);
    }
    public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
        return IIOMetadataUtils.instantiateMetadataFormat(formatName,
                supportsStandardStreamMetadataFormat, nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName, extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames);
    }
    public String[] getMIMETypes() {
        return MIMETypes == null ? null : MIMETypes.clone();
    }
    public String getNativeImageMetadataFormatName() {
        return nativeImageMetadataFormatName;
    }
    public String getNativeStreamMetadataFormatName() {
        return nativeStreamMetadataFormatName;
    }
    public String getPluginClassName() {
        return pluginClassName;
    }
    public boolean isStandardImageMetadataFormatSupported() {
        return supportsStandardImageMetadataFormat;
    }
    public boolean isStandardStreamMetadataFormatSupported() {
        return supportsStandardStreamMetadataFormat;
    }
}
