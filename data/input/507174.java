public class MockReader implements XMLReader {
    private MethodLogger logger;
    private ContentHandler contentHandler;
    private DTDHandler dtdHandler;
    private EntityResolver resolver;
    private ErrorHandler errorHandler;
    private Set<String> features = new HashSet<String>();
    private Map<String, Object> properties = new HashMap<String, Object>();
    public MockReader(MethodLogger logger) {
        super();
        this.logger = logger;
    }
    public ContentHandler getContentHandler() {
        return contentHandler;
    }
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }
    public EntityResolver getEntityResolver() {
        return resolver;
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return features.contains(name);
    }
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return properties.get(name);
    }
    public void parse(InputSource input) throws IOException, SAXException {
        logger.add("parse", input);
    }
    public void parse(String systemId) throws IOException, SAXException {
        logger.add("parse", systemId);
    }
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }
    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }
    public void setFeature(String name, boolean value) {
        if (value) {
            features.add(name);
        } else {
            features.remove(name);
        }
    }
    public void setProperty(String name, Object value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }
}
