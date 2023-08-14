public class SSLContextImpl extends SSLContextSpi {
    private ClientSessionContext clientSessionContext;
    private ServerSessionContext serverSessionContext;
    protected SSLParameters sslParameters;
    public SSLContextImpl() {
        super();
    }
    @Override
    public void engineInit(KeyManager[] kms, TrustManager[] tms,
            SecureRandom sr) throws KeyManagementException {
        engineInit(kms, tms, sr, null, null);
    }
    public void engineInit(KeyManager[] kms, TrustManager[] tms,
            SecureRandom sr, SSLClientSessionCache clientCache,
            SSLServerSessionCache serverCache) throws KeyManagementException {
        sslParameters = new SSLParameters(kms, tms, sr,
                clientCache, serverCache);
        clientSessionContext = sslParameters.getClientSessionContext();
        serverSessionContext = sslParameters.getServerSessionContext();
    }
    public SSLSocketFactory engineGetSocketFactory() {
        if (sslParameters == null) {
            throw new IllegalStateException("SSLContext is not initiallized.");
        }
        return new OpenSSLSocketFactoryImpl(sslParameters);
    }
    @Override
    public SSLServerSocketFactory engineGetServerSocketFactory() {
        if (sslParameters == null) {
            throw new IllegalStateException("SSLContext is not initiallized.");
        }
        return new OpenSSLServerSocketFactoryImpl(sslParameters);
    }
    @Override
    public SSLEngine engineCreateSSLEngine(String host, int port) {
        if (sslParameters == null) {
            throw new IllegalStateException("SSLContext is not initiallized.");
        }
        return new SSLEngineImpl(host, port,
                (SSLParameters) sslParameters.clone());
    }
    @Override
    public SSLEngine engineCreateSSLEngine() {
        if (sslParameters == null) {
            throw new IllegalStateException("SSLContext is not initiallized.");
        }
        return new SSLEngineImpl((SSLParameters) sslParameters.clone());
    }
    @Override
    public ServerSessionContext engineGetServerSessionContext() {
        return serverSessionContext;
    }
    @Override
    public ClientSessionContext engineGetClientSessionContext() {
        return clientSessionContext;
    }
}