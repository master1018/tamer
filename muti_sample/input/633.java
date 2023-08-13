public final class java_util_Collections_CheckedMap extends AbstractTest<Map<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_CheckedMap().test(true);
    }
    protected Map<String, String> getObject() {
        Map<String, String> map = Collections.singletonMap("key", "value");
        return Collections.checkedMap(map, String.class, String.class);
    }
    protected Map<String, String> getAnotherObject() {
        Map<String, String> map = Collections.emptyMap();
        return Collections.checkedMap(map, String.class, String.class);
    }
}
