public class JPEGImageReaderSpi extends ImageReaderSpi {
    public JPEGImageReaderSpi() {
        super(JPEGSpiConsts.vendorName, JPEGSpiConsts.version,
                JPEGSpiConsts.names, JPEGSpiConsts.suffixes,
                JPEGSpiConsts.MIMETypes, JPEGSpiConsts.readerClassName,
                STANDARD_INPUT_TYPE, JPEGSpiConsts.writerSpiNames,
                JPEGSpiConsts.supportsStandardStreamMetadataFormat,
                JPEGSpiConsts.nativeStreamMetadataFormatName,
                JPEGSpiConsts.nativeStreamMetadataFormatClassName,
                JPEGSpiConsts.extraStreamMetadataFormatNames,
                JPEGSpiConsts.extraStreamMetadataFormatClassNames,
                JPEGSpiConsts.supportsStandardImageMetadataFormat,
                JPEGSpiConsts.nativeImageMetadataFormatName,
                JPEGSpiConsts.nativeImageMetadataFormatClassName,
                JPEGSpiConsts.extraImageMetadataFormatNames,
                JPEGSpiConsts.extraImageMetadataFormatClassNames);
    }
    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        ImageInputStream markable = (ImageInputStream) source;
        try {
            markable.mark();
            byte[] signature = new byte[3];
            markable.seek(0);
            markable.read(signature, 0, 3);
            markable.reset();
            if ((signature[0] & 0xFF) == 0xFF &&
                    (signature[1] & 0xFF) == JPEGConsts.SOI &&
                    (signature[2] & 0xFF) == 0xFF) { 
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new JPEGImageReader(this);
    }
    @Override
    public String getDescription(Locale locale) {
        return "DRL JPEG decoder";
    }
    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
    }
}
