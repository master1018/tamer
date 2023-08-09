public class SAXResult implements Result {
    public static final String FEATURE =
        "http:
    public SAXResult() {
    }
    public SAXResult(ContentHandler handler) {
        setHandler(handler);
    }
    public void setHandler(ContentHandler handler) {
        this.handler = handler;
    }
    public ContentHandler getHandler() {
        return handler;
    }
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexhandler = handler;
    }
    public LexicalHandler getLexicalHandler() {
        return lexhandler;
    }
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }
    public String getSystemId() {
        return systemId;
    }
    private ContentHandler handler;
    private LexicalHandler lexhandler;
    private String systemId;
}
