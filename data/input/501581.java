public class PropertyResourceBundle extends ResourceBundle {
    Properties resources;
    public PropertyResourceBundle(InputStream stream) throws IOException {
        resources = new Properties();
        resources.load(stream);
    }
    @SuppressWarnings("unchecked")
    private Enumeration<String> getLocalKeys() {
        return (Enumeration<String>) resources.propertyNames();
    }
    @Override
    public Enumeration<String> getKeys() {
        if (parent == null) {
            return getLocalKeys();
        }
        return new Enumeration<String>() {
            Enumeration<String> local = getLocalKeys();
            Enumeration<String> pEnum = parent.getKeys();
            String nextElement;
            private boolean findNext() {
                if (nextElement != null) {
                    return true;
                }
                while (pEnum.hasMoreElements()) {
                    String next = pEnum.nextElement();
                    if (!resources.containsKey(next)) {
                        nextElement = next;
                        return true;
                    }
                }
                return false;
            }
            public boolean hasMoreElements() {
                if (local.hasMoreElements()) {
                    return true;
                }
                return findNext();
            }
            public String nextElement() {
                if (local.hasMoreElements()) {
                    return local.nextElement();
                }
                if (findNext()) {
                    String result = nextElement;
                    nextElement = null;
                    return result;
                }
                return pEnum.nextElement();
            }
        };
    }
    @Override
    public Object handleGetObject(String key) {
        return resources.get(key);
    }
}
