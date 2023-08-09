public abstract class TrustManagerFactorySpi {
    public TrustManagerFactorySpi() {
        super();
    }
    protected abstract void engineInit(KeyStore ks) throws KeyStoreException;
    protected abstract void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException;
    protected abstract TrustManager[] engineGetTrustManagers();
}
