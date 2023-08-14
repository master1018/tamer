public class HttpsURLConnectionImpl extends HttpsURLConnection {
    private final HttpsEngine httpsEngine;
    private SSLSocket sslSocket;
    protected HttpsURLConnectionImpl(URL url, int port) {
        super(url);
        httpsEngine = new HttpsEngine(url, port);
    }
    protected HttpsURLConnectionImpl(URL url, int port, Proxy proxy) {
        super(url);
        httpsEngine = new HttpsEngine(url, port, proxy);
    }
    @Override
    public String getCipherSuite() {
        if (sslSocket == null) {
            throw new IllegalStateException(Messages.getString("luni.00")); 
        }
        return sslSocket.getSession().getCipherSuite();
    }
    @Override
    public Certificate[] getLocalCertificates() {
        if (sslSocket == null) {
            throw new IllegalStateException(Messages.getString("luni.00")); 
        }
        return sslSocket.getSession().getLocalCertificates();
    }
    @Override
    public Certificate[] getServerCertificates()
            throws SSLPeerUnverifiedException {
        if (sslSocket == null) {
            throw new IllegalStateException(Messages.getString("luni.00")); 
        }
        return sslSocket.getSession().getPeerCertificates();
    }
    @Override
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        if (sslSocket == null) {
            throw new IllegalStateException(Messages.getString("luni.00")); 
        }
        return sslSocket.getSession().getPeerPrincipal();
    }
    @Override
    public Principal getLocalPrincipal() {
        if (sslSocket == null) {
            throw new IllegalStateException(Messages.getString("luni.00")); 
        }
        return sslSocket.getSession().getLocalPrincipal();
    }
    @Override
    public void disconnect() {
        httpsEngine.disconnect();
    }
    @Override
    public InputStream getErrorStream() {
        return httpsEngine.getErrorStream();
    }
    @Override
    public String getRequestMethod() {
        return httpsEngine.getRequestMethod();
    }
    @Override
    public int getResponseCode() throws IOException {
        return httpsEngine.getResponseCode();
    }
    @Override
    public String getResponseMessage() throws IOException {
        return httpsEngine.getResponseMessage();
    }
    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        httpsEngine.setRequestMethod(method);
    }
    @Override
    public boolean usingProxy() {
        return httpsEngine.usingProxy();
    }
    @Override
    public boolean getInstanceFollowRedirects() {
        return httpsEngine.getInstanceFollowRedirects();
    }
    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        httpsEngine.setInstanceFollowRedirects(followRedirects);
    }
    @Override
    public void connect() throws IOException {
        httpsEngine.connect();
    }
    @Override
    public boolean getAllowUserInteraction() {
        return httpsEngine.getAllowUserInteraction();
    }
    @Override
    public Object getContent() throws IOException {
        return httpsEngine.getContent();
    }
    @SuppressWarnings("unchecked") 
    @Override
    public Object getContent(Class[] types) throws IOException {
        return httpsEngine.getContent(types);
    }
    @Override
    public String getContentEncoding() {
        return httpsEngine.getContentEncoding();
    }
    @Override
    public int getContentLength() {
        return httpsEngine.getContentLength();
    }
    @Override
    public String getContentType() {
        return httpsEngine.getContentType();
    }
    @Override
    public long getDate() {
        return httpsEngine.getDate();
    }
    @Override
    public boolean getDefaultUseCaches() {
        return httpsEngine.getDefaultUseCaches();
    }
    @Override
    public boolean getDoInput() {
        return httpsEngine.getDoInput();
    }
    @Override
    public boolean getDoOutput() {
        return httpsEngine.getDoOutput();
    }
    @Override
    public long getExpiration() {
        return httpsEngine.getExpiration();
    }
    @Override
    public String getHeaderField(int pos) {
        return httpsEngine.getHeaderField(pos);
    }
    @Override
    public Map<String, List<String>> getHeaderFields() {
        return httpsEngine.getHeaderFields();
    }
    @Override
    public Map<String, List<String>> getRequestProperties() {
        return httpsEngine.getRequestProperties();
    }
    @Override
    public void addRequestProperty(String field, String newValue) {
        httpsEngine.addRequestProperty(field, newValue);
    }
    @Override
    public String getHeaderField(String key) {
        return httpsEngine.getHeaderField(key);
    }
    @Override
    public long getHeaderFieldDate(String field, long defaultValue) {
        return httpsEngine.getHeaderFieldDate(field, defaultValue);
    }
    @Override
    public int getHeaderFieldInt(String field, int defaultValue) {
        return httpsEngine.getHeaderFieldInt(field, defaultValue);
    }
    @Override
    public String getHeaderFieldKey(int posn) {
        return httpsEngine.getHeaderFieldKey(posn);
    }
    @Override
    public long getIfModifiedSince() {
        return httpsEngine.getIfModifiedSince();
    }
    @Override
    public InputStream getInputStream() throws IOException {
        return httpsEngine.getInputStream();
    }
    @Override
    public long getLastModified() {
        return httpsEngine.getLastModified();
    }
    @Override
    public OutputStream getOutputStream() throws IOException {
        return httpsEngine.getOutputStream();
    }
    @Override
    public Permission getPermission() throws IOException {
        return httpsEngine.getPermission();
    }
    @Override
    public String getRequestProperty(String field) {
        return httpsEngine.getRequestProperty(field);
    }
    @Override
    public URL getURL() {
        return httpsEngine.getURL();
    }
    @Override
    public boolean getUseCaches() {
        return httpsEngine.getUseCaches();
    }
    @Override
    public void setAllowUserInteraction(boolean newValue) {
        httpsEngine.setAllowUserInteraction(newValue);
    }
    @Override
    public void setDefaultUseCaches(boolean newValue) {
        httpsEngine.setDefaultUseCaches(newValue);
    }
    @Override
    public void setDoInput(boolean newValue) {
        httpsEngine.setDoInput(newValue);
    }
    @Override
    public void setDoOutput(boolean newValue) {
        httpsEngine.setDoOutput(newValue);
    }
    @Override
    public void setIfModifiedSince(long newValue) {
        httpsEngine.setIfModifiedSince(newValue);
    }
    @Override
    public void setRequestProperty(String field, String newValue) {
        httpsEngine.setRequestProperty(field, newValue);
    }
    @Override
    public void setUseCaches(boolean newValue) {
        httpsEngine.setUseCaches(newValue);
    }
    @Override
    public void setConnectTimeout(int timeout) {
        httpsEngine.setConnectTimeout(timeout);
    }
    @Override
    public int getConnectTimeout() {
        return httpsEngine.getConnectTimeout();
    }
    @Override
    public void setReadTimeout(int timeout) {
        httpsEngine.setReadTimeout(timeout);
    }
    @Override
    public int getReadTimeout() {
        return httpsEngine.getReadTimeout();
    }
    @Override
    public String toString() {
        return httpsEngine.toString();
    }
    private class HttpsEngine extends HttpURLConnectionImpl {
        private boolean makingSSLTunnel;
        protected HttpsEngine(URL url, int port) {
            super(url, port);
        }
        protected HttpsEngine(URL url, int port, Proxy proxy) {
            super(url, port, proxy);
        }
        @Override
        public void connect() throws IOException {
            if (connected) {
                return;
            }
            if (super.usingProxy() && !makingSSLTunnel) {
                makingSSLTunnel = true;
                super.connect();
                String save_meth = method;
                method = "CONNECT"; 
                try {
                    doRequest();
                    endRequest();
                } finally {
                    method = save_meth;
                }
                if (!connected) {
                    throw new IOException(Messages.getString("luni.01", 
                            responseMessage, responseCode));
                }
                InputStream is = connection.getInputStream();
                while (is.available() != 0) {
                    is.read();
                }
                makingSSLTunnel = false;
            } else {
                super.connect();
            }
            if (!makingSSLTunnel) {
                sslSocket = connection.getSecureSocket(getSSLSocketFactory(), getHostnameVerifier());
                setUpTransportIO(connection);
            }
        }
        @Override
        protected String requestString() {
            if (super.usingProxy()) {
                if (makingSSLTunnel) {
                    int port = url.getPort();
                    return (port > 0) ? url.getHost() + ":" + port 
                    : url.getHost();
                }
                String file = url.getFile();
                if (file == null || file.length() == 0) {
                    file = "/"; 
                }
                return file;
            }
            return super.requestString();
        }
    }
}
