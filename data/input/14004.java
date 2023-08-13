public class DOMCryptoContext implements XMLCryptoContext {
    private HashMap nsMap = new HashMap();
    private HashMap idMap = new HashMap();
    private HashMap objMap = new HashMap();
    private String baseURI;
    private KeySelector ks;
    private URIDereferencer dereferencer;
    private HashMap propMap = new HashMap();
    private String defaultPrefix;
    protected DOMCryptoContext() {}
    public String getNamespacePrefix(String namespaceURI,
        String defaultPrefix) {
        if (namespaceURI == null) {
            throw new NullPointerException("namespaceURI cannot be null");
        }
        String prefix = (String) nsMap.get(namespaceURI);
        return (prefix != null ? prefix : defaultPrefix);
    }
    public String putNamespacePrefix(String namespaceURI, String prefix) {
        if (namespaceURI == null) {
            throw new NullPointerException("namespaceURI is null");
        }
        return (String) nsMap.put(namespaceURI, prefix);
    }
    public String getDefaultNamespacePrefix() {
        return defaultPrefix;
    }
    public void setDefaultNamespacePrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }
    public String getBaseURI() {
        return baseURI;
    }
    public void setBaseURI(String baseURI) {
        if (baseURI != null) {
            java.net.URI.create(baseURI);
        }
        this.baseURI = baseURI;
    }
    public URIDereferencer getURIDereferencer() {
        return dereferencer;
    }
    public void setURIDereferencer(URIDereferencer dereferencer) {
        this.dereferencer = dereferencer;
    }
    public Object getProperty(String name) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return propMap.get(name);
    }
    public Object setProperty(String name, Object value) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }
        return propMap.put(name, value);
    }
    public KeySelector getKeySelector() {
        return ks;
    }
    public void setKeySelector(KeySelector ks) {
        this.ks = ks;
    }
    public Element getElementById(String idValue) {
        if (idValue == null) {
            throw new NullPointerException("idValue is null");
        }
        return (Element) idMap.get(idValue);
    }
    public void setIdAttributeNS(Element element, String namespaceURI,
        String localName) {
        if (element == null) {
            throw new NullPointerException("element is null");
        }
        if (localName == null) {
            throw new NullPointerException("localName is null");
        }
        String idValue = element.getAttributeNS(namespaceURI, localName);
        if (idValue == null || idValue.length() == 0) {
            throw new IllegalArgumentException(localName + " is not an " +
                "attribute");
        }
        idMap.put(idValue, element);
    }
    public Iterator iterator() {
        return Collections.unmodifiableMap(idMap).entrySet().iterator();
    }
    public Object get(Object key) {
        return objMap.get(key);
    }
    public Object put(Object key, Object value) {
        return objMap.put(key, value);
    }
}
