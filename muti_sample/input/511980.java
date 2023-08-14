public class OpenSSLMessageDigestJDK extends MessageDigest {
    private int ctx;
    private byte[] singleByte = new byte[1];
    public static OpenSSLMessageDigestJDK getInstance(String algorithm) throws NoSuchAlgorithmException{
        return new OpenSSLMessageDigestJDK(algorithm);
    }
    private OpenSSLMessageDigestJDK(String algorithm) throws NoSuchAlgorithmException {
        super(algorithm);
        if ("MD2".equalsIgnoreCase(algorithm) || "1.2.840.113549.2.2"
                .equalsIgnoreCase(algorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        ctx = NativeCrypto.EVP_new();
        try {
            NativeCrypto.EVP_DigestInit(ctx, getAlgorithm().replace("-", "").toLowerCase());
        } catch (Exception ex) {
            throw new NoSuchAlgorithmException(ex.getMessage() + " (" + algorithm + ")");
        }
    }
    @Override
    protected byte[] engineDigest() {
        byte[] result = new byte[NativeCrypto.EVP_DigestSize(ctx)];
        NativeCrypto.EVP_DigestFinal(ctx, result, 0);
        engineReset();
        return result;
    }
    @Override
    protected void engineReset() {
        NativeCrypto.EVP_DigestInit(ctx, getAlgorithm().replace("-", "").toLowerCase());
    }
    @Override
    protected int engineGetDigestLength() {
        return NativeCrypto.EVP_DigestSize(ctx);
    }
    @Override
    protected void engineUpdate(byte input) {
        singleByte[0] = input;
        engineUpdate(singleByte, 0, 1);
    }
    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        NativeCrypto.EVP_DigestUpdate(ctx, input, offset, len);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        NativeCrypto.EVP_free(ctx);
    }
    static public class MD5 extends OpenSSLMessageDigestJDK {
        public MD5() throws NoSuchAlgorithmException {
            super("MD5");
        }
    }
    static public class SHA1 extends OpenSSLMessageDigestJDK {
        public SHA1() throws NoSuchAlgorithmException {
            super("SHA-1");
        }
    }
    static public class SHA224 extends OpenSSLMessageDigestJDK {
        public SHA224() throws NoSuchAlgorithmException {
            super("SHA-224");
        }
    }
    static public class SHA256 extends OpenSSLMessageDigestJDK {
        public SHA256() throws NoSuchAlgorithmException {
            super("SHA-256");
        }
    }
}
