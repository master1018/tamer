public class B6526913 {
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
            int c ,count = 0;
            byte [] buf = new byte [32 * 1024];
            while (count < 32 * 1024) {
                count += is.read (buf);
            }
            is.close();
        } finally {
            server.stop(2);
            executor.shutdown();
        }
        if (error) {
            throw new RuntimeException ("Test failed");
        }
    }
    public static boolean error = false;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            try {
                while (is.read() != -1) ;
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                error = true;
            }
            t.sendResponseHeaders (200, 0);
            OutputStream os = t.getResponseBody();
            byte[] bb = new byte [32 * 1024];
            os.write (bb);
            os.flush();
            try {Thread.sleep (5000); } catch (InterruptedException e){}
            try {
                os.close();
            } catch (IOException e) {
                error = true;
                e.printStackTrace();
            }
            t.close();
        }
    }
}
