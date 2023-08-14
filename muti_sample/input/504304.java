public class NoInstanceXMLReader implements XMLReader {
    public NoInstanceXMLReader(int i) {
    }
    public ContentHandler getContentHandler() {
        return null;
    }
    public DTDHandler getDTDHandler() {
        return null;
    }
    public EntityResolver getEntityResolver() {
        return null;
    }
    public ErrorHandler getErrorHandler() {
        return null;
    }
    public boolean getFeature(String name) {
        return false;
    }
    public Object getProperty(String name) {
        return null;
    }
    public void parse(InputSource input) {
    }
    public void parse(String systemId) {
    }
    public void setContentHandler(ContentHandler handler) {
    }
    public void setDTDHandler(DTDHandler handler) {
    }
    public void setEntityResolver(EntityResolver resolver) {
    }
    public void setErrorHandler(ErrorHandler handler) {
    }
    public void setFeature(String name, boolean value) {
    }
    public void setProperty(String name, Object value) {
    }
}
