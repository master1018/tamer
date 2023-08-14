abstract class P11KeyFactory extends KeyFactorySpi {
    final Token token;
    final String algorithm;
    P11KeyFactory(Token token, String algorithm) {
        super();
        this.token = token;
        this.algorithm = algorithm;
    }
    static P11Key convertKey(Token token, Key key, String algorithm)
            throws InvalidKeyException {
        return (P11Key)token.getKeyFactory(algorithm).engineTranslateKey(key);
    }
    protected final <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if ((key == null) || (keySpec == null)) {
            throw new InvalidKeySpecException
                ("key and keySpec must not be null");
        }
        if (PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)
                || X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
            try {
                return (T)implGetSoftwareFactory().getKeySpec(key, keySpec);
            } catch (GeneralSecurityException e) {
                throw new InvalidKeySpecException("Could not encode key", e);
            }
        }
        P11Key p11Key;
        try {
            p11Key = (P11Key)engineTranslateKey(key);
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException("Could not convert key", e);
        }
        Session[] session = new Session[1];
        try {
            if (p11Key.isPublic()) {
                return (T)implGetPublicKeySpec(p11Key, keySpec, session);
            } else {
                return (T)implGetPrivateKeySpec(p11Key, keySpec, session);
            }
        } catch (PKCS11Exception e) {
            throw new InvalidKeySpecException("Could not generate KeySpec", e);
        } finally {
            session[0] = token.releaseSession(session[0]);
        }
    }
    protected final Key engineTranslateKey(Key key) throws InvalidKeyException {
        token.ensureValid();
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        if (key.getAlgorithm().equals(this.algorithm) == false) {
            throw new InvalidKeyException
                ("Key algorithm must be " + algorithm);
        }
        if (key instanceof P11Key) {
            P11Key p11Key = (P11Key)key;
            if (p11Key.token == token) {
                return key;
            }
        }
        P11Key p11Key = token.privateCache.get(key);
        if (p11Key != null) {
            return p11Key;
        }
        if (key instanceof PublicKey) {
            PublicKey publicKey = implTranslatePublicKey((PublicKey)key);
            token.privateCache.put(key, (P11Key)publicKey);
            return publicKey;
        } else if (key instanceof PrivateKey) {
            PrivateKey privateKey = implTranslatePrivateKey((PrivateKey)key);
            token.privateCache.put(key, (P11Key)privateKey);
            return privateKey;
        } else {
            throw new InvalidKeyException
                ("Key must be instance of PublicKey or PrivateKey");
        }
    }
    abstract KeySpec implGetPublicKeySpec(P11Key key, Class keySpec,
            Session[] session) throws PKCS11Exception, InvalidKeySpecException;
    abstract KeySpec implGetPrivateKeySpec(P11Key key, Class keySpec,
            Session[] session) throws PKCS11Exception, InvalidKeySpecException;
    abstract PublicKey implTranslatePublicKey(PublicKey key)
            throws InvalidKeyException;
    abstract PrivateKey implTranslatePrivateKey(PrivateKey key)
            throws InvalidKeyException;
    abstract KeyFactory implGetSoftwareFactory() throws GeneralSecurityException;
}
