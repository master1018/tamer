final class OSNetworkSystem implements INetworkSystem {
    private static final int ERRORCODE_SOCKET_TIMEOUT = -209;
    private static final int ERRORCODE_SOCKET_INTERRUPTED = -208;
    private static final int INETADDR_REACHABLE = 0;
    private static OSNetworkSystem singleton = new OSNetworkSystem();
    public static OSNetworkSystem getOSNetworkSystem() {
        return singleton;
    }
    private OSNetworkSystem() {
        super();
    }
    public void accept(FileDescriptor fdServer, SocketImpl newSocket,
            FileDescriptor fdnewSocket, int timeout) throws IOException {
        acceptSocketImpl(fdServer, newSocket, fdnewSocket, timeout);
    }
    static native void acceptSocketImpl(FileDescriptor fdServer,
            SocketImpl newSocket, FileDescriptor fdnewSocket, int timeout)
            throws IOException;
    public int availableStream(FileDescriptor fd) throws SocketException {
        return availableStreamImpl(fd);
    }
    static native int availableStreamImpl(FileDescriptor aFD) throws SocketException;
    public void bind(FileDescriptor fd, InetAddress inetAddress, int port) throws SocketException {
        socketBindImpl(fd, port, inetAddress);
    }
    static native void socketBindImpl(FileDescriptor aFD, int port, InetAddress inetAddress) throws SocketException;
    public void connect(FileDescriptor fd, int trafficClass,
            InetAddress inetAddress, int port) throws IOException{
        connectStreamWithTimeoutSocketImpl(fd, port, 0, trafficClass, inetAddress);
    }
    public void connectDatagram(FileDescriptor fd, int port,
            int trafficClass, InetAddress inetAddress) throws SocketException {
        connectDatagramImpl2(fd, port, trafficClass, inetAddress);
    }
    static native void connectDatagramImpl2(FileDescriptor aFD, int port,
            int trafficClass, InetAddress inetAddress) throws SocketException;
    public void connectStreamWithTimeoutSocket(FileDescriptor aFD,
            int aport, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException {
        connectStreamWithTimeoutSocketImpl(aFD, aport, timeout, trafficClass,
                inetAddress);
    }
    static native void connectStreamWithTimeoutSocketImpl(FileDescriptor aFD,
            int aport, int timeout, int trafficClass, InetAddress inetAddress)
            throws IOException;
    public int connectWithTimeout(FileDescriptor fd, int timeout,
            int trafficClass, InetAddress inetAddress, int port, int step,
            byte[] context) throws IOException {
        return connectWithTimeoutSocketImpl(fd, timeout, trafficClass,
                inetAddress, port, step, context);
    }
    static native int connectWithTimeoutSocketImpl(FileDescriptor aFD,
            int timeout, int trafficClass, InetAddress hostname, int port, int step,
            byte[] context);
    public void createDatagramSocket(FileDescriptor fd,
            boolean preferIPv4Stack) throws SocketException {
        createDatagramSocketImpl(fd, preferIPv4Stack);
    }
    static native void createDatagramSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;
    public void createServerStreamSocket(FileDescriptor fd,
            boolean preferIPv4Stack) throws SocketException {
        createServerStreamSocketImpl(fd, preferIPv4Stack);
    }
    static native void createServerStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;
    public void createStreamSocket(FileDescriptor fd,
            boolean preferIPv4Stack) throws SocketException {
        createStreamSocketImpl(fd, preferIPv4Stack);
    }
    static native void createStreamSocketImpl(FileDescriptor aFD,
            boolean preferIPv4Stack) throws SocketException;
    public void disconnectDatagram(FileDescriptor fd)
            throws SocketException {
        disconnectDatagramImpl(fd);
    }
    static native void disconnectDatagramImpl(FileDescriptor aFD)
            throws SocketException;
    public InetAddress getHostByAddr(byte[] ipAddress)
            throws UnknownHostException {
        return InetAddress.getByAddress(ipAddress);
    }
    public InetAddress getHostByName(String hostName) throws UnknownHostException {
        return InetAddress.getByName(hostName);
    }
    public int getSocketFlags() {
        return getSocketFlagsImpl();
    }
    public native String byteArrayToIpString(byte[] address)
            throws UnknownHostException;
    public native byte[] ipStringToByteArray(String address)
            throws UnknownHostException;
    static native int getSocketFlagsImpl();
    public InetAddress getSocketLocalAddress(FileDescriptor fd) {
        return getSocketLocalAddressImpl(fd);
    }
    static native InetAddress getSocketLocalAddressImpl(FileDescriptor aFD);
    public int getSocketLocalPort(FileDescriptor aFD) {
        return getSocketLocalPortImpl(aFD);
    }
    static native int getSocketLocalPortImpl(FileDescriptor aFD);
    public Object getSocketOption(FileDescriptor fd, int opt)
            throws SocketException {
        return getSocketOptionImpl(fd, opt);
    }
    static native Object getSocketOptionImpl(FileDescriptor aFD, int opt)
            throws SocketException;
    public native Channel inheritedChannel();
    public void listenStreamSocket(FileDescriptor aFD, int backlog)
            throws SocketException {
        listenStreamSocketImpl(aFD, backlog);
    }
    static native void listenStreamSocketImpl(FileDescriptor aFD, int backlog)
            throws SocketException;
    public int peekDatagram(FileDescriptor fd, InetAddress sender,
            int receiveTimeout) throws IOException {
        return peekDatagramImpl(fd, sender, receiveTimeout);
    }
    static native int peekDatagramImpl(FileDescriptor aFD,
            InetAddress sender, int receiveTimeout) throws IOException;
    public int read(FileDescriptor fd, byte[] data, int offset, int count,
            int timeout) throws IOException {
        if (offset < 0 || count < 0 || offset > data.length - count) {
            throw new IllegalArgumentException("data.length=" + data.length + " offset=" + offset +
                    " count=" + count);
        }
        return readSocketImpl(fd, data, offset, count, timeout);
    }
    static native int readSocketImpl(FileDescriptor aFD, byte[] data,
            int offset, int count, int timeout) throws IOException;
    public int readDirect(FileDescriptor fd, int address, int count,
            int timeout) throws IOException {
        return readSocketDirectImpl(fd, address, count, timeout);
    }
    static native int readSocketDirectImpl(FileDescriptor aFD, int address, int count,
            int timeout) throws IOException;
    public int receiveDatagram(FileDescriptor fd, DatagramPacket packet,
            byte[] data, int offset, int length, int receiveTimeout,
            boolean peek) throws IOException {
        return receiveDatagramImpl(fd, packet, data, offset, length,
                receiveTimeout, peek);
    }
    static native int receiveDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    public int receiveDatagramDirect(FileDescriptor fd,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException {
        return receiveDatagramDirectImpl(fd, packet, address, offset, length,
                receiveTimeout, peek);
    }
    static native int receiveDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    public int recvConnectedDatagram(FileDescriptor fd,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException {
        return recvConnectedDatagramImpl(fd, packet, data, offset, length,
                receiveTimeout, peek);
    }
    static native int recvConnectedDatagramImpl(FileDescriptor aFD,
            DatagramPacket packet, byte[] data, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    public int recvConnectedDatagramDirect(FileDescriptor aFD, DatagramPacket packet, int address,
            int offset, int length, int receiveTimeout, boolean peek)
            throws IOException {
        return recvConnectedDatagramDirectImpl(aFD, packet, address, offset, length, receiveTimeout, peek);
    }
    static native int recvConnectedDatagramDirectImpl(FileDescriptor aFD,
            DatagramPacket packet, int address, int offset, int length,
            int receiveTimeout, boolean peek) throws IOException;
    public boolean select(FileDescriptor[] readFDs, FileDescriptor[] writeFDs,
            int numReadable, int numWritable, long timeout, int[] flags)
            throws SocketException {
        if (numReadable < 0 || numWritable < 0) {
            throw new IllegalArgumentException();
        }
        int total = numReadable + numWritable;
        if (total == 0) {
            return true;
        }
        assert validateFDs(readFDs, writeFDs, numReadable, numWritable) : "Invalid file descriptor arrays"; 
        return selectImpl(readFDs, writeFDs, numReadable, numWritable, flags, timeout);
    }
    static native boolean selectImpl(FileDescriptor[] readfd,
            FileDescriptor[] writefd, int cread, int cwirte, int[] flags,
            long timeout);
    public int sendConnectedDatagram(FileDescriptor fd, byte[] data,
            int offset, int length, boolean bindToDevice) throws IOException {
        return sendConnectedDatagramImpl(fd, data, offset, length, bindToDevice);
    }
    static native int sendConnectedDatagramImpl(FileDescriptor fd,
            byte[] data, int offset, int length, boolean bindToDevice)
            throws IOException;
    public int sendConnectedDatagramDirect(FileDescriptor fd,
            int address, int offset, int length, boolean bindToDevice)
            throws IOException {
        return sendConnectedDatagramDirectImpl(fd, address, offset, length, bindToDevice);
    }
    static native int sendConnectedDatagramDirectImpl(FileDescriptor fd,
            int address, int offset, int length, boolean bindToDevice)
            throws IOException;
    public int sendDatagram(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException {
        return sendDatagramImpl(fd, data, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }
    static native int sendDatagramImpl(FileDescriptor fd, byte[] data, int offset,
            int length, int port, boolean bindToDevice, int trafficClass,
            InetAddress inetAddress) throws IOException;
    public int sendDatagram2(FileDescriptor fd, byte[] data, int offset,
            int length, int port, InetAddress inetAddress) throws IOException {
        return sendDatagramImpl2(fd, data, offset, length, port, inetAddress);
    }
    static native int sendDatagramImpl2(FileDescriptor fd, byte[] data,
            int offset, int length, int port, InetAddress inetAddress) throws IOException;
    public int sendDatagramDirect(FileDescriptor fd, int address,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException {
        return sendDatagramDirectImpl(fd, address, offset, length, port, bindToDevice,
                trafficClass, inetAddress);
    }
    static native int sendDatagramDirectImpl(FileDescriptor fd, int address,
            int offset, int length, int port, boolean bindToDevice,
            int trafficClass, InetAddress inetAddress) throws IOException;
    public void sendUrgentData(FileDescriptor fd, byte value) {
        sendUrgentDataImpl(fd, value);
    }
    static native void sendUrgentDataImpl(FileDescriptor fd, byte value);
    public void setInetAddress(InetAddress sender, byte[] address) {
        setInetAddressImpl(sender, address);
    }
    native void setInetAddressImpl(InetAddress sender, byte[] address);
    public void setNonBlocking(FileDescriptor fd, boolean block)
            throws IOException {
        setNonBlockingImpl(fd, block);
    }
    static native void setNonBlockingImpl(FileDescriptor aFD, boolean block);
    public void setSocketOption(FileDescriptor aFD, int opt,
            Object optVal) throws SocketException {
        setSocketOptionImpl(aFD, opt, optVal);
    }
    static native void setSocketOptionImpl(FileDescriptor aFD, int opt,
            Object optVal) throws SocketException;
    public void shutdownInput(FileDescriptor descriptor) throws IOException {
        shutdownInputImpl(descriptor);
    }
    private native void shutdownInputImpl(FileDescriptor descriptor)
            throws IOException;
    public void shutdownOutput(FileDescriptor fd) throws IOException {
        shutdownOutputImpl(fd);
    }
    private native void shutdownOutputImpl(FileDescriptor descriptor)
            throws IOException;
    public void socketClose(FileDescriptor fd) throws IOException {
        socketCloseImpl(fd);
    }
    static native void socketCloseImpl(FileDescriptor fD);
    public boolean supportsUrgentData(FileDescriptor fd) {
        return supportsUrgentDataImpl(fd);
    }
    static native boolean supportsUrgentDataImpl(FileDescriptor fd);
    private boolean validateFDs(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs) {
        for (FileDescriptor fd : readFDs) {
            if (!fd.valid()) {
                return false;
            }
        }
        for (FileDescriptor fd : writeFDs) {
            if (!fd.valid()) {
                return false;
            }
        }
        return true;
    }
    private boolean validateFDs(FileDescriptor[] readFDs,
            FileDescriptor[] writeFDs, int countRead, int countWrite) {
        for (int i = 0; i < countRead; ++i) {
            if (!readFDs[i].valid()) {
                return false;
            }
        }
        for (int i = 0; i < countWrite; ++i) {
            if (!writeFDs[i].valid()) {
                return false;
            }
        }
        return true;
    }
    public int write(FileDescriptor fd, byte[] data, int offset, int count)
            throws IOException {
        return writeSocketImpl(fd, data, offset, count);
    }
    static native int writeSocketImpl(FileDescriptor fd, byte[] data, int offset,
            int count) throws IOException;
    public int writeDirect(FileDescriptor fd, int address, int offset, int count)
            throws IOException {
        return writeSocketDirectImpl(fd, address, offset, count);
    }
    static native int writeSocketDirectImpl(FileDescriptor fd, int address, int offset, int count)
            throws IOException;
}
