public class KeyManagerFactory {
    private static final String SERVICE = "KeyManagerFactory";
    private static Engine engine = new Engine(SERVICE);
    private static final String PROPERTY_NAME = "ssl.KeyManagerFactory.algorithm";
    public static final String getDefaultAlgorithm() {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(PROPERTY_NAME);
            }
        });
    }
    public static final KeyManagerFactory getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new KeyManagerFactory((KeyManagerFactorySpi) engine.spi, engine.provider,
                    algorithm);
        }
    }
    public static final KeyManagerFactory getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Provider is null or empty");
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }
    public static final KeyManagerFactory getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("Provider is null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm is null");
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new KeyManagerFactory((KeyManagerFactorySpi) engine.spi, provider, algorithm);
        }
    }
    private final Provider provider;
    private final KeyManagerFactorySpi spiImpl;
    private final String algorithm;
    protected KeyManagerFactory(KeyManagerFactorySpi factorySpi, Provider provider, String algorithm) {
        super();
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
    public final void init(KeyStore ks, char[] password) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        spiImpl.engineInit(ks, password);
    }
    public final void init(ManagerFactoryParameters spec) throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(spec);
    }
    public final KeyManager[] getKeyManagers() {
        return spiImpl.engineGetKeyManagers();
    }
}
