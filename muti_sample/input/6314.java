public class NoExceptionOnClose {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    boolean useSSL = true;
    void doServerSide() throws Exception {
        ServerSocket  serverSocket;
        if (useSSL) {
            SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) sslssf.
                        createServerSocket(serverPort);
        } else {
           serverSocket = new ServerSocket(serverPort);
        }
        serverPort = serverSocket.getLocalPort();
        serverReady = true;
        Socket socket =  serverSocket.accept();
        InputStream sslIS = socket.getInputStream();
        OutputStream sslOS = socket.getOutputStream();
        int read = sslIS.read();
        System.out.println("Server read: " + read);
        sslOS.write(85);
        sslOS.flush();
        socket.close();
        socket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        Socket socket;
        if (useSSL) {
            SSLSocketFactory sslsf =
                        (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket)
                sslsf.createSocket("localhost", serverPort);
        } else
                socket = new Socket("localhost", serverPort);
        InputStream sslIS = socket.getInputStream();
        OutputStream sslOS = socket.getOutputStream();
        sslOS.write(80);
        sslOS.flush();
        int read = sslIS.read();
        System.out.println("client read: " + read);
        socket.close();
        boolean isSocketClosedThrown = false;
        try {
            sslOS.write(22);
            sslOS.flush();
        } catch (SocketException socketClosed) {
                System.out.println("Received \"" + socketClosed.getMessage()
                        + "\" exception as expected");
                isSocketClosedThrown = true;
          }
        if (!isSocketClosedThrown) {
                throw new Exception("No Exception thrown on write() after"
                                + " close()");
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
        new NoExceptionOnClose();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    NoExceptionOnClose() throws Exception {
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
