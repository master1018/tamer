public abstract class KeyPairGenerator extends KeyPairGeneratorSpi {
    private static final String SERVICE = "KeyPairGenerator"; 
    private static Engine engine = new Engine(SERVICE);
    private static SecureRandom random = new SecureRandom();
    private Provider provider;
    private String algorithm;
    protected KeyPairGenerator(String algorithm) {
        this.algorithm = algorithm;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public static KeyPairGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        KeyPairGenerator result;
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            if (engine.spi instanceof KeyPairGenerator) {
                result = (KeyPairGenerator) engine.spi;
                result.algorithm = algorithm;
                result.provider = engine.provider;
                return result;
            }
            result = new KeyPairGeneratorImpl((KeyPairGeneratorSpi) engine.spi,
                    engine.provider, algorithm);
            return result;
        }
    }
    public static KeyPairGenerator getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
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
    public static KeyPairGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        KeyPairGenerator result;
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            if (engine.spi instanceof KeyPairGenerator) {
                result = (KeyPairGenerator) engine.spi;
                result.algorithm = algorithm;
                result.provider = provider;
                return result;
            }
            result = new KeyPairGeneratorImpl((KeyPairGeneratorSpi) engine.spi,
                    provider, algorithm);
            return result;
        }
    }
    public final Provider getProvider() {
        return provider;
    }
    public void initialize(int keysize) {
        initialize(keysize, random);
    }
    public void initialize(AlgorithmParameterSpec param)
            throws InvalidAlgorithmParameterException {
        initialize(param, random);
    }
    public final KeyPair genKeyPair() {
        return generateKeyPair();
    }
    @Override
    public KeyPair generateKeyPair() {
        return null;
    }
    @Override
    public void initialize(int keysize, SecureRandom random) {
    }
    @Override
    public void initialize(AlgorithmParameterSpec param, SecureRandom random)
            throws InvalidAlgorithmParameterException {
    }
    private static class KeyPairGeneratorImpl extends KeyPairGenerator {
        private KeyPairGeneratorSpi spiImpl;
        private KeyPairGeneratorImpl(KeyPairGeneratorSpi keyPairGeneratorSpi,
                Provider provider, String algorithm) {
            super(algorithm);
            super.provider = provider;
            spiImpl = keyPairGeneratorSpi;
        }
        @Override
        public void initialize(int keysize, SecureRandom random) {
            spiImpl.initialize(keysize, random);
        }
        @Override
        public KeyPair generateKeyPair() {
            return spiImpl.generateKeyPair();
        }
        @Override
        public void initialize(AlgorithmParameterSpec param, SecureRandom random)
                throws InvalidAlgorithmParameterException {
            spiImpl.initialize(param, random);
        }
    }
}
