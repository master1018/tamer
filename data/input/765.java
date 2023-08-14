public class Test8a extends Test {
    public static void main (String[] args) throws Exception {
        HttpsServer server = null;
        ExecutorService executor = null;
        try {
            Handler handler = new Handler();
            InetSocketAddress addr = new InetSocketAddress (0);
            server = HttpsServer.create (addr, 0);
            HttpContext ctx = server.createContext ("/test", handler);
            executor = Executors.newCachedThreadPool();
            SSLContext ssl = new SimpleSSLContext(System.getProperty("test.src")).get();
            server.setHttpsConfigurator(new HttpsConfigurator (ssl));
            server.setExecutor (executor);
            server.start ();
            URL url = new URL ("https:
            System.out.print ("Test8a: " );
            HttpsURLConnection urlc = (HttpsURLConnection)url.openConnection ();
            urlc.setDoOutput (true);
            urlc.setRequestMethod ("POST");
            urlc.setHostnameVerifier (new DummyVerifier());
            urlc.setSSLSocketFactory (ssl.getSocketFactory());
            OutputStream os = new BufferedOutputStream (urlc.getOutputStream(), 8000);
            for (int i=0; i<SIZE; i++) {
                os.write (i % 250);
            }
            os.close();
            int resp = urlc.getResponseCode();
            if (resp != 200) {
                throw new RuntimeException ("test failed response code");
            }
            InputStream is = urlc.getInputStream ();
            for (int i=0; i<SIZE; i++) {
                int f = is.read();
                if (f != (i % 250)) {
                    System.out.println ("Setting error(" +f +")("+i+")" );
                    error = true;
                    break;
                }
            }
            is.close();
        } finally {
            delay();
            if (server != null) server.stop(2);
            if (executor != null) executor.shutdown();
        }
        if (error) {
            throw new RuntimeException ("test failed error");
        }
        System.out.println ("OK");
    }
    public static boolean error = false;
    final static int SIZE = 9999;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
        System.out.println ("Handler.handle");
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            int c, count=0;
            while ((c=is.read ()) != -1) {
                if (c != (count % 250)) {
                System.out.println ("Setting error 1");
                    error = true;
                    break;
                }
                count ++;
            }
            if (count != SIZE) {
                System.out.println ("Setting error 2");
                error = true;
            }
            is.close();
            t.sendResponseHeaders (200, SIZE);
                System.out.println ("Sending 200 OK");
            OutputStream os = new BufferedOutputStream(t.getResponseBody(), 8000);
            for (int i=0; i<SIZE; i++) {
                os.write (i % 250);
            }
            os.close();
                System.out.println ("Finished");
        }
    }
}
