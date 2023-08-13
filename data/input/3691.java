public class SSLCtxAccessToSessCtx  {
    static boolean separateServerThread = true;
    static String pathToStores = "/../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide(int serverPort) throws Exception {
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPorts[createdPorts++] = sslServerSocket.getLocalPort();
        serverReady = true;
        int read = 0;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        read = sslIS.read();
        SSLSessionContext sslctxCache = sslctx.getServerSessionContext();
        SSLSessionContext sessCache = sslSocket.getSession().
                                getSessionContext();
        if (sessCache != sslctxCache)
            throw new Exception("Test failed, session_cache != sslctx_cache");
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        SSLSocket sslSocket;
        sslSocket = (SSLSocket) sslsf.
                createSocket("localhost", serverPorts[0]);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(237);
        sslOS.flush();
        SSLSession sess = sslSocket.getSession();
        SSLSessionContext sessCache = sess.getSessionContext();
        SSLSessionContext sslctxCache = sslctx.getClientSessionContext();
        if (sessCache != sslctxCache)
            throw new Exception("Test failed, session_cache != sslctx_cache");
        int read = sslIS.read();
        sslSocket.close();
    }
    volatile int serverPorts[] = new int[]{0};
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
        new SSLCtxAccessToSessCtx();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    SSLCtxAccessToSessCtx() throws Exception {
        if (separateServerThread) {
            for (int i = 0; i < serverPorts.length; i++) {
                startServer(serverPorts[i], true);
            }
            startClient(false);
        } else {
            startClient(true);
            for (int i = 0; i < serverPorts.length; i++) {
                startServer(serverPorts[i], false);
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
        System.out.println("The Session context tests passed");
    }
    void startServer(final int port,
                        boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide(port);
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
            doServerSide(port);
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
