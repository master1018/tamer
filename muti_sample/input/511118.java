public class MulticastSocket extends DatagramSocket {
    final static int SO_REUSEPORT = 512;
    private InetAddress interfaceSet;
    public MulticastSocket() throws IOException {
        super();
        setReuseAddress(true);
    }
    public MulticastSocket(int aPort) throws IOException {
        super(aPort);
        setReuseAddress(true);
    }
    public InetAddress getInterface() throws SocketException {
        checkClosedAndBind(false);
        if (interfaceSet == null) {
            InetAddress ipvXaddress = (InetAddress) impl
                    .getOption(SocketOptions.IP_MULTICAST_IF);
            if (ipvXaddress.isAnyLocalAddress()) {
                NetworkInterface theInterface = getNetworkInterface();
                if (theInterface != null) {
                    Enumeration<InetAddress> addresses = theInterface
                            .getInetAddresses();
                    if (addresses != null) {
                        while (addresses.hasMoreElements()) {
                            InetAddress nextAddress = addresses.nextElement();
                            if (nextAddress instanceof Inet6Address) {
                                return nextAddress;
                            }
                        }
                    }
                }
            }
            return ipvXaddress;
        }
        return interfaceSet;
    }
    public NetworkInterface getNetworkInterface() throws SocketException {
        checkClosedAndBind(false);
        Integer theIndex = Integer.valueOf(0);
        try {
            theIndex = (Integer) impl.getOption(SocketOptions.IP_MULTICAST_IF2);
        } catch (SocketException e) {
        }
        if (theIndex.intValue() != 0) {
            Enumeration<NetworkInterface> theInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (theInterfaces.hasMoreElements()) {
                NetworkInterface nextInterface = theInterfaces.nextElement();
                if (nextInterface.getIndex() == theIndex.intValue()) {
                    return nextInterface;
                }
            }
        }
        InetAddress theAddress = (InetAddress) impl
                .getOption(SocketOptions.IP_MULTICAST_IF);
        if (theAddress != null) {
            if (!theAddress.isAnyLocalAddress()) {
                return NetworkInterface.getByInetAddress(theAddress);
            }
            InetAddress theAddresses[] = new InetAddress[1];
            if (!Socket.preferIPv4Stack() && NetUtil.preferIPv6Addresses()) {
                theAddresses[0] = Inet6Address.ANY;
            } else {
                theAddresses[0] = Inet4Address.ANY;
            }
            return new NetworkInterface(null, null, theAddresses,
                    NetworkInterface.UNSET_INTERFACE_INDEX);
        }
        return null;
    }
    public int getTimeToLive() throws IOException {
        checkClosedAndBind(false);
        return impl.getTimeToLive();
    }
    @Deprecated
    public byte getTTL() throws IOException {
        checkClosedAndBind(false);
        return impl.getTTL();
    }
    public void joinGroup(InetAddress groupAddr) throws IOException {
        checkClosedAndBind(false);
        if (!groupAddr.isMulticastAddress()) {
            throw new IOException(Msg.getString("K0039")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(groupAddr);
        }
        impl.join(groupAddr);
    }
    public void joinGroup(SocketAddress groupAddress,
            NetworkInterface netInterface) throws IOException {
        checkClosedAndBind(false);
        if (null == groupAddress) {
            throw new IllegalArgumentException(Msg.getString("K0318")); 
        }
        if ((netInterface != null) && (netInterface.getFirstAddress() == null)) {
            throw new SocketException(Msg.getString("K0335")); 
        }
        if (!(groupAddress instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", groupAddress.getClass())); 
        }
        InetAddress groupAddr = ((InetSocketAddress) groupAddress).getAddress();
        if (groupAddr == null) {
            throw new SocketException(Msg.getString("K0331")); 
        }
        if (!groupAddr.isMulticastAddress()) {
            throw new IOException(Msg.getString("K0039")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(groupAddr);
        }
        impl.joinGroup(groupAddress, netInterface);
    }
    public void leaveGroup(InetAddress groupAddr) throws IOException {
        checkClosedAndBind(false);
        if (!groupAddr.isMulticastAddress()) {
            throw new IOException(Msg.getString("K003a")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(groupAddr);
        }
        impl.leave(groupAddr);
    }
    public void leaveGroup(SocketAddress groupAddress,
            NetworkInterface netInterface) throws IOException {
        checkClosedAndBind(false);
        if (null == groupAddress) {
            throw new IllegalArgumentException(Msg.getString("K0318")); 
        }
        if ((netInterface != null) && (netInterface.getFirstAddress() == null)) {
            throw new SocketException(Msg.getString("K0335")); 
        }
        if (!(groupAddress instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", groupAddress.getClass())); 
        }
        InetAddress groupAddr = ((InetSocketAddress) groupAddress).getAddress();
        if (groupAddr == null) {
            throw new SocketException(Msg.getString("K0331")); 
        }
        if (!groupAddr.isMulticastAddress()) {
            throw new IOException(Msg.getString("K003a")); 
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkMulticast(groupAddr);
        }
        impl.leaveGroup(groupAddress, netInterface);
    }
    @Deprecated
    public void send(DatagramPacket pack, byte ttl) throws IOException {
        checkClosedAndBind(false);
        InetAddress packAddr = pack.getAddress();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            if (packAddr.isMulticastAddress()) {
                security.checkMulticast(packAddr, ttl);
            } else {
                security.checkConnect(packAddr.getHostName(), pack.getPort());
            }
        }
        int currTTL = getTimeToLive();
        if (packAddr.isMulticastAddress() && (byte) currTTL != ttl) {
            try {
                setTimeToLive(ttl & 0xff);
                impl.send(pack);
            } finally {
                setTimeToLive(currTTL);
            }
        } else {
            impl.send(pack);
        }
    }
    public void setInterface(InetAddress addr) throws SocketException {
        checkClosedAndBind(false);
        if (addr == null) {
            throw new NullPointerException();
        }
        if (addr.isAnyLocalAddress()) {
            impl.setOption(SocketOptions.IP_MULTICAST_IF, Inet4Address.ANY);
        } else if (addr instanceof Inet4Address) {
            impl.setOption(SocketOptions.IP_MULTICAST_IF, addr);
            interfaceSet = addr;
        }
        NetworkInterface theInterface = NetworkInterface.getByInetAddress(addr);
        if ((theInterface != null) && (theInterface.getIndex() != 0)) {
            try {
                impl.setOption(SocketOptions.IP_MULTICAST_IF2, Integer
                        .valueOf(theInterface.getIndex()));
            } catch (SocketException e) {
            }
        } else if (addr.isAnyLocalAddress()) {
            try {
                impl.setOption(SocketOptions.IP_MULTICAST_IF2, Integer
                        .valueOf(0));
            } catch (SocketException e) {
            }
        } else if (addr instanceof Inet6Address) {
            throw new SocketException(Msg.getString("K0338")); 
        }
    }
    public void setNetworkInterface(NetworkInterface netInterface)
            throws SocketException {
        checkClosedAndBind(false);
        if (netInterface == null) {
            throw new SocketException(Msg.getString("K0334")); 
        }
        InetAddress firstAddress = netInterface.getFirstAddress();
        if (firstAddress == null) {
            throw new SocketException(Msg.getString("K0335")); 
        }
        if (netInterface.getIndex() == NetworkInterface.UNSET_INTERFACE_INDEX) {
            impl.setOption(SocketOptions.IP_MULTICAST_IF, Inet4Address.ANY);
            try {
                impl.setOption(SocketOptions.IP_MULTICAST_IF2, Integer
                        .valueOf(NetworkInterface.NO_INTERFACE_INDEX));
            } catch (SocketException e) {
            }
        }
        Enumeration<InetAddress> theAddresses = netInterface.getInetAddresses();
        boolean found = false;
        firstAddress = null;
        while ((theAddresses.hasMoreElements()) && (found != true)) {
            InetAddress theAddress = theAddresses.nextElement();
            if (theAddress instanceof Inet4Address) {
                firstAddress = theAddress;
                found = true;
            }
        }
        if (netInterface.getIndex() == NetworkInterface.NO_INTERFACE_INDEX) {
            if (firstAddress != null) {
                impl.setOption(SocketOptions.IP_MULTICAST_IF, firstAddress);
            } else {
                throw new SocketException(Msg.getString("K0335")); 
            }
        } else {
            if (firstAddress != null) {
                impl.setOption(SocketOptions.IP_MULTICAST_IF, firstAddress);
            }
            try {
                impl.setOption(SocketOptions.IP_MULTICAST_IF2, Integer
                        .valueOf(netInterface.getIndex()));
            } catch (SocketException e) {
            }
        }
        interfaceSet = null;
    }
    public void setTimeToLive(int ttl) throws IOException {
        checkClosedAndBind(false);
        if (ttl < 0 || ttl > 255) {
            throw new IllegalArgumentException(Msg.getString("K003c")); 
        }
        impl.setTimeToLive(ttl);
    }
    @Deprecated
    public void setTTL(byte ttl) throws IOException {
        checkClosedAndBind(false);
        impl.setTTL(ttl);
    }
    @Override
    synchronized void createSocket(int aPort, InetAddress addr)
            throws SocketException {
        impl = factory != null ? factory.createDatagramSocketImpl()
                : new PlainDatagramSocketImpl();
        impl.create();
        try {
            impl.setOption(SocketOptions.SO_REUSEADDR, Boolean.TRUE);
            impl.bind(aPort, addr);
            isBound = true;
        } catch (SocketException e) {
            close();
            throw e;
        }
    }
    public MulticastSocket(SocketAddress localAddr) throws IOException {
        super(localAddr);
        setReuseAddress(true);
    }
    public boolean getLoopbackMode() throws SocketException {
        checkClosedAndBind(false);
        return !((Boolean) impl.getOption(SocketOptions.IP_MULTICAST_LOOP))
                .booleanValue();
    }
    public void setLoopbackMode(boolean loop) throws SocketException {
        checkClosedAndBind(false);
        impl.setOption(SocketOptions.IP_MULTICAST_LOOP, loop ? Boolean.FALSE
                : Boolean.TRUE);
    }
}
