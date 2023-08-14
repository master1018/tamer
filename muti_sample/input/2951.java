public class SocksProxyVersion implements Runnable {
    final ServerSocket ss;
    volatile boolean failed;
    public static void main(String[] args) throws Exception {
        new SocksProxyVersion();
    }
    @SuppressWarnings("try")
    public SocksProxyVersion() throws Exception {
        ss = new ServerSocket(0);
        try (ServerSocket socket = ss) {
            runTest();
        }
    }
    void runTest() throws Exception {
        int port = ss.getLocalPort();
        Thread serverThread = new Thread(this);
        serverThread.start();
        System.setProperty("socksProxyHost", "localhost");
        System.setProperty("socksProxyPort", Integer.toString(port));
        System.setProperty("socksProxyVersion", Integer.toString(4));
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", port));
        } catch (SocketException e) {
        }
        System.setProperty("socksProxyVersion", Integer.toString(5));
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", port));
        } catch (SocketException e) {  }
        serverThread.join();
        if (failed) {
            throw new RuntimeException("socksProxyVersion not being set correctly");
        }
    }
    public void run() {
        try {
            try (Socket s = ss.accept()) {
                int version = (s.getInputStream()).read();
                if (version != 4) {
                    System.out.println("Got " + version + ", expected 4");
                    failed = true;
                }
            }
            try (Socket s = ss.accept()) {
                int version = (s.getInputStream()).read();
                if (version != 5) {
                    System.out.println("Got " + version + ", expected 5");
                    failed = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
