public class SAXParserImpl
    extends SAXParser
{
    final org.ccil.cowan.tagsoup.Parser parser;
    protected SAXParserImpl() 
    {
        super();
        parser = new org.ccil.cowan.tagsoup.Parser();
    }
    public static SAXParserImpl newInstance(Map features)
        throws SAXException
    {
        SAXParserImpl parser = new SAXParserImpl();
        if (features != null) {
            Iterator it = features.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                parser.setFeature((String) entry.getKey(), ((Boolean) entry.getValue()).booleanValue());
            }
        }
        return parser;
    }
    public org.xml.sax.Parser getParser()
        throws SAXException
    {
        return new SAX1ParserAdapter(parser);
    }
    public XMLReader getXMLReader() { return parser; }
    public boolean isNamespaceAware()
    {
        try {
            return parser.getFeature(Parser.namespacesFeature);
        } catch (SAXException sex) { 
            throw new RuntimeException(sex.getMessage());
        }
    }
    public boolean isValidating()
    {
        try {
            return parser.getFeature(Parser.validationFeature);
        } catch (SAXException sex) { 
            throw new RuntimeException(sex.getMessage());
        }
    }
    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        parser.setProperty(name, value);
    }
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return parser.getProperty(name);
    }
    public void setFeature(String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        parser.setFeature(name, value);
    }
    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return parser.getFeature(name);
    }
}
