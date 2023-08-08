public class FileIconFactoryImpl implements FileIconFactory {
    private Map<String, Asset> factory;
    private Logger logger;
    public FileIconFactoryImpl(Map<String, Asset> contributions, Logger logger) {
        this.factory = contributions;
        this.logger = logger;
    }
    public Asset image(String extension) {
        if (!factory.containsKey(extension)) {
            logger.warn(String.format("No asset founded for file type [%s]", extension));
            return factory.get("application/octet-stream");
        }
        return factory.get(extension);
    }
}
