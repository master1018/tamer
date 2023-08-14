public class Test6860438
{
    static final String KEY = "Test6860438.key";
    static final String VALUE = "Test6860438.value";
    void check(Object key, Object value, boolean present, int size) {
        check(UIManager.get(key) == value, "UIManager.get()");
        check(UIManager.getDefaults().size() == size, "MultiUIDefaults.size()");
        checkEnumeration(UIManager.getDefaults().keys(),
                key, present, "MultiUIDefaults.keys()");
        checkEnumeration(UIManager.getDefaults().elements(),
                value, present, "MultiUIDefaults.elements()");
        boolean found = false;
        Set<Entry<Object, Object>> entries = UIManager.getDefaults().entrySet();
        for (Entry<Object, Object> e: entries) {
            if (e.getKey() == key) {
                check(e.getValue() == value, "MultiUIDefaults.entrySet()");
                found = true;
            }
        }
        check(found == present, "MultiUIDefaults.entrySet()");
    }
    void checkEnumeration(Enumeration<Object> e, Object elem,
            boolean present, String error) {
        boolean found = false;
        while (e.hasMoreElements()) {
            if (e.nextElement() == elem) {
                found = true;
            }
        }
        check(found == present, error);
    }
    void check(boolean condition, String methodName) {
        if (! condition) {
            throw new RuntimeException(methodName + " failed");
        }
    }
    void test() {
        int size = UIManager.getDefaults().size();
        UIManager.getLookAndFeelDefaults().put(KEY, VALUE);
        check(KEY, VALUE, true, size + 1);
        UIManager.put(KEY, VALUE);
        check(KEY, VALUE, true, size + 1);
        UIManager.getDefaults().remove(KEY);
        check(KEY, null, false, size);
    }
    public static void main(String[] args) {
        new Test6860438().test();
    }
}
