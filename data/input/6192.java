public class HttpsCreateSockTest
{
    static String pathToStores = "../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    com.sun.net.httpserver.HttpsServer httpsServer;
    MyHandler httpHandler;
    public static void main(String[] args) {
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
        new HttpsCreateSockTest();
    }
    public HttpsCreateSockTest() {
        try {
            startHttpsServer();
            doClient();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
           httpsServer.stop(1);
        }
    }
    void doClient() throws IOException {
        InetSocketAddress address = httpsServer.getAddress();
        URL url = new URL("https:
        System.out.println("trying to connect to " + url + "...");
        HttpsURLConnection uc = (HttpsURLConnection) url.openConnection();
        uc.setHostnameVerifier(new AllHostnameVerifier());
        if (uc instanceof javax.net.ssl.HttpsURLConnection) {
            ((javax.net.ssl.HttpsURLConnection) uc).setSSLSocketFactory(new SimpleSSLSocketFactory());
            System.out.println("Using TestSocketFactory");
        }
        uc.connect();
        System.out.println("CONNECTED " + uc);
        System.out.println(uc.getResponseMessage());
        uc.disconnect();
    }
    public void startHttpsServer() throws IOException, NoSuchAlgorithmException  {
        httpsServer = com.sun.net.httpserver.HttpsServer.create(new InetSocketAddress(0), 0);
        httpsServer.createContext("/", new MyHandler());
        httpsServer.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
        httpsServer.start();
    }
    class MyHandler implements HttpHandler {
        private String message = "This is a message!";
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, message.length());
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(t.getResponseBody(), "ISO8859-1"));
            writer.write(message, 0, message.length());
            writer.close();
            t.close();
        }
    }
    class SimpleSSLSocketFactory extends SSLSocketFactory
    {
        boolean socketCreated = false;
        boolean socketWrapped = false;
        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            socketCreated = true;
            return SocketFactory.getDefault().createSocket(host, port);
        }
        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                                   int localPort) throws IOException {
            socketCreated = true;
            return SocketFactory.getDefault().createSocket(address, port, localAddress, localPort);
        }
        @Override
        public Socket createSocket(String host, int port) throws IOException {
            socketCreated = true;
            return SocketFactory.getDefault().createSocket(host, port);
        }
        @Override
        public Socket createSocket(String host, int port, InetAddress localHost,
                                   int localPort) throws IOException {
            socketCreated = true;
            return SocketFactory.getDefault().createSocket(host, port, localHost, localPort);
        }
        @Override
        public Socket createSocket(Socket s, String host, int port,
                                   boolean autoClose) throws IOException {
            socketWrapped = true;
            return ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket
                                                               (s, host, port, autoClose);
        }
        @Override
        public String[] getDefaultCipherSuites() {
            return ((SSLSocketFactory) SSLSocketFactory.getDefault()).getDefaultCipherSuites();
        }
        @Override
        public String[] getSupportedCipherSuites()  {
             return ((SSLSocketFactory) SSLSocketFactory.getDefault()).getSupportedCipherSuites();
        }
    }
    class AllHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
