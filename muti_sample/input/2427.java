public class JavaxKeyManagerFactoryImpl extends KeyManagerFactorySpi {
    public JavaxKeyManagerFactoryImpl () {
        System.out.println("JavaxKeyManagerFactoryImpl initialized");
    }
    protected void engineInit(KeyStore ks, char[] passwd)
            throws KeyStoreException {
        System.out.println("JavaxKeyManagerFactoryImpl init'd");
    }
    protected void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
    }
    protected KeyManager[] engineGetKeyManagers() {
        return null;
    }
}
