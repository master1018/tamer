public class UserCookie
{
    com.sun.net.httpserver.HttpServer httpServer;
    public static void main(String[] args) {
        new UserCookie();
    }
    public UserCookie() {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    void doClient() {
        try {
            CookieHandler.setDefault(new CookieManager());
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setRequestProperty("Cookie", "value=ValueDoesNotMatter");
            int resp = uc.getResponseCode();
            System.out.println("Response Code is " + resp);
            if (resp != 200)
                throw new RuntimeException("Failed: Cookie header was not retained");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpServer.stop(1);
        }
    }
    void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext ctx = httpServer.createContext("/test/", new MyHandler());
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            Headers reqHeaders = t.getRequestHeaders();
            List<String> cookie = reqHeaders.get("Cookie");
            if (cookie == null || !cookie.get(0).equals("value=ValueDoesNotMatter"))
                t.sendResponseHeaders(400, -1);
            t.sendResponseHeaders(200, -1);
            t.close();
        }
    }
}
