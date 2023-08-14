public class NativeCrypto {
    static {
        OpenSSLSocketImpl.class.getClass();
    }
    public static native int EVP_PKEY_new_DSA(byte[] p, byte[] q, byte[] g, byte[] priv_key, byte[] pub_key);
    public static native int EVP_PKEY_new_RSA(byte[] n, byte[] e, byte[] d, byte[] p, byte[] q);
    public static native void EVP_PKEY_free(int pkey);
    public static native int EVP_new();
    public static native void EVP_free(int ctx);
    public static native void EVP_DigestInit(int ctx, String algorithm);
    public static native void EVP_DigestUpdate(int ctx, byte[] buffer, int offset, int length);
    public static native int EVP_DigestFinal(int ctx, byte[] hash, int offset);
    public static native int EVP_DigestSize(int ctx);
    public static native int EVP_DigestBlockSize(int ctx);
    public static native void EVP_VerifyInit(int ctx, String algorithm);
    public static native void EVP_VerifyUpdate(int ctx, byte[] buffer, int offset, int length);
    public static native int EVP_VerifyFinal(int ctx, byte[] signature, int offset, int length, int key);
}
