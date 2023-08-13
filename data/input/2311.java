public class ClientHelloRead {
    static String pathToStores = "/../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    volatile private static int serverPort = 0;
    static class TestServer extends OriginServer {
        public TestServer(ServerSocket ss) throws Exception {
            super(ss);
        }
        public byte[] getBytes() {
            return "SSL V2 Client Hello read was successful..".
                        getBytes();
        }
    }
    public static void main(String args[]) throws Exception
    {
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
        boolean useSSL = true;
        try {
            ServerSocketFactory ssf =
                ClientHelloRead.getServerSocketFactory(useSSL);
            ServerSocket ss = ssf.createServerSocket(serverPort);
            serverPort = ss.getLocalPort();
            new TestServer(ss);
        } catch (Exception e) {
            System.out.println("Server side failed:" +
                                e.getMessage());
            throw e;
        }
        try {
            doClientSide();
        } catch (Exception e) {
            System.out.println("Client side failed: " +
                                e.getMessage());
            throw e;
          }
    }
    private static ServerSocketFactory getServerSocketFactory
                   (boolean useSSL) throws Exception {
        if (useSSL) {
            SSLServerSocketFactory ssf = null;
            SSLContext ctx;
            KeyManagerFactory kmf;
            KeyStore ks;
            char[] passphrase = passwd.toCharArray();
            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(System.getProperty(
                        "javax.net.ssl.keyStore")), passphrase);
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), null, null);
            ssf = ctx.getServerSocketFactory();
            return ssf;
        } else {
            return ServerSocketFactory.getDefault();
        }
    }
    static void doClientSide() throws Exception {
        setupProxy();
        HttpsURLConnection.setDefaultHostnameVerifier(
                                      new NameVerifier());
        URL url = new URL("https:
                                + "/index.html");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                               url.openStream()));
            String inputLine;
            System.out.print("Client recieved from the server: ");
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            in.close();
        } catch (SSLException e) {
            if (in != null)
                in.close();
            throw e;
        }
    }
    static class NameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    static void setupProxy() throws IOException {
        ProxyTunnelServer pserver = new ProxyTunnelServer();
        pserver.start();
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", String.valueOf(
                                        pserver.getPort()));
    }
}
