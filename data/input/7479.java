public abstract class OpenListResourceBundle extends ResourceBundle {
    protected OpenListResourceBundle() {
    }
    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        loadLookupTablesIfNecessary();
        return lookup.get(key); 
    }
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(handleGetKeys(),
                (parent != null) ? parent.getKeys() : null);
    }
    public Set<String> handleGetKeys() {
        loadLookupTablesIfNecessary();
        return lookup.keySet();
    }
    public OpenListResourceBundle getParent() {
        return (OpenListResourceBundle)parent;
    }
    abstract protected Object[][] getContents();
    void loadLookupTablesIfNecessary() {
        if (lookup == null) {
            loadLookup();
        }
    }
    private synchronized void loadLookup() {
        if (lookup != null)
            return;
        Object[][] contents = getContents();
        Map temp = createMap(contents.length);
        for (int i = 0; i < contents.length; ++i) {
            String key = (String) contents[i][0];
            Object value = contents[i][1];
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            temp.put(key, value);
        }
        lookup = temp;
    }
    protected Map createMap(int size) {
        return new HashMap(size);
    }
    private Map lookup = null;
}
