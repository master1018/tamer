public abstract class ImageInputStreamSpi extends IIOServiceProvider implements RegisterableService {
    protected Class<?> inputClass;
    protected ImageInputStreamSpi() {
        throw new UnsupportedOperationException("Not supported yet");
    }
    public ImageInputStreamSpi(String vendorName, String version, Class<?> inputClass) {
        super(vendorName, version);
        this.inputClass = inputClass;
    }
    public Class<?> getInputClass() {
        return inputClass;
    }
    public boolean canUseCacheFile() {
        return false; 
    }
    public boolean needsCacheFile() {
        return false; 
    }
    public abstract ImageInputStream createInputStreamInstance(Object input, boolean useCache,
            File cacheDir) throws IOException;
    public ImageInputStream createInputStreamInstance(Object input) throws IOException {
        return createInputStreamInstance(input, true, null);
    }
}
