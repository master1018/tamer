public final class ECKeyFactory extends KeyFactorySpi {
    public final static KeyFactory INSTANCE;
    public final static Provider ecInternalProvider;
    static {
        final Provider p = new Provider("SunEC-Internal", 1.0d, null) {};
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                p.put("KeyFactory.EC", "sun.security.ec.ECKeyFactory");
                p.put("AlgorithmParameters.EC", "sun.security.ec.ECParameters");
                p.put("Alg.Alias.AlgorithmParameters.1.2.840.10045.2.1", "EC");
                return null;
            }
        });
        try {
            INSTANCE = KeyFactory.getInstance("EC", p);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        ecInternalProvider = p;
    }
    public ECKeyFactory() {
    }
    public static ECKey toECKey(Key key) throws InvalidKeyException {
        if (key instanceof ECKey) {
            ECKey ecKey = (ECKey)key;
            checkKey(ecKey);
            return ecKey;
        } else {
            return (ECKey)INSTANCE.translateKey(key);
        }
    }
    private static void checkKey(ECKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            if (key instanceof ECPublicKeyImpl) {
                return;
            }
        } else if (key instanceof ECPrivateKey) {
            if (key instanceof ECPrivateKeyImpl) {
                return;
            }
        } else {
            throw new InvalidKeyException("Neither a public nor a private key");
        }
        String keyAlg = ((Key)key).getAlgorithm();
        if (keyAlg.equals("EC") == false) {
            throw new InvalidKeyException("Not an EC key: " + keyAlg);
        }
    }
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        String keyAlg = key.getAlgorithm();
        if (keyAlg.equals("EC") == false) {
            throw new InvalidKeyException("Not an EC key: " + keyAlg);
        }
        if (key instanceof PublicKey) {
            return implTranslatePublicKey((PublicKey)key);
        } else if (key instanceof PrivateKey) {
            return implTranslatePrivateKey((PrivateKey)key);
        } else {
            throw new InvalidKeyException("Neither a public nor a private key");
        }
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        try {
            return implGeneratePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        try {
            return implGeneratePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw new InvalidKeySpecException(e);
        }
    }
    private PublicKey implTranslatePublicKey(PublicKey key)
            throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            if (key instanceof ECPublicKeyImpl) {
                return key;
            }
            ECPublicKey ecKey = (ECPublicKey)key;
            return new ECPublicKeyImpl(
                ecKey.getW(),
                ecKey.getParams()
            );
        } else if ("X.509".equals(key.getFormat())) {
            byte[] encoded = key.getEncoded();
            return new ECPublicKeyImpl(encoded);
        } else {
            throw new InvalidKeyException("Public keys must be instance "
                + "of ECPublicKey or have X.509 encoding");
        }
    }
    private PrivateKey implTranslatePrivateKey(PrivateKey key)
            throws InvalidKeyException {
        if (key instanceof ECPrivateKey) {
            if (key instanceof ECPrivateKeyImpl) {
                return key;
            }
            ECPrivateKey ecKey = (ECPrivateKey)key;
            return new ECPrivateKeyImpl(
                ecKey.getS(),
                ecKey.getParams()
            );
        } else if ("PKCS#8".equals(key.getFormat())) {
            return new ECPrivateKeyImpl(key.getEncoded());
        } else {
            throw new InvalidKeyException("Private keys must be instance "
                + "of ECPrivateKey or have PKCS#8 encoding");
        }
    }
    private PublicKey implGeneratePublic(KeySpec keySpec)
            throws GeneralSecurityException {
        if (keySpec instanceof X509EncodedKeySpec) {
            X509EncodedKeySpec x509Spec = (X509EncodedKeySpec)keySpec;
            return new ECPublicKeyImpl(x509Spec.getEncoded());
        } else if (keySpec instanceof ECPublicKeySpec) {
            ECPublicKeySpec ecSpec = (ECPublicKeySpec)keySpec;
            return new ECPublicKeyImpl(
                ecSpec.getW(),
                ecSpec.getParams()
            );
        } else {
            throw new InvalidKeySpecException("Only ECPublicKeySpec "
                + "and X509EncodedKeySpec supported for EC public keys");
        }
    }
    private PrivateKey implGeneratePrivate(KeySpec keySpec)
            throws GeneralSecurityException {
        if (keySpec instanceof PKCS8EncodedKeySpec) {
            PKCS8EncodedKeySpec pkcsSpec = (PKCS8EncodedKeySpec)keySpec;
            return new ECPrivateKeyImpl(pkcsSpec.getEncoded());
        } else if (keySpec instanceof ECPrivateKeySpec) {
            ECPrivateKeySpec ecSpec = (ECPrivateKeySpec)keySpec;
            return new ECPrivateKeyImpl(ecSpec.getS(), ecSpec.getParams());
        } else {
            throw new InvalidKeySpecException("Only ECPrivateKeySpec "
                + "and PKCS8EncodedKeySpec supported for EC private keys");
        }
    }
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        try {
            key = engineTranslateKey(key);
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException(e);
        }
        if (key instanceof ECPublicKey) {
            ECPublicKey ecKey = (ECPublicKey)key;
            if (ECPublicKeySpec.class.isAssignableFrom(keySpec)) {
                return (T) new ECPublicKeySpec(
                    ecKey.getW(),
                    ecKey.getParams()
                );
            } else if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return (T) new X509EncodedKeySpec(key.getEncoded());
            } else {
                throw new InvalidKeySpecException
                        ("KeySpec must be ECPublicKeySpec or "
                        + "X509EncodedKeySpec for EC public keys");
            }
        } else if (key instanceof ECPrivateKey) {
            if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return (T) new PKCS8EncodedKeySpec(key.getEncoded());
            } else if (ECPrivateKeySpec.class.isAssignableFrom(keySpec)) {
                ECPrivateKey ecKey = (ECPrivateKey)key;
                return (T) new ECPrivateKeySpec(
                    ecKey.getS(),
                    ecKey.getParams()
                );
            } else {
                throw new InvalidKeySpecException
                        ("KeySpec must be ECPrivateKeySpec or "
                        + "PKCS8EncodedKeySpec for EC private keys");
            }
        } else {
            throw new InvalidKeySpecException("Neither public nor private key");
        }
    }
}
