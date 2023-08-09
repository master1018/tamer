public class B6341616 {
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        BasicAuthenticator filter = new BasicAuthenticator ("foobar@test.realm") {
            public boolean checkCredentials (String username, String pw) {
                throw new RuntimeException ("");
            }
        };
        ctx.setAuthenticator (filter);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        java.net.Authenticator.setDefault (new MyAuthenticator());
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
    public static boolean error = false;
    static class MyAuthenticator extends java.net.Authenticator {
        public PasswordAuthentication getPasswordAuthentication () {
            PasswordAuthentication pw;
            if (!getRequestingPrompt().equals ("foobar@test.realm")) {
                B6341616.error = true;
            }
            pw = new PasswordAuthentication ("fred", "xyz".toCharArray());
            return pw;
        }
    }
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
            t.sendResponseHeaders (200, -1);
            t.close();
        }
    }
}
