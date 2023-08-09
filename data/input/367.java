public final class java_util_Collections_SynchronizedCollection extends AbstractTest<Collection<String>> {
    public static void main(String[] args) {
        new java_util_Collections_SynchronizedCollection().test(true);
    }
    protected Collection<String> getObject() {
        List<String> list = Collections.singletonList("string");
        return Collections.synchronizedCollection(list);
    }
    protected Collection<String> getAnotherObject() {
        List<String> list = Collections.emptyList();
        return Collections.synchronizedCollection(list);
    }
}
