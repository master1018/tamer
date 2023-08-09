public class SnmpEngineId implements Serializable {
    private static final long serialVersionUID = 5434729655830763317L;
    byte[] engineId = null;
    String hexString = null;
    String humanString = null;
    SnmpEngineId(String hexString) {
        engineId = SnmpTools.ascii2binary(hexString);
        this.hexString = hexString.toLowerCase();
    }
    SnmpEngineId(byte[] bin) {
        engineId = bin;
        hexString = SnmpTools.binary2ascii(bin).toLowerCase();
    }
    public String getReadableId() {
        return humanString;
    }
    public String toString() {
        return hexString;
    }
    public byte[] getBytes() {
        return engineId;
    }
    void setStringValue(String val) {
        humanString = val;
    }
    static void validateId(String str) throws IllegalArgumentException {
        byte[] arr = SnmpTools.ascii2binary(str);
        validateId(arr);
    }
    static void validateId(byte[] arr) throws IllegalArgumentException {
        if(arr.length < 5) throw new IllegalArgumentException("Id size lower than 5 bytes.");
        if(arr.length > 32) throw new IllegalArgumentException("Id size greater than 32 bytes.");
        if( ((arr[0] & 0x80) == 0) && arr.length != 12)
            throw new IllegalArgumentException("Very first bit = 0 and length != 12 octets");
        byte[] zeroedArrays = new byte[arr.length];
        if(Arrays.equals(zeroedArrays, arr)) throw new IllegalArgumentException("Zeroed Id.");
        byte[] FFArrays = new byte[arr.length];
        Arrays.fill(FFArrays, (byte)0xFF);
        if(Arrays.equals(FFArrays, arr)) throw new IllegalArgumentException("0xFF Id.");
    }
    public static SnmpEngineId createEngineId(byte[] arr) throws IllegalArgumentException {
        if( (arr == null) || arr.length == 0) return null;
        validateId(arr);
        return new SnmpEngineId(arr);
    }
    public static SnmpEngineId createEngineId() {
        byte[] address = null;
        byte[] engineid = new byte[13];
        int iana = 42;
        long mask = 0xFF;
        long time = System.currentTimeMillis();
        engineid[0] = (byte) ( (iana & 0xFF000000) >> 24 );
        engineid[0] |= 0x80;
        engineid[1] = (byte) ( (iana & 0x00FF0000) >> 16 );
        engineid[2] = (byte) ( (iana & 0x0000FF00) >> 8 );
        engineid[3] = (byte) (iana & 0x000000FF);
        engineid[4] = 0x05;
        engineid[5] =  (byte) ( (time & (mask << 56)) >>> 56 );
        engineid[6] =  (byte) ( (time & (mask << 48) ) >>> 48 );
        engineid[7] =  (byte) ( (time & (mask << 40) ) >>> 40 );
        engineid[8] =  (byte) ( (time & (mask << 32) ) >>> 32 );
        engineid[9] =  (byte) ( (time & (mask << 24) ) >>> 24 );
        engineid[10] = (byte) ( (time & (mask << 16) ) >>> 16 );
        engineid[11] = (byte) ( (time & (mask << 8) ) >>> 8 );
        engineid[12] = (byte) (time & mask);
        return new SnmpEngineId(engineid);
    }
    public SnmpOid toOid() {
        long[] oid = new long[engineId.length + 1];
        oid[0] = engineId.length;
        for(int i = 1; i <= engineId.length; i++)
            oid[i] = (long) (engineId[i-1] & 0xFF);
        return new SnmpOid(oid);
    }
    public static SnmpEngineId createEngineId(String str)
        throws IllegalArgumentException, UnknownHostException {
        return createEngineId(str, null);
    }
    public static SnmpEngineId createEngineId(String str, String separator)
        throws IllegalArgumentException, UnknownHostException {
        if(str == null) return null;
        if(str.startsWith("0x") || str.startsWith("0X")) {
            validateId(str);
            return new SnmpEngineId(str);
        }
        separator = separator == null ? ":" : separator;
        StringTokenizer token = new StringTokenizer(str,
                                                    separator,
                                                    true);
        String address = null;
        String port = null;
        String iana = null;
        int objPort = 161;
        int objIana = 42;
        InetAddress objAddress = null;
        SnmpEngineId eng = null;
        try {
            try {
                address = token.nextToken();
            }catch(NoSuchElementException e) {
                throw new IllegalArgumentException("Passed string is invalid : ["+str+"]");
            }
            if(!address.equals(separator)) {
                objAddress = InetAddress.getByName(address);
                try {
                    token.nextToken();
                }catch(NoSuchElementException e) {
                    eng = SnmpEngineId.createEngineId(objAddress,
                                                      objPort,
                                                      objIana);
                    eng.setStringValue(str);
                    return eng;
                }
            }
            else
                objAddress = InetAddress.getLocalHost();
            try {
                port = token.nextToken();
            }catch(NoSuchElementException e) {
                eng = SnmpEngineId.createEngineId(objAddress,
                                                  objPort,
                                                  objIana);
                eng.setStringValue(str);
                return eng;
            }
            if(!port.equals(separator)) {
                objPort = Integer.parseInt(port);
                try {
                    token.nextToken();
                }catch(NoSuchElementException e) {
                    eng = SnmpEngineId.createEngineId(objAddress,
                                                      objPort,
                                                      objIana);
                    eng.setStringValue(str);
                    return eng;
                }
            }
            try {
                iana = token.nextToken();
            }catch(NoSuchElementException e) {
                eng = SnmpEngineId.createEngineId(objAddress,
                                                  objPort,
                                                  objIana);
                eng.setStringValue(str);
                return eng;
            }
            if(!iana.equals(separator))
                objIana = Integer.parseInt(iana);
            eng = SnmpEngineId.createEngineId(objAddress,
                                              objPort,
                                              objIana);
            eng.setStringValue(str);
            return eng;
        } catch(Exception e) {
            throw new IllegalArgumentException("Passed string is invalid : ["+str+"]. Check that the used separator ["+ separator + "] is compatible with IPv6 address format.");
        }
    }
    public static SnmpEngineId createEngineId(int port)
        throws UnknownHostException {
        int suniana = 42;
        InetAddress address = null;
        address = InetAddress.getLocalHost();
        return createEngineId(address, port, suniana);
    }
    public static SnmpEngineId createEngineId(InetAddress address, int port)
        throws IllegalArgumentException {
        int suniana = 42;
        if(address == null)
            throw new IllegalArgumentException("InetAddress is null.");
        return createEngineId(address, port, suniana);
    }
    public static SnmpEngineId createEngineId(int port, int iana) throws UnknownHostException {
        InetAddress address = null;
        address = InetAddress.getLocalHost();
        return createEngineId(address, port, iana);
    }
    public static SnmpEngineId createEngineId(InetAddress addr,
                                              int port,
                                              int iana) {
        if(addr == null) throw new IllegalArgumentException("InetAddress is null.");
        byte[] address = addr.getAddress();
        byte[] engineid = new byte[9 + address.length];
        engineid[0] = (byte) ( (iana & 0xFF000000) >> 24 );
        engineid[0] |= 0x80;
        engineid[1] = (byte) ( (iana & 0x00FF0000) >> 16 );
        engineid[2] = (byte) ( (iana & 0x0000FF00) >> 8 );
engineid[3] = (byte) (iana & 0x000000FF);
        engineid[4] = 0x05;
        if(address.length == 4)
            engineid[4] = 0x01;
        if(address.length == 16)
            engineid[4] = 0x02;
        for(int i = 0; i < address.length; i++) {
            engineid[i + 5] = address[i];
        }
        engineid[5 + address.length] = (byte)  ( (port & 0xFF000000) >> 24 );
        engineid[6 + address.length] = (byte) ( (port & 0x00FF0000) >> 16 );
        engineid[7 + address.length] = (byte) ( (port & 0x0000FF00) >> 8 );
        engineid[8 + address.length] = (byte) (  port & 0x000000FF );
        return new SnmpEngineId(engineid);
    }
    public static SnmpEngineId createEngineId(int iana, InetAddress addr)
    {
        if(addr == null) throw new IllegalArgumentException("InetAddress is null.");
        byte[] address = addr.getAddress();
        byte[] engineid = new byte[5 + address.length];
        engineid[0] = (byte) ( (iana & 0xFF000000) >> 24 );
        engineid[0] |= 0x80;
        engineid[1] = (byte) ( (iana & 0x00FF0000) >> 16 );
        engineid[2] = (byte) ( (iana & 0x0000FF00) >> 8 );
        engineid[3] = (byte) (iana & 0x000000FF);
        if(address.length == 4)
            engineid[4] = 0x01;
        if(address.length == 16)
            engineid[4] = 0x02;
        for(int i = 0; i < address.length; i++) {
            engineid[i + 5] = address[i];
        }
        return new SnmpEngineId(engineid);
    }
    public static SnmpEngineId createEngineId(InetAddress addr) {
        return createEngineId(42, addr);
    }
    public boolean equals(Object a) {
        if(!(a instanceof SnmpEngineId) ) return false;
        return hexString.equals(((SnmpEngineId) a).toString());
    }
    public int hashCode() {
        return hexString.hashCode();
    }
}
