public final class java_util_Collections_UnmodifiableSortedSet extends AbstractTest<SortedSet<String>> {
    public static void main(String[] args) {
        new java_util_Collections_UnmodifiableSortedSet().test(true);
    }
    protected SortedSet<String> getObject() {
        SortedSet<String> set = new TreeSet<String>();
        set.add("string");
        return Collections.unmodifiableSortedSet(set);
    }
    protected SortedSet<String> getAnotherObject() {
        SortedSet<String> set = new TreeSet<String>();
        return Collections.unmodifiableSortedSet(set);
    }
}
