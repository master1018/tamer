public final class TestCheckedRandomAccessList {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        TestEncoder.test(
                Collections.checkedList(list, String.class),
                new ArrayList() {
                    private final Object type = OBJECT;
                },
                OBJECT
        );
    }
}
