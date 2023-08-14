public abstract class SAXParserFactory {
    private boolean validating = false;
    private boolean namespaceAware = false;
    protected SAXParserFactory () {
    }
    public static SAXParserFactory newInstance() {
        return new SAXParserFactoryImpl();
    }
    public abstract SAXParser newSAXParser()
        throws ParserConfigurationException, SAXException;
    public void setNamespaceAware(boolean awareness) {
        this.namespaceAware = awareness;
    }
    public void setValidating(boolean validating) {
        this.validating = validating;
    }
    public boolean isNamespaceAware() {
        return namespaceAware;
    }
    public boolean isValidating() {
        return validating;
    }
    public abstract void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException;
    public abstract boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException;
    public Schema getSchema() {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
    public void setSchema(Schema schema) {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
    public void setXIncludeAware(final boolean state) {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
}
