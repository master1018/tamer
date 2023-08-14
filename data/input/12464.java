public class NTLMAuthentication extends AuthenticationInfo {
    private static final long serialVersionUID = 170L;
    private static final NTLMAuthenticationCallback NTLMAuthCallback =
        NTLMAuthenticationCallback.getNTLMAuthenticationCallback();
    private String hostname;
    private static String defaultDomain; 
    static {
        defaultDomain = java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("http.auth.ntlm.domain",
                                                      "domain"));
    };
    public static boolean supportsTransparentAuth () {
        return false;
    }
    public static boolean isTrustedSite(URL url) {
        return NTLMAuthCallback.isTrustedSite(url);
    }
    private void init0() {
        hostname = java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<String>() {
            public String run() {
                String localhost;
                try {
                    localhost = InetAddress.getLocalHost().getHostName().toUpperCase();
                } catch (UnknownHostException e) {
                     localhost = "localhost";
                }
                return localhost;
            }
        });
        int x = hostname.indexOf ('.');
        if (x != -1) {
            hostname = hostname.substring (0, x);
        }
    };
    PasswordAuthentication pw;
    Client client;
    public NTLMAuthentication(boolean isProxy, URL url, PasswordAuthentication pw) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
                AuthScheme.NTLM,
                url,
                "");
        init (pw);
    }
    private void init (PasswordAuthentication pw) {
        String username;
        String ntdomain;
        char[] password;
        this.pw = pw;
        String s = pw.getUserName();
        int i = s.indexOf ('\\');
        if (i == -1) {
            username = s;
            ntdomain = defaultDomain;
        } else {
            ntdomain = s.substring (0, i).toUpperCase();
            username = s.substring (i+1);
        }
        password = pw.getPassword();
        init0();
        try {
            client = new Client(System.getProperty("ntlm.version"), hostname,
                    username, ntdomain, password);
        } catch (NTLMException ne) {
            try {
                client = new Client(null, hostname, username, ntdomain, password);
            } catch (NTLMException ne2) {
                throw new AssertionError("Really?");
            }
        }
    }
    public NTLMAuthentication(boolean isProxy, String host, int port,
                                PasswordAuthentication pw) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
                AuthScheme.NTLM,
                host,
                port,
                "");
        init (pw);
    }
    @Override
    public boolean supportsPreemptiveAuthorization() {
        return false;
    }
    @Override
    public String getHeaderValue(URL url, String method) {
        throw new RuntimeException ("getHeaderValue not supported");
    }
    @Override
    public boolean isAuthorizationStale (String header) {
        return false; 
    }
    @Override
    public synchronized boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        try {
            String response;
            if (raw.length() < 6) { 
                response = buildType1Msg ();
            } else {
                String msg = raw.substring (5); 
                response = buildType3Msg (msg);
            }
            conn.setAuthenticationProperty(getHeaderName(), response);
            return true;
        } catch (IOException e) {
            return false;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
    private String buildType1Msg () {
        byte[] msg = client.type1();
        String result = "NTLM " + (new B64Encoder()).encode (msg);
        return result;
    }
    private String buildType3Msg (String challenge) throws GeneralSecurityException,
                                                           IOException  {
        byte[] type2 = (new sun.misc.BASE64Decoder()).decodeBuffer (challenge);
        byte[] nonce = new byte[8];
        new java.util.Random().nextBytes(nonce);
        byte[] msg = client.type3(type2, nonce);
        String result = "NTLM " + (new B64Encoder()).encode (msg);
        return result;
    }
}
class B64Encoder extends sun.misc.BASE64Encoder {
    protected int bytesPerLine () {
        return 1024;
    }
}
