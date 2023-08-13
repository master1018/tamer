public class KeyStoreBuilder {
    private final static String DIR = System.getProperty("test.src", ".");
    private static final char[] password = "passphrase".toCharArray();
    private static final char[] wrongPassword = "wrong".toCharArray();
    public static void main(String[] args) throws Exception {
        File KSFILE = new File(DIR, "keystore.jks");
        KeyStore ks;
        String alias = "vajra";
        Entry entry = null;
        Builder builder;
        builder = Builder.newInstance("JKS", null, KSFILE, new PasswordProtection(password));
        ks = builder.getKeyStore();
        System.out.println("-KeyStore: " + ks.size());
        entry = ks.getEntry(alias, builder.getProtectionParameter(alias));
        showEntry(entry);
        builder = Builder.newInstance("JKS", Security.getProvider("Sun"), KSFILE,
                new CallbackHandlerProtection(new DummyHandler()));
        ks = builder.getKeyStore();
        System.out.println("-KeyStore: " + ks.size());
        entry = ks.getEntry(alias, builder.getProtectionParameter(alias));
        showEntry(entry);
        builder = Builder.newInstance("JKS", null, new PasswordProtection(password));
        ks = builder.getKeyStore();
        int k = ks.size();
        System.out.println("-KeyStore: " + k);
        if (k != 0) {
            throw new Exception("Size not zero: " + k);
        }
        DummyHandler handler = new DummyHandler();
        handler.useWrongPassword = 2;
        builder = Builder.newInstance("JKS", null, KSFILE, new CallbackHandlerProtection(handler));
        ks = builder.getKeyStore();
        System.out.println("-KeyStore: " + ks.size());
        entry = ks.getEntry(alias, builder.getProtectionParameter(alias));
        showEntry(entry);
        handler.useWrongPassword = 3;
        builder = Builder.newInstance("JKS", null, KSFILE, new CallbackHandlerProtection(handler));
        try {
            ks = builder.getKeyStore();
            throw new Exception("should not succeed");
        } catch (KeyStoreException e) {
            System.out.println(e);
        }
        try {
            ks = builder.getKeyStore();
            throw new Exception("should not succeed");
        } catch (KeyStoreException e) {
            System.out.println(e);
        }
        Provider p = new MyProvider();
        handler.useWrongPassword = 2;
        builder = Builder.newInstance("My", p, new CallbackHandlerProtection(handler));
        ks = builder.getKeyStore();
        k = ks.size();
        System.out.println("-KeyStore: " + k);
        if (k != 0) {
            throw new Exception("Size not zero: " + k);
        }
        handler.useWrongPassword = 3;
        builder = Builder.newInstance("My", p, new CallbackHandlerProtection(handler));
        try {
            ks = builder.getKeyStore();
            throw new Exception("should not succeed");
        } catch (KeyStoreException e) {
            System.out.println(e);
        }
        try {
            ks = builder.getKeyStore();
            throw new Exception("should not succeed");
        } catch (KeyStoreException e) {
            System.out.println(e);
        }
        System.out.println("-OK");
    }
    private static void showEntry(Entry entry) {
        PrivateKeyEntry pke = (PrivateKeyEntry)entry;
        X509Certificate cert = (X509Certificate)pke.getCertificate();
        System.out.println("subject: " + cert.getSubjectX500Principal());
    }
    private static class DummyHandler implements CallbackHandler {
        int useWrongPassword;
        public void handle(Callback[] callbacks)
                throws IOException, UnsupportedCallbackException {
            System.out.println("** Callbackhandler invoked");
            for (int i = 0; i < callbacks.length; i++) {
                Callback cb = callbacks[i];
                if (cb instanceof PasswordCallback) {
                    System.out.println("Found PasswordCallback");
                    PasswordCallback pcb = (PasswordCallback)cb;
                    if (useWrongPassword == 0) {
                        pcb.setPassword(password);
                    } else {
                        pcb.setPassword(wrongPassword);
                        useWrongPassword--;
                    }
                    break;
                }
            }
        }
    }
    private static class BaseKeyStoreSpi extends KeyStoreSpi {
        public Key engineGetKey(String alias, char[] password) {
            return null;
        }
        public Certificate[] engineGetCertificateChain(String alias) {
            return null;
        }
        public Certificate engineGetCertificate(String alias) {
            return null;
        }
        public Date engineGetCreationDate(String alias) {
            return null;
        }
        public void engineSetKeyEntry(String alias, Key key, char[] password, Certificate[] certs) {
        }
        public void engineSetKeyEntry(String alias, byte[] key, Certificate[] certs) {
        }
        public void engineSetCertificateEntry(String alias, Certificate cert) {
        }
        public void engineDeleteEntry(String alias) {
        }
        public Enumeration<String> engineAliases() {
            return new Vector<String>().elements();
        }
        public boolean engineContainsAlias(String alias) {
            return false;
        }
        public int engineSize() {
            return 0;
        }
        public boolean engineIsKeyEntry(String alias) {
            return false;
        }
        public boolean engineIsCertificateEntry(String alias) {
            return false;
        }
        public String engineGetCertificateAlias(Certificate cert) {
            return null;
        }
        public void engineStore(OutputStream stream, char[] password) {
        }
        public void engineLoad(InputStream stream, char[] password) throws IOException {
        }
    }
    public static class MyKeyStoreSpi extends BaseKeyStoreSpi {
        public void engineLoad(InputStream stream, char[] pw) throws IOException {
            if (Arrays.equals(password, pw) == false) {
                Throwable t = new UnrecoverableKeyException("Wrong password: " + new String(pw));
                throw (IOException)new IOException("load() failed").initCause(t);
            }
        }
    }
    private static class MyProvider extends Provider {
        MyProvider() {
            super("My", 1.0d, null);
            put("KeyStore.My", "KeyStoreBuilder$MyKeyStoreSpi");
        }
    }
}
