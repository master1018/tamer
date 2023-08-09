public final class Test4864117 extends DefaultHandler implements ExceptionListener {
    private static final String TEST = "test";
    private static final String DATA
            = "<test>\n"
            + " <void property=\"message\">\n"
            + "  <string>Hello, world!</string>\n"
            + " </void>\n"
            + "</test>";
    public static void main(String[] args) {
        Test4864117 test = new Test4864117();
        InputStream input = new ByteArrayInputStream(DATA.getBytes());
        Exception error = null;
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(input, test);
        }
        catch (ParserConfigurationException exception) {
            error = exception;
        }
        catch (SAXException exception) {
            error = exception.getException();
            if (error == null) {
                error = exception;
            }
        }
        catch (IOException exception) {
            error = exception;
        }
        if (error != null) {
            throw new Error("unexpected error", error);
        }
        test.print('?', test.getMessage());
    }
    private String message;
    public String getMessage() {
        if (this.message == null) {
            throw new Error("owner's method is not called");
        }
        return this.message;
    }
    public void setMessage(String message) {
        this.message = message;
        print(':', this.message);
    }
    private DefaultHandler handler;
    private int depth;
    @Override
    public void startDocument() throws SAXException {
        this.handler = XMLDecoder.createHandler(this, this, null);
        this.handler.startDocument();
    }
    @Override
    public void endDocument() {
        this.handler = null;
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        print('>', qName);
        if (this.depth > 0) {
            this.handler.startElement(uri, localName, qName, attributes);
        } else if (!TEST.equals(qName)) {
            throw new SAXException("unexpected element name: " + qName);
        }
        this.depth++;
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.depth--;
        print('<', qName);
        if (this.depth > 0) {
            this.handler.endElement(uri, localName, qName);
        } else if (!TEST.equals(qName)) {
            throw new SAXException("unexpected element name: " + qName);
        }
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.handler.characters(ch, start, length);
    }
    public void exceptionThrown(Exception exception) {
        throw new Error("unexpected exception", exception);
    }
    private void print(char ch, String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.depth; i++) sb.append(' ');
        sb.append(ch).append(' ').append(name);
        System.out.println(sb.toString());
    }
}
