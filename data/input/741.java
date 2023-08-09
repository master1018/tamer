public class Redirect {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    volatile static boolean closeReady = false;
    static boolean debug = false;
    static String postMsg = "Testing HTTP post on a https server";
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        try {
            InputStream sslIS = sslSocket.getInputStream();
            OutputStream sslOS = sslSocket.getOutputStream();
            BufferedReader br =
                        new BufferedReader(new InputStreamReader(sslIS));
            PrintStream ps = new PrintStream(sslOS);
            System.out.println("status line: "+br.readLine());
            ps.println("HTTP/1.1 307 Redirect");
            ps.println("Location: https:
                                                            "/index.html\n\n");
            ps.flush();
            sslSocket = (SSLSocket) sslServerSocket.accept();
            sslOS = sslSocket.getOutputStream();
            ps = new PrintStream(sslOS);
            ps.println("HTTP/1.1 200 Redirect succeeded\n\n");
            ps.flush();
        } finally {
            while (!closeReady) {
                Thread.sleep(50);
            }
            sslSocket.close();
            sslServerSocket.close();
        }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        URL url = new URL("https:
        HttpsURLConnection.setDefaultHostnameVerifier(
                                      new NameVerifier());
        HttpsURLConnection http = (HttpsURLConnection)url.openConnection();
        try {
            System.out.println("response header: "+http.getHeaderField(0));
            if (http.getResponseCode() != 200) {
                throw new RuntimeException("test Failed");
            }
        } finally {
            http.disconnect();
            closeReady = true;
        }
    }
    static class NameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
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
        new Redirect();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    Redirect() throws Exception {
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
