public class NonAutoClose {
    private static boolean separateServerThread = true;
    private final static String pathToStores = "../../../../../../../etc";
    private final static String keyStoreFile = "keystore";
    private final static String trustStoreFile = "truststore";
    private final static String passwd = "passphrase";
    private final static char[] cpasswd = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    private final static boolean DEBUG = false;
    private final static boolean VERBOSE = false;
    private final static int NUM_ITERATIONS  = 10;
    private final static int PLAIN_SERVER_VAL = 1;
    private final static int PLAIN_CLIENT_VAL = 2;
    private final static int TLS_SERVER_VAL = 3;
    private final static int TLS_CLIENT_VAL = 4;
    void expectValue(int got, int expected, String msg) throws IOException {
        if (VERBOSE) {
            System.out.println(msg + ": read (" + got + ")");
        }
        if (got != expected) {
            throw new IOException(msg + ": read (" + got
                + ") but expecting(" + expected + ")");
        }
    }
     void doServerSide() throws Exception {
        if (VERBOSE) {
            System.out.println("Starting server");
        }
        SSLSocketFactory sslsf =
             (SSLSocketFactory) SSLSocketFactory.getDefault();
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        SERVER_PORT = serverSocket.getLocalPort();
        serverReady = true;
        Socket plainSocket = serverSocket.accept();
        InputStream is = plainSocket.getInputStream();
        OutputStream os = plainSocket.getOutputStream();
        expectValue(is.read(), PLAIN_CLIENT_VAL, "Server");
        os.write(PLAIN_SERVER_VAL);
        os.flush();
        for (int i = 1; i <= NUM_ITERATIONS; i++) {
            if (VERBOSE) {
                System.out.println("=================================");
                System.out.println("Server Iteration #" + i);
            }
            SSLSocket ssls = (SSLSocket) sslsf.createSocket(plainSocket,
                SERVER_NAME, plainSocket.getPort(), false);
            ssls.setUseClientMode(false);
            InputStream sslis = ssls.getInputStream();
            OutputStream sslos = ssls.getOutputStream();
            expectValue(sslis.read(), TLS_CLIENT_VAL, "Server");
            sslos.write(TLS_SERVER_VAL);
            sslos.flush();
            sslis.close();
            sslos.close();
            ssls.close();
            if (VERBOSE) {
                System.out.println("TLS socket is closed");
            }
        }
        expectValue(is.read(), PLAIN_CLIENT_VAL, "Server");
        os.write(PLAIN_SERVER_VAL);
        os.flush();
        is.close();
        os.close();
        plainSocket.close();
        if (VERBOSE) {
            System.out.println("Server plain socket is closed");
        }
    }
    private void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        if (VERBOSE) {
            System.out.println("Starting client");
        }
        SSLSocketFactory sslsf =
             (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket plainSocket = new Socket(SERVER_NAME, SERVER_PORT);
        InputStream is = plainSocket.getInputStream();
        OutputStream os = plainSocket.getOutputStream();
        os.write(PLAIN_CLIENT_VAL);
        os.flush();
        expectValue(is.read(), PLAIN_SERVER_VAL, "Client");
        for (int i = 1; i <= NUM_ITERATIONS; i++) {
            if (VERBOSE) {
                System.out.println("===================================");
                System.out.println("Client Iteration #" + i);
              }
            SSLSocket ssls = (SSLSocket) sslsf.createSocket(plainSocket,
               SERVER_NAME, plainSocket.getPort(), false);
            ssls.setUseClientMode(true);
            InputStream sslis = ssls.getInputStream();
            OutputStream sslos = ssls.getOutputStream();
            sslos.write(TLS_CLIENT_VAL);
            sslos.flush();
            expectValue(sslis.read(), TLS_SERVER_VAL, "Client");
            sslis.close();
            sslos.close();
            ssls.close();
            if (VERBOSE) {
                System.out.println("Client TLS socket is closed");
            }
        }
        os.write(PLAIN_CLIENT_VAL);
        os.flush();
        expectValue(is.read(), PLAIN_SERVER_VAL, "Client");
        is.close();
        os.close();
        plainSocket.close();
        if (VERBOSE) {
            System.out.println("Client plain socket is closed");
        }
    }
    private volatile int SERVER_PORT = 0;
    private final static String SERVER_NAME = "localhost";
    private volatile Exception serverException = null;
    private volatile Exception clientException = null;
    private final static String keyFilename =
        System.getProperty("test.src", ".") + "/" + pathToStores +
        "/" + keyStoreFile;
    private final static String trustFilename =
        System.getProperty("test.src", ".") + "/" + pathToStores +
        "/" + trustStoreFile;
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        if (DEBUG)
            System.setProperty("javax.net.debug", "all");
        new NonAutoClose();
    }
    private Thread clientThread = null;
    private Thread serverThread = null;
    NonAutoClose() throws Exception {
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
            System.err.print("Server Exception:");
            throw serverException;
        }
        if (clientException != null) {
            System.err.print("Client Exception:");
            throw clientException;
        }
    }
    private void startServer(boolean newThread) throws Exception {
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
    private void startClient(boolean newThread) throws Exception {
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
