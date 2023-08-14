class Inet6Address extends InetAddress {
    final static int INADDRSZ = 16;
    private transient int cached_scope_id = 0;
    byte[] ipaddress;
    private int scope_id = 0;
    private boolean scope_id_set = false;
    private transient NetworkInterface scope_ifname = null;
    private boolean scope_ifname_set = false;
    private static final long serialVersionUID = 6880410070516793377L;
    static {
        init();
    }
    Inet6Address() {
        super();
        hostName = null;
        ipaddress = new byte[INADDRSZ];
        family = IPv6;
    }
    Inet6Address(String hostName, byte addr[], int scope_id) {
        this.hostName = hostName;
        if (addr.length == INADDRSZ) { 
            family = IPv6;
            ipaddress = addr.clone();
        }
        if (scope_id >= 0) {
            this.scope_id = scope_id;
            scope_id_set = true;
        }
    }
    Inet6Address(String hostName, byte addr[]) {
        try {
            initif (hostName, addr, null);
        } catch (UnknownHostException e) {} 
    }
    Inet6Address (String hostName, byte addr[], NetworkInterface nif) throws UnknownHostException {
        initif (hostName, addr, nif);
    }
    Inet6Address (String hostName, byte addr[], String ifname) throws UnknownHostException {
        initstr (hostName, addr, ifname);
    }
    public static Inet6Address getByAddress(String host, byte[] addr, NetworkInterface nif)
        throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[') {
            if (host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet6Address.INADDRSZ) {
                return new Inet6Address(host, addr, nif);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }
    public static Inet6Address getByAddress(String host, byte[] addr, int scope_id)
        throws UnknownHostException {
        if (host != null && host.length() > 0 && host.charAt(0) == '[') {
            if (host.charAt(host.length()-1) == ']') {
                host = host.substring(1, host.length() -1);
            }
        }
        if (addr != null) {
            if (addr.length == Inet6Address.INADDRSZ) {
                return new Inet6Address(host, addr, scope_id);
            }
        }
        throw new UnknownHostException("addr is of illegal length");
    }
    private void initstr (String hostName, byte addr[], String ifname) throws UnknownHostException {
        try {
            NetworkInterface nif = NetworkInterface.getByName (ifname);
            if (nif == null) {
                throw new UnknownHostException ("no such interface " + ifname);
            }
            initif (hostName, addr, nif);
        } catch (SocketException e) {
            throw new UnknownHostException ("SocketException thrown" + ifname);
        }
    }
    private void initif(String hostName, byte addr[],NetworkInterface nif) throws UnknownHostException {
        this.hostName = hostName;
        if (addr.length == INADDRSZ) { 
            family = IPv6;
            ipaddress = addr.clone();
        }
        if (nif != null) {
            this.scope_ifname = nif;
            scope_ifname_set = true;
            scope_id = deriveNumericScope (nif);
            scope_id_set = true;
        }
    }
    private boolean differentLocalAddressTypes(Inet6Address other) {
        if (isLinkLocalAddress() && !other.isLinkLocalAddress()) {
            return false;
        }
        if (isSiteLocalAddress() && !other.isSiteLocalAddress()) {
            return false;
        }
        return true;
    }
    private int deriveNumericScope (NetworkInterface ifc) throws UnknownHostException {
        Enumeration<InetAddress> addresses = ifc.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress addr = addresses.nextElement();
            if (!(addr instanceof Inet6Address)) {
                continue;
            }
            Inet6Address ia6_addr = (Inet6Address)addr;
            if (!differentLocalAddressTypes(ia6_addr)){
                continue;
            }
            return ia6_addr.scope_id;
        }
        throw new UnknownHostException ("no scope_id found");
    }
    private int deriveNumericScope (String ifname) throws UnknownHostException {
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new UnknownHostException ("could not enumerate local network interfaces");
        }
        while (en.hasMoreElements()) {
            NetworkInterface ifc = en.nextElement();
            if (ifc.getName().equals (ifname)) {
                Enumeration addresses = ifc.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = (InetAddress)addresses.nextElement();
                    if (!(addr instanceof Inet6Address)) {
                        continue;
                    }
                    Inet6Address ia6_addr = (Inet6Address)addr;
                    if (!differentLocalAddressTypes(ia6_addr)){
                        continue;
                    }
                    return ia6_addr.scope_id;
                }
            }
        }
        throw new UnknownHostException ("No matching address found for interface : " +ifname);
    }
    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        scope_ifname = null;
        scope_ifname_set = false;
        s.defaultReadObject();
        if (ifname != null && !"".equals (ifname)) {
            try {
                scope_ifname = NetworkInterface.getByName(ifname);
                if (scope_ifname == null) {
                    scope_id_set = false;
                    scope_ifname_set = false;
                    scope_id = 0;
                } else {
                    try {
                        scope_id = deriveNumericScope (scope_ifname);
                    } catch (UnknownHostException e) {
                    }
                }
            } catch (SocketException e) {}
        }
        ipaddress = ipaddress.clone();
        if (ipaddress.length != INADDRSZ) {
            throw new InvalidObjectException("invalid address length: "+
                                             ipaddress.length);
        }
        if (family != IPv6) {
            throw new InvalidObjectException("invalid address family type");
        }
    }
    @Override
    public boolean isMulticastAddress() {
        return ((ipaddress[0] & 0xff) == 0xff);
    }
    @Override
    public boolean isAnyLocalAddress() {
        byte test = 0x00;
        for (int i = 0; i < INADDRSZ; i++) {
            test |= ipaddress[i];
        }
        return (test == 0x00);
    }
    @Override
    public boolean isLoopbackAddress() {
        byte test = 0x00;
        for (int i = 0; i < 15; i++) {
            test |= ipaddress[i];
        }
        return (test == 0x00) && (ipaddress[15] == 0x01);
    }
    @Override
    public boolean isLinkLocalAddress() {
        return ((ipaddress[0] & 0xff) == 0xfe
                && (ipaddress[1] & 0xc0) == 0x80);
    }
    @Override
    public boolean isSiteLocalAddress() {
        return ((ipaddress[0] & 0xff) == 0xfe
                && (ipaddress[1] & 0xc0) == 0xc0);
    }
    @Override
    public boolean isMCGlobal() {
        return ((ipaddress[0] & 0xff) == 0xff
                && (ipaddress[1] & 0x0f) == 0x0e);
    }
    @Override
    public boolean isMCNodeLocal() {
        return ((ipaddress[0] & 0xff) == 0xff
                && (ipaddress[1] & 0x0f) == 0x01);
    }
    @Override
    public boolean isMCLinkLocal() {
        return ((ipaddress[0] & 0xff) == 0xff
                && (ipaddress[1] & 0x0f) == 0x02);
    }
    @Override
    public boolean isMCSiteLocal() {
        return ((ipaddress[0] & 0xff) == 0xff
                && (ipaddress[1] & 0x0f) == 0x05);
    }
    @Override
    public boolean isMCOrgLocal() {
        return ((ipaddress[0] & 0xff) == 0xff
                && (ipaddress[1] & 0x0f) == 0x08);
    }
    @Override
    public byte[] getAddress() {
        return ipaddress.clone();
    }
     public int getScopeId () {
        return scope_id;
     }
     public NetworkInterface getScopedInterface () {
        return scope_ifname;
     }
    @Override
    public String getHostAddress() {
        String s = numericToTextFormat(ipaddress);
        if (scope_ifname_set) { 
            s = s + "%" + scope_ifname.getName();
        } else if (scope_id_set) {
            s = s + "%" + scope_id;
        }
        return s;
    }
    @Override
    public int hashCode() {
        if (ipaddress != null) {
            int hash = 0;
            int i=0;
            while (i<INADDRSZ) {
                int j=0;
                int component=0;
                while (j<4 && i<INADDRSZ) {
                    component = (component << 8) + ipaddress[i];
                    j++;
                    i++;
                }
                hash += component;
            }
            return hash;
        } else {
            return 0;
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null ||
            !(obj instanceof Inet6Address))
            return false;
        Inet6Address inetAddr = (Inet6Address)obj;
        for (int i = 0; i < INADDRSZ; i++) {
            if (ipaddress[i] != inetAddr.ipaddress[i])
                return false;
        }
        return true;
    }
    public boolean isIPv4CompatibleAddress() {
        if ((ipaddress[0] == 0x00) && (ipaddress[1] == 0x00) &&
            (ipaddress[2] == 0x00) && (ipaddress[3] == 0x00) &&
            (ipaddress[4] == 0x00) && (ipaddress[5] == 0x00) &&
            (ipaddress[6] == 0x00) && (ipaddress[7] == 0x00) &&
            (ipaddress[8] == 0x00) && (ipaddress[9] == 0x00) &&
            (ipaddress[10] == 0x00) && (ipaddress[11] == 0x00))  {
            return true;
        }
        return false;
    }
    private final static int INT16SZ = 2;
    static String numericToTextFormat(byte[] src)
    {
        StringBuffer sb = new StringBuffer(39);
        for (int i = 0; i < (INADDRSZ / INT16SZ); i++) {
            sb.append(Integer.toHexString(((src[i<<1]<<8) & 0xff00)
                                          | (src[(i<<1)+1] & 0xff)));
            if (i < (INADDRSZ / INT16SZ) -1 ) {
               sb.append(":");
            }
        }
        return sb.toString();
    }
    private static native void init();
    private String ifname;
    private synchronized void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        if (scope_ifname_set) {
            ifname = scope_ifname.getName();
        }
        s.defaultWriteObject();
    }
}
