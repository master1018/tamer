public class CloseKeepAliveCached {
    static Map cookies;
    ServerSocket ss;
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    private SSLServerSocket sslServerSocket = null;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = null;
        try {
            sslSocket = (SSLSocket) sslServerSocket.accept();
            for (int i = 0; i < 3 && !sslSocket.isClosed(); i++) {
                InputStream is = sslSocket.getInputStream ();
                BufferedReader r = new BufferedReader(
                                                new InputStreamReader(is));
                String x;
                while ((x=r.readLine()) != null) {
                    if (x.length() ==0) {
                        break;
                    }
                }
                PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    sslSocket.getOutputStream() ));
                out.print("HTTP/1.1 200 OK\r\n");
                out.print("Keep-Alive: timeout=15, max=100\r\n");
                out.print("Connection: Keep-Alive\r\n");
                out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
                out.print("Content-Length: 9\r\n");
                out.print("\r\n");
                out.print("Testing\r\n");
                out.flush();
                Thread.sleep(50);
            }
            sslSocket.close();
            sslServerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        try {
            HttpsURLConnection http = null;
            URL url = new URL("https:
            HttpsURLConnection.setDefaultHostnameVerifier(new NameVerifier());
            http = (HttpsURLConnection)url.openConnection();
            InputStream is = http.getInputStream ();
            while (is.read() != -1);
            is.close();
            url = new URL("https:
            http = (HttpsURLConnection)url.openConnection();
            is = http.getInputStream ();
            while (is.read() != -1);
            http.disconnect();
        } catch (IOException ioex) {
            if (sslServerSocket != null)
                sslServerSocket.close();
            throw ioex;
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
    public static void main(String args[]) throws Exception {
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
        new CloseKeepAliveCached();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    CloseKeepAliveCached() throws Exception {
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
