public class MyKeyManagerFactorySpi extends KeyManagerFactorySpi {
    protected void engineInit(KeyStore ks, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        if (password == null) {
            throw new KeyStoreException("Incorrect password");            
        }
        if (ks == null) {
            throw new UnrecoverableKeyException("Incorrect keystore");
        }
    }
    protected void engineInit(ManagerFactoryParameters spec)
            throws InvalidAlgorithmParameterException {
        if (spec == null) {
            throw new InvalidAlgorithmParameterException("Incorrect parameter");
        }
        if (spec instanceof Parameters) {
            try {
                engineInit(((Parameters)spec).getKeyStore(),
                        ((Parameters)spec).getPassword());
            } catch (Exception e) {
                throw new InvalidAlgorithmParameterException(e.toString()); 
            }
        } else {
            throw new InvalidAlgorithmParameterException("Invalid parameter");
        }
    }
    protected KeyManager[] engineGetKeyManagers() {
        return null;
    }
    public static class Parameters implements ManagerFactoryParameters {
        private KeyStore keyStore;
        private char[] passWD;
        public Parameters (KeyStore ks, char[] pass) {
            this.keyStore = ks;
            this.passWD = pass; 
        }
        public KeyStore getKeyStore() {
            return keyStore;
        }
        public char[] getPassword() {
            return passWD;
        }
    }}
