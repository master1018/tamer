public class KeySetTest {
    static final List<String> fullKeys = Arrays.asList("food", "drink", "tea");
    static final List<String> localKeys = Arrays.asList("food", "tea");
    public static void main(String[] args) {
        testKeys("KeySetResources", Locale.JAPAN);
        testKeys("KeySetMessages", Locale.CHINA);
    }
    static void testKeys(String bundleName, Locale locale) {
        ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
        System.out.println("food = " + rb.getString("food"));
        Set<String> allKeys = rb.keySet();
        if (!(allKeys.containsAll(fullKeys) && fullKeys.containsAll(allKeys))) {
            throw new RuntimeException("got "+allKeys + ", expected " + fullKeys);
        }
        for (String key : fullKeys) {
            if (!rb.containsKey(key)) {
                throw new RuntimeException("rb doesn't contain: " + key);
            }
        }
        for (String key : new String[] { "snack", "beer" }) {
            if (rb.containsKey(key)) {
                throw new RuntimeException("rb contains: " + key);
            }
        }
        TestBundle tb = new TestBundle(bundleName, locale);
        Set<String> childKeys = tb.handleKeySet();
        if (!(childKeys.containsAll(localKeys) || localKeys.containsAll(childKeys))) {
            throw new RuntimeException("get " + childKeys + ", expected " + localKeys);
        }
    }
    static class TestBundle extends ResourceBundle {
        ResourceBundle bundle;
        Method m;
        public TestBundle() {}
        public TestBundle(String name, Locale locale) {
            bundle = ResourceBundle.getBundle(name, locale);
            try {
                Class clazz = bundle.getClass();
                m = clazz.getMethod("handleGetObject", String.class);
                m.setAccessible(true);
            } catch (Exception e) {
                throw new RuntimeException("method preparation error", e);
            }
        }
        public Enumeration<String> getKeys() {
            return bundle.getKeys();
        }
        protected Object handleGetObject(String key) {
            try {
                return m.invoke(bundle, key);
            } catch (Exception e) {
                throw new RuntimeException("handleGetObject error", e);
            }
        }
        public Set<String> handleKeySet() {
            return super.handleKeySet();
        }
    }
}
