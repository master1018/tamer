public class AuthHeaderTest implements HttpCallback {
    static int count = 0;
    static String authstring;
    void errorReply (HttpTransaction req, String reply) throws IOException {
        req.addResponseHeader ("Connection", "close");
        req.addResponseHeader ("Www-authenticate", reply);
        req.sendResponse (401, "Unauthorized");
        req.orderlyClose();
    }
    void okReply (HttpTransaction req) throws IOException {
        req.setResponseEntityBody ("Hello .");
        req.sendResponse (200, "Ok");
        req.orderlyClose();
    }
    public void request (HttpTransaction req) {
        try {
            authstring = req.getRequestHeader ("Authorization");
            System.out.println (authstring);
            switch (count) {
            case 0:
                errorReply (req, "Basic realm=\"wallyworld\"");
                break;
            case 1:
                okReply (req);
                break;
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
            server = new HttpServer (new AuthHeaderTest(), 1, 10, 0);
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            client ("http:
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        int f = auth.getCount();
        if (f != 1) {
            except ("Authenticator was called "+f+" times. Should be 1");
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
        int count = 0;
        public PasswordAuthentication getPasswordAuthentication () {
            PasswordAuthentication pw;
            pw = new PasswordAuthentication ("user", "pass2".toCharArray());
            count ++;
            return pw;
        }
        public int getCount () {
            return (count);
        }
    }
}
