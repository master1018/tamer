public final class DHKeyPairGenerator extends KeyPairGeneratorSpi {
    private DHParameterSpec params;
    private int pSize;
    private int lSize;
    private SecureRandom random;
    public DHKeyPairGenerator() {
        super();
        initialize(1024, null);
    }
    public void initialize(int keysize, SecureRandom random) {
        if ((keysize < 512) || (keysize > 1024) || (keysize % 64 != 0)) {
            throw new InvalidParameterException("Keysize must be multiple "
                                                + "of 64, and can only range "
                                                + "from 512 to 1024 "
                                                + "(inclusive)");
        }
        this.pSize = keysize;
        this.lSize = 0;
        this.random = random;
        this.params = null;
    }
    public void initialize(AlgorithmParameterSpec algParams,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        if (!(algParams instanceof DHParameterSpec)){
            throw new InvalidAlgorithmParameterException
                ("Inappropriate parameter type");
        }
        params = (DHParameterSpec)algParams;
        pSize = params.getP().bitLength();
        if ((pSize < 512) || (pSize > 1024) ||
            (pSize % 64 != 0)) {
            throw new InvalidAlgorithmParameterException
                ("Prime size must be multiple of 64, and can only range "
                 + "from 512 to 1024 (inclusive)");
        }
        lSize = params.getL();
        if ((lSize != 0) && (lSize > pSize)) {
            throw new InvalidAlgorithmParameterException
                ("Exponent size must not be larger than modulus size");
        }
        this.random = random;
    }
    public KeyPair generateKeyPair() {
        if (random == null) {
            random = SunJCE.RANDOM;
        }
        if (params == null) {
            try {
                params = ParameterCache.getDHParameterSpec(pSize, random);
            } catch (GeneralSecurityException e) {
                throw new ProviderException(e);
            }
        }
        BigInteger p = params.getP();
        BigInteger g = params.getG();
        if (lSize <= 0) {
            lSize = Math.max(384, pSize >> 1);
            lSize = Math.min(lSize, pSize);
        }
        BigInteger x;
        BigInteger pMinus2 = p.subtract(BigInteger.valueOf(2));
        do {
            x = new BigInteger(lSize, random);
        } while ((x.compareTo(BigInteger.ONE) < 0) ||
            ((x.compareTo(pMinus2) > 0)));
        BigInteger y = g.modPow(x, p);
        DHPublicKey pubKey = new DHPublicKey(y, p, g, lSize);
        DHPrivateKey privKey = new DHPrivateKey(x, p, g, lSize);
        return new KeyPair(pubKey, privKey);
    }
}
