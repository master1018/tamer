public class RAFIISSpi extends ImageInputStreamSpi {
    private static final String vendor = "Apache";
    private static final String ver = "0.1";
    public RAFIISSpi() {
        super(vendor, ver, RandomAccessFile.class);
    }
    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache,
            File cacheDir) throws IOException {
        if (RandomAccessFile.class.isInstance(input)) {
            return new FileImageInputStream((RandomAccessFile) input);
        }
        throw new IllegalArgumentException(
                "input is not an instance of java.io.RandomAccessFile");
    }
    @Override
    public String getDescription(Locale locale) {
        return "RandomAccessFile IIS Spi";
    }
}
