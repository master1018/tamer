public abstract class SchemaFactory {
    protected SchemaFactory() {
    }
    public static final SchemaFactory newInstance(String schemaLanguage) {
        ClassLoader cl;        
        cl = SecuritySupport.getContextClassLoader();
        if (cl == null) {
            cl = SchemaFactory.class.getClassLoader();
        } 
        SchemaFactory f = new SchemaFactoryFinder(cl).newFactory(schemaLanguage);
        if (f == null) {
            throw new IllegalArgumentException(schemaLanguage);
        }
        return f;
    }
	public abstract boolean isSchemaLanguageSupported(String schemaLanguage);
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
        	throw new NullPointerException("the name parameter is null");
        } 
        throw new SAXNotRecognizedException(name);
    }
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
        	throw new NullPointerException("the name parameter is null");
        } 
        throw new SAXNotRecognizedException(name);
    }
    public void setProperty(String name, Object object) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
        	throw new NullPointerException("the name parameter is null");
        } 
        throw new SAXNotRecognizedException(name);
    }
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
        	throw new NullPointerException("the name parameter is null");
        } 
        throw new SAXNotRecognizedException(name);
    }
    public abstract void setErrorHandler(ErrorHandler errorHandler);
    public abstract ErrorHandler getErrorHandler();
    public abstract void setResourceResolver(LSResourceResolver resourceResolver);
    public abstract LSResourceResolver getResourceResolver();
    public Schema newSchema(Source schema) throws SAXException {
    	return newSchema(new Source[]{schema});
    }
    public Schema newSchema(File schema) throws SAXException {
        return newSchema(new StreamSource(schema));
    }
    public Schema newSchema(URL schema) throws SAXException {
        return newSchema(new StreamSource(schema.toExternalForm()));
    }
    public abstract Schema newSchema(Source[] schemas) throws SAXException;
    public abstract Schema newSchema() throws SAXException;
}
