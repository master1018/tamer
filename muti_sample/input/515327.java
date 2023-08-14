public class KeyFactory {
    private static final String SERVICE = "KeyFactory"; 
    private Provider provider;
    static private Engine engine = new Engine(SERVICE);
    private KeyFactorySpi spiImpl; 
    private String algorithm;
    protected KeyFactory(KeyFactorySpi keyFacSpi, 
                         Provider provider,
                         String algorithm) {
        this.provider = provider;
        this. algorithm = algorithm;
        this.spiImpl = keyFacSpi;
    }
    public static KeyFactory getInstance(String algorithm)
                                throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new KeyFactory((KeyFactorySpi)engine.spi, engine.provider, algorithm);
        }
    }
    @SuppressWarnings("nls")
    public static KeyFactory getInstance(String algorithm, String provider)
                                throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02"));
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(Messages.getString("security.03", provider));
        }
        return getInstance(algorithm, p);    
    }
    public static KeyFactory getInstance(String algorithm, Provider provider)
                                 throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new KeyFactory((KeyFactorySpi)engine.spi, provider, algorithm);
        }
    }
    public final Provider getProvider() {
        return provider;
    }
    public final String getAlgorithm() {
        return algorithm;
    }
    public final PublicKey generatePublic(KeySpec keySpec)
                                throws InvalidKeySpecException {
        return spiImpl.engineGeneratePublic(keySpec);
    }
    public final PrivateKey generatePrivate(KeySpec keySpec)
                                throws InvalidKeySpecException {
        return spiImpl.engineGeneratePrivate(keySpec);
    }
    public final <T extends KeySpec> T getKeySpec(Key key,
                                    Class<T> keySpec)
                            throws InvalidKeySpecException {
        return spiImpl.engineGetKeySpec(key, keySpec);
    }
    public final Key translateKey(Key key)
                        throws InvalidKeyException {
        return spiImpl.engineTranslateKey(key);
    }
}
