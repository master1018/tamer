public final class TestCheckedSortedMap {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        SortedMap<String, String> map = new TreeMap<String, String>();
        TestEncoder.test(
                Collections.checkedSortedMap(map, String.class, String.class),
                new TreeMap() {
                    private final Object keyType = OBJECT;
                    private final Object valueType = OBJECT;
                },
                OBJECT
        );
    }
}
