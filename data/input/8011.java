public class HttpsURLConnectionLocalCertificateChain
        implements HandshakeCompletedListener,
        HostnameVerifier {
    static boolean separateServerThread = false;
    static String pathToStores = "../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile static boolean serverReady = false;
    public boolean verify(String hostname, SSLSession session) {
        try {
            Certificate [] certs = session.getPeerCertificates();
            for (int i = 0; i< certs.length; i++) {
                if (certs[i] instanceof X509Certificate) {
                    System.out.println("Hostname Verification cert #1: ");
                }
            }
        } catch (Exception e) {
            serverException = e;
        }
        return true;
    }
    HandshakeCompletedEvent event;
    public void handshakeCompleted(HandshakeCompletedEvent theEvent) {
        event = theEvent;
    }
    void examineHandshakeCompletedEvent() throws Exception {
        dumpCerts("examineHandshakeCompletedEvent received",
            event.getPeerCertificates());
        dumpCerts("examineHandshakeCompletedEvent sent",
            event.getLocalCertificates());
    }
    synchronized void dumpCerts(String where, Certificate [] certs)
            throws Exception {
        System.out.println("");
        System.out.println(where + ":");
        if (certs == null) {
            throw new Exception("certs == null");
        }
        for (int i = 0; i< certs.length; i++) {
            if (certs[i] instanceof X509Certificate) {
                System.out.println("cert #1: " +
                    ((X509Certificate) certs[i]).getSubjectDN());
            }
        }
    }
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf;
        SSLServerSocket sslServerSocket;
        System.out.println("Starting Server...");
        sslssf =
            (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        System.out.println("Kicking off Client...");
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        sslSocket.setNeedClientAuth(true);
        sslSocket.addHandshakeCompletedListener(this);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        DataOutputStream out = new DataOutputStream(sslOS);
        System.out.println("Server reading request...");
        sslIS.read();
        System.out.println("Server replying...");
        try {
            out.writeBytes("HTTP/1.0 200 OK\r\n");
            out.writeBytes("Content-Length: " + 1 + "\r\n");
            out.writeBytes("Content-Type: text/html\r\n\r\n");
            out.write(57);
            out.flush();
        } catch (IOException ie) {
            serverException = ie;
        }
        System.out.println("Server getting certs...");
        SSLSession sslSession = sslSocket.getSession();
        dumpCerts("ServerSide sent", sslSession.getLocalCertificates());
        dumpCerts("ServerSide received", sslSession.getPeerCertificates());
        while (event == null) {
            Thread.sleep(1000);
        }
        System.out.println("Server examining Event...");
        examineHandshakeCompletedEvent();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        System.out.println("Starting Client...");
        String url = "https:
        System.out.println("connecting to: " + url);
        URL myURL = new URL(url);
        HttpsURLConnection myURLc;
        System.out.println("Client setting up URL/connecting...");
        myURLc = (HttpsURLConnection) myURL.openConnection();
        myURLc.setHostnameVerifier(this);
        myURLc.connect();
        InputStream sslIS = myURLc.getInputStream();
        System.out.println("Client reading...");
        sslIS.read();
        System.out.println("Client dumping certs...");
        dumpCerts("ClientSide received", myURLc.getServerCertificates());
        dumpCerts("ClientSide sent", myURLc.getLocalCertificates());
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
        new HttpsURLConnectionLocalCertificateChain();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    HttpsURLConnectionLocalCertificateChain () throws Exception {
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
