public final class Short extends Number implements Comparable<Short> {
	private static final long serialVersionUID = 7515723908773894738L;
    private final short value;
    public static final short MAX_VALUE = (short) 0x7FFF;
    public static final short MIN_VALUE = (short) 0x8000;
    public static final int SIZE = 16;
    @SuppressWarnings("unchecked")
    public static final Class<Short> TYPE
            = (Class<Short>) short[].class.getComponentType();
	public Short(String string) throws NumberFormatException {
		this(parseShort(string));
	}
	public Short(short value) {
		this.value = value;
	}
	@Override
    public byte byteValue() {
		return (byte) value;
	}
	public int compareTo(Short object) {
		return value > object.value ? 1 : (value < object.value ? -1 : 0);
	}
	public static Short decode(String string) throws NumberFormatException {
		int intValue = Integer.decode(string).intValue();
		short result = (short) intValue;
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
		return (object instanceof Short)
				&& (value == ((Short) object).value);
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
	public static short parseShort(String string) throws NumberFormatException {
		return parseShort(string, 10);
	}
	public static short parseShort(String string, int radix)
			throws NumberFormatException {
		int intValue = Integer.parseInt(string, radix);
		short result = (short) intValue;
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
	public static String toString(short value) {
		return Integer.toString(value);
	}
	public static Short valueOf(String string) throws NumberFormatException {
		return valueOf(parseShort(string));
	}
	public static Short valueOf(String string, int radix)
			throws NumberFormatException {
		return valueOf(parseShort(string, radix));
	}
    public static short reverseBytes(short s) {
        return (short) ((s << 8) | ((s >>> 8) & 0xFF));
    }
    public static Short valueOf(short s) {
        return s < -128 || s >= 128 ? new Short(s) : SMALL_VALUES[s + 128];
    }
    private static final Short[] SMALL_VALUES = new Short[256];
    static {
        for(int i = -128; i < 128; i++) {
            SMALL_VALUES[i + 128] = new Short((short) i);
        }
    }
}
