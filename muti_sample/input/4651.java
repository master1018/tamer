public class ClientServer {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf = getDefaultServer();
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
        if (!serverTM.wasServerChecked() && serverTM.wasClientChecked()) {
            System.out.println("SERVER TEST PASSED!");
        } else {
            throw new Exception("SERVER TEST FAILED!  " +
                !serverTM.wasServerChecked() + " " +
                serverTM.wasClientChecked());
        }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        SSLSocketFactory sslsf = getDefaultClient();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(280);
        sslOS.flush();
        sslIS.read();
        sslSocket.close();
        if (clientTM.wasServerChecked() && !clientTM.wasClientChecked()) {
            System.out.println("CLIENT TEST PASSED!");
        } else {
            throw new Exception("CLIENT TEST FAILED!  " +
                clientTM.wasServerChecked() + " " +
                !clientTM.wasClientChecked());
        }
    }
    private com.sun.net.ssl.SSLContext getDefault(MyX509TM tm)
            throws Exception {
        String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
        char[] passphrase = "passphrase".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFilename), passphrase);
        com.sun.net.ssl.KeyManagerFactory kmf =
            com.sun.net.ssl.KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(trustFilename), passphrase);
        com.sun.net.ssl.TrustManagerFactory tmf =
            com.sun.net.ssl.TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        com.sun.net.ssl.TrustManager [] tms = tmf.getTrustManagers();
        int i;
        for (i = 0; i < tms.length; i++) {
            if (tms[i] instanceof com.sun.net.ssl.X509TrustManager) {
                break;
            }
        }
        if (i >= tms.length) {
            throw new Exception("Couldn't find X509TM");
        }
        tm.init((com.sun.net.ssl.X509TrustManager)tms[i]);
        tms = new MyX509TM [] { tm };
        com.sun.net.ssl.SSLContext ctx =
            com.sun.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tms, null);
        return ctx;
    }
    MyX509TM serverTM;
    MyX509TM clientTM;
    private SSLServerSocketFactory getDefaultServer() throws Exception {
        serverTM = new MyX509TM();
        return getDefault(serverTM).getServerSocketFactory();
    }
    private SSLSocketFactory getDefaultClient() throws Exception {
        clientTM = new MyX509TM();
        return getDefault(clientTM).getSocketFactory();
    }
    static class MyX509TM implements com.sun.net.ssl.X509TrustManager {
        com.sun.net.ssl.X509TrustManager tm;
        boolean clientChecked;
        boolean serverChecked;
        void init(com.sun.net.ssl.X509TrustManager x509TM) {
            tm = x509TM;
        }
        public boolean wasClientChecked() {
            return clientChecked;
        }
        public boolean wasServerChecked() {
            return serverChecked;
        }
        public boolean isClientTrusted(X509Certificate[] chain) {
            clientChecked = true;
            return true;
        }
        public boolean isServerTrusted(X509Certificate[] chain) {
            serverChecked = true;
            return true;
        }
        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers();
        }
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new ClientServer();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    ClientServer() throws Exception {
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
