public final class java_util_Collections_SingletonMap extends AbstractTest<Map<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_SingletonMap().test(true);
    }
    protected Map<String, String> getObject() {
        return Collections.singletonMap("key", "value");
    }
    protected Map<String, String> getAnotherObject() {
        return Collections.singletonMap("value", "key");
    }
}
