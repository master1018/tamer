public class ComTrustManagerFactoryImpl extends TrustManagerFactorySpi {
    public ComTrustManagerFactoryImpl() {
        System.out.println("ComTrustManagerFactoryImpl initialized");
    }
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        System.out.println("ComTrustManagerFactoryImpl init'd");
    }
    protected TrustManager[] engineGetTrustManagers()  {
        return null;
    }
}
