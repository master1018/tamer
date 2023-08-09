public class B6641309
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    public static void main(String[] args)
    {
        new B6641309();
    }
    public B6641309()
    {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    void doClient() {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        try {
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            CookieHandler ch = CookieHandler.getDefault();
            Map<String,List<String>> header = new HashMap<String,List<String>>();
            List<String> values = new LinkedList<String>();
            values.add("Test1Cookie=TEST1; path=/test/");
            values.add("Test2Cookie=TEST2; path=/test/");
            header.put("Set-Cookie", values);
            ch.put(url.toURI(), header);
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            int resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Response code from GET is not 200");
            System.out.println("Response code from GET = 200 OK");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
            int i = 0;
            do {
                i = is.read();
            } while (i != -1);
            is.close();
            List<String> cookies = reqHeaders.get("Cookie");
            if (cookies != null) {
                for (String str : cookies) {
                    if (str.equals("Test1Cookie=TEST1; Test2Cookie=TEST2"))
                        t.sendResponseHeaders(200, -1);
                }
            }
            t.sendResponseHeaders(400, -1);
            t.close();
        }
    }
}
