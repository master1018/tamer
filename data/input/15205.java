public final class java_util_HashMap extends AbstractTest<Map<String, String>> {
    public static void main(String[] args) {
        new java_util_HashMap().test(true);
    }
    @Override
    protected Map<String, String> getObject() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(null, null);
        map.put("key", "value");
        map.put("key-null", "null-value");
        map.put("way", "remove");
        return map;
    }
    @Override
    protected Map<String, String> getAnotherObject() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(null, "null-value");
        map.put("key", "value");
        map.put("key-null", null);
        return map;
    }
    @Override
    protected void validate(Map<String, String> before, Map<String, String> after) {
        super.validate(before, after);
        validate(before);
        validate(after);
    }
    private static void validate(Map<String, String> map) {
        switch (map.size()) {
        case 3:
            validate(map, null, "null-value");
            validate(map, "key", "value");
            validate(map, "key-null", null);
            break;
        case 4:
            validate(map, null, null);
            validate(map, "key", "value");
            validate(map, "key-null", "null-value");
            validate(map, "way", "remove");
            break;
        }
    }
    private static void validate(Map<String, String> map, String key, String value) {
        if (!map.containsKey(key))
            throw new Error("There are no key: " + key);
        if (!map.containsValue(value))
            throw new Error("There are no value: " + value);
        if (!isEqual(value, map.get(key)))
            throw new Error("There are no entry: " + key + ", " + value);
    }
    private static boolean isEqual(String str1, String str2) {
        return (str1 == null)
                ? str2 == null
                : str1.equals(str2);
    }
}
