public class RelativeRedirect implements HttpCallback {
    static int count = 0;
    static HttpServer server;
    static class MyAuthenticator extends Authenticator {
        public MyAuthenticator () {
            super ();
        }
        public PasswordAuthentication getPasswordAuthentication ()
        {
            return (new PasswordAuthentication ("user", "Wrongpassword".toCharArray()));
        }
    }
    void firstReply (HttpTransaction req) throws IOException {
        req.addResponseHeader ("Connection", "close");
        req.addResponseHeader ("Location", "/redirect/file.html");
        req.sendResponse (302, "Moved Permamently");
        req.orderlyClose();
    }
    void secondReply (HttpTransaction req) throws IOException {
        if (req.getRequestURI().toString().equals("/redirect/file.html") &&
            req.getRequestHeader("Host").equals("localhost:"+server.getLocalPort())) {
            req.setResponseEntityBody ("Hello .");
            req.sendResponse (200, "Ok");
        } else {
            req.setResponseEntityBody (req.getRequestURI().toString());
            req.sendResponse (400, "Bad request");
        }
        req.orderlyClose();
    }
    public void request (HttpTransaction req) {
        try {
            switch (count) {
            case 0:
                firstReply (req);
                break;
            case 1:
                secondReply (req);
                break;
            }
            count ++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main (String[] args) throws Exception {
        MyAuthenticator auth = new MyAuthenticator ();
        Authenticator.setDefault (auth);
        try {
            server = new HttpServer (new RelativeRedirect(), 1, 10, 0);
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            URL url = new URL("http:
            System.out.println ("client opening connection to: " + url);
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
            InputStream is = urlc.getInputStream ();
            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (server != null) {
                server.terminate();
            }
        }
    }
}
