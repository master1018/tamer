public final class java_util_Collections_UnmodifiableMap extends AbstractTest<Map<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_UnmodifiableMap().test(true);
    }
    protected Map<String, String> getObject() {
        Map<String, String> map = Collections.singletonMap("key", "value");
        return Collections.unmodifiableMap(map);
    }
    protected Map<String, String> getAnotherObject() {
        Map<String, String> map = Collections.emptyMap();
        return Collections.unmodifiableMap(map);
    }
}
