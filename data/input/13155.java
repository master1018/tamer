abstract class ECDSASignature extends SignatureSpi {
    private final MessageDigest messageDigest;
    private SecureRandom random;
    private boolean needsReset;
    private ECPrivateKey privateKey;
    private ECPublicKey publicKey;
    ECDSASignature() {
        messageDigest = null;
    }
    ECDSASignature(String digestName) {
        try {
            messageDigest = MessageDigest.getInstance(digestName);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        needsReset = false;
    }
    public static final class Raw extends ECDSASignature {
        private static final int RAW_ECDSA_MAX = 64;
        private final byte[] precomputedDigest;
        private int offset = 0;
        public Raw() {
            precomputedDigest = new byte[RAW_ECDSA_MAX];
        }
        @Override
        protected void engineUpdate(byte b) throws SignatureException {
            if (offset >= precomputedDigest.length) {
                offset = RAW_ECDSA_MAX + 1;
                return;
            }
            precomputedDigest[offset++] = b;
        }
        @Override
        protected void engineUpdate(byte[] b, int off, int len)
                throws SignatureException {
            if (offset >= precomputedDigest.length) {
                offset = RAW_ECDSA_MAX + 1;
                return;
            }
            System.arraycopy(b, off, precomputedDigest, offset, len);
            offset += len;
        }
        @Override
        protected void engineUpdate(ByteBuffer byteBuffer) {
            int len = byteBuffer.remaining();
            if (len <= 0) {
                return;
            }
            if (offset + len >= precomputedDigest.length) {
                offset = RAW_ECDSA_MAX + 1;
                return;
            }
            byteBuffer.get(precomputedDigest, offset, len);
            offset += len;
        }
        @Override
        protected void resetDigest(){
            offset = 0;
        }
        @Override
        protected byte[] getDigestValue() throws SignatureException {
            if (offset > RAW_ECDSA_MAX) {
                throw new SignatureException("Message digest is too long");
            }
            byte[] result = new byte[offset];
            System.arraycopy(precomputedDigest, 0, result, 0, offset);
            offset = 0;
            return result;
        }
    }
    public static final class SHA1 extends ECDSASignature {
        public SHA1() {
            super("SHA1");
        }
    }
    public static final class SHA256 extends ECDSASignature {
        public SHA256() {
            super("SHA-256");
        }
    }
    public static final class SHA384 extends ECDSASignature {
        public SHA384() {
            super("SHA-384");
        }
    }
    public static final class SHA512 extends ECDSASignature {
        public SHA512() {
            super("SHA-512");
        }
    }
    @Override
    protected void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException {
        this.publicKey = (ECPublicKey) ECKeyFactory.toECKey(publicKey);
        this.privateKey = null;
        resetDigest();
    }
    @Override
    protected void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException {
        engineInitSign(privateKey, null);
    }
    @Override
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        this.privateKey = (ECPrivateKey) ECKeyFactory.toECKey(privateKey);
        this.publicKey = null;
        this.random = random;
        resetDigest();
    }
    protected void resetDigest() {
        if (needsReset) {
            if (messageDigest != null) {
                messageDigest.reset();
            }
            needsReset = false;
        }
    }
    protected byte[] getDigestValue() throws SignatureException {
        needsReset = false;
        return messageDigest.digest();
    }
    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        messageDigest.update(b);
        needsReset = true;
    }
    @Override
    protected void engineUpdate(byte[] b, int off, int len)
            throws SignatureException {
        messageDigest.update(b, off, len);
        needsReset = true;
    }
    @Override
    protected void engineUpdate(ByteBuffer byteBuffer) {
        int len = byteBuffer.remaining();
        if (len <= 0) {
            return;
        }
        messageDigest.update(byteBuffer);
        needsReset = true;
    }
    @Override
    protected byte[] engineSign() throws SignatureException {
        byte[] s = privateKey.getS().toByteArray();
        ECParameterSpec params = privateKey.getParams();
        byte[] encodedParams = ECParameters.encodeParameters(params); 
        int keySize = params.getCurve().getField().getFieldSize();
        byte[] seed = new byte[(((keySize + 7) >> 3) + 1) * 2];
        if (random == null) {
            random = JCAUtil.getSecureRandom();
        }
        random.nextBytes(seed);
        try {
            return encodeSignature(
                signDigest(getDigestValue(), s, encodedParams, seed));
        } catch (GeneralSecurityException e) {
            throw new SignatureException("Could not sign data", e);
        }
    }
    @Override
    protected boolean engineVerify(byte[] signature) throws SignatureException {
        byte[] w;
        ECParameterSpec params = publicKey.getParams();
        byte[] encodedParams = ECParameters.encodeParameters(params); 
        if (publicKey instanceof ECPublicKeyImpl) {
            w = ((ECPublicKeyImpl)publicKey).getEncodedPublicValue();
        } else { 
            w = ECParameters.encodePoint(publicKey.getW(), params.getCurve());
        }
        try {
            return verifySignedDigest(
                decodeSignature(signature), getDigestValue(), w, encodedParams);
        } catch (GeneralSecurityException e) {
            throw new SignatureException("Could not verify signature", e);
        }
    }
    @Override
    protected void engineSetParameter(String param, Object value)
            throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }
    @Override
    protected Object engineGetParameter(String param)
            throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }
    private byte[] encodeSignature(byte[] signature) throws SignatureException {
        try {
            int n = signature.length >> 1;
            byte[] bytes = new byte[n];
            System.arraycopy(signature, 0, bytes, 0, n);
            BigInteger r = new BigInteger(1, bytes);
            System.arraycopy(signature, n, bytes, 0, n);
            BigInteger s = new BigInteger(1, bytes);
            DerOutputStream out = new DerOutputStream(signature.length + 10);
            out.putInteger(r);
            out.putInteger(s);
            DerValue result =
                new DerValue(DerValue.tag_Sequence, out.toByteArray());
            return result.toByteArray();
        } catch (Exception e) {
            throw new SignatureException("Could not encode signature", e);
        }
    }
    private byte[] decodeSignature(byte[] signature) throws SignatureException {
        try {
            DerInputStream in = new DerInputStream(signature);
            DerValue[] values = in.getSequence(2);
            BigInteger r = values[0].getPositiveBigInteger();
            BigInteger s = values[1].getPositiveBigInteger();
            byte[] rBytes = trimZeroes(r.toByteArray());
            byte[] sBytes = trimZeroes(s.toByteArray());
            int k = Math.max(rBytes.length, sBytes.length);
            byte[] result = new byte[k << 1];
            System.arraycopy(rBytes, 0, result, k - rBytes.length,
                rBytes.length);
            System.arraycopy(sBytes, 0, result, result.length - sBytes.length,
                sBytes.length);
            return result;
        } catch (Exception e) {
            throw new SignatureException("Could not decode signature", e);
        }
    }
    private static byte[] trimZeroes(byte[] b) {
        int i = 0;
        while ((i < b.length - 1) && (b[i] == 0)) {
            i++;
        }
        if (i == 0) {
            return b;
        }
        byte[] t = new byte[b.length - i];
        System.arraycopy(b, i, t, 0, t.length);
        return t;
    }
    private static native byte[] signDigest(byte[] digest, byte[] s,
        byte[] encodedParams, byte[] seed) throws GeneralSecurityException;
    private static native boolean verifySignedDigest(byte[] signature,
        byte[] digest, byte[] w, byte[] encodedParams)
            throws GeneralSecurityException;
}
