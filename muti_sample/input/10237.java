public final class java_util_Collections_EmptyList extends AbstractTest<List<String>> {
    public static void main(String[] args) {
        new java_util_Collections_EmptyList().test(true);
    }
    protected List<String> getObject() {
        return Collections.emptyList();
    }
    protected List<String> getAnotherObject() {
        return Collections.singletonList("string");
    }
}
