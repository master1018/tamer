public class DSAKeyFactory extends KeyFactorySpi {
    static final boolean SERIAL_INTEROP;
    private static final String SERIAL_PROP = "sun.security.key.serial.interop";
    static {
        String prop = AccessController.doPrivileged
                (new GetPropertyAction(SERIAL_PROP, null));
        SERIAL_INTEROP = "true".equalsIgnoreCase(prop);
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
    throws InvalidKeySpecException {
        try {
            if (keySpec instanceof DSAPublicKeySpec) {
                DSAPublicKeySpec dsaPubKeySpec = (DSAPublicKeySpec)keySpec;
                if (SERIAL_INTEROP) {
                    return new DSAPublicKey(dsaPubKeySpec.getY(),
                                        dsaPubKeySpec.getP(),
                                        dsaPubKeySpec.getQ(),
                                        dsaPubKeySpec.getG());
                } else {
                    return new DSAPublicKeyImpl(dsaPubKeySpec.getY(),
                                        dsaPubKeySpec.getP(),
                                        dsaPubKeySpec.getQ(),
                                        dsaPubKeySpec.getG());
                }
            } else if (keySpec instanceof X509EncodedKeySpec) {
                if (SERIAL_INTEROP) {
                    return new DSAPublicKey
                        (((X509EncodedKeySpec)keySpec).getEncoded());
                } else {
                    return new DSAPublicKeyImpl
                        (((X509EncodedKeySpec)keySpec).getEncoded());
                }
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException
                ("Inappropriate key specification: " + e.getMessage());
        }
    }
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
    throws InvalidKeySpecException {
        try {
            if (keySpec instanceof DSAPrivateKeySpec) {
                DSAPrivateKeySpec dsaPrivKeySpec = (DSAPrivateKeySpec)keySpec;
                return new DSAPrivateKey(dsaPrivKeySpec.getX(),
                                         dsaPrivKeySpec.getP(),
                                         dsaPrivKeySpec.getQ(),
                                         dsaPrivKeySpec.getG());
            } else if (keySpec instanceof PKCS8EncodedKeySpec) {
                return new DSAPrivateKey
                    (((PKCS8EncodedKeySpec)keySpec).getEncoded());
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException
                ("Inappropriate key specification: " + e.getMessage());
        }
    }
    protected <T extends KeySpec>
        T engineGetKeySpec(Key key, Class<T> keySpec)
    throws InvalidKeySpecException {
        DSAParams params;
        try {
            if (key instanceof java.security.interfaces.DSAPublicKey) {
                Class<?> dsaPubKeySpec = Class.forName
                    ("java.security.spec.DSAPublicKeySpec");
                Class<?> x509KeySpec = Class.forName
                    ("java.security.spec.X509EncodedKeySpec");
                if (dsaPubKeySpec.isAssignableFrom(keySpec)) {
                    java.security.interfaces.DSAPublicKey dsaPubKey
                        = (java.security.interfaces.DSAPublicKey)key;
                    params = dsaPubKey.getParams();
                    return (T) new DSAPublicKeySpec(dsaPubKey.getY(),
                                                    params.getP(),
                                                    params.getQ(),
                                                    params.getG());
                } else if (x509KeySpec.isAssignableFrom(keySpec)) {
                    return (T) new X509EncodedKeySpec(key.getEncoded());
                } else {
                    throw new InvalidKeySpecException
                        ("Inappropriate key specification");
                }
            } else if (key instanceof java.security.interfaces.DSAPrivateKey) {
                Class<?> dsaPrivKeySpec = Class.forName
                    ("java.security.spec.DSAPrivateKeySpec");
                Class<?> pkcs8KeySpec = Class.forName
                    ("java.security.spec.PKCS8EncodedKeySpec");
                if (dsaPrivKeySpec.isAssignableFrom(keySpec)) {
                    java.security.interfaces.DSAPrivateKey dsaPrivKey
                        = (java.security.interfaces.DSAPrivateKey)key;
                    params = dsaPrivKey.getParams();
                    return (T) new DSAPrivateKeySpec(dsaPrivKey.getX(),
                                                     params.getP(),
                                                     params.getQ(),
                                                     params.getG());
                } else if (pkcs8KeySpec.isAssignableFrom(keySpec)) {
                    return (T) new PKCS8EncodedKeySpec(key.getEncoded());
                } else {
                    throw new InvalidKeySpecException
                        ("Inappropriate key specification");
                }
            } else {
                throw new InvalidKeySpecException("Inappropriate key type");
            }
        } catch (ClassNotFoundException e) {
            throw new InvalidKeySpecException
                ("Unsupported key specification: " + e.getMessage());
        }
    }
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        try {
            if (key instanceof java.security.interfaces.DSAPublicKey) {
                if (key instanceof sun.security.provider.DSAPublicKey) {
                    return key;
                }
                DSAPublicKeySpec dsaPubKeySpec
                    = engineGetKeySpec(key, DSAPublicKeySpec.class);
                return engineGeneratePublic(dsaPubKeySpec);
            } else if (key instanceof java.security.interfaces.DSAPrivateKey) {
                if (key instanceof sun.security.provider.DSAPrivateKey) {
                    return key;
                }
                DSAPrivateKeySpec dsaPrivKeySpec
                    = engineGetKeySpec(key, DSAPrivateKeySpec.class);
                return engineGeneratePrivate(dsaPrivKeySpec);
            } else {
                throw new InvalidKeyException("Wrong algorithm type");
            }
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException("Cannot translate key: "
                                          + e.getMessage());
        }
    }
}
