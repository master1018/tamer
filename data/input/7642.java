public abstract class LazyCompositeData
        implements CompositeData, Serializable {
    private CompositeData compositeData;
    public boolean containsKey(String key) {
        return compositeData().containsKey(key);
    }
    public boolean containsValue(Object value) {
        return compositeData().containsValue(value);
    }
    public boolean equals(Object obj) {
        return compositeData().equals(obj);
    }
    public Object get(String key) {
        return compositeData().get(key);
    }
    public Object[] getAll(String[] keys) {
        return compositeData().getAll(keys);
    }
    public CompositeType getCompositeType() {
        return compositeData().getCompositeType();
    }
    public int hashCode() {
        return compositeData().hashCode();
    }
    public String toString() {
        return compositeData().toString();
    }
    public Collection values() {
        return compositeData().values();
    }
    private synchronized CompositeData compositeData() {
        if (compositeData != null)
            return compositeData;
        compositeData = getCompositeData();
        return compositeData;
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return compositeData();
    }
    protected abstract CompositeData getCompositeData();
    static String getString(CompositeData cd, String itemName) {
        if (cd == null)
            throw new IllegalArgumentException("Null CompositeData");
        return (String) cd.get(itemName);
    }
    static boolean getBoolean(CompositeData cd, String itemName) {
        if (cd == null)
            throw new IllegalArgumentException("Null CompositeData");
        return ((Boolean) cd.get(itemName)).booleanValue();
    }
    static long getLong(CompositeData cd, String itemName) {
        if (cd == null)
            throw new IllegalArgumentException("Null CompositeData");
        return ((Long) cd.get(itemName)).longValue();
    }
    static int getInt(CompositeData cd, String itemName) {
        if (cd == null)
            throw new IllegalArgumentException("Null CompositeData");
        return ((Integer) cd.get(itemName)).intValue();
    }
    protected static boolean isTypeMatched(CompositeType type1, CompositeType type2) {
        if (type1 == type2) return true;
        Set allItems = type1.keySet();
        if (!type2.keySet().containsAll(allItems))
            return false;
        for (Iterator iter = allItems.iterator(); iter.hasNext(); ) {
            String item = (String) iter.next();
            OpenType ot1 = type1.getType(item);
            OpenType ot2 = type2.getType(item);
            if (ot1 instanceof CompositeType) {
                if (! (ot2 instanceof CompositeType))
                    return false;
                if (!isTypeMatched((CompositeType) ot1, (CompositeType) ot2))
                    return false;
            } else if (ot1 instanceof TabularType) {
                if (! (ot2 instanceof TabularType))
                    return false;
                if (!isTypeMatched((TabularType) ot1, (TabularType) ot2))
                    return false;
            } else if (!ot1.equals(ot2)) {
                return false;
            }
        }
        return true;
    }
    protected static boolean isTypeMatched(TabularType type1, TabularType type2) {
        if (type1 == type2) return true;
        List list1 = type1.getIndexNames();
        List list2 = type2.getIndexNames();
        if (!list1.equals(list2))
            return false;
        return isTypeMatched(type1.getRowType(), type2.getRowType());
    }
    private static final long serialVersionUID = -2190411934472666714L;
}
