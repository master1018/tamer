public class SnmpStringFixed extends SnmpString {
    private static final long serialVersionUID = -9120939046874646063L;
    public SnmpStringFixed(byte[] v) {
        super(v) ;
    }
    public SnmpStringFixed(Byte[] v) {
        super(v) ;
    }
    public SnmpStringFixed(String v) {
        super(v) ;
    }
    public SnmpStringFixed(int l, byte[] v) throws IllegalArgumentException {
        if ((l <= 0) || (v == null)) {
            throw new IllegalArgumentException() ;
        }
        int length = Math.min(l, v.length);
        value = new byte[l] ;
        for (int i = 0 ; i < length ; i++) {
            value[i] = v[i] ;
        }
        for (int i = length ; i < l ; i++) {
            value[i] = 0 ;
        }
    }
    public SnmpStringFixed(int l, Byte[] v) throws IllegalArgumentException {
        if ((l <= 0) || (v == null)) {
            throw new IllegalArgumentException() ;
        }
        int length = Math.min(l, v.length);
        value = new byte[l] ;
        for (int i = 0 ; i < length ; i++) {
            value[i] = v[i].byteValue() ;
        }
        for (int i = length ; i < l ; i++) {
            value[i] = 0 ;
        }
    }
    public SnmpStringFixed(int l, String s) throws IllegalArgumentException {
        if ((l <= 0) || (s == null)) {
            throw new IllegalArgumentException() ;
        }
        byte[] v = s.getBytes();
        int length = Math.min(l, v.length);
        value = new byte[l] ;
        for (int i = 0 ; i < length ; i++) {
            value[i] = v[i] ;
        }
        for (int i = length ; i < l ; i++) {
            value[i] = 0 ;
        }
    }
    public static SnmpOid toOid(int l, long[] index, int start) throws SnmpStatusException {
        try {
            long[] ids = new long[l] ;
            for (int i = 0 ; i < l ; i++) {
                ids[i] = index[start + i] ;
            }
            return new SnmpOid(ids) ;
        }
        catch(IndexOutOfBoundsException e) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }
    public static int nextOid(int l, long[] index, int start) throws SnmpStatusException {
        int result = start + l ;
        if (result > index.length) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
        return result ;
    }
    public static void appendToOid(int l, SnmpOid source, SnmpOid dest) {
        dest.append(source) ;
    }
}
