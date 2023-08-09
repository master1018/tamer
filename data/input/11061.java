public class SocketCreation {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide(int style) throws Exception {
        Socket sslSocket = null;
        switch (style) {
        case 0:
            sslSocket = acceptNormally0();
            break;
        case 1:
            sslSocket = acceptNormally1();
            break;
        case 2:
            sslSocket = acceptNormally2();
            break;
        case 3:
            sslSocket = acceptUnbound();
            break;
        case 4:
            sslSocket = acceptLayered();
            break;
        default:
            throw new Exception("Incorrectly written test for server side!");
        }
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        System.out.println("Server read: " + sslIS.read());
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
    }
    private Socket acceptNormally0() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        System.out.println("Server: Will call createServerSocket(int)");
        ServerSocket sslServerSocket = sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        System.out.println("Server: Will accept on SSL server socket...");
        serverReady = true;
        Socket sslSocket = sslServerSocket.accept();
        sslServerSocket.close();
        return sslSocket;
    }
    private Socket acceptNormally1() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        System.out.println("Server: Will call createServerSocket(int, int)");
        ServerSocket sslServerSocket = sslssf.createServerSocket(serverPort,
                                                                 1);
        serverPort = sslServerSocket.getLocalPort();
        System.out.println("Server: Will accept on SSL server socket...");
        serverReady = true;
        Socket sslSocket = sslServerSocket.accept();
        sslServerSocket.close();
        return sslSocket;
    }
    private Socket acceptNormally2() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        System.out.println("Server: Will call createServerSocket(int, " +
                           " int, InetAddress)");
        ServerSocket sslServerSocket = sslssf.createServerSocket(serverPort,
                                         1,
                                         InetAddress.getByName("localhost"));
        serverPort = sslServerSocket.getLocalPort();
        System.out.println("Server: Will accept on SSL server socket...");
        serverReady = true;
        Socket sslSocket = sslServerSocket.accept();
        sslServerSocket.close();
        return sslSocket;
    }
    private Socket acceptUnbound() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        System.out.println("Server: Will create unbound SSL server socket...");
        ServerSocket sslServerSocket = sslssf.createServerSocket();
        if (sslServerSocket.isBound())
            throw new Exception("Server socket is already bound!");
        System.out.println("Server: Will bind SSL server socket to port " +
                           serverPort + "...");
        sslServerSocket.bind(new java.net.InetSocketAddress(serverPort));
        if (!sslServerSocket.isBound())
            throw new Exception("Server socket is not bound!");
        serverReady = true;
        System.out.println("Server: Will accept on SSL server socket...");
        Socket sslSocket = sslServerSocket.accept();
        sslServerSocket.close();
        return sslSocket;
    }
    private Socket acceptLayered() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Server: Will create normal server socket bound"
                           + " to port " + serverPort + "...");
        ServerSocket ss = new ServerSocket(serverPort);
        serverPort = ss.getLocalPort();
        System.out.println("Server: Will accept on server socket...");
        serverReady = true;
        Socket s = ss.accept();
        ss.close();
        System.out.println("Server: Will layer SSLSocket on top of" +
                           " server socket...");
        SSLSocket sslSocket =
            (SSLSocket) sslsf.createSocket(s,
                                            s.getInetAddress().getHostName(),
                                            s.getPort(),
                                            true);
        sslSocket.setUseClientMode(false);
        return sslSocket;
    }
    void doClientSide(int style) throws Exception {
        Socket sslSocket = null;
        while (!serverReady) {
            Thread.sleep(50);
        }
        switch (style) {
        case 0:
            sslSocket = connectNormally0();
            break;
        case 1:
            sslSocket = connectNormally1();
            break;
        case 2:
            sslSocket = connectNormally2();
            break;
        case 3:
            sslSocket = connectNormally3();
            break;
        case 4:
            sslSocket = connectUnconnected();
            break;
        case 5:
            sslSocket = connectLayered();
            break;
        default:
            throw new Exception("Incorrectly written test for client side!");
        }
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(280);
        sslOS.flush();
        System.out.println("Client read: " + sslIS.read());
        sslSocket.close();
    }
    private Socket connectNormally0() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Client: Will call createSocket(String, int)");
        return sslsf.createSocket("localhost", serverPort);
    }
    private Socket connectNormally1() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Client: Will call createSocket(InetAddress, int)");
        return sslsf.createSocket(InetAddress.getByName("localhost"),
                                  serverPort);
    }
    private Socket connectNormally2() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Client: Will call createSocket(String," +
                           " int, InetAddress, int)");
        return sslsf.createSocket("localhost", serverPort,
                                  InetAddress.getByName("localhost"),
                                  0);
    }
    private Socket connectNormally3() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Client: Will call createSocket(InetAddress," +
                           " int, InetAddress, int)");
        return sslsf.createSocket(InetAddress.getByName("localhost"),
                                  serverPort,
                                  InetAddress.getByName("localhost"),
                                  0);
    }
    private Socket connectUnconnected() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Client: Will call createSocket()");
        Socket sslSocket = sslsf.createSocket();
        if (sslSocket.isConnected())
            throw new Exception("Client socket is already connected!");
        System.out.println("Client: Will connect to server on port " +
                           serverPort + "...");
        sslSocket.connect(new java.net.InetSocketAddress("localhost",
                                                         serverPort));
        if (!sslSocket.isConnected())
            throw new Exception("Client socket is not connected!");
        return sslSocket;
    }
    private Socket connectLayered() throws Exception {
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        System.out.println("Client: Will connect to server on port " +
                           serverPort + "...");
        Socket s = new Socket("localhost", serverPort);
        System.out.println("Client: Will layer SSL socket on top...");
        return sslsf.createSocket(s, "localhost", serverPort, true);
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
        new SocketCreation();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    SocketCreation() throws Exception {
        for (int serverStyle = 0; serverStyle < 5; serverStyle++) {
            System.out.println("-------------------------------------");
            for (int clientStyle = 0; clientStyle < 6; clientStyle++) {
                serverReady = false;
                startServer(separateServerThread, serverStyle);
                startClient(!separateServerThread, clientStyle);
                if (separateServerThread) {
                    serverThread.join();
                } else {
                    clientThread.join();
                }
                if (serverException != null)
                    throw serverException;
                if (clientException != null)
                    throw clientException;
                System.out.println();
            }
        }
    }
    void startServer(boolean newThread, final int style) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide(style);
                    } catch (Exception e) {
                        System.err.println("Server died..." + e);
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            doServerSide(style);
        }
    }
    void startClient(boolean newThread, final int style) throws Exception {
        if (newThread) {
            clientThread = new Thread() {
                public void run() {
                    try {
                        doClientSide(style);
                    } catch (Exception e) {
                        System.err.println("Client died...");
                        clientException = e;
                    }
                }
            };
            clientThread.start();
        } else {
            doClientSide(style);
        }
    }
}
