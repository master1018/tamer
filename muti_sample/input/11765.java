public final class java_util_Collections_SynchronizedSortedMap extends AbstractTest<SortedMap<String, String>> {
    public static void main(String[] args) {
        new java_util_Collections_SynchronizedSortedMap().test(true);
    }
    protected SortedMap<String, String> getObject() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("key", "value");
        return Collections.synchronizedSortedMap(map);
    }
    protected SortedMap<String, String> getAnotherObject() {
        SortedMap<String, String> map = new TreeMap<String, String>();
        return Collections.synchronizedSortedMap(map);
    }
}
