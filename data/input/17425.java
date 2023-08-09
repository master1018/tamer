public class AsyncDisconnect implements Runnable
{
    com.sun.net.httpserver.HttpServer httpServer;
    MyHandler httpHandler;
    ExecutorService executorService;
    HttpURLConnection uc;
    public static void main(String[] args) {
        new AsyncDisconnect();
    }
    public AsyncDisconnect() {
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
            uc = (HttpURLConnection)url.openConnection();
            (new Thread(this)).start();
            uc.getInputStream();
            throw new RuntimeException("Failed: We Expect a SocketException to be thrown");
        } catch (SocketException se) {
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpServer.stop(1);
            executorService.shutdown();
        }
    }
    public void run() {
        try { Thread.sleep(2000); }
        catch (Exception e) {}
        uc.disconnect();
    }
    public void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        httpHandler = new MyHandler();
        HttpContext ctx = httpServer.createContext("/test/", httpHandler);
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            try { Thread.sleep(4000); }
            catch (Exception e) {}
            t.sendResponseHeaders(400, -1);
            t.close();
        }
    }
}
