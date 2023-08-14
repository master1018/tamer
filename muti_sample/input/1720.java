public class B6421581 {
    static boolean error = false;
    static int iter = 0;
    public static void main(String[] args) throws Exception {
                once();
    }
    public static void once() throws Exception {
        InetSocketAddress inetAddress = new InetSocketAddress(
            "localhost", 0);
        HttpServer server = HttpServer.create(inetAddress, 5);
        int port = server.getAddress().getPort();
        ExecutorService e = (Executors.newFixedThreadPool(5));
        server.setExecutor(e);
        HttpContext context = server.createContext("/hello");
        server.start();
        context.setHandler(new HttpHandler() {
            public void handle(HttpExchange msg) {
                iter ++;
                System.out.println("Got request");
                switch (iter) {
                case 1:
                    try {
                        msg.sendResponseHeaders(200, 0);
                        OutputStream out = msg.getResponseBody();
                        out.write("hello".getBytes());
                        out.close();
                    } catch(Exception e) {
                        error = true;
                    } finally {
                        msg.close();
                    }
                    break;
                case 2:
                    try {
                        msg.sendResponseHeaders(200, 5);
                        OutputStream out = msg.getResponseBody();
                        out.write("hello".getBytes());
                        out.close();
                    } catch(Exception e) {
                        error = true;
                    } finally {
                        msg.close();
                    }
                    break;
                case 3:
                    try {
                        msg.sendResponseHeaders(200, -1);
                        msg.close();
                    } catch(Exception e) {
                        error = true;
                    }
                    break;
                }
            }
        });
        URL url = new URL("http:
        doURL(url);
        doURL(url);
        doURL(url);
        e.shutdown();
        e.awaitTermination(4, TimeUnit.SECONDS);
        server.stop(0);
        if (error) {
            throw new RuntimeException ("test failed");
        }
    }
    static void doURL (URL url) throws Exception {
        InputStream is = url.openStream();
        while (is.read() != -1) ;
        is.close();
    }
}
