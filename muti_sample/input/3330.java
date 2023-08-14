public class RetryHttps {
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
            for (int i = 0; i < 2; i++) {
            sslSocket = (SSLSocket) sslServerSocket.accept();
            InputStream is = sslSocket.getInputStream ();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            boolean flag = false;
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
            out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
            out.print("Content-Length: "+10+"\r\n");
            out.print("\r\n");
            out.print("Testing"+i+"\r\n");
            out.flush();
            sslSocket.close();
            }
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
        System.out.println("url is "+url.toString());
        HttpsURLConnection.setDefaultHostnameVerifier(new NameVerifier());
        http = (HttpsURLConnection)url.openConnection();
        int respCode = http.getResponseCode();
        int cl = http.getContentLength();
        InputStream is = http.getInputStream ();
        int count = 0;
        while (is.read() != -1 && count++ < cl);
        System.out.println("respCode1 = "+respCode);
        Thread.sleep(2000);
        url = new URL("https:
        http = (HttpsURLConnection)url.openConnection();
        respCode = http.getResponseCode();
        System.out.println("respCode2 = "+respCode);
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
        new RetryHttps();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    RetryHttps() throws Exception {
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
