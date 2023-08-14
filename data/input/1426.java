public class SystemProxies {
    static final String uriAuthority = "myMachine/";
    static final ProxySelector proxySel = ProxySelector.getDefault();
    public static void main(String[] args) {
        if (! "true".equals(System.getProperty("java.net.useSystemProxies"))) {
            System.out.println("Usage: java -Djava.net.useSystemProxies SystemProxies");
            return;
        }
        printProxies("http:
        printProxies("https:
        printProxies("ftp:
    }
    static void printProxies(String proto) {
        String uriStr =  proto + uriAuthority;
        try {
            List<Proxy> proxies = proxySel.select(new URI(uriStr));
            System.out.println("Proxies returned for " + uriStr);
            for (Proxy proxy : proxies)
                System.out.println("\t" + proxy);
        } catch (URISyntaxException e) {
            System.err.println(e);
        }
    }
}
