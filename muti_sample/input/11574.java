public class TestLogging extends Test {
    public static void main (String[] args) throws Exception {
        HttpServer s1 = null;
        ExecutorService executor=null;
        try {
            System.out.print ("Test9: ");
            String root = System.getProperty ("test.src")+ "/docs";
            InetSocketAddress addr = new InetSocketAddress (0);
            Logger logger = Logger.getLogger ("com.sun.net.httpserver");
            logger.setLevel (Level.ALL);
            Handler h1 = new ConsoleHandler ();
            h1.setLevel (Level.ALL);
            logger.addHandler (h1);
            s1 = HttpServer.create (addr, 0);
            logger.info (root);
            HttpHandler h = new FileServerHandler (root);
            HttpContext c1 = s1.createContext ("/test1", h);
            executor = Executors.newCachedThreadPool();
            s1.setExecutor (executor);
            s1.start();
            int p1 = s1.getAddress().getPort();
            URL url = new URL ("http:
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            InputStream is = urlc.getInputStream();
            while (is.read() != -1) ;
            is.close();
            url = new URL ("http:
            urlc = (HttpURLConnection)url.openConnection();
            try {
                is = urlc.getInputStream();
                while (is.read() != -1) ;
                is.close();
            } catch (IOException e) {
                System.out.println ("caught expected exception");
            }
            Socket s = new Socket ("127.0.0.1", p1);
            OutputStream os = s.getOutputStream();
            os.write ("HELLO WORLD\r\n".getBytes());
            is = s.getInputStream();
            while (is.read() != -1) ;
            os.close(); is.close(); s.close();
            System.out.println ("OK");
        } finally {
            delay();
            if (s1 != null)
                s1.stop(2);
            if (executor != null)
                executor.shutdown();
        }
    }
}
