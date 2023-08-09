public final class java_util_Collections_UnmodifiableList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_UnmodifiableList().test(true);
    }
    protected List<String> getObject() {
        List<String> list = Collections.singletonList("string");
        return Collections.unmodifiableList(list);
    }
    protected List<String> getAnotherObject() {
        List<String> list = Collections.emptyList();
        return Collections.unmodifiableList(list);
    }
}
