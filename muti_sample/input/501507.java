public final class Inet6Address extends InetAddress {
    private static final long serialVersionUID = 6880410070516793377L;
    static final InetAddress ANY = new Inet6Address(new byte[]
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    static final InetAddress LOOPBACK = new Inet6Address(new byte[]
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, "localhost"); 
    int scope_id;
    boolean scope_id_set;
    boolean scope_ifname_set;
    String ifname;
    transient NetworkInterface scopedIf;
    Inet6Address(byte address[]) {
        family = AF_INET6;
        ipaddress = address;
        scope_id = 0;
    }
    Inet6Address(byte address[], String name) {
        family = AF_INET6;
        hostName = name;
        ipaddress = address;
        scope_id = 0;
    }
    Inet6Address(byte address[], String name, int scope_id) {
        family = AF_INET6;
        hostName = name;
        ipaddress = address;
        this.scope_id = scope_id;
        if (scope_id != 0) {
            scope_id_set = true;
        }
    }
    public static Inet6Address getByAddress(String host, byte[] addr,
            int scope_id) throws UnknownHostException {
        if (null == addr || 16 != addr.length) {
            throw new UnknownHostException(Msg.getString("KA020")); 
        }
        if (scope_id < 0) {
            scope_id = 0;
        }
        return new Inet6Address(addr, host, scope_id);
    }
    public static Inet6Address getByAddress(String host, byte[] addr,
            NetworkInterface nif) throws UnknownHostException {
        Inet6Address address = Inet6Address.getByAddress(host, addr, 0);
        if (null == nif) {
            return address;
        }
        Enumeration<InetAddress> addressList = nif.getInetAddresses();
        while (addressList.hasMoreElements()) {
            InetAddress ia = addressList.nextElement();
            if (ia.getAddress().length == 16) {
                Inet6Address v6ia = (Inet6Address) ia;
                boolean isSameType = v6ia.compareLocalType(address);
                if (isSameType) {
                    address.scope_id_set = true;
                    address.scope_id = v6ia.scope_id;
                    address.scope_ifname_set = true;
                    address.ifname = nif.getName();
                    address.scopedIf = nif;
                    break;
                }
            }
        }
        if (!address.scope_id_set) {
            throw new UnknownHostException(Msg.getString("KA021")); 
        }
        return address;
    }
    private boolean compareLocalType(Inet6Address ia) {
        if (ia.isSiteLocalAddress() && isSiteLocalAddress()) {
            return true;
        }
        if (ia.isLinkLocalAddress() && isLinkLocalAddress()) {
            return true;
        }
        if (!ia.isSiteLocalAddress() && !ia.isLinkLocalAddress()) {
            return true;
        }
        return false;
    }
    Inet6Address(byte address[], int scope_id) {
        ipaddress = address;
        this.scope_id = scope_id;
        if (scope_id != 0) {
            scope_id_set = true;
        }
    }
    @Override
    public boolean isMulticastAddress() {
        return ipaddress[0] == -1;
    }
    @Override
    public boolean isAnyLocalAddress() {
        for (int i = 0; i < ipaddress.length; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isLoopbackAddress() {
        if (ipaddress[15] != 1) {
            return false;
        }
        for (int i = 0; i < 15; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isLinkLocalAddress() {
        return (ipaddress[0] == -2) && ((ipaddress[1] & 255) >>> 6) == 2;
    }
    @Override
    public boolean isSiteLocalAddress() {
        return (ipaddress[0] == -2) && ((ipaddress[1] & 255) >>> 6) == 3;
    }
    @Override
    public boolean isMCGlobal() {
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 14;
    }
    @Override
    public boolean isMCNodeLocal() {
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 1;
    }
    @Override
    public boolean isMCLinkLocal() {
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 2;
    }
    @Override
    public boolean isMCSiteLocal() {
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 5;
    }
    @Override
    public boolean isMCOrgLocal() {
        return (ipaddress[0] == -1) && (ipaddress[1] & 15) == 8;
    }
    public int getScopeId() {
        if (scope_id_set) {
            return scope_id;
        }
        return 0;
    }
    public NetworkInterface getScopedInterface() {
        if (scope_ifname_set) {
            return scopedIf;
        }
        return null;
    }
    public boolean isIPv4CompatibleAddress() {
        for (int i = 0; i < 12; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }
        return true;
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("ipaddress", new byte[0].getClass()), 
            new ObjectStreamField("scope_id", Integer.TYPE), 
            new ObjectStreamField("scope_id_set", Boolean.TYPE), 
            new ObjectStreamField("scope_ifname_set", Boolean.TYPE), 
            new ObjectStreamField("ifname", String.class), }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        if (ipaddress == null) {
            fields.put("ipaddress", null); 
        } else {
            fields.put("ipaddress", ipaddress); 
        }
        fields.put("scope_id", scope_id); 
        fields.put("scope_id_set", scope_id_set); 
        fields.put("scope_ifname_set", scope_ifname_set); 
        fields.put("ifname", ifname); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        ipaddress = (byte[]) fields.get("ipaddress", null); 
        scope_id = fields.get("scope_id", 0); 
        scope_id_set = fields.get("scope_id_set", false); 
        ifname = (String) fields.get("ifname", null); 
        scope_ifname_set = fields.get("scope_ifname_set", false); 
        if (scope_ifname_set && null != ifname) {
            scopedIf = NetworkInterface.getByName(ifname);
        }
    }
    @Override
    public String toString() {
        if (ifname != null) {
            return super.toString() + "%" + ifname; 
        }
        if (scope_id != 0) {
            return super.toString() + "%" + scope_id; 
        }
        return super.toString();
    }
}
