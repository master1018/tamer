public final class java_util_Collections_EmptyMap extends AbstractTest<Map<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_EmptyMap().test(true);
    }
    protected Map<String, String> getObject() {
        return Collections.emptyMap();
    }
    protected Map<String, String> getAnotherObject() {
        return Collections.singletonMap("key", "value");
    }
}
