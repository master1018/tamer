public class ExemptionMechanism {
    private Provider provider;
    private ExemptionMechanismSpi exmechSpi;
    private String mechanism;
    private boolean done = false;
    private boolean initialized = false;
    private Key keyStored = null;
    protected ExemptionMechanism(ExemptionMechanismSpi exmechSpi,
                                 Provider provider,
                                 String mechanism) {
        this.exmechSpi = exmechSpi;
        this.provider = provider;
        this.mechanism = mechanism;
    }
    public final String getName() {
        return this.mechanism;
    }
    public static final ExemptionMechanism getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance("ExemptionMechanism",
                ExemptionMechanismSpi.class, algorithm);
        return new ExemptionMechanism((ExemptionMechanismSpi)instance.impl,
                instance.provider, algorithm);
    }
    public static final ExemptionMechanism getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Instance instance = JceSecurity.getInstance("ExemptionMechanism",
                ExemptionMechanismSpi.class, algorithm, provider);
        return new ExemptionMechanism((ExemptionMechanismSpi)instance.impl,
                instance.provider, algorithm);
    }
    public static final ExemptionMechanism getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance("ExemptionMechanism",
                ExemptionMechanismSpi.class, algorithm, provider);
        return new ExemptionMechanism((ExemptionMechanismSpi)instance.impl,
                instance.provider, algorithm);
    }
    public final Provider getProvider() {
        return this.provider;
    }
    public final boolean isCryptoAllowed(Key key)
            throws ExemptionMechanismException {
        boolean ret = false;
        if (done && (key != null)) {
            ret = keyStored.equals(key);
        }
        return ret;
     }
    public final int getOutputSize(int inputLen) throws IllegalStateException {
        if (!initialized) {
            throw new IllegalStateException(
                "ExemptionMechanism not initialized");
        }
        if (inputLen < 0) {
            throw new IllegalArgumentException(
                "Input size must be equal to " + "or greater than zero");
        }
        return exmechSpi.engineGetOutputSize(inputLen);
    }
    public final void init(Key key)
            throws InvalidKeyException, ExemptionMechanismException {
        done = false;
        initialized = false;
        keyStored = key;
        exmechSpi.engineInit(key);
        initialized = true;
    }
    public final void init(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        done = false;
        initialized = false;
        keyStored = key;
        exmechSpi.engineInit(key, params);
        initialized = true;
    }
    public final void init(Key key, AlgorithmParameters params)
            throws InvalidKeyException, InvalidAlgorithmParameterException,
            ExemptionMechanismException {
        done = false;
        initialized = false;
        keyStored = key;
        exmechSpi.engineInit(key, params);
        initialized = true;
    }
    public final byte[] genExemptionBlob() throws IllegalStateException,
            ExemptionMechanismException {
        if (!initialized) {
            throw new IllegalStateException(
                "ExemptionMechanism not initialized");
        }
        byte[] blob = exmechSpi.engineGenExemptionBlob();
        done = true;
        return blob;
    }
    public final int genExemptionBlob(byte[] output)
            throws IllegalStateException, ShortBufferException,
            ExemptionMechanismException {
        if (!initialized) {
            throw new IllegalStateException
            ("ExemptionMechanism not initialized");
        }
        int n = exmechSpi.engineGenExemptionBlob(output, 0);
        done = true;
        return n;
    }
    public final int genExemptionBlob(byte[] output, int outputOffset)
            throws IllegalStateException, ShortBufferException,
            ExemptionMechanismException {
        if (!initialized) {
            throw new IllegalStateException
            ("ExemptionMechanism not initialized");
        }
        int n = exmechSpi.engineGenExemptionBlob(output, outputOffset);
        done = true;
        return n;
    }
    protected void finalize() {
        keyStored = null;
    }
}
