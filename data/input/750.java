public class ClientModeClientAuth {
    static boolean separateServerThread = false;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    void doServerSide() throws Exception {
        ServerSocket serverSocket = null;
        serverSocket = new ServerSocket(serverPort);
        serverPort = serverSocket.getLocalPort();
        serverReady = true;
        Socket socket = serverSocket.accept();
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        out.write(85);
        out.flush();
        in.read();
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket =
            (SSLSocket) sslsf.createSocket(
                socket, socket.getInetAddress().getHostName(),
                socket.getPort(), true);
        sslSocket.setUseClientMode(false);
        sslSocket.setNeedClientAuth(true);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(85);
        sslOS.flush();
        sslIS.read();
        System.out.println("About to get PeerCertificates");
        Certificate[] certs =
            sslSocket.getSession().getPeerCertificates();
        if (certs[0] instanceof X509Certificate) {
            System.out.println("Peer: " +
                ((X509Certificate)certs[0]).getSubjectDN());
        }
        sslIS.close();
        sslOS.close();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        Socket socket = new Socket("localhost", serverPort);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        in.read();
        out.write(280);
        out.flush();
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket(socket, socket.getInetAddress().getHostName(),
                socket.getPort(), true);
        sslSocket.setUseClientMode(true);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write(280);
        sslOS.flush();
        sslIS.close();
        sslOS.close();
        sslSocket.close();
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
        new ClientModeClientAuth();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    ClientModeClientAuth() throws Exception {
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
                        System.out.println("Server died...");
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
                        System.out.println("Client died...");
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
