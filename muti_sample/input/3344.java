public final class TestCheckedSortedSet {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        SortedSet<String> set = new TreeSet<String>();
        TestEncoder.test(
                Collections.checkedSortedSet(set, String.class),
                new TreeSet() {
                    private final Object type = OBJECT;
                },
                OBJECT
        );
    }
}
