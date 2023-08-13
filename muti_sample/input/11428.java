public class B4769350 {
    static int count = 0;
    static boolean error = false;
    static void read (InputStream is) throws IOException {
        int c;
        while ((c=is.read()) != -1) {
        }
    }
    static class Client extends Thread {
        String authority, path;
        boolean allowerror;
        Client (String authority, String path, boolean allowerror) {
            super("Thread-" + path);
            this.authority = authority;
            this.path = path;
            this.allowerror = allowerror;
        }
        public void run () {
            try {
                URI u = new URI ("http", authority, path, null, null);
                URL url = u.toURL();
                URLConnection urlc = url.openConnection ();
                InputStream is = urlc.getInputStream ();
                read (is);
                is.close();
            } catch (URISyntaxException  e) {
                System.out.println (e);
                error = true;
            } catch (IOException e) {
                if (!allowerror) {
                    System.out.println (Thread.currentThread().getName() + " " + e);
                    e.printStackTrace();
                    error = true;
                }
            }
        }
    }
    static class CallBack extends AbstractCallback {
        void errorReply (HttpTransaction req, String reply) throws IOException {
            req.addResponseHeader ("Connection", "close");
            req.addResponseHeader ("WWW-Authenticate", reply);
            req.sendResponse (401, "Unauthorized");
            req.orderlyClose();
        }
        void proxyReply (HttpTransaction req, String reply) throws IOException {
            req.addResponseHeader ("Proxy-Authenticate", reply);
            req.sendResponse (407, "Proxy Authentication Required");
        }
        void okReply (HttpTransaction req) throws IOException {
            req.addResponseHeader ("Connection", "close");
            req.setResponseEntityBody ("Hello .");
            req.sendResponse (200, "Ok");
            req.orderlyClose();
        }
        public void request (HttpTransaction req, int count) {
            try {
                URI uri = req.getRequestURI();
                String path = uri.getPath();
                if (path.endsWith ("/t1a")) {
                    doT1a (req, count);
                } else if (path.endsWith ("/t1b")) {
                    doT1b (req, count);
                } else if (path.endsWith ("/t1c")) {
                    doT1c (req, count);
                } else if (path.endsWith ("/t1d")) {
                    doT1d (req, count);
                } else if (path.endsWith ("/t2a")) {
                    doT2a (req, count);
                } else if (path.endsWith ("/t2b")) {
                    doT2b (req, count);
                } else if (path.endsWith ("/t3a")) {
                    doT3a (req, count);
                } else if (path.endsWith ("/t3b")) {
                    doT3bc (req, count);
                } else if (path.endsWith ("/t3c")) {
                    doT3bc (req, count);
                } else {
                   System.out.println ("unexpected request URI");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        void doT1a (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                errorReply (req, "Basic realm=\"realm1\"");
                HttpServer.rendezvous ("one", 2);
                break;
            case 1:
                HttpServer.waitForCondition ("cond2");
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
        void doT1b (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                errorReply (req, "Basic realm=\"realm2\"");
                HttpServer.rendezvous ("one", 2);
                HttpServer.setCondition ("cond1");
                break;
            case 1:
                HttpServer.waitForCondition ("cond2");
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
        void doT1c (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                errorReply (req, "Basic realm=\"realm1\"");
                HttpServer.rendezvous ("two", 2);
                break;
            case 1:
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
        void doT1d (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                errorReply (req, "Basic realm=\"realm2\"");
                HttpServer.rendezvous ("two", 2);
                HttpServer.setCondition ("cond2");
                break;
            case 1:
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
        void doT2a (HttpTransaction req, int count) throws IOException {
            if (count == 1) {
                HttpServer.setCondition ("T2cond1");
            }
            errorReply (req, "Basic realm=\"realm3\"");
        }
        void doT2b (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                errorReply (req, "Basic realm=\"realm3\"");
                break;
            case 1:
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
        void doT3a (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                proxyReply (req, "Basic realm=\"proxy\"");
                HttpServer.setCondition ("T3cond1");
                break;
            case 1:
                errorReply (req, "Basic realm=\"realm4\"");
                break;
            case 2:
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
        void doT3bc (HttpTransaction req, int count) throws IOException {
            switch (count) {
            case 0:
                proxyReply (req, "Basic realm=\"proxy\"");
                break;
            case 1:
                okReply (req);
                break;
            default:
                System.out.println ("Unexpected request");
            }
        }
    };
    static HttpServer server;
    static MyAuthenticator auth = new MyAuthenticator ();
    static int redirects = 4;
    static Client c1,c2,c3,c4,c5,c6,c7,c8,c9;
    static void doServerTests (String authority) throws Exception {
        System.out.println ("Doing Server tests");
        System.out.println ("T1");
        c1 = new Client (authority, "/test/realm1/t1a", false);
        c2 = new Client (authority, "/test/realm2/t1b", false);
        c3 = new Client (authority, "/test/realm1/t1c", false);
        c4 = new Client (authority, "/test/realm2/t1d", false);
        c1.start(); c2.start();
        HttpServer.waitForCondition ("cond1");
        c3.start(); c4.start();
        c1.join(); c2.join(); c3.join(); c4.join();
        int f = auth.getCount();
        if (f != 2) {
            except ("Authenticator was called "+f+" times. Should be 2");
        }
        if (error) {
            except ("error occurred");
        }
        auth.resetCount();
        System.out.println ("T2");
        c5 = new Client (authority, "/test/realm3/t2a", true);
        c6 = new Client (authority, "/test/realm3/t2b", false);
        c5.start ();
        HttpServer.waitForCondition ("T2cond1");
        c6.start ();
        c5.join(); c6.join();
        f = auth.getCount();
        if (f != redirects+1) {
            except ("Authenticator was called "+f+" times. Should be: " + redirects+1);
        }
        if (error) {
            except ("error occurred");
        }
    }
    static void doProxyTests (String authority) throws Exception {
        System.out.println ("Doing Proxy tests");
        c7 = new Client (authority, "/test/realm4/t3a", false);
        c8 = new Client (authority, "/test/realm4/t3b", false);
        c9 = new Client (authority, "/test/realm4/t3c", false);
        c7.start ();
        HttpServer.waitForCondition ("T3cond1");
        c8.start ();
        c9.start ();
        c7.join(); c8.join(); c9.join();
        int f = auth.getCount();
        if (f != 2) {
            except ("Authenticator was called "+f+" times. Should be: " + 2);
        }
        if (error) {
            except ("error occurred");
        }
    }
    public static void main (String[] args) throws Exception {
        System.setProperty ("http.maxRedirects", Integer.toString (redirects));
        System.setProperty ("http.auth.serializeRequests", "true");
        Authenticator.setDefault (auth);
        boolean proxy = args[0].equals ("proxy");
        try {
            server = new HttpServer (new CallBack(), 10, 1, 0);
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            if (proxy) {
                System.setProperty ("http.proxyHost", "localhost");
                System.setProperty ("http.proxyPort",Integer.toString(server.getLocalPort()));
                doProxyTests ("www.foo.com");
            } else {
                doServerTests ("localhost:"+server.getLocalPort());
            }
            server.terminate();
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
    }
    static void pause (int millis) {
        try {
            Thread.sleep (millis);
        } catch (InterruptedException e) {}
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
            pw = new PasswordAuthentication ("user", "pass1".toCharArray());
            count ++;
            return pw;
        }
        public void resetCount () {
            count = 0;
        }
        public int getCount () {
            return (count);
        }
    }
}
