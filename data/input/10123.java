public class PostThruProxy {
    static String pathToStores = "../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    private static int serverPort = 0;
    static class TestServer extends OriginServer {
        public TestServer(ServerSocket ss) throws Exception {
            super(ss);
        }
        public byte[] getBytes() {
            return "Https POST thru proxy is successful".
                        getBytes();
        }
    }
    public static void main(String args[]) throws Exception
    {
        String keyFilename =
            args[1] + "/" + pathToStores +
                "/" + keyStoreFile;
        String trustFilename =
           args[1] + "/" + pathToStores +
                "/" + trustStoreFile;
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        boolean useSSL = true;
        try {
            ServerSocketFactory ssf =
                PostThruProxy.getServerSocketFactory(useSSL);
            ServerSocket ss = ssf.createServerSocket(serverPort);
            serverPort = ss.getLocalPort();
            new TestServer(ss);
        } catch (Exception e) {
            System.out.println("Server side failed:" +
                                e.getMessage());
            throw e;
        }
        try {
            doClientSide(args[0]);
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
    static String postMsg = "Testing HTTP post on a https server";
    static void doClientSide(String hostname) throws Exception {
        setupProxy();
        HttpsURLConnection.setDefaultHostnameVerifier(
                                      new NameVerifier());
        URL url = new URL("https:
        HttpsURLConnection https = (HttpsURLConnection)url.openConnection();
        https.setDoOutput(true);
        https.setRequestMethod("POST");
        PrintStream ps = null;
        try {
           ps = new PrintStream(https.getOutputStream());
           ps.println(postMsg);
           ps.flush();
           if (https.getResponseCode() != 200) {
                throw new RuntimeException("test Failed");
           }
           ps.close();
           BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                https.getInputStream()));
           String inputLine;
           while ((inputLine = in.readLine()) != null)
                System.out.println("Client received: " + inputLine);
           in.close();
        } catch (SSLException e) {
            if (ps != null)
                ps.close();
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
        pserver.needUserAuth(false);
        pserver.start();
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", String.valueOf(
                                        pserver.getPort()));
    }
}
