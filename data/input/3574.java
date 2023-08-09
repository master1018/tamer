public final class HmacMD5KeyGenerator extends KeyGeneratorSpi {
    private SecureRandom random = null;
    private int keysize = 64; 
    public HmacMD5KeyGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        this.random = random;
    }
    protected void engineInit(AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        throw new InvalidAlgorithmParameterException
            ("HMAC-MD5 key generation does not take any parameters");
    }
    protected void engineInit(int keysize, SecureRandom random) {
        this.keysize = (keysize+7) / 8;
        this.engineInit(random);
    }
    protected SecretKey engineGenerateKey() {
        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }
        byte[] keyBytes = new byte[this.keysize];
        this.random.nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, "HmacMD5");
    }
}
