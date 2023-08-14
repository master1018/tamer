public class RetryPost
{
    static boolean shouldRetry = true;
    com.sun.net.httpserver.HttpServer httpServer;
    MyHandler httpHandler;
    ExecutorService executorService;
    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("noRetry"))
            shouldRetry = false;
        new RetryPost();
    }
    public RetryPost() {
        try {
            startHttpServer(shouldRetry);
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
            uc.setDoOutput(true);
            uc.setRequestMethod("POST");
            uc.getResponseCode();
            throw new RuntimeException("Failed: POST request being retried");
        } catch (SocketException se) {
            if (shouldRetry && httpHandler.getCallCount() != 2)
                throw new RuntimeException("Failed: Handler should have been called twice. " +
                                           "It was called "+ httpHandler.getCallCount() + " times");
            else if (!shouldRetry && httpHandler.getCallCount() != 1)
                throw new RuntimeException("Failed: Handler should have only been called once" +
                                           "It was called "+ httpHandler.getCallCount() + " times");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpServer.stop(1);
            executorService.shutdown();
        }
    }
    public void startHttpServer(boolean shouldRetry) throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        httpHandler = new MyHandler(shouldRetry);
        HttpContext ctx = httpServer.createContext("/test/", httpHandler);
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        int callCount = 0;
        boolean shouldRetry;
        public MyHandler(boolean shouldRetry) {
            this.shouldRetry = shouldRetry;
        }
        public void handle(HttpExchange t) throws IOException {
            callCount++;
            if (callCount > 1 && !shouldRetry) {
                t.sendResponseHeaders(400, -1);  
            } else {
                OutputStream os = t.getResponseBody();
                os.close();
            }
        }
        public int getCallCount() {
            return callCount;
        }
    }
}
