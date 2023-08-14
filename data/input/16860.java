public class UserAuth
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    public static void main(String[] args) {
        new UserAuth();
    }
    public UserAuth() {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    void doClient() {
        try {
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setRequestProperty("Authorization", "testString:ValueDoesNotMatter");
            int resp = uc.getResponseCode();
            System.out.println("Response Code is " + resp);
            if (resp != 200)
                throw new RuntimeException("Failed: Authorization header was not retained after redirect");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpServer.stop(1);
            executorService.shutdown();
        }
    }
    void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext ctx = httpServer.createContext("/redirect/", new RedirectHandler());
        HttpContext ctx1 = httpServer.createContext("/doStuff/", new HasAuthHandler());
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class RedirectHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InetSocketAddress address = httpServer.getAddress();
            String redirectUrl = "http:
            Headers resHeaders = t.getResponseHeaders();
            resHeaders.add("Location", redirectUrl);
            t.sendResponseHeaders(307, -1);
            t.close();
        }
    }
    class HasAuthHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            Headers reqHeaders = t.getRequestHeaders();
            List<String> auth = reqHeaders.get("Authorization");
            if (auth == null || !auth.get(0).equals("testString:ValueDoesNotMatter"))
                t.sendResponseHeaders(400, -1);
            t.sendResponseHeaders(200, -1);
            t.close();
        }
    }
}
