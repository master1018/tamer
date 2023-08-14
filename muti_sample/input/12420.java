public class ServerIdentityTest {
    static boolean separateServerThread = true;
    static String pathToStores = "./";
    static String[] keyStoreFiles = {"dnsstore", "ipstore"};
    static String[] trustStoreFiles = {"dnsstore", "ipstore"};
    static String passwd = "changeit";
    boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            context.getServerSocketFactory();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        OutputStream sslOS = sslSocket.getOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sslOS));
        bw.write("HTTP/1.1 200 OK\r\n\r\n\r\n");
        bw.flush();
        Thread.sleep(2000);
        sslSocket.getSession().invalidate();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        String host = iphost? "127.0.0.1": "localhost";
        URL url = new URL("https:
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
        InputStream is = urlc.getInputStream();
        is.close();
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < keyStoreFiles.length; i++) {
            String keyFilename =
                System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + keyStoreFiles[i];
            String trustFilename =
                System.getProperty("test.src", ".") + "/" + pathToStores +
                "/" + trustStoreFiles[i];
            System.setProperty("javax.net.ssl.keyStore", keyFilename);
            System.setProperty("javax.net.ssl.keyStorePassword", passwd);
            System.setProperty("javax.net.ssl.trustStore", trustFilename);
            System.setProperty("javax.net.ssl.trustStorePassword", passwd);
            if (debug)
                System.setProperty("javax.net.debug", "all");
            SSLContext context = SSLContext.getInstance("SSL");
            KeyManager[] kms = new KeyManager[1];
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(keyFilename);
            ks.load(fis, passwd.toCharArray());
            fis.close();
            KeyManager km = new MyKeyManager(ks, passwd.toCharArray());
            kms[0] = km;
            context.init(kms, null, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(
                 context.getSocketFactory());
            System.out.println("Testing " + keyFilename);
            new ServerIdentityTest(context, keyStoreFiles[i]);
        }
    }
    Thread clientThread = null;
    Thread serverThread = null;
    SSLContext context;
    boolean iphost = false;
    ServerIdentityTest(SSLContext context, String keystore)
        throws Exception {
        this.context = context;
        iphost = keystore.equals("ipstore");
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
                        e.printStackTrace();
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
