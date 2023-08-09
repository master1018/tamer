public class PrintSSL {
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStore",
                System.getProperty("test.src", "./") + "/../../ssl/etc/keystore");
        SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        final ServerSocket server = sslssf.createServerSocket(0);
        System.out.println(server.getLocalPort());
        System.out.flush();
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(30000);
                    server.close();
                } catch (Exception e) {
                    ;
                }
                throw new RuntimeException("Timeout");
            }
        };
        t.setDaemon(true);
        t.start();
        ((SSLSocket)server.accept()).startHandshake();
    }
}
