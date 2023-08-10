public class InxStringMap extends InxValue {
    private Map<String, String> map = new HashMap<String, String>();
    public Map<String, String> getValue() {
        return this.map;
    }
    @Override
    public String toEncodedString() {
        return this.map.toString();
    }
    public void put(String key, String value) {
        this.map.put(key, value);
    }
    public String get(String key) {
        if (this.map.containsKey(key)) {
            return this.map.get(key);
        }
        return null;
    }
    public boolean containsKey(String key) {
        return this.map.containsKey(key);
    }
}
