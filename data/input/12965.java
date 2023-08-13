public class RMIConnectorServer extends JMXConnectorServer {
    public static final String JNDI_REBIND_ATTRIBUTE =
        "jmx.remote.jndi.rebind";
    public static final String RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE =
        "jmx.remote.rmi.client.socket.factory";
    public static final String RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE =
        "jmx.remote.rmi.server.socket.factory";
    public RMIConnectorServer(JMXServiceURL url, Map<String,?> environment)
            throws IOException {
        this(url, environment, (MBeanServer) null);
    }
    public RMIConnectorServer(JMXServiceURL url, Map<String,?> environment,
                              MBeanServer mbeanServer)
            throws IOException {
        this(url, environment, (RMIServerImpl) null, mbeanServer);
    }
    public RMIConnectorServer(JMXServiceURL url, Map<String,?> environment,
                              RMIServerImpl rmiServerImpl,
                              MBeanServer mbeanServer)
            throws IOException {
        super(mbeanServer);
        if (url == null) throw new
            IllegalArgumentException("Null JMXServiceURL");
        if (rmiServerImpl == null) {
            final String prt = url.getProtocol();
            if (prt == null || !(prt.equals("rmi") || prt.equals("iiop"))) {
                final String msg = "Invalid protocol type: " + prt;
                throw new MalformedURLException(msg);
            }
            final String urlPath = url.getURLPath();
            if (!urlPath.equals("")
                && !urlPath.equals("/")
                && !urlPath.startsWith("/jndi/")) {
                final String msg = "URL path must be empty or start with " +
                    "/jndi/";
                throw new MalformedURLException(msg);
            }
        }
        if (environment == null)
            this.attributes = Collections.emptyMap();
        else {
            EnvHelp.checkAttributes(environment);
            this.attributes = Collections.unmodifiableMap(environment);
        }
        this.address = url;
        this.rmiServerImpl = rmiServerImpl;
    }
    public JMXConnector toJMXConnector(Map<String,?> env) throws IOException {
        if (!isActive()) throw new
            IllegalStateException("Connector is not active");
        Map<String, Object> usemap = new HashMap<String, Object>(
                (this.attributes==null)?Collections.<String, Object>emptyMap():
                    this.attributes);
        if (env != null) {
            EnvHelp.checkAttributes(env);
            usemap.putAll(env);
        }
        usemap = EnvHelp.filterAttributes(usemap);
        final RMIServer stub=(RMIServer)rmiServerImpl.toStub();
        return new RMIConnector(stub, usemap);
    }
    public synchronized void start() throws IOException {
        final boolean tracing = logger.traceOn();
        if (state == STARTED) {
            if (tracing) logger.trace("start", "already started");
            return;
        } else if (state == STOPPED) {
            if (tracing) logger.trace("start", "already stopped");
            throw new IOException("The server has been stopped.");
        }
        if (getMBeanServer() == null)
            throw new IllegalStateException("This connector server is not " +
                                            "attached to an MBean server");
        if (attributes != null) {
            String accessFile =
                (String) attributes.get("jmx.remote.x.access.file");
            if (accessFile != null) {
                MBeanServerForwarder mbsf;
                try {
                    mbsf = new MBeanServerFileAccessController(accessFile);
                } catch (IOException e) {
                    throw EnvHelp.initCause(
                        new IllegalArgumentException(e.getMessage()), e);
                }
                setMBeanServerForwarder(mbsf);
            }
        }
        try {
            if (tracing) logger.trace("start", "setting default class loader");
            defaultClassLoader = EnvHelp.resolveServerClassLoader(
                    attributes, getMBeanServer());
        } catch (InstanceNotFoundException infc) {
            IllegalArgumentException x = new
                IllegalArgumentException("ClassLoader not found: "+infc);
            throw EnvHelp.initCause(x,infc);
        }
        if (tracing) logger.trace("start", "setting RMIServer object");
        final RMIServerImpl rmiServer;
        if (rmiServerImpl != null)
            rmiServer = rmiServerImpl;
        else
            rmiServer = newServer();
        rmiServer.setMBeanServer(getMBeanServer());
        rmiServer.setDefaultClassLoader(defaultClassLoader);
        rmiServer.setRMIConnectorServer(this);
        rmiServer.export();
        try {
            if (tracing) logger.trace("start", "getting RMIServer object to export");
            final RMIServer objref = objectToBind(rmiServer, attributes);
            if (address != null && address.getURLPath().startsWith("/jndi/")) {
                final String jndiUrl = address.getURLPath().substring(6);
                if (tracing)
                    logger.trace("start", "Using external directory: " + jndiUrl);
                final boolean rebind = EnvHelp.computeBooleanFromString(
                    attributes,
                    JNDI_REBIND_ATTRIBUTE,false);
                if (tracing)
                    logger.trace("start", JNDI_REBIND_ATTRIBUTE + "=" + rebind);
                try {
                    if (tracing) logger.trace("start", "binding to " + jndiUrl);
                    final Hashtable<?, ?> usemap = EnvHelp.mapToHashtable(attributes);
                    bind(jndiUrl, usemap, objref, rebind);
                    boundJndiUrl = jndiUrl;
                } catch (NamingException e) {
                    throw newIOException("Cannot bind to URL ["+jndiUrl+"]: "
                                         + e, e);
                }
            } else {
                if (tracing) logger.trace("start", "Encoding URL");
                encodeStubInAddress(objref, attributes);
                if (tracing) logger.trace("start", "Encoded URL: " + this.address);
            }
        } catch (Exception e) {
            try {
                rmiServer.close();
            } catch (Exception x) {
            }
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else if (e instanceof IOException)
                throw (IOException) e;
            else
                throw newIOException("Got unexpected exception while " +
                                     "starting the connector server: "
                                     + e, e);
        }
        rmiServerImpl = rmiServer;
        synchronized(openedServers) {
            openedServers.add(this);
        }
        state = STARTED;
        if (tracing) {
            logger.trace("start", "Connector Server Address = " + address);
            logger.trace("start", "started.");
        }
    }
    public void stop() throws IOException {
        final boolean tracing = logger.traceOn();
        synchronized (this) {
            if (state == STOPPED) {
                if (tracing) logger.trace("stop","already stopped.");
                return;
            } else if (state == CREATED) {
                if (tracing) logger.trace("stop","not started yet.");
            }
            if (tracing) logger.trace("stop", "stopping.");
            state = STOPPED;
        }
        synchronized(openedServers) {
            openedServers.remove(this);
        }
        IOException exception = null;
        if (rmiServerImpl != null) {
            try {
                if (tracing) logger.trace("stop", "closing RMI server.");
                rmiServerImpl.close();
            } catch (IOException e) {
                if (tracing) logger.trace("stop", "failed to close RMI server: " + e);
                if (logger.debugOn()) logger.debug("stop",e);
                exception = e;
            }
        }
        if (boundJndiUrl != null) {
            try {
                if (tracing)
                    logger.trace("stop",
                          "unbind from external directory: " + boundJndiUrl);
                final Hashtable<?, ?> usemap = EnvHelp.mapToHashtable(attributes);
                InitialContext ctx =
                    new InitialContext(usemap);
                ctx.unbind(boundJndiUrl);
                ctx.close();
            } catch (NamingException e) {
                if (tracing) logger.trace("stop", "failed to unbind RMI server: "+e);
                if (logger.debugOn()) logger.debug("stop",e);
                if (exception == null)
                    exception = newIOException("Cannot bind to URL: " + e, e);
            }
        }
        if (exception != null) throw exception;
        if (tracing) logger.trace("stop", "stopped");
    }
    public synchronized boolean isActive() {
        return (state == STARTED);
    }
    public JMXServiceURL getAddress() {
        if (!isActive())
            return null;
        return address;
    }
    public Map<String,?> getAttributes() {
        Map<String, ?> map = EnvHelp.filterAttributes(attributes);
        return Collections.unmodifiableMap(map);
    }
    @Override
    public synchronized
        void setMBeanServerForwarder(MBeanServerForwarder mbsf) {
        super.setMBeanServerForwarder(mbsf);
        if (rmiServerImpl != null)
            rmiServerImpl.setMBeanServer(getMBeanServer());
    }
    @Override
    protected void connectionOpened(String connectionId, String message,
                                    Object userData) {
        super.connectionOpened(connectionId, message, userData);
    }
    @Override
    protected void connectionClosed(String connectionId, String message,
                                    Object userData) {
        super.connectionClosed(connectionId, message, userData);
    }
    @Override
    protected void connectionFailed(String connectionId, String message,
                                    Object userData) {
        super.connectionFailed(connectionId, message, userData);
    }
    void bind(String jndiUrl, Hashtable<?, ?> attributes,
              RMIServer rmiServer, boolean rebind)
        throws NamingException, MalformedURLException {
        InitialContext ctx =
            new InitialContext(attributes);
        if (rebind)
            ctx.rebind(jndiUrl, rmiServer);
        else
            ctx.bind(jndiUrl, rmiServer);
        ctx.close();
    }
    RMIServerImpl newServer() throws IOException {
        final boolean iiop = isIiopURL(address,true);
        final int port;
        if (address == null)
            port = 0;
        else
            port = address.getPort();
        if (iiop)
            return newIIOPServer(attributes);
        else
            return newJRMPServer(attributes, port);
    }
    private void encodeStubInAddress(
            RMIServer rmiServer, Map<String, ?> attributes)
            throws IOException {
        final String protocol, host;
        final int port;
        if (address == null) {
            if (IIOPHelper.isStub(rmiServer))
                protocol = "iiop";
            else
                protocol = "rmi";
            host = null; 
            port = 0;
        } else {
            protocol = address.getProtocol();
            host = (address.getHost().equals("")) ? null : address.getHost();
            port = address.getPort();
        }
        final String urlPath = encodeStub(rmiServer, attributes);
        address = new JMXServiceURL(protocol, host, port, urlPath);
    }
    static boolean isIiopURL(JMXServiceURL directoryURL, boolean strict)
        throws MalformedURLException {
        String protocol = directoryURL.getProtocol();
        if (protocol.equals("rmi"))
            return false;
        else if (protocol.equals("iiop"))
            return true;
        else if (strict) {
            throw new MalformedURLException("URL must have protocol " +
                                            "\"rmi\" or \"iiop\": \"" +
                                            protocol + "\"");
        }
        return false;
    }
    static String encodeStub(
            RMIServer rmiServer, Map<String, ?> env) throws IOException {
        if (IIOPHelper.isStub(rmiServer))
            return "/ior/" + encodeIIOPStub(rmiServer, env);
        else
            return "/stub/" + encodeJRMPStub(rmiServer, env);
    }
    static String encodeJRMPStub(
            RMIServer rmiServer, Map<String, ?> env)
            throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(rmiServer);
        oout.close();
        byte[] bytes = bout.toByteArray();
        return byteArrayToBase64(bytes);
    }
    static String encodeIIOPStub(
            RMIServer rmiServer, Map<String, ?> env)
            throws IOException {
        try {
            Object orb = IIOPHelper.getOrb(rmiServer);
            return IIOPHelper.objectToString(orb, rmiServer);
        } catch (RuntimeException x) {
            throw newIOException(x.getMessage(), x);
        }
    }
    private static RMIServer objectToBind(
            RMIServerImpl rmiServer, Map<String, ?> env)
        throws IOException {
        return RMIConnector.
            connectStub((RMIServer)rmiServer.toStub(),env);
    }
    private static RMIServerImpl newJRMPServer(Map<String, ?> env, int port)
            throws IOException {
        RMIClientSocketFactory csf = (RMIClientSocketFactory)
            env.get(RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE);
        RMIServerSocketFactory ssf = (RMIServerSocketFactory)
            env.get(RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE);
        return new RMIJRMPServerImpl(port, csf, ssf, env);
    }
    private static RMIServerImpl newIIOPServer(Map<String, ?> env)
            throws IOException {
        return new RMIIIOPServerImpl(env);
    }
    private static String byteArrayToBase64(byte[] a) {
        int aLen = a.length;
        int numFullGroups = aLen/3;
        int numBytesInPartialGroup = aLen - 3*numFullGroups;
        int resultLen = 4*((aLen + 2)/3);
        final StringBuilder result = new StringBuilder(resultLen);
        int inCursor = 0;
        for (int i=0; i<numFullGroups; i++) {
            int byte0 = a[inCursor++] & 0xff;
            int byte1 = a[inCursor++] & 0xff;
            int byte2 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[(byte0 << 4)&0x3f | (byte1 >> 4)]);
            result.append(intToAlpha[(byte1 << 2)&0x3f | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 0x3f]);
        }
        if (numBytesInPartialGroup != 0) {
            int byte0 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                int byte1 = a[inCursor++] & 0xff;
                result.append(intToAlpha[(byte0 << 4)&0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2)&0x3f]);
                result.append('=');
            }
        }
        return result.toString();
    }
    private static final char intToAlpha[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    private static IOException newIOException(String message,
                                              Throwable cause) {
        final IOException x = new IOException(message);
        return EnvHelp.initCause(x,cause);
    }
    private static ClassLogger logger =
        new ClassLogger("javax.management.remote.rmi", "RMIConnectorServer");
    private JMXServiceURL address;
    private RMIServerImpl rmiServerImpl;
    private final Map<String, ?> attributes;
    private ClassLoader defaultClassLoader = null;
    private String boundJndiUrl;
    private static final int CREATED = 0;
    private static final int STARTED = 1;
    private static final int STOPPED = 2;
    private int state = CREATED;
    private final static Set<RMIConnectorServer> openedServers =
            new HashSet<RMIConnectorServer>();
}
