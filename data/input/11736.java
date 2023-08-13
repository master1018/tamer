public final class java_util_Collections_UnmodifiableCollection extends AbstractTest<Collection<String>> {
    public static void main(String[] args) {
        new java_util_Collections_UnmodifiableCollection().test(true);
    }
    protected Collection<String> getObject() {
        List<String> list = Collections.singletonList("string");
        return Collections.unmodifiableCollection(list);
    }
    protected Collection<String> getAnotherObject() {
        List<String> list = Collections.emptyList();
        return Collections.unmodifiableCollection(list);
    }
}
