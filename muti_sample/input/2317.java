public class TlsPrfParameterSpec implements AlgorithmParameterSpec {
    private final SecretKey secret;
    private final String label;
    private final byte[] seed;
    private final int outputLength;
    private final String prfHashAlg;
    private final int prfHashLength;
    private final int prfBlockSize;
    public TlsPrfParameterSpec(SecretKey secret, String label,
            byte[] seed, int outputLength,
            String prfHashAlg, int prfHashLength, int prfBlockSize) {
        if ((label == null) || (seed == null)) {
            throw new NullPointerException("label and seed must not be null");
        }
        if (outputLength <= 0) {
            throw new IllegalArgumentException("outputLength must be positive");
        }
        this.secret = secret;
        this.label = label;
        this.seed = seed.clone();
        this.outputLength = outputLength;
        this.prfHashAlg = prfHashAlg;
        this.prfHashLength = prfHashLength;
        this.prfBlockSize = prfBlockSize;
    }
    public SecretKey getSecret() {
        return secret;
    }
    public String getLabel() {
        return label;
    }
    public byte[] getSeed() {
        return seed.clone();
    }
    public int getOutputLength() {
        return outputLength;
    }
    public String getPRFHashAlg() {
        return prfHashAlg;
    }
    public int getPRFHashLength() {
        return prfHashLength;
    }
    public int getPRFBlockSize() {
        return prfBlockSize;
    }
}
