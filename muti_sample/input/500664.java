public class OpenSSLMessageDigest implements ExtendedDigest {
    private String algorithm;
    private int ctx;
    private byte[] singleByte = new byte[1];
    public static OpenSSLMessageDigest getInstance(String algorithm) {
        return new OpenSSLMessageDigest(algorithm);
    }
    private OpenSSLMessageDigest(String algorithm) {
        this.algorithm = algorithm;
        if ("MD2".equalsIgnoreCase(algorithm) || "1.2.840.113549.2.2"
                .equalsIgnoreCase(algorithm)) {
            throw new RuntimeException(algorithm + " not supported");
        }
        ctx = NativeCrypto.EVP_new();
        try {
            NativeCrypto.EVP_DigestInit(ctx, algorithm.replace("-", "").toLowerCase());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage() + " (" + algorithm + ")");
        }
    }
    public int doFinal(byte[] out, int outOff) {
        int i = NativeCrypto.EVP_DigestFinal(ctx, out, outOff);
        reset();
        return i;
    }
    public String getAlgorithmName() {
        return algorithm;
    }
    public int getDigestSize() {
        return NativeCrypto.EVP_DigestSize(ctx);
    }
    public int getByteLength() {
        return NativeCrypto.EVP_DigestBlockSize(ctx);
    }
    public void reset() {
        NativeCrypto.EVP_DigestInit(ctx, algorithm.replace("-", "").toLowerCase());
    }
    public void update(byte in) {
        singleByte[0] = in;
        NativeCrypto.EVP_DigestUpdate(ctx, singleByte, 0, 1);
    }
    public void update(byte[] in, int inOff, int len) {
        NativeCrypto.EVP_DigestUpdate(ctx, in, inOff, len);
    }
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        NativeCrypto.EVP_free(ctx);
    }
}
