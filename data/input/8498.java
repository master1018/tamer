public class SnmpOpaque extends SnmpString {
    private static final long serialVersionUID = 380952213936036664L;
    public SnmpOpaque(byte[] v) {
        super(v) ;
    }
    public SnmpOpaque(Byte[] v) {
        super(v) ;
    }
    public SnmpOpaque(String v) {
        super(v) ;
    }
    public String toString() {
        StringBuffer result = new StringBuffer() ;
        for (int i = 0 ; i < value.length ; i++) {
            byte b = value[i] ;
            int n = (b >= 0) ? b : b + 256 ;
            result.append(Character.forDigit(n / 16, 16)) ;
            result.append(Character.forDigit(n % 16, 16)) ;
        }
        return result.toString() ;
    }
    final public String getTypeName() {
        return name ;
    }
    final static String name = "Opaque" ;
}
