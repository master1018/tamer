public final class RSAKeyPairGenerator extends KeyPairGeneratorSpi {
    private BigInteger publicExponent;
    private int keySize;
    private SecureRandom random;
    public RSAKeyPairGenerator() {
        initialize(1024, null);
    }
    public void initialize(int keySize, SecureRandom random) {
        try {
            RSAKeyFactory.checkKeyLengths(keySize, RSAKeyGenParameterSpec.F4,
                512, 64 * 1024);
        } catch (InvalidKeyException e) {
            throw new InvalidParameterException(e.getMessage());
        }
        this.keySize = keySize;
        this.random = random;
        this.publicExponent = RSAKeyGenParameterSpec.F4;
    }
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (params instanceof RSAKeyGenParameterSpec == false) {
            throw new InvalidAlgorithmParameterException
                ("Params must be instance of RSAKeyGenParameterSpec");
        }
        RSAKeyGenParameterSpec rsaSpec = (RSAKeyGenParameterSpec)params;
        int tmpKeySize = rsaSpec.getKeysize();
        BigInteger tmpPublicExponent = rsaSpec.getPublicExponent();
        if (tmpPublicExponent == null) {
            tmpPublicExponent = RSAKeyGenParameterSpec.F4;
        } else {
            if (tmpPublicExponent.compareTo(RSAKeyGenParameterSpec.F0) < 0) {
                throw new InvalidAlgorithmParameterException
                        ("Public exponent must be 3 or larger");
            }
            if (tmpPublicExponent.bitLength() > tmpKeySize) {
                throw new InvalidAlgorithmParameterException
                        ("Public exponent must be smaller than key size");
            }
        }
        try {
            RSAKeyFactory.checkKeyLengths(tmpKeySize, tmpPublicExponent,
                512, 64 * 1024);
        } catch (InvalidKeyException e) {
            throw new InvalidAlgorithmParameterException(
                "Invalid key sizes", e);
        }
        this.keySize = tmpKeySize;
        this.publicExponent = tmpPublicExponent;
        this.random = random;
    }
    public KeyPair generateKeyPair() {
        int lp = (keySize + 1) >> 1;
        int lq = keySize - lp;
        if (random == null) {
            random = JCAUtil.getSecureRandom();
        }
        BigInteger e = publicExponent;
        while (true) {
            BigInteger p = BigInteger.probablePrime(lp, random);
            BigInteger q, n;
            do {
                q = BigInteger.probablePrime(lq, random);
                if (p.compareTo(q) < 0) {
                    BigInteger tmp = p;
                    p = q;
                    q = tmp;
                }
                n = p.multiply(q);
            } while (n.bitLength() < keySize);
            BigInteger p1 = p.subtract(BigInteger.ONE);
            BigInteger q1 = q.subtract(BigInteger.ONE);
            BigInteger phi = p1.multiply(q1);
            if (e.gcd(phi).equals(BigInteger.ONE) == false) {
                continue;
            }
            BigInteger d = e.modInverse(phi);
            BigInteger pe = d.mod(p1);
            BigInteger qe = d.mod(q1);
            BigInteger coeff = q.modInverse(p);
            try {
                PublicKey publicKey = new RSAPublicKeyImpl(n, e);
                PrivateKey privateKey =
                        new RSAPrivateCrtKeyImpl(n, e, d, p, q, pe, qe, coeff);
                return new KeyPair(publicKey, privateKey);
            } catch (InvalidKeyException exc) {
                throw new RuntimeException(exc);
            }
        }
    }
}
