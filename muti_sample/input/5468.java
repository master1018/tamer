public final class java_util_Collections_UnmodifiableSet extends AbstractTest<Set<String>> {
    public static void main(String[] args) {
        new java_util_Collections_UnmodifiableSet().test(true);
    }
    protected Set<String> getObject() {
        Set<String> set = Collections.singleton("string");
        return Collections.unmodifiableSet(set);
    }
    protected Set<String> getAnotherObject() {
        Set<String> set = Collections.emptySet();
        return Collections.unmodifiableSet(set);
    }
}
