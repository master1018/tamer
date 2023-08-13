public abstract class SecureCacheResponse extends CacheResponse {
    public abstract String getCipherSuite();
    public abstract List<Certificate> getLocalCertificateChain();
    public abstract List<Certificate> getServerCertificateChain()
        throws SSLPeerUnverifiedException;
     public abstract Principal getPeerPrincipal()
             throws SSLPeerUnverifiedException;
     public abstract Principal getLocalPrincipal();
}
