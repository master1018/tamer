public class B4962064 implements HttpCallback {
    static int count = 0;
    public void request (HttpTransaction req) {
        try {
            switch (count) {
              case 0:
                req.addResponseHeader ("Connection", "close");
                req.addResponseHeader ("WWW-Authenticate", "Basic realm=\"foo\"");
                req.sendResponse (401, "Unauthorized");
                req.orderlyClose();
                break;
              case 1:
              case 3:
                req.setResponseEntityBody ("Hello .");
                req.sendResponse (200, "Ok");
                req.orderlyClose();
                break;
              case 2:
                req.addResponseHeader ("Connection", "close");
                req.addResponseHeader ("Proxy-Authenticate", "Basic realm=\"foo\"");
                req.sendResponse (407, "Proxy Authentication Required");
                req.orderlyClose();
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
    static URL urlsave;
    public static void main (String[] args) throws Exception {
        try {
            server = new HttpServer (new B4962064(), 1, 10, 0);
            int port = server.getLocalPort();
            System.setProperty ("http.proxyHost", "localhost");
            System.setProperty ("http.proxyPort", Integer.toString (port));
            MyAuthenticator auth = new MyAuthenticator ();
            Authenticator.setDefault (auth);
            System.out.println ("Server started: listening on port: " + port);
            String s = new String ("http:
            urlsave = new URL (s);
            client (s);
            s = new String ("http:
            urlsave = new URL (s);
            client (s);
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
        int count = 0;
        MyAuthenticator () {
            super ();
        }
        public PasswordAuthentication getPasswordAuthentication () {
            URL url = getRequestingURL ();
            if (!url.equals (urlsave)) {
                except ("urls not equal");
            }
            Authenticator.RequestorType expected;
            if (count == 0) {
                expected = Authenticator.RequestorType.SERVER;
            } else {
                expected = Authenticator.RequestorType.PROXY;
            }
            if (getRequestorType() != expected) {
                except ("wrong authtype");
            }
            count ++;
            return (new PasswordAuthentication ("user", "passwordNotCheckedAnyway".toCharArray()));
        }
    }
}
