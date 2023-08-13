public final class DESedeKeyGenerator extends KeyGeneratorSpi {
    private SecureRandom random = null;
    private int keysize = 168;
    public DESedeKeyGenerator() {
    }
    protected void engineInit(SecureRandom random) {
        this.random = random;
    }
    protected void engineInit(AlgorithmParameterSpec params,
                              SecureRandom random)
        throws InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException
                ("Triple DES key generation does not take any parameters");
    }
    protected void engineInit(int keysize, SecureRandom random) {
        if ((keysize != 112) && (keysize != 168)) {
            throw new InvalidParameterException("Wrong keysize: must be "
                                                + "equal to 112 or 168");
        }
        this.keysize = keysize;
        this.engineInit(random);
    }
    protected SecretKey engineGenerateKey() {
        if (this.random == null) {
            this.random = SunJCE.RANDOM;
        }
        byte[] rawkey = new byte[DESedeKeySpec.DES_EDE_KEY_LEN];
        if (keysize == 168) {
            this.random.nextBytes(rawkey);
            DESKeyGenerator.setParityBit(rawkey, 0);
            DESKeyGenerator.setParityBit(rawkey, 8);
            DESKeyGenerator.setParityBit(rawkey, 16);
        } else {
            byte[] tmpkey = new byte[16];
            this.random.nextBytes(tmpkey);
            DESKeyGenerator.setParityBit(tmpkey, 0);
            DESKeyGenerator.setParityBit(tmpkey, 8);
            System.arraycopy(tmpkey, 0, rawkey, 0, tmpkey.length);
            System.arraycopy(tmpkey, 0, rawkey, 16, 8);
            java.util.Arrays.fill(tmpkey, (byte)0x00);
        }
        DESedeKey desEdeKey = null;
        try {
            desEdeKey = new DESedeKey(rawkey);
        } catch (InvalidKeyException ike) {
            throw new RuntimeException(ike.getMessage());
        }
        java.util.Arrays.fill(rawkey, (byte)0x00);
        return desEdeKey;
    }
}
