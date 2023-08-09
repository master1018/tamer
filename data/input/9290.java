public class HeadTest {
    public static void main(String[] args) throws Exception {
        server();
    }
    static void server() throws Exception {
        InetSocketAddress inetAddress = new InetSocketAddress(0);
        HttpServer server = HttpServer.create(inetAddress, 5);
        try {
            server.setExecutor(Executors.newFixedThreadPool(5));
            HttpContext chunkedContext = server.createContext("/chunked");
            chunkedContext.setHandler(new HttpHandler() {
                @Override
                public void handle(HttpExchange msg) {
                    try {
                        try {
                            if (msg.getRequestMethod().equals("HEAD")) {
                                msg.getRequestBody().close();
                                msg.getResponseHeaders().add("Transfer-encoding", "chunked");
                                msg.sendResponseHeaders(200, -1);
                            }
                        } catch(IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } finally {
                        msg.close();
                    }
                }
            });
            HttpContext clContext = server.createContext("/content");
            clContext.setHandler(new HttpHandler() {
                @Override
                public void handle(HttpExchange msg) {
                    try {
                        try {
                            if (msg.getRequestMethod().equals("HEAD")) {
                                msg.getRequestBody().close();
                                msg.getResponseHeaders().add("Content-length", "1024");
                                msg.sendResponseHeaders(200, -1);
                            }
                        } catch(IOException ioe) {
                            ioe.printStackTrace();
                        }
                    } finally {
                        msg.close();
                    }
                }
            });
            server.start();
            String urlStr = "http:
            System.out.println("Server is at " + urlStr);
            for(int i=0; i < 10; i++) {
                runClient(urlStr + "chunked/");
            }
            for(int i=0; i < 10; i++) {
                runClient(urlStr + "content/");
            }
        } finally {
            ((ExecutorService)server.getExecutor()).shutdown();
            server.stop(0);
        }
    }
    static void runClient(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("HEAD");
        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("HEAD request doesn't return 200, but returns " + status);
        }
    }
}
