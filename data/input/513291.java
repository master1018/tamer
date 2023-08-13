public class XmlPrimitiveParser implements
        PrimitiveParser {
    private XMLReader mXmlReader;
    private PrimitiveContentHandler mContentHandler;
    public XmlPrimitiveParser() throws ImException {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
        try {
            mXmlReader = XMLReaderFactory.createXMLReader();
            mContentHandler = new PrimitiveContentHandler();
            mXmlReader.setContentHandler(mContentHandler);
        } catch (SAXException e) {
            throw new ImException(e);
        }
    }
    public Primitive parse(InputStream in) throws ParserException, IOException {
        mContentHandler.reset();
        try {
            mXmlReader.parse(new InputSource(in));
        } catch (SAXException e) {
            throw new ParserException(e);
        }
        return mContentHandler.getPrimitive();
    }
}
