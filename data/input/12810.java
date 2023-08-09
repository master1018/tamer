public final class XPathFilterParameterSpec implements TransformParameterSpec {
    private String xPath;
    private Map nsMap;
    public XPathFilterParameterSpec(String xPath) {
        if (xPath == null) {
            throw new NullPointerException();
        }
        this.xPath = xPath;
        this.nsMap = Collections.EMPTY_MAP;
    }
    public XPathFilterParameterSpec(String xPath, Map namespaceMap) {
        if (xPath == null || namespaceMap == null) {
            throw new NullPointerException();
        }
        this.xPath = xPath;
        nsMap = new HashMap(namespaceMap);
        Iterator entries = nsMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry me = (Map.Entry) entries.next();
            if (!(me.getKey() instanceof String) ||
                !(me.getValue() instanceof String)) {
                throw new ClassCastException("not a String");
            }
        }
        nsMap = Collections.unmodifiableMap(nsMap);
    }
    public String getXPath() {
        return xPath;
    }
    public Map getNamespaceMap() {
        return nsMap;
    }
}
