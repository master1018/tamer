public class B5017051
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    public static void main(String[] args)
    {
        new B5017051();
    }
    public B5017051()
    {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    void doClient() {
        java.net.Authenticator.setDefault(new MyAuthenticator());
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        try {
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            int resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Part 1, Response code is not 200");
            System.out.println("Response code from Part 1 = 200 OK");
            URL url2 = new URL("http:
            CookieHandler ch = CookieHandler.getDefault();
            Map<String,List<String>> header = new HashMap<String,List<String>>();
            List<String> values = new LinkedList<String>();
            values.add("Test2Cookie=\"TEST2\"; path=\"/test2/\"");
            header.put("Set-Cookie2", values);
            ch.put(url2.toURI(), header);
            uc = (HttpURLConnection)url2.openConnection();
            resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Part 2, Response code is not 200");
            System.out.println("Response code from Part 2 = 200 OK");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException ue) {
            ue.printStackTrace();
        } finally {
            httpServer.stop(1);
            executorService.shutdown();
        }
    }
    public void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext ctx = httpServer.createContext("/test/", new MyHandler());
        ctx.setAuthenticator( new MyBasicAuthenticator("foo"));
        ctx.getFilters().add(0, new CookieFilter());
        HttpContext ctx2 = httpServer.createContext("/test2/", new MyHandler2());
        ctx2.setAuthenticator( new MyBasicAuthenticator("foobar"));
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            Headers reqHeaders = t.getRequestHeaders();
            Headers resHeaders = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            if (!reqHeaders.containsKey("Authorization"))
                t.sendResponseHeaders(400, -1);
            List<String> cookies = reqHeaders.get("Cookie");
            if (cookies != null) {
                for (String str : cookies) {
                    if (str.equals("Customer=WILE_E_COYOTE"))
                        t.sendResponseHeaders(200, -1);
                }
            }
            t.sendResponseHeaders(400, -1);
        }
    }
    class MyHandler2 implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            Headers reqHeaders = t.getRequestHeaders();
            Headers resHeaders = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            if (!reqHeaders.containsKey("Authorization"))
                t.sendResponseHeaders(400, -1);
            List<String> cookies = reqHeaders.get("Cookie");
            if (cookies != null && (cookies.size() == 1)) {
                t.sendResponseHeaders(200, -1);
            }
            t.sendResponseHeaders(400, -1);
        }
    }
    class MyAuthenticator extends java.net.Authenticator {
        public PasswordAuthentication getPasswordAuthentication () {
            return new PasswordAuthentication("tester", "passwd".toCharArray());
        }
    }
    class MyBasicAuthenticator extends BasicAuthenticator
    {
        public MyBasicAuthenticator(String realm) {
            super(realm);
        }
        public boolean checkCredentials (String username, String password) {
            return username.equals("tester") && password.equals("passwd");
        }
    }
    class CookieFilter extends Filter
    {
        public void doFilter(HttpExchange t, Chain chain) throws IOException
        {
            Headers resHeaders = t.getResponseHeaders();
            Headers reqHeaders = t.getRequestHeaders();
            if (!reqHeaders.containsKey("Authorization"))
                resHeaders.set("Set-Cookie2", "Customer=\"WILE_E_COYOTE\"; path=\"/test/\"");
            chain.doFilter(t);
        }
        public void destroy(HttpContext c) { }
        public void init(HttpContext c) { }
        public String description() {
            return new String("Filter for setting a cookie for requests without an \"Authorization\" header.");
        }
    }
}
