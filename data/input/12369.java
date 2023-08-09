public abstract class ImageReaderWriterSpi extends IIOServiceProvider {
    protected String[] names = null;
    protected String[] suffixes = null;
    protected String[] MIMETypes = null;
    protected String pluginClassName = null;
    protected boolean supportsStandardStreamMetadataFormat = false;
    protected String nativeStreamMetadataFormatName = null;
    protected String nativeStreamMetadataFormatClassName = null;
    protected String[] extraStreamMetadataFormatNames = null;
    protected String[] extraStreamMetadataFormatClassNames = null;
    protected boolean supportsStandardImageMetadataFormat = false;
    protected String nativeImageMetadataFormatName = null;
    protected String nativeImageMetadataFormatClassName = null;
    protected String[] extraImageMetadataFormatNames = null;
    protected String[] extraImageMetadataFormatClassNames = null;
    public ImageReaderWriterSpi(String vendorName,
                                String version,
                                String[] names,
                                String[] suffixes,
                                String[] MIMETypes,
                                String pluginClassName,
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
        super(vendorName, version);
        if (names == null) {
            throw new IllegalArgumentException("names == null!");
        }
        if (names.length == 0) {
            throw new IllegalArgumentException("names.length == 0!");
        }
        if (pluginClassName == null) {
            throw new IllegalArgumentException("pluginClassName == null!");
        }
        this.names = (String[])names.clone();
        if (suffixes != null && suffixes.length > 0) {
            this.suffixes = (String[])suffixes.clone();
        }
        if (MIMETypes != null && MIMETypes.length > 0) {
            this.MIMETypes = (String[])MIMETypes.clone();
        }
        this.pluginClassName = pluginClassName;
        this.supportsStandardStreamMetadataFormat =
            supportsStandardStreamMetadataFormat;
        this.nativeStreamMetadataFormatName = nativeStreamMetadataFormatName;
        this.nativeStreamMetadataFormatClassName =
            nativeStreamMetadataFormatClassName;
        if (extraStreamMetadataFormatNames != null &&
            extraStreamMetadataFormatNames.length > 0) {
            this.extraStreamMetadataFormatNames =
                (String[])extraStreamMetadataFormatNames.clone();
        }
        if (extraStreamMetadataFormatClassNames != null &&
            extraStreamMetadataFormatClassNames.length > 0) {
            this.extraStreamMetadataFormatClassNames =
                (String[])extraStreamMetadataFormatClassNames.clone();
        }
        this.supportsStandardImageMetadataFormat =
            supportsStandardImageMetadataFormat;
        this.nativeImageMetadataFormatName = nativeImageMetadataFormatName;
        this.nativeImageMetadataFormatClassName =
            nativeImageMetadataFormatClassName;
        if (extraImageMetadataFormatNames != null &&
            extraImageMetadataFormatNames.length > 0) {
            this.extraImageMetadataFormatNames =
                (String[])extraImageMetadataFormatNames.clone();
        }
        if (extraImageMetadataFormatClassNames != null &&
            extraImageMetadataFormatClassNames.length > 0) {
            this.extraImageMetadataFormatClassNames =
                (String[])extraImageMetadataFormatClassNames.clone();
        }
    }
    public ImageReaderWriterSpi() {
    }
    public String[] getFormatNames() {
        return (String[])names.clone();
    }
    public String[] getFileSuffixes() {
        return suffixes == null ? null : (String[])suffixes.clone();
    }
    public String[] getMIMETypes() {
        return MIMETypes == null ? null : (String[])MIMETypes.clone();
    }
    public String getPluginClassName() {
        return pluginClassName;
    }
    public boolean isStandardStreamMetadataFormatSupported() {
        return supportsStandardStreamMetadataFormat;
    }
    public String getNativeStreamMetadataFormatName() {
        return nativeStreamMetadataFormatName;
    }
    public String[] getExtraStreamMetadataFormatNames() {
        return extraStreamMetadataFormatNames == null ?
            null : (String[])extraStreamMetadataFormatNames.clone();
    }
    public boolean isStandardImageMetadataFormatSupported() {
        return supportsStandardImageMetadataFormat;
    }
    public String getNativeImageMetadataFormatName() {
        return nativeImageMetadataFormatName;
    }
    public String[] getExtraImageMetadataFormatNames() {
        return extraImageMetadataFormatNames == null ?
            null : (String[])extraImageMetadataFormatNames.clone();
    }
    public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
        return getMetadataFormat(formatName,
                                 supportsStandardStreamMetadataFormat,
                                 nativeStreamMetadataFormatName,
                                 nativeStreamMetadataFormatClassName,
                                 extraStreamMetadataFormatNames,
                                 extraStreamMetadataFormatClassNames);
    }
    public IIOMetadataFormat getImageMetadataFormat(String formatName) {
        return getMetadataFormat(formatName,
                                 supportsStandardImageMetadataFormat,
                                 nativeImageMetadataFormatName,
                                 nativeImageMetadataFormatClassName,
                                 extraImageMetadataFormatNames,
                                 extraImageMetadataFormatClassNames);
    }
    private IIOMetadataFormat getMetadataFormat(String formatName,
                                                boolean supportsStandard,
                                                String nativeName,
                                                String nativeClassName,
                                                String [] extraNames,
                                                String [] extraClassNames) {
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }
        if (supportsStandard && formatName.equals
                (IIOMetadataFormatImpl.standardMetadataFormatName)) {
            return IIOMetadataFormatImpl.getStandardFormatInstance();
        }
        String formatClassName = null;
        if (formatName.equals(nativeName)) {
            formatClassName = nativeClassName;
        } else if (extraNames != null) {
            for (int i = 0; i < extraNames.length; i++) {
                if (formatName.equals(extraNames[i])) {
                    formatClassName = extraClassNames[i];
                    break;  
                }
            }
        }
        if (formatClassName == null) {
            throw new IllegalArgumentException("Unsupported format name");
        }
        try {
            Class cls = Class.forName(formatClassName, true,
                                      ClassLoader.getSystemClassLoader());
            Method meth = cls.getMethod("getInstance");
            return (IIOMetadataFormat) meth.invoke(null);
        } catch (Exception e) {
            RuntimeException ex =
                new IllegalStateException ("Can't obtain format");
            ex.initCause(e);
            throw ex;
        }
    }
}
