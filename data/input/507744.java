public abstract class KeyManagerFactorySpi {
    public KeyManagerFactorySpi() {
        super();
    }
    protected abstract void engineInit(KeyStore ks, char[] password) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableKeyException;
    protected abstract void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException;
    protected abstract KeyManager[] engineGetKeyManagers();
}
