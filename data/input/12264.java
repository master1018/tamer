public class SimpleBindings implements Bindings {
    private Map<String,Object> map;
    public SimpleBindings(Map<String,Object> m) {
        if (m == null) {
            throw new NullPointerException();
        }
        this.map = m;
    }
    public SimpleBindings() {
        this(new HashMap<String,Object>());
    }
    public Object put(String name, Object value) {
        checkKey(name);
        return map.put(name,value);
    }
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        if (toMerge == null) {
            throw new NullPointerException("toMerge map is null");
        }
        for (Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
            String key = entry.getKey();
            checkKey(key);
            put(key, entry.getValue());
        }
    }
    public void clear() {
        map.clear();
    }
    public boolean containsKey(Object key) {
        checkKey(key);
        return map.containsKey(key);
    }
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }
    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
    public Object get(Object key) {
        checkKey(key);
        return map.get(key);
    }
    public boolean isEmpty() {
        return map.isEmpty();
    }
    public Set<String> keySet() {
        return map.keySet();
    }
    public Object remove(Object key) {
        checkKey(key);
        return map.remove(key);
    }
    public int size() {
        return map.size();
    }
    public Collection<Object> values() {
        return map.values();
    }
    private void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("key should be a String");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can not be empty");
        }
    }
}
