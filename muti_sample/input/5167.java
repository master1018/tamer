public class BasicLongCredentials {
    static final String USERNAME = "ThisIsMyReallyReallyReallyReallyReallyReally" +
                                   "LongFirstNameDotLastNameAtCompanyEmailAddress";
    static final String PASSWORD = "AndThisIsALongLongLongLongLongLongLongLongLong" +
                                   "LongLongLongLongLongLongLongLongLongPassword";
    static final String REALM = "foobar@test.realm";
    public static void main (String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        try {
            Handler handler = new Handler();
            HttpContext ctx = server.createContext("/test", handler);
            BasicAuthenticator a = new BasicAuthenticator(REALM) {
                public boolean checkCredentials (String username, String pw) {
                    return USERNAME.equals(username) && PASSWORD.equals(pw);
                }
            };
            ctx.setAuthenticator(a);
            server.start();
            Authenticator.setDefault(new MyAuthenticator());
            URL url = new URL("http:
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            InputStream is = urlc.getInputStream();
            int c = 0;
            while (is.read()!= -1) { c ++; }
            if (c != 0) { throw new RuntimeException("Test failed c = " + c); }
            if (error) { throw new RuntimeException("Test failed: error"); }
            System.out.println ("OK");
        } finally {
            server.stop(0);
        }
    }
    public static boolean error = false;
    static class MyAuthenticator extends java.net.Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication () {
            if (!getRequestingPrompt().equals(REALM)) {
                BasicLongCredentials.error = true;
            }
            return new PasswordAuthentication (USERNAME, PASSWORD.toCharArray());
        }
    }
    static class Handler implements HttpHandler {
        public void handle (HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            while (is.read () != -1) ;
            is.close();
            t.sendResponseHeaders(200, -1);
            HttpPrincipal p = t.getPrincipal();
            if (!p.getUsername().equals(USERNAME)) {
                error = true;
            }
            if (!p.getRealm().equals(REALM)) {
                error = true;
            }
            t.close();
        }
    }
}
