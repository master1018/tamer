public final class java_util_Collections_SynchronizedList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_SynchronizedList().test(true);
    }
    protected List<String> getObject() {
        List<String> list = Collections.singletonList("string");
        return Collections.synchronizedList(list);
    }
    protected List<String> getAnotherObject() {
        List<String> list = Collections.emptyList();
        return Collections.synchronizedList(list);
    }
}
