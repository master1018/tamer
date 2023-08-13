public final class DESKeyGenerator extends KeyGeneratorSpi {
    private SecureRandom random = null;
    public DESKeyGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        this.random = random;
    }
    protected void engineInit(AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException
                ("DES key generation does not take any parameters");
    }
    protected void engineInit(int keysize, SecureRandom random) {
        if (keysize != 56) {
            throw new InvalidParameterException("Wrong keysize: must "
                                                + "be equal to 56");
        }
        this.engineInit(random);
    }
    protected SecretKey engineGenerateKey() {
        DESKey desKey = null;
        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }
        try {
            byte[] key = new byte[DESKeySpec.DES_KEY_LEN];
            do {
                this.random.nextBytes(key);
                setParityBit(key, 0);
            } while (DESKeySpec.isWeak(key, 0));
            desKey = new DESKey(key);
        } catch (InvalidKeyException e) {
        }
        return desKey;
    }
    static void setParityBit(byte[] key, int offset) {
        if (key == null)
            return;
        for (int i = 0; i < DESKeySpec.DES_KEY_LEN; i++) {
            int b = key[offset] & 0xfe;
            b |= (Integer.bitCount(b) & 1) ^ 1;
            key[offset++] = (byte)b;
        }
    }
}
