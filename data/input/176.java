public final class TlsRsaPremasterSecretGenerator extends KeyGeneratorSpi {
    private final static String MSG = "TlsRsaPremasterSecretGenerator must be "
        + "initialized using a TlsRsaPremasterSecretParameterSpec";
    private TlsRsaPremasterSecretParameterSpec spec;
    private SecureRandom random;
    public TlsRsaPremasterSecretGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }
    protected void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (params instanceof TlsRsaPremasterSecretParameterSpec == false) {
            throw new InvalidAlgorithmParameterException(MSG);
        }
        this.spec = (TlsRsaPremasterSecretParameterSpec)params;
        this.random = random;
    }
    protected void engineInit(int keysize, SecureRandom random) {
        throw new InvalidParameterException(MSG);
    }
    protected SecretKey engineGenerateKey() {
        if (spec == null) {
            throw new IllegalStateException(
                "TlsRsaPremasterSecretGenerator must be initialized");
        }
        if (random == null) {
            random = new SecureRandom();
        }
        byte[] b = new byte[48];
        random.nextBytes(b);
        b[0] = (byte)spec.getMajorVersion();
        b[1] = (byte)spec.getMinorVersion();
        return new SecretKeySpec(b, "TlsRsaPremasterSecret");
    }
}
