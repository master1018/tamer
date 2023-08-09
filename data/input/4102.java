public class SelfRef {
    public static void main(String[] args) {
        testMap(new Hashtable<Object,Object>());
        testMap(new HashMap<Object,Object>());
        testMap(new LinkedHashMap<Object,Object>());
        testMap(new ConcurrentHashMap<Object,Object>());
    }
    private static void testMap(Map<Object,Object> m) {
        if (! (m.toString().equals("{}")))
            throw new Error();
        m.put("Harvey", m);
        if (! (m.toString().equals("{Harvey=(this Map)}")))
            throw new Error();
        m.clear();
        m.put(m, "Harvey");
        if (! (m.toString().equals("{(this Map)=Harvey}")))
            throw new Error();
        m.clear();
        m.hashCode();
    }
}
