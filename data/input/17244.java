public final class java_util_Collections_CheckedSet extends AbstractTest<Set<String>> {
    public static void main(String[] args) {
        new java_util_Collections_CheckedSet().test(true);
    }
    protected Set<String> getObject() {
        Set<String> set = Collections.singleton("string");
        return Collections.checkedSet(set, String.class);
    }
    protected Set<String> getAnotherObject() {
        Set<String> set = Collections.emptySet();
        return Collections.checkedSet(set, String.class);
    }
}
