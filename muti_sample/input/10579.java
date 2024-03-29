public class B6518816
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    public static void main(String[] args)
    {
        new B6518816();
    }
    public B6518816()
    {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    final static int MAX_CONNS = 10;
    final static int TEN_MB = 10 * 1024 * 1024;
    static boolean stopped = false;
    void doClient() {
        try {
            InetSocketAddress address = httpServer.getAddress();
            List<HttpURLConnection> conns = new LinkedList<HttpURLConnection>();
            long totalmem1=0, totalmem2=0;
            System.gc();
            Runtime runtime = Runtime.getRuntime();
            totalmem1 = runtime.totalMemory() - runtime.freeMemory();
            System.out.println ("Sending " + (TEN_MB*MAX_CONNS/1024) + " KB");
            System.out.println ("At start: " + totalmem1/1024 + " KB");
            byte [] buf = new byte [TEN_MB];
            for (int j=0; j<MAX_CONNS; j++) {
                URL url = new URL("http:
                HttpURLConnection uc = (HttpURLConnection)url.openConnection();
                uc.setDoOutput (true);
                OutputStream os = uc.getOutputStream ();
                os.write (buf);
                InputStream is = uc.getInputStream();
                int resp = uc.getResponseCode();
                if (resp != 200) {
                    throw new RuntimeException("Failed: Part 1, Response code is not 200");
                }
                while  (is.read() != -1) ;
                is.close();
                conns.add (uc);
            }
            httpServer.stop(1);
            httpServer = null;
            executorService.shutdown();
            executorService = null;
            stopped = true;
            buf = null;
            System.runFinalization();
            try {Thread.sleep (1000); } catch (InterruptedException e){}
            System.gc();
            try {Thread.sleep (1000); } catch (InterruptedException e){}
            System.gc();
            totalmem2 = runtime.totalMemory() - runtime.freeMemory();
            System.out.println ("At end: " + totalmem2/1024 + " KB");
            long diff = (totalmem2 - totalmem1) ;;
            System.out.println ("Diff " + diff/1024 + " kbytes ");
            if (diff > (TEN_MB*MAX_CONNS*0.9)) {
                throw new RuntimeException ("Excessive memory retained");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException ("IOException");
        } finally {
            if (!stopped) {
                httpServer.stop(1);
                executorService.shutdown();
            }
        }
    }
    public void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext ctx = httpServer.createContext("/test/", new MyHandler());
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            Headers reqHeaders = t.getRequestHeaders();
            Headers resHeaders = t.getResponseHeaders();
            byte[] buf = new byte [16 * 1024];
            while (is.read (buf) != -1) ;
            t.sendResponseHeaders(200, -1);
            t.close();
        }
    }
}
