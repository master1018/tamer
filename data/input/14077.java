public class ServerSocketAdaptor                        
    extends ServerSocket
{
    private final ServerSocketChannelImpl ssc;
    private volatile int timeout = 0;
    public static ServerSocket create(ServerSocketChannelImpl ssc) {
        try {
            return new ServerSocketAdaptor(ssc);
        } catch (IOException x) {
            throw new Error(x);
        }
    }
    private ServerSocketAdaptor(ServerSocketChannelImpl ssc)
        throws IOException
    {
        this.ssc = ssc;
    }
    public void bind(SocketAddress local) throws IOException {
        bind(local, 50);
    }
    public void bind(SocketAddress local, int backlog) throws IOException {
        if (local == null)
            local = new InetSocketAddress(0);
        try {
            ssc.bind(local, backlog);
        } catch (Exception x) {
            Net.translateException(x);
        }
    }
    public InetAddress getInetAddress() {
        if (!ssc.isBound())
            return null;
        return Net.asInetSocketAddress(ssc.localAddress()).getAddress();
    }
    public int getLocalPort() {
        if (!ssc.isBound())
            return -1;
        return Net.asInetSocketAddress(ssc.localAddress()).getPort();
    }
    public Socket accept() throws IOException {
        synchronized (ssc.blockingLock()) {
            if (!ssc.isBound())
                throw new IllegalBlockingModeException();
            try {
                if (timeout == 0) {
                    SocketChannel sc = ssc.accept();
                    if (sc == null && !ssc.isBlocking())
                        throw new IllegalBlockingModeException();
                    return sc.socket();
                }
                SelectionKey sk = null;
                Selector sel = null;
                ssc.configureBlocking(false);
                try {
                    SocketChannel sc;
                    if ((sc = ssc.accept()) != null)
                        return sc.socket();
                    sel = Util.getTemporarySelector(ssc);
                    sk = ssc.register(sel, SelectionKey.OP_ACCEPT);
                    long to = timeout;
                    for (;;) {
                        if (!ssc.isOpen())
                            throw new ClosedChannelException();
                        long st = System.currentTimeMillis();
                        int ns = sel.select(to);
                        if (ns > 0 &&
                            sk.isAcceptable() && ((sc = ssc.accept()) != null))
                            return sc.socket();
                        sel.selectedKeys().remove(sk);
                        to -= System.currentTimeMillis() - st;
                        if (to <= 0)
                            throw new SocketTimeoutException();
                    }
                } finally {
                    if (sk != null)
                        sk.cancel();
                    if (ssc.isOpen())
                        ssc.configureBlocking(true);
                    if (sel != null)
                        Util.releaseTemporarySelector(sel);
                }
            } catch (Exception x) {
                Net.translateException(x);
                assert false;
                return null;            
            }
        }
    }
    public void close() throws IOException {
        ssc.close();
    }
    public ServerSocketChannel getChannel() {
        return ssc;
    }
    public boolean isBound() {
        return ssc.isBound();
    }
    public boolean isClosed() {
        return !ssc.isOpen();
    }
    public void setSoTimeout(int timeout) throws SocketException {
        this.timeout = timeout;
    }
    public int getSoTimeout() throws SocketException {
        return timeout;
    }
    public void setReuseAddress(boolean on) throws SocketException {
        try {
            ssc.setOption(StandardSocketOptions.SO_REUSEADDR, on);
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }
    public boolean getReuseAddress() throws SocketException {
        try {
            return ssc.getOption(StandardSocketOptions.SO_REUSEADDR).booleanValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return false;       
        }
    }
    public String toString() {
        if (!isBound())
            return "ServerSocket[unbound]";
        return "ServerSocket[addr=" + getInetAddress() +
                ",localport=" + getLocalPort()  + "]";
    }
    public void setReceiveBufferSize(int size) throws SocketException {
        if (size <= 0)
            throw new IllegalArgumentException("size cannot be 0 or negative");
        try {
            ssc.setOption(StandardSocketOptions.SO_RCVBUF, size);
        } catch (IOException x) {
            Net.translateToSocketException(x);
        }
    }
    public int getReceiveBufferSize() throws SocketException {
        try {
            return ssc.getOption(StandardSocketOptions.SO_RCVBUF).intValue();
        } catch (IOException x) {
            Net.translateToSocketException(x);
            return -1;          
        }
    }
}
