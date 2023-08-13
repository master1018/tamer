public  class XPathFactoryImpl extends XPathFactory {
	private static final String CLASS_NAME = "XPathFactoryImpl";
	private XPathFunctionResolver xPathFunctionResolver = null;
	private XPathVariableResolver xPathVariableResolver = null;
	private boolean featureSecureProcessing = false;
	public boolean isObjectModelSupported(String objectModel) {
            if (objectModel == null) {
                String fmsg = XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_OBJECT_MODEL_NULL,
                        new Object[] { this.getClass().getName() } );
                throw new NullPointerException( fmsg );
            }
            if (objectModel.length() == 0) {
                String fmsg = XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_OBJECT_MODEL_EMPTY,
                        new Object[] { this.getClass().getName() } );
                throw new IllegalArgumentException( fmsg );
            }
            if (objectModel.equals(XPathFactory.DEFAULT_OBJECT_MODEL_URI)) {
                return true;
            }
            return false;
	}
	public javax.xml.xpath.XPath newXPath() {
	    return new org.apache.xpath.jaxp.XPathImpl(
                    xPathVariableResolver, xPathFunctionResolver,
                    featureSecureProcessing );
	}
	public void setFeature(String name, boolean value)
		throws XPathFactoryConfigurationException {
            if (name == null) {
                String fmsg = XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_FEATURE_NAME_NULL,
                        new Object[] { CLASS_NAME, new Boolean( value) } );
                throw new NullPointerException( fmsg );
             }
            if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
                featureSecureProcessing = value;
                return;
            }
            String fmsg = XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_FEATURE_UNKNOWN,
                    new Object[] { name, CLASS_NAME, new Boolean(value) } );
            throw new XPathFactoryConfigurationException( fmsg );
	}
	public boolean getFeature(String name)
		throws XPathFactoryConfigurationException {
            if (name == null) {
                String fmsg = XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_GETTING_NULL_FEATURE,
                        new Object[] { CLASS_NAME } );
                throw new NullPointerException( fmsg );
            }
            if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
                return featureSecureProcessing;
            }
            String fmsg = XSLMessages.createXPATHMessage(
                    XPATHErrorResources.ER_GETTING_UNKNOWN_FEATURE,
                    new Object[] { name, CLASS_NAME } );
            throw new XPathFactoryConfigurationException( fmsg );
        }
        public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
            if (resolver == null) {
                String fmsg = XSLMessages.createXPATHMessage(
                        XPATHErrorResources.ER_NULL_XPATH_FUNCTION_RESOLVER,
                        new Object[] {  CLASS_NAME } );
                throw new NullPointerException( fmsg );
            }
            xPathFunctionResolver = resolver;
        }
	public void setXPathVariableResolver(XPathVariableResolver resolver) {
		if (resolver == null) {
                    String fmsg = XSLMessages.createXPATHMessage(
                            XPATHErrorResources.ER_NULL_XPATH_VARIABLE_RESOLVER,
                            new Object[] {  CLASS_NAME } );
		    throw new NullPointerException( fmsg );
		}
		xPathVariableResolver = resolver;
	}
}
