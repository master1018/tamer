public class B6660405
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    static class MyCacheResponse extends CacheResponse {
        private byte[] buf = new byte[1024];
        public MyCacheResponse() {
        }
        @Override
        public Map<String, List<String>> getHeaders() throws IOException
        {
            Map<String, List<String>> h = new HashMap<String, List<String>>();
            ArrayList<String> l = new ArrayList<String>();
            l.add("HTTP/1.1 200 OK");
            h.put(null, l);
            l = new ArrayList<String>();
            l.add("1024");
            h.put("Content-Length", l);
            return h;
        }
        @Override
        public InputStream getBody() throws IOException
        {
            return new ByteArrayInputStream(buf);
        }
    }
    static class MyResponseCache extends ResponseCache {
        public MyResponseCache() {
        }
        @Override
        public CacheResponse get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders) throws IOException
        {
            if (uri.getPath().equals("/redirect/index.html")) {
                return new MyCacheResponse();
            }
            return null;
        }
        @Override
        public CacheRequest put(URI uri, URLConnection conn) throws IOException
        {
            return null;
        }
    }
    public static void main(String[] args)
    {
        new B6660405();
    }
    public B6660405()
    {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    void doClient() {
        ResponseCache.setDefault(new MyResponseCache());
        try {
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            int code = uc.getResponseCode();
            System.err.println("response code = " + code);
            int l = uc.getContentLength();
            System.err.println("content-length = " + l);
            InputStream in = uc.getInputStream();
            int i = 0;
            do {
                i = in.read();
            } while (i != -1);
            in.close();
        } catch (IOException e) {
            throw new RuntimeException("Got the wrong InputStream after checking headers");
        } finally {
            httpServer.stop(1);
            executorService.shutdown();
        }
    }
    public void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext ctx = httpServer.createContext("/test/", new MyHandler());
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            Headers reqHeaders = t.getRequestHeaders();
            Headers resHeaders = t.getResponseHeaders();
            int i = 0;
            do {
                i = is.read();
            } while (i != -1);
            is.close();
            resHeaders.add("Location", "http:
            t.sendResponseHeaders(302, -1);
            t.close();
        }
    }
}
