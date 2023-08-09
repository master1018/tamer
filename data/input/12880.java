public final class AESKeyGenerator extends KeyGeneratorSpi {
    private SecureRandom random = null;
    private int keySize = 16; 
    public AESKeyGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        this.random = random;
    }
    protected void engineInit(AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException
                ("AES key generation does not take any parameters");
    }
    protected void engineInit(int keysize, SecureRandom random) {
        if (((keysize % 8) != 0) ||
            (!AESCrypt.isKeySizeValid(keysize/8))) {
            throw new InvalidParameterException
                ("Wrong keysize: must be equal to 128, 192 or 256");
        }
        this.keySize = keysize/8;
        this.engineInit(random);
    }
    protected SecretKey engineGenerateKey() {
        SecretKeySpec aesKey = null;
        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }
        byte[] keyBytes = new byte[keySize];
        this.random.nextBytes(keyBytes);
        aesKey = new SecretKeySpec(keyBytes, "AES");
        return aesKey;
    }
}
