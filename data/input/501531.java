public final class IIORegistry extends ServiceRegistry {
    private static IIORegistry instance;
    private static final Class[] CATEGORIES = new Class[] {
            javax.imageio.spi.ImageWriterSpi.class, javax.imageio.spi.ImageReaderSpi.class,
            javax.imageio.spi.ImageInputStreamSpi.class,
            javax.imageio.spi.ImageOutputStreamSpi.class
    };
    private IIORegistry() {
        super(Arrays.<Class<?>> asList(CATEGORIES).iterator());
        registerBuiltinSpis();
        registerApplicationClasspathSpis();
    }
    private void registerBuiltinSpis() {
        registerServiceProvider(new JPEGImageWriterSpi());
        registerServiceProvider(new JPEGImageReaderSpi());
        registerServiceProvider(new PNGImageReaderSpi());
        registerServiceProvider(new PNGImageWriterSpi());
        registerServiceProvider(new FileIOSSpi());
        registerServiceProvider(new FileIISSpi());
        registerServiceProvider(new RAFIOSSpi());
        registerServiceProvider(new RAFIISSpi());
        registerServiceProvider(new OutputStreamIOSSpi());
        registerServiceProvider(new InputStreamIISSpi());
    }
    public static IIORegistry getDefaultInstance() {
        synchronized (IIORegistry.class) {
            if (instance == null) {
                instance = new IIORegistry();
            }
            return instance;
        }
    }
    public void registerApplicationClasspathSpis() {
    }
}
