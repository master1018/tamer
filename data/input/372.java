public class NullEntityResolver implements EntityResolver {
    private static final NullEntityResolver INSTANCE = new NullEntityResolver();
    private final InputSource _source;
    private NullEntityResolver() {
        _source = new InputSource();
    }
    public static NullEntityResolver getInstance() {
        return INSTANCE;
    }
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        _source.setCharacterStream(new StringReader(""));
        return _source;
    }
}
