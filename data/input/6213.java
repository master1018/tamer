public class ComSSLContextImpl extends SSLContextSpi {
    public ComSSLContextImpl() {
        System.out.println("ComSSLContextImpl initialized");
    }
    protected void engineInit(KeyManager[] km,
           TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
        System.out.println("ComSSLContextImpl init'd");
    }
    protected javax.net.ssl.SSLSocketFactory engineGetSocketFactory() {
        return null;
    }
    protected javax.net.ssl.SSLServerSocketFactory
            engineGetServerSocketFactory() {
        return null;
    }
}
