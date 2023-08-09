public class FileIOSSpi extends ImageOutputStreamSpi {
    private static final String vendor = "Apache";
    private static final String ver = "0.1";
    public FileIOSSpi() {
        super(vendor, ver, File.class);
    }
    @Override
    public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache,
            File cacheDir) throws IOException {
        if (output instanceof File) {
            return new FileImageOutputStream((File) output);
        }
        throw new IllegalArgumentException("output is not instance of File");
    }
    @Override
    public String getDescription(Locale locale) {
        return "File IOS Spi";
    }
}
