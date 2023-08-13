public class B4933582 implements HttpCallback {
    static int count = 0;
    static String authstring;
    void errorReply (HttpTransaction req, String reply) throws IOException {
        req.addResponseHeader ("Connection", "close");
        req.addResponseHeader ("WWW-Authenticate", reply);
        req.sendResponse (401, "Unauthorized");
        req.orderlyClose();
    }
    void okReply (HttpTransaction req) throws IOException {
        req.setResponseEntityBody ("Hello .");
        req.sendResponse (200, "Ok");
        req.orderlyClose();
    }
    static boolean firstTime = true;
    public void request (HttpTransaction req) {
        try {
            authstring = req.getRequestHeader ("Authorization");
            if (firstTime) {
                switch (count) {
                case 0:
                    errorReply (req, "Basic realm=\"wallyworld\"");
                    break;
                case 1:
                    save (authstring);
                    okReply (req);
                    break;
                }
            } else {
                String savedauth = retrieve();
                if (savedauth.equals (authstring)) {
                    okReply (req);
                } else {
                    System.out.println ("savedauth = " + savedauth);
                    System.out.println ("authstring = " + authstring);
                    errorReply (req, "Basic realm=\"wallyworld\"");
                }
            }
            count ++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void save (String s) {
        try {
            FileOutputStream f = new FileOutputStream ("auth.save");
            ObjectOutputStream os = new ObjectOutputStream (f);
            os.writeObject (s);
        } catch (IOException e) {
            assert false;
        }
    }
    String retrieve () {
        String s = null;
        try {
            FileInputStream f = new FileInputStream ("auth.save");
            ObjectInputStream is = new ObjectInputStream (f);
            s = (String) is.readObject();
        } catch (Exception e) {
            assert false;
        }
        return s;
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
        firstTime = args[0].equals ("first");
        MyAuthenticator auth = new MyAuthenticator ();
        Authenticator.setDefault (auth);
        CacheImpl cache;
        try {
            if (firstTime) {
                server = new HttpServer (new B4933582(), 1, 10, 0);
                cache = new CacheImpl (server.getLocalPort());
            } else {
                cache = new CacheImpl ();
                server = new HttpServer(new B4933582(), 1, 10, cache.getPort());
            }
            AuthCacheValue.setAuthCache (cache);
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            client ("http:
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        int f = auth.getCount();
        if (firstTime && f != 1) {
            except ("Authenticator was called "+f+" times. Should be 1");
        }
        if (!firstTime && f != 0) {
            except ("Authenticator was called "+f+" times. Should be 0");
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
            pw = new PasswordAuthentication ("user", "pass1".toCharArray());
            count ++;
            return pw;
        }
        public int getCount () {
            return (count);
        }
    }
    static class CacheImpl extends AuthCacheImpl {
        HashMap map;
        int port; 
        CacheImpl () throws IOException {
            this (-1);
        }
        CacheImpl (int port) throws IOException {
            super();
            this.port = port;
            File src = new File ("cache.ser");
            if (src.exists()) {
                ObjectInputStream is = new ObjectInputStream (
                    new FileInputStream (src)
                );
                try {
                    map = (HashMap)is.readObject ();
                    this.port = (Integer)is.readObject ();
                    System.out.println ("read port from file " + port);
                } catch (ClassNotFoundException e) {
                    assert false;
                }
                is.close();
                System.out.println ("setMap from cache.ser");
            } else {
                map = new HashMap();
            }
            setMap (map);
        }
        int getPort () {
            return port;
        }
        private void writeMap () {
            try {
                File dst = new File ("cache.ser");
                dst.delete();
                if (!dst.createNewFile()) {
                    return;
                }
                ObjectOutputStream os = new ObjectOutputStream (
                        new FileOutputStream (dst)
                );
                os.writeObject(map);
                os.writeObject(port);
                System.out.println ("wrote port " + port);
                os.close();
            } catch (IOException e) {}
        }
        public void put (String pkey, AuthCacheValue value) {
            System.out.println ("put: " + pkey + " " + value);
            super.put (pkey, value);
            writeMap();
        }
        public AuthCacheValue get (String pkey, String skey) {
            System.out.println ("get: " + pkey + " " + skey);
            AuthCacheValue i = super.get (pkey, skey);
            System.out.println ("---> " + i);
            return i;
        }
        public void remove (String pkey, AuthCacheValue value) {
            System.out.println ("remove: " + pkey + " " + value);
            super.remove (pkey, value);
            writeMap();
        }
    }
}
