public class SetClientMode {
    private static String[] algorithms = {"TLS", "SSL", "SSLv3", "TLS"};
    volatile int serverPort = 0;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    public SetClientMode() {
    }
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
        new SetClientMode().run();
    }
    public void run() throws Exception {
        for (int i = 0; i < algorithms.length; i++) {
            testCombo( algorithms[i] );
        }
    }
    public void testCombo(String algorithm) throws Exception {
        Exception modeException = null ;
        SSLServerSocketFactory ssf =
            (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket serverSocket =
            (SSLServerSocket)ssf.createServerSocket(serverPort);
        serverPort = serverSocket.getLocalPort();
        SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket clientSocket = (SSLSocket)sf.createSocket(
                                InetAddress.getLocalHost(),
                                serverPort );
        SocketClient client = new SocketClient(clientSocket);
        client.start();
        SSLSocket connectedSocket = (SSLSocket)serverSocket.accept();
        connectedSocket.getSession();
        try {
            clientSocket.setUseClientMode(false);
            modeException = new Exception("no IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            System.out.println("succeeded, we can't set the client mode");
        } catch (Exception e) {
            modeException = e;
        } finally {
            connectedSocket.close();
            serverSocket.close();
            if (modeException != null) {
                throw modeException;
            }
        }
        return;
    }
    class SocketClient extends Thread {
        SSLSocket clientsideSocket;
        Exception clientException = null;
        boolean done = false;
        public SocketClient( SSLSocket s ) {
            clientsideSocket = s;
        }
        public void run() {
            try {
                clientsideSocket.startHandshake();
            } catch ( Exception e ) {
                e.printStackTrace();
                clientException = e;
            } finally {
                done = true;
                try {
                    clientsideSocket.close();
                } catch ( IOException e ) {
                }
            }
            return;
        }
        boolean isDone() {
            return done;
        }
        Exception getException() {
            return clientException;
        }
    }
}
