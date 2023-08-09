public class JavaxTrustManagerFactoryImpl extends TrustManagerFactorySpi {
    public JavaxTrustManagerFactoryImpl () {
        System.out.println("JavaxTrustManagerFactoryImpl initialized");
    }
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        System.out.println("JavaxTrustManagerFactoryImpl init'd");
    }
    protected void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
    }
    protected TrustManager[] engineGetTrustManagers() {
        return null;
    }
}
