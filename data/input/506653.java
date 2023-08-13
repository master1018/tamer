public final class NetworkInterface extends Object {
    private static final int CHECK_CONNECT_NO_PORT = -1;
    static final int NO_INTERFACE_INDEX = 0;
    static final int UNSET_INTERFACE_INDEX = -1;
    private String name;
    private String displayName;
    InetAddress addresses[];
    private int interfaceIndex;
    private int hashCode;
    private static native InterfaceAddress[] getInterfaceAddresses() throws SocketException;
    private static NetworkInterface[] getNetworkInterfacesImpl() throws SocketException {
        Map<String, NetworkInterface> networkInterfaces = new LinkedHashMap<String, NetworkInterface>();
        for (InterfaceAddress ia : getInterfaceAddresses()) {
            if (ia != null) { 
                String name = ia.name;
                NetworkInterface ni = networkInterfaces.get(name);
                if (ni == null) {
                    ni = new NetworkInterface(name, name, new InetAddress[] { ia.address }, ia.index);
                    networkInterfaces.put(name, ni);
                } else {
                    ni.addInterfaceAddress(ia.address);
                }
            }
        }
        return networkInterfaces.values().toArray(new NetworkInterface[networkInterfaces.size()]);
    }
    private void addInterfaceAddress(InetAddress address) {
        InetAddress[] newAddresses = new InetAddress[addresses.length + 1];
        System.arraycopy(addresses, 0, newAddresses, 0, addresses.length);
        newAddresses[addresses.length] = address;
        addresses = newAddresses;
    }
    NetworkInterface(String name, String displayName, InetAddress addresses[],
            int interfaceIndex) {
        this.name = name;
        this.displayName = displayName;
        this.addresses = addresses;
        this.interfaceIndex = interfaceIndex;
    }
    int getIndex() {
        return interfaceIndex;
    }
    InetAddress getFirstAddress() {
        if ((addresses != null) && (addresses.length >= 1)) {
            return addresses[0];
        }
        return null;
    }
    public String getName() {
        return name;
    }
    public Enumeration<InetAddress> getInetAddresses() {
        if (addresses == null) {
            return new Vector<InetAddress>(0).elements();
        }
        Vector<InetAddress> accessibleAddresses = new Vector<InetAddress>(
                addresses.length);
        SecurityManager security = System.getSecurityManager();
        if (security == null) {
            return (new Vector<InetAddress>(Arrays.asList(addresses)))
                    .elements();
        }
        for (InetAddress element : addresses) {
            if (security != null) {
                try {
                    security.checkConnect(element.getHostName(),
                            CHECK_CONNECT_NO_PORT);
                    accessibleAddresses.add(element);
                } catch (SecurityException e) {
                }
            }
        }
        Enumeration<InetAddress> theAccessibleElements = accessibleAddresses
                .elements();
        if (theAccessibleElements.hasMoreElements()) {
            return accessibleAddresses.elements();
        }
        return new Vector<InetAddress>(0).elements();
    }
    public String getDisplayName() {
        if (!(displayName.equals(""))) { 
            return displayName;
        }
        return name;
    }
    public static NetworkInterface getByName(String interfaceName)
            throws SocketException {
        if (interfaceName == null) {
            throw new NullPointerException(Msg.getString("K0330")); 
        }
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface netif = interfaces.nextElement();
                if (netif.getName().equals(interfaceName)) {
                    return netif;
                }
            }
        }
        return null;
    }
    public static NetworkInterface getByInetAddress(InetAddress address)
            throws SocketException {
        if (address == null) {
            throw new NullPointerException(Msg.getString("K0331")); 
        }
        Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
        if (interfaces != null) {
            while (interfaces.hasMoreElements()) {
                NetworkInterface netif = interfaces.nextElement();
                if ((netif.addresses != null) && (netif.addresses.length != 0)) {
                    Enumeration<InetAddress> netifAddresses = (new Vector<InetAddress>(
                            Arrays.asList(netif.addresses))).elements();
                    if (netifAddresses != null) {
                        while (netifAddresses.hasMoreElements()) {
                            if (address.equals(netifAddresses.nextElement())) {
                                return netif;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    public static Enumeration<NetworkInterface> getNetworkInterfaces()
            throws SocketException {
        NetworkInterface[] interfaces = getNetworkInterfacesImpl();
        if (interfaces == null) {
            return null;
        }
        for (NetworkInterface netif : interfaces) {
            if (netif.addresses != null) {
                for (InetAddress addr : netif.addresses) {
                    if (16 == addr.ipaddress.length) {
                        if (addr.isLinkLocalAddress()
                                || addr.isSiteLocalAddress()) {
                            ((Inet6Address) addr).scopedIf = netif;
                            ((Inet6Address) addr).ifname = netif.name;
                            ((Inet6Address) addr).scope_ifname_set = true;
                        }
                    }
                }
            }
        }
        return (new Vector<NetworkInterface>(Arrays.asList(interfaces)))
                .elements();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface netif = (NetworkInterface) obj;
        if (netif.getIndex() != interfaceIndex) {
            return false;
        }
        if (!(name.equals("")) && (!netif.getName().equals(name))) { 
            return false;
        }
        if ((name.equals("")) && (!netif.getName().equals(displayName))) { 
            return false;
        }
        Enumeration<InetAddress> netifAddresses = netif.getInetAddresses();
        Enumeration<InetAddress> localifAddresses = getInetAddresses();
        if (netifAddresses == null) {
            return localifAddresses == null;
        }
        if (localifAddresses == null) {
            return false;
        }
        while (netifAddresses.hasMoreElements()
                && localifAddresses.hasMoreElements()) {
            if (!(localifAddresses.nextElement()).equals(
                    netifAddresses.nextElement())) {
                return false;
            }
        }
        return !netifAddresses.hasMoreElements()
                && !localifAddresses.hasMoreElements();
    }
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = name.hashCode();
        }
        return hashCode;
    }
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(25);
        string.append("["); 
        string.append(name);
        string.append("]["); 
        string.append(displayName);
        string.append("]["); 
        string.append(interfaceIndex);
        string.append("]"); 
        Enumeration<InetAddress> theAddresses = getInetAddresses();
        if (theAddresses != null) {
            while (theAddresses.hasMoreElements()) {
                InetAddress nextAddress = theAddresses.nextElement();
                string.append("["); 
                string.append(nextAddress.toString());
                string.append("]"); 
            }
        }
        return string.toString();
    }
}
