public abstract class TrustManagerFactorySpi {
    protected abstract void engineInit(KeyStore ks) throws KeyStoreException;
    protected abstract TrustManager[] engineGetTrustManagers();
}
