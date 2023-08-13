public class ExtendedKeySocket {
    static boolean separateServerThread = false;
    static String pathToStores = "../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static char [] passwd = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    static boolean debug = false;
    private String keyFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + keyStoreFile;
    private String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
    SSLContext getSSLContext(boolean abs) throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        KeyStore keyKS = KeyStore.getInstance("JKS");
        keyKS.load(new FileInputStream(keyFilename), passwd);
        KeyStore trustKS = KeyStore.getInstance("JKS");
        trustKS.load(new FileInputStream(trustFilename), passwd);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyKS, passwd);
        KeyManager [] kms = kmf.getKeyManagers();
        if (!(kms[0] instanceof X509ExtendedKeyManager)) {
            throw new Exception("kms[0] not X509ExtendedKeyManager");
        }
        if (abs) {
            kms = new KeyManager [] {
                new MyX509ExtendedKeyManager((X509ExtendedKeyManager)kms[0])
            };
        } else {
            kms = new KeyManager [] {
                new MyX509KeyManager((X509KeyManager)kms[0])
            };
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(trustKS);
        TrustManager [] tms = tmf.getTrustManagers();
        ctx.init(kms, tms, null);
        return ctx;
    }
    void doServerSide() throws Exception {
        System.out.println("Starting Server1");
        doServerTest(getSSLContext(true));
        System.out.println("Starting Server2");
        doServerTest(getSSLContext(false));
        System.out.println("Finishing Server");
    }
    void doServerTest(SSLContext ctx) throws Exception {
        serverPort = 0;
        SSLServerSocketFactory sslssf = ctx.getServerSocketFactory();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        sslServerSocket.setNeedClientAuth(true);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        System.out.println("Starting Client1");
        doClientTest(getSSLContext(true));
        System.out.println("Starting Client2");
        doClientTest(getSSLContext(false));
        System.out.println("Finishing Client");
    }
    void doClientTest(SSLContext ctx) throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        serverReady = false;
        SSLSocketFactory sslsf = ctx.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
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
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new ExtendedKeySocket();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    ExtendedKeySocket() throws Exception {
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
