public class InetAddress implements Serializable {
    private static final AddressCache addressCache = new AddressCache();
    private final static INetworkSystem NETIMPL = Platform.getNetworkSystem();
    private static final String ERRMSG_CONNECTION_REFUSED = "Connection refused"; 
    private static final long serialVersionUID = 3286316764910316507L;
    String hostName;
    private static class WaitReachable {
    }
    private transient Object waitReachable = new WaitReachable();
    private boolean reached;
    private int addrCount;
    int family = 0;
    static final int AF_INET = 2;
    static final int AF_INET6 = 10;
    byte[] ipaddress;
    InetAddress() {
        super();
    }
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InetAddress)) {
            return false;
        }
        return Arrays.equals(this.ipaddress, ((InetAddress) obj).ipaddress);
    }
    public byte[] getAddress() {
        return ipaddress.clone();
    }
    static final Comparator<byte[]> SHORTEST_FIRST = new Comparator<byte[]>() {
        public int compare(byte[] a1, byte[] a2) {
            return a1.length - a2.length;
        }
    };
    static InetAddress[] bytesToInetAddresses(byte[][] rawAddresses,
            String hostName) {
        if (!NetUtil.preferIPv6Addresses()) {
            Arrays.sort(rawAddresses, SHORTEST_FIRST);
        }
        InetAddress[] returnedAddresses = new InetAddress[rawAddresses.length];
        for (int i = 0; i < rawAddresses.length; i++) {
            byte[] rawAddress = rawAddresses[i];
            if (rawAddress.length == 16) {
                returnedAddresses[i] = new Inet6Address(rawAddress, hostName);
            } else if (rawAddress.length == 4) {
                returnedAddresses[i] = new Inet4Address(rawAddress, hostName);
            } else {
              throw new AssertionError("Impossible address length " +
                                       rawAddress.length);
            }
        }
        return returnedAddresses;
    }
    public static InetAddress[] getAllByName(String host)
            throws UnknownHostException {
        return getAllByNameImpl(host, true);
    }
    static InetAddress[] getAllByNameImpl(String host, boolean returnUnshared)
            throws UnknownHostException {
        if (host == null || 0 == host.length()) {
            if (NetUtil.preferIPv6Addresses()) {
                return new InetAddress[] { Inet6Address.LOOPBACK,
                                           Inet4Address.LOOPBACK };
            } else {
                return new InetAddress[] { Inet4Address.LOOPBACK,
                                           Inet6Address.LOOPBACK };
            }
        }
        if (host.equals("0")) { 
            return new InetAddress[] { Inet4Address.ANY };
        }
        if (isHostName(host)) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(host, -1);
            }
            if (returnUnshared) {
                return lookupHostByName(host).clone();
            } else {
                return lookupHostByName(host);
            }
        }
        byte[] hBytes = NETIMPL.ipStringToByteArray(host);
        if (hBytes.length == 4) {
            return (new InetAddress[] { new Inet4Address(hBytes) });
        } else if (hBytes.length == 16) {
            return (new InetAddress[] { new Inet6Address(hBytes) });
        } else {
            throw new UnknownHostException(
                    Msg.getString("K0339")); 
        }
    }
    public static InetAddress getByName(String host) throws UnknownHostException {
        return getAllByNameImpl(host, false)[0];
    }
    private static String ipAddressToString(byte[] ipAddress) {
        try {
            return NETIMPL.byteArrayToIpString(ipAddress);
        } catch (IOException ex) {
            throw new IllegalArgumentException("byte[] neither 4 nor 16 bytes", ex);
        }
    }
    public String getHostAddress() {
        return ipAddressToString(ipaddress);
    }
    public String getHostName() {
        try {
            if (hostName == null) {
                int address = 0;
                if (ipaddress.length == 4) {
                    address = bytesToInt(ipaddress, 0);
                    if (address == 0) {
                        return hostName = ipAddressToString(ipaddress);
                    }
                }
                hostName = getHostByAddrImpl(ipaddress).hostName;
                if (hostName.equals("localhost") && ipaddress.length == 4 
                        && address != 0x7f000001) {
                    return hostName = ipAddressToString(ipaddress);
                }
            }
        } catch (UnknownHostException e) {
            return hostName = ipAddressToString(ipaddress);
        }
        SecurityManager security = System.getSecurityManager();
        try {
            if (security != null && isHostName(hostName)) {
                security.checkConnect(hostName, -1);
            }
        } catch (SecurityException e) {
            return ipAddressToString(ipaddress);
        }
        return hostName;
    }
    public String getCanonicalHostName() {
        String canonicalName;
        try {
            int address = 0;
            if (ipaddress.length == 4) {
                address = bytesToInt(ipaddress, 0);
                if (address == 0) {
                    return ipAddressToString(ipaddress);
                }
            }
            canonicalName = getHostByAddrImpl(ipaddress).hostName;
        } catch (UnknownHostException e) {
            return ipAddressToString(ipaddress);
        }
        SecurityManager security = System.getSecurityManager();
        try {
            if (security != null && isHostName(canonicalName)) {
                security.checkConnect(canonicalName, -1);
            }
        } catch (SecurityException e) {
            return ipAddressToString(ipaddress);
        }
        return canonicalName;
    }
    public static InetAddress getLocalHost() throws UnknownHostException {
        String host = gethostname();
        SecurityManager security = System.getSecurityManager();
        try {
            if (security != null) {
                security.checkConnect(host, -1);
            }
        } catch (SecurityException e) {
            return Inet4Address.LOOPBACK;
        }
        return lookupHostByName(host)[0];
    }
    private static native String gethostname();
    @Override
    public int hashCode() {
        return Arrays.hashCode(ipaddress);
    }
    public boolean isMulticastAddress() {
        return false;
    }
    private static InetAddress[] lookupHostByName(String host) throws UnknownHostException {
        InetAddress[] cachedResult = addressCache.get(host);
        if (cachedResult != null) {
            if (cachedResult.length > 0) {
                return cachedResult;
            } else {
                throw new UnknownHostException(host);
            }
        }
        try {
            InetAddress[] addresses = bytesToInetAddresses(getaddrinfo(host), host);
            addressCache.put(host, addresses);
            return addresses;
        } catch (UnknownHostException e) {
            addressCache.putUnknownHost(host);
            throw new UnknownHostException(host);
        }
    }
    private static native byte[][] getaddrinfo(String name) throws UnknownHostException;
    static InetAddress getHostByAddrImpl(byte[] addr)
            throws UnknownHostException {
        if (addr.length == 4) {
            return new Inet4Address(addr, getnameinfo(addr));
        } else if (addr.length == 16) {
            return new Inet6Address(addr, getnameinfo(addr));
        } else {
            throw new UnknownHostException(Msg.getString(
                    "K0339")); 
        }
    }
    private static native String getnameinfo(byte[] addr);
    static String getHostNameInternal(String host, boolean isCheck) throws UnknownHostException {
        if (host == null || 0 == host.length()) {
            return Inet4Address.LOOPBACK.getHostAddress();
        }
        if (isHostName(host)) {
            if (isCheck) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkConnect(host, -1);
                }
            }
            return lookupHostByName(host)[0].getHostAddress();
        }
        return host;
    }
    @Override
    public String toString() {
        return (hostName == null ? "" : hostName) + "/" + getHostAddress(); 
    }
    private static boolean isHostName(String value) {
        try {
            NETIMPL.ipStringToByteArray(value);
            return false;
        } catch (UnknownHostException e) {
            return true;
        }
    }
    public boolean isLoopbackAddress() {
        return false;
    }
    public boolean isLinkLocalAddress() {
        return false;
    }
    public boolean isSiteLocalAddress() {
        return false;
    }
    public boolean isMCGlobal() {
        return false;
    }
    public boolean isMCNodeLocal() {
        return false;
    }
    public boolean isMCLinkLocal() {
        return false;
    }
    public boolean isMCSiteLocal() {
        return false;
    }
    public boolean isMCOrgLocal() {
        return false;
    }
    public boolean isAnyLocalAddress() {
        return false;
    }
    public boolean isReachable(int timeout) throws IOException {
        return isReachable(null, 0, timeout);
    }
    public boolean isReachable(NetworkInterface netif, final int ttl,
            final int timeout) throws IOException {
        if (0 > ttl || 0 > timeout) {
            throw new IllegalArgumentException(Msg.getString("K0051")); 
        }
        boolean reachable = false;
        if (null == netif) {
                reachable = isReachableByTCP(this, null, timeout);
        } else {
            if (null == netif.addresses) {
                return false;
            }
                reachable = isReachableByMultiThread(netif, ttl, timeout);
        }
        return reachable;
    }
    private boolean isReachableByMultiThread(NetworkInterface netif,
            final int ttl, final int timeout)
            throws IOException {
        if (null == netif.addresses) {
            return false;
        }
        Enumeration<InetAddress> addresses = netif.getInetAddresses();
        reached = false;
        addrCount = netif.addresses.length;
        boolean needWait = false;
        while (addresses.hasMoreElements()) {
            final InetAddress addr = addresses.nextElement();
            if (addr.isLoopbackAddress()) {
                Enumeration<NetworkInterface> NetworkInterfaces = NetworkInterface
                        .getNetworkInterfaces();
                while (NetworkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = NetworkInterfaces
                            .nextElement();
                    Enumeration<InetAddress> localAddresses = networkInterface
                            .getInetAddresses();
                    while (localAddresses.hasMoreElements()) {
                        if (InetAddress.this.equals(localAddresses
                                .nextElement())) {
                            return true;
                        }
                    }
                }
                synchronized (waitReachable) {
                    addrCount--;
                    if (addrCount == 0) {
                        waitReachable.notifyAll();
                    }
                }
                continue;
            }
            needWait = true;
            new Thread() {
                @Override
                public void run() {
                    boolean threadReached = false;
                        try {
                            threadReached = isReachableByTCP(addr,
                                    InetAddress.this, timeout);
                        } catch (IOException e) {
                        }
                    synchronized (waitReachable) {
                        if (threadReached) {
                            reached = true;
                            waitReachable.notifyAll();
                        } else {
                            addrCount--;
                            if (0 == addrCount) {
                                waitReachable.notifyAll();
                            }
                        }
                    }
                }
            }.start();
        }
        if (needWait) {
            synchronized (waitReachable) {
                try {
                    while (!reached && (addrCount != 0)) {
                        waitReachable.wait(1000);
                    }
                } catch (InterruptedException e) {
                }
                return reached;
            }
        }
        return false;
    }
    private boolean isReachableByTCP(InetAddress dest, InetAddress source,
            int timeout) throws IOException {
        FileDescriptor fd = new FileDescriptor();
        int traffic = 0;
        boolean reached = false;
        NETIMPL.createStreamSocket(fd, NetUtil.preferIPv4Stack());
        try {
            if (null != source) {
                NETIMPL.bind(fd, source, 0);
            }
            NETIMPL.connectStreamWithTimeoutSocket(fd, 7, timeout, traffic,
                    dest);
            reached = true;
        } catch (IOException e) {
            if (ERRMSG_CONNECTION_REFUSED.equals(e.getMessage())) {
                reached = true;
            }
        }
        NETIMPL.socketClose(fd);
        return reached;
    }
    public static InetAddress getByAddress(byte[] ipAddress)
            throws UnknownHostException {
        return getByAddressInternal(null, ipAddress, 0);
    }
    static InetAddress getByAddress(byte[] ipAddress, int scope_id)
            throws UnknownHostException {
        return getByAddressInternal(null, ipAddress, scope_id);
    }
    private static boolean isIPv4MappedAddress(byte ipAddress[]) {
        if (ipAddress == null || ipAddress.length != 16) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (ipAddress[i] != 0) {
                return false;
            }
        }
        if (ipAddress[10] != -1 || ipAddress[11] != -1) {
            return false;
        }
        return true;
    }
    private static byte[] ipv4MappedToIPv4(byte[] mappedAddress) {
        byte[] ipv4Address = new byte[4];
        for(int i = 0; i < 4; i++) {
            ipv4Address[i] = mappedAddress[12 + i];
        }
        return ipv4Address;
    }
    public static InetAddress getByAddress(String hostName, byte[] ipAddress)
            throws UnknownHostException {
        return getByAddressInternal(hostName, ipAddress, 0);
    }
    static InetAddress getByAddressInternal(String hostName, byte[] ipAddress,
            int scope_id) throws UnknownHostException {
        if (ipAddress == null) {
            throw new UnknownHostException(
                Msg.getString("K0331", hostName)); 
        }
        switch (ipAddress.length) {
            case 4:
                return new Inet4Address(ipAddress.clone());
            case 16:
                if (isIPv4MappedAddress(ipAddress)) {
                    return new Inet4Address(ipv4MappedToIPv4(ipAddress));
                } else {
                    return new Inet6Address(ipAddress.clone(), scope_id);
                }
            default:
                if (hostName != null) {
                    throw new UnknownHostException(
                            Msg.getString("K0332", hostName)); 
                } else {
                    throw new UnknownHostException(
                            Msg.getString("K0339")); 
                }
        }
    }
    static void intToBytes(int value, byte bytes[], int start) {
        bytes[start] = (byte) ((value >> 24) & 255);
        bytes[start + 1] = (byte) ((value >> 16) & 255);
        bytes[start + 2] = (byte) ((value >> 8) & 255);
        bytes[start + 3] = (byte) (value & 255);
    }
    static int bytesToInt(byte bytes[], int start) {
        int value = ((bytes[start + 3] & 255))
                | ((bytes[start + 2] & 255) << 8)
                | ((bytes[start + 1] & 255) << 16)
                | ((bytes[start] & 255) << 24);
        return value;
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("address", Integer.TYPE), 
            new ObjectStreamField("family", Integer.TYPE), 
            new ObjectStreamField("hostName", String.class) }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        if (ipaddress == null) {
            fields.put("address", 0); 
        } else {
            fields.put("address", bytesToInt(ipaddress, 0)); 
        }
        fields.put("family", family); 
        fields.put("hostName", hostName); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        int addr = fields.get("address", 0); 
        ipaddress = new byte[4];
        intToBytes(addr, ipaddress, 0);
        hostName = (String) fields.get("hostName", null); 
        family = fields.get("family", 2); 
    }
    private Object readResolve() throws ObjectStreamException {
        return new Inet4Address(ipaddress, hostName);
    }
}
