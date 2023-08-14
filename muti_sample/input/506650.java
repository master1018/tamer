public class SmilXmlParser {
    private XMLReader mXmlReader;
    private SmilContentHandler mContentHandler;
    public SmilXmlParser() throws MmsException {
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
        try {
            mXmlReader = XMLReaderFactory.createXMLReader();
            mContentHandler = new SmilContentHandler();
            mXmlReader.setContentHandler(mContentHandler);
        } catch (SAXException e) {
            throw new MmsException(e);
        }
    }
    public SMILDocument parse(InputStream in) throws IOException, SAXException {
        mContentHandler.reset();
        mXmlReader.parse(new InputSource(in));
        SMILDocument doc = mContentHandler.getSmilDocument();
        validateDocument(doc);
        return doc;
    }
    private void validateDocument(SMILDocument doc) {
        doc.getBody();
        doc.getLayout();
    }
}
