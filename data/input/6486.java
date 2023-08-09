public abstract class ImageOutputStreamSpi extends IIOServiceProvider {
    protected Class<?> outputClass;
    protected ImageOutputStreamSpi() {
    }
    public ImageOutputStreamSpi(String vendorName,
                                String version,
                                Class<?> outputClass) {
        super(vendorName, version);
        this.outputClass = outputClass;
    }
    public Class<?> getOutputClass() {
        return outputClass;
    }
    public boolean canUseCacheFile() {
        return false;
    }
    public boolean needsCacheFile() {
        return false;
    }
    public abstract
        ImageOutputStream createOutputStreamInstance(Object output,
                                                     boolean useCache,
                                                     File cacheDir)
        throws IOException;
    public ImageOutputStream createOutputStreamInstance(Object output)
        throws IOException {
        return createOutputStreamInstance(output, true, null);
    }
}
