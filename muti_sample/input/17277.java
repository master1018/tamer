public final class TestCheckedMap {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        Map<String, String> map = Collections.emptyMap();
        TestEncoder.test(
                Collections.checkedMap(map, String.class, String.class),
                new HashMap() {
                    private final Object keyType = OBJECT;
                    private final Object valueType = OBJECT;
                },
                OBJECT
        );
    }
}
