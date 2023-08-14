public class B6433018 {
    static String CRLF = "\r\n";
    static String cmd =
        "POST /test/item HTTP/1.1"+CRLF+
        "Keep-Alive: 300"+CRLF+
        "Proxy-Connection: keep-alive"+CRLF+
        "Content-Type: text/xml"+CRLF+
        "Content-Length: 22"+CRLF+
        "Pragma: no-cache"+CRLF+
        "Cache-Control: no-cache"+CRLF+ CRLF+
        "<item desc=\"excuse\" />"+CRLF+
        "GET /test/items HTTP/1.1"+CRLF+
        "Host: araku:9999"+CRLF+
        "Accept-Language: en-us,en;q=0.5"+CRLF+
        "Accept-Encoding: gzip,deflate"+CRLF+
        "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7"+CRLF+
        "Keep-Alive: 300"+CRLF+
        "Proxy-Connection: keep-alive"+CRLF+
        "Pragma: no-cache"+CRLF+
        "Cache-Control: no-cache"+CRLF+CRLF;
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        server.start ();
        Socket s = new Socket ("localhost", server.getAddress().getPort());
        try {
            OutputStream os = s.getOutputStream();
            os.write (cmd.getBytes());
            Thread.sleep (3000);
            s.close();
        } catch (IOException e) { }
        server.stop(2);
        if (requests != 2) {
            throw new RuntimeException ("did not receive the 2 requests");
        }
        System.out.println ("OK");
    }
    public static boolean error = false;
    static int requests = 0;
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
            requests ++;
        }
    }
}
