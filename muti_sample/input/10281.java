public abstract class ExtendedSSLSession implements SSLSession {
    public abstract String[] getLocalSupportedSignatureAlgorithms();
    public abstract String[] getPeerSupportedSignatureAlgorithms();
}
