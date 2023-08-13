public class SessionTimeOutTests {
    static boolean separateServerThread = true;
    static String pathToStores = "/../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    private static int PORTS = 3;
    volatile static int serverReady = PORTS;
    static boolean debug = false;
    static int MAX_ACTIVE_CONNECTIONS = 3;
    void doServerSide(int serverPort, int serverConns) throws Exception {
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPorts[createdPorts++] = sslServerSocket.getLocalPort();
        serverReady--;
        int read = 0;
        int nConnections = 0;
        SSLSession sessions [] = new SSLSession [serverConns];
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
        while (serverReady > 0) {
            Thread.sleep(50);
        }
        int nConnections = 0;
        SSLSocket sslSockets[] = new SSLSocket [MAX_ACTIVE_CONNECTIONS];
        Vector sessions = new Vector();
        SSLSessionContext sessCtx = sslctx.getClientSessionContext();
        sessCtx.setSessionTimeout(10); 
        int timeout = sessCtx.getSessionTimeout();
        while (nConnections < MAX_ACTIVE_CONNECTIONS) {
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
        System.out.println();
        System.out.println("Current timeout is set to: " + timeout);
        System.out.println("Testing SSLSessionContext.getSession()......");
        System.out.println("========================================"
                                + "=======================");
        System.out.println("Session                                 "
                                + "Session-     Session");
        System.out.println("                                        "
                                + "lifetime     timedout?");
        System.out.println("========================================"
                                + "=======================");
        for (int i = 0; i < sessions.size(); i++) {
            SSLSession session = (SSLSession) sessions.elementAt(i);
            long currentTime  = System.currentTimeMillis();
            long creationTime = session.getCreationTime();
            long lifetime = (currentTime - creationTime) / 1000;
            System.out.print(session + "      " + lifetime + "            ");
            if (sessCtx.getSession(session.getId()) == null) {
                if (lifetime < timeout)
                    System.out.println("Invalidated before timeout");
                else
                    System.out.println("YES");
            } else {
                System.out.println("NO");
                if ((timeout != 0) && (lifetime > timeout)) {
                    throw new Exception("Session timeout test failed for the"
                        + " obove session");
                }
            }
            if (i == ((sessions.size()) / 2)) {
                System.out.println();
                sessCtx.setSessionTimeout(2); 
                timeout = sessCtx.getSessionTimeout();
                System.out.println("timeout is changed to: " + timeout);
                System.out.println();
            }
        }
        Enumeration e = sessCtx.getIds();
        System.out.println("----------------------------------------"
                                + "-----------------------");
        System.out.println("Testing SSLSessionContext.getId()......");
        System.out.println();
        SSLSession nextSess = null;
        SSLSession sess;
        for (int i = 0; i < sessions.size(); i++) {
            sess = (SSLSession)sessions.elementAt(i);
            String isTimedout = "YES";
            long currentTime  = System.currentTimeMillis();
            long creationTime  = sess.getCreationTime();
            long lifetime = (currentTime - creationTime) / 1000;
            if (nextSess != null) {
                if (isEqualSessionId(nextSess.getId(), sess.getId())) {
                    isTimedout = "NO";
                    nextSess = null;
                }
            } else if (e.hasMoreElements()) {
                nextSess = sessCtx.getSession((byte[]) e.nextElement());
                if ((nextSess != null) && isEqualSessionId(nextSess.getId(),
                                        sess.getId())) {
                    nextSess = null;
                    isTimedout = "NO";
                }
            }
            if ((timeout != 0) && (lifetime > timeout) &&
                        (isTimedout.equals("NO"))) {
                throw new Exception("Session timeout test failed for session: "
                                + sess + " lifetime: " + lifetime);
            }
            System.out.print(sess + "      " + lifetime);
            if (((timeout == 0) || (lifetime < timeout)) &&
                                  (isTimedout == "YES")) {
                isTimedout = "Invalidated before timeout";
            }
            System.out.println("            " + isTimedout);
        }
        for (int i = 0; i < nConnections; i++) {
            sslSockets[i].close();
        }
         System.out.println("----------------------------------------"
                                 + "-----------------------");
        System.out.println("Session timeout test passed");
    }
    boolean isEqualSessionId(byte[] id1, byte[] id2) {
        if (id1.length != id2.length)
           return false;
        else {
            for (int i = 0; i < id1.length; i++) {
                if (id1[i] != id2[i]) {
                   return false;
                }
            }
            return true;
        }
    }
    volatile int serverPorts[] = new int[PORTS];
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
        new SessionTimeOutTests();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    SessionTimeOutTests() throws Exception {
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
                        serverReady = 0;
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
