public class HttpNegotiateServer {
    final static String REALM_WEB = "WEB.DOMAIN";
    final static String REALM_PROXY = "PROXY.DOMAIN";
    final static String KRB5_CONF = "web.conf";
    final static String KRB5_TAB = "web.ktab";
    final static String WEB_USER = "web";
    final static char[] WEB_PASS = "webby".toCharArray();
    final static String PROXY_USER = "pro";
    final static char[] PROXY_PASS = "proxy".toCharArray();
    final static String WEB_HOST = "host.web.domain";
    final static String PROXY_HOST = "host.proxy.domain";
    final static String CONTENT = "Hello, World!";
    static int count = 0;
    static int webPort, proxyPort;
    static URL webUrl, proxyUrl;
    static class KnowAllAuthenticator extends java.net.Authenticator {
        public PasswordAuthentication getPasswordAuthentication () {
            if (!getRequestingScheme().equalsIgnoreCase("Negotiate")) {
                throw new RuntimeException("Bad scheme");
            }
            if (!getRequestingProtocol().equalsIgnoreCase("HTTP")) {
                throw new RuntimeException("Bad protocol");
            }
            if (getRequestorType() == RequestorType.SERVER) {
                if (!this.getRequestingHost().equalsIgnoreCase(webUrl.getHost())) {
                    throw new RuntimeException("Bad host");
                }
                if (this.getRequestingPort() != webUrl.getPort()) {
                    throw new RuntimeException("Bad port");
                }
                if (!this.getRequestingURL().equals(webUrl)) {
                    throw new RuntimeException("Bad url");
                }
                return new PasswordAuthentication(
                        WEB_USER+"@"+REALM_WEB, WEB_PASS);
            } else if (getRequestorType() == RequestorType.PROXY) {
                if (!this.getRequestingHost().equalsIgnoreCase(PROXY_HOST)) {
                    throw new RuntimeException("Bad host");
                }
                if (this.getRequestingPort() != proxyPort) {
                    throw new RuntimeException("Bad port");
                }
                if (!this.getRequestingURL().equals(proxyUrl)) {
                    throw new RuntimeException("Bad url");
                }
                return new PasswordAuthentication(
                        PROXY_USER+"@"+REALM_PROXY, PROXY_PASS);
            } else  {
                throw new RuntimeException("Bad requster type");
            }
        }
    }
    static class KnowNothingAuthenticator extends java.net.Authenticator {
        @Override
        public PasswordAuthentication getPasswordAuthentication () {
            HttpNegotiateServer.count++;
            return null;
        }
    }
    public static void main(String[] args)
            throws Exception {
        KDC kdcw = KDC.create(REALM_WEB);
        kdcw.addPrincipal(WEB_USER, WEB_PASS);
        kdcw.addPrincipalRandKey("krbtgt/" + REALM_WEB);
        kdcw.addPrincipalRandKey("HTTP/" + WEB_HOST);
        KDC kdcp = KDC.create(REALM_PROXY);
        kdcp.addPrincipal(PROXY_USER, PROXY_PASS);
        kdcp.addPrincipalRandKey("krbtgt/" + REALM_PROXY);
        kdcp.addPrincipalRandKey("HTTP/" + PROXY_HOST);
        KDC.saveConfig(KRB5_CONF, kdcw, kdcp,
                "default_keytab_name = " + KRB5_TAB,
                "[domain_realm]",
                "",
                ".web.domain="+REALM_WEB,
                ".proxy.domain="+REALM_PROXY);
        System.setProperty("java.security.krb5.conf", KRB5_CONF);
        Config.refresh();
        KDC.writeMultiKtab(KRB5_TAB, kdcw, kdcp);
        System.setProperty("java.security.auth.login.config", OneKDC.JAAS_CONF);
        File f = new File(OneKDC.JAAS_CONF);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write((
                "com.sun.security.jgss.krb5.initiate {\n" +
                "    com.sun.security.auth.module.Krb5LoginModule required;\n};\n"
                ).getBytes());
        fos.close();
        f.deleteOnExit();
        HttpServer h1 = httpd("Negotiate", false,
                "HTTP/" + WEB_HOST + "@" + REALM_WEB, KRB5_TAB);
        webPort = h1.getAddress().getPort();
        HttpServer h2 = httpd("Negotiate", true,
                "HTTP/" + PROXY_HOST + "@" + REALM_PROXY, KRB5_TAB);
        proxyPort = h2.getAddress().getPort();
        webUrl = new URL("http:
        proxyUrl = new URL("http:
        try {
            Exception e1 = null, e2 = null;
            try {
                test6578647();
            } catch (Exception e) {
                e1 = e;
                e.printStackTrace();
            }
            try {
                test6829283();
            } catch (Exception e) {
                e2 = e;
                e.printStackTrace();
            }
            if (e1 != null || e2 != null) {
                throw new RuntimeException("Test error");
            }
        } finally {
            if (h1 != null) h1.stop(0);
            if (h2 != null) h2.stop(0);
        }
    }
    static void test6578647() throws Exception {
        BufferedReader reader;
        java.net.Authenticator.setDefault(new KnowAllAuthenticator());
        reader = new BufferedReader(new InputStreamReader(
                webUrl.openConnection().getInputStream()));
        if (!reader.readLine().equals(CONTENT)) {
            throw new RuntimeException("Bad content");
        }
        reader = new BufferedReader(new InputStreamReader(
                proxyUrl.openConnection(
                new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(PROXY_HOST, proxyPort)))
                .getInputStream()));
        if (!reader.readLine().equals(CONTENT)) {
            throw new RuntimeException("Bad content");
        }
    }
    static void test6829283() throws Exception {
        BufferedReader reader;
        java.net.Authenticator.setDefault(new KnowNothingAuthenticator());
        try {
            new BufferedReader(new InputStreamReader(
                    webUrl.openConnection().getInputStream()));
        } catch (IOException ioe) {
        }
        if (count > 1) {
            throw new RuntimeException("Authenticator called twice");
        }
    }
    public static HttpServer httpd(String scheme, boolean proxy,
            String principal, String ktab) throws Exception {
        MyHttpHandler h = new MyHttpHandler();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext hc = server.createContext("/", h);
        hc.setAuthenticator(new MyServerAuthenticator(
                proxy, scheme, principal, ktab));
        server.start();
        return server;
    }
    static class MyHttpHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            t.sendResponseHeaders(200, 0);
            t.getResponseBody().write(CONTENT.getBytes());
            t.close();
        }
    }
    static class MyServerAuthenticator
            extends com.sun.net.httpserver.Authenticator {
        Subject s = new Subject();
        GSSManager m = null;
        GSSCredential cred = null;
        String scheme = null;
        String reqHdr = "WWW-Authenticate";
        String respHdr = "Authorization";
        int err = HttpURLConnection.HTTP_UNAUTHORIZED;
        public MyServerAuthenticator(boolean proxy, String scheme,
                String principal, String ktab) throws Exception {
            this.scheme = scheme;
            if (proxy) {
                reqHdr = "Proxy-Authenticate";
                respHdr = "Proxy-Authorization";
                err = HttpURLConnection.HTTP_PROXY_AUTH;
            }
            Krb5LoginModule krb5 = new Krb5LoginModule();
            Map<String, String> map = new HashMap<>();
            Map<String, Object> shared = new HashMap<>();
            map.put("storeKey", "true");
            map.put("isInitiator", "false");
            map.put("useKeyTab", "true");
            map.put("keyTab", ktab);
            map.put("principal", principal);
            krb5.initialize(s, null, shared, map);
            krb5.login();
            krb5.commit();
            m = GSSManager.getInstance();
            cred = Subject.doAs(s, new PrivilegedExceptionAction<GSSCredential>() {
                @Override
                public GSSCredential run() throws Exception {
                    System.err.println("Creating GSSCredential");
                    return m.createCredential(
                            null,
                            GSSCredential.INDEFINITE_LIFETIME,
                            MyServerAuthenticator.this.scheme.equalsIgnoreCase("Negotiate")?
                                    GSSUtil.GSS_SPNEGO_MECH_OID:
                                    GSSUtil.GSS_KRB5_MECH_OID,
                            GSSCredential.ACCEPT_ONLY);
                }
            });
        }
        @Override
        public Result authenticate(HttpExchange exch) {
            GSSContext c = null;
            String auth = exch.getRequestHeaders().getFirst(respHdr);
            try {
                c = (GSSContext)exch.getHttpContext().getAttributes().get("GSSContext");
                if (auth == null) {                 
                    Headers map = exch.getResponseHeaders();
                    map.set (reqHdr, scheme);        
                    c = Subject.doAs(s, new PrivilegedExceptionAction<GSSContext>() {
                        @Override
                        public GSSContext run() throws Exception {
                            return m.createContext(cred);
                        }
                    });
                    exch.getHttpContext().getAttributes().put("GSSContext", c);
                    return new com.sun.net.httpserver.Authenticator.Retry(err);
                } else {                            
                    byte[] token = new sun.misc.BASE64Decoder()
                            .decodeBuffer(auth.split(" ")[1]);
                    token = c.acceptSecContext(token, 0, token.length);
                    Headers map = exch.getResponseHeaders();
                    map.set (reqHdr, scheme + " " + new sun.misc.BASE64Encoder()
                            .encode(token).replaceAll("\\s", ""));
                    if (c.isEstablished()) {
                        return new com.sun.net.httpserver.Authenticator.Success(
                                new HttpPrincipal(c.getSrcName().toString(), ""));
                    } else {
                        return new com.sun.net.httpserver.Authenticator.Retry(err);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
