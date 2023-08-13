public class Test10 extends Test {
    public static void main (String[] args) throws Exception {
        System.out.print ("Test10: ");
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        int port = server.getAddress().getPort();
        HttpContext c2 = server.createContext ("/test", handler);
        ExecutorService exec = Executors.newCachedThreadPool();
        server.setExecutor (exec);
        try {
            server.start ();
            doClient(port);
            System.out.println ("OK");
        } finally {
            delay();
            if (server != null)
                server.stop(2);
            if (exec != null)
                exec.shutdown();
        }
    }
    static class Handler implements HttpHandler {
        volatile int invocation = 0;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            while (is.read() != -1);
            Headers map = t.getRequestHeaders();
            t.sendResponseHeaders (200, -1);
            t.close();
        }
    }
    public static void doClient (int port) throws Exception {
        String s = "GET /test/1.html HTTP/1.1\r\n\r\n";
        Socket socket = new Socket ("localhost", port);
        OutputStream os = socket.getOutputStream();
        os.write (s.getBytes());
        socket.setSoTimeout (10 * 1000);
        InputStream is = socket.getInputStream();
        int c;
        byte[] b = new byte [1024];
        while ((c=is.read(b)) != -1) ;
        is.close();
        socket.close();
    }
}
