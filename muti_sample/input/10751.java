public class InterruptedIO {
    public static void main(String[] args) throws Exception {
        String reason =
            "Test valid only on SunOS.\n" +
            "=========================\n" +
            "It was determined that Thread.interrupt() could \n" +
            "not be reliably return InterruptedIOException \n" +
            "on non-Solaris implementations.  Thread.interrupt() \n" +
            "API was updated in merlin (JDK 1.4) to reflect this.\n";
        System.out.println(reason);
        String osName = System.getProperty("os.name", "");
        if (!osName.equalsIgnoreCase("SunOS")) {
            System.out.println("Ignoring test on '" + osName + "'");
            return;
        }
        String testRoot = System.getProperty("test.src", ".");
        System.setProperty("javax.net.ssl.keyStore",
                           testRoot +
                           "/../../../../../../../etc/keystore");
        System.setProperty("javax.net.ssl.keyStorePassword",
                           "passphrase");
        System.setProperty("javax.net.ssl.trustStore",
                           testRoot +
                           "/../../../../../../../etc/truststore");
        Server server = new Server();
        server.start();
        Client client = new Client(server.getPort()); 
        client.start(); 
        System.out.println("Main - Pausing for 5 seconds...");
        Thread.sleep(5 * 1000);
        System.out.println("Main - Interrupting client reader thread");
        client.interrupt();
        client.join(); 
        if (client.failed())
            throw new Exception("Main - Test InterruptedIO failed "
                                + "on client side.");
        else
            System.out.println("Main - Test InterruptedIO successful!");
    }
    static class Server extends Thread {
        private SSLServerSocket ss;
        public Server() throws Exception {
            ss = (SSLServerSocket) SSLServerSocketFactory.getDefault().
                createServerSocket(0);
        }
        public int getPort() {
            return ss.getLocalPort();
        }
        public void run() {
            try {
                System.out.println("Server - Will accept connections on port "
                                   + getPort());
                Socket s = ss.accept();
                InputStream is = s.getInputStream();
                is.read();
            } catch (IOException e) {
            }
        }
    }
    static class Client extends Thread {
        private SSLSocket socket;
        private InputStream inStream;
        private boolean failed = false;
        public Client(int port) throws Exception {
            socket = (SSLSocket) SSLSocketFactory.getDefault().
                createSocket("localhost", port);
            inStream = socket.getInputStream();
            System.out.println("Client - "
                               + "Connected to: localhost" + ":" + port);
            System.out.println("Client - "
                               + "Doing SSL Handshake...");
            socket.startHandshake(); 
            System.out.println("Client - Done with SSL Handshake");
        }
        public void run() {
            try {
                System.out.println("Client - Reading from input stream ...");
                if (inStream.read() == -1) {
                    System.out.println("Client - End-of-stream detected");
                    failed = true;
                }
            } catch (InterruptedIOException e) {
                System.out.println("Client - "
                                   + "As expected, InterruptedIOException "
                                   + "was thrown. Message: "
                                   + e.getMessage());
            } catch (Exception e) {
                System.out.println("Client - Unexpected exception:");
                e.printStackTrace();
                failed = true;
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        public boolean failed() {
            return failed;
        }
    }
}
