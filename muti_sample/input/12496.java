public final class TestCheckedList {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        List<String> list = Collections.emptyList();
        TestEncoder.test(
                Collections.checkedList(list, String.class),
                new ArrayList() {
                    private final Object type = OBJECT;
                },
                OBJECT
        );
    }
}
