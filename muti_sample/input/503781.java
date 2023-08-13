public class RAFIOSSpi extends ImageOutputStreamSpi {
    private static final String vendor = "Apache";
    private static final String ver = "0.1";
    public RAFIOSSpi() {
        super(vendor, ver, RandomAccessFile.class);
    }
    @Override
    public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache,
            File cacheDir) throws IOException {
        if (output instanceof RandomAccessFile) {
            return new FileImageOutputStream((RandomAccessFile) output);
        }
        throw new IllegalArgumentException("output is not instance of java.io.RandomAccessFile");
    }
    @Override
    public String getDescription(Locale locale) {
        return "RandomAccessFile IOS Spi";
    }
}
