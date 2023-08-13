public class ProxyTest implements HttpCallback {
    static HttpServer server;
    public ProxyTest() {
    }
    public void request (HttpTransaction req) {
        req.setResponseEntityBody ("Hello .");
        try {
            req.sendResponse (200, "Ok");
            req.orderlyClose();
        } catch (IOException e) {
        }
    }
    static public class MyProxySelector extends ProxySelector {
        private ProxySelector def = null;
        private ArrayList<Proxy> noProxy;
        public MyProxySelector() {
            noProxy = new ArrayList<Proxy>(1);
            noProxy.add(Proxy.NO_PROXY);
        }
        public java.util.List<Proxy> select(URI uri) {
            return noProxy;
        }
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        }
    }
    public static void main(String[] args) {
        ProxySelector defSelector = ProxySelector.getDefault();
        if (defSelector == null)
            throw new RuntimeException("Default ProxySelector is null");
        ProxySelector.setDefault(new MyProxySelector());
        try {
            server = new HttpServer (new ProxyTest(), 1, 10, 0);
            URL url = new URL("http:
            System.out.println ("client opening connection to: " + url);
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
            InputStream is = urlc.getInputStream ();
            is.close();
        } catch (Exception e) {
                throw new RuntimeException(e);
        } finally {
            if (server != null) {
                server.terminate();
            }
        }
    }
}
