final class P11SecretKeyFactory extends SecretKeyFactorySpi {
    private final Token token;
    private final String algorithm;
    P11SecretKeyFactory(Token token, String algorithm) {
        super();
        this.token = token;
        this.algorithm = algorithm;
    }
    private static final Map<String,Long> keyTypes;
    static {
        keyTypes = new HashMap<String,Long>();
        addKeyType("RC4",      CKK_RC4);
        addKeyType("ARCFOUR",  CKK_RC4);
        addKeyType("DES",      CKK_DES);
        addKeyType("DESede",   CKK_DES3);
        addKeyType("AES",      CKK_AES);
        addKeyType("Blowfish", CKK_BLOWFISH);
        addKeyType("RC2",      CKK_RC2);
        addKeyType("IDEA",     CKK_IDEA);
        addKeyType("TlsPremasterSecret",    PCKK_TLSPREMASTER);
        addKeyType("TlsRsaPremasterSecret", PCKK_TLSRSAPREMASTER);
        addKeyType("TlsMasterSecret",       PCKK_TLSMASTER);
        addKeyType("Generic",               CKK_GENERIC_SECRET);
    }
    private static void addKeyType(String name, long id) {
        Long l = Long.valueOf(id);
        keyTypes.put(name, l);
        keyTypes.put(name.toUpperCase(Locale.ENGLISH), l);
    }
    static long getKeyType(String algorithm) {
        Long l = keyTypes.get(algorithm);
        if (l == null) {
            algorithm = algorithm.toUpperCase(Locale.ENGLISH);
            l = keyTypes.get(algorithm);
            if (l == null) {
                if (algorithm.startsWith("HMAC")) {
                    return PCKK_HMAC;
                } else if (algorithm.startsWith("SSLMAC")) {
                    return PCKK_SSLMAC;
                }
            }
        }
        return (l != null) ? l.longValue() : -1;
    }
    static P11Key convertKey(Token token, Key key, String algo)
            throws InvalidKeyException {
        return convertKey(token, key, algo, null);
    }
    static P11Key convertKey(Token token, Key key, String algo,
            CK_ATTRIBUTE[] extraAttrs)
            throws InvalidKeyException {
        token.ensureValid();
        if (key == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        if (key instanceof SecretKey == false) {
            throw new InvalidKeyException("Key must be a SecretKey");
        }
        long algoType;
        if (algo == null) {
            algo = key.getAlgorithm();
            algoType = getKeyType(algo);
        } else {
            algoType = getKeyType(algo);
            long keyAlgorithmType = getKeyType(key.getAlgorithm());
            if (algoType != keyAlgorithmType) {
                if ((algoType == PCKK_HMAC) || (algoType == PCKK_SSLMAC)) {
                } else {
                    throw new InvalidKeyException
                            ("Key algorithm must be " + algo);
                }
            }
        }
        if (key instanceof P11Key) {
            P11Key p11Key = (P11Key)key;
            if (p11Key.token == token) {
                if (extraAttrs != null) {
                    Session session = null;
                    try {
                        session = token.getObjSession();
                        long newKeyID = token.p11.C_CopyObject(session.id(),
                                p11Key.keyID, extraAttrs);
                        p11Key = (P11Key) (P11Key.secretKey(session,
                                newKeyID, p11Key.algorithm, p11Key.keyLength,
                                extraAttrs));
                    } catch (PKCS11Exception p11e) {
                        throw new InvalidKeyException
                                ("Cannot duplicate the PKCS11 key", p11e);
                    } finally {
                        token.releaseSession(session);
                    }
                }
                return p11Key;
            }
        }
        P11Key p11Key = token.secretCache.get(key);
        if (p11Key != null) {
            return p11Key;
        }
        if ("RAW".equalsIgnoreCase(key.getFormat()) == false) {
            throw new InvalidKeyException("Encoded format must be RAW");
        }
        byte[] encoded = key.getEncoded();
        p11Key = createKey(token, encoded, algo, algoType, extraAttrs);
        token.secretCache.put(key, p11Key);
        return p11Key;
    }
    static void fixDESParity(byte[] key, int offset) {
        for (int i = 0; i < 8; i++) {
            int b = key[offset] & 0xfe;
            b |= (Integer.bitCount(b) & 1) ^ 1;
            key[offset++] = (byte)b;
        }
    }
    private static P11Key createKey(Token token, byte[] encoded,
            String algorithm, long keyType, CK_ATTRIBUTE[] extraAttrs)
            throws InvalidKeyException {
        int n = encoded.length << 3;
        int keyLength = n;
        try {
            switch ((int)keyType) {
                case (int)CKK_DES:
                    keyLength =
                        P11KeyGenerator.checkKeySize(CKM_DES_KEY_GEN, n, token);
                    fixDESParity(encoded, 0);
                    break;
                case (int)CKK_DES3:
                    keyLength =
                        P11KeyGenerator.checkKeySize(CKM_DES3_KEY_GEN, n, token);
                    fixDESParity(encoded, 0);
                    fixDESParity(encoded, 8);
                    if (keyLength == 112) {
                        keyType = CKK_DES2;
                    } else {
                        keyType = CKK_DES3;
                        fixDESParity(encoded, 16);
                    }
                    break;
                case (int)CKK_AES:
                    keyLength =
                        P11KeyGenerator.checkKeySize(CKM_AES_KEY_GEN, n, token);
                    break;
                case (int)CKK_RC4:
                    keyLength =
                        P11KeyGenerator.checkKeySize(CKM_RC4_KEY_GEN, n, token);
                    break;
                case (int)CKK_BLOWFISH:
                    keyLength =
                        P11KeyGenerator.checkKeySize(CKM_BLOWFISH_KEY_GEN, n,
                        token);
                    break;
                case (int)CKK_GENERIC_SECRET:
                case (int)PCKK_TLSPREMASTER:
                case (int)PCKK_TLSRSAPREMASTER:
                case (int)PCKK_TLSMASTER:
                    keyType = CKK_GENERIC_SECRET;
                    break;
                case (int)PCKK_SSLMAC:
                case (int)PCKK_HMAC:
                    if (n == 0) {
                        throw new InvalidKeyException
                                ("MAC keys must not be empty");
                    }
                    keyType = CKK_GENERIC_SECRET;
                    break;
                default:
                    throw new InvalidKeyException("Unknown algorithm " +
                            algorithm);
            }
        } catch (InvalidAlgorithmParameterException iape) {
            throw new InvalidKeyException("Invalid key for " + algorithm,
                    iape);
        } catch (ProviderException pe) {
            throw new InvalidKeyException("Could not create key", pe);
        }
        Session session = null;
        try {
            CK_ATTRIBUTE[] attributes;
            if (extraAttrs != null) {
                attributes = new CK_ATTRIBUTE[3 + extraAttrs.length];
                System.arraycopy(extraAttrs, 0, attributes, 3,
                        extraAttrs.length);
            } else {
                attributes = new CK_ATTRIBUTE[3];
            }
            attributes[0] = new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY);
            attributes[1] = new CK_ATTRIBUTE(CKA_KEY_TYPE, keyType);
            attributes[2] = new CK_ATTRIBUTE(CKA_VALUE, encoded);
            attributes = token.getAttributes
                (O_IMPORT, CKO_SECRET_KEY, keyType, attributes);
            session = token.getObjSession();
            long keyID = token.p11.C_CreateObject(session.id(), attributes);
            P11Key p11Key = (P11Key)P11Key.secretKey
                (session, keyID, algorithm, keyLength, attributes);
            return p11Key;
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("Could not create key", e);
        } finally {
            token.releaseSession(session);
        }
    }
    protected SecretKey engineGenerateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if (keySpec == null) {
            throw new InvalidKeySpecException("KeySpec must not be null");
        }
        if (keySpec instanceof SecretKeySpec) {
            try {
                Key key = convertKey(token, (SecretKey)keySpec, algorithm);
                return (SecretKey)key;
            } catch (InvalidKeyException e) {
                throw new InvalidKeySpecException(e);
            }
        } else if (algorithm.equalsIgnoreCase("DES")) {
            if (keySpec instanceof DESKeySpec) {
                byte[] keyBytes = ((DESKeySpec)keySpec).getKey();
                keySpec = new SecretKeySpec(keyBytes, "DES");
                return engineGenerateSecret(keySpec);
            }
        } else if (algorithm.equalsIgnoreCase("DESede")) {
            if (keySpec instanceof DESedeKeySpec) {
                byte[] keyBytes = ((DESedeKeySpec)keySpec).getKey();
                keySpec = new SecretKeySpec(keyBytes, "DESede");
                return engineGenerateSecret(keySpec);
            }
        }
        throw new InvalidKeySpecException
                ("Unsupported spec: " + keySpec.getClass().getName());
    }
    private byte[] getKeyBytes(SecretKey key) throws InvalidKeySpecException {
        try {
            key = engineTranslateKey(key);
            if ("RAW".equalsIgnoreCase(key.getFormat()) == false) {
                throw new InvalidKeySpecException
                    ("Could not obtain key bytes");
            }
            byte[] k = key.getEncoded();
            return k;
        } catch (InvalidKeyException e) {
            throw new InvalidKeySpecException(e);
        }
    }
    protected KeySpec engineGetKeySpec(SecretKey key, Class keySpec)
            throws InvalidKeySpecException {
        token.ensureValid();
        if ((key == null) || (keySpec == null)) {
            throw new InvalidKeySpecException
                ("key and keySpec must not be null");
        }
        if (SecretKeySpec.class.isAssignableFrom(keySpec)) {
            return new SecretKeySpec(getKeyBytes(key), algorithm);
        } else if (algorithm.equalsIgnoreCase("DES")) {
            try {
                if (DESKeySpec.class.isAssignableFrom(keySpec)) {
                    return new DESKeySpec(getKeyBytes(key));
                }
            } catch (InvalidKeyException e) {
                throw new InvalidKeySpecException(e);
            }
        } else if (algorithm.equalsIgnoreCase("DESede")) {
            try {
                if (DESedeKeySpec.class.isAssignableFrom(keySpec)) {
                    return new DESedeKeySpec(getKeyBytes(key));
                }
            } catch (InvalidKeyException e) {
                throw new InvalidKeySpecException(e);
            }
        }
        throw new InvalidKeySpecException
                ("Unsupported spec: " + keySpec.getName());
    }
    protected SecretKey engineTranslateKey(SecretKey key)
            throws InvalidKeyException {
        return (SecretKey)convertKey(token, key, algorithm);
    }
}
