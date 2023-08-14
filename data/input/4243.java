public final class BlowfishKeyGenerator extends KeyGeneratorSpi {
    private SecureRandom random = null;
    private int keysize = 16; 
    public BlowfishKeyGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        this.random = random;
    }
    protected void engineInit(AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        throw new InvalidAlgorithmParameterException
            ("Blowfish key generation does not take any parameters");
    }
    protected void engineInit(int keysize, SecureRandom random) {
        if (((keysize % 8) != 0) || (keysize < 32) || (keysize > 448)) {
            throw new InvalidParameterException("Keysize must be "
                                                + "multiple of 8, and can "
                                                + "only range from 32 to 448 "
                                                + "(inclusive)");
        }
        this.keysize = keysize / 8;
        this.engineInit(random);
    }
    protected SecretKey engineGenerateKey() {
        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }
        byte[] keyBytes = new byte[this.keysize];
        this.random.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "Blowfish");
    }
}
