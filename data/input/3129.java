public class B6369510
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    public static void main(String[] args)
    {
        new B6369510();
    }
    public B6369510()
    {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    void doClient() {
        try {
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            int resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Response code from GET is not 200");
            System.out.println("Response code from GET = 200 OK");
            uc = (HttpURLConnection)url.openConnection();
            uc.setDoOutput(true);
            uc.setRequestMethod("POST");
            OutputStream os = uc.getOutputStream();
            resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Response code form POST is not 200");
            System.out.println("Response code from POST = 200 OK");
        } catch (IOException e) {
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
            Headers resHeaders = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            List<String> ct = reqHeaders.get("content-type");
            String requestMethod = t.getRequestMethod();
            if (requestMethod.equalsIgnoreCase("GET") && ct != null &&
                ct.get(0).equals("application/x-www-form-urlencoded"))
                t.sendResponseHeaders(400, -1);
            else if (requestMethod.equalsIgnoreCase("POST") && ct != null &&
                     !ct.get(0).equals("application/x-www-form-urlencoded"))
                t.sendResponseHeaders(400, -1);
            t.sendResponseHeaders(200, -1);
            t.close();
        }
    }
}
