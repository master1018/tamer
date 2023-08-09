public class MySSLContextImpl extends SSLContextSpi {
    private static MySSLServerSocketFacImpl ssf =
        new MySSLServerSocketFacImpl();
    private static MySSLSocketFacImpl sf = new MySSLSocketFacImpl();
    private static MySSLEngineImpl se1 = new MySSLEngineImpl();
    private static MySSLEngineImpl se2 = new MySSLEngineImpl("schnauzer", 1024);
    public static void useStandardCipherSuites() {
        MySSLServerSocketFacImpl.useStandardCipherSuites();
        MySSLSocketFacImpl.useStandardCipherSuites();
        MySSLEngineImpl.useStandardCipherSuites();
    }
    public static void useCustomCipherSuites() {
        MySSLServerSocketFacImpl.useCustomCipherSuites();
        MySSLSocketFacImpl.useCustomCipherSuites();
        MySSLEngineImpl.useCustomCipherSuites();
    }
    protected SSLSessionContext engineGetClientSessionContext() {
        return null;
    }
    protected SSLSessionContext engineGetServerSessionContext() {
        return null;
    }
    protected SSLServerSocketFactory engineGetServerSocketFactory() {
        return ssf;
    }
    protected SSLSocketFactory engineGetSocketFactory() {
        return sf;
    }
    protected SSLEngine engineCreateSSLEngine() {
        return se1;
    }
    protected SSLEngine engineCreateSSLEngine(String host, int port) {
        return se2;
    }
    protected void engineInit(KeyManager[] km, TrustManager[] tm,
                              SecureRandom sr) throws KeyManagementException {}
}
