public class JavaxSSLContextImpl extends SSLContextSpi {
    public JavaxSSLContextImpl() {
        System.out.println("JavaxSSLContextImpl initialized");
    }
    protected void engineInit(KeyManager[] km,
           TrustManager[] tm, SecureRandom sr) throws KeyManagementException {
        System.out.println("JavaxSSLContextImpl init'd");
    }
    protected SSLEngine engineCreateSSLEngine() {
        return null;
    }
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        return null;
    }
    protected SSLSocketFactory engineGetSocketFactory() {
        return null;
    }
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        return null;
    }
    protected SSLSessionContext engineGetServerSessionContext() {
        return null;
    }
    protected SSLSessionContext engineGetClientSessionContext() {
        return null;
    }
}
