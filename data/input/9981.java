public class B4921848 implements HttpCallback {
    static int count = 0;
    public void request (HttpTransaction req) {
        try {
            if (count == 0 ) {
                req.addResponseHeader ("Connection", "close");
                req.addResponseHeader ("WWW-Authenticate", "Basic realm=\"foo\"");
                req.addResponseHeader ("WWW-Authenticate", "Digest realm=\"bar\" domain=/biz nonce=\"hereisanonce\"");
                req.sendResponse (401, "Unauthorized");
                req.orderlyClose();
            } else {
                String authheader = req.getRequestHeader ("Authorization");
                if (authheader.startsWith ("Basic")) {
                    req.setResponseEntityBody ("Hello .");
                    req.sendResponse (200, "Ok");
                    req.orderlyClose();
                } else {
                    req.sendResponse (400, "Bad Request");
                    req.orderlyClose();
                }
            }
            count ++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void read (InputStream is) throws IOException {
        int c;
        System.out.println ("reading");
        while ((c=is.read()) != -1) {
            System.out.write (c);
        }
        System.out.println ("");
        System.out.println ("finished reading");
    }
    static void client (String u) throws Exception {
        URL url = new URL (u);
        System.out.println ("client opening connection to: " + u);
        URLConnection urlc = url.openConnection ();
        InputStream is = urlc.getInputStream ();
        read (is);
        is.close();
    }
    static HttpServer server;
    public static void main (String[] args) throws Exception {
        MyAuthenticator auth = new MyAuthenticator ();
        Authenticator.setDefault (auth);
        try {
            server = new HttpServer (new B4921848(), 1, 10, 0);
            System.out.println ("Server started: listening on port: " + server.getLocalPort());
            client ("http:
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        server.terminate();
    }
    public static void except (String s) {
        server.terminate();
        throw new RuntimeException (s);
    }
    static class MyAuthenticator extends Authenticator {
        MyAuthenticator () {
            super ();
        }
        public PasswordAuthentication getPasswordAuthentication () {
            return (new PasswordAuthentication ("user", "passwordNotCheckedAnyway".toCharArray()));
        }
    }
}
