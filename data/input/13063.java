public class B6886436 {
    public static void main (String[] args) throws Exception {
        Logger logger = Logger.getLogger ("com.sun.net.httpserver");
        ConsoleHandler c = new ConsoleHandler();
        c.setLevel (Level.WARNING);
        logger.addHandler (c);
        logger.setLevel (Level.WARNING);
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
            while (is.read()!= -1) ;
            is.close ();
            urlc = (HttpURLConnection)url.openConnection ();
            urlc.setReadTimeout (3000);
            is = urlc.getInputStream();
            while (is.read()!= -1);
            is.close ();
        } catch (IOException e) {
            server.stop(2);
            executor.shutdown();
            throw new RuntimeException ("Test failed");
        }
        server.stop(2);
        executor.shutdown();
        System.out.println ("OK");
    }
    public static boolean error = false;
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
            t.sendResponseHeaders (204, 0);
            t.close();
        }
    }
}
