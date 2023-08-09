public class TCPChannel implements Channel {
    private final TCPEndpoint ep;
    private final TCPTransport tr;
    private final List<TCPConnection> freeList =
        new ArrayList<TCPConnection>();
    private Future<?> reaper = null;
    private boolean usingMultiplexer = false;
    private ConnectionMultiplexer multiplexer = null;
    private ConnectionAcceptor acceptor;
    private AccessControlContext okContext;
    private WeakHashMap<AccessControlContext,
                        Reference<AccessControlContext>> authcache;
    private SecurityManager cacheSecurityManager = null;
    private static final long idleTimeout =             
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.transport.connectionTimeout", 15000));
    private static final int handshakeTimeout =         
        AccessController.doPrivileged(
            new GetIntegerAction("sun.rmi.transport.tcp.handshakeTimeout",
                                 60000));
    private static final int responseTimeout =          
        AccessController.doPrivileged(
            new GetIntegerAction("sun.rmi.transport.tcp.responseTimeout", 0));
    private static final ScheduledExecutorService scheduler =
        AccessController.doPrivileged(
            new RuntimeUtil.GetInstanceAction()).getScheduler();
    TCPChannel(TCPTransport tr, TCPEndpoint ep) {
        this.tr = tr;
        this.ep = ep;
    }
    public Endpoint getEndpoint() {
        return ep;
    }
    private void checkConnectPermission() throws SecurityException {
        SecurityManager security = System.getSecurityManager();
        if (security == null)
            return;
        if (security != cacheSecurityManager) {
            okContext = null;
            authcache = new WeakHashMap<AccessControlContext,
                                        Reference<AccessControlContext>>();
            cacheSecurityManager = security;
        }
        AccessControlContext ctx = AccessController.getContext();
        if (okContext == null ||
            !(okContext.equals(ctx) || authcache.containsKey(ctx)))
        {
            security.checkConnect(ep.getHost(), ep.getPort());
            authcache.put(ctx, new SoftReference<AccessControlContext>(ctx));
        }
        okContext = ctx;
    }
    public Connection newConnection() throws RemoteException {
        TCPConnection conn;
        do {
            conn = null;
            synchronized (freeList) {
                int elementPos = freeList.size()-1;
                if (elementPos >= 0) {
                    checkConnectPermission();
                    conn = freeList.get(elementPos);
                    freeList.remove(elementPos);
                }
            }
            if (conn != null) {
                if (!conn.isDead()) {
                    TCPTransport.tcpLog.log(Log.BRIEF, "reuse connection");
                    return conn;
                }
                this.free(conn, false);
            }
        } while (conn != null);
        return (createConnection());
    }
    private Connection createConnection() throws RemoteException {
        Connection conn;
        TCPTransport.tcpLog.log(Log.BRIEF, "create connection");
        if (!usingMultiplexer) {
            Socket sock = ep.newSocket();
            conn = new TCPConnection(this, sock);
            try {
                DataOutputStream out =
                    new DataOutputStream(conn.getOutputStream());
                writeTransportHeader(out);
                if (!conn.isReusable()) {
                    out.writeByte(TransportConstants.SingleOpProtocol);
                } else {
                    out.writeByte(TransportConstants.StreamProtocol);
                    out.flush();
                    int originalSoTimeout = 0;
                    try {
                        originalSoTimeout = sock.getSoTimeout();
                        sock.setSoTimeout(handshakeTimeout);
                    } catch (Exception e) {
                    }
                    DataInputStream in =
                        new DataInputStream(conn.getInputStream());
                    byte ack = in.readByte();
                    if (ack != TransportConstants.ProtocolAck) {
                        throw new ConnectIOException(
                            ack == TransportConstants.ProtocolNack ?
                            "JRMP StreamProtocol not supported by server" :
                            "non-JRMP server at remote endpoint");
                    }
                    String suggestedHost = in.readUTF();
                    int    suggestedPort = in.readInt();
                    if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                        TCPTransport.tcpLog.log(Log.VERBOSE,
                            "server suggested " + suggestedHost + ":" +
                            suggestedPort);
                    }
                    TCPEndpoint.setLocalHost(suggestedHost);
                    TCPEndpoint localEp =
                        TCPEndpoint.getLocalEndpoint(0, null, null);
                    out.writeUTF(localEp.getHost());
                    out.writeInt(localEp.getPort());
                    if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
                        TCPTransport.tcpLog.log(Log.VERBOSE, "using " +
                            localEp.getHost() + ":" + localEp.getPort());
                    }
                    try {
                        sock.setSoTimeout((originalSoTimeout != 0 ?
                                           originalSoTimeout :
                                           responseTimeout));
                    } catch (Exception e) {
                    }
                    out.flush();
                }
            } catch (IOException e) {
                if (e instanceof RemoteException)
                    throw (RemoteException) e;
                else
                    throw new ConnectIOException(
                        "error during JRMP connection establishment", e);
            }
        } else {
            try {
                conn = multiplexer.openConnection();
            } catch (IOException e) {
                synchronized (this) {
                    usingMultiplexer = false;
                    multiplexer = null;
                }
                throw new ConnectIOException(
                    "error opening virtual connection " +
                    "over multiplexed connection", e);
            }
        }
        return conn;
    }
    public void free(Connection conn, boolean reuse) {
        if (conn == null) return;
        if (reuse && conn.isReusable()) {
            long lastuse = System.currentTimeMillis();
            TCPConnection tcpConnection = (TCPConnection) conn;
            TCPTransport.tcpLog.log(Log.BRIEF, "reuse connection");
            synchronized (freeList) {
                freeList.add(tcpConnection);
                if (reaper == null) {
                    TCPTransport.tcpLog.log(Log.BRIEF, "create reaper");
                    reaper = scheduler.scheduleWithFixedDelay(
                        new Runnable() {
                            public void run() {
                                TCPTransport.tcpLog.log(Log.VERBOSE,
                                                        "wake up");
                                freeCachedConnections();
                            }
                        }, idleTimeout, idleTimeout, TimeUnit.MILLISECONDS);
                }
            }
            tcpConnection.setLastUseTime(lastuse);
            tcpConnection.setExpiration(lastuse + idleTimeout);
        } else {
            TCPTransport.tcpLog.log(Log.BRIEF, "close connection");
            try {
                conn.close();
            } catch (IOException ignored) {
            }
        }
    }
    private void writeTransportHeader(DataOutputStream out)
        throws RemoteException
    {
        try {
            DataOutputStream dataOut =
                new DataOutputStream(out);
            dataOut.writeInt(TransportConstants.Magic);
            dataOut.writeShort(TransportConstants.Version);
        } catch (IOException e) {
            throw new ConnectIOException(
                "error writing JRMP transport header", e);
        }
    }
    synchronized void useMultiplexer(ConnectionMultiplexer newMultiplexer) {
        multiplexer = newMultiplexer;
        usingMultiplexer = true;
    }
    void acceptMultiplexConnection(Connection conn) {
        if (acceptor == null) {
            acceptor = new ConnectionAcceptor(tr);
            acceptor.startNewAcceptor();
        }
        acceptor.accept(conn);
    }
    public void shedCache() {
        Connection[] conn;
        synchronized (freeList) {
            conn = freeList.toArray(new Connection[freeList.size()]);
            freeList.clear();
        }
        for (int i = conn.length; --i >= 0; ) {
            Connection c = conn[i];
            conn[i] = null; 
            try {
                c.close();
            } catch (java.io.IOException e) {
            }
        }
    }
    private void freeCachedConnections() {
        synchronized (freeList) {
            int size = freeList.size();
            if (size > 0) {
                long time = System.currentTimeMillis();
                ListIterator<TCPConnection> iter = freeList.listIterator(size);
                while (iter.hasPrevious()) {
                    TCPConnection conn = iter.previous();
                    if (conn.expired(time)) {
                        TCPTransport.tcpLog.log(Log.VERBOSE,
                            "connection timeout expired");
                        try {
                            conn.close();
                        } catch (java.io.IOException e) {
                        }
                        iter.remove();
                    }
                }
            }
            if (freeList.isEmpty()) {
                reaper.cancel(false);
                reaper = null;
            }
        }
    }
}
class ConnectionAcceptor implements Runnable {
    private TCPTransport transport;
    private List<Connection> queue = new ArrayList<Connection>();
    private static int threadNum = 0;
    public ConnectionAcceptor(TCPTransport transport) {
        this.transport = transport;
    }
    public void startNewAcceptor() {
        Thread t = AccessController.doPrivileged(
            new NewThreadAction(ConnectionAcceptor.this,
                                "Multiplex Accept-" + ++ threadNum,
                                true));
        t.start();
    }
    public void accept(Connection conn) {
        synchronized (queue) {
            queue.add(conn);
            queue.notify();
        }
    }
    public void run() {
        Connection conn;
        synchronized (queue) {
            while (queue.size() == 0) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                }
            }
            startNewAcceptor();
            conn = queue.remove(0);
        }
        transport.handleMessages(conn, true);
    }
}
