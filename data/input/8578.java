public final class TestEnumMap {
    private static final Object OBJECT = new Object();
    public static void main(String[] args) {
        TestEncoder.test(
                new EnumMap<Point, String>(Point.class),
                new HashMap() {
                    private final Object keyType = OBJECT;
                },
                OBJECT);
    }
    public enum Point { X, Y, Z }
}
