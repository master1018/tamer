public abstract class SAXTransformerFactory extends TransformerFactory {
    public static final String FEATURE =
        "http:
    public static final String FEATURE_XMLFILTER =
        "http:
    protected SAXTransformerFactory() {}
    public abstract TransformerHandler newTransformerHandler(Source src)
        throws TransformerConfigurationException;
    public abstract TransformerHandler newTransformerHandler(
        Templates templates) throws TransformerConfigurationException;
    public abstract TransformerHandler newTransformerHandler()
        throws TransformerConfigurationException;
    public abstract TemplatesHandler newTemplatesHandler()
        throws TransformerConfigurationException;
    public abstract XMLFilter newXMLFilter(Source src)
        throws TransformerConfigurationException;
    public abstract XMLFilter newXMLFilter(Templates templates)
        throws TransformerConfigurationException;
}
