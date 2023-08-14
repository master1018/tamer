final class P11Digest extends MessageDigestSpi {
    private final static int S_BLANK    = 1;
    private final static int S_BUFFERED = 2;
    private final static int S_INIT     = 3;
    private final static int BUFFER_SIZE = 96;
    private final Token token;
    private final String algorithm;
    private final long mechanism;
    private final int digestLength;
    private Session session;
    private int state;
    private byte[] oneByte;
    private final byte[] buffer;
    private int bufOfs;
    P11Digest(Token token, String algorithm, long mechanism) {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
        switch ((int)mechanism) {
        case (int)CKM_MD2:
        case (int)CKM_MD5:
            digestLength = 16;
            break;
        case (int)CKM_SHA_1:
            digestLength = 20;
            break;
        case (int)CKM_SHA256:
            digestLength = 32;
            break;
        case (int)CKM_SHA384:
            digestLength = 48;
            break;
        case (int)CKM_SHA512:
            digestLength = 64;
            break;
        default:
            throw new ProviderException("Unknown mechanism: " + mechanism);
        }
        buffer = new byte[BUFFER_SIZE];
        state = S_BLANK;
        engineReset();
    }
    protected int engineGetDigestLength() {
        return digestLength;
    }
    private void cancelOperation() {
        token.ensureValid();
        if (session == null) {
            return;
        }
        if ((state != S_INIT) || (token.explicitCancel == false)) {
            return;
        }
        try {
            token.p11.C_DigestFinal(session.id(), buffer, 0, buffer.length);
        } catch (PKCS11Exception e) {
            throw new ProviderException("cancel() failed", e);
        } finally {
            state = S_BUFFERED;
        }
    }
    private void fetchSession() {
        token.ensureValid();
        if (state == S_BLANK) {
            engineReset();
        }
    }
    protected void engineReset() {
        try {
            cancelOperation();
            bufOfs = 0;
            if (session == null) {
                session = token.getOpSession();
            }
            state = S_BUFFERED;
        } catch (PKCS11Exception e) {
            state = S_BLANK;
            throw new ProviderException("reset() failed, ", e);
        }
    }
    protected byte[] engineDigest() {
        try {
            byte[] digest = new byte[digestLength];
            int n = engineDigest(digest, 0, digestLength);
            return digest;
        } catch (DigestException e) {
            throw new ProviderException("internal error", e);
        }
    }
    protected int engineDigest(byte[] digest, int ofs, int len)
            throws DigestException {
        if (len < digestLength) {
            throw new DigestException("Length must be at least " + digestLength);
        }
        fetchSession();
        try {
            int n;
            if (state == S_BUFFERED) {
                n = token.p11.C_DigestSingle(session.id(),
                        new CK_MECHANISM(mechanism),
                        buffer, 0, bufOfs, digest, ofs, len);
            } else {
                if (bufOfs != 0) {
                    doUpdate(buffer, 0, bufOfs);
                }
                n = token.p11.C_DigestFinal(session.id(), digest, ofs, len);
            }
            if (n != digestLength) {
                throw new ProviderException("internal digest length error");
            }
            return n;
        } catch (PKCS11Exception e) {
            throw new ProviderException("digest() failed", e);
        } finally {
            state = S_BLANK;
            bufOfs = 0;
            session = token.releaseSession(session);
        }
    }
    protected void engineUpdate(byte in) {
        if (oneByte == null) {
            oneByte = new byte[1];
        }
        oneByte[0] = in;
        engineUpdate(oneByte, 0, 1);
    }
    protected void engineUpdate(byte[] in, int ofs, int len) {
        fetchSession();
        if (len <= 0) {
            return;
        }
        if ((bufOfs != 0) && (bufOfs + len > buffer.length)) {
            doUpdate(buffer, 0, bufOfs);
            bufOfs = 0;
        }
        if (bufOfs + len > buffer.length) {
            doUpdate(in, ofs, len);
        } else {
            System.arraycopy(in, ofs, buffer, bufOfs, len);
            bufOfs += len;
        }
    }
    protected void implUpdate(SecretKey key) throws InvalidKeyException {
        fetchSession();
        if (bufOfs != 0) {
            doUpdate(buffer, 0, bufOfs);
            bufOfs = 0;
        }
        if (key instanceof P11Key == false) {
            throw new InvalidKeyException("Not a P11Key: " + key);
        }
        P11Key p11Key = (P11Key)key;
        if (p11Key.token != token) {
            throw new InvalidKeyException("Not a P11Key of this provider: " + key);
        }
        try {
            if (state == S_BUFFERED) {
                token.p11.C_DigestInit(session.id(), new CK_MECHANISM(mechanism));
                state = S_INIT;
            }
            token.p11.C_DigestKey(session.id(), p11Key.keyID);
        } catch (PKCS11Exception e) {
            throw new ProviderException("update(SecretKey) failed", e);
        }
    }
    protected void engineUpdate(ByteBuffer byteBuffer) {
        fetchSession();
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
        try {
            if (state == S_BUFFERED) {
                token.p11.C_DigestInit(session.id(), new CK_MECHANISM(mechanism));
                state = S_INIT;
                if (bufOfs != 0) {
                    doUpdate(buffer, 0, bufOfs);
                    bufOfs = 0;
                }
            }
            token.p11.C_DigestUpdate(session.id(), addr + ofs, null, 0, len);
            byteBuffer.position(ofs + len);
        } catch (PKCS11Exception e) {
            throw new ProviderException("update() failed", e);
        }
    }
    private void doUpdate(byte[] in, int ofs, int len) {
        if (len <= 0) {
            return;
        }
        try {
            if (state == S_BUFFERED) {
                token.p11.C_DigestInit(session.id(), new CK_MECHANISM(mechanism));
                state = S_INIT;
            }
            token.p11.C_DigestUpdate(session.id(), 0, in, ofs, len);
        } catch (PKCS11Exception e) {
            throw new ProviderException("update() failed", e);
        }
    }
}
