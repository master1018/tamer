public class B6339483 {
    public static void main (String[] args) throws Exception {
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test");
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
        } catch (IOException e) {
            server.stop(2);
            executor.shutdown();
            System.out.println ("OK");
        }
    }
}
