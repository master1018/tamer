public class CheckMyTrustedKeystore {
    static boolean separateServerThread = true;
    final static String pathToStores = "../../../../etc";
    final static String keyStoreFile = "keystore";
    final static String trustStoreFile = "truststore";
    final static String unknownStoreFile = "unknown_keystore";
    final static String passwd = "passphrase";
    final static char[] cpasswd = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    final static boolean debug = false;
    void doServerSide() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        com.sun.net.ssl.SSLContext ctx =
            com.sun.net.ssl.SSLContext.getInstance("TLS");
        com.sun.net.ssl.KeyManagerFactory kmf =
            com.sun.net.ssl.KeyManagerFactory.getInstance("SunX509");
        ks.load(new FileInputStream(keyFilename), cpasswd);
        kmf.init(ks, cpasswd);
        com.sun.net.ssl.TrustManager [] tms =
            new com.sun.net.ssl.TrustManager []
            { new MyComX509TrustManager() };
        ctx.init(kmf.getKeyManagers(), tms, null);
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) ctx.getServerSocketFactory();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        sslServerSocket.setNeedClientAuth(true);
        SSLContext ctx1 =
            SSLContext.getInstance("TLS");
        KeyManagerFactory kmf1 =
            KeyManagerFactory.getInstance("SunX509");
        TrustManager [] tms1 =
            new TrustManager []
            { new MyJavaxX509TrustManager() };
        kmf1.init(ks, cpasswd);
        ctx1.init(kmf1.getKeyManagers(), tms1, null);
        sslssf = (SSLServerSocketFactory) ctx1.getServerSocketFactory();
        SSLServerSocket sslServerSocket1 =
            (SSLServerSocket) sslssf.createServerSocket(serverPort1);
        serverPort1 = sslServerSocket1.getLocalPort();
        sslServerSocket1.setNeedClientAuth(true);
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        sslServerSocket.close();
        serverReady = false;
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
        sslSocket = (SSLSocket) sslServerSocket1.accept();
        sslIS = sslSocket.getInputStream();
        sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
        System.out.println("Server exiting!");
        System.out.flush();
    }
    void doTest(SSLSocket sslSocket) throws Exception {
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        System.out.println("  Writing");
        sslOS.write(280);
        sslOS.flush();
        System.out.println("  Reading");
        sslIS.read();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        System.out.println("==============");
        System.out.println("Starting test0");
        KeyStore uks = KeyStore.getInstance("JKS");
        SSLContext ctx =
            SSLContext.getInstance("TLS");
        KeyManagerFactory kmf =
            KeyManagerFactory.getInstance("SunX509");
        uks.load(new FileInputStream(unknownFilename), cpasswd);
        kmf.init(uks, cpasswd);
        TrustManager [] tms = new TrustManager []
            { new MyJavaxX509TrustManager() };
        ctx.init(kmf.getKeyManagers(), tms, null);
        SSLSocketFactory sslsf =
            (SSLSocketFactory) ctx.getSocketFactory();
        System.out.println("Trying first socket " + serverPort);
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        doTest(sslSocket);
        com.sun.net.ssl.SSLContext ctx1 =
            com.sun.net.ssl.SSLContext.getInstance("TLS");
        com.sun.net.ssl.KeyManagerFactory kmf1 =
            com.sun.net.ssl.KeyManagerFactory.getInstance("SunX509");
        kmf1.init(uks, cpasswd);
        com.sun.net.ssl.TrustManager [] tms1 =
            new com.sun.net.ssl.TrustManager []
            { new MyComX509TrustManager() };
        ctx1.init(kmf1.getKeyManagers(), tms1, null);
        sslsf = (SSLSocketFactory) ctx1.getSocketFactory();
        System.out.println("Trying second socket " + serverPort1);
        sslSocket = (SSLSocket) sslsf.createSocket("localhost",
            serverPort1);
        doTest(sslSocket);
        System.out.println("Completed test1");
    }
    int serverPort = 0;
    int serverPort1 = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    final static String keyFilename =
        System.getProperty("test.src", "./") + "/" + pathToStores +
        "/" + keyStoreFile;
    final static String unknownFilename =
        System.getProperty("test.src", "./") + "/" + pathToStores +
        "/" + unknownStoreFile;
    public static void main(String[] args) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new CheckMyTrustedKeystore();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    CheckMyTrustedKeystore() throws Exception {
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
            System.out.print("Server Exception:");
            throw serverException;
        }
        if (clientException != null) {
            System.out.print("Client Exception:");
            throw clientException;
        }
    }
    void startServer(boolean newThread) throws Exception {
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
class MyComX509TrustManager implements com.sun.net.ssl.X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        return (new X509Certificate[0]);
    }
    public boolean isClientTrusted(X509Certificate[] chain) {
        System.out.println("    IsClientTrusted?");
        return true;
    }
    public boolean isServerTrusted(X509Certificate[] chain) {
        System.out.println("    IsServerTrusted?");
        return true;
    }
}
class MyJavaxX509TrustManager implements X509TrustManager {
    public X509Certificate[] getAcceptedIssuers() {
        return (new X509Certificate[0]);
    }
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        System.out.println("    CheckClientTrusted(" + authType + ")?");
    }
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        System.out.println("    CheckServerTrusted(" + authType + ")?");
    }
}
