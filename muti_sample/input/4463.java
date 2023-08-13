public class B6744329 {
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        URL url = new URL ("http:
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
        try {
            InputStream is = urlc.getInputStream();
            int c = 0;
            while (is.read()!= -1) {
                c ++;
            }
            System.out.println ("OK");
        } catch (IOException e) {
            System.out.println ("exception");
            error = true;
        }
        server.stop(2);
        executor.shutdown();
        if (error) {
            throw new RuntimeException ("Test failed");
        }
    }
    public static boolean error = false;
    final static int CHUNK_SIZE = 4096;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            t.sendResponseHeaders (200, 0);
            OutputStream os = t.getResponseBody();
            byte[] first = new byte [CHUNK_SIZE * 2];
            byte[] second = new byte [2];
            os.write (first);
            os.write ('x');
            os.write ('x');
            os.write ('x');
            os.write ('x');
            os.write ('x');
            t.close();
        }
    }
}
