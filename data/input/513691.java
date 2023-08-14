public class AlgorithmParameterGenerator {
    private static final String SERVICE = "AlgorithmParameterGenerator"; 
    private static Engine engine = new Engine(SERVICE);
    private static SecureRandom randm = new SecureRandom();
    private final Provider provider;
    private final AlgorithmParameterGeneratorSpi spiImpl;
    private final String algorithm;
    protected AlgorithmParameterGenerator(
            AlgorithmParameterGeneratorSpi paramGenSpi, Provider provider,
            String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = paramGenSpi;
    }
    public final String getAlgorithm() {
        return algorithm;
    }
    public static AlgorithmParameterGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new AlgorithmParameterGenerator(
                    (AlgorithmParameterGeneratorSpi) engine.spi, engine.provider,
                    algorithm);
        }
    }
    public static AlgorithmParameterGenerator getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.02")); 
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, impProvider);
    }
    public static AlgorithmParameterGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new AlgorithmParameterGenerator(
                    (AlgorithmParameterGeneratorSpi) engine.spi, provider,
                    algorithm);
        }
    }
    public final Provider getProvider() {
        return provider;
    }
    public final void init(int size) {
        spiImpl.engineInit(size, randm);
    }
    public final void init(int size, SecureRandom random) {
        spiImpl.engineInit(size, random);
    }
    public final void init(AlgorithmParameterSpec genParamSpec)
            throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(genParamSpec, randm);
    }
    public final void init(AlgorithmParameterSpec genParamSpec,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        spiImpl.engineInit(genParamSpec, random);
    }
    public final AlgorithmParameters generateParameters() {
        return spiImpl.engineGenerateParameters();
    }
}
