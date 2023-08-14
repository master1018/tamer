public final class ECKeyPairGenerator extends KeyPairGeneratorSpi {
    private static final int KEY_SIZE_MIN = 112; 
    private static final int KEY_SIZE_MAX = 571; 
    private static final int KEY_SIZE_DEFAULT = 256;
    private SecureRandom random;
    private int keySize;
    private AlgorithmParameterSpec params = null;
    public ECKeyPairGenerator() {
        initialize(KEY_SIZE_DEFAULT, null);
    }
    @Override
    public void initialize(int keySize, SecureRandom random) {
        checkKeySize(keySize);
        this.params = NamedCurve.getECParameterSpec(keySize);
        if (params == null) {
            throw new InvalidParameterException(
                "No EC parameters available for key size " + keySize + " bits");
        }
        this.random = random;
    }
    @Override
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (params instanceof ECParameterSpec) {
            this.params = ECParameters.getNamedCurve((ECParameterSpec)params);
            if (this.params == null) {
                throw new InvalidAlgorithmParameterException(
                    "Unsupported curve: " + params);
            }
        } else if (params instanceof ECGenParameterSpec) {
            String name = ((ECGenParameterSpec)params).getName();
            this.params = NamedCurve.getECParameterSpec(name);
            if (this.params == null) {
                throw new InvalidAlgorithmParameterException(
                    "Unknown curve name: " + name);
            }
        } else {
            throw new InvalidAlgorithmParameterException(
                "ECParameterSpec or ECGenParameterSpec required for EC");
        }
        this.keySize =
            ((ECParameterSpec)this.params).getCurve().getField().getFieldSize();
        this.random = random;
    }
    @Override
    public KeyPair generateKeyPair() {
        byte[] encodedParams =
            ECParameters.encodeParameters((ECParameterSpec)params);
        byte[] seed = new byte[(((keySize + 7) >> 3) + 1) * 2];
        if (random == null) {
            random = JCAUtil.getSecureRandom();
        }
        random.nextBytes(seed);
        try {
            long[] handles = generateECKeyPair(keySize, encodedParams, seed);
            BigInteger s = new BigInteger(1, getEncodedBytes(handles[0]));
            PrivateKey privateKey =
                new ECPrivateKeyImpl(s, (ECParameterSpec)params);
            ECPoint w = ECParameters.decodePoint(getEncodedBytes(handles[1]),
                ((ECParameterSpec)params).getCurve());
            PublicKey publicKey =
                new ECPublicKeyImpl(w, (ECParameterSpec)params);
            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            throw new ProviderException(e);
        }
    }
    private void checkKeySize(int keySize) throws InvalidParameterException {
        if (keySize < KEY_SIZE_MIN) {
            throw new InvalidParameterException
                ("Key size must be at least " + KEY_SIZE_MIN + " bits");
        }
        if (keySize > KEY_SIZE_MAX) {
            throw new InvalidParameterException
                ("Key size must be at most " + KEY_SIZE_MAX + " bits");
        }
        this.keySize = keySize;
    }
    private static native long[] generateECKeyPair(int keySize,
        byte[] encodedParams, byte[] seed) throws GeneralSecurityException;
    private static native byte[] getEncodedBytes(long handle);
}
