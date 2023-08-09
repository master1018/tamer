@TestTargetClass(KeyManagerFactory.class)
public class KeyManagerFactory1Test extends TestCase {
    private static final String srvKeyManagerFactory = "KeyManagerFactory";
    private static String defaultAlgorithm = null;
    private static String defaultProviderName = null;
    private static Provider defaultProvider = null;
    private static boolean DEFSupported = false;
    private static final String NotSupportedMsg = "There is no suitable provider for KeyManagerFactory";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static String[] validValues = new String[3];
    static {
        defaultAlgorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (defaultAlgorithm != null) {
            defaultProvider = SpiEngUtils.isSupport(defaultAlgorithm,
                    srvKeyManagerFactory);            
            DEFSupported = (defaultProvider != null);
            defaultProviderName = (DEFSupported ? defaultProvider.getName()
                    : null);
            validValues[0] = defaultAlgorithm;
            validValues[1] = defaultAlgorithm.toUpperCase();
            validValues[2] = defaultAlgorithm.toLowerCase();
        }
    }
    protected KeyManagerFactory[] createKMFac() {
        if (!DEFSupported) {
            fail(defaultAlgorithm + " algorithm is not supported");
            return null;
        }
        KeyManagerFactory[] kMF = new KeyManagerFactory[3];
        try {
            kMF[0] = KeyManagerFactory.getInstance(defaultAlgorithm);
            kMF[1] = KeyManagerFactory.getInstance(defaultAlgorithm,
                    defaultProvider);
            kMF[2] = KeyManagerFactory.getInstance(defaultAlgorithm,
                    defaultProviderName);
            return kMF;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm()
        throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) fail(NotSupportedMsg);
        assertEquals("Incorrect algorithm",
                defaultAlgorithm,
                KeyManagerFactory
                .getInstance(defaultAlgorithm).getAlgorithm());
        assertEquals("Incorrect algorithm",
                defaultAlgorithm,
                KeyManagerFactory
                .getInstance(defaultAlgorithm, defaultProviderName)
                .getAlgorithm());
        assertEquals("Incorrect algorithm",
                defaultAlgorithm,
                KeyManagerFactory.getInstance(defaultAlgorithm, defaultProvider)
                .getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultAlgorithm",
        args = {}
    )
    public void test_getDefaultAlgorithm() {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        String def = KeyManagerFactory.getDefaultAlgorithm();
        if (defaultAlgorithm == null) {
            assertNull("DefaultAlgorithm must be null", def);
        } else {
            assertEquals("Invalid default algorithm", def, defaultAlgorithm);
        }
        String defA = "Proba.keymanagerfactory.defaul.type";
        Security.setProperty("ssl.KeyManagerFactory.algorithm", defA);
        assertEquals("Incorrect defaultAlgorithm", 
                KeyManagerFactory.getDefaultAlgorithm(), defA);
        if (def == null) {
            def = "";
        }
        Security.setProperty("ssl.KeyManagerFactory.algorithm", def); 
        assertEquals("Incorrect defaultAlgorithm", 
                KeyManagerFactory.getDefaultAlgorithm(), def);        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String01() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyManagerFactory keyMF;
        for (int i = 0; i < validValues.length; i++) {
            keyMF = KeyManagerFactory.getInstance(validValues[i]);
            assertNotNull("No KeyManagerFactory created", keyMF);
            assertEquals("Invalid algorithm", keyMF.getAlgorithm(),
                    validValues[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String02() {
        try {
            KeyManagerFactory.getInstance(null);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException was not thrown as expected for algorithm: "
                        .concat(invalidValues[i]));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String01() throws NoSuchProviderException,
            NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(validValues[i], provider);
                fail("Expected IllegalArgumentException was not thrown for null provider");
            } catch (IllegalArgumentException e) {
            }
            try {
                KeyManagerFactory.getInstance(validValues[i], "");
                fail("Expected IllegalArgumentException was not thrown for empty provider");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String02() throws NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        try {
            KeyManagerFactory.getInstance(null, defaultProviderName);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(invalidValues[i],
                        defaultProviderName);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String03()
        throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    KeyManagerFactory.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NuSuchProviderException must be thrown (algorithm: "
                            + validValues[i] + " provider: " + invalidValues[j]
                            + ")");
                } catch (NoSuchProviderException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String04()
        throws NoSuchProviderException,
            NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyManagerFactory kMF;
        for (int i = 0; i < validValues.length; i++) {
            kMF = KeyManagerFactory.getInstance(validValues[i],
                    defaultProviderName);
            assertNotNull("No KeyManagerFactory created", kMF);
            assertEquals("Incorrect algorithm", kMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", kMF.getProvider().getName(),
                    defaultProviderName);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider01()
        throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                KeyManagerFactory.getInstance(validValues[i], provider);
                fail("Expected IllegalArgumentException was not thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider02() {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }        
        try {
            KeyManagerFactory.getInstance(null, defaultProvider);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                KeyManagerFactory
                        .getInstance(invalidValues[i], defaultProvider);
                fail("Expected NuSuchAlgorithmException was not thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        } 
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider03()
        throws NoSuchAlgorithmException,
            IllegalArgumentException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyManagerFactory kMF;
        for (int i = 0; i < validValues.length; i++) {
            kMF = KeyManagerFactory
                    .getInstance(validValues[i], defaultProvider);
            assertNotNull("No KeyManagerFactory created", kMF);
            assertEquals(kMF.getAlgorithm(), validValues[i]);
            assertEquals(kMF.getProvider(), defaultProvider);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyManagerFactory",
        args = {javax.net.ssl.KeyManagerFactorySpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void test_Constructor() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyManagerFactorySpi spi = new MyKeyManagerFactorySpi();
        KeyManagerFactory keyMF = new myKeyManagerFactory(spi, defaultProvider,
                defaultAlgorithm);
        assertEquals("Incorrect algorithm", keyMF.getAlgorithm(),
                defaultAlgorithm);
        assertEquals("Incorrect provider", keyMF.getProvider(), defaultProvider);
        try {
            keyMF.init(null, new char[1]);
            fail("UnrecoverableKeyException must be thrown");
        } catch (UnrecoverableKeyException e) {
        } catch (Exception e) {
            fail("Unexpected: "+e.toString()+" was thrown");
        }
        keyMF = new myKeyManagerFactory(null, null, null);
        assertNull("Aalgorithm must be null", keyMF.getAlgorithm());
        assertNull("Provider must be null", keyMF.getProvider());
        try {
            keyMF.getKeyManagers();
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKeyManagers",
        args = {}
    )
    public void test_getKeyManagers()
        throws Exception {
        if (!DEFSupported) fail(NotSupportedMsg);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(defaultAlgorithm);
        char[] pass = "password".toCharArray();
        kmf.init(null, pass);
        assertNotNull("Key manager array is null", kmf.getKeyManagers());
        assertEquals("Incorrect size of array",
                1, kmf.getKeyManagers().length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider()
        throws Exception {
        if (!DEFSupported) fail(NotSupportedMsg);
        assertEquals("Incorrect provider",
                defaultProvider,
                KeyManagerFactory
                .getInstance(defaultAlgorithm).getProvider());
        assertEquals("Incorrect provider",
                defaultProvider,
                KeyManagerFactory
                .getInstance(defaultAlgorithm, defaultProviderName)
                .getProvider());
        assertEquals("Incorrect provider",
                defaultProvider,
                KeyManagerFactory.getInstance(defaultAlgorithm, defaultProvider)
                .getProvider());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException checking missed",
        method = "init",
        args = {java.security.KeyStore.class, char[].class}
    )
    public void test_initLjava_security_KeyStore$C()
        throws NoSuchAlgorithmException,
        KeyStoreException, UnrecoverableKeyException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyManagerFactory[] keyMF = createKMFac();
        assertNotNull("KeyManagerFactory object were not created", keyMF);
        KeyStore ksNull = null;
        KeyManager[] km;
        for (int i = 0; i < keyMF.length; i++) {
            keyMF[i].init(ksNull, new char[10]);
            km = keyMF[i].getKeyManagers();
            assertNotNull("Result should not be null", km);
            assertTrue("Length of result KeyManager array should not be 0",
                    (km.length > 0));
        }
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
        } catch (KeyStoreException e) {
            fail(e.toString() + "default KeyStore type is not supported");
            return;
        } catch (Exception e) {
            fail("Unexpected: " + e.toString());
            return;
        }
        for (int i = 0; i < keyMF.length; i++) {
            try {
                keyMF[i].init(ks, new char[10]);
            } catch (KeyStoreException e) {              
            }
            km = keyMF[i].getKeyManagers();
            assertNotNull("Result has not be null", km);
            assertTrue("Length of result KeyManager array should not be 0",
                    (km.length > 0));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "functionality is not implemented in org.apache.harmony.xnet.provider.jsse.engineInit(ManagerFactoryParameters)",
        method = "init",
        args = {javax.net.ssl.ManagerFactoryParameters.class}
    )
    public void test_initLjavax_net_ssl_ManagerFactoryParameters()
        throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        ManagerFactoryParameters par = null;
        KeyManagerFactory[] keyMF = createKMFac();
        assertNotNull("KeyManagerFactory object were not created", keyMF);
        for (int i = 0; i < keyMF.length; i++) {
            try {
                keyMF[i].init(par);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
        }
        KeyStore.ProtectionParameter pp = new ProtectionParameterImpl();
        KeyStore.Builder bld = KeyStore.Builder.newInstance("testType", null, pp);
        assertNotNull("Null object KeyStore.Builder", bld);
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(defaultAlgorithm);
            KeyStoreBuilderParameters ksp = new KeyStoreBuilderParameters(bld);
            assertNotNull(ksp.getParameters());
            kmf.init(ksp);
            fail("InvalidAlgorithmParameterException must be thrown");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
}
class myKeyManagerFactory extends KeyManagerFactory {
    public myKeyManagerFactory(KeyManagerFactorySpi spi, Provider prov,
            String alg) {
        super(spi, prov, alg);
    }
}
class ProtectionParameterImpl implements KeyStore.ProtectionParameter {
    ProtectionParameterImpl(){}
}