public final class X500PrivateCredential implements Destroyable {
    private X509Certificate cert;
    private PrivateKey key;
    private String alias;
    public X500PrivateCredential(X509Certificate cert, PrivateKey key) {
        if (cert == null || key == null )
            throw new IllegalArgumentException();
        this.cert = cert;
        this.key = key;
        this.alias=null;
    }
    public X500PrivateCredential(X509Certificate cert, PrivateKey key,
                                 String alias) {
        if (cert == null || key == null|| alias == null )
            throw new IllegalArgumentException();
        this.cert = cert;
        this.key = key;
        this.alias=alias;
    }
    public X509Certificate getCertificate() {
        return cert;
    }
    public PrivateKey getPrivateKey() {
        return key;
    }
    public String getAlias() {
        return alias;
    }
    public void destroy() {
        cert = null;
        key = null;
        alias =null;
    }
    public boolean isDestroyed() {
        return cert == null && key == null && alias==null;
    }
}
