public class SSLContext {
    private static final String SERVICE = "SSLContext";
    private static Engine engine = new Engine(SERVICE);
    public static SSLContext getInstance(String protocol) throws NoSuchAlgorithmException {
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        synchronized (engine) {
            engine.getInstance(protocol, null);
            return new SSLContext((SSLContextSpi) engine.spi, engine.provider, protocol);
        }
    }
    public static SSLContext getInstance(String protocol, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (provider.length() == 0) {
            throw new IllegalArgumentException("Provider is empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(protocol, impProvider);
    }
    public static SSLContext getInstance(String protocol, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider is null");
        }
        if (protocol == null) {
            throw new NullPointerException("protocol is null");
        }
        synchronized (engine) {
            engine.getInstance(protocol, provider, null);
            return new SSLContext((SSLContextSpi) engine.spi, provider, protocol);
        }
    }
    private final Provider provider;
    private final SSLContextSpi spiImpl;
    private final String protocol;
    protected SSLContext(SSLContextSpi contextSpi, Provider provider, String protocol) {
        this.provider = provider;
        this.protocol = protocol;
        this.spiImpl = contextSpi;
    }
    public final String getProtocol() {
        return protocol;
    }
    public final Provider getProvider() {
        return provider;
    }
    public final void init(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
            throws KeyManagementException {
        spiImpl.engineInit(km, tm, sr);
    }
    public final SSLSocketFactory getSocketFactory() {
        return spiImpl.engineGetSocketFactory();
    }
    public final SSLServerSocketFactory getServerSocketFactory() {
        return spiImpl.engineGetServerSocketFactory();
    }
    public final SSLEngine createSSLEngine() {
        return spiImpl.engineCreateSSLEngine();
    }
    public final SSLEngine createSSLEngine(String peerHost, int peerPort) {
        return spiImpl.engineCreateSSLEngine(peerHost, peerPort);
    }
    public final SSLSessionContext getServerSessionContext() {
        return spiImpl.engineGetServerSessionContext();
    }
    public final SSLSessionContext getClientSessionContext() {
        return spiImpl.engineGetClientSessionContext();
    }
}
