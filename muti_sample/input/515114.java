public class MockFilter extends XMLFilterImpl {
    private MethodLogger logger;
    private Set<String> features = new HashSet<String>();
    private Map<String, Object> properties = new HashMap<String, Object>();
    public MockFilter(MethodLogger logger) {
        super();
        this.logger = logger;
    }
    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return features.contains(name);
    }
    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return properties.get(name);
    }
    @Override
    public void setFeature(String name, boolean value) {
        if (value) {
            features.add(name);
        } else {
            features.remove(name);
        }
    }
    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }
    @Override
    public void parse(InputSource input) throws SAXException, IOException {
        logger.add("parse", input);
    }
    @Override
    public void parse(String systemId) throws SAXException, IOException {
        logger.add("parse", systemId);
    }
}
