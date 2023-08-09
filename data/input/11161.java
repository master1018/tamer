public class SnmpIpAddress extends SnmpOid {
    private static final long serialVersionUID = 7204629998270874474L;
    public SnmpIpAddress(byte[] bytes) throws IllegalArgumentException {
        buildFromByteArray(bytes);
    }
    public SnmpIpAddress(long addr) {
        int address = (int)addr ;
        byte[] ipaddr = new byte[4];
        ipaddr[0] = (byte) ((address >>> 24) & 0xFF);
        ipaddr[1] = (byte) ((address >>> 16) & 0xFF);
        ipaddr[2] = (byte) ((address >>> 8) & 0xFF);
        ipaddr[3] = (byte) (address & 0xFF);
        buildFromByteArray(ipaddr);
    }
    public SnmpIpAddress(String dotAddress) throws IllegalArgumentException {
        super(dotAddress) ;
        if ((componentCount > 4) ||
            (components[0] > 255) ||
            (components[1] > 255) ||
            (components[2] > 255) ||
            (components[3] > 255)) {
            throw new IllegalArgumentException(dotAddress) ;
        }
    }
    public SnmpIpAddress(long b1, long b2, long b3, long b4) {
        super(b1, b2, b3, b4) ;
        if ((components[0] > 255) ||
            (components[1] > 255) ||
            (components[2] > 255) ||
            (components[3] > 255)) {
            throw new IllegalArgumentException() ;
        }
    }
    public byte[] byteValue() {
        byte[] result = new byte[4] ;
        result[0] = (byte)components[0] ;
        result[1] = (byte)components[1] ;
        result[2] = (byte)components[2] ;
        result[3] = (byte)components[3] ;
        return result ;
    }
    public String stringValue() {
        return toString() ;
    }
    public static SnmpOid toOid(long[] index, int start) throws SnmpStatusException {
        if (start + 4 <= index.length) {
            try {
                return new SnmpOid(
                                   index[start],
                                   index[start+1],
                                   index[start+2],
                                   index[start+3]) ;
            }
            catch(IllegalArgumentException e) {
                throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
            }
        }
        else {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }
    public static int nextOid(long[] index, int start) throws SnmpStatusException {
        if (start + 4 <= index.length) {
            return start + 4 ;
        }
        else {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }
    public static void appendToOid(SnmpOid source, SnmpOid dest) {
        if (source.getLength() != 4) {
            throw new IllegalArgumentException() ;
        }
        dest.append(source) ;
    }
    final public String getTypeName() {
        return name ;
    }
    private void buildFromByteArray(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException() ;
        }
        components = new long[4] ;
        componentCount= 4;
        components[0] = (bytes[0] >= 0) ? bytes[0] : bytes[0] + 256 ;
        components[1] = (bytes[1] >= 0) ? bytes[1] : bytes[1] + 256 ;
        components[2] = (bytes[2] >= 0) ? bytes[2] : bytes[2] + 256 ;
        components[3] = (bytes[3] >= 0) ? bytes[3] : bytes[3] + 256 ;
    }
    final static String name = "IpAddress" ;
}
