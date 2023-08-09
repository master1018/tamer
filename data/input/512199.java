public class FileIISSpi extends ImageInputStreamSpi {
    private static final String vendor = "Apache";
    private static final String ver = "0.1";
    public FileIISSpi() {
        super(vendor, ver, File.class);
    }
    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache,
            File cacheDir) throws IOException {
        if (File.class.isInstance(input)) {
            return new FileImageInputStream((File) input);
        }
        throw new IllegalArgumentException("input is not an instance of java.io.File");
    }
    @Override
    public String getDescription(Locale locale) {
        return "File IIS Spi";
    }
}
