public final class DatagramPacket {
    byte[] data;
    int length;
    int capacity;
    InetAddress address;
    int port = -1; 
    int offset = 0;
    public DatagramPacket(byte[] data, int length) {
        this(data, 0, length);
    }
    public DatagramPacket(byte[] data, int offset, int length) {
        super();
        setData(data, offset, length);
    }
    public DatagramPacket(byte[] data, int offset, int length,
            InetAddress host, int aPort) {
        this(data, offset, length);
        setPort(aPort);
        address = host;
    }
    public DatagramPacket(byte[] data, int length, InetAddress host, int port) {
        this(data, 0, length, host, port);
    }
    public synchronized InetAddress getAddress() {
        return address;
    }
    public synchronized byte[] getData() {
        return data;
    }
    public synchronized int getLength() {
        return length;
    }
    public synchronized int getOffset() {
        return offset;
    }
    public synchronized int getPort() {
        return port;
    }
    public synchronized void setAddress(InetAddress addr) {
        address = addr;
    }
    public synchronized void setData(byte[] buf, int anOffset, int aLength) {
        if (0 > anOffset || anOffset > buf.length || 0 > aLength
                || aLength > buf.length - anOffset) {
            throw new IllegalArgumentException(Msg.getString("K002f")); 
        }
        data = buf;
        offset = anOffset;
        length = aLength;
        capacity = aLength;
    }
    public synchronized void setData(byte[] buf) {
        length = buf.length; 
        capacity = buf.length;
        data = buf;
        offset = 0;
    }
    synchronized int getCapacity() {
        return capacity;
    }
    public synchronized void setLength(int len) {
        if (0 > len || offset + len > data.length) {
            throw new IllegalArgumentException(Msg.getString("K002f")); 
        }
        length = len;
        capacity = len;
    }
    synchronized void setLengthOnly(int len) {
        if (0 > len || offset + len > data.length) {
            throw new IllegalArgumentException(Msg.getString("K002f")); 
        }
        length = len;
    }
    public synchronized void setPort(int aPort) {
        if (aPort < 0 || aPort > 65535) {
            throw new IllegalArgumentException(Msg.getString("K0325", aPort)); 
        }
        port = aPort;
    }
    public DatagramPacket(byte[] data, int length, SocketAddress sockAddr)
            throws SocketException {
        this(data, 0, length);
        setSocketAddress(sockAddr);
    }
    public DatagramPacket(byte[] data, int offset, int length,
            SocketAddress sockAddr) throws SocketException {
        this(data, offset, length);
        setSocketAddress(sockAddr);
    }
    public synchronized SocketAddress getSocketAddress() {
        return new InetSocketAddress(getAddress(), getPort());
    }
    public synchronized void setSocketAddress(SocketAddress sockAddr) {
        if (!(sockAddr instanceof InetSocketAddress)) {
            throw new IllegalArgumentException(Msg.getString(
                    "K0316", sockAddr == null ? null : sockAddr.getClass())); 
        }
        InetSocketAddress inetAddr = (InetSocketAddress) sockAddr;
        port = inetAddr.getPort();
        address = inetAddr.getAddress();
    }
}
