public final class java_util_Collections_EmptySet extends AbstractTest<Set<String>> {
    public static void main(String[] args) {
        new java_util_Collections_EmptySet().test(true);
    }
    protected Set<String> getObject() {
        return Collections.emptySet();
    }
    protected Set<String> getAnotherObject() {
        return Collections.singleton("string");
    }
}
