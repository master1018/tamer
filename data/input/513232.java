public class DSAKeyFactoryImpl extends KeyFactorySpi {
    protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
            throws InvalidKeySpecException {
        if (keySpec != null) {
            if (keySpec instanceof DSAPrivateKeySpec) {
                return new DSAPrivateKeyImpl((DSAPrivateKeySpec) keySpec);
            }
            if (keySpec instanceof PKCS8EncodedKeySpec) {
                return new DSAPrivateKeyImpl((PKCS8EncodedKeySpec) keySpec);
            }
        }
        throw new InvalidKeySpecException(Messages.getString("security.19C")); 
    }
    protected PublicKey engineGeneratePublic(KeySpec keySpec)
            throws InvalidKeySpecException {
        if (keySpec != null) {
            if (keySpec instanceof DSAPublicKeySpec) {
                return new DSAPublicKeyImpl((DSAPublicKeySpec) keySpec);
            }
            if (keySpec instanceof X509EncodedKeySpec) {
                return new DSAPublicKeyImpl((X509EncodedKeySpec) keySpec);
            }
        }
        throw new InvalidKeySpecException(Messages.getString("security.19D")); 
    }
    protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        BigInteger p, q, g, x, y;
        if (key != null) {
            if (keySpec == null) {
                throw new NullPointerException(Messages
                        .getString("security.19E")); 
            }
            if (key instanceof DSAPrivateKey) {
                DSAPrivateKey privateKey = (DSAPrivateKey) key;
                if (keySpec.equals(DSAPrivateKeySpec.class)) {
                    x = privateKey.getX();
                    DSAParams params = privateKey.getParams();
                    p = params.getP();
                    q = params.getQ();
                    g = params.getG();
                    return (T) (new DSAPrivateKeySpec(x, p, q, g));
                }
                if (keySpec.equals(PKCS8EncodedKeySpec.class)) {
                    return (T) (new PKCS8EncodedKeySpec(key.getEncoded()));
                }
                throw new InvalidKeySpecException(Messages
                        .getString("security.19C")); 
            }
            if (key instanceof DSAPublicKey) {
                DSAPublicKey publicKey = (DSAPublicKey) key;
                if (keySpec.equals(DSAPublicKeySpec.class)) {
                    y = publicKey.getY();
                    DSAParams params = publicKey.getParams();
                    p = params.getP();
                    q = params.getQ();
                    g = params.getG();
                    return (T) (new DSAPublicKeySpec(y, p, q, g));
                }
                if (keySpec.equals(X509EncodedKeySpec.class)) {
                    return (T) (new X509EncodedKeySpec(key.getEncoded()));
                }
                throw new InvalidKeySpecException(Messages
                        .getString("security.19D")); 
            }
        }
        throw new InvalidKeySpecException(Messages.getString("security.19F")); 
    }
    protected Key engineTranslateKey(Key key) throws InvalidKeyException {
        if (key != null) {
            if (key instanceof DSAPrivateKey) {
                DSAPrivateKey privateKey = (DSAPrivateKey) key;
                DSAParams params = privateKey.getParams();
                try {
                    return engineGeneratePrivate(new DSAPrivateKeySpec(
                            privateKey.getX(), params.getP(), params.getQ(),
                            params.getG()));
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeyException(Messages.getString(
                            "security.1A0", e)); 
                }
            }
            if (key instanceof DSAPublicKey) {
                DSAPublicKey publicKey = (DSAPublicKey) key;
                DSAParams params = publicKey.getParams();
                try {
                    return engineGeneratePublic(new DSAPublicKeySpec(publicKey
                            .getY(), params.getP(), params.getQ(), params
                            .getG()));
                } catch (InvalidKeySpecException e) {
                    throw new InvalidKeyException(Messages.getString(
                            "security.1A1", e)); 
                }
            }
        }
        throw new InvalidKeyException(Messages.getString("security.19F")); 
    }
}
