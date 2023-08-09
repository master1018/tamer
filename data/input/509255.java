class FakeSession implements SSLSession {
    final String host;
    FakeSession(String host) {
        this.host = host;
    }
    public int getApplicationBufferSize() {
        throw new UnsupportedOperationException();
    }
    public String getCipherSuite() {
        throw new UnsupportedOperationException();
    }
    public long getCreationTime() {
        throw new UnsupportedOperationException();
    }
    public byte[] getId() {
        return host.getBytes();
    }
    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }
    public Certificate[] getLocalCertificates() {
        throw new UnsupportedOperationException();
    }
    public Principal getLocalPrincipal() {
        throw new UnsupportedOperationException();
    }
    public int getPacketBufferSize() {
        throw new UnsupportedOperationException();
    }
    public javax.security.cert.X509Certificate[] getPeerCertificateChain() {
        throw new UnsupportedOperationException();
    }
    public Certificate[] getPeerCertificates() {
        throw new UnsupportedOperationException();
    }
    public String getPeerHost() {
        return host;
    }
    public int getPeerPort() {
        return 443;
    }
    public Principal getPeerPrincipal() {
        throw new UnsupportedOperationException();
    }
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }
    public SSLSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }
    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }
    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }
    public void invalidate() {
        throw new UnsupportedOperationException();
    }
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }
    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }
}
