public class B6299712 {
    static SimpleHttpTransaction httpTrans;
    static HttpServer server;
    public static void main(String[] args) throws Exception {
        ResponseCache.setDefault(new DeployCacheHandler());
        startHttpServer();
        makeHttpCall();
    }
    public static void startHttpServer() {
        try {
            httpTrans = new SimpleHttpTransaction();
            server = new HttpServer(httpTrans, 1, 10, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void makeHttpCall() {
        try {
            System.out.println("http server listen on: " + server.getLocalPort());
            URL url = new URL("http" , InetAddress.getLocalHost().getHostAddress(),
                                server.getLocalPort(), "/");
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            System.out.println(uc.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.terminate();
        }
    }
}
class SimpleHttpTransaction implements HttpCallback {
    public void request(HttpTransaction trans) {
        try {
            String path = trans.getRequestURI().getPath();
            if (path.equals("/")) {
                String location = "/redirect";
                trans.addResponseHeader("Location", location);
                trans.sendResponse(302, "Moved Temporarily");
            } else {
                trans.sendResponse(200, "OK");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
class DeployCacheHandler extends java.net.ResponseCache {
    private boolean inCacheHandler = false;
    private boolean _downloading = false;
    public synchronized CacheResponse get(final URI uri, String rqstMethod,
            Map requestHeaders) throws IOException {
        System.out.println("get!!!: " + uri);
        try {
            if (!uri.toString().endsWith("redirect")) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DeployCacheResponse(new EmptyInputStream(), new HashMap());
    }
    public synchronized CacheRequest put(URI uri, URLConnection conn)
    throws IOException {
        URL url = uri.toURL();
        return new DeployCacheRequest(url, conn);
    }
}
class DeployCacheRequest extends java.net.CacheRequest {
    private URL _url;
    private URLConnection _conn;
    private boolean _downloading = false;
    DeployCacheRequest(URL url, URLConnection conn) {
        _url = url;
        _conn = conn;
    }
    public void abort() {
    }
    public OutputStream getBody() throws IOException {
        return null;
    }
}
class DeployCacheResponse extends java.net.CacheResponse {
    protected InputStream is;
    protected Map headers;
    DeployCacheResponse(InputStream is, Map headers) {
        this.is = is;
        this.headers = headers;
    }
    public InputStream getBody() throws IOException {
        return is;
    }
    public Map getHeaders() throws IOException {
        return headers;
    }
}
class EmptyInputStream extends InputStream {
    public EmptyInputStream() {
    }
    public int read()
    throws IOException {
        return -1;
    }
}
