public abstract class SnmpUnsignedInt extends SnmpInt {
    public static final long   MAX_VALUE = 0x0ffffffffL;
    public SnmpUnsignedInt(int v) throws IllegalArgumentException {
        super(v);
    }
    public SnmpUnsignedInt(Integer v) throws IllegalArgumentException {
        super(v);
    }
    public SnmpUnsignedInt(long v) throws IllegalArgumentException {
        super(v);
    }
    public SnmpUnsignedInt(Long v) throws IllegalArgumentException {
        super(v);
    }
    public String getTypeName() {
        return name ;
    }
    boolean isInitValueValid(int v) {
        if ((v < 0) || (v > SnmpUnsignedInt.MAX_VALUE)) {
            return false;
        }
        return true;
    }
    boolean isInitValueValid(long v) {
        if ((v < 0) || (v > SnmpUnsignedInt.MAX_VALUE)) {
            return false;
        }
        return true;
    }
    final static String name = "Unsigned32" ;
}
