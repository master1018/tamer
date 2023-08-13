public final class java_util_Collections_CheckedCollection extends AbstractTest<Collection<String>> {
    public static void main(String[] args) {
        new java_util_Collections_CheckedCollection().test(true);
    }
    protected Collection<String> getObject() {
        List<String> list = Collections.singletonList("string");
        return Collections.checkedCollection(list, String.class);
    }
    protected Collection<String> getAnotherObject() {
        List<String> list = Collections.emptyList();
        return Collections.checkedCollection(list, String.class);
    }
}
