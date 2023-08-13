public final class Endianness {
    public static final Endianness BIG_ENDIAN = new Endianness("BIG_ENDIAN"); 
    public static final Endianness LITTLE_ENDIAN = new Endianness(
            "LITTLE_ENDIAN"); 
    private final String displayName;
    private Endianness(String displayName) {
        super();
        this.displayName = displayName;
    }
    public String toString() {
        return displayName;
    }
}
