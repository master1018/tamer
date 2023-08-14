public class NotifyHandshakeTest implements HandshakeCompletedListener {
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static int serverPort = 0;
    public boolean set;
    SSLSession sess;
    static public int triggerState = 0;
    static public void trigger() {
        triggerState++;
    }
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        set = true;
        sess = event.getSession();
        trigger();
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
        SSLSocketFactory sslsf =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLServerSocketFactory sslssf =
                (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket sslss =
            (SSLServerSocket)sslssf.createServerSocket(serverPort);
        serverPort = sslss.getLocalPort();
        Server server = new Server(sslss);
        server.start();
        System.out.println("Server started...");
        SSLSocket socket =
            (SSLSocket)sslsf.createSocket("localhost", serverPort);
        edu.NotifyHandshakeTestHeyYou heyYou =
            new edu.NotifyHandshakeTestHeyYou(socket);
        heyYou.start();
        while (triggerState < 1) {
            Thread.sleep(500);
        }
        System.out.println("HeyYou thread ready...");
        NotifyHandshakeTest listener = new NotifyHandshakeTest();
        socket.addHandshakeCompletedListener(listener);
        System.out.println("Client starting handshake...");
        socket.startHandshake();
        System.out.println("Client done handshaking...");
        InputStream is = socket.getInputStream();
        if ((byte)is.read() != (byte)0x77) {
            throw new Exception("problem reading byte");
        }
        while (triggerState < 3) {
            Thread.sleep(500);
        }
        boolean heyYouSet = heyYou.set;
        AccessControlContext heyYouACC = heyYou.acc;
        SSLSession  heyYouSess = heyYou.ssls;
        heyYou.interrupt();
        heyYou.join();
        server.join();
        socket.close();
        if (!heyYouSet) {
            throw new Exception("HeyYou's wasn't set");
        }
        if (!listener.set) {
            throw new Exception("This' wasn't set");
        }
        if (heyYouACC.equals(AccessController.getContext())) {
            throw new Exception("Access Control Contexts were the same");
        }
        if (!heyYouSess.equals(listener.sess)) {
            throw new Exception("SSLSessions were not equal");
        }
        System.out.println("Everything Passed");
    }
    static class Server extends Thread {
        SSLServerSocket ss;
        Server(SSLServerSocket ss) {
            this.ss = ss;
        }
        public void run() {
            try {
                System.out.println("Server accepting socket...");
                SSLSocket s = (SSLSocket) ss.accept();
                System.out.println(
                    "Server accepted socket...starting handshake");
                s.startHandshake();
                System.out.println("Server done handshaking");
                OutputStream os = s.getOutputStream();
                os.write(0x77);
                os.flush();
                System.out.println("Server returning");
            } catch (Exception e) {
                System.out.println("Server died");
                e.printStackTrace();
            }
        }
    }
}
