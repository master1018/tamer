public final class TestCheckedSet {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        Set<String> set = Collections.emptySet();
        TestEncoder.test(
                Collections.checkedSet(set, String.class),
                new HashSet() {
                    private final Object type = OBJECT;
                },
                OBJECT
        );
    }
}
