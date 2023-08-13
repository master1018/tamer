public class HostAddress implements Cloneable {
    int addrType;
    byte[] address = null;
    private static InetAddress localInetAddress; 
    private static final boolean DEBUG = sun.security.krb5.internal.Krb5.DEBUG;
    private volatile int hashCode = 0;
    private HostAddress(int dummy) {}
    public Object clone() {
        HostAddress new_hostAddress = new HostAddress(0);
        new_hostAddress.addrType = addrType;
        if (address != null) {
            new_hostAddress.address = address.clone();
        }
        return new_hostAddress;
    }
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 37*result + addrType;
            if (address != null) {
                for (int i=0; i < address.length; i++)  {
                    result = 37*result + address[i];
                }
            }
            hashCode = result;
        }
        return hashCode;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HostAddress)) {
            return false;
        }
        HostAddress h = (HostAddress)obj;
        if (addrType != h.addrType ||
            (address != null && h.address == null) ||
            (address == null && h.address != null))
            return false;
        if (address != null && h.address != null) {
            if (address.length != h.address.length)
                return false;
            for (int i = 0; i < address.length; i++)
                if (address[i] != h.address[i])
                    return false;
        }
        return true;
    }
    private static synchronized InetAddress getLocalInetAddress()
        throws UnknownHostException {
        if (localInetAddress == null) {
           localInetAddress = InetAddress.getLocalHost();
        }
        if (localInetAddress == null) {
            throw new UnknownHostException();
        }
        return (localInetAddress);
    }
    public InetAddress getInetAddress() throws UnknownHostException {
        if (addrType == Krb5.ADDRTYPE_INET ||
            addrType == Krb5.ADDRTYPE_INET6) {
            return (InetAddress.getByAddress(address));
        } else {
            return null;
        }
    }
    private int getAddrType(InetAddress inetAddress) {
        int addressType = 0;
        if (inetAddress instanceof Inet4Address)
            addressType = Krb5.ADDRTYPE_INET;
        else if (inetAddress instanceof Inet6Address)
            addressType = Krb5.ADDRTYPE_INET6;
        return (addressType);
    }
    public HostAddress() throws UnknownHostException {
        InetAddress inetAddress = getLocalInetAddress();
        addrType = getAddrType(inetAddress);
        address = inetAddress.getAddress();
    }
    public HostAddress(int new_addrType, byte[] new_address)
        throws KrbApErrException, UnknownHostException {
        switch(new_addrType) {
        case Krb5.ADDRTYPE_INET:        
            if (new_address.length != 4)
                throw new KrbApErrException(0, "Invalid Internet address");
            break;
        case Krb5.ADDRTYPE_CHAOS:
            if (new_address.length != 2) 
                throw new KrbApErrException(0, "Invalid CHAOSnet address");
            break;
        case Krb5.ADDRTYPE_ISO:   
            break;
        case Krb5.ADDRTYPE_IPX:   
            if (new_address.length != 6)
                throw new KrbApErrException(0, "Invalid XNS address");
            break;
        case Krb5.ADDRTYPE_APPLETALK:  
            if (new_address.length != 3)
                throw new KrbApErrException(0, "Invalid DDP address");
            break;
        case Krb5.ADDRTYPE_DECNET:    
            if (new_address.length != 2)
                throw new KrbApErrException(0, "Invalid DECnet Phase IV address");
            break;
        case Krb5.ADDRTYPE_INET6:     
            if (new_address.length != 16)
                throw new KrbApErrException(0, "Invalid Internet IPv6 address");
            break;
        }
        addrType = new_addrType;
        if (new_address != null) {
           address = new_address.clone();
        }
        if (DEBUG) {
            if (addrType == Krb5.ADDRTYPE_INET ||
                addrType == Krb5.ADDRTYPE_INET6) {
                System.out.println("Host address is " +
                        InetAddress.getByAddress(address));
            }
        }
    }
    public HostAddress(InetAddress inetAddress) {
        addrType = getAddrType(inetAddress);
        address = inetAddress.getAddress();
    }
    public HostAddress(DerValue encoding) throws Asn1Exception, IOException {
        DerValue der = encoding.getData().getDerValue();
        if ((der.getTag() & (byte)0x1F) == (byte)0x00) {
            addrType = der.getData().getBigInteger().intValue();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        der = encoding.getData().getDerValue();
        if ((der.getTag() & (byte)0x1F) == (byte)0x01) {
            address = der.getData().getOctetString();
        }
        else
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        if (encoding.getData().available() > 0)
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
    }
    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        temp.putInteger(this.addrType);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x00), temp);
        temp = new DerOutputStream();
        temp.putOctetString(address);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte)0x01), temp);
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }
    public static HostAddress parse(DerInputStream data, byte explicitTag,
                                    boolean optional)
        throws Asn1Exception, IOException{
        if ((optional) &&
            (((byte)data.peekByte() & (byte)0x1F) != explicitTag)) {
            return null;
        }
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte)0x1F))  {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        else {
            DerValue subDer = der.getData().getDerValue();
            return new HostAddress(subDer);
        }
    }
}
