public class InputStreamIISSpi extends ImageInputStreamSpi {
    private static final String vendor = "Apache";
    private static final String ver = "0.1";
    public InputStreamIISSpi() {
        super(vendor, ver, InputStream.class);
    }
    @Override
    public String getDescription(Locale locale) {
        return "Output Stream IOS Spi";
    }
    @Override
    public boolean canUseCacheFile() {
        return true;
    }
    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) throws IOException {
        if (input instanceof InputStream) {
            if (useCache) {
                return new FileCacheImageInputStream((InputStream) input, cacheDir);
            } else {
                return new MemoryCacheImageInputStream((InputStream) input);
            }
        }
        throw new IllegalArgumentException("Output is not an instance of InputStream");
    }
}
