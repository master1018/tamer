public class KeyStoreCompatibilityMode {
    private static final String KEYSTORE_COMPATIBILITY_MODE_PROP =
        "sun.security.mscapi.keyStoreCompatibilityMode";
    private static boolean mode;
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("sun.security.mscapi.SunMSCAPI");
        } catch (Exception e) {
            System.out.println(
                "The SunMSCAPI provider is not available on this platform: " +
                e);
            return;
        }
        if (args.length > 0 && "-disable".equals(args[0])) {
            mode = false;
        } else {
            mode = true;
        }
        Provider p = Security.getProvider("SunMSCAPI");
        System.out.println("SunMSCAPI provider classname is " +
            p.getClass().getName());
        System.out.println(KEYSTORE_COMPATIBILITY_MODE_PROP + " = " +
            System.getProperty(KEYSTORE_COMPATIBILITY_MODE_PROP));
        KeyStore myKeyStore = KeyStore.getInstance("Windows-MY", p);
        KeyStore myKeyStore2 = KeyStore.getInstance("Windows-MY", p);
        KeyStore rootKeyStore = KeyStore.getInstance("Windows-ROOT", p);
        KeyStore rootKeyStore2 = KeyStore.getInstance("Windows-ROOT", p);
        InputStream inStream = new ByteArrayInputStream(new byte[1]);
        OutputStream outStream = new ByteArrayOutputStream();
        char[] password = new char[1];
        testLoadStore(myKeyStore, null, null, true);
        testLoadStore(myKeyStore2, null, password, true);
        testLoadStore(rootKeyStore, inStream, null, true);
        testLoadStore(rootKeyStore2, inStream, password, true);
        testLoadStore(myKeyStore, null, null, false);
        testLoadStore(myKeyStore2, null, password, false);
        testLoadStore(rootKeyStore, outStream, null, false);
        testLoadStore(rootKeyStore2, outStream, password, false);
    }
    private static void testLoadStore(KeyStore keyStore, Object stream,
        char[] password, boolean doLoad) throws Exception {
        String streamValue = stream == null ? "null" : "non-null";
        String passwordValue = password == null ? "null" : "non-null";
        System.out.println("Checking " + (doLoad ? "load" : "store") +
            "(stream=" + streamValue + ", password=" + passwordValue + ")...");
        try {
            if (doLoad) {
                keyStore.load((InputStream) stream, password);
            } else {
                keyStore.store((OutputStream) stream, password);
            }
            if (!mode && keyStore != null && password != null) {
                throw new Exception(
                    "Expected an IOException to be thrown by KeyStore.load");
            }
        } catch (IOException ioe) {
            if (mode) {
                throw ioe;
            } else {
                System.out.println("caught the expected exception: " + ioe);
            }
        } catch (KeyStoreException kse) {
            if (doLoad) {
                throw kse;
            } else {
                System.out.println("caught the expected exception: " + kse);
            }
        }
    }
}
