final class P11KeyGenerator extends KeyGeneratorSpi {
    private final Token token;
    private final String algorithm;
    private long mechanism;
    private int keySize;
    private int significantKeySize;
    private long keyType;
    private boolean supportBothKeySizes;
    static int checkKeySize(long keyGenMech, int keySize, Token token)
        throws InvalidAlgorithmParameterException, ProviderException {
        int sigKeySize;
        switch ((int)keyGenMech) {
            case (int)CKM_DES_KEY_GEN:
                if ((keySize != 64) && (keySize != 56)) {
                    throw new InvalidAlgorithmParameterException
                            ("DES key length must be 56 bits");
                }
                sigKeySize = 56;
                break;
            case (int)CKM_DES2_KEY_GEN:
            case (int)CKM_DES3_KEY_GEN:
                if ((keySize == 112) || (keySize == 128)) {
                    sigKeySize = 112;
                } else if ((keySize == 168) || (keySize == 192)) {
                    sigKeySize = 168;
                } else {
                    throw new InvalidAlgorithmParameterException
                            ("DESede key length must be 112, or 168 bits");
                }
                break;
            default:
                CK_MECHANISM_INFO info = null;
                try {
                    info = token.getMechanismInfo(keyGenMech);
                } catch (PKCS11Exception p11e) {
                    throw new ProviderException
                            ("Cannot retrieve mechanism info", p11e);
                }
                if (info == null) {
                    return keySize;
                }
                int minKeySize = (int)info.ulMinKeySize;
                int maxKeySize = (int)info.ulMaxKeySize;
                if (keyGenMech != CKM_RC4_KEY_GEN || minKeySize < 8) {
                    minKeySize = (int)info.ulMinKeySize << 3;
                    maxKeySize = (int)info.ulMaxKeySize << 3;
                }
                if (minKeySize < 40) minKeySize = 40;
                if (keySize < minKeySize || keySize > maxKeySize) {
                    throw new InvalidAlgorithmParameterException
                            ("Key length must be between " + minKeySize +
                            " and " + maxKeySize + " bits");
                }
                if (keyGenMech == CKM_AES_KEY_GEN) {
                    if ((keySize != 128) && (keySize != 192) &&
                        (keySize != 256)) {
                        throw new InvalidAlgorithmParameterException
                                ("AES key length must be " + minKeySize +
                                (maxKeySize >= 192? ", 192":"") +
                                (maxKeySize >= 256? ", or 256":"") + " bits");
                    }
                }
                sigKeySize = keySize;
        }
        return sigKeySize;
    }
    P11KeyGenerator(Token token, String algorithm, long mechanism)
            throws PKCS11Exception {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
        if (this.mechanism == CKM_DES3_KEY_GEN) {
            supportBothKeySizes =
                (token.provider.config.isEnabled(CKM_DES2_KEY_GEN) &&
                 (token.getMechanismInfo(CKM_DES2_KEY_GEN) != null));
        }
        setDefaultKeySize();
    }
    private void setDefaultKeySize() {
        switch ((int)mechanism) {
        case (int)CKM_DES_KEY_GEN:
            keySize = 64;
            keyType = CKK_DES;
            break;
        case (int)CKM_DES2_KEY_GEN:
            keySize = 128;
            keyType = CKK_DES2;
            break;
        case (int)CKM_DES3_KEY_GEN:
            keySize = 192;
            keyType = CKK_DES3;
            break;
        case (int)CKM_AES_KEY_GEN:
            keySize = 128;
            keyType = CKK_AES;
            break;
        case (int)CKM_RC4_KEY_GEN:
            keySize = 128;
            keyType = CKK_RC4;
            break;
        case (int)CKM_BLOWFISH_KEY_GEN:
            keySize = 128;
            keyType = CKK_BLOWFISH;
            break;
        default:
            throw new ProviderException("Unknown mechanism " + mechanism);
        }
        try {
            significantKeySize = checkKeySize(mechanism, keySize, token);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new ProviderException("Unsupported default key size", iape);
        }
    }
    protected void engineInit(SecureRandom random) {
        token.ensureValid();
        setDefaultKeySize();
    }
    protected void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException
                ("AlgorithmParameterSpec not supported");
    }
    protected void engineInit(int keySize, SecureRandom random) {
        token.ensureValid();
        int newSignificantKeySize;
        try {
            newSignificantKeySize = checkKeySize(mechanism, keySize, token);
        } catch (InvalidAlgorithmParameterException iape) {
            throw (InvalidParameterException)
                    (new InvalidParameterException().initCause(iape));
        }
        if ((mechanism == CKM_DES2_KEY_GEN) ||
            (mechanism == CKM_DES3_KEY_GEN))  {
            long newMechanism = (newSignificantKeySize == 112 ?
                CKM_DES2_KEY_GEN : CKM_DES3_KEY_GEN);
            if (mechanism != newMechanism) {
                if (supportBothKeySizes) {
                    mechanism = newMechanism;
                    keyType = (mechanism == CKM_DES2_KEY_GEN ?
                        CKK_DES2 : CKK_DES3);
                } else {
                    throw new InvalidParameterException
                            ("Only " + significantKeySize +
                             "-bit DESede is supported");
                }
            }
        }
        this.keySize = keySize;
        this.significantKeySize = newSignificantKeySize;
    }
    protected SecretKey engineGenerateKey() {
        Session session = null;
        try {
            session = token.getObjSession();
            CK_ATTRIBUTE[] attributes;
            switch ((int)keyType) {
            case (int)CKK_DES:
            case (int)CKK_DES2:
            case (int)CKK_DES3:
                attributes = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY),
                };
                break;
            default:
                attributes = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY),
                    new CK_ATTRIBUTE(CKA_VALUE_LEN, keySize >> 3),
                };
                break;
            }
            attributes = token.getAttributes
                (O_GENERATE, CKO_SECRET_KEY, keyType, attributes);
            long keyID = token.p11.C_GenerateKey
                (session.id(), new CK_MECHANISM(mechanism), attributes);
            return P11Key.secretKey
                (session, keyID, algorithm, significantKeySize, attributes);
        } catch (PKCS11Exception e) {
            throw new ProviderException("Could not generate key", e);
        } finally {
            token.releaseSession(session);
        }
    }
}
