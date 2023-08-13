public class PlainDatagramSocketImpl extends DatagramSocketImpl {
    static final int MULTICAST_IF = 1;
    static final int MULTICAST_TTL = 2;
    static final int TCP_NODELAY = 4;
    static final int FLAG_SHUTDOWN = 8;
    private final static int SO_BROADCAST = 32;
    final static int IP_MULTICAST_ADD = 19;
    final static int IP_MULTICAST_DROP = 20;
    final static int IP_MULTICAST_TTL = 17;
    static final int REUSEADDR_AND_REUSEPORT = 10001;
    private boolean bindToDevice = false;
    private byte[] ipaddress = { 0, 0, 0, 0 };
    private int ttl = 1;
    private INetworkSystem netImpl = Platform.getNetworkSystem();
    private volatile boolean isNativeConnected;
    public int receiveTimeout;
    public boolean streaming = true;
    public boolean shutdownInput;
    private InetAddress connectedAddress;
    private int connectedPort = -1;
    private int trafficClass;
    public PlainDatagramSocketImpl(FileDescriptor fd, int localPort) {
        super();
        this.fd = fd;
        this.localPort = localPort;
    }
    public PlainDatagramSocketImpl() {
        super();
        fd = new FileDescriptor();
    }
    @Override
    public void bind(int port, InetAddress addr) throws SocketException {
        String prop = AccessController.doPrivileged(new PriviAction<String>("bindToDevice")); 
        boolean useBindToDevice = prop != null && prop.toLowerCase().equals("true"); 
        netImpl.bind(fd, addr, port);
        if (0 != port) {
            localPort = port;
        } else {
            localPort = netImpl.getSocketLocalPort(fd);
        }
        try {
            setOption(SO_BROADCAST, Boolean.TRUE);
        } catch (IOException e) {
        }
    }
    @Override
    public void close() {
        synchronized (fd) {
            if (fd.valid()) {
                try {
                    netImpl.socketClose(fd);
                } catch (IOException e) {
                }
                fd = new FileDescriptor();
            }
        }
    }
    @Override
    public void create() throws SocketException {
        netImpl.createDatagramSocket(fd, NetUtil.preferIPv4Stack());
    }
    @Override
    protected void finalize() {
        close();
    }
    public Object getOption(int optID) throws SocketException {
        if (optID == SocketOptions.SO_TIMEOUT) {
            return Integer.valueOf(receiveTimeout);
        } else if (optID == SocketOptions.IP_TOS) {
            return Integer.valueOf(trafficClass);
        } else {
            Object result = netImpl.getSocketOption(fd, optID);
            if (optID == SocketOptions.IP_MULTICAST_IF
                    && (netImpl.getSocketFlags() & MULTICAST_IF) != 0) {
                try {
                    return InetAddress.getByAddress(ipaddress);
                } catch (UnknownHostException e) {
                    return null;
                }
            }
            return result;
        }
    }
    @Override
    public int getTimeToLive() throws IOException {
        int result = (((Byte) getOption(IP_MULTICAST_TTL)).byteValue()) & 0xFF;
        if ((netImpl.getSocketFlags() & MULTICAST_TTL) != 0) {
            return ttl;
        }
        return result;
    }
    @Override
    public byte getTTL() throws IOException {
        byte result = ((Byte) getOption(IP_MULTICAST_TTL)).byteValue();
        if ((netImpl.getSocketFlags() & MULTICAST_TTL) != 0) {
            return (byte) ttl;
        }
        return result;
    }
    @Override
    public void join(InetAddress addr) throws IOException {
        setOption(IP_MULTICAST_ADD, new GenericIPMreq(addr));
    }
    @Override
    public void joinGroup(SocketAddress addr, NetworkInterface netInterface) throws IOException {
        if (addr instanceof InetSocketAddress) {
            InetAddress groupAddr = ((InetSocketAddress) addr).getAddress();
            setOption(IP_MULTICAST_ADD, new GenericIPMreq(groupAddr, netInterface));
        }
    }
    @Override
    public void leave(InetAddress addr) throws IOException {
        setOption(IP_MULTICAST_DROP, new GenericIPMreq(addr));
    }
    @Override
    public void leaveGroup(SocketAddress addr, NetworkInterface netInterface)
            throws IOException {
        if (addr instanceof InetSocketAddress) {
            InetAddress groupAddr = ((InetSocketAddress) addr).getAddress();
            setOption(IP_MULTICAST_DROP, new GenericIPMreq(groupAddr, netInterface));
        }
    }
    @Override
    protected int peek(InetAddress sender) throws IOException {
        if (isNativeConnected) {
            byte[] storageArray = new byte[10];
            DatagramPacket pack = new DatagramPacket(storageArray, storageArray.length);
            netImpl.recvConnectedDatagram(fd, pack, pack.getData(), pack.getOffset(), pack
                    .getLength(), receiveTimeout, true); 
            netImpl.setInetAddress(sender, connectedAddress.getAddress());
            return connectedPort;
        }
        return netImpl.peekDatagram(fd, sender, receiveTimeout);
    }
    @Override
    public void receive(DatagramPacket pack) throws java.io.IOException {
        try {
            if (isNativeConnected) {
                netImpl.recvConnectedDatagram(fd, pack, pack.getData(), pack.getOffset(), pack
                        .getLength(), receiveTimeout, false);
                updatePacketRecvAddress(pack);
            } else {
                netImpl.receiveDatagram(fd, pack, pack.getData(), pack.getOffset(), pack
                        .getLength(), receiveTimeout, false);
            }
        } catch (InterruptedIOException e) {
            throw new SocketTimeoutException(e.getMessage());
        }
    }
    @Override
    public void send(DatagramPacket packet) throws IOException {
        if (isNativeConnected) {
            netImpl.sendConnectedDatagram(fd, packet.getData(), packet.getOffset(), packet
                    .getLength(), bindToDevice);
        } else {
            netImpl.sendDatagram(fd, packet.getData(), packet.getOffset(), packet.getLength(),
                    packet.getPort(), bindToDevice, trafficClass, packet.getAddress());
        }
    }
    public void setOption(int optID, Object val) throws SocketException {
        if (optID == SocketOptions.SO_REUSEADDR) {
            optID = REUSEADDR_AND_REUSEPORT;
        }
        if (optID == SocketOptions.SO_TIMEOUT) {
            receiveTimeout = ((Integer) val).intValue();
        } else {
            int flags = netImpl.getSocketFlags();
            try {
                netImpl.setSocketOption(fd, optID | (flags << 16), val);
            } catch (SocketException e) {
                if (optID != SocketOptions.IP_TOS) {
                    throw e;
                }
            }
            if (optID == SocketOptions.IP_MULTICAST_IF && (flags & MULTICAST_IF) != 0) {
                InetAddress inet = (InetAddress) val;
                if (NetUtil.bytesToInt(inet.getAddress(), 0) == 0 || inet.isLoopbackAddress()) {
                    ipaddress = ((InetAddress) val).getAddress();
                } else {
                    InetAddress local = null;
                    try {
                        local = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        throw new SocketException("getLocalHost(): " + e.toString());
                    }
                    if (inet.equals(local)) {
                        ipaddress = ((InetAddress) val).getAddress();
                    } else {
                        throw new SocketException(val + " != getLocalHost(): " + local);
                    }
                }
            }
            if (optID == SocketOptions.IP_TOS) {
                trafficClass = ((Integer) val).intValue();
            }
        }
    }
    @Override
    public void setTimeToLive(int ttl) throws java.io.IOException {
        setOption(IP_MULTICAST_TTL, Integer.valueOf(ttl));
        if ((netImpl.getSocketFlags() & MULTICAST_TTL) != 0) {
            this.ttl = ttl;
        }
    }
    @Override
    public void setTTL(byte ttl) throws java.io.IOException {
        setTimeToLive(ttl);
    }
    @Override
    public void connect(InetAddress inetAddr, int port) throws SocketException {
        netImpl.connectDatagram(fd, port, trafficClass, inetAddr);
        try {
            connectedAddress = InetAddress.getByAddress(inetAddr.getAddress());
        } catch (UnknownHostException e) {
            throw new SocketException(Msg.getString("K0317", inetAddr.getHostName())); 
        }
        connectedPort = port;
        isNativeConnected = true;
    }
    @Override
    public void disconnect() {
        try {
            netImpl.disconnectDatagram(fd);
        } catch (Exception e) {
        }
        connectedPort = -1;
        connectedAddress = null;
        isNativeConnected = false;
    }
    @Override
    public int peekData(DatagramPacket pack) throws IOException {
        try {
            if (isNativeConnected) {
                netImpl.recvConnectedDatagram(fd, pack, pack.getData(), pack.getOffset(), pack
                        .getLength(), receiveTimeout, true); 
                updatePacketRecvAddress(pack);
            } else {
                netImpl.receiveDatagram(fd, pack, pack.getData(), pack.getOffset(), pack
                        .getLength(), receiveTimeout, true); 
            }
        } catch (InterruptedIOException e) {
            throw new SocketTimeoutException(e.toString());
        }
        return pack.getPort();
    }
    private void updatePacketRecvAddress(DatagramPacket packet) {
        packet.setAddress(connectedAddress);
        packet.setPort(connectedPort);
    }
}
