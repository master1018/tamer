public class MockParser implements Parser {
    private MethodLogger logger;
    public MockParser(MethodLogger logger) {
        super();
        this.logger = logger;
    }
    public void parse(InputSource source) throws SAXException, IOException {
        logger.add("parse", source);
    }
    public void parse(String systemId) throws SAXException, IOException {
        logger.add("parse", systemId);
    }
    public void setDTDHandler(DTDHandler handler) {
        logger.add("setDTDHandler", handler);
    }
    public void setDocumentHandler(DocumentHandler handler) {
        logger.add("setDocumentHandler", handler);
    }
    public void setEntityResolver(EntityResolver resolver) {
        logger.add("setEntityResolver", resolver);
    }
    public void setErrorHandler(ErrorHandler handler) {
        logger.add("setErrorHandler", handler);
    }
    public void setLocale(Locale locale) throws SAXException {
        logger.add("setLocale", locale);
    }
}
