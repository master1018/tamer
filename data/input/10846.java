public class ClientTimeout {
    static boolean separateServerThread = true;
    static String pathToStores = "/../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    static boolean debug = false;
    static boolean rhythm = true;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslSocket.startHandshake();
        String transFilename =
            System.getProperty("test.src", "./") + "/" +
                        this.getClass().getName() + ".java";
        MessageDigest md = MessageDigest.getInstance("SHA");
        DigestInputStream transIns = new DigestInputStream(
                new FileInputStream(transFilename), md);
        byte[] bytes = new byte[2000];
        int i = 0;
        while (true) {
            if (i >= bytes.length) {
                i = 0;
            }
            int length = 0;
            if ((length = transIns.read(bytes, 0, i++)) == -1) {
                break;
            } else {
                sslOS.write(bytes, 0, length);
                sslOS.flush();
                if (i % 3 == 0) {
                    Thread.sleep(300);  
                }
            }
        }
        serverDigest = md.digest();
        transIns.close();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        boolean caught = false;
        while (!serverReady) {
            Thread.sleep(50);
        }
        Socket baseSocket = new Socket("localhost", serverPort) {
            MyInputStream ins = null;
            public InputStream getInputStream() throws IOException {
                if (ins != null) {
                    return ins;
                } else {
                    ins = new MyInputStream(super.getInputStream());
                    return ins;
                }
            }
        };
        SSLSocketFactory sslsf =
            (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket(baseSocket, "localhost", serverPort, true);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslSocket.setSoTimeout(100); 
        while (true) {
            try {
                rhythm = true;
                sslSocket.startHandshake();
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("Handshaker exception: "
                        + e.getMessage());
            }
        }
        MessageDigest md = MessageDigest.getInstance("SHA");
        DigestInputStream transIns = new DigestInputStream(sslIS, md);
        byte[] bytes = new byte[2000];
        while (true) {
            try {
                rhythm = true;
                while (transIns.read(bytes, 0, 17) != -1) {
                    rhythm = true;
                }
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("InputStream Exception: "
                        + e.getMessage());
            }
        }
        while (serverDigest == null) {
            Thread.sleep(20);
        }
        byte[] cliDigest = md.digest();
        if (!Arrays.equals(cliDigest, serverDigest)) {
            throw new Exception("Application data trans error");
        }
        transIns.close();
        sslSocket.close();
    }
    static class MyInputStream extends InputStream {
        InputStream ins = null;
        public MyInputStream(InputStream ins) {
            this.ins = ins;
        }
        public int read() throws IOException {
            return read(new byte[1], 0, 1);
        }
        public int read(byte[] data, int offset, int len) throws IOException {
            if (!ClientTimeout.rhythm) {
                throw new SocketTimeoutException(
                        "Throwing a timeout exception");
            }
            ClientTimeout.rhythm = false;
            return ins.read(data, offset, len);
        }
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    volatile byte[] serverDigest = null;
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
        new ClientTimeout();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    ClientTimeout() throws Exception {
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
            System.out.print("Server Exception:");
            throw serverException;
        }
        if (clientException != null) {
            System.out.print("Client Exception:");
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
                        System.err.println(e);
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
