final class P11Mac extends MacSpi {
    private final static int S_UNINIT   = 1;
    private final static int S_RESET    = 2;
    private final static int S_UPDATE   = 3;
    private final static int S_DOFINAL  = 4;
    private final Token token;
    private final String algorithm;
    private final long mechanism;
    private final CK_MECHANISM ckMechanism;
    private final int macLength;
    private P11Key p11Key;
    private Session session;
    private int state;
    private byte[] oneByte;
    P11Mac(Token token, String algorithm, long mechanism)
            throws PKCS11Exception {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
        Long params = null;
        switch ((int)mechanism) {
        case (int)CKM_MD5_HMAC:
            macLength = 16;
            break;
        case (int)CKM_SHA_1_HMAC:
            macLength = 20;
            break;
        case (int)CKM_SHA256_HMAC:
            macLength = 32;
            break;
        case (int)CKM_SHA384_HMAC:
            macLength = 48;
            break;
        case (int)CKM_SHA512_HMAC:
            macLength = 64;
            break;
        case (int)CKM_SSL3_MD5_MAC:
            macLength = 16;
            params = Long.valueOf(16);
            break;
        case (int)CKM_SSL3_SHA1_MAC:
            macLength = 20;
            params = Long.valueOf(20);
            break;
        default:
            throw new ProviderException("Unknown mechanism: " + mechanism);
        }
        ckMechanism = new CK_MECHANISM(mechanism, params);
        state = S_UNINIT;
        initialize();
    }
    private void ensureInitialized() throws PKCS11Exception {
        token.ensureValid();
        if (state == S_UNINIT) {
            initialize();
        }
    }
    private void cancelOperation() {
        token.ensureValid();
        if (state == S_UNINIT) {
            return;
        }
        state = S_UNINIT;
        if ((session == null) || (token.explicitCancel == false)) {
            return;
        }
        try {
            token.p11.C_SignFinal(session.id(), 0);
        } catch (PKCS11Exception e) {
            throw new ProviderException("Cancel failed", e);
        }
    }
    private void initialize() throws PKCS11Exception {
        if (state == S_RESET) {
            return;
        }
        if (session == null) {
            session = token.getOpSession();
        }
        if (p11Key != null) {
            token.p11.C_SignInit
                (session.id(), ckMechanism, p11Key.keyID);
            state = S_RESET;
        } else {
            state = S_UNINIT;
        }
    }
    protected int engineGetMacLength() {
        return macLength;
    }
    protected void engineReset() {
        if (state == S_DOFINAL) {
            state = S_UNINIT;
            return;
        }
        cancelOperation();
        try {
            initialize();
        } catch (PKCS11Exception e) {
            throw new ProviderException("reset() failed, ", e);
        }
    }
    protected void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (params != null) {
            throw new InvalidAlgorithmParameterException
                ("Parameters not supported");
        }
        cancelOperation();
        p11Key = P11SecretKeyFactory.convertKey(token, key, algorithm);
        try {
            initialize();
        } catch (PKCS11Exception e) {
            throw new InvalidKeyException("init() failed", e);
        }
    }
    protected byte[] engineDoFinal() {
        try {
            ensureInitialized();
            byte[] mac = token.p11.C_SignFinal(session.id(), 0);
            state = S_DOFINAL;
            return mac;
        } catch (PKCS11Exception e) {
            throw new ProviderException("doFinal() failed", e);
        } finally {
            session = token.releaseSession(session);
        }
    }
    protected void engineUpdate(byte input) {
        if (oneByte == null) {
           oneByte = new byte[1];
        }
        oneByte[0] = input;
        engineUpdate(oneByte, 0, 1);
    }
    protected void engineUpdate(byte[] b, int ofs, int len) {
        try {
            ensureInitialized();
            token.p11.C_SignUpdate(session.id(), 0, b, ofs, len);
            state = S_UPDATE;
        } catch (PKCS11Exception e) {
            throw new ProviderException("update() failed", e);
        }
    }
    protected void engineUpdate(ByteBuffer byteBuffer) {
        try {
            ensureInitialized();
            int len = byteBuffer.remaining();
            if (len <= 0) {
                return;
            }
            if (byteBuffer instanceof DirectBuffer == false) {
                super.engineUpdate(byteBuffer);
                return;
            }
            long addr = ((DirectBuffer)byteBuffer).address();
            int ofs = byteBuffer.position();
            token.p11.C_SignUpdate(session.id(), addr + ofs, null, 0, len);
            byteBuffer.position(ofs + len);
            state = S_UPDATE;
        } catch (PKCS11Exception e) {
            throw new ProviderException("update() failed", e);
        }
    }
}
