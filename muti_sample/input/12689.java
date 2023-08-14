public class LargePacket extends SSLEngineService {
    static boolean separateServerThread = true;
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLEngine ssle = createSSLEngine(false);
        InetSocketAddress isa =
                new InetSocketAddress(InetAddress.getLocalHost(), serverPort);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(isa);
        serverPort = ssc.socket().getLocalPort();
        serverReady = true;
        SocketChannel sc = ssc.accept();
        while (!sc.finishConnect() ) {
        }
        handshaking(ssle, sc);
        receive(ssle, sc);
        deliver(ssle, sc);
        sc.close();
        ssc.close();
    }
    void doClientSide() throws Exception {
        SSLEngine ssle = createSSLEngine(true);
        while (!serverReady) {
            Thread.sleep(50);
        }
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        InetSocketAddress isa =
                new InetSocketAddress(InetAddress.getLocalHost(), serverPort);
        sc.connect(isa);
        while (!sc.finishConnect() ) {
        }
        handshaking(ssle, sc);
        deliver(ssle, sc);
        receive(ssle, sc);
        sc.close();
    }
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    volatile int serverPort = 0;
    public static void main(String args[]) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new LargePacket();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    LargePacket() throws Exception {
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
