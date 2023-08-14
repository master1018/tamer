public class B6563259 {
    public static void main(String[] args) throws Exception {
        System.setProperty("http.proxyHost", "myproxy");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("http.nonProxyHosts", "host1.*");
        ProxySelector sel = ProxySelector.getDefault();
        java.util.List<Proxy> l = sel.select(new URI("http:
        if (l.get(0) != Proxy.NO_PROXY) {
            throw new RuntimeException("ProxySelector returned the wrong proxy");
        }
    }
}
