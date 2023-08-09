public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {
    private static final String NAMESPACES =
            "http:
    private static final String VALIDATION =
            "http:
    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        throw new IllegalArgumentException(name);
    }
    @Override
    public boolean getFeature(String name) throws ParserConfigurationException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (NAMESPACES.equals(name)) {
            return isNamespaceAware();
        } else if (VALIDATION.equals(name)) {
            return isValidating();
        } else {
            throw new ParserConfigurationException(name);
        }
    }
    @Override
    public DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException {
        if (isValidating()) {
            throw new ParserConfigurationException(
                    "No validating DocumentBuilder implementation available");
        }
        DocumentBuilderImpl builder = new DocumentBuilderImpl();
        builder.setCoalescing(isCoalescing());
        builder.setIgnoreComments(isIgnoringComments());
        builder.setIgnoreElementContentWhitespace(isIgnoringElementContentWhitespace());
        builder.setNamespaceAware(isNamespaceAware());
        return builder;
    }
    @Override
    public void setAttribute(String name, Object value)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(name);
    }
    @Override
    public void setFeature(String name, boolean value)
            throws ParserConfigurationException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (NAMESPACES.equals(name)) {
            setNamespaceAware(value);
        } else if (VALIDATION.equals(name)) {
            setValidating(value);
        } else {
            throw new ParserConfigurationException(name);
        }
    }
}
