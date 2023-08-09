public class B6431193 {
    static boolean error = false;
    public static void read (InputStream i) throws IOException {
        while (i.read() != -1);
        i.close();
    }
    public static void main(String[] args) {
        class MyHandler implements HttpHandler {
            public void handle(HttpExchange t) throws IOException {
                InputStream is = t.getRequestBody();
                read(is);
                    String response = "This is the response";
                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                error = Thread.currentThread().isDaemon();
            }
        }
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(0), 10);
            server.createContext("/apps", new MyHandler());
            server.setExecutor(null);
                server.start();
            int port = server.getAddress().getPort();
            String s = "http:
            URL url = new URL (s);
            InputStream is = url.openStream();
            read (is);
            server.stop (1);
            if (error) {
                throw new RuntimeException ("error in test");
            }
        }
        catch (IOException e) {
                e.printStackTrace();
        }
    }
}
