public abstract class AbstractDelegateHttpsURLConnection extends
        HttpURLConnection {
    protected AbstractDelegateHttpsURLConnection(URL url,
            sun.net.www.protocol.http.Handler handler) throws IOException {
        this(url, null, handler);
    }
    protected AbstractDelegateHttpsURLConnection(URL url, Proxy p,
            sun.net.www.protocol.http.Handler handler) throws IOException {
        super(url, p, handler);
    }
    protected abstract javax.net.ssl.SSLSocketFactory getSSLSocketFactory();
    protected abstract javax.net.ssl.HostnameVerifier getHostnameVerifier();
    public void setNewClient (URL url)
        throws IOException {
        setNewClient (url, false);
    }
    public void setNewClient (URL url, boolean useCache)
        throws IOException {
        http = HttpsClient.New (getSSLSocketFactory(),
                                url,
                                getHostnameVerifier(),
                                useCache);
        ((HttpsClient)http).afterConnect();
    }
    public void setProxiedClient (URL url, String proxyHost, int proxyPort)
            throws IOException {
        setProxiedClient(url, proxyHost, proxyPort, false);
    }
    public void setProxiedClient (URL url, String proxyHost, int proxyPort,
            boolean useCache) throws IOException {
        proxiedConnect(url, proxyHost, proxyPort, useCache);
        if (!http.isCachedConnection()) {
            doTunneling();
        }
        ((HttpsClient)http).afterConnect();
    }
    protected void proxiedConnect(URL url, String proxyHost, int proxyPort,
            boolean useCache) throws IOException {
        if (connected)
            return;
        http = HttpsClient.New (getSSLSocketFactory(),
                                url,
                                getHostnameVerifier(),
                                proxyHost, proxyPort, useCache);
        connected = true;
    }
    public boolean isConnected() {
        return connected;
    }
    public void setConnected(boolean conn) {
        connected = conn;
    }
    public void connect() throws IOException {
        if (connected)
            return;
        plainConnect();
        if (cachedResponse != null) {
            return;
        }
        if (!http.isCachedConnection() && http.needsTunneling()) {
            doTunneling();
        }
        ((HttpsClient)http).afterConnect();
    }
    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout)
        throws IOException {
        return HttpsClient.New(getSSLSocketFactory(), url,
                               getHostnameVerifier(), p, true, connectTimeout);
    }
    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout,
                                          boolean useCache)
        throws IOException {
        return HttpsClient.New(getSSLSocketFactory(), url,
                               getHostnameVerifier(), p,
                               useCache, connectTimeout);
    }
    public String getCipherSuite () {
        if (cachedResponse != null) {
            return ((SecureCacheResponse)cachedResponse).getCipherSuite();
        }
        if (http == null) {
            throw new IllegalStateException("connection not yet open");
        } else {
           return ((HttpsClient)http).getCipherSuite ();
        }
    }
    public java.security.cert.Certificate[] getLocalCertificates() {
        if (cachedResponse != null) {
            List l = ((SecureCacheResponse)cachedResponse).getLocalCertificateChain();
            if (l == null) {
                return null;
            } else {
                return (java.security.cert.Certificate[])l.toArray();
            }
        }
        if (http == null) {
            throw new IllegalStateException("connection not yet open");
        } else {
            return (((HttpsClient)http).getLocalCertificates ());
        }
    }
    public java.security.cert.Certificate[] getServerCertificates()
            throws SSLPeerUnverifiedException {
        if (cachedResponse != null) {
            List l = ((SecureCacheResponse)cachedResponse).getServerCertificateChain();
            if (l == null) {
                return null;
            } else {
                return (java.security.cert.Certificate[])l.toArray();
            }
        }
        if (http == null) {
            throw new IllegalStateException("connection not yet open");
        } else {
            return (((HttpsClient)http).getServerCertificates ());
        }
    }
    public javax.security.cert.X509Certificate[] getServerCertificateChain()
            throws SSLPeerUnverifiedException {
        if (cachedResponse != null) {
            throw new UnsupportedOperationException("this method is not supported when using cache");
        }
        if (http == null) {
            throw new IllegalStateException("connection not yet open");
        } else {
            return ((HttpsClient)http).getServerCertificateChain ();
        }
    }
    Principal getPeerPrincipal()
            throws SSLPeerUnverifiedException
    {
        if (cachedResponse != null) {
            return ((SecureCacheResponse)cachedResponse).getPeerPrincipal();
        }
        if (http == null) {
            throw new IllegalStateException("connection not yet open");
        } else {
            return (((HttpsClient)http).getPeerPrincipal());
        }
    }
    Principal getLocalPrincipal()
    {
        if (cachedResponse != null) {
            return ((SecureCacheResponse)cachedResponse).getLocalPrincipal();
        }
        if (http == null) {
            throw new IllegalStateException("connection not yet open");
        } else {
            return (((HttpsClient)http).getLocalPrincipal());
        }
    }
}
