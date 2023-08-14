public class X509ExtendedTMEnabled {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    private final static char[] cpasswd = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
                                getContext(true).getServerSocketFactory();
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
        SSLSocketFactory sslsf = getContext(false).getSocketFactory();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        SSLParameters params = sslSocket.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS");
        sslSocket.setSSLParameters(params);
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
    MyExtendedX509TM serverTM;
    MyExtendedX509TM clientTM;
    private SSLContext getContext(boolean server) throws Exception {
        String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFilename), cpasswd);
        kmf.init(ks, cpasswd);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        KeyStore ts = KeyStore.getInstance("JKS");
        ts.load(new FileInputStream(trustFilename), cpasswd);
        tmf.init(ts);
        TrustManager tms[] = tmf.getTrustManagers();
        if (tms == null || tms.length == 0) {
            throw new Exception("unexpected trust manager implementation");
        } else {
           if (!(tms[0] instanceof X509TrustManager)) {
            throw new Exception("unexpected trust manager implementation: "
                                + tms[0].getClass().getCanonicalName());
           }
        }
        if (server) {
            serverTM = new MyExtendedX509TM((X509TrustManager)tms[0]);
            tms = new TrustManager[] {serverTM};
        } else {
            clientTM = new MyExtendedX509TM((X509TrustManager)tms[0]);
            tms = new TrustManager[] {clientTM};
        }
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tms, null);
        return ctx;
    }
    static class MyExtendedX509TM extends X509ExtendedTrustManager
            implements X509TrustManager {
        X509TrustManager tm;
        boolean clientChecked;
        boolean serverChecked;
        MyExtendedX509TM(X509TrustManager tm) {
            clientChecked = false;
            serverChecked = false;
            this.tm = tm;
        }
        public boolean wasClientChecked() {
            return clientChecked;
        }
        public boolean wasServerChecked() {
            return serverChecked;
        }
        public void checkClientTrusted(X509Certificate chain[], String authType)
                throws CertificateException {
            tm.checkClientTrusted(chain, authType);
        }
        public void checkServerTrusted(X509Certificate chain[], String authType)
                throws CertificateException {
            tm.checkServerTrusted(chain, authType);
        }
        public X509Certificate[] getAcceptedIssuers() {
            return tm.getAcceptedIssuers();
        }
        public void checkClientTrusted(X509Certificate[] chain, String authType,
                Socket socket) throws CertificateException {
            clientChecked = true;
            tm.checkClientTrusted(chain, authType);
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType,
                Socket socket) throws CertificateException {
            serverChecked = true;
            tm.checkServerTrusted(chain, authType);
        }
        public void checkClientTrusted(X509Certificate[] chain, String authType,
            SSLEngine engine) throws CertificateException {
            clientChecked = true;
            tm.checkClientTrusted(chain, authType);
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType,
            SSLEngine engine) throws CertificateException {
            serverChecked = true;
            tm.checkServerTrusted(chain, authType);
        }
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new X509ExtendedTMEnabled();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    X509ExtendedTMEnabled() throws Exception {
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
