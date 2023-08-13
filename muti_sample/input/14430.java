public class B4722333 implements HttpCallback {
    static int count = 0;
    static String [][] expected = {
        {"basic", "foo"},
        {"basic", "foobar"},
        {"digest", "biz"},
        {"digest", "bizbar"},
        {"digest", "foobiz"}
    };
    public void request (HttpTransaction req) {
        try {
            if (count % 2 == 1 ) {
                req.setResponseEntityBody ("Hello .");
                req.sendResponse (200, "Ok");
                req.orderlyClose();
            } else {
                switch (count) {
                  case 0:
                    req.addResponseHeader ("Connection", "close");
                    req.addResponseHeader ("WWW-Authenticate", "Basic realm=\"foo\"");
                    req.addResponseHeader ("WWW-Authenticate", "Foo realm=\"bar\"");
                    req.sendResponse (401, "Unauthorized");
                    req.orderlyClose();
                    break;
                  case 2:
                    req.addResponseHeader ("Connection", "close");
                    req.addResponseHeader ("WWW-Authenticate", "Basic realm=\"foobar\" Foo realm=\"bar\"");
                    req.sendResponse (401, "Unauthorized");
                    break;
                  case 4:
                    req.addResponseHeader ("Connection", "close");
                    req.addResponseHeader ("WWW-Authenticate", "Digest realm=biz domain=/foo nonce=thisisanonce ");
                    req.addResponseHeader ("WWW-Authenticate", "Basic realm=bizbar");
                    req.sendResponse (401, "Unauthorized");
                    req.orderlyClose();
                    break;
                  case 6:
                    req.addResponseHeader ("Connection", "close");
                    req.addResponseHeader ("WWW-Authenticate", "Digest realm=\"bizbar\" domain=/biz nonce=\"hereisanonce\" Basic realm=\"foobar\" Foo realm=\"bar\"");
                    req.sendResponse (401, "Unauthorized");
                    req.orderlyClose();
                    break;
                  case 8:
                    req.addResponseHeader ("Connection", "close");
                    req.addResponseHeader ("WWW-Authenticate", "Foo p1=1 p2=2 p3=3 p4=4 p5=5 p6=6 p7=7 p8=8 p9=10 Digest realm=foobiz domain=/foobiz nonce=newnonce");
                    req.addResponseHeader ("WWW-Authenticate", "Basic realm=bizbar");
                    req.sendResponse (401, "Unauthorized");
                    req.orderlyClose();
                    break;
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
            server = new HttpServer (new B4722333(), 1, 10, 0);
            System.out.println ("Server started: listening on port: " + server.getLocalPort());
            client ("http:
            client ("http:
            client ("http:
            client ("http:
            client ("http:
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        int f = auth.getCount();
        if (f != expected.length) {
            except ("Authenticator was called "+f+" times. Should be " + expected.length);
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
        public PasswordAuthentication getPasswordAuthentication ()
            {
            System.out.println ("Auth called");
            String scheme = getRequestingScheme();
            System.out.println ("getRequestingScheme() returns " + scheme);
            String prompt = getRequestingPrompt();
            System.out.println ("getRequestingPrompt() returns " + prompt);
            if (!scheme.equals (expected [count][0])) {
                B4722333.except ("wrong scheme received, " + scheme + " expected " + expected [count][0]);
            }
            if (!prompt.equals (expected [count][1])) {
                B4722333.except ("wrong realm received, " + prompt + " expected " + expected [count][1]);
            }
            count ++;
            return (new PasswordAuthentication ("user", "passwordNotCheckedAnyway".toCharArray()));
        }
        public int getCount () {
            return (count);
        }
    }
}
