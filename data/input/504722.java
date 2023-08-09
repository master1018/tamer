public class XMLReaderAdapter implements Parser, ContentHandler
{
    public XMLReaderAdapter ()
      throws SAXException
    {
    setup(XMLReaderFactory.createXMLReader());
    }
    public XMLReaderAdapter (XMLReader xmlReader)
    {
    setup(xmlReader);
    }
    private void setup (XMLReader xmlReader)
    {
    if (xmlReader == null) {
        throw new NullPointerException("XMLReader must not be null");
    }
    this.xmlReader = xmlReader;
    qAtts = new AttributesAdapter();
    }
    public void setLocale (Locale locale)
    throws SAXException
    {
    throw new SAXNotSupportedException("setLocale not supported");
    }
    public void setEntityResolver (EntityResolver resolver)
    {
    xmlReader.setEntityResolver(resolver);
    }
    public void setDTDHandler (DTDHandler handler)
    {
    xmlReader.setDTDHandler(handler);
    }
    public void setDocumentHandler (DocumentHandler handler)
    {
    documentHandler = handler;
    }
    public void setErrorHandler (ErrorHandler handler)
    {
    xmlReader.setErrorHandler(handler);
    }
    public void parse (String systemId)
    throws IOException, SAXException
    {
    parse(new InputSource(systemId));
    }
    public void parse (InputSource input)
    throws IOException, SAXException
    {
    setupXMLReader();
    xmlReader.parse(input);
    }
    private void setupXMLReader ()
    throws SAXException
    {
    xmlReader.setFeature("http:
    try {
        xmlReader.setFeature("http:
                             false);
    } catch (SAXException e) {
    }
    xmlReader.setContentHandler(this);
    }
    public void setDocumentLocator (Locator locator)
    {
    if (documentHandler != null)
        documentHandler.setDocumentLocator(locator);
    }
    public void startDocument ()
    throws SAXException
    {
    if (documentHandler != null)
        documentHandler.startDocument();
    }
    public void endDocument ()
    throws SAXException
    {
    if (documentHandler != null)
        documentHandler.endDocument();
    }
    public void startPrefixMapping (String prefix, String uri)
    {
    }
    public void endPrefixMapping (String prefix)
    {
    }
    public void startElement (String uri, String localName,
                  String qName, Attributes atts)
    throws SAXException
    {
    if (documentHandler != null) {
        qAtts.setAttributes(atts);
        documentHandler.startElement(qName, qAtts);
    }
    }
    public void endElement (String uri, String localName,
                String qName)
    throws SAXException
    {
    if (documentHandler != null)
        documentHandler.endElement(qName);
    }
    public void characters (char ch[], int start, int length)
    throws SAXException
    {
    if (documentHandler != null)
        documentHandler.characters(ch, start, length);
    }
    public void ignorableWhitespace (char ch[], int start, int length)
    throws SAXException
    {
    if (documentHandler != null)
        documentHandler.ignorableWhitespace(ch, start, length);
    }
    public void processingInstruction (String target, String data)
    throws SAXException
    {
    if (documentHandler != null)
        documentHandler.processingInstruction(target, data);
    }
    public void skippedEntity (String name)
    throws SAXException
    {
    }
    XMLReader xmlReader;
    DocumentHandler documentHandler;
    AttributesAdapter qAtts;
    final class AttributesAdapter implements AttributeList
    {
    AttributesAdapter ()
    {
    }
    void setAttributes (Attributes attributes)
    {
        this.attributes = attributes;
    }
    public int getLength ()
    {
        return attributes.getLength();
    }
    public String getName (int i)
    {
        return attributes.getQName(i);
    }
    public String getType (int i)
    {
        return attributes.getType(i);
    }
    public String getValue (int i)
    {
        return attributes.getValue(i);
    }
    public String getType (String qName)
    {
        return attributes.getType(qName);
    }
    public String getValue (String qName)
    {
        return attributes.getValue(qName);
    }
    private Attributes attributes;
    }
}
