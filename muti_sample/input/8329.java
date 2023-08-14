public class ProviderInit {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        sslSocket.setNeedClientAuth(true);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        SSLContext context = SSLContext.getInstance("TLS");
        SSLSocketFactory sslsf = null;
        try {
            sslsf =
                (SSLSocketFactory) context.getSocketFactory();
            communicate(sslsf, false);
        } catch (IllegalStateException e) {
            System.out.println("Caught the right exception" + e);
        }
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                                                        "sunX509");
            TrustManager[] tms = tmf.getTrustManagers();
            context.init(null, tms, null);
            sslsf =
                (SSLSocketFactory) context.getSocketFactory();
            communicate(sslsf, false);
        } catch (IllegalStateException e) {
            System.out.println("Caught the right exception" + e);
        }
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunX509");
            KeyManager kms[] = kmf.getKeyManagers();
            context.init(kms, null, null);
            sslsf =
                (SSLSocketFactory) context.getSocketFactory();
            communicate(sslsf, true);
        } catch (Exception e) {
            System.out.println("Caught the right exception" + e);
            sslsf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            communicate(sslsf, false);
        }
    }
    void communicate(SSLSocketFactory sslsf, boolean needClientAuth)
                throws Exception {
        SSLSocket sslSocket = (SSLSocket) sslsf.createSocket(
                        "localhost", serverPort);
        sslSocket.setNeedClientAuth(needClientAuth);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(280);
        sslOS.flush();
        sslIS.read();
        sslSocket.close();
    }
    volatile int serverPort = 0;
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
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new ProviderInit();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    ProviderInit() throws Exception {
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
        if (serverException != null)
            throw serverException;
        if (clientException != null)
            throw clientException;
    }
    void startServer(boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide();
                    } catch (Exception e) {
                        System.err.println("Server died..." + e);
                        e.printStackTrace();
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
    void startClient(boolean newThread) throws Exception {
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
