public final class DHKeyFactory extends KeyFactorySpi {
    public DHKeyFactory() {
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        try {
            if (keySpec instanceof DHPublicKeySpec) {
                DHPublicKeySpec dhPubKeySpec = (DHPublicKeySpec)keySpec;
                return new DHPublicKey(dhPubKeySpec.getY(),
                                       dhPubKeySpec.getP(),
                                       dhPubKeySpec.getG());
            } else if (keySpec instanceof X509EncodedKeySpec) {
                return new DHPublicKey
                    (((X509EncodedKeySpec)keySpec).getEncoded());
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException
                ("Inappropriate key specification");
        }
    }
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
        throws InvalidKeySpecException
    {
        try {
            if (keySpec instanceof DHPrivateKeySpec) {
                DHPrivateKeySpec dhPrivKeySpec = (DHPrivateKeySpec)keySpec;
                return new DHPrivateKey(dhPrivKeySpec.getX(),
                                        dhPrivKeySpec.getP(),
                                        dhPrivKeySpec.getG());
            } else if (keySpec instanceof PKCS8EncodedKeySpec) {
                return new DHPrivateKey
                    (((PKCS8EncodedKeySpec)keySpec).getEncoded());
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException
                ("Inappropriate key specification");
        }
    }
    protected KeySpec engineGetKeySpec(Key key, Class keySpec)
        throws InvalidKeySpecException {
        DHParameterSpec params;
        if (key instanceof javax.crypto.interfaces.DHPublicKey) {
            if (DHPublicKeySpec.class.isAssignableFrom(keySpec)) {
                javax.crypto.interfaces.DHPublicKey dhPubKey
                    = (javax.crypto.interfaces.DHPublicKey) key;
                params = dhPubKey.getParams();
                return new DHPublicKeySpec(dhPubKey.getY(),
                                           params.getP(),
                                           params.getG());
            } else if (X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new X509EncodedKeySpec(key.getEncoded());
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } else if (key instanceof javax.crypto.interfaces.DHPrivateKey) {
            if (DHPrivateKeySpec.class.isAssignableFrom(keySpec)) {
                javax.crypto.interfaces.DHPrivateKey dhPrivKey
                    = (javax.crypto.interfaces.DHPrivateKey)key;
                params = dhPrivKey.getParams();
                return new DHPrivateKeySpec(dhPrivKey.getX(),
                                            params.getP(),
                                            params.getG());
            } else if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
                return new PKCS8EncodedKeySpec(key.getEncoded());
            } else {
                throw new InvalidKeySpecException
                    ("Inappropriate key specification");
            }
        } else {
            throw new InvalidKeySpecException("Inappropriate key type");
        }
    }
    protected Key engineTranslateKey(Key key)
        throws InvalidKeyException
    {
        try {
            if (key instanceof javax.crypto.interfaces.DHPublicKey) {
                if (key instanceof com.sun.crypto.provider.DHPublicKey) {
                    return key;
                }
                DHPublicKeySpec dhPubKeySpec
                    = (DHPublicKeySpec)engineGetKeySpec
                    (key, DHPublicKeySpec.class);
                return engineGeneratePublic(dhPubKeySpec);
            } else if (key instanceof javax.crypto.interfaces.DHPrivateKey) {
                if (key instanceof com.sun.crypto.provider.DHPrivateKey) {
                    return key;
                }
                DHPrivateKeySpec dhPrivKeySpec
                    = (DHPrivateKeySpec)engineGetKeySpec
                    (key, DHPrivateKeySpec.class);
                return engineGeneratePrivate(dhPrivKeySpec);
            } else {
                throw new InvalidKeyException("Wrong algorithm type");
            }
        } catch (InvalidKeySpecException e) {
            throw new InvalidKeyException("Cannot translate key");
        }
    }
}
