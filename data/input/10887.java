public abstract class HttpsParameters {
    private String[] cipherSuites;
    private String[] protocols;
    private boolean wantClientAuth;
    private boolean needClientAuth;
    protected HttpsParameters() {}
    public abstract HttpsConfigurator getHttpsConfigurator();
    public abstract InetSocketAddress getClientAddress();
    public abstract void setSSLParameters (SSLParameters params);
    public String[] getCipherSuites() {
        return cipherSuites != null ? cipherSuites.clone() : null;
    }
    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = cipherSuites != null ? cipherSuites.clone() : null;
    }
    public String[] getProtocols() {
        return protocols != null ? protocols.clone() : null;
    }
    public void setProtocols(String[] protocols) {
        this.protocols = protocols != null ? protocols.clone() : null;
    }
    public boolean getWantClientAuth() {
        return wantClientAuth;
    }
    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }
    public boolean getNeedClientAuth() {
        return needClientAuth;
    }
    public void setNeedClientAuth(boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }
}
