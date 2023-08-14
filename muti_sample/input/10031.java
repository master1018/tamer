public class NewSocketMethods {
    static boolean separateServerThread = true;
    static boolean useSSL = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        Socket socket;
        ServerSocket serverSocket;
        if (useSSL) {
            SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket =
                (SSLServerSocket) sslssf.createServerSocket(serverPort);
        } else {
           serverSocket = (ServerSocket) ServerSocketFactory.
                getDefault().createServerSocket(serverPort);
        }
        serverPort = serverSocket.getLocalPort();
        serverReady = true;
        try {
            socket = serverSocket.accept();
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            System.out.println("Server getChannel(): "
                         + socket.getChannel());
            try {
                socket.setOOBInline(true);
            } catch (IOException success) {
              }
            try {
                System.out.println("Server getOOBInline(): "
                                + socket.getOOBInline());
            } catch (IOException success) {
              }
            System.out.println("Server read: " + is.read());
            os.write(85);
            os.flush();
            socket.close();
         } catch (Exception unexpected) {
               throw new Exception(" test failed, caught exception: "
                        + unexpected);
           }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        Socket socket;
        if (useSSL) {
            SSLSocketFactory sslsf =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket plainSocket = new Socket("localhost", serverPort);
            socket = (SSLSocket)
                sslsf.createSocket(plainSocket, "localhost", serverPort, true);
        }
        else
            socket = new Socket("localhost", serverPort);
        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            socket.setTrafficClass(8);
            socket.setReuseAddress(true);
            System.out.println("Client getTrafficClass(): "
                        + socket.getTrafficClass());
            System.out.println("Client isInputShutdown() "
                        + socket.isInputShutdown());
            System.out.println("Client getReuseAddress(): "
                        + socket.getReuseAddress());
            os.write(237);
            os.flush();
            System.out.println("Client read: " + is.read());
            socket.close();
            System.out.println("Client isOutputShutdown() "
                        + socket.isOutputShutdown());
        } catch (Exception unexpected) {
            throw new Exception(" test failed, caught exception: "
                        + unexpected);
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
        new NewSocketMethods();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    NewSocketMethods() throws Exception {
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
                        System.err.println("Server died... ");
                        e.printStackTrace();
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
