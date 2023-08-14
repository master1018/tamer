public abstract class X509ExtendedTrustManager implements X509TrustManager {
    protected X509ExtendedTrustManager() {
    }
    public abstract void checkClientTrusted(X509Certificate[] chain,
        String authType, String hostname, String algorithm)
        throws CertificateException;
    public abstract void checkServerTrusted(X509Certificate[] chain,
        String authType, String hostname, String algorithm)
        throws CertificateException;
}
