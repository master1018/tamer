public class SnmpInt extends SnmpValue {
    private static final long serialVersionUID = -7163624758070343373L;
    public SnmpInt(int v) throws IllegalArgumentException {
        if ( isInitValueValid(v) == false ) {
            throw new IllegalArgumentException() ;
        }
        value = (long)v ;
    }
    public SnmpInt(Integer v) throws IllegalArgumentException {
        this(v.intValue()) ;
    }
    public SnmpInt(long v) throws IllegalArgumentException {
        if ( isInitValueValid(v) == false ) {
            throw new IllegalArgumentException() ;
        }
        value = v ;
    }
    public SnmpInt(Long v) throws IllegalArgumentException {
        this(v.longValue()) ;
    }
    public SnmpInt(Enumerated v) throws IllegalArgumentException {
        this(v.intValue()) ;
    }
    public SnmpInt(boolean v) {
        value = v ? 1 : 2 ;
    }
    public long longValue() {
        return value ;
    }
    public Long toLong() {
        return new Long(value) ;
    }
    public int intValue() {
        return (int) value ;
    }
    public Integer toInteger() {
        return new Integer((int)value) ;
    }
    public String toString() {
        return String.valueOf(value) ;
    }
    public SnmpOid toOid() {
        return new SnmpOid(value) ;
    }
    public static SnmpOid toOid(long[] index, int start) throws SnmpStatusException {
        try {
            return new SnmpOid(index[start]) ;
        }
        catch(IndexOutOfBoundsException e) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }
    public static int nextOid(long[] index, int start) throws SnmpStatusException {
        if (start >= index.length) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
        else {
            return start + 1 ;
        }
    }
    public static void appendToOid(SnmpOid source, SnmpOid dest) {
        if (source.getLength() != 1) {
            throw new IllegalArgumentException() ;
        }
        dest.append(source) ;
    }
    final synchronized public SnmpValue duplicate() {
        return (SnmpValue) clone() ;
    }
    final synchronized public Object clone() {
        SnmpInt  newclone = null ;
        try {
            newclone = (SnmpInt) super.clone() ;
            newclone.value = value ;
        } catch (CloneNotSupportedException e) {
            throw new InternalError() ; 
        }
        return newclone ;
    }
    public String getTypeName() {
        return name ;
    }
    boolean isInitValueValid(int v) {
        if ((v < Integer.MIN_VALUE) || (v > Integer.MAX_VALUE)) {
            return false;
        }
        return true;
    }
    boolean isInitValueValid(long v) {
        if ((v < Integer.MIN_VALUE) || (v > Integer.MAX_VALUE)) {
            return false;
        }
        return true;
    }
    final static String name = "Integer32" ;
    protected long value = 0 ;
}
