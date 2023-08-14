public class NullCerts {
    private static boolean separateServerThread = true;
    private final static String pathToStores = "../../../../../../../etc";
    private final static String keyStoreFile = "keystore";
    private final static String trustStoreFile = "truststore";
    private final static String passwd = "passphrase";
    private final static char[] cpasswd = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    private final static boolean DEBUG = false;
    private void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort, 3);
        sslServerSocket.setNeedClientAuth(true);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        try {
            sslIS.read();
            sslOS.write(85);
            sslOS.flush();
        } catch (SSLHandshakeException e) {
            System.out.println(
                "Should see a null cert chain exception for server: "
                + e.toString());
        }
        sslSocket.close();
        System.out.println("Server done and exiting!");
    }
    private void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        System.out.println("Starting test");
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyStore uks = KeyStore.getInstance("JKS");
        SSLContext ctx = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        uks.load(new FileInputStream(unknownFilename), cpasswd);
        kmf.init(uks, cpasswd);
        ks.load(new FileInputStream(trustFilename), cpasswd);
        tmf.init(ks);
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        SSLSocketFactory sslsf =
            (SSLSocketFactory) ctx.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        try {
            sslOS.write(280);
            sslOS.flush();
            sslIS.read();
            sslSocket.close();
        } catch (IOException e) {
            String str =
                "\nYou will either see a bad_certificate SSLException\n" +
                "or an IOException if the server shutdown while the\n" +
                "client was still sending the remainder of its \n" +
                "handshake data.";
            System.out.println(str + e.toString());
        }
    }
    volatile int serverPort = 0;
    private volatile Exception serverException = null;
    private volatile Exception clientException = null;
    private final static String keyFilename =
        System.getProperty("test.src", ".") + "/" + pathToStores +
        "/" + keyStoreFile;
    private final static String trustFilename =
        System.getProperty("test.src", ".") + "/" + pathToStores +
        "/" + trustStoreFile;
    private final static String unknownFilename =
        System.getProperty("test.src", ".") + "/" + pathToStores +
        "/" + "unknown_keystore";
    public static void main(String[] args) throws Exception {
        String testRoot = System.getProperty("test.src", ".");
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        if (DEBUG)
            System.setProperty("javax.net.debug", "all");
        new NullCerts();
    }
    private Thread clientThread = null;
    private Thread serverThread = null;
    NullCerts() throws Exception {
        if (separateServerThread) {
            startServer(true);
            startClient(false);
        } else {
            startClient(true);
            startServer(false);
        }
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }
        if (serverException != null) {
            System.err.print("Server Exception:");
            throw serverException;
        }
        if (clientException != null) {
            System.err.print("Client Exception:");
            throw clientException;
        }
    }
    private void startServer(boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide();
                    } catch (Exception e) {
                        System.err.println("Server died...");
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            doServerSide();
        }
    }
    private void startClient(boolean newThread) throws Exception {
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
