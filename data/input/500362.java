public class MyTrustManagerFactorySpi extends TrustManagerFactorySpi {
    protected void engineInit(KeyStore ks) throws KeyStoreException {
        if (ks == null) {
            throw new KeyStoreException("Not supported operation for null KeyStore");
        }
    }
    protected void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        if (spec == null) {
            throw new InvalidAlgorithmParameterException("Null parameter");
        }
        if (spec instanceof Parameters) {
            try {
                engineInit(((Parameters)spec).getKeyStore());
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new InvalidAlgorithmParameterException("Invalid parameter");
        }
    }
    protected TrustManager[] engineGetTrustManagers() {
        return null;
    }
    public static class Parameters implements ManagerFactoryParameters {
        private KeyStore keyStore;
        public Parameters (KeyStore ks) {
            this.keyStore = ks;
        }
        public KeyStore getKeyStore() {
            return keyStore;
        }
    }
}
