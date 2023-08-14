public class TCPTransport extends Transport {
    static final Log tcpLog = Log.getLog("sun.rmi.transport.tcp", "tcp",
        LogStream.parseLevel(AccessController.doPrivileged(
            new GetPropertyAction("sun.rmi.transport.tcp.logLevel"))));
    private static final int maxConnectionThreads =     
        AccessController.doPrivileged(
            new GetIntegerAction("sun.rmi.transport.tcp.maxConnectionThreads",
                                 Integer.MAX_VALUE));
    private static final long threadKeepAliveTime =     
        AccessController.doPrivileged(
            new GetLongAction("sun.rmi.transport.tcp.threadKeepAliveTime",
                              60000));
    private static final ExecutorService connectionThreadPool =
        new ThreadPoolExecutor(0, maxConnectionThreads,
            threadKeepAliveTime, TimeUnit.MILLISECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                public Thread newThread(Runnable runnable) {
                    return AccessController.doPrivileged(new NewThreadAction(
                        runnable, "TCP Connection(idle)", true, true));
                }
            });
    private static final AtomicInteger connectionCount = new AtomicInteger(0);
    private static final ThreadLocal<ConnectionHandler>
        threadConnectionHandler = new ThreadLocal<ConnectionHandler>();
    private final LinkedList<TCPEndpoint> epList;
    private int exportCount = 0;
    private ServerSocket server = null;
    private final Map<TCPEndpoint,Reference<TCPChannel>> channelTable =
        new WeakHashMap<TCPEndpoint,Reference<TCPChannel>>();
    static final RMISocketFactory defaultSocketFactory =
        RMISocketFactory.getDefaultSocketFactory();
    private static final int connectionReadTimeout =    
        AccessController.doPrivileged(
            new GetIntegerAction("sun.rmi.transport.tcp.readTimeout",
                                 2 * 3600 * 1000));
    TCPTransport(LinkedList<TCPEndpoint> epList)  {
        this.epList = epList;
        if (tcpLog.isLoggable(Log.BRIEF)) {
            tcpLog.log(Log.BRIEF, "Version = " +
                TransportConstants.Version + ", ep = " + getEndpoint());
        }
    }
    public void shedConnectionCaches() {
        List<TCPChannel> channels;
        synchronized (channelTable) {
            channels = new ArrayList<TCPChannel>(channelTable.values().size());
            for (Reference<TCPChannel> ref : channelTable.values()) {
                TCPChannel ch = ref.get();
                if (ch != null) {
                    channels.add(ch);
                }
            }
        }
        for (TCPChannel channel : channels) {
            channel.shedCache();
        }
    }
    public TCPChannel getChannel(Endpoint ep) {
        TCPChannel ch = null;
        if (ep instanceof TCPEndpoint) {
            synchronized (channelTable) {
                Reference<TCPChannel> ref = channelTable.get(ep);
                if (ref != null) {
                    ch = ref.get();
                }
                if (ch == null) {
                    TCPEndpoint tcpEndpoint = (TCPEndpoint) ep;
                    ch = new TCPChannel(this, tcpEndpoint);
                    channelTable.put(tcpEndpoint,
                                     new WeakReference<TCPChannel>(ch));
                }
            }
        }
        return ch;
    }
    public void free(Endpoint ep) {
        if (ep instanceof TCPEndpoint) {
            synchronized (channelTable) {
                Reference<TCPChannel> ref = channelTable.remove(ep);
                if (ref != null) {
                    TCPChannel channel = ref.get();
                    if (channel != null) {
                        channel.shedCache();
                    }
                }
            }
        }
    }
    public void exportObject(Target target) throws RemoteException {
        synchronized (this) {
            listen();
            exportCount++;
        }
        boolean ok = false;
        try {
            super.exportObject(target);
            ok = true;
        } finally {
            if (!ok) {
                synchronized (this) {
                    decrementExportCount();
                }
            }
        }
    }
    protected synchronized void targetUnexported() {
        decrementExportCount();
    }
    private void decrementExportCount() {
        assert Thread.holdsLock(this);
        exportCount--;
        if (exportCount == 0 && getEndpoint().getListenPort() != 0) {
            ServerSocket ss = server;
            server = null;
            try {
                ss.close();
            } catch (IOException e) {
            }
        }
    }
    protected void checkAcceptPermission(AccessControlContext acc) {
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        ConnectionHandler h = threadConnectionHandler.get();
        if (h == null) {
            throw new Error(
                "checkAcceptPermission not in ConnectionHandler thread");
        }
        h.checkAcceptPermission(sm, acc);
    }
    private TCPEndpoint getEndpoint() {
        synchronized (epList) {
            return epList.getLast();
        }
    }
    private void listen() throws RemoteException {
        assert Thread.holdsLock(this);
        TCPEndpoint ep = getEndpoint();
        int port = ep.getPort();
        if (server == null) {
            if (tcpLog.isLoggable(Log.BRIEF)) {
                tcpLog.log(Log.BRIEF,
                    "(port " + port + ") create server socket");
            }
            try {
                server = ep.newServerSocket();
                Thread t = AccessController.doPrivileged(
                    new NewThreadAction(new AcceptLoop(server),
                                        "TCP Accept-" + port, true));
                t.start();
            } catch (java.net.BindException e) {
                throw new ExportException("Port already in use: " + port, e);
            } catch (IOException e) {
                throw new ExportException("Listen failed on port: " + port, e);
            }
        } else {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkListen(port);
            }
        }
    }
    private class AcceptLoop implements Runnable {
        private final ServerSocket serverSocket;
        private long lastExceptionTime = 0L;
        private int recentExceptionCount;
        AcceptLoop(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }
        public void run() {
            try {
                executeAcceptLoop();
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                }
            }
        }
        private void executeAcceptLoop() {
            if (tcpLog.isLoggable(Log.BRIEF)) {
                tcpLog.log(Log.BRIEF, "listening on port " +
                           getEndpoint().getPort());
            }
            while (true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    InetAddress clientAddr = socket.getInetAddress();
                    String clientHost = (clientAddr != null
                                         ? clientAddr.getHostAddress()
                                         : "0.0.0.0");
                    try {
                        connectionThreadPool.execute(
                            new ConnectionHandler(socket, clientHost));
                    } catch (RejectedExecutionException e) {
                        closeSocket(socket);
                        tcpLog.log(Log.BRIEF,
                                   "rejected connection from " + clientHost);
                    }
                } catch (Throwable t) {
                    try {
                        if (serverSocket.isClosed()) {
                            break;
                        }
                        try {
                            if (tcpLog.isLoggable(Level.WARNING)) {
                                tcpLog.log(Level.WARNING,
                                           "accept loop for " + serverSocket +
                                           " throws", t);
                            }
                        } catch (Throwable tt) {
                        }
                    } finally {
                        if (socket != null) {
                            closeSocket(socket);
                        }
                    }
                    if (!(t instanceof SecurityException)) {
                        try {
                            TCPEndpoint.shedConnectionCaches();
                        } catch (Throwable tt) {
                        }
                    }
                    if (t instanceof Exception ||
                        t instanceof OutOfMemoryError ||
                        t instanceof NoClassDefFoundError)
                    {
                        if (!continueAfterAcceptFailure(t)) {
                            return;
                        }
                    } else {
                        throw (Error) t;
                    }
                }
            }
        }
        private boolean continueAfterAcceptFailure(Throwable t) {
            RMIFailureHandler fh = RMISocketFactory.getFailureHandler();
            if (fh != null) {
                return fh.failure(t instanceof Exception ? (Exception) t :
                                  new InvocationTargetException(t));
            } else {
                throttleLoopOnException();
                return true;
            }
        }
        private void throttleLoopOnException() {
            long now = System.currentTimeMillis();
            if (lastExceptionTime == 0L || (now - lastExceptionTime) > 5000) {
                lastExceptionTime = now;
                recentExceptionCount = 0;
            } else {
                if (++recentExceptionCount >= 10) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }
        }
    }
    private static void closeSocket(Socket sock) {
        try {
            sock.close();
        } catch (IOException ex) {
        }
    }
    void handleMessages(Connection conn, boolean persistent) {
        int port = getEndpoint().getPort();
        try {
            DataInputStream in = new DataInputStream(conn.getInputStream());
            do {
                int op = in.read();     
                if (op == -1) {
                    if (tcpLog.isLoggable(Log.BRIEF)) {
                        tcpLog.log(Log.BRIEF, "(port " +
                            port + ") connection closed");
                    }
                    break;
                }
                if (tcpLog.isLoggable(Log.BRIEF)) {
                    tcpLog.log(Log.BRIEF, "(port " + port +
                        ") op = " + op);
                }
                switch (op) {
                case TransportConstants.Call:
                    RemoteCall call = new StreamRemoteCall(conn);
                    if (serviceCall(call) == false)
                        return;
                    break;
                case TransportConstants.Ping:
                    DataOutputStream out =
                        new DataOutputStream(conn.getOutputStream());
                    out.writeByte(TransportConstants.PingAck);
                    conn.releaseOutputStream();
                    break;
                case TransportConstants.DGCAck:
                    DGCAckHandler.received(UID.read(in));
                    break;
                default:
                    throw new IOException("unknown transport op " + op);
                }
            } while (persistent);
        } catch (IOException e) {
            if (tcpLog.isLoggable(Log.BRIEF)) {
                tcpLog.log(Log.BRIEF, "(port " + port +
                    ") exception: ", e);
            }
        } finally {
            try {
                conn.close();
            } catch (IOException ex) {
            }
        }
    }
    public static String getClientHost() throws ServerNotActiveException {
        ConnectionHandler h = threadConnectionHandler.get();
        if (h != null) {
            return h.getClientHost();
        } else {
            throw new ServerNotActiveException("not in a remote call");
        }
    }
    private class ConnectionHandler implements Runnable {
        private static final int POST = 0x504f5354;
        private AccessControlContext okContext;
        private Map<AccessControlContext,
                    Reference<AccessControlContext>> authCache;
        private SecurityManager cacheSecurityManager = null;
        private Socket socket;
        private String remoteHost;
        ConnectionHandler(Socket socket, String remoteHost) {
            this.socket = socket;
            this.remoteHost = remoteHost;
        }
        String getClientHost() {
            return remoteHost;
        }
        void checkAcceptPermission(SecurityManager sm,
                                   AccessControlContext acc)
        {
            if (sm != cacheSecurityManager) {
                okContext = null;
                authCache = new WeakHashMap<AccessControlContext,
                                            Reference<AccessControlContext>>();
                cacheSecurityManager = sm;
            }
            if (acc.equals(okContext) || authCache.containsKey(acc)) {
                return;
            }
            InetAddress addr = socket.getInetAddress();
            String host = (addr != null) ? addr.getHostAddress() : "*";
            sm.checkAccept(host, socket.getPort());
            authCache.put(acc, new SoftReference<AccessControlContext>(acc));
            okContext = acc;
        }
        public void run() {
            Thread t = Thread.currentThread();
            String name = t.getName();
            try {
                t.setName("RMI TCP Connection(" +
                          connectionCount.incrementAndGet() +
                          ")-" + remoteHost);
                run0();
            } finally {
                t.setName(name);
            }
        }
        private void run0() {
            TCPEndpoint endpoint = getEndpoint();
            int port = endpoint.getPort();
            threadConnectionHandler.set(this);
            try {
                socket.setTcpNoDelay(true);
            } catch (Exception e) {
            }
            try {
                if (connectionReadTimeout > 0)
                    socket.setSoTimeout(connectionReadTimeout);
            } catch (Exception e) {
            }
            try {
                InputStream sockIn = socket.getInputStream();
                InputStream bufIn = sockIn.markSupported()
                        ? sockIn
                        : new BufferedInputStream(sockIn);
                bufIn.mark(4);
                DataInputStream in = new DataInputStream(bufIn);
                int magic = in.readInt();
                if (magic == POST) {
                    tcpLog.log(Log.BRIEF, "decoding HTTP-wrapped call");
                    bufIn.reset();      
                    try {
                        socket = new HttpReceiveSocket(socket, bufIn, null);
                        remoteHost = "0.0.0.0";
                        sockIn = socket.getInputStream();
                        bufIn = new BufferedInputStream(sockIn);
                        in = new DataInputStream(bufIn);
                        magic = in.readInt();
                    } catch (IOException e) {
                        throw new RemoteException("Error HTTP-unwrapping call",
                                                  e);
                    }
                }
                short version = in.readShort();
                if (magic != TransportConstants.Magic ||
                    version != TransportConstants.Version) {
                    closeSocket(socket);
                    return;
                }
                OutputStream sockOut = socket.getOutputStream();
                BufferedOutputStream bufOut =
                    new BufferedOutputStream(sockOut);
                DataOutputStream out = new DataOutputStream(bufOut);
                int remotePort = socket.getPort();
                if (tcpLog.isLoggable(Log.BRIEF)) {
                    tcpLog.log(Log.BRIEF, "accepted socket from [" +
                                     remoteHost + ":" + remotePort + "]");
                }
                TCPEndpoint ep;
                TCPChannel ch;
                TCPConnection conn;
                byte protocol = in.readByte();
                switch (protocol) {
                case TransportConstants.SingleOpProtocol:
                    ep = new TCPEndpoint(remoteHost, socket.getLocalPort(),
                                         endpoint.getClientSocketFactory(),
                                         endpoint.getServerSocketFactory());
                    ch = new TCPChannel(TCPTransport.this, ep);
                    conn = new TCPConnection(ch, socket, bufIn, bufOut);
                    handleMessages(conn, false);
                    break;
                case TransportConstants.StreamProtocol:
                    out.writeByte(TransportConstants.ProtocolAck);
                    if (tcpLog.isLoggable(Log.VERBOSE)) {
                        tcpLog.log(Log.VERBOSE, "(port " + port +
                            ") " + "suggesting " + remoteHost + ":" +
                            remotePort);
                    }
                    out.writeUTF(remoteHost);
                    out.writeInt(remotePort);
                    out.flush();
                    String clientHost = in.readUTF();
                    int    clientPort = in.readInt();
                    if (tcpLog.isLoggable(Log.VERBOSE)) {
                        tcpLog.log(Log.VERBOSE, "(port " + port +
                            ") client using " + clientHost + ":" + clientPort);
                    }
                    ep = new TCPEndpoint(remoteHost, socket.getLocalPort(),
                                         endpoint.getClientSocketFactory(),
                                         endpoint.getServerSocketFactory());
                    ch = new TCPChannel(TCPTransport.this, ep);
                    conn = new TCPConnection(ch, socket, bufIn, bufOut);
                    handleMessages(conn, true);
                    break;
                case TransportConstants.MultiplexProtocol:
                    if (tcpLog.isLoggable(Log.VERBOSE)) {
                        tcpLog.log(Log.VERBOSE, "(port " + port +
                            ") accepting multiplex protocol");
                    }
                    out.writeByte(TransportConstants.ProtocolAck);
                    if (tcpLog.isLoggable(Log.VERBOSE)) {
                        tcpLog.log(Log.VERBOSE, "(port " + port +
                            ") suggesting " + remoteHost + ":" + remotePort);
                    }
                    out.writeUTF(remoteHost);
                    out.writeInt(remotePort);
                    out.flush();
                    ep = new TCPEndpoint(in.readUTF(), in.readInt(),
                                         endpoint.getClientSocketFactory(),
                                         endpoint.getServerSocketFactory());
                    if (tcpLog.isLoggable(Log.VERBOSE)) {
                        tcpLog.log(Log.VERBOSE, "(port " +
                            port + ") client using " +
                            ep.getHost() + ":" + ep.getPort());
                    }
                    ConnectionMultiplexer multiplexer;
                    synchronized (channelTable) {
                        ch = getChannel(ep);
                        multiplexer =
                            new ConnectionMultiplexer(ch, bufIn, sockOut,
                                                      false);
                        ch.useMultiplexer(multiplexer);
                    }
                    multiplexer.run();
                    break;
                default:
                    out.writeByte(TransportConstants.ProtocolNack);
                    out.flush();
                    break;
                }
            } catch (IOException e) {
                tcpLog.log(Log.BRIEF, "terminated with exception:", e);
            } finally {
                closeSocket(socket);
            }
        }
    }
}
