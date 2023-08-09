final class P11RSACipher extends CipherSpi {
    private final static int PKCS1_MIN_PADDING_LENGTH = 11;
    private final static byte[] B0 = new byte[0];
    private final static int MODE_ENCRYPT = 1;
    private final static int MODE_DECRYPT = 2;
    private final static int MODE_SIGN    = 3;
    private final static int MODE_VERIFY  = 4;
    private final static int PAD_NONE = 1;
    private final static int PAD_PKCS1 = 2;
    private final Token token;
    private final String algorithm;
    private final long mechanism;
    private Session session;
    private int mode;
    private int padType;
    private byte[] buffer;
    private int bufOfs;
    private P11Key p11Key;
    private boolean initialized;
    private int maxInputSize;
    private int outputSize;
    P11RSACipher(Token token, String algorithm, long mechanism)
            throws PKCS11Exception {
        super();
        this.token = token;
        this.algorithm = "RSA";
        this.mechanism = mechanism;
    }
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (mode.equalsIgnoreCase("ECB") == false) {
            throw new NoSuchAlgorithmException("Unsupported mode " + mode);
        }
    }
    protected void engineSetPadding(String padding)
            throws NoSuchPaddingException {
        String lowerPadding = padding.toLowerCase(Locale.ENGLISH);
        if (lowerPadding.equals("pkcs1padding")) {
            padType = PAD_PKCS1;
        } else if (lowerPadding.equals("nopadding")) {
            padType = PAD_NONE;
        } else {
            throw new NoSuchPaddingException("Unsupported padding " + padding);
        }
    }
    protected int engineGetBlockSize() {
        return 0;
    }
    protected int engineGetOutputSize(int inputLen) {
        return outputSize;
    }
    protected byte[] engineGetIV() {
        return null;
    }
    protected AlgorithmParameters engineGetParameters() {
        return null;
    }
    protected void engineInit(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException {
        implInit(opmode, key);
    }
    protected void engineInit(int opmode, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
        }
        implInit(opmode, key);
    }
    protected void engineInit(int opmode, Key key, AlgorithmParameters params,
            SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
        }
        implInit(opmode, key);
    }
    private void implInit(int opmode, Key key) throws InvalidKeyException {
        cancelOperation();
        p11Key = P11KeyFactory.convertKey(token, key, algorithm);
        boolean encrypt;
        if (opmode == Cipher.ENCRYPT_MODE) {
            encrypt = true;
        } else if (opmode == Cipher.DECRYPT_MODE) {
            encrypt = false;
        } else if (opmode == Cipher.WRAP_MODE) {
            if (p11Key.isPublic() == false) {
                throw new InvalidKeyException
                                ("Wrap has to be used with public keys");
            }
            return;
        } else if (opmode == Cipher.UNWRAP_MODE) {
            if (p11Key.isPrivate() == false) {
                throw new InvalidKeyException
                                ("Unwrap has to be used with private keys");
            }
            return;
        } else {
            throw new InvalidKeyException("Unsupported mode: " + opmode);
        }
        if (p11Key.isPublic()) {
            mode = encrypt ? MODE_ENCRYPT : MODE_VERIFY;
        } else if (p11Key.isPrivate()) {
            mode = encrypt ? MODE_SIGN : MODE_DECRYPT;
        } else {
            throw new InvalidKeyException("Unknown key type: " + p11Key);
        }
        int n = (p11Key.keyLength() + 7) >> 3;
        outputSize = n;
        buffer = new byte[n];
        maxInputSize = ((padType == PAD_PKCS1 && encrypt) ?
                            (n - PKCS1_MIN_PADDING_LENGTH) : n);
        try {
            initialize();
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("init() failed", e);
        }
    }
    private void cancelOperation() {
        token.ensureValid();
        if (initialized == false) {
            return;
        }
        initialized = false;
        if ((session == null) || (token.explicitCancel == false)) {
            return;
        }
        if (session.hasObjects() == false) {
            session = token.killSession(session);
            return;
        }
        try {
            PKCS11 p11 = token.p11;
            int inLen = maxInputSize;
            int outLen = buffer.length;
            switch (mode) {
            case MODE_ENCRYPT:
                p11.C_Encrypt
                        (session.id(), buffer, 0, inLen, buffer, 0, outLen);
                break;
            case MODE_DECRYPT:
                p11.C_Decrypt
                        (session.id(), buffer, 0, inLen, buffer, 0, outLen);
                break;
            case MODE_SIGN:
                byte[] tmpBuffer = new byte[maxInputSize];
                p11.C_Sign
                        (session.id(), tmpBuffer);
                break;
            case MODE_VERIFY:
                p11.C_VerifyRecover
                        (session.id(), buffer, 0, inLen, buffer, 0, outLen);
                break;
            default:
                throw new ProviderException("internal error");
            }
        } catch (PKCS11Exception e) {
        }
    }
    private void ensureInitialized() throws PKCS11Exception {
        token.ensureValid();
        if (initialized == false) {
            initialize();
        }
    }
    private void initialize() throws PKCS11Exception {
        if (session == null) {
            session = token.getOpSession();
        }
        PKCS11 p11 = token.p11;
        CK_MECHANISM ckMechanism = new CK_MECHANISM(mechanism);
        switch (mode) {
        case MODE_ENCRYPT:
            p11.C_EncryptInit(session.id(), ckMechanism, p11Key.keyID);
            break;
        case MODE_DECRYPT:
            p11.C_DecryptInit(session.id(), ckMechanism, p11Key.keyID);
            break;
        case MODE_SIGN:
            p11.C_SignInit(session.id(), ckMechanism, p11Key.keyID);
            break;
        case MODE_VERIFY:
            p11.C_VerifyRecoverInit(session.id(), ckMechanism, p11Key.keyID);
            break;
        default:
            throw new AssertionError("internal error");
        }
        bufOfs = 0;
        initialized = true;
    }
    private void implUpdate(byte[] in, int inOfs, int inLen) {
        try {
            ensureInitialized();
        } catch (PKCS11Exception e) {
            throw new ProviderException("update() failed", e);
        }
        if ((inLen == 0) || (in == null)) {
            return;
        }
        if (bufOfs + inLen > maxInputSize) {
            bufOfs = maxInputSize + 1;
            return;
        }
        System.arraycopy(in, inOfs, buffer, bufOfs, inLen);
        bufOfs += inLen;
    }
    private int implDoFinal(byte[] out, int outOfs, int outLen)
            throws BadPaddingException, IllegalBlockSizeException {
        if (bufOfs > maxInputSize) {
            throw new IllegalBlockSizeException("Data must not be longer "
                + "than " + maxInputSize + " bytes");
        }
        try {
            ensureInitialized();
            PKCS11 p11 = token.p11;
            int n;
            switch (mode) {
            case MODE_ENCRYPT:
                n = p11.C_Encrypt
                        (session.id(), buffer, 0, bufOfs, out, outOfs, outLen);
                break;
            case MODE_DECRYPT:
                n = p11.C_Decrypt
                        (session.id(), buffer, 0, bufOfs, out, outOfs, outLen);
                break;
            case MODE_SIGN:
                byte[] tmpBuffer = new byte[bufOfs];
                System.arraycopy(buffer, 0, tmpBuffer, 0, bufOfs);
                tmpBuffer = p11.C_Sign(session.id(), tmpBuffer);
                if (tmpBuffer.length > outLen) {
                    throw new BadPaddingException("Output buffer too small");
                }
                System.arraycopy(tmpBuffer, 0, out, outOfs, tmpBuffer.length);
                n = tmpBuffer.length;
                break;
            case MODE_VERIFY:
                n = p11.C_VerifyRecover
                        (session.id(), buffer, 0, bufOfs, out, outOfs, outLen);
                break;
            default:
                throw new ProviderException("internal error");
            }
            return n;
        } catch (PKCS11Exception e) {
            throw (BadPaddingException)new BadPaddingException
                ("doFinal() failed").initCause(e);
        } finally {
            initialized = false;
            session = token.releaseSession(session);
        }
    }
    protected byte[] engineUpdate(byte[] in, int inOfs, int inLen) {
        implUpdate(in, inOfs, inLen);
        return B0;
    }
    protected int engineUpdate(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws ShortBufferException {
        implUpdate(in, inOfs, inLen);
        return 0;
    }
    protected byte[] engineDoFinal(byte[] in, int inOfs, int inLen)
            throws IllegalBlockSizeException, BadPaddingException {
        implUpdate(in, inOfs, inLen);
        int n = implDoFinal(buffer, 0, buffer.length);
        byte[] out = new byte[n];
        System.arraycopy(buffer, 0, out, 0, n);
        return out;
    }
    protected int engineDoFinal(byte[] in, int inOfs, int inLen,
            byte[] out, int outOfs) throws ShortBufferException,
            IllegalBlockSizeException, BadPaddingException {
        implUpdate(in, inOfs, inLen);
        return implDoFinal(out, outOfs, out.length - outOfs);
    }
    private byte[] doFinal() throws BadPaddingException,
            IllegalBlockSizeException {
        byte[] t = new byte[2048];
        int n = implDoFinal(t, 0, t.length);
        byte[] out = new byte[n];
        System.arraycopy(t, 0, out, 0, n);
        return out;
    }
    protected byte[] engineWrap(Key key) throws InvalidKeyException,
            IllegalBlockSizeException {
        String keyAlg = key.getAlgorithm();
        P11Key sKey = null;
        try {
            sKey = P11SecretKeyFactory.convertKey(token, key, keyAlg);
        } catch (InvalidKeyException ike) {
            byte[] toBeWrappedKey = key.getEncoded();
            if (toBeWrappedKey == null) {
                throw new InvalidKeyException
                        ("wrap() failed, no encoding available", ike);
            }
            implInit(Cipher.ENCRYPT_MODE, p11Key);
            implUpdate(toBeWrappedKey, 0, toBeWrappedKey.length);
            try {
                return doFinal();
            } catch (BadPaddingException bpe) {
                throw new InvalidKeyException("wrap() failed", bpe);
            } finally {
                implInit(Cipher.WRAP_MODE, p11Key);
            }
        }
        Session s = null;
        try {
            s = token.getOpSession();
            return token.p11.C_WrapKey(s.id(), new CK_MECHANISM(mechanism),
                p11Key.keyID, sKey.keyID);
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("wrap() failed", e);
        } finally {
            token.releaseSession(s);
        }
    }
    protected Key engineUnwrap(byte[] wrappedKey, String algorithm,
            int type) throws InvalidKeyException, NoSuchAlgorithmException {
        if (algorithm.equals("TlsRsaPremasterSecret")) {
            Session s = null;
            try {
                s = token.getObjSession();
                long keyType = CKK_GENERIC_SECRET;
                CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[] {
                    new CK_ATTRIBUTE(CKA_CLASS, CKO_SECRET_KEY),
                    new CK_ATTRIBUTE(CKA_KEY_TYPE, keyType),
                };
                attributes = token.getAttributes
                    (O_IMPORT, CKO_SECRET_KEY, keyType, attributes);
                long keyID = token.p11.C_UnwrapKey(s.id(),
                        new CK_MECHANISM(mechanism), p11Key.keyID, wrappedKey,
                        attributes);
                return P11Key.secretKey(s, keyID, algorithm, 48 << 3,
                        attributes);
            } catch (PKCS11Exception e) {
                throw new InvalidKeyException("unwrap() failed", e);
            } finally {
                token.releaseSession(s);
            }
        }
        implInit(Cipher.DECRYPT_MODE, p11Key);
        if (wrappedKey.length > maxInputSize) {
            throw new InvalidKeyException("Key is too long for unwrapping");
        }
        implUpdate(wrappedKey, 0, wrappedKey.length);
        try {
            byte[] encoded = doFinal();
            return ConstructKeys.constructKey(encoded, algorithm, type);
        } catch (BadPaddingException e) {
            throw new InvalidKeyException("Unwrapping failed", e);
        } catch (IllegalBlockSizeException e) {
            throw new InvalidKeyException("Unwrapping failed", e);
        }
    }
    protected int engineGetKeySize(Key key) throws InvalidKeyException {
        int n = P11KeyFactory.convertKey(token, key, algorithm).keyLength();
        return n;
    }
}
final class ConstructKeys {
    private static final PublicKey constructPublicKey(byte[] encodedKey,
            String encodedKeyAlgorithm)
            throws InvalidKeyException, NoSuchAlgorithmException {
        try {
            KeyFactory keyFactory =
                KeyFactory.getInstance(encodedKeyAlgorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException nsae) {
            throw new NoSuchAlgorithmException("No installed providers " +
                                               "can create keys for the " +
                                               encodedKeyAlgorithm +
                                               "algorithm", nsae);
        } catch (InvalidKeySpecException ike) {
            throw new InvalidKeyException("Cannot construct public key", ike);
        }
    }
    private static final PrivateKey constructPrivateKey(byte[] encodedKey,
            String encodedKeyAlgorithm) throws InvalidKeyException,
            NoSuchAlgorithmException {
        try {
            KeyFactory keyFactory =
                KeyFactory.getInstance(encodedKeyAlgorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException nsae) {
            throw new NoSuchAlgorithmException("No installed providers " +
                                               "can create keys for the " +
                                               encodedKeyAlgorithm +
                                               "algorithm", nsae);
        } catch (InvalidKeySpecException ike) {
            throw new InvalidKeyException("Cannot construct private key", ike);
        }
    }
    private static final SecretKey constructSecretKey(byte[] encodedKey,
            String encodedKeyAlgorithm) {
        return new SecretKeySpec(encodedKey, encodedKeyAlgorithm);
    }
    static final Key constructKey(byte[] encoding, String keyAlgorithm,
            int keyType) throws InvalidKeyException, NoSuchAlgorithmException {
        switch (keyType) {
        case Cipher.SECRET_KEY:
            return constructSecretKey(encoding, keyAlgorithm);
        case Cipher.PRIVATE_KEY:
            return constructPrivateKey(encoding, keyAlgorithm);
        case Cipher.PUBLIC_KEY:
            return constructPublicKey(encoding, keyAlgorithm);
        default:
            throw new InvalidKeyException("Unknown keytype " + keyType);
        }
    }
}
