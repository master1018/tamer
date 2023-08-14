public class ComKeyManagerFactoryImpl extends KeyManagerFactorySpi {
    public ComKeyManagerFactoryImpl() {
        System.out.println("ComKeyManagerFactoryImpl initialized");
    }
    protected void engineInit(KeyStore ks, char [] password)
            throws KeyStoreException {
        System.out.println("ComKeyManagerFactoryImpl init'd");
    }
    protected KeyManager[] engineGetKeyManagers()  {
        return null;
    }
}
