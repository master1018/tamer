public class SnmpCounter64 extends SnmpValue {
    private static final long serialVersionUID = 8784850650494679937L;
    public SnmpCounter64(long v) throws IllegalArgumentException {
        if ((v < 0) || (v > Long.MAX_VALUE)) {
            throw new IllegalArgumentException() ;
        }
        value = v ;
    }
    public SnmpCounter64(Long v) throws IllegalArgumentException {
        this(v.longValue()) ;
    }
    public long longValue() {
        return value ;
    }
    public Long toLong() {
        return new Long(value) ;
    }
    public int intValue() {
        return (int)value ;
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
        return (SnmpValue)clone() ;
    }
    final synchronized public Object clone() {
        SnmpCounter64  newclone = null ;
        try {
            newclone = (SnmpCounter64) super.clone() ;
            newclone.value = value ;
        } catch (CloneNotSupportedException e) {
            throw new InternalError() ; 
        }
        return newclone ;
    }
    final public String getTypeName() {
        return name ;
    }
    final static String name = "Counter64" ;
    private long value = 0 ;
}
