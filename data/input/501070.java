public class JPEGImageWriterSpi extends ImageWriterSpi {
    public JPEGImageWriterSpi() {
        super(JPEGSpiConsts.vendorName, JPEGSpiConsts.version,
                JPEGSpiConsts.names, JPEGSpiConsts.suffixes, JPEGSpiConsts.MIMETypes,
                JPEGSpiConsts.writerClassName, STANDARD_OUTPUT_TYPE,
                JPEGSpiConsts.readerSpiNames, JPEGSpiConsts.supportsStandardStreamMetadataFormat ,
                JPEGSpiConsts.nativeStreamMetadataFormatName, JPEGSpiConsts.nativeStreamMetadataFormatClassName,
                JPEGSpiConsts.extraStreamMetadataFormatNames, JPEGSpiConsts.extraStreamMetadataFormatClassNames,
                JPEGSpiConsts.supportsStandardImageMetadataFormat, JPEGSpiConsts.nativeImageMetadataFormatName, JPEGSpiConsts.nativeImageMetadataFormatClassName,
                JPEGSpiConsts.extraImageMetadataFormatNames, JPEGSpiConsts.extraImageMetadataFormatClassNames);
    }
    @Override
    public boolean canEncodeImage(ImageTypeSpecifier imageTypeSpecifier) {
        return true;
    }
    @Override
    public ImageWriter createWriterInstance(Object o) throws IOException {
        return new JPEGImageWriter(this);
    }
    @Override
    public String getDescription(Locale locale) {
        return "DRL JPEG Encoder";
    }
}
