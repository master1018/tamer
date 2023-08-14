public final class TestEnumSet {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        TestEncoder.test(
                EnumSet.noneOf(Point.class),
                new HashSet() {
                    private final Object elementType = OBJECT;
                },
                OBJECT);
    }
    public enum Point { X, Y, Z }
}
