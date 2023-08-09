public abstract class RSASignature extends SignatureSpi {
    private static final int baseLength = 8;
    private final ObjectIdentifier digestOID;
    private final int encodedLength;
    private final MessageDigest md;
    private boolean digestReset;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private RSAPadding padding;
    RSASignature(String algorithm, ObjectIdentifier digestOID, int oidLength) {
        this.digestOID = digestOID;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new ProviderException(e);
        }
        digestReset = true;
        encodedLength = baseLength + oidLength + md.getDigestLength();
    }
    protected void engineInitVerify(PublicKey publicKey)
            throws InvalidKeyException {
        RSAPublicKey rsaKey = (RSAPublicKey)RSAKeyFactory.toRSAKey(publicKey);
        this.privateKey = null;
        this.publicKey = rsaKey;
        initCommon(rsaKey, null);
    }
    protected void engineInitSign(PrivateKey privateKey)
            throws InvalidKeyException {
        engineInitSign(privateKey, null);
    }
    protected void engineInitSign(PrivateKey privateKey, SecureRandom random)
            throws InvalidKeyException {
        RSAPrivateKey rsaKey =
            (RSAPrivateKey)RSAKeyFactory.toRSAKey(privateKey);
        this.privateKey = rsaKey;
        this.publicKey = null;
        initCommon(rsaKey, random);
    }
    private void initCommon(RSAKey rsaKey, SecureRandom random)
            throws InvalidKeyException {
        resetDigest();
        int keySize = RSACore.getByteLength(rsaKey);
        try {
            padding = RSAPadding.getInstance
                (RSAPadding.PAD_BLOCKTYPE_1, keySize, random);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new InvalidKeyException(iape.getMessage());
        }
        int maxDataSize = padding.getMaxDataSize();
        if (encodedLength > maxDataSize) {
            throw new InvalidKeyException
                ("Key is too short for this signature algorithm");
        }
    }
    private void resetDigest() {
        if (digestReset == false) {
            md.reset();
            digestReset = true;
        }
    }
    private byte[] getDigestValue() {
        digestReset = true;
        return md.digest();
    }
    protected void engineUpdate(byte b) throws SignatureException {
        md.update(b);
        digestReset = false;
    }
    protected void engineUpdate(byte[] b, int off, int len)
            throws SignatureException {
        md.update(b, off, len);
        digestReset = false;
    }
    protected void engineUpdate(ByteBuffer b) {
        md.update(b);
        digestReset = false;
    }
    protected byte[] engineSign() throws SignatureException {
        byte[] digest = getDigestValue();
        try {
            byte[] encoded = encodeSignature(digestOID, digest);
            byte[] padded = padding.pad(encoded);
            byte[] encrypted = RSACore.rsa(padded, privateKey);
            return encrypted;
        } catch (GeneralSecurityException e) {
            throw new SignatureException("Could not sign data", e);
        } catch (IOException e) {
            throw new SignatureException("Could not encode data", e);
        }
    }
    protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
        if (sigBytes.length != RSACore.getByteLength(publicKey)) {
            throw new SignatureException("Signature length not correct: got " +
                    sigBytes.length + " but was expecting " +
                    RSACore.getByteLength(publicKey));
        }
        byte[] digest = getDigestValue();
        try {
            byte[] decrypted = RSACore.rsa(sigBytes, publicKey);
            byte[] unpadded = padding.unpad(decrypted);
            byte[] decodedDigest = decodeSignature(digestOID, unpadded);
            return Arrays.equals(digest, decodedDigest);
        } catch (javax.crypto.BadPaddingException e) {
            return false;
        } catch (GeneralSecurityException e) {
            throw new SignatureException("Signature verification failed", e);
        } catch (IOException e) {
            throw new SignatureException("Signature encoding error", e);
        }
    }
    public static byte[] encodeSignature(ObjectIdentifier oid, byte[] digest)
            throws IOException {
        DerOutputStream out = new DerOutputStream();
        new AlgorithmId(oid).encode(out);
        out.putOctetString(digest);
        DerValue result =
            new DerValue(DerValue.tag_Sequence, out.toByteArray());
        return result.toByteArray();
    }
    public static byte[] decodeSignature(ObjectIdentifier oid, byte[] signature)
            throws IOException {
        DerInputStream in = new DerInputStream(signature);
        DerValue[] values = in.getSequence(2);
        if ((values.length != 2) || (in.available() != 0)) {
            throw new IOException("SEQUENCE length error");
        }
        AlgorithmId algId = AlgorithmId.parse(values[0]);
        if (algId.getOID().equals(oid) == false) {
            throw new IOException("ObjectIdentifier mismatch: "
                + algId.getOID());
        }
        if (algId.getEncodedParams() != null) {
            throw new IOException("Unexpected AlgorithmId parameters");
        }
        byte[] digest = values[1].getOctetString();
        return digest;
    }
    protected void engineSetParameter(String param, Object value)
            throws InvalidParameterException {
        throw new UnsupportedOperationException("setParameter() not supported");
    }
    protected Object engineGetParameter(String param)
            throws InvalidParameterException {
        throw new UnsupportedOperationException("getParameter() not supported");
    }
    public static final class MD2withRSA extends RSASignature {
        public MD2withRSA() {
            super("MD2", AlgorithmId.MD2_oid, 10);
        }
    }
    public static final class MD5withRSA extends RSASignature {
        public MD5withRSA() {
            super("MD5", AlgorithmId.MD5_oid, 10);
        }
    }
    public static final class SHA1withRSA extends RSASignature {
        public SHA1withRSA() {
            super("SHA-1", AlgorithmId.SHA_oid, 7);
        }
    }
    public static final class SHA256withRSA extends RSASignature {
        public SHA256withRSA() {
            super("SHA-256", AlgorithmId.SHA256_oid, 11);
        }
    }
    public static final class SHA384withRSA extends RSASignature {
        public SHA384withRSA() {
            super("SHA-384", AlgorithmId.SHA384_oid, 11);
        }
    }
    public static final class SHA512withRSA extends RSASignature {
        public SHA512withRSA() {
            super("SHA-512", AlgorithmId.SHA512_oid, 11);
        }
    }
}
