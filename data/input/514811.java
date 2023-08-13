public final class Byte extends Number implements Comparable<Byte> {
    private static final long serialVersionUID = -7183698231559129828L;
    private final byte value;
    public static final byte MAX_VALUE = (byte) 0x7F;
    public static final byte MIN_VALUE = (byte) 0x80;
    public static final int SIZE = 8;
    @SuppressWarnings("unchecked")
    public static final Class<Byte> TYPE
            = (Class<Byte>) byte[].class.getComponentType();
    public Byte(byte value) {
        this.value = value;
    }
    public Byte(String string) throws NumberFormatException {
        this(parseByte(string));
    }
    @Override
    public byte byteValue() {
        return value;
    }
    public int compareTo(Byte object) {
        return value > object.value ? 1 : (value < object.value ? -1 : 0);
    }
    public static Byte decode(String string) throws NumberFormatException {
        int intValue = Integer.decode(string).intValue();
        byte result = (byte) intValue;
        if (result == intValue) {
            return valueOf(result);
        }
        throw new NumberFormatException();
    }
    @Override
    public double doubleValue() {
        return value;
    }
    @Override
    public boolean equals(Object object) {
        return (object == this) || (object instanceof Byte)
                && (value == ((Byte) object).value);
    }
    @Override
    public float floatValue() {
        return value;
    }
    @Override
    public int hashCode() {
        return value;
    }
    @Override
    public int intValue() {
        return value;
    }
    @Override
    public long longValue() {
        return value;
    }
    public static byte parseByte(String string) throws NumberFormatException {
        int intValue = Integer.parseInt(string);
        byte result = (byte) intValue;
        if (result == intValue) {
            return result;
        }
        throw new NumberFormatException();
    }
    public static byte parseByte(String string, int radix)
            throws NumberFormatException {
        int intValue = Integer.parseInt(string, radix);
        byte result = (byte) intValue;
        if (result == intValue) {
            return result;
        }
        throw new NumberFormatException();
    }
    @Override
    public short shortValue() {
        return value;
    }
    @Override
    public String toString() {
        return Integer.toString(value);
    }
    public static String toString(byte value) {
        return Integer.toString(value);
    }
    public static Byte valueOf(String string) throws NumberFormatException {
        return valueOf(parseByte(string));
    }
    public static Byte valueOf(String string, int radix)
            throws NumberFormatException {
        return valueOf(parseByte(string, radix));
    }
    public static Byte valueOf(byte b) {
        return VALUES[b + 128];
    }
    private static final Byte[] VALUES = new Byte[256];
    static {
        for (int i = -128; i < 128; i++) {
            VALUES[i + 128] = new Byte((byte) i);
        }
    }
}
