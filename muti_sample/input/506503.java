public class SAXParserFactoryImpl extends SAXParserFactory {
    private static final String NAMESPACES
            = "http:
    private static final String VALIDATION
            = "http:
    private Map<String, Boolean> features = new HashMap<String, Boolean>();
    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!name.startsWith("http:
            throw new SAXNotRecognizedException(name);
        }
        return Boolean.TRUE.equals(features.get(name));
    }
    @Override
    public boolean isNamespaceAware() {
        try {
            return getFeature(NAMESPACES);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }
    @Override
    public boolean isValidating() {
        try {
            return getFeature(VALIDATION);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }
    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException {
        if (isValidating()) {
            throw new ParserConfigurationException(
                    "No validating SAXParser implementation available");
        }
        try {
            return new SAXParserImpl(features);
        } catch (Exception ex) {
            throw new ParserConfigurationException(ex.toString());
        }
    }
    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (!name.startsWith("http:
            throw new SAXNotRecognizedException(name);
        }
        if (value) {
            features.put(name, Boolean.TRUE);
        } else {
            features.put(name, Boolean.FALSE);
        }
    }
    @Override
    public void setNamespaceAware(boolean value) {
        try {
            setFeature(NAMESPACES, value);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }
    @Override
    public void setValidating(boolean value) {
        try {
            setFeature(VALIDATION, value);
        } catch (SAXNotRecognizedException ex) {
            throw new AssertionError(ex);
        }
    }
}
