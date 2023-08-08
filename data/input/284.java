public class InputFactory {
    private static final Logger logger = Logger.getLogger(InputFactory.class);
    public static MediaDescriptor getMediaDescriptor(File file) {
        if (file == null || file.isDirectory()) return null;
        JmfMediaDescriptor retr = new JmfMediaDescriptor();
        Processor processor = null;
        try {
            URL mediaURL = new URL(file.toURI().toString());
            processor = Manager.createProcessor(mediaURL);
            if (processor != null) {
                if (processor instanceof MediaProcessor) {
                    MediaProcessor mp = (MediaProcessor) processor;
                    retr.setMimeType(mp.getContentType().replaceAll("\\.", "/"));
                } else {
                    retr.setMimeType("application/unknown");
                }
                JmfMediaContainer container = new JmfMediaContainer();
                retr.getContainers().add(container);
            }
        } catch (Throwable t) {
            logger.error("Couldn't process media type requested", t);
        } finally {
            if (processor != null) {
                processor.close();
                processor.deallocate();
                processor = null;
            }
        }
        return retr;
    }
}
