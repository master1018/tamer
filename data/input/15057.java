public class JSSERenegotiate {
    static final String suite1 = "SSL_RSA_WITH_NULL_MD5";
    static final String suite2 = "SSL_RSA_WITH_NULL_SHA";
    static final String dataString = "This is a test";
    static boolean separateServerThread = false;
    static String pathToStores = "../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort, 3);
        sslServerSocket.setNeedClientAuth(true);
        sslServerSocket.setEnabledCipherSuites(new String[] {suite1, suite2 });
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        DataInputStream sslIS =
            new DataInputStream(sslSocket.getInputStream());
        DataOutputStream sslOS =
            new DataOutputStream(sslSocket.getOutputStream());
        while (true) {
            try {
                System.out.println("Received: " + sslIS.readUTF());
            } catch (SSLException e) {
                System.out.println ("Received wrong exception");
                break;
            } catch (IOException e) {
                System.out.println ("Received right exception");
                break;
            }
        }
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
        sslSocket.setEnabledCipherSuites(new String[] { suite1 });
        System.out.println("Enabled " + suite1);
        DataInputStream sslIS =
            new DataInputStream(sslSocket.getInputStream());
        DataOutputStream sslOS =
            new DataOutputStream(sslSocket.getOutputStream());
        BufferedReader in = new BufferedReader(
            new InputStreamReader(sslSocket.getInputStream()));
        sslOS.writeUTF("With " + suite1);
        sslSocket.setEnabledCipherSuites(new String[] { suite2 });
        sslSocket.startHandshake();
        System.out.println("Enabled " + suite2);
        sslOS.writeUTF("With " + suite2);
        sslOS.writeUTF("With " + suite2);
        sslOS.writeUTF("With " + suite2);
        sslSocket.setEnabledCipherSuites(new String[] { suite1 });
        sslSocket.startHandshake();
        System.out.println("Re-enabled " + suite1);
        sslOS.writeUTF("With " + suite1);
        sslOS.writeUTF("With " + suite1);
        sslOS.writeUTF("With " + suite1);
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
        new JSSERenegotiate();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    JSSERenegotiate() throws Exception {
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
