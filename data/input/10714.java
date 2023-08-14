public class B6393710 {
    static String CRLF = "\r\n";
    static String cmd =
        "POST /test/foo HTTP/1.1"+CRLF+
        "Content-Length: 22"+CRLF+
        "Pragma: no-cache"+CRLF+
        "Cache-Control: no-cache"+CRLF+ CRLF+
        "<item desc=\"excuse\" />"+
        "POST /test/foo HTTP/1.1"+CRLF+
        "Content-Length: 22"+CRLF+
        "Pragma: no-cache"+CRLF+
        "Authorization: Basic ZnJlZDpmcmVkcGFzc3dvcmQ="+CRLF+
        "Cache-Control: no-cache"+CRLF+ CRLF+
        "<item desc=\"excuse\" />";
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ctx.setAuthenticator (new BasicAuthenticator ("test") {
            public boolean checkCredentials (String user, String pass) {
                return user.equals ("fred") && pass.equals("fredpassword");
            }
        });
        server.start ();
        Socket s = new Socket ("localhost", server.getAddress().getPort());
        s.setSoTimeout (5000);
        OutputStream os = s.getOutputStream();
        os.write (cmd.getBytes());
        InputStream is = s.getInputStream ();
        try {
            ok = readAndCheck (is, "401 Unauthorized") &&
                 readAndCheck (is, "200 OK");
        } catch (SocketTimeoutException e) {
            System.out.println ("Did not received expected data");
            ok = false;
        } finally {
            s.close();
            server.stop(2);
        }
        if (requests != 1) {
            throw new RuntimeException ("server handler did not receive the request");
        }
        if (!ok) {
            throw new RuntimeException ("did not get 200 OK");
        }
        System.out.println ("OK");
    }
    static boolean readAndCheck (InputStream is, String expected) throws IOException {
        int c;
        int count = 0;
        int expLen = expected.length();
        expected = expected.toLowerCase();
        while ((c=is.read()) != -1) {
            c = Character.toLowerCase (c);
            if (c == expected.charAt (count)) {
                count ++;
                if (count == expLen) {
                    return true;
                }
            } else {
                count = 0;
            }
        }
        return false;
    }
    public static boolean ok = false;
    static int requests = 0;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            int count = 0;
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            while (is.read () != -1) {
                count ++;
            }
            if (count != 22) {
                System.out.println ("Handler expected 22. got " + count);
                ok = false;
            }
            is.close();
            t.sendResponseHeaders (200, -1);
            t.close();
            requests ++;
        }
    }
}
