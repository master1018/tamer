public final class java_util_Collections_UnmodifiableSortedMap extends AbstractTest<SortedMap<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_UnmodifiableSortedMap().test(true);
    }
    protected SortedMap<String, String> getObject() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("key", "value");
        return Collections.unmodifiableSortedMap(map);
    }
    protected SortedMap<String, String> getAnotherObject() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        return Collections.unmodifiableSortedMap(map);
    }
}
