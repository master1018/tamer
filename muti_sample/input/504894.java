public class SAXFactoryImpl
    extends SAXParserFactory
{
    private SAXParserImpl prototypeParser = null;
    private HashMap features = null;
    public SAXFactoryImpl()
    {
        super();
    }
    public SAXParser newSAXParser()
        throws ParserConfigurationException
    {
        try {
            return SAXParserImpl.newInstance(features);
        } catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
    }
    public void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException, 
		SAXNotSupportedException
    {
        getPrototype().setFeature(name, value);
        if (features == null) {
            features = new LinkedHashMap();
        }
        features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
    }
    public boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
		SAXNotSupportedException
    {
        return getPrototype().getFeature(name);
    }
    private SAXParserImpl getPrototype()
    {
        if (prototypeParser == null) {
            prototypeParser = new SAXParserImpl();
        }
        return prototypeParser;
    }
}
