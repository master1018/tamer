public final class java_util_Collections_SynchronizedMap extends AbstractTest<Map<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_SynchronizedMap().test(true);
    }
    protected Map<String, String> getObject() {
        Map<String, String> map = Collections.singletonMap("key", "value");
        return Collections.synchronizedMap(map);
    }
    protected Map<String, String> getAnotherObject() {
        Map<String, String> map = Collections.emptyMap();
        return Collections.synchronizedMap(map);
    }
}
