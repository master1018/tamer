public abstract class SSLContextSpi {
    public SSLContextSpi() {
        super();
    }
    protected abstract void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
            throws KeyManagementException;
    protected abstract SSLSocketFactory engineGetSocketFactory();
    protected abstract SSLServerSocketFactory engineGetServerSocketFactory();
    protected abstract SSLEngine engineCreateSSLEngine(String host, int port);
    protected abstract SSLEngine engineCreateSSLEngine();
    protected abstract SSLSessionContext engineGetServerSessionContext();
    protected abstract SSLSessionContext engineGetClientSessionContext();
}
