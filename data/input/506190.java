public class WbxmlPrimitiveParser implements PrimitiveParser{
    private WbxmlParser mParser;
    private PrimitiveContentHandler mContentHandler;
    public WbxmlPrimitiveParser() {
        mParser= new WbxmlParser();
        mContentHandler = new PrimitiveContentHandler();
    }
    public Primitive parse(InputStream in) throws ParserException, IOException {
        mContentHandler.reset();
        mParser.reset();
        mParser.setContentHandler(mContentHandler);
        try {
            mParser.parse(new InputSource(in));
        } catch (SAXException e) {
            throw new ParserException(e);
        }
        return mContentHandler.getPrimitive();
    }
}
