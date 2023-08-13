public class TCPEndpoint implements Endpoint {
    private String host;
    private int port;
    private final RMIClientSocketFactory csf;
    private final RMIServerSocketFactory ssf;
    private int listenPort = -1;
    private TCPTransport transport = null;
    private static String localHost;
    private static boolean localHostKnown;
    private static int getInt(String name, int def) {
        return AccessController.doPrivileged(new GetIntegerAction(name, def));
    }
    private static boolean getBoolean(String name) {
        return AccessController.doPrivileged(new GetBooleanAction(name));
    }
    private static String getHostnameProperty() {
        return AccessController.doPrivileged(
            new GetPropertyAction("java.rmi.server.hostname"));
    }
    static {
        localHostKnown = true;
        localHost = getHostnameProperty();
        if (localHost == null) {
            try {
                InetAddress localAddr = InetAddress.getLocalHost();
                byte[] raw = localAddr.getAddress();
                if ((raw[0] == 127) &&
                    (raw[1] ==   0) &&
                    (raw[2] ==   0) &&
                    (raw[3] ==   1)) {
                    localHostKnown = false;
                }
                if (getBoolean("java.rmi.server.useLocalHostName")) {
                    localHost = FQDN.attemptFQDN(localAddr);
                } else {
                    localHost = localAddr.getHostAddress();
                }
            } catch (Exception e) {
                localHostKnown = false;
                localHost = null;
            }
        }
        if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
            TCPTransport.tcpLog.log(Log.BRIEF,
                "localHostKnown = " + localHostKnown +
                ", localHost = " + localHost);
        }
    }
    private static final
        Map<TCPEndpoint,LinkedList<TCPEndpoint>> localEndpoints =
        new HashMap<TCPEndpoint,LinkedList<TCPEndpoint>>();
    public TCPEndpoint(String host, int port) {
        this(host, port, null, null);
    }
    public TCPEndpoint(String host, int port, RMIClientSocketFactory csf,
                       RMIServerSocketFactory ssf)
    {
        if (host == null)
            host = "";
        this.host = host;
        this.port = port;
        this.csf = csf;
        this.ssf = ssf;
    }
    public static TCPEndpoint getLocalEndpoint(int port) {
        return getLocalEndpoint(port, null, null);
    }
    public static TCPEndpoint getLocalEndpoint(int port,
                                               RMIClientSocketFactory csf,
                                               RMIServerSocketFactory ssf)
    {
        TCPEndpoint ep = null;
        synchronized (localEndpoints) {
            TCPEndpoint endpointKey = new TCPEndpoint(null, port, csf, ssf);
            LinkedList<TCPEndpoint> epList = localEndpoints.get(endpointKey);
            String localHost = resampleLocalHost();
            if (epList == null) {
                ep = new TCPEndpoint(localHost, port, csf, ssf);
                epList = new LinkedList<TCPEndpoint>();
                epList.add(ep);
                ep.listenPort = port;
                ep.transport = new TCPTransport(epList);
                localEndpoints.put(endpointKey, epList);
                if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
                    TCPTransport.tcpLog.log(Log.BRIEF,
                        "created local endpoint for socket factory " + ssf +
                        " on port " + port);
                }
            } else {
                synchronized (epList) {
                    ep = epList.getLast();
                    String lastHost = ep.host;
                    int lastPort =  ep.port;
                    TCPTransport lastTransport = ep.transport;
                    if (localHost != null && !localHost.equals(lastHost)) {
                        if (lastPort != 0) {
                            epList.clear();
                        }
                        ep = new TCPEndpoint(localHost, lastPort, csf, ssf);
                        ep.listenPort = port;
                        ep.transport = lastTransport;
                        epList.add(ep);
                    }
                }
            }
        }
        return ep;
    }
    private static String resampleLocalHost() {
        String hostnameProperty = getHostnameProperty();
        synchronized (localEndpoints) {
            if (hostnameProperty != null) {
                if (!localHostKnown) {
                    setLocalHost(hostnameProperty);
                } else if (!hostnameProperty.equals(localHost)) {
                    localHost = hostnameProperty;
                    if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
                        TCPTransport.tcpLog.log(Log.BRIEF,
                            "updated local hostname to: " + localHost);
                    }
                }
            }
            return localHost;
        }
    }
    static void setLocalHost(String host) {
        synchronized (localEndpoints) {
            if (!localHostKnown) {
                localHost = host;
                localHostKnown = true;
                if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
                    TCPTransport.tcpLog.log(Log.BRIEF,
                        "local host set to " + host);
                }
                for (LinkedList<TCPEndpoint> epList : localEndpoints.values())
                {
                    synchronized (epList) {
                        for (TCPEndpoint ep : epList) {
                            ep.host = host;
                        }
                    }
                }
            }
        }
    }
    static void setDefaultPort(int port, RMIClientSocketFactory csf,
                               RMIServerSocketFactory ssf)
    {
        TCPEndpoint endpointKey = new TCPEndpoint(null, 0, csf, ssf);
        synchronized (localEndpoints) {
            LinkedList<TCPEndpoint> epList = localEndpoints.get(endpointKey);
            synchronized (epList) {
                int size = epList.size();
                TCPEndpoint lastEp = epList.getLast();
                for (TCPEndpoint ep : epList) {
                    ep.port = port;
                }
                if (size > 1) {
                    epList.clear();
                    epList.add(lastEp);
                }
            }
            TCPEndpoint newEndpointKey = new TCPEndpoint(null, port, csf, ssf);
            localEndpoints.put(newEndpointKey, epList);
            if (TCPTransport.tcpLog.isLoggable(Log.BRIEF)) {
                TCPTransport.tcpLog.log(Log.BRIEF,
                    "default port for server socket factory " + ssf +
                    " and client socket factory " + csf +
                    " set to " + port);
            }
        }
    }
    public Transport getOutboundTransport() {
        TCPEndpoint localEndpoint = getLocalEndpoint(0, null, null);
        return localEndpoint.transport;
    }
    private static Collection<TCPTransport> allKnownTransports() {
        Set<TCPTransport> s;
        synchronized (localEndpoints) {
            s = new HashSet<TCPTransport>(localEndpoints.size());
            for (LinkedList<TCPEndpoint> epList : localEndpoints.values()) {
                TCPEndpoint ep = epList.getFirst();
                s.add(ep.transport);
            }
        }
        return s;
    }
    public static void shedConnectionCaches() {
        for (TCPTransport transport : allKnownTransports()) {
            transport.shedConnectionCaches();
        }
    }
    public void exportObject(Target target) throws RemoteException {
        transport.exportObject(target);
    }
    public Channel getChannel() {
        return getOutboundTransport().getChannel(this);
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public int getListenPort() {
        return listenPort;
    }
    public Transport getInboundTransport() {
        return transport;
    }
    public RMIClientSocketFactory getClientSocketFactory() {
        return csf;
    }
    public RMIServerSocketFactory getServerSocketFactory() {
        return ssf;
    }
    public String toString() {
        return "[" + host + ":" + port +
            (ssf != null ? "," + ssf : "") +
            (csf != null ? "," + csf : "") +
            "]";
    }
    public int hashCode() {
        return port;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof TCPEndpoint)) {
            TCPEndpoint ep = (TCPEndpoint) obj;
            if (port != ep.port || !host.equals(ep.host))
                return false;
            if (((csf == null) ^ (ep.csf == null)) ||
                ((ssf == null) ^ (ep.ssf == null)))
                return false;
            if ((csf != null) &&
                !(csf.getClass() == ep.csf.getClass() && csf.equals(ep.csf)))
                return false;
            if ((ssf != null) &&
                !(ssf.getClass() == ep.ssf.getClass() && ssf.equals(ep.ssf)))
                return false;
            return true;
        } else {
            return false;
        }
    }
    private static final int FORMAT_HOST_PORT           = 0;
    private static final int FORMAT_HOST_PORT_FACTORY   = 1;
    public void write(ObjectOutput out) throws IOException {
        if (csf == null) {
            out.writeByte(FORMAT_HOST_PORT);
            out.writeUTF(host);
            out.writeInt(port);
        } else {
            out.writeByte(FORMAT_HOST_PORT_FACTORY);
            out.writeUTF(host);
            out.writeInt(port);
            out.writeObject(csf);
        }
    }
    public static TCPEndpoint read(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        String host;
        int port;
        RMIClientSocketFactory csf = null;
        byte format = in.readByte();
        switch (format) {
          case FORMAT_HOST_PORT:
            host = in.readUTF();
            port = in.readInt();
            break;
          case FORMAT_HOST_PORT_FACTORY:
            host = in.readUTF();
            port = in.readInt();
            csf = (RMIClientSocketFactory) in.readObject();
          break;
          default:
            throw new IOException("invalid endpoint format");
        }
        return new TCPEndpoint(host, port, csf, null);
    }
    public void writeHostPortFormat(DataOutput out) throws IOException {
        if (csf != null) {
            throw new InternalError("TCPEndpoint.writeHostPortFormat: " +
                "called for endpoint with non-null socket factory");
        }
        out.writeUTF(host);
        out.writeInt(port);
    }
    public static TCPEndpoint readHostPortFormat(DataInput in)
        throws IOException
    {
        String host = in.readUTF();
        int port = in.readInt();
        return new TCPEndpoint(host, port);
    }
    private static RMISocketFactory chooseFactory() {
        RMISocketFactory sf = RMISocketFactory.getSocketFactory();
        if (sf == null) {
            sf = TCPTransport.defaultSocketFactory;
        }
        return sf;
    }
    Socket newSocket() throws RemoteException {
        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
            TCPTransport.tcpLog.log(Log.VERBOSE,
                "opening socket to " + this);
        }
        Socket socket;
        try {
            RMIClientSocketFactory clientFactory = csf;
            if (clientFactory == null) {
                clientFactory = chooseFactory();
            }
            socket = clientFactory.createSocket(host, port);
        } catch (java.net.UnknownHostException e) {
            throw new java.rmi.UnknownHostException(
                "Unknown host: " + host, e);
        } catch (java.net.ConnectException e) {
            throw new java.rmi.ConnectException(
                "Connection refused to host: " + host, e);
        } catch (IOException e) {
            try {
                TCPEndpoint.shedConnectionCaches();
            } catch (OutOfMemoryError mem) {
            } catch (Exception ex) {
            }
            throw new ConnectIOException("Exception creating connection to: " +
                host, e);
        }
        try {
            socket.setTcpNoDelay(true);
        } catch (Exception e) {
        }
        try {
            socket.setKeepAlive(true);
        } catch (Exception e) {
        }
        return socket;
    }
    ServerSocket newServerSocket() throws IOException {
        if (TCPTransport.tcpLog.isLoggable(Log.VERBOSE)) {
            TCPTransport.tcpLog.log(Log.VERBOSE,
                "creating server socket on " + this);
        }
        RMIServerSocketFactory serverFactory = ssf;
        if (serverFactory == null) {
            serverFactory = chooseFactory();
        }
        ServerSocket server = serverFactory.createServerSocket(listenPort);
        if (listenPort == 0)
            setDefaultPort(server.getLocalPort(), csf, ssf);
        return server;
    }
    private static class FQDN implements Runnable {
        private String reverseLookup;
        private String hostAddress;
        private FQDN(String hostAddress) {
            this.hostAddress = hostAddress;
        }
        static String attemptFQDN(InetAddress localAddr)
            throws java.net.UnknownHostException
        {
            String hostName = localAddr.getHostName();
            if (hostName.indexOf('.') < 0 ) {
                String hostAddress = localAddr.getHostAddress();
                FQDN f = new FQDN(hostAddress);
                int nameServiceTimeOut =
                    TCPEndpoint.getInt("sun.rmi.transport.tcp.localHostNameTimeOut",
                                       10000);
                try {
                    synchronized(f) {
                        f.getFQDN();
                        f.wait(nameServiceTimeOut);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                hostName = f.getHost();
                if ((hostName == null) || (hostName.equals(""))
                    || (hostName.indexOf('.') < 0 )) {
                    hostName = hostAddress;
                }
            }
            return hostName;
        }
        private void getFQDN() {
            Thread t = AccessController.doPrivileged(
                new NewThreadAction(FQDN.this, "FQDN Finder", true));
            t.start();
        }
        private synchronized String getHost() {
            return reverseLookup;
        }
        public void run()  {
            String name = null;
            try {
                name = InetAddress.getByName(hostAddress).getHostName();
            } catch (java.net.UnknownHostException e) {
            } finally {
                synchronized(this) {
                    reverseLookup = name;
                    this.notify();
                }
            }
        }
    }
}
