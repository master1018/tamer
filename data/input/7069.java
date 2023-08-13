public class RMIMasterSocketFactory extends RMISocketFactory {
    static int logLevel = LogStream.parseLevel(getLogLevel());
    private static String getLogLevel() {
        return java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.rmi.transport.proxy.logLevel"));
    }
    static final Log proxyLog =
        Log.getLog("sun.rmi.transport.tcp.proxy",
                   "transport", RMIMasterSocketFactory.logLevel);
    private static long connectTimeout = getConnectTimeout();
    private static long getConnectTimeout() {
        return java.security.AccessController.doPrivileged(
                new GetLongAction("sun.rmi.transport.proxy.connectTimeout",
                              15000)).longValue(); 
    }
    private static final boolean eagerHttpFallback =
        java.security.AccessController.doPrivileged(new GetBooleanAction(
            "sun.rmi.transport.proxy.eagerHttpFallback")).booleanValue();
    private Hashtable successTable = new Hashtable();
    private static final int MaxRememberedHosts = 64;
    private Vector hostList = new Vector(MaxRememberedHosts);
    protected RMISocketFactory initialFactory = new RMIDirectSocketFactory();
    protected Vector altFactoryList;
    public RMIMasterSocketFactory() {
        altFactoryList = new Vector(2);
        boolean setFactories = false;
        try {
            String proxyHost;
            proxyHost = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("http.proxyHost"));
            if (proxyHost == null)
                proxyHost = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction("proxyHost"));
            Boolean tmp = java.security.AccessController.doPrivileged(
                new sun.security.action.GetBooleanAction("java.rmi.server.disableHttp"));
            if (!tmp.booleanValue() &&
                (proxyHost != null && proxyHost.length() > 0)) {
                setFactories = true;
            }
        } catch (Exception e) {
            setFactories = true;
        }
        if (setFactories) {
            altFactoryList.addElement(new RMIHttpToPortSocketFactory());
            altFactoryList.addElement(new RMIHttpToCGISocketFactory());
        }
    }
    public Socket createSocket(String host, int port)
        throws IOException
    {
        if (proxyLog.isLoggable(Log.BRIEF)) {
            proxyLog.log(Log.BRIEF, "host: " + host + ", port: " + port);
        }
        if (altFactoryList.size() == 0) {
            return initialFactory.createSocket(host, port);
        }
        RMISocketFactory factory;
        factory = (RMISocketFactory) successTable.get(host);
        if (factory != null) {
            if (proxyLog.isLoggable(Log.BRIEF)) {
                proxyLog.log(Log.BRIEF,
                    "previously successful factory found: " + factory);
            }
            return factory.createSocket(host, port);
        }
        Socket initialSocket = null;
        Socket fallbackSocket = null;
        final AsyncConnector connector =
            new AsyncConnector(initialFactory, host, port,
                AccessController.getContext());
        IOException initialFailure = null;
        try {
            synchronized (connector) {
                Thread t = java.security.AccessController.doPrivileged(
                    new NewThreadAction(connector, "AsyncConnector", true));
                t.start();
                try {
                    long now = System.currentTimeMillis();
                    long deadline = now + connectTimeout;
                    do {
                        connector.wait(deadline - now);
                        initialSocket = checkConnector(connector);
                        if (initialSocket != null)
                            break;
                        now = System.currentTimeMillis();
                    } while (now < deadline);
                } catch (InterruptedException e) {
                    throw new InterruptedIOException(
                        "interrupted while waiting for connector");
                }
            }
            if (initialSocket == null)
                throw new NoRouteToHostException(
                    "connect timed out: " + host);
            proxyLog.log(Log.BRIEF, "direct socket connection successful");
            return initialSocket;
        } catch (UnknownHostException e) {
            initialFailure = e;
        } catch (NoRouteToHostException e) {
            initialFailure = e;
        } catch (SocketException e) {
            if (eagerHttpFallback) {
                initialFailure = e;
            } else {
                throw e;
            }
        } finally {
            if (initialFailure != null) {
                if (proxyLog.isLoggable(Log.BRIEF)) {
                    proxyLog.log(Log.BRIEF,
                        "direct socket connection failed: ", initialFailure);
                }
                for (int i = 0; i < altFactoryList.size(); ++ i) {
                    factory = (RMISocketFactory) altFactoryList.elementAt(i);
                    try {
                        if (proxyLog.isLoggable(Log.BRIEF)) {
                            proxyLog.log(Log.BRIEF,
                                "trying with factory: " + factory);
                        }
                        Socket testSocket = factory.createSocket(host, port);
                        InputStream in = testSocket.getInputStream();
                        int b = in.read(); 
                        testSocket.close();
                    } catch (IOException ex) {
                        if (proxyLog.isLoggable(Log.BRIEF)) {
                            proxyLog.log(Log.BRIEF, "factory failed: ", ex);
                        }
                        continue;
                    }
                    proxyLog.log(Log.BRIEF, "factory succeeded");
                    try {
                        fallbackSocket = factory.createSocket(host, port);
                    } catch (IOException ex) {  
                    }                           
                    break;
                }
            }
        }
        synchronized (successTable) {
            try {
                synchronized (connector) {
                    initialSocket = checkConnector(connector);
                }
                if (initialSocket != null) {
                    if (fallbackSocket != null)
                        fallbackSocket.close();
                    return initialSocket;
                }
                connector.notUsed();
            } catch (UnknownHostException e) {
                initialFailure = e;
            } catch (NoRouteToHostException e) {
                initialFailure = e;
            } catch (SocketException e) {
                if (eagerHttpFallback) {
                    initialFailure = e;
                } else {
                    throw e;
                }
            }
            if (fallbackSocket != null) {
                rememberFactory(host, factory);
                return fallbackSocket;
            }
            throw initialFailure;
        }
    }
    void rememberFactory(String host, RMISocketFactory factory) {
        synchronized (successTable) {
            while (hostList.size() >= MaxRememberedHosts) {
                successTable.remove(hostList.elementAt(0));
                hostList.removeElementAt(0);
            }
            hostList.addElement(host);
            successTable.put(host, factory);
        }
    }
    Socket checkConnector(AsyncConnector connector)
        throws IOException
    {
        Exception e = connector.getException();
        if (e != null) {
            e.fillInStackTrace();
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new Error("internal error: " +
                    "unexpected checked exception: " + e.toString());
            }
        }
        return connector.getSocket();
    }
    public ServerSocket createServerSocket(int port) throws IOException {
        return initialFactory.createServerSocket(port);
    }
    private class AsyncConnector implements Runnable {
        private RMISocketFactory factory;
        private String host;
        private int port;
        private AccessControlContext acc;
        private Exception exception = null;
        private Socket socket = null;
        private boolean cleanUp = false;
        AsyncConnector(RMISocketFactory factory, String host, int port,
                       AccessControlContext acc)
        {
            this.factory = factory;
            this.host    = host;
            this.port    = port;
            this.acc     = acc;
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(host, port);
            }
        }
        public void run() {
            try {
                try {
                    Socket temp = factory.createSocket(host, port);
                    synchronized (this) {
                        socket = temp;
                        notify();
                    }
                    rememberFactory(host, factory);
                    synchronized (this) {
                        if (cleanUp)
                          try {
                              socket.close();
                          } catch (IOException e) {
                          }
                    }
                } catch (Exception e) {
                    synchronized (this) {
                        exception = e;
                        notify();
                    }
                }
            } finally {
            }
        }
        private synchronized Exception getException() {
            return exception;
        }
        private synchronized Socket getSocket() {
            return socket;
        }
        synchronized void notUsed() {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
            cleanUp = true;
        }
    }
}
