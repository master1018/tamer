public final class AttributeMap extends AbstractMap<TextAttribute, Object> {
    private AttributeValues values;
    private Map<TextAttribute, Object> delegateMap;
    public AttributeMap(AttributeValues values) {
        this.values = values;
    }
    public Set<Entry<TextAttribute, Object>> entrySet() {
        return delegate().entrySet();
    }
    public Object put(TextAttribute key, Object value) {
        return delegate().put(key, value);
    }
    public AttributeValues getValues() {
        return values;
    }
    private static boolean first = false; 
    private Map<TextAttribute, Object> delegate() {
        if (delegateMap == null) {
            if (first) {
                first = false;
                Thread.dumpStack();
            }
            delegateMap = values.toMap(new HashMap<TextAttribute, Object>(27));
            values = null;
        }
        return delegateMap;
    }
    public String toString() {
        if (values != null) {
            return "map of " + values.toString();
        }
        return super.toString();
    }
}
