public class StreamingOutputStream
{
    com.sun.net.httpserver.HttpServer httpServer;
    public static void main(String[] args) {
        new StreamingOutputStream();
    }
    public StreamingOutputStream() {
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
            uc.setDoOutput(true);
            uc.setFixedLengthStreamingMode(1);
            OutputStream os1 = uc.getOutputStream();
            OutputStream os2 = uc.getOutputStream();
            if (os1 != os2)
                throw new RuntimeException("Failed: OutputStreams should reference the same object");
            os2.write('b');
            os2.close();
            int resp = uc.getResponseCode();
            if (resp != 200)
                throw new RuntimeException("Failed: Server should return 200 OK");
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
            InputStream is = t.getRequestBody();
            while(is.read() != -1);
            is.close();
            t.sendResponseHeaders(200, -1);
            t.close();
        }
    }
}
