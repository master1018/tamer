public class SessionCacheSizeTests {
    static boolean separateServerThread = true;
    static String pathToStores = "/../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    static int MAX_ACTIVE_CONNECTIONS = 4;
    void doServerSide(int serverPort, int serverConns) throws Exception {
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPorts[createdPorts++] = sslServerSocket.getLocalPort();
        serverReady = true;
        int read = 0;
        int nConnections = 0;
        SSLSession sessions [] = new SSLSession [serverConns];
        SSLSessionContext sessCtx = sslctx.getServerSessionContext();
        while (nConnections < serverConns) {
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
            InputStream sslIS = sslSocket.getInputStream();
            OutputStream sslOS = sslSocket.getOutputStream();
            read = sslIS.read();
            sessions[nConnections] = sslSocket.getSession();
            sslOS.write(85);
            sslOS.flush();
            sslSocket.close();
            nConnections++;
        }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        int nConnections = 0;
        SSLSocket sslSockets[] = new SSLSocket [MAX_ACTIVE_CONNECTIONS];
        Vector sessions = new Vector();
        SSLSessionContext sessCtx = sslctx.getClientSessionContext();
        sessCtx.setSessionTimeout(0); 
        while (nConnections < (MAX_ACTIVE_CONNECTIONS - 1)) {
            sslSockets[nConnections] = (SSLSocket) sslsf.
                        createSocket("localhost",
                        serverPorts [nConnections % (serverPorts.length)]);
            InputStream sslIS = sslSockets[nConnections].getInputStream();
            OutputStream sslOS = sslSockets[nConnections].getOutputStream();
            sslOS.write(237);
            sslOS.flush();
            int read = sslIS.read();
            SSLSession sess = sslSockets[nConnections].getSession();
            if (!sessions.contains(sess))
                sessions.add(sess);
            nConnections++;
        }
        System.out.println("Current cacheSize is set to: " +
                                 sessCtx.getSessionCacheSize());
        System.out.println();
        System.out.println("Currently cached Sessions......");
        System.out.println("============================================"
                                + "============================");
        System.out.println("Session                                     "
                                + "      Session-last-accessTime");
        System.out.println("============================================"
                                + "============================");
        checkCachedSessions(sessCtx, nConnections);
        sessCtx.setSessionCacheSize(2);
        System.out.println("Session cache size changed to: "
                        + sessCtx.getSessionCacheSize());
        System.out.println();
        checkCachedSessions(sessCtx, nConnections);
        sessCtx.setSessionCacheSize(3);
        System.out.println("Session cache size changed to: "
                        + sessCtx.getSessionCacheSize());
        sslSockets[nConnections] = (SSLSocket) sslsf.
                        createSocket("localhost",
                        serverPorts [nConnections % (serverPorts.length)]);
        InputStream sslIS = sslSockets[nConnections].getInputStream();
        OutputStream sslOS = sslSockets[nConnections].getOutputStream();
        sslOS.write(237);
        sslOS.flush();
        int read = sslIS.read();
        SSLSession sess = sslSockets[nConnections].getSession();
        if (!sessions.contains(sess))
            sessions.add(sess);
        nConnections++;
        checkCachedSessions(sessCtx, nConnections);
        for (int i = 0; i < nConnections; i++) {
            sslSockets[i].close();
        }
        System.out.println("Session cache size tests passed");
    }
    void checkCachedSessions(SSLSessionContext sessCtx,
                int nConn) throws Exception {
        int nSessions = 0;
        Enumeration e = sessCtx.getIds();
        int cacheSize = sessCtx.getSessionCacheSize();
        SSLSession sess;
        while (e.hasMoreElements()) {
            sess = sessCtx.getSession((byte[]) e.nextElement());
            long lastAccessedTime  = sess.getLastAccessedTime();
                System.out.println(sess + "       "
                        +  new Date(lastAccessedTime));
            nSessions++;
        }
        System.out.println("--------------------------------------------"
                                + "----------------------------");
        if ((cacheSize > 0) && (nSessions > cacheSize)) {
            for (int conn = nConn; conn < MAX_ACTIVE_CONNECTIONS; conn++) {
                SSLSocket s = (SSLSocket) sslsf.createSocket("localhost",
                        serverPorts [conn % (serverPorts.length)]);
                s.close();
            }
            throw new Exception("Session cache size test failed,"
                + " current cache size: " + cacheSize + " #sessions cached: "
                + nSessions);
        }
    }
    volatile int serverPorts[] = new int[]{0, 0, 0, 0};
    volatile int createdPorts = 0;
    static SSLServerSocketFactory sslssf;
    static SSLSocketFactory sslsf;
    static SSLContext sslctx;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        System.setProperty("javax.net.ssl.sessionCacheSize", String.valueOf(0));
        sslctx = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFilename), passwd.toCharArray());
        kmf.init(ks, passwd.toCharArray());
        sslctx.init(kmf.getKeyManagers(), null, null);
        sslssf = (SSLServerSocketFactory) sslctx.getServerSocketFactory();
        sslsf = (SSLSocketFactory) sslctx.getSocketFactory();
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new SessionCacheSizeTests();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    SessionCacheSizeTests() throws Exception {
        int serverConns = MAX_ACTIVE_CONNECTIONS / (serverPorts.length);
        int remainingConns = MAX_ACTIVE_CONNECTIONS % (serverPorts.length);
        if (separateServerThread) {
            for (int i = 0; i < serverPorts.length; i++) {
                if (i < remainingConns)
                    startServer(serverPorts[i], (serverConns + 1), true);
                else
                    startServer(serverPorts[i], serverConns, true);
            }
            startClient(false);
        } else {
            startClient(true);
            for (int i = 0; i < serverPorts.length; i++) {
                if (i < remainingConns)
                    startServer(serverPorts[i], (serverConns + 1), false);
                else
                    startServer(serverPorts[i], serverConns, false);
            }
        }
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }
        if (serverException != null)
            throw serverException;
        if (clientException != null)
            throw clientException;
    }
    void startServer(final int port, final int nConns,
                        boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide(port, nConns);
                    } catch (Exception e) {
                        System.err.println("Server died...");
                        e.printStackTrace();
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            doServerSide(port, nConns);
        }
    }
    void startClient(boolean newThread)
                 throws Exception {
        if (newThread) {
            clientThread = new Thread() {
                public void run() {
                    try {
                        doClientSide();
                    } catch (Exception e) {
                        System.err.println("Client died...");
                        clientException = e;
                    }
                }
            };
            clientThread.start();
        } else {
            doClientSide();
        }
    }
}
