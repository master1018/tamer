final class P11Signature extends SignatureSpi {
    private final Token token;
    private final String algorithm;
    private final String keyAlgorithm;
    private final long mechanism;
    private final ObjectIdentifier digestOID;
    private final int type;
    private P11Key p11Key;
    private final MessageDigest md;
    private Session session;
    private int mode;
    private boolean initialized;
    private final byte[] buffer;
    private int bytesProcessed;
    private final static int M_SIGN   = 1;
    private final static int M_VERIFY = 2;
    private final static int T_DIGEST = 1;
    private final static int T_UPDATE = 2;
    private final static int T_RAW    = 3;
    private final static int RAW_ECDSA_MAX = 128;
    P11Signature(Token token, String algorithm, long mechanism)
            throws NoSuchAlgorithmException, PKCS11Exception {
        super();
        this.token = token;
        this.algorithm = algorithm;
        this.mechanism = mechanism;
        byte[] buffer = null;
        ObjectIdentifier digestOID = null;
        MessageDigest md = null;
        switch ((int)mechanism) {
        case (int)CKM_MD2_RSA_PKCS:
        case (int)CKM_MD5_RSA_PKCS:
        case (int)CKM_SHA1_RSA_PKCS:
        case (int)CKM_SHA256_RSA_PKCS:
        case (int)CKM_SHA384_RSA_PKCS:
        case (int)CKM_SHA512_RSA_PKCS:
            keyAlgorithm = "RSA";
            type = T_UPDATE;
            buffer = new byte[1];
            break;
        case (int)CKM_DSA_SHA1:
            keyAlgorithm = "DSA";
            type = T_UPDATE;
            buffer = new byte[1];
            break;
        case (int)CKM_ECDSA_SHA1:
            keyAlgorithm = "EC";
            type = T_UPDATE;
            buffer = new byte[1];
            break;
        case (int)CKM_DSA:
            keyAlgorithm = "DSA";
            if (algorithm.equals("DSA")) {
                type = T_DIGEST;
                md = MessageDigest.getInstance("SHA-1");
            } else if (algorithm.equals("RawDSA")) {
                type = T_RAW;
                buffer = new byte[20];
            } else {
                throw new ProviderException(algorithm);
            }
            break;
        case (int)CKM_ECDSA:
            keyAlgorithm = "EC";
            if (algorithm.equals("NONEwithECDSA")) {
                type = T_RAW;
                buffer = new byte[RAW_ECDSA_MAX];
            } else {
                String digestAlg;
                if (algorithm.equals("SHA1withECDSA")) {
                    digestAlg = "SHA-1";
                } else if (algorithm.equals("SHA256withECDSA")) {
                    digestAlg = "SHA-256";
                } else if (algorithm.equals("SHA384withECDSA")) {
                    digestAlg = "SHA-384";
                } else if (algorithm.equals("SHA512withECDSA")) {
                    digestAlg = "SHA-512";
                } else {
                    throw new ProviderException(algorithm);
                }
                type = T_DIGEST;
                md = MessageDigest.getInstance(digestAlg);
            }
            break;
        case (int)CKM_RSA_PKCS:
        case (int)CKM_RSA_X_509:
            keyAlgorithm = "RSA";
            type = T_DIGEST;
            if (algorithm.equals("MD5withRSA")) {
                md = MessageDigest.getInstance("MD5");
                digestOID = AlgorithmId.MD5_oid;
            } else if (algorithm.equals("SHA1withRSA")) {
                md = MessageDigest.getInstance("SHA-1");
                digestOID = AlgorithmId.SHA_oid;
            } else if (algorithm.equals("MD2withRSA")) {
                md = MessageDigest.getInstance("MD2");
                digestOID = AlgorithmId.MD2_oid;
            } else if (algorithm.equals("SHA256withRSA")) {
                md = MessageDigest.getInstance("SHA-256");
                digestOID = AlgorithmId.SHA256_oid;
            } else if (algorithm.equals("SHA384withRSA")) {
                md = MessageDigest.getInstance("SHA-384");
                digestOID = AlgorithmId.SHA384_oid;
            } else if (algorithm.equals("SHA512withRSA")) {
                md = MessageDigest.getInstance("SHA-512");
                digestOID = AlgorithmId.SHA512_oid;
            } else {
                throw new ProviderException("Unknown signature: " + algorithm);
            }
            break;
        default:
            throw new ProviderException("Unknown mechanism: " + mechanism);
        }
        this.buffer = buffer;
        this.digestOID = digestOID;
        this.md = md;
    }
    private void ensureInitialized() {
        token.ensureValid();
        if (initialized == false) {
            initialize();
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
        if (mode == M_SIGN) {
            try {
                if (type == T_UPDATE) {
                    token.p11.C_SignFinal(session.id(), 0);
                } else {
                    byte[] digest;
                    if (type == T_DIGEST) {
                        digest = md.digest();
                    } else { 
                        digest = buffer;
                    }
                    token.p11.C_Sign(session.id(), digest);
                }
            } catch (PKCS11Exception e) {
                throw new ProviderException("cancel failed", e);
            }
        } else { 
            try {
                byte[] signature;
                if (keyAlgorithm.equals("DSA")) {
                    signature = new byte[40];
                } else {
                    signature = new byte[(p11Key.keyLength() + 7) >> 3];
                }
                if (type == T_UPDATE) {
                    token.p11.C_VerifyFinal(session.id(), signature);
                } else {
                    byte[] digest;
                    if (type == T_DIGEST) {
                        digest = md.digest();
                    } else { 
                        digest = buffer;
                    }
                    token.p11.C_Verify(session.id(), digest, signature);
                }
            } catch (PKCS11Exception e) {
            }
        }
    }
    private void initialize() {
        try {
            if (session == null) {
                session = token.getOpSession();
            }
            if (mode == M_SIGN) {
                token.p11.C_SignInit(session.id(),
                        new CK_MECHANISM(mechanism), p11Key.keyID);
            } else {
                token.p11.C_VerifyInit(session.id(),
                        new CK_MECHANISM(mechanism), p11Key.keyID);
            }
            initialized = true;
        } catch (PKCS11Exception e) {
            throw new ProviderException("Initialization failed", e);
        }
        if (bytesProcessed != 0) {
            bytesProcessed = 0;
            if (md != null) {
                md.reset();
            }
        }
    }
    private void checkRSAKeyLength(int len) throws InvalidKeyException {
        RSAPadding padding;
        try {
            padding = RSAPadding.getInstance
                (RSAPadding.PAD_BLOCKTYPE_1, (len + 7) >> 3);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new InvalidKeyException(iape.getMessage());
        }
        int maxDataSize = padding.getMaxDataSize();
        int encodedLength;
        if (algorithm.equals("MD5withRSA") ||
            algorithm.equals("MD2withRSA")) {
            encodedLength = 34;
        } else if (algorithm.equals("SHA1withRSA")) {
            encodedLength = 35;
        } else if (algorithm.equals("SHA256withRSA")) {
            encodedLength = 51;
        } else if (algorithm.equals("SHA384withRSA")) {
            encodedLength = 67;
        } else if (algorithm.equals("SHA512withRSA")) {
            encodedLength = 83;
        } else {
            throw new ProviderException("Unknown signature algo: " + algorithm);
        }
        if (encodedLength > maxDataSize) {
            throw new InvalidKeyException
                ("Key is too short for this signature algorithm");
        }
    }
    protected void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException {
        if (publicKey == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        if (keyAlgorithm.equals("RSA") && publicKey != p11Key) {
            int keyLen;
            if (publicKey instanceof P11Key) {
                keyLen = ((P11Key) publicKey).keyLength();
            } else {
                keyLen = ((RSAKey) publicKey).getModulus().bitLength();
            }
            checkRSAKeyLength(keyLen);
        }
        cancelOperation();
        mode = M_VERIFY;
        p11Key = P11KeyFactory.convertKey(token, publicKey, keyAlgorithm);
        initialize();
    }
    protected void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException {
        if (privateKey == null) {
            throw new InvalidKeyException("Key must not be null");
        }
        if (keyAlgorithm.equals("RSA") && privateKey != p11Key) {
            int keyLen;
            if (privateKey instanceof P11Key) {
                keyLen = ((P11Key) privateKey).keyLength;
            } else {
                keyLen = ((RSAKey) privateKey).getModulus().bitLength();
            }
            checkRSAKeyLength(keyLen);
        }
        cancelOperation();
        mode = M_SIGN;
        p11Key = P11KeyFactory.convertKey(token, privateKey, keyAlgorithm);
        initialize();
    }
    protected void engineUpdate(byte b) throws SignatureException {
        ensureInitialized();
        switch (type) {
        case T_UPDATE:
            buffer[0] = (byte)b;
            engineUpdate(buffer, 0, 1);
            break;
        case T_DIGEST:
            md.update(b);
            bytesProcessed++;
            break;
        case T_RAW:
            if (bytesProcessed >= buffer.length) {
                bytesProcessed = buffer.length + 1;
                return;
            }
            buffer[bytesProcessed++] = b;
            break;
        default:
            throw new ProviderException("Internal error");
        }
    }
    protected void engineUpdate(byte[] b, int ofs, int len)
            throws SignatureException {
        ensureInitialized();
        if (len == 0) {
            return;
        }
        switch (type) {
        case T_UPDATE:
            try {
                if (mode == M_SIGN) {
                    token.p11.C_SignUpdate(session.id(), 0, b, ofs, len);
                } else {
                    token.p11.C_VerifyUpdate(session.id(), 0, b, ofs, len);
                }
                bytesProcessed += len;
            } catch (PKCS11Exception e) {
                throw new ProviderException(e);
            }
            break;
        case T_DIGEST:
            md.update(b, ofs, len);
            bytesProcessed += len;
            break;
        case T_RAW:
            if (bytesProcessed + len > buffer.length) {
                bytesProcessed = buffer.length + 1;
                return;
            }
            System.arraycopy(b, ofs, buffer, bytesProcessed, len);
            bytesProcessed += len;
            break;
        default:
            throw new ProviderException("Internal error");
        }
    }
    protected void engineUpdate(ByteBuffer byteBuffer) {
        ensureInitialized();
        int len = byteBuffer.remaining();
        if (len <= 0) {
            return;
        }
        switch (type) {
        case T_UPDATE:
            if (byteBuffer instanceof DirectBuffer == false) {
                super.engineUpdate(byteBuffer);
                return;
            }
            long addr = ((DirectBuffer)byteBuffer).address();
            int ofs = byteBuffer.position();
            try {
                if (mode == M_SIGN) {
                    token.p11.C_SignUpdate
                        (session.id(), addr + ofs, null, 0, len);
                } else {
                    token.p11.C_VerifyUpdate
                        (session.id(), addr + ofs, null, 0, len);
                }
                bytesProcessed += len;
                byteBuffer.position(ofs + len);
            } catch (PKCS11Exception e) {
                throw new ProviderException("Update failed", e);
            }
            break;
        case T_DIGEST:
            md.update(byteBuffer);
            bytesProcessed += len;
            break;
        case T_RAW:
            if (bytesProcessed + len > buffer.length) {
                bytesProcessed = buffer.length + 1;
                return;
            }
            byteBuffer.get(buffer, bytesProcessed, len);
            bytesProcessed += len;
            break;
        default:
            throw new ProviderException("Internal error");
        }
    }
    protected byte[] engineSign() throws SignatureException {
        ensureInitialized();
        try {
            byte[] signature;
            if (type == T_UPDATE) {
                int len = keyAlgorithm.equals("DSA") ? 40 : 0;
                signature = token.p11.C_SignFinal(session.id(), len);
            } else {
                byte[] digest;
                if (type == T_DIGEST) {
                    digest = md.digest();
                } else { 
                    if (mechanism == CKM_DSA) {
                        if (bytesProcessed != buffer.length) {
                            throw new SignatureException
                            ("Data for RawDSA must be exactly 20 bytes long");
                        }
                        digest = buffer;
                    } else { 
                        if (bytesProcessed > buffer.length) {
                            throw new SignatureException("Data for NONEwithECDSA"
                            + " must be at most " + RAW_ECDSA_MAX + " bytes long");
                        }
                        digest = new byte[bytesProcessed];
                        System.arraycopy(buffer, 0, digest, 0, bytesProcessed);
                    }
                }
                if (keyAlgorithm.equals("RSA") == false) {
                    signature = token.p11.C_Sign(session.id(), digest);
                } else { 
                    byte[] data = encodeSignature(digest);
                    if (mechanism == CKM_RSA_X_509) {
                        data = pkcs1Pad(data);
                    }
                    signature = token.p11.C_Sign(session.id(), data);
                }
            }
            if (keyAlgorithm.equals("RSA") == false) {
                return dsaToASN1(signature);
            } else {
                return signature;
            }
        } catch (PKCS11Exception e) {
            throw new ProviderException(e);
        } finally {
            initialized = false;
            session = token.releaseSession(session);
        }
    }
    protected boolean engineVerify(byte[] signature) throws SignatureException {
        ensureInitialized();
        try {
            if (keyAlgorithm.equals("DSA")) {
                signature = asn1ToDSA(signature);
            } else if (keyAlgorithm.equals("EC")) {
                signature = asn1ToECDSA(signature);
            }
            if (type == T_UPDATE) {
                token.p11.C_VerifyFinal(session.id(), signature);
            } else {
                byte[] digest;
                if (type == T_DIGEST) {
                    digest = md.digest();
                } else { 
                    if (mechanism == CKM_DSA) {
                        if (bytesProcessed != buffer.length) {
                            throw new SignatureException
                            ("Data for RawDSA must be exactly 20 bytes long");
                        }
                        digest = buffer;
                    } else {
                        if (bytesProcessed > buffer.length) {
                            throw new SignatureException("Data for NONEwithECDSA"
                            + " must be at most " + RAW_ECDSA_MAX + " bytes long");
                        }
                        digest = new byte[bytesProcessed];
                        System.arraycopy(buffer, 0, digest, 0, bytesProcessed);
                    }
                }
                if (keyAlgorithm.equals("RSA") == false) {
                    token.p11.C_Verify(session.id(), digest, signature);
                } else { 
                    byte[] data = encodeSignature(digest);
                    if (mechanism == CKM_RSA_X_509) {
                        data = pkcs1Pad(data);
                    }
                    token.p11.C_Verify(session.id(), data, signature);
                }
            }
            return true;
        } catch (PKCS11Exception e) {
            long errorCode = e.getErrorCode();
            if (errorCode == CKR_SIGNATURE_INVALID) {
                return false;
            }
            if (errorCode == CKR_SIGNATURE_LEN_RANGE) {
                return false;
            }
            if (errorCode == CKR_DATA_LEN_RANGE) {
                return false;
            }
            throw new ProviderException(e);
        } finally {
            initialized = false;
            session = token.releaseSession(session);
        }
    }
    private byte[] pkcs1Pad(byte[] data) {
        try {
            int len = (p11Key.keyLength() + 7) >> 3;
            RSAPadding padding = RSAPadding.getInstance
                                        (RSAPadding.PAD_BLOCKTYPE_1, len);
            byte[] padded = padding.pad(data);
            return padded;
        } catch (GeneralSecurityException e) {
            throw new ProviderException(e);
        }
    }
    private byte[] encodeSignature(byte[] digest) throws SignatureException {
        try {
            return RSASignature.encodeSignature(digestOID, digest);
        } catch (IOException e) {
            throw new SignatureException("Invalid encoding", e);
        }
    }
    private static byte[] dsaToASN1(byte[] signature) {
        int n = signature.length >> 1;
        BigInteger r = new BigInteger(1, P11Util.subarray(signature, 0, n));
        BigInteger s = new BigInteger(1, P11Util.subarray(signature, n, n));
        try {
            DerOutputStream outseq = new DerOutputStream(100);
            outseq.putInteger(r);
            outseq.putInteger(s);
            DerValue result = new DerValue(DerValue.tag_Sequence,
                                           outseq.toByteArray());
            return result.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Internal error", e);
        }
    }
    private static byte[] asn1ToDSA(byte[] signature) throws SignatureException {
        try {
            DerInputStream in = new DerInputStream(signature);
            DerValue[] values = in.getSequence(2);
            BigInteger r = values[0].getPositiveBigInteger();
            BigInteger s = values[1].getPositiveBigInteger();
            byte[] br = toByteArray(r, 20);
            byte[] bs = toByteArray(s, 20);
            if ((br == null) || (bs == null)) {
                throw new SignatureException("Out of range value for R or S");
            }
            return P11Util.concat(br, bs);
        } catch (SignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureException("invalid encoding for signature", e);
        }
    }
    private byte[] asn1ToECDSA(byte[] signature) throws SignatureException {
        try {
            DerInputStream in = new DerInputStream(signature);
            DerValue[] values = in.getSequence(2);
            BigInteger r = values[0].getPositiveBigInteger();
            BigInteger s = values[1].getPositiveBigInteger();
            byte[] br = P11Util.trimZeroes(r.toByteArray());
            byte[] bs = P11Util.trimZeroes(s.toByteArray());
            int k = Math.max(br.length, bs.length);
            byte[] res = new byte[k << 1];
            System.arraycopy(br, 0, res, k - br.length, br.length);
            System.arraycopy(bs, 0, res, res.length - bs.length, bs.length);
            return res;
        } catch (Exception e) {
            throw new SignatureException("invalid encoding for signature", e);
        }
    }
    private static byte[] toByteArray(BigInteger bi, int len) {
        byte[] b = bi.toByteArray();
        int n = b.length;
        if (n == len) {
            return b;
        }
        if ((n == len + 1) && (b[0] == 0)) {
            byte[] t = new byte[len];
            System.arraycopy(b, 1, t, 0, len);
            return t;
        }
        if (n > len) {
            return null;
        }
        byte[] t = new byte[len];
        System.arraycopy(b, 0, t, (len - n), n);
        return t;
    }
    protected void engineSetParameter(String param, Object value)
            throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }
    protected Object engineGetParameter(String param)
            throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }
}
