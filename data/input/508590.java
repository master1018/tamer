public class TrustManagerFactory {
    private static final String SERVICE = "TrustManagerFactory";
    private static Engine engine = new Engine(SERVICE);
    private static final String PROPERTYNAME = "ssl.TrustManagerFactory.algorithm";
    public static final String getDefaultAlgorithm() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(PROPERTYNAME);
            }
        });
    }
    public static final TrustManagerFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new TrustManagerFactory((TrustManagerFactorySpi) engine.spi, engine.provider,
                    algorithm);
        }
    }
    public static final TrustManagerFactory getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null oe empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }
    public static final TrustManagerFactory getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new TrustManagerFactory((TrustManagerFactorySpi) engine.spi, provider, algorithm);
        }
    }
    private final Provider provider;
    private final TrustManagerFactorySpi spiImpl;
    private final String algorithm;
    protected TrustManagerFactory(TrustManagerFactorySpi factorySpi, Provider provider,
            String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = factorySpi;
    }
    public final String getAlgorithm() {
        return algorithm;
    }
    public final Provider getProvider() {
        return provider;
    }
    public final void init(KeyStore ks) throws KeyStoreException {
        spiImpl.engineInit(ks);
    }
    public final void init(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(spec);
    }
    public final TrustManager[] getTrustManagers() {
        return spiImpl.engineGetTrustManagers();
    }
}
