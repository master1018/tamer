public abstract class ImageTranscoderSpi extends IIOServiceProvider implements RegisterableService {
    protected ImageTranscoderSpi() {
    }
    public ImageTranscoderSpi(String vendorName, String version) {
        super(vendorName, version);
    }
    public abstract String getReaderServiceProviderName();
    public abstract String getWriterServiceProviderName();
    public abstract ImageTranscoder createTranscoderInstance();
}
