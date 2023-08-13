public final class RSAKeyFactory extends KeyFactorySpi {
    private final static Class<?> rsaPublicKeySpecClass =
                                                RSAPublicKeySpec.class;
    private final static Class<?> rsaPrivateKeySpecClass =
                                                RSAPrivateKeySpec.class;
    private final static Class<?> rsaPrivateCrtKeySpecClass =
                                                RSAPrivateCrtKeySpec.class;
    private final static Class<?> x509KeySpecClass  = X509EncodedKeySpec.class;
    private final static Class<?> pkcs8KeySpecClass = PKCS8EncodedKeySpec.class;
    public final static int MIN_MODLEN = 512;
    public final static int MAX_MODLEN = 16384;
    public final static int MAX_MODLEN_RESTRICT_EXP = 3072;
    public final static int MAX_RESTRICTED_EXPLEN = 64;
    private static final boolean restrictExpLen =
        "true".equalsIgnoreCase(AccessController.doPrivileged(
            new GetPropertyAction(
                "sun.security.rsa.restrictRSAExponent", "true")));
    private final static RSAKeyFactory INSTANCE = new RSAKeyFactory();
    public RSAKeyFactory() {
    }
    public static RSAKey toRSAKey(Key key) throws InvalidKeyException {
        if ((key instanceof RSAPrivateKeyImpl) ||
            (key instanceof RSAPrivateCrtKeyImpl) ||
            (key instanceof RSAPublicKeyImpl)) {
            return (RSAKey)key;
        } else {
            return (RSAKey)INSTANCE.engineTranslateKey(key);
        }
    }
    static void checkRSAProviderKeyLengths(int modulusLen, BigInteger exponent)
            throws InvalidKeyException {
        checkKeyLengths(((modulusLen + 7) & ~7), exponent,
            RSAKeyFactory.MIN_MODLEN, Integer.MAX_VALUE);
    }
     public static void checkKeyLengths(int modulusLen, BigInteger exponent,
            int minModulusLen, int maxModulusLen) throws InvalidKeyException {
        if ((minModulusLen > 0) && (modulusLen < (minModulusLen))) {
            throw new InvalidKeyException( "RSA keys must be at least " +
                minModulusLen + " bits long");
        }
        int maxLen = Math.min(maxModulusLen, MAX_MODLEN);
        if (modulusLen > maxLen) {
            throw new InvalidKeyException(
                "RSA keys must be no longer than " + maxLen + " bits");
        }
        if (restrictExpLen && (exponent != null) &&
                (modulusLen > MAX_MODLEN_RESTRICT_EXP) &&
                (exponent.bitLength() > MAX_RESTRICTED_EXPLEN)) {
            throw new InvalidKeyException(
                "RSA exponents can be no longer than " +
                MAX_RESTRICTED_EXPLEN + " bits " +
                " if modulus is greater than " +
                MAX_MODLEN_RESTRICT_EXP + " bits");
        }
    }
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        String keyAlg = key.getAlgorithm();
        if (keyAlg.equals("RSA") == false) {
            throw new InvalidKeyException("Not an RSA key: " + keyAlg);
        }
        if (key instanceof PublicKey) {
            return translatePublicKey((PublicKey)key);
        } else if (key instanceof PrivateKey) {
            return translatePrivateKey((PrivateKey)key);
        } else {
            throw new InvalidKeyException("Neither a public nor a private key");
        }
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        try {
            return generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        try {
            return generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }
    private PublicKey translatePublicKey(PublicKey key)
            throws InvalidKeyException {
        if (key instanceof RSAPublicKey) {
            if (key instanceof RSAPublicKeyImpl) {
                return key;
            }
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            try {
                return new RSAPublicKeyImpl(
                    rsaKey.getModulus(),
                    rsaKey.getPublicExponent()
                );
            } catch (RuntimeException e) {
                throw new InvalidKeyException("Invalid key", e);
            }
        } else if ("X.509".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            return new RSAPublicKeyImpl(encoded);
        } else {
            throw new InvalidKeyException("Public keys must be instance "
                + "of RSAPublicKey or have X.509 encoding");
        }
    }
    private PrivateKey translatePrivateKey(PrivateKey key)
            throws InvalidKeyException {
        if (key instanceof RSAPrivateCrtKey) {
            if (key instanceof RSAPrivateCrtKeyImpl) {
                return key;
            }
            RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey)key;
            try {
                return new RSAPrivateCrtKeyImpl(
                    rsaKey.getModulus(),
                    rsaKey.getPublicExponent(),
                    rsaKey.getPrivateExponent(),
                    rsaKey.getPrimeP(),
                    rsaKey.getPrimeQ(),
                    rsaKey.getPrimeExponentP(),
                    rsaKey.getPrimeExponentQ(),
                    rsaKey.getCrtCoefficient()
                );
            } catch (RuntimeException e) {
                throw new InvalidKeyException("Invalid key", e);
            }
        } else if (key instanceof RSAPrivateKey) {
            if (key instanceof RSAPrivateKeyImpl) {
                return key;
            }
            RSAPrivateKey rsaKey = (RSAPrivateKey)key;
            try {
                return new RSAPrivateKeyImpl(
                    rsaKey.getModulus(),
                    rsaKey.getPrivateExponent()
                );
            } catch (RuntimeException e) {
                throw new InvalidKeyException("Invalid key", e);
            }
        } else if ("PKCS#8".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            return RSAPrivateCrtKeyImpl.newKey(encoded);
        } else {
            throw new InvalidKeyException("Private keys must be instance "
                + "of RSAPrivate(Crt)Key or have PKCS#8 encoding");
        }
    }
    private PublicKey generatePublic(KeySpec keySpec)
            throws GeneralSecurityException {
        if (keySpec instanceof X509EncodedKeySpec) {
            X509EncodedKeySpec x509Spec = (X509EncodedKeySpec)keySpec;
            return new RSAPublicKeyImpl(x509Spec.getEncoded());
        } else if (keySpec instanceof RSAPublicKeySpec) {
            RSAPublicKeySpec rsaSpec = (RSAPublicKeySpec)keySpec;
            return new RSAPublicKeyImpl(
                rsaSpec.getModulus(),
                rsaSpec.getPublicExponent()
            );
        } else {
            throw new InvalidKeySpecException("Only RSAPublicKeySpec "
                + "and X509EncodedKeySpec supported for RSA public keys");
        }
    }
    private PrivateKey generatePrivate(KeySpec keySpec)
            throws GeneralSecurityException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec)keySpec;
            return RSAPrivateCrtKeyImpl.newKey(pkcsSpec.getEncoded());
        } else if (keySpec instanceof RSAPrivateCrtKeySpec) {
            RSAPrivateCrtKeySpec rsaSpec = (RSAPrivateCrtKeySpec)keySpec;
            return new RSAPrivateCrtKeyImpl(
                rsaSpec.getModulus(),
                rsaSpec.getPublicExponent(),
                rsaSpec.getPrivateExponent(),
                rsaSpec.getPrimeP(),
                rsaSpec.getPrimeQ(),
                rsaSpec.getPrimeExponentP(),
                rsaSpec.getPrimeExponentQ(),
                rsaSpec.getCrtCoefficient()
            );
        } else if (keySpec instanceof RSAPrivateKeySpec) {
            RSAPrivateKeySpec rsaSpec = (RSAPrivateKeySpec)keySpec;
            return new RSAPrivateKeyImpl(
                rsaSpec.getModulus(),
                rsaSpec.getPrivateExponent()
            );
        } else {
            throw new InvalidKeySpecException("Only RSAPrivate(Crt)KeySpec "
                + "and PKCS8EncodedKeySpec supported for RSA private keys");
        }
    }
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        try {
            key = engineTranslateKey(key);
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException(e);
        }
        if (key instanceof RSAPublicKey) {
            RSAPublicKey rsaKey = (RSAPublicKey)key;
            if (rsaPublicKeySpecClass.isAssignableFrom(keySpec)) {
                return (T) new RSAPublicKeySpec(
                    rsaKey.getModulus(),
                    rsaKey.getPublicExponent()
                );
            } else if (x509KeySpecClass.isAssignableFrom(keySpec)) {
                return (T) new X509EncodedKeySpec(key.getEncoded());
            } else {
                throw new InvalidKeySpecException
                        ("KeySpec must be RSAPublicKeySpec or "
                        + "X509EncodedKeySpec for RSA public keys");
            }
        } else if (key instanceof RSAPrivateKey) {
            if (pkcs8KeySpecClass.isAssignableFrom(keySpec)) {
                return (T) new PKCS8EncodedKeySpec(key.getEncoded());
            } else if (rsaPrivateCrtKeySpecClass.isAssignableFrom(keySpec)) {
                if (key instanceof RSAPrivateCrtKey) {
                    RSAPrivateCrtKey crtKey = (RSAPrivateCrtKey)key;
                    return (T) new RSAPrivateCrtKeySpec(
                        crtKey.getModulus(),
                        crtKey.getPublicExponent(),
                        crtKey.getPrivateExponent(),
                        crtKey.getPrimeP(),
                        crtKey.getPrimeQ(),
                        crtKey.getPrimeExponentP(),
                        crtKey.getPrimeExponentQ(),
                        crtKey.getCrtCoefficient()
                    );
                } else {
                    throw new InvalidKeySpecException
                    ("RSAPrivateCrtKeySpec can only be used with CRT keys");
                }
            } else if (rsaPrivateKeySpecClass.isAssignableFrom(keySpec)) {
                RSAPrivateKey rsaKey = (RSAPrivateKey)key;
                return (T) new RSAPrivateKeySpec(
                    rsaKey.getModulus(),
                    rsaKey.getPrivateExponent()
                );
            } else {
                throw new InvalidKeySpecException
                        ("KeySpec must be RSAPrivate(Crt)KeySpec or "
                        + "PKCS8EncodedKeySpec for RSA private keys");
            }
        } else {
            throw new InvalidKeySpecException("Neither public nor private key");
        }
    }
}
