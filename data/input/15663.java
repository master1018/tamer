public class Test11 {
    static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            read (t.getRequestBody());
            String response = "response";
            t.sendResponseHeaders (200, response.length());
            OutputStream os = t.getResponseBody();
            os.write (response.getBytes ("ISO8859_1"));
            t.close();
        }
        void read (InputStream is ) throws IOException {
            byte[] b = new byte [8096];
            while (is.read (b) != -1) {}
        }
    }
    public static void main (String[] args) throws Exception {
        System.out.print ("Test 11: ");
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        ExecutorService s = Executors.newCachedThreadPool();
        try {
            HttpContext ctx = server.createContext (
                "/foo/bar/", new Handler ()
            );
            s =  Executors.newCachedThreadPool();
            server.start ();
            URL url = new URL ("http:
                    "/Foo/bar/test.html");
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            int r = urlc.getResponseCode();
            if (r == 200) {
                throw new RuntimeException ("wrong response received");
            }
            System.out.println ("OK");
        } finally {
            s.shutdown();
            server.stop(2);
        }
    }
}
