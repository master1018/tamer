public class TunnelThroughProxy {
    public static void main(String[] args) throws Exception {
        try {
            setupProxy();
            URL u = new URL("https:
            URLConnection uc = u.openConnection();
            InputStream is = uc.getInputStream();
            is.close();
        } catch (Exception e) {
            if (!e.getMessage().matches(".*HTTP\\/.*500.*")) {
                throw new RuntimeException(e);
            }
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
