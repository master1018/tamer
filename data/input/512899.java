public class SSLUtils {
    private static SSLSocketFactory sInsecureFactory;
    private static SSLSocketFactory sSecureFactory;
    public synchronized static final SSLSocketFactory getSSLSocketFactory(boolean insecure) {
        if (insecure) {
            if (sInsecureFactory == null) {
                sInsecureFactory = SSLCertificateSocketFactory.getInsecure(0, null);
            }
            return sInsecureFactory;
        } else {
            if (sSecureFactory == null) {
                sSecureFactory = SSLCertificateSocketFactory.getDefault(0, null);
            }
            return sSecureFactory;
        }
    }
}
