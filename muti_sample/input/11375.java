public class ReadHandshake {
    static boolean separateServerThread = true;
    private final static String[] CLIENT_SUITES = new String[] {
        "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA",
    };
    private final static String[] SERVER_SUITES = new String[] {
        "SSL_DH_anon_WITH_RC4_128_MD5",
    };
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLSocket sslSocket = null;
        SSLServerSocket sslServerSocket = null;
        try {
            SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            sslServerSocket =
                (SSLServerSocket) sslssf.createServerSocket(serverPort);
            serverPort = sslServerSocket.getLocalPort();
            sslServerSocket.setEnabledCipherSuites(SERVER_SUITES);
            serverReady = true;
            System.out.println("Server waiting for connection");
            sslSocket = (SSLSocket) sslServerSocket.accept();
            InputStream sslIS = sslSocket.getInputStream();
            OutputStream sslOS = sslSocket.getOutputStream();
            System.out.println("Server starting handshake...");
            try {
                sslIS.read();
                throw new Exception("No handshake exception on server side");
            } catch (IOException e) {
                System.out.println("Handshake failed on server side, OK");
            }
            for (int i = 0; i < 3; i++) {
                try {
                    int ch;
                    if ((ch = sslIS.read()) != -1) {
                        throw new Exception("Read succeeded server side: "
                            + ch);
                    }
                } catch (IOException e) {
                    System.out.println("Exception for read() on server, OK");
                }
            }
        } finally {
            closeSocket(sslSocket);
            closeSocket(sslServerSocket);
        }
    }
    private static void closeSocket(Socket s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
        }
    }
    private static void closeSocket(ServerSocket s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (Exception e) {
        }
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(80);
        }
        SSLSocket sslSocket = null;
        try {
            SSLSocketFactory sslsf =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
            sslSocket = (SSLSocket)
                sslsf.createSocket("localhost", serverPort);
            sslSocket.setEnabledCipherSuites(CLIENT_SUITES);
            InputStream sslIS = sslSocket.getInputStream();
            OutputStream sslOS = sslSocket.getOutputStream();
            System.out.println("Client starting handshake...");
            try {
                sslIS.read();
                throw new Exception("No handshake exception on client side");
            } catch (IOException e) {
                System.out.println("Handshake failed on client side, OK");
            }
            for (int i = 0; i < 3; i++) {
                try {
                    int ch;
                    if ((ch = sslIS.read()) != -1) {
                        throw new Exception("Read succeeded on client side: "
                            + ch);
                    }
                } catch (IOException e) {
                    System.out.println("Exception for read() on client, OK");
                }
            }
        } finally {
            sslSocket.close();
        }
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new ReadHandshake();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    ReadHandshake() throws Exception {
        startServer(true);
        startClient(true);
        serverThread.join();
        clientThread.join();
        if (serverException != null) {
            if (clientException != null) {
                System.out.println("Client exception:");
                clientException.printStackTrace(System.out);
            }
            throw serverException;
        }
        if (clientException != null) {
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
