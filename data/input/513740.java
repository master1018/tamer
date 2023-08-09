public abstract class TransformerFactory {
    protected TransformerFactory() { }
    public static TransformerFactory newInstance()
            throws TransformerFactoryConfigurationError {
        return new TransformerFactoryImpl();
    }
    public abstract Transformer newTransformer(Source source)
        throws TransformerConfigurationException;
    public abstract Transformer newTransformer()
        throws TransformerConfigurationException;
    public abstract Templates newTemplates(Source source)
        throws TransformerConfigurationException;
    public abstract Source getAssociatedStylesheet(
        Source source,
        String media,
        String title,
        String charset)
        throws TransformerConfigurationException;
    public abstract void setURIResolver(URIResolver resolver);
    public abstract URIResolver getURIResolver();
	public abstract void setFeature(String name, boolean value)
		throws TransformerConfigurationException;
    public abstract boolean getFeature(String name);
    public abstract void setAttribute(String name, Object value);
    public abstract Object getAttribute(String name);
    public abstract void setErrorListener(ErrorListener listener);
    public abstract ErrorListener getErrorListener();
}
