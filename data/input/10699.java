public class EmptyCertificateAuthorities {
    static boolean separateServerThread = false;
    static String pathToStores = "/../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf = getSSLServerSF();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        sslServerSocket.setNeedClientAuth(true);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write('A');
        sslOS.flush();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        sslSocket.setEnabledProtocols(new String[] {"TLSv1.1"});
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write('B');
        sslOS.flush();
        sslIS.read();
        sslSocket.close();
    }
    private SSLServerSocketFactory getSSLServerSF() throws Exception {
        char [] password =
            System.getProperty("javax.net.ssl.keyStorePassword").toCharArray();
        String keyFilename = System.getProperty("javax.net.ssl.keyStore");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keyFilename), password);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
        kmf.init(ks, password);
        KeyManager[] kms = kmf.getKeyManagers();
        TrustManager[] tms = new MyX509TM[] {new MyX509TM()};
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kms, tms, null);
        return ctx.getServerSocketFactory();
    }
    static class MyX509TM implements X509TrustManager {
        X509TrustManager tm;
        public void checkClientTrusted(X509Certificate[] chain,
            String authType) throws CertificateException {
            if (tm == null) {
                initialize();
            }
            tm.checkClientTrusted(chain, authType);
        }
        public void checkServerTrusted(X509Certificate[] chain,
            String authType) throws CertificateException {
            if (tm == null) {
                initialize();
            }
            tm.checkServerTrusted(chain, authType);
        }
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
        private void initialize() throws CertificateException {
            String passwd =
                System.getProperty("javax.net.ssl.trustStorePassword");
            char [] password = passwd.toCharArray();
            String trustFilename =
                System.getProperty("javax.net.ssl.trustStore");
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream(trustFilename), password);
                TrustManagerFactory tmf =
                        TrustManagerFactory.getInstance("PKIX");
                tmf.init(ks);
                tm = (X509TrustManager)tmf.getTrustManagers()[0];
            } catch (Exception e) {
                throw new CertificateException("Unable to initialize TM");
            }
        }
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        String keyFilename =
            System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
            System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + trustStoreFile;
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new EmptyCertificateAuthorities();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    EmptyCertificateAuthorities() throws Exception {
        try {
            if (separateServerThread) {
                startServer(true);
                startClient(false);
            } else {
                startClient(true);
                startServer(false);
            }
        } catch (Exception e) {
        }
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }
        Exception local;
        Exception remote;
        String whichRemote;
        if (separateServerThread) {
            remote = serverException;
            local = clientException;
            whichRemote = "server";
        } else {
            remote = clientException;
            local = serverException;
            whichRemote = "client";
        }
        if ((local != null) && (remote != null)) {
            System.out.println(whichRemote + " also threw:");
            remote.printStackTrace();
            System.out.println();
            throw local;
        }
        if (remote != null) {
            throw remote;
        }
        if (local != null) {
            throw local;
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
            try {
                doServerSide();
            } catch (Exception e) {
                serverException = e;
            } finally {
                serverReady = true;
            }
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
            try {
                doClientSide();
            } catch (Exception e) {
                clientException = e;
            }
        }
    }
}
