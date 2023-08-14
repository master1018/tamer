public final class java_util_Collections_CheckedSortedSet extends AbstractTest<SortedSet<String>> {
    public static void main(String[] args) {
        new java_util_Collections_CheckedSortedSet().test(true);
    }
    protected SortedSet<String> getObject() {
        SortedSet<String> set = new TreeSet<String>();
        set.add("string");
        return Collections.checkedSortedSet(set, String.class);
    }
    protected SortedSet<String> getAnotherObject() {
        SortedSet<String> set = new TreeSet<String>();
        return Collections.checkedSortedSet(set, String.class);
    }
}
