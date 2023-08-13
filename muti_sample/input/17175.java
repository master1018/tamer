public class B6737819 {
    private static String[] uris = {
        "http:
        "http:
        "http:
        "http:
    };
    public static void main(String[] args) throws Exception {
        System.setProperty("http.proxyHost", "myproxy");
        System.setProperty("http.proxyPort", "8080");
        ProxySelector sel = ProxySelector.getDefault();
        java.util.List<Proxy> l;
        for (String s : uris) {
            l = sel.select(new URI(s));
            if (l.size() == 1 && l.get(0).type() != Proxy.Type.DIRECT) {
                throw new RuntimeException("ProxySelector returned the wrong proxy for " + s);
            }
        }
        System.setProperty("http.nonProxyHosts", "");
        for (String s : uris) {
            l = sel.select(new URI(s));
            if (l.size() == 1 && l.get(0).type() != Proxy.Type.HTTP) {
                throw new RuntimeException("ProxySelector returned the wrong proxy for " + s);
            }
        }
    }
}
