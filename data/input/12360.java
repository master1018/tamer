class PerfDataType {
    private final String name;
    private final byte value;
    private final int size;
    public static final PerfDataType BOOLEAN = new PerfDataType("boolean",  "Z", 1);
    public static final PerfDataType CHAR    = new PerfDataType("char",     "C", 1);
    public static final PerfDataType FLOAT   = new PerfDataType("float",    "F", 8);
    public static final PerfDataType DOUBLE  = new PerfDataType("double",   "D", 8);
    public static final PerfDataType BYTE    = new PerfDataType("byte",     "B", 1);
    public static final PerfDataType SHORT   = new PerfDataType("short",    "S", 2);
    public static final PerfDataType INT     = new PerfDataType("int",      "I", 4);
    public static final PerfDataType LONG    = new PerfDataType("long",     "J", 8);
    public static final PerfDataType ILLEGAL = new PerfDataType("illegal",  "X", 0);
    private static PerfDataType basicTypes[] = {
        LONG, BYTE, BOOLEAN, CHAR, FLOAT, DOUBLE, SHORT, INT
    };
    public String toString() {
        return name;
    }
    public byte byteValue() {
        return value;
    }
    public int size() {
        return size;
    }
    public static PerfDataType toPerfDataType(byte type) {
        for (int j = 0; j < basicTypes.length; j++) {
            if (basicTypes[j].byteValue() == type) {
                return (basicTypes[j]);
            }
        }
        return ILLEGAL;
    }
    private PerfDataType(String name, String c, int size) {
        this.name = name;
        this.size = size;
        try {
            byte[] b = c.getBytes("UTF-8");
            this.value = b[0];
        } catch (UnsupportedEncodingException e) {
            throw new InternalError("Unknown encoding");
        }
    }
}
