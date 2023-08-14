public class OutputStreamIOSSpi extends ImageOutputStreamSpi {
    private static final String vendor = "Apache";
    private static final String ver = "0.1";
    public OutputStreamIOSSpi() {
        super(vendor, ver, OutputStream.class);
    }
    @Override
    public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir) throws IOException {
        if (output instanceof OutputStream) {
            if (useCache) {
                return new FileCacheImageOutputStream((OutputStream) output, cacheDir);
            } else {
                return new MemoryCacheImageOutputStream((OutputStream) output);
            }
        }
        throw new IllegalArgumentException("Output is not an instance of OutputStream");
    }
    @Override
    public String getDescription(Locale locale) {
        return "Output Stream IOS Spi";
    }
    @Override
    public boolean canUseCacheFile() {
        return true;
    }
}
