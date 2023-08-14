public class SetValue {
    static final String key      = "key";
    static final String oldValue = "old";
    static final String newValue = "new";
    public static void main(String args[]) throws Exception {
        Map m = new HashMap();
        m.put(key, oldValue);
        Map.Entry e = (Map.Entry) m.entrySet().iterator().next();
        Object returnVal = e.setValue(newValue);
        if (!returnVal.equals(oldValue))
            throw new RuntimeException("Return value: " + returnVal);
    }
}
