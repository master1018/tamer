public class HttpsProtocols implements HostnameVerifier {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
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
        DataOutputStream out =
                        new DataOutputStream(sslSocket.getOutputStream());
        BufferedReader in =
            new BufferedReader(new InputStreamReader(
            sslSocket.getInputStream()));
        readRequest(in);
        byte [] bytecodes = "Hello world".getBytes();
        out.writeBytes("HTTP/1.0 200 OK\r\n");
        out.writeBytes("Content-Length: " + bytecodes.length +
                                           "\r\n");
        out.writeBytes("Content-Type: text/html\r\n\r\n");
        out.write(bytecodes);
        out.flush();
        sslSocket.close();
    }
    private static void readRequest(BufferedReader in)
        throws IOException {
        String line = null;
        do {
            line = in.readLine();
            System.out.println("Server received: " + line);
        } while ((line.length() != 0) &&
                (line.charAt(0) != '\r') && (line.charAt(0) != '\n'));
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(this);
        URL url = new URL("https:
        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
        System.out.println("response is " + urlc.getResponseCode());
    }
    public boolean verify(String hostname, SSLSession session) {
        return true;
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
        String prop = System.getProperty("https.protocols");
        System.out.println("protocols = " + prop);
        if ((prop == null) || (!prop.equals("SSLv3"))) {
            throw new Exception("https.protocols not set properly");
        }
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new HttpsProtocols();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    HttpsProtocols() throws Exception {
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
