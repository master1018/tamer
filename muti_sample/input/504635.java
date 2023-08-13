public class DebugKeyProvider {
    public interface IKeyGenOutput {
        public void out(String message);
        public void err(String message);
    }
    private static final String PASSWORD_STRING = "android";
    private static final char[] PASSWORD_CHAR = PASSWORD_STRING.toCharArray();
    private static final String DEBUG_ALIAS = "AndroidDebugKey";
    private static final String CERTIFICATE_DESC = "CN=Android Debug,O=Android,C=US";
    private KeyStore.PrivateKeyEntry mEntry;
    public static class KeytoolException extends Exception {
        private static final long serialVersionUID = 1L;
        private String mJavaHome = null;
        private String mCommandLine = null;
        KeytoolException(String message) {
            super(message);
        }
        KeytoolException(String message, String javaHome, String commandLine) {
            super(message);
            mJavaHome = javaHome;
            mCommandLine = commandLine;
        }
        public String getJavaHome() {
            return mJavaHome;
        }
        public String getCommandLine() {
            return mCommandLine;
        }
    }
    public DebugKeyProvider(String osKeyStorePath, String storeType, IKeyGenOutput output)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableEntryException, IOException, KeytoolException, AndroidLocationException {
        if (osKeyStorePath == null) {
            osKeyStorePath = getDefaultKeyStoreOsPath();
        }
        if (loadKeyEntry(osKeyStorePath, storeType) == false) {
            createNewStore(osKeyStorePath, storeType, output);
        }
    }
    public static String getDefaultKeyStoreOsPath()
            throws KeytoolException, AndroidLocationException {
        String folder = AndroidLocation.getFolder();
        if (folder == null) {
            throw new KeytoolException("Failed to get HOME directory!\n");
        }
        String osKeyStorePath = folder + "debug.keystore";
        return osKeyStorePath;
    }
    public PrivateKey getDebugKey() throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException, UnrecoverableEntryException {
        if (mEntry != null) {
            return mEntry.getPrivateKey();
        }
        return null;
    }
    public Certificate getCertificate() throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException, UnrecoverableEntryException {
        if (mEntry != null) {
            return mEntry.getCertificate();
        }
        return null;
    }
    private boolean loadKeyEntry(String osKeyStorePath, String storeType) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException,
            UnrecoverableEntryException {
        try {
            KeyStore keyStore = KeyStore.getInstance(
                    storeType != null ? storeType : KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(osKeyStorePath);
            keyStore.load(fis, PASSWORD_CHAR);
            fis.close();
            mEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(
                    DEBUG_ALIAS, new KeyStore.PasswordProtection(PASSWORD_CHAR));
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }
    private void createNewStore(String osKeyStorePath, String storeType, IKeyGenOutput output)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableEntryException, IOException, KeytoolException {
        if (KeystoreHelper.createNewStore(osKeyStorePath, storeType, PASSWORD_STRING, DEBUG_ALIAS,
                PASSWORD_STRING, CERTIFICATE_DESC, 1 , output)) {
            loadKeyEntry(osKeyStorePath, storeType);
        }
    }
}
