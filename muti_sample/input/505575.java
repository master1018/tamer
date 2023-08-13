public abstract class XPathFactory {
    public static final String DEFAULT_PROPERTY_NAME = "javax.xml.xpath.XPathFactory";
    public static final String DEFAULT_OBJECT_MODEL_URI = "http:
    protected XPathFactory() {
    }
    public static final XPathFactory newInstance() {
    	try {
        	return newInstance(DEFAULT_OBJECT_MODEL_URI);
    	} 
    	catch (XPathFactoryConfigurationException xpathFactoryConfigurationException) {
    		throw new RuntimeException(
    			"XPathFactory#newInstance() failed to create an XPathFactory for the default object model: "
    			+ DEFAULT_OBJECT_MODEL_URI
    			+ " with the XPathFactoryConfigurationException: "
    			+ xpathFactoryConfigurationException.toString()
    		);
    	}
    }
    public static final XPathFactory newInstance(final String uri)
        throws XPathFactoryConfigurationException {
        if (uri == null) {
            throw new NullPointerException(
                "XPathFactory#newInstance(String uri) cannot be called with uri == null"
            );
        }
        if (uri.length() == 0) {
            throw new IllegalArgumentException(
                "XPathFactory#newInstance(String uri) cannot be called with uri == \"\""
            );
        }
        ClassLoader classLoader = SecuritySupport.getContextClassLoader();
        if (classLoader == null) {            
            classLoader = XPathFactory.class.getClassLoader();
        } 
        XPathFactory xpathFactory = new XPathFactoryFinder(classLoader).newFactory(uri);
        if (xpathFactory == null) {
            throw new XPathFactoryConfigurationException(
                "No XPathFctory implementation found for the object model: "
                + uri
            );
        }
        return xpathFactory;
    }
    public static XPathFactory newInstance(String uri, String factoryClassName,
            ClassLoader classLoader) throws XPathFactoryConfigurationException {
        if (uri == null) {
            throw new NullPointerException(
                "XPathFactory#newInstance(String uri) cannot be called with uri == null"
            );
        }
        if (uri.length() == 0) {
            throw new IllegalArgumentException(
                "XPathFactory#newInstance(String uri) cannot be called with uri == \"\""
            );
        }
        if (factoryClassName == null) {
            throw new XPathFactoryConfigurationException("factoryClassName cannot be null.");
        }
        if (classLoader == null) {
            classLoader = SecuritySupport.getContextClassLoader();
        }
        XPathFactory xpathFactory = new XPathFactoryFinder(classLoader).createInstance(factoryClassName);
        if (xpathFactory == null || !xpathFactory.isObjectModelSupported(uri)) {
            throw new XPathFactoryConfigurationException(
                "No XPathFctory implementation found for the object model: "
                + uri
            );
        }
        return xpathFactory;
    }
	public abstract boolean isObjectModelSupported(String objectModel);
	public abstract void setFeature(String name, boolean value)
		throws XPathFactoryConfigurationException;
	public abstract boolean getFeature(String name)
		throws XPathFactoryConfigurationException;
    public abstract void setXPathVariableResolver(XPathVariableResolver resolver);
    public abstract void setXPathFunctionResolver(XPathFunctionResolver resolver);
    public abstract XPath newXPath();
}
