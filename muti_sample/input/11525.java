public class XPathType {
    public static class Filter {
        private final String operation;
        private Filter(String operation) {
            this.operation = operation;
        }
        public String toString() {
            return operation;
        }
        public static final Filter INTERSECT = new Filter("intersect");
        public static final Filter SUBTRACT = new Filter("subtract");
        public static final Filter UNION = new Filter("union");
    }
    private final String expression;
    private final Filter filter;
    private Map nsMap;
    public XPathType(String expression, Filter filter) {
        if (expression == null) {
            throw new NullPointerException("expression cannot be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter cannot be null");
        }
        this.expression = expression;
        this.filter = filter;
        this.nsMap = Collections.EMPTY_MAP;
    }
    public XPathType(String expression, Filter filter, Map namespaceMap) {
        this(expression, filter);
        if (namespaceMap == null) {
            throw new NullPointerException("namespaceMap cannot be null");
        }
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
    public String getExpression() {
        return expression;
    }
    public Filter getFilter() {
        return filter;
    }
    public Map getNamespaceMap() {
        return nsMap;
    }
}
