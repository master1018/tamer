public class CertPathBuilder {
    private static final String SERVICE = "CertPathBuilder"; 
    private static Engine engine = new Engine(SERVICE);
    private static final String PROPERTYNAME = "certpathbuilder.type"; 
    private static final String DEFAULTPROPERTY = "PKIX"; 
    private final Provider provider;
    private CertPathBuilderSpi spiImpl;
    private final String algorithm;
    protected CertPathBuilder(CertPathBuilderSpi builderSpi, Provider provider,
            String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = builderSpi;
    }
    public final String getAlgorithm() {
        return algorithm;
    }
    public final Provider getProvider() {
        return provider;
    }
    public static CertPathBuilder getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new CertPathBuilder((CertPathBuilderSpi) engine.spi,
                    engine.provider, algorithm);
        }
    }
    public static CertPathBuilder getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); 
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }
    public static CertPathBuilder getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new CertPathBuilder((CertPathBuilderSpi) engine.spi, provider,
                    algorithm);
        }
    }
    public final CertPathBuilderResult build(CertPathParameters params)
            throws CertPathBuilderException, InvalidAlgorithmParameterException {
        return spiImpl.engineBuild(params);
    }
    public static final String getDefaultType() {
        String defaultType = AccessController
                .doPrivileged(new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                });
        return (defaultType != null ? defaultType : DEFAULTPROPERTY);
    }
}
