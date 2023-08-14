public final class ByteOrder {
    public static final ByteOrder BIG_ENDIAN = new ByteOrder("BIG_ENDIAN"); 
    public static final ByteOrder LITTLE_ENDIAN = new ByteOrder("LITTLE_ENDIAN"); 
    private static final ByteOrder NATIVE_ORDER;
    static {
        if (Platform.getMemorySystem().isLittleEndian()) {
            NATIVE_ORDER = LITTLE_ENDIAN;
        } else {
            NATIVE_ORDER = BIG_ENDIAN;
        }
    }
    public static ByteOrder nativeOrder() {
        return NATIVE_ORDER;
    }
    private final String name;
    private ByteOrder(String name) {
        super();
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
