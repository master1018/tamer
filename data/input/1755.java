public class B6216082 {
    static SimpleHttpTransaction httpTrans;
    static HttpServer server;
    static TunnelProxy proxy;
    static InetAddress firstNonLoAddress = null;
    public static void main(String[] args) throws Exception {
        Class.forName("java.nio.channels.ClosedByInterruptException");
        setupEnv();
        startHttpServer();
        System.setProperty( "https.proxyPort", (new Integer(proxy.getLocalPort())).toString() );
        makeHttpCall();
        if (httpTrans.hasBadRequest) {
            throw new RuntimeException("Test failed : bad http request");
        }
    }
    static String pathToStores = "../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    public static void setupEnv() {
        try {
            firstNonLoAddress = getNonLoAddress();
            if (firstNonLoAddress == null) {
                System.out.println("The test needs at least one non-loopback address to run. Quit now.");
                System.exit(0);
            }
            System.out.println(firstNonLoAddress.getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.setProperty( "https.proxyHost", firstNonLoAddress.getHostAddress());
        String keyFilename = System.getProperty("test.src", "./") + "/" +
                             pathToStores + "/" + keyStoreFile;
        String trustFilename = System.getProperty("test.src", "./") + "/" +
                               pathToStores + "/" + trustStoreFile;
        System.setProperty("javax.net.ssl.keyStore", keyFilename);
        System.setProperty("javax.net.ssl.keyStorePassword", passwd);
        System.setProperty("javax.net.ssl.trustStore", trustFilename);
        System.setProperty("javax.net.ssl.trustStorePassword", passwd);
        HttpsURLConnection.setDefaultHostnameVerifier(new NameVerifier());
    }
    public static InetAddress getNonLoAddress() throws Exception {
        NetworkInterface loNIC = NetworkInterface.getByInetAddress(InetAddress.getByName("localhost"));
        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();
            if (!nic.getName().equalsIgnoreCase(loNIC.getName())) {
                Enumeration<InetAddress> addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (!addr.isLoopbackAddress())
                        return addr;
                }
            }
        }
        return null;
    }
    public static void startHttpServer() {
        try {
            httpTrans = new SimpleHttpTransaction();
            server = new HttpServer(httpTrans, 1, 10, 0);
            proxy = new TunnelProxy(1, 10, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void makeHttpCall() {
        try {
            System.out.println("https server listen on: " + server.getLocalPort());
            System.out.println("https proxy listen on: " + proxy.getLocalPort());
            URL url = new URL("https" , firstNonLoAddress.getHostAddress(),
                                server.getLocalPort(), "/");
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            System.out.println(uc.getResponseCode());
            uc.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            proxy.terminate();
            server.terminate();
        }
    }
    static class NameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
class SimpleHttpTransaction implements HttpCallback {
    public boolean hasBadRequest = false;
    public void request(HttpTransaction trans) {
        try {
            String path = trans.getRequestURI().getPath();
            if (path.equals("/")) {
                String location = "/redirect";
                trans.addResponseHeader("Location", location);
                trans.sendResponse(302, "Moved Temporarily");
            } else {
                String duplicatedGet = trans.getRequestHeader(null);
                if (duplicatedGet != null &&
                    duplicatedGet.toUpperCase().indexOf("GET") >= 0) {
                    trans.sendResponse(400, "Bad Request");
                    hasBadRequest = true;
                } else {
                    trans.sendResponse(200, "OK");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
