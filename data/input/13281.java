class HttpsURLConnection extends HttpURLConnection
{
    public HttpsURLConnection(URL url) throws IOException {
        super(url);
    }
    public abstract String getCipherSuite();
    public abstract X509Certificate [] getServerCertificateChain();
    private static HostnameVerifier defaultHostnameVerifier =
        new HostnameVerifier() {
            public boolean verify(String urlHostname, String certHostname) {
                return false;
            }
        };
    protected HostnameVerifier hostnameVerifier = defaultHostnameVerifier;
    public static void setDefaultHostnameVerifier(HostnameVerifier v) {
        if (v == null) {
            throw new IllegalArgumentException(
                "no default HostnameVerifier specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SSLPermission("setHostnameVerifier"));
        }
        defaultHostnameVerifier = v;
    }
    public static HostnameVerifier getDefaultHostnameVerifier() {
        return defaultHostnameVerifier;
    }
    public void setHostnameVerifier(HostnameVerifier v) {
        if (v == null) {
            throw new IllegalArgumentException(
                "no HostnameVerifier specified");
        }
        hostnameVerifier = v;
    }
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }
    private static SSLSocketFactory defaultSSLSocketFactory = null;
    private SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();
    public static void setDefaultSSLSocketFactory(SSLSocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException(
                "no default SSLSocketFactory specified");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSetFactory();
        }
        defaultSSLSocketFactory = sf;
    }
    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        if (defaultSSLSocketFactory == null) {
            defaultSSLSocketFactory =
                (SSLSocketFactory)SSLSocketFactory.getDefault();
        }
        return defaultSSLSocketFactory;
    }
    public void setSSLSocketFactory(SSLSocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException(
                "no SSLSocketFactory specified");
        }
        sslSocketFactory = sf;
    }
    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }
}
