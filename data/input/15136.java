public abstract class SSLContextSpi {
    protected abstract void engineInit(KeyManager[] ah, TrustManager[] th,
        SecureRandom sr) throws KeyManagementException;
    protected abstract SSLSocketFactory engineGetSocketFactory();
    protected abstract SSLServerSocketFactory engineGetServerSocketFactory();
}
