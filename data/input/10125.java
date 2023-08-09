public class CipherSuiteOrder {
    static boolean separateServerThread = true;
    static String pathToStores = "/../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        String enabledSuites[] = {
                        "SSL_RSA_WITH_DES_CBC_SHA",
                        "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                        "SSL_RSA_WITH_RC4_128_MD5"
                        };
        sslSocket.setEnabledCipherSuites(enabledSuites);
        System.out.println("");
        System.out.println("server enabled suites: ");
        System.out.println("=====================");
        String suites[] = sslSocket.getEnabledCipherSuites();
        for (int i = 0; i < suites.length; i++)
            System.out.println(suites[i]);
        System.out.println("");
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        int read = sslIS.read();
        System.out.println("Server read: " + read);
        sslOS.write(85);
        sslOS.flush();
        String cipherSuiteChosen = sslSocket.getSession().getCipherSuite();
        System.out.println("Cipher suite in use: " +
                                cipherSuiteChosen);
        sslSocket.close();
        if (!cipherSuiteChosen.equals("SSL_RSA_WITH_RC4_128_MD5"))
            throw new Exception("Test failed: Wrong cipher suite is chosen");
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        String enabledSuites[] = {
                        "SSL_RSA_WITH_RC4_128_MD5",
                        "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                        "SSL_RSA_WITH_RC4_128_SHA",
                        "SSL_DHE_DSS_WITH_DES_CBC_SHA"
                        };
        sslSocket.setEnabledCipherSuites(enabledSuites);
        System.out.println("");
        System.out.println("client enabled suites: ");
        System.out.println("======================");
        String[] suites = sslSocket.getEnabledCipherSuites();
        for (int i = 0; i < suites.length; i++)
            System.out.println(suites[i]);
        System.out.println("");
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(80);
        sslOS.flush();
        int read = sslIS.read();
        System.out.println("client read: " + read);
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
        new CipherSuiteOrder();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    CipherSuiteOrder() throws Exception {
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
