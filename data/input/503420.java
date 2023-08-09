public class ExemptionMechanism {
    private static final Engine engine = new Engine("ExemptionMechanism");
    private final Provider provider;
    private final ExemptionMechanismSpi spiImpl;
    private final String mechanism;
    private boolean isInit;
    private Key initKey;
    private boolean generated;
    protected ExemptionMechanism(ExemptionMechanismSpi exmechSpi,
            Provider provider, String mechanism) {
        this.mechanism = mechanism;
        this.spiImpl = exmechSpi;
        this.provider = provider;
        isInit = false;
    }
    public final String getName() {
        return mechanism;
    }
    public static final ExemptionMechanism getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("crypto.02")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new ExemptionMechanism((ExemptionMechanismSpi) engine.spi,
                    engine.provider, algorithm);
        }
    }
    public static final ExemptionMechanism getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.04")); 
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("crypto.02")); 
        }
        return getInstance(algorithm, impProvider);
    }
    public static final ExemptionMechanism getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("crypto.02")); 
        }
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("crypto.04")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new ExemptionMechanism((ExemptionMechanismSpi) engine.spi,
                    provider, algorithm);
        }
    }
    public final Provider getProvider() {
        return provider;
    }
    public final boolean isCryptoAllowed(Key key)
            throws ExemptionMechanismException {
        if (generated
                && (initKey.equals(key) || Arrays.equals(initKey.getEncoded(),
                        key.getEncoded()))) {
            return true;
        }
        return false;
    }
    public final int getOutputSize(int inputLen) throws IllegalStateException {
        if (!isInit) {
            throw new IllegalStateException(Messages.getString("crypto.2D"));
        }
        return spiImpl.engineGetOutputSize(inputLen);
    }
    public final void init(Key key) throws InvalidKeyException,
            ExemptionMechanismException {
        generated = false;
        spiImpl.engineInit(key);
        initKey = key;
        isInit = true;
    }
    public final void init(Key key, AlgorithmParameters param)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        generated = false;
        spiImpl.engineInit(key, param);
        initKey = key;
        isInit = true;
    }
    public final void init(Key key, AlgorithmParameterSpec param)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        generated = false;
        spiImpl.engineInit(key, param);
        initKey = key;
        isInit = true;
    }
    public final byte[] genExemptionBlob() throws IllegalStateException,
            ExemptionMechanismException {
        if (!isInit) {
            throw new IllegalStateException(Messages.getString("crypto.2D"));
        }
        generated = false;
        byte[] result = spiImpl.engineGenExemptionBlob();
        generated = true;
        return result;
    }
    public final int genExemptionBlob(byte[] output)
            throws IllegalStateException, ShortBufferException,
            ExemptionMechanismException {
        return genExemptionBlob(output, 0);
    }
    public final int genExemptionBlob(byte[] output, int outputOffset)
            throws IllegalStateException, ShortBufferException,
            ExemptionMechanismException {
        if (!isInit) {
            throw new IllegalStateException(Messages.getString("crypto.2D"));
        }
        generated = false;
        int len = spiImpl.engineGenExemptionBlob(output, outputOffset);
        generated = true;
        return len;
    }
    @Override
    protected void finalize() {
        initKey = null;
    }
}