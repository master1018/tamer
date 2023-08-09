public class B6529200 {
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        Socket sock = new Socket ("localhost", server.getAddress().getPort());
        OutputStream os = sock.getOutputStream();
        System.out.println ("GET /test/foo HTTP/1.0\r\nConnection: keep-alive\r\n\r\n");
        os.write ("GET /test/foo HTTP/1.0\r\nConnection: keep-alive\r\n\r\n".getBytes());
        os.flush();
        InputStream is = sock.getInputStream();
        StringBuffer s = new StringBuffer();
        boolean finished = false;
        sock.setSoTimeout (10 * 1000);
        try {
            while (!finished) {
                char c = (char) is.read();
                s.append (c);
                finished = s.indexOf ("\r\n\r\nhello") != -1;
            }
        } catch (SocketTimeoutException e) {
            server.stop (2);
            executor.shutdown ();
            throw new RuntimeException ("Test failed in test1");
        }
        System.out.println (new String (s));
        System.out.println("GET /test/foo HTTP/1.0\r\nConnection: keep-alive\r\n\r\n");
        os.write ("GET /test/foo HTTP/1.0\r\nConnection: keep-alive\r\n\r\n".getBytes());
        os.flush();
        int i=0,c;
        byte [] buf = new byte [8*1024];
        try {
            while ((c=is.read()) != -1) {
                buf[i++] = (byte)c;
            }
        } catch (SocketTimeoutException e) {
            server.stop (2);
            executor.shutdown ();
            throw new RuntimeException ("Test failed in test2");
        }
        String ss = new String (buf, "ISO-8859-1");
        if (ss.indexOf ("\r\n\r\nhello world") == -1) {
            server.stop (2);
            executor.shutdown ();
            throw new RuntimeException ("Test failed in test2: wrong string");
        }
        System.out.println (ss);
        is.close ();
        server.stop (2);
        executor.shutdown();
    }
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is;
            OutputStream os;
            switch (invocation++) {
              case 1:
                is = t.getRequestBody();
                while (is.read() != -1) ;
                is.close();
                t.sendResponseHeaders (200, "hello".length());
                os = t.getResponseBody();
                os.write ("hello".getBytes());
                os.close();
                break;
              case 2:
                is = t.getRequestBody();
                while (is.read() != -1) ;
                is.close();
                t.sendResponseHeaders (200, 0);
                os = t.getResponseBody();
                os.write ("hello world".getBytes());
                os.close();
                break;
            }
        }
    }
}
