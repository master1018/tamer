@TestTargetClass(KeyManagerFactory.class) 
public class KeyManagerFactory2Test extends TestCase {
    private static final String srvKeyManagerFactory = "KeyManagerFactory";
    private static final String defaultAlg = "KeyMF";
    private static final String KeyManagerFactoryProviderClass = "org.apache.harmony.xnet.tests.support.MyKeyManagerFactorySpi";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static final String[] validValues;
    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "Keymf";
        validValues[3] = "kEYMF";
    }
    Provider mProv;
    protected void setUp() throws Exception {
        super.setUp();
        mProv = (new SpiEngUtils()).new MyProvider("MyKMFProvider",
                "Provider for testing", srvKeyManagerFactory.concat(".")
                        .concat(defaultAlg), KeyManagerFactoryProviderClass);
        Security.insertProviderAt(mProv, 2);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }
    private void checkResult(KeyManagerFactory keyMF)
        throws Exception {
        KeyStore kStore = null;
        ManagerFactoryParameters mfp = null;
        char[] pass = { 'a', 'b', 'c' };
        try {
            keyMF.init(kStore, null);
            fail("KeyStoreException must be thrown");
        } catch (KeyStoreException e) {
        }
        try {
            keyMF.init(kStore, pass);
            fail("UnrecoverableKeyException must be thrown");
        } catch (UnrecoverableKeyException e) {
        }
        try {
            keyMF.init(mfp);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
        assertNull("getKeyManagers() should return null object", keyMF
                .getKeyManagers());
        try {
            kStore = KeyStore.getInstance(KeyStore.getDefaultType());
            kStore.load(null, null);            
        } catch (KeyStoreException e) {
            fail("default keystore is not supported");
            return;
        }
        keyMF.init(kStore, pass);
        mfp = new MyKeyManagerFactorySpi.Parameters(kStore, null);
        try {
            keyMF.init(mfp);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
        mfp = new MyKeyManagerFactorySpi.Parameters(kStore, pass);
        keyMF.init(mfp);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws Exception {
        try {
            KeyManagerFactory.getInstance(null);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        KeyManagerFactory keyMF;
        for (int i = 0; i < validValues.length; i++) {
            keyMF = KeyManagerFactory.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", keyMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", keyMF.getProvider(), mProv);
            checkResult(keyMF);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String()
        throws Exception
    {
        try {
            KeyManagerFactory.getInstance(null, mProv.getName());
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyManagerFactory
                        .getInstance(invalidValues[i], mProv.getName());
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
            try {
                KeyManagerFactory.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    KeyManagerFactory.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(invalidValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        KeyManagerFactory keyMF;
        for (int i = 0; i < validValues.length; i++) {
            keyMF = KeyManagerFactory.getInstance(validValues[i], mProv
                    .getName());
            assertEquals("Incorrect algorithm", keyMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", keyMF.getProvider().getName(),
                    mProv.getName());
            checkResult(keyMF);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider()
        throws Exception
    {
        try {
            KeyManagerFactory.getInstance(null, mProv);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        KeyManagerFactory keyMF;
        for (int i = 0; i < validValues.length; i++) {
            keyMF = KeyManagerFactory.getInstance(validValues[i], mProv);
            assertEquals("Incorrect algorithm", keyMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", keyMF.getProvider(), mProv);
            checkResult(keyMF);
       }
    }
}