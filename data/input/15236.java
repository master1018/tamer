public abstract class KeyManagerFactorySpi {
    protected abstract void engineInit(KeyStore ks, char[] password)
        throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException;
    protected abstract KeyManager[] engineGetKeyManagers();
}
