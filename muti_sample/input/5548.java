public class SocketAdaptor
    extends Socket
{
    private final SocketChannelImpl sc;
    private volatile int timeout = 0;
    private SocketAdaptor(SocketChannelImpl sc) {
        this.sc = sc;
    }
    public static Socket create(SocketChannelImpl sc) {
        return new SocketAdaptor(sc);
    }
    public SocketChannel getChannel() {
        return sc;
    }
    public void connect(SocketAddress remote) throws IOException {
        connect(remote, 0);
    }
    public void connect(SocketAddress remote, int timeout) throws IOException {
        if (remote == null)
            throw new IllegalArgumentException("connect: The address can't be null");
        if (timeout < 0)
            throw new IllegalArgumentException("connect: timeout can't be negative");
        synchronized (sc.blockingLock()) {
            if (!sc.isBlocking())
                throw new IllegalBlockingModeException();
            try {
                if (timeout == 0) {
                    sc.connect(remote);
                    return;
                }
                SelectionKey sk = null;
                Selector sel = null;
                sc.configureBlocking(false);
                try {
                    if (sc.connect(remote))
                        return;
                    sel = Util.getTemporarySelector(sc);
                    sk = sc.register(sel, SelectionKey.OP_CONNECT);
                    long to = timeout;
                    for (;;) {
                        if (!sc.isOpen())
                            throw new ClosedChannelException();
                        long st = System.currentTimeMillis();
                        int ns = sel.select(to);
                        if (ns > 0 &&
                            sk.isConnectable() && sc.finishConnect())
                            break;
                        sel.selectedKeys().remove(sk);
                        to -= System.currentTimeMillis() - st;
                        if (to <= 0) {
                            try {
                                sc.close();
                            } catch (IOException x) { }
                            throw new SocketTimeoutException();
                        }
                    }
                } finally {
                    if (sk != null)
                        sk.cancel();
                    if (sc.isOpen())
                        sc.configureBlocking(true);
                    if (sel != null)
                        Util.releaseTemporarySelector(sel);
                }
            } catch (Exception x) {
                Net.translateException(x, true);
            }
        }
    }
    public void bind(SocketAddress local) throws IOException {
        try {
            sc.bind(local);
        } catch (Exception x) {
            Net.translateException(x);
        }
    }
    public InetAddress getInetAddress() {
        SocketAddress remote = sc.remoteAddress();
        if (remote == null) {
            return null;
        } else {
            return ((InetSocketAddress)remote).getAddress();
        }
    }
    public InetAddress getLocalAddress() {
        if (sc.isOpen()) {
            SocketAddress local = sc.localAddress();
            if (local != null)
                return ((InetSocketAddress)local).getAddress();
        }
        return new InetSocketAddress(0).getAddress();
    }
    public int getPort() {
        SocketAddress remote = sc.remoteAddress();
        if (remote == null) {
            return 0;
        } else {
            return ((InetSocketAddress)remote).getPort();
        }
    }
    public int getLocalPort() {
        SocketAddress local = sc.localAddress();
        if (local == null) {
            return -1;
        } else {
            return ((InetSocketAddress)local).getPort();
        }
    }
    private class SocketInputStream
        extends ChannelInputStream
    {
        private SocketInputStream() {
            super(sc);
        }
        protected int read(ByteBuffer bb)
            throws IOException
        {
            synchronized (sc.blockingLock()) {
                if (!sc.isBlocking())
                    throw new IllegalBlockingModeException();
                if (timeout == 0)
                    return sc.read(bb);
                SelectionKey sk = null;
                Selector sel = null;
                sc.configureBlocking(false);
                try {
                    int n;
                    if ((n = sc.read(bb)) != 0)
                        return n;
                    sel = Util.getTemporarySelector(sc);
                    sk = sc.register(sel, SelectionKey.OP_READ);
                    long to = timeout;
                    for (;;) {
                        if (!sc.isOpen())
                            throw new ClosedChannelException();
                        long st = System.currentTimeMillis();
                        int ns = sel.select(to);
                        if (ns > 0 && sk.isReadable()) {
                            if ((n = sc.read(bb)) != 0)
                                return n;
                        }
                        sel.selectedKeys().remove(sk);
                        to -= System.currentTimeMillis() - st;
                        if (to <= 0)
                            throw new SocketTimeoutException();
                    }
                } finally {
                    if (sk != null)
                        sk.cancel();
                    if (sc.isOpen())
                        sc.configureBlocking(true);
                    if (sel != null)
                        Util.releaseTemporarySelector(sel);
                }
            }
        }
    }
    private InputStream socketInputStream = null;
    public InputStream getInputStream() throws IOException {
        if (!sc.isOpen())
            throw new SocketException("Socket is closed");
        if (!sc.isConnected())
            throw new SocketException("Socket is not connected");
        if (!sc.isInputOpen())
            throw new SocketException("Socket input is shutdown");
        if (socketInputStream == null) {
            try {
                socketInputStream = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<InputStream>() {
                        public InputStream run() throws IOException {
                            return new SocketInputStream();
                        }
                    });
            } catch (java.security.PrivilegedActionException e) {
                throw (IOException)e.getException();
            }
        }
        return socketInputStream;
    }
    public OutputStream getOutputStream() throws IOException {
        if (!sc.isOpen())
            throw new SocketException("Socket is closed");
        if (!sc.isConnected())
            throw new SocketException("Socket is not connected");
        if (!sc.isOutputOpen())
            throw new SocketException("Socket output is shutdown");
        OutputStream os = null;
        try {
            os = AccessController.doPrivileged(
                new PrivilegedExceptionAction<OutputStream>() {
                    public OutputStream run() throws IOException {
                        return Channels.newOutputStream(sc);
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            throw (IOException)e.getException();
        }
        return os;
    }
    private void setBooleanOption(SocketOption<Boolean> name, boolean value)
        throws SocketException
    {
        try {
            sc.setOption(name, value);
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }
    private void setIntOption(SocketOption<Integer> name, int value)
        throws SocketException
    {
        try {
            sc.setOption(name, value);
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }
    private boolean getBooleanOption(SocketOption<Boolean> name) throws SocketException {
        try {
            return sc.getOption(name).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;       
        }
    }
    private int getIntOption(SocketOption<Integer> name) throws SocketException {
        try {
            return sc.getOption(name).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;          
        }
    }
    public void setTcpNoDelay(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.TCP_NODELAY, on);
    }
    public boolean getTcpNoDelay() throws SocketException {
        return getBooleanOption(StandardSocketOptions.TCP_NODELAY);
    }
    public void setSoLinger(boolean on, int linger) throws SocketException {
        if (!on)
            linger = -1;
        setIntOption(StandardSocketOptions.SO_LINGER, linger);
    }
    public int getSoLinger() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_LINGER);
    }
    public void sendUrgentData(int data) throws IOException {
        synchronized (sc.blockingLock()) {
            if (!sc.isBlocking())
                throw new IllegalBlockingModeException();
            int n = sc.sendOutOfBandData((byte)data);
            assert n == 1;
        }
    }
    public void setOOBInline(boolean on) throws SocketException {
        setBooleanOption(ExtendedSocketOption.SO_OOBINLINE, on);
    }
    public boolean getOOBInline() throws SocketException {
        return getBooleanOption(ExtendedSocketOption.SO_OOBINLINE);
    }
    public void setSoTimeout(int timeout) throws SocketException {
        if (timeout < 0)
            throw new IllegalArgumentException("timeout can't be negative");
        this.timeout = timeout;
    }
    public int getSoTimeout() throws SocketException {
        return timeout;
    }
    public void setSendBufferSize(int size) throws SocketException {
        if (size <= 0)
            throw new IllegalArgumentException("Invalid send size");
        setIntOption(StandardSocketOptions.SO_SNDBUF, size);
    }
    public int getSendBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_SNDBUF);
    }
    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0)
            throw new IllegalArgumentException("Invalid receive size");
        setIntOption(StandardSocketOptions.SO_RCVBUF, size);
    }
    public int getReceiveBufferSize() throws SocketException {
        return getIntOption(StandardSocketOptions.SO_RCVBUF);
    }
    public void setKeepAlive(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_KEEPALIVE, on);
    }
    public boolean getKeepAlive() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_KEEPALIVE);
    }
    public void setTrafficClass(int tc) throws SocketException {
        setIntOption(StandardSocketOptions.IP_TOS, tc);
    }
    public int getTrafficClass() throws SocketException {
        return getIntOption(StandardSocketOptions.IP_TOS);
    }
    public void setReuseAddress(boolean on) throws SocketException {
        setBooleanOption(StandardSocketOptions.SO_REUSEADDR, on);
    }
    public boolean getReuseAddress() throws SocketException {
        return getBooleanOption(StandardSocketOptions.SO_REUSEADDR);
    }
    public void close() throws IOException {
        sc.close();
    }
    public void shutdownInput() throws IOException {
        try {
            sc.shutdownInput();
        } catch (Exception x) {
            Net.translateException(x);
        }
    }
    public void shutdownOutput() throws IOException {
        try {
            sc.shutdownOutput();
        } catch (Exception x) {
            Net.translateException(x);
        }
    }
    public String toString() {
        if (sc.isConnected())
            return "Socket[addr=" + getInetAddress() +
                ",port=" + getPort() +
                ",localport=" + getLocalPort() + "]";
        return "Socket[unconnected]";
    }
    public boolean isConnected() {
        return sc.isConnected();
    }
    public boolean isBound() {
        return sc.localAddress() != null;
    }
    public boolean isClosed() {
        return !sc.isOpen();
    }
    public boolean isInputShutdown() {
        return !sc.isInputOpen();
    }
    public boolean isOutputShutdown() {
        return !sc.isOutputOpen();
    }
}
