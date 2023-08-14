@TestTargetClass(TrustManagerFactory.class) 
public class TrustManagerFactory1Test extends TestCase {
    private static final String srvTrustManagerFactory = "TrustManagerFactory";
    private static String defaultAlgorithm = null;
    private static String defaultProviderName = null;
    private static Provider defaultProvider = null;
    private static boolean DEFSupported = false;
    private static final String NotSupportedMsg = "There is no suitable provider for TrustManagerFactory";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static String[] validValues = new String[3];
    static {
        defaultAlgorithm = Security
                .getProperty("ssl.TrustManagerFactory.algorithm");
        if (defaultAlgorithm != null) {
            defaultProvider = SpiEngUtils.isSupport(defaultAlgorithm,
                    srvTrustManagerFactory);
            DEFSupported = (defaultProvider != null);
            defaultProviderName = (DEFSupported ? defaultProvider.getName()
                    : null);
            validValues[0] = defaultAlgorithm;
            validValues[1] = defaultAlgorithm.toUpperCase();
            validValues[2] = defaultAlgorithm.toLowerCase();
        }
    }
    protected TrustManagerFactory[] createTMFac() {
        if (!DEFSupported) {
            fail(defaultAlgorithm + " algorithm is not supported");
            return null;
        }
        TrustManagerFactory[] tMF = new TrustManagerFactory[3];
        try {
            tMF[0] = TrustManagerFactory.getInstance(defaultAlgorithm);
            tMF[1] = TrustManagerFactory.getInstance(defaultAlgorithm,
                    defaultProvider);
            tMF[2] = TrustManagerFactory.getInstance(defaultAlgorithm,
                    defaultProviderName);
            return tMF;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TrustManagerFactory",
        args = {javax.net.ssl.TrustManagerFactorySpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void test_ConstructorLjavax_net_ssl_TrustManagerFactorySpiLjava_security_ProviderLjava_lang_String()
        throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactorySpi spi = new MyTrustManagerFactorySpi(); 
        TrustManagerFactory tmF = new myTrustManagerFactory(spi, defaultProvider,
                defaultAlgorithm);
        assertTrue("Not CertStore object", tmF instanceof TrustManagerFactory);
        assertEquals("Incorrect algorithm", tmF.getAlgorithm(),
                defaultAlgorithm);
        assertEquals("Incorrect provider", tmF.getProvider(), defaultProvider);
        assertNull("Incorrect result", tmF.getTrustManagers());
        tmF = new myTrustManagerFactory(null, null, null);
        assertTrue("Not CertStore object", tmF instanceof TrustManagerFactory);
        assertNull("Provider must be null", tmF.getProvider());
        assertNull("Algorithm must be null", tmF.getAlgorithm());
        try {
            tmF.getTrustManagers();
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
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
                TrustManagerFactory
                .getInstance(defaultAlgorithm).getAlgorithm());
        assertEquals("Incorrect algorithm",
                defaultAlgorithm,
                TrustManagerFactory
                .getInstance(defaultAlgorithm, defaultProviderName)
                .getAlgorithm());
        assertEquals("Incorrect algorithm",
                defaultAlgorithm,
                TrustManagerFactory.getInstance(defaultAlgorithm, defaultProvider)
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
        String def = TrustManagerFactory.getDefaultAlgorithm();
        if (defaultAlgorithm == null) {
            assertNull("DefaultAlgorithm must be null", def);
        } else {
            assertEquals("Invalid default algorithm", def, defaultAlgorithm);
        }
        String defA = "Proba.trustmanagerfactory.defaul.type";
        Security.setProperty("ssl.TrustManagerFactory.algorithm", defA);
        assertEquals("Incorrect defaultAlgorithm", 
                TrustManagerFactory.getDefaultAlgorithm(), defA);
        if (def == null) {
            def = "";
        }
        Security.setProperty("ssl.TrustManagerFactory.algorithm", def); 
        assertEquals("Incorrect defaultAlgorithm", 
                TrustManagerFactory.getDefaultAlgorithm(), def);        
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
        TrustManagerFactory trustMF;
        for (int i = 0; i < validValues.length; i++) {
            trustMF = TrustManagerFactory.getInstance(validValues[i]);
            assertTrue("Not TrustManagerFactory object",
                    trustMF instanceof TrustManagerFactory);
            assertEquals("Invalid algorithm", trustMF.getAlgorithm(),
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
            TrustManagerFactory.getInstance(null);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(invalidValues[i]);
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
                TrustManagerFactory.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                TrustManagerFactory.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty");
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
            TrustManagerFactory.getInstance(null, defaultProviderName);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(invalidValues[i],
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
    public void test_getInstanceLjava_lang_StringLjava_lang_String03() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        for (int i = 1; i < invalidValues.length; i++) {
            for (int j = 0; j < validValues.length; j++) {
                try {
                    TrustManagerFactory.getInstance(validValues[j],
                            invalidValues[i]);
                    fail("NuSuchProviderException must be thrown (algorithm: "
                            .concat(validValues[j]).concat(" provider: ")
                            .concat(invalidValues[i]).concat(")"));
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
    public void test_getInstanceLjava_lang_StringLjava_lang_String04() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactory trustMF;
        for (int i = 0; i < validValues.length; i++) {
            trustMF = TrustManagerFactory.getInstance(validValues[i],
                    defaultProviderName);
            assertTrue("Not TrustManagerFactory object",
                    trustMF instanceof TrustManagerFactory);
            assertEquals("Invalid algorithm", trustMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Invalid provider", trustMF.getProvider(),
                    defaultProvider);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider01() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown  when provider is null");
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
            TrustManagerFactory.getInstance(null, defaultProvider);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (algorithm is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                TrustManagerFactory.getInstance(invalidValues[i],
                        defaultProvider);
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
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider03() throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        TrustManagerFactory trustMF;
        for (int i = 0; i < validValues.length; i++) {
            trustMF = TrustManagerFactory.getInstance(validValues[i],
                    defaultProvider);
            assertTrue("Not TrustManagerFactory object",
                    trustMF instanceof TrustManagerFactory);
            assertEquals("Invalid algorithm", trustMF.getAlgorithm(),
                    validValues[i]);
            assertEquals("Invalid provider", trustMF.getProvider(),
                    defaultProvider);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider()
        throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) fail(NotSupportedMsg);
        assertEquals("Incorrect provider",
                defaultProvider,
                TrustManagerFactory
                .getInstance(defaultAlgorithm).getProvider());
        assertEquals("Incorrect provider",
                defaultProvider,
                TrustManagerFactory
                .getInstance(defaultAlgorithm, defaultProviderName)
                .getProvider());
        assertEquals("Incorrect provider",
                defaultProvider,
                TrustManagerFactory.getInstance(defaultAlgorithm, defaultProvider)
                .getProvider());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTrustManagers",
        args = {}
    )
    public void test_getTrustManagers() {
        try {
            TrustManagerFactory trustMF = TrustManagerFactory.getInstance(defaultAlgorithm);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            trustMF.init(ks);
            TrustManager[] tm = trustMF.getTrustManagers();
            assertNotNull("Result has not be null", tm);
            assertTrue("Length of result TrustManager array should not be 0",
                    (tm.length > 0));
        } catch (Exception ex) {
            fail("Unexpected exception " + ex.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "init",
        args = {java.security.KeyStore.class}
    )
    public void test_initLjava_security_KeyStore_01() {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyStore ksNull = null;
        TrustManagerFactory[] trustMF = createTMFac();
        assertNotNull("TrustManagerFactory objects were not created", trustMF);
        try {
            trustMF[0].init(ksNull);
        } catch (Exception ex) {
            fail(ex + " unexpected exception was thrown for null parameter");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "init",
        args = {java.security.KeyStore.class}
    )
    public void test_initLjava_security_KeyStore_02() throws KeyStoreException {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        KeyStore ks;
        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        TrustManagerFactory[] trustMF = createTMFac();
        assertNotNull("TrustManagerFactory objects were not created", trustMF);
        try {
            trustMF[0].init(ks);
        } catch (Exception ex) {
            fail(ex + " unexpected exception was thrown for not null parameter");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {javax.net.ssl.ManagerFactoryParameters.class}
    )
    @KnownFailure("ManagerFactoryParameters object is not supported " + 
                  "and InvalidAlgorithmParameterException was thrown.")
    public void test_initLjavax_net_ssl_ManagerFactoryParameters() {
        if (!DEFSupported) {
            fail(NotSupportedMsg);
            return;
        }
        ManagerFactoryParameters par = null;
        TrustManagerFactory[] trustMF = createTMFac();
        assertNotNull("TrustManagerFactory objects were not created", trustMF);
        for (int i = 0; i < trustMF.length; i++) {
            try {
                trustMF[i].init(par);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch (InvalidAlgorithmParameterException e) {
            }
        }
        String keyAlg = "DSA";
        String validCaNameRfc2253 = "CN=Test CA," +
                                    "OU=Testing Division," +
                                    "O=Test It All," +
                                    "L=Test Town," +
                                    "ST=Testifornia," +
                                    "C=Testland";
        try {
            KeyStore kStore = KeyStore.getInstance(KeyStore.getDefaultType());
            kStore.load(null, null);     
            PublicKey pk = new TestKeyPair(keyAlg).getPublic();
            TrustAnchor ta = new TrustAnchor(validCaNameRfc2253, pk, getFullEncoding());
            Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
            trustAnchors.add(ta);
            X509CertSelector xcs = new X509CertSelector();
            PKIXBuilderParameters pkixBP = new PKIXBuilderParameters(trustAnchors, xcs);
            CertPathTrustManagerParameters cptmp = new CertPathTrustManagerParameters(pkixBP);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(defaultAlgorithm);
            try {
                tmf.init(cptmp);
            } catch (Exception ex) {
                fail(ex + " was thrown for init(ManagerFactoryParameters spec)");
            }
        } catch (Exception e) {
            fail("Unexpected exception for configuration: " + e);
        }
    }
    private static final byte[] getFullEncoding() {
        return new byte[] {
                (byte)0x30,(byte)0x81,(byte)0x8c,(byte)0xa0,
                (byte)0x44,(byte)0x30,(byte)0x16,(byte)0x86,
                (byte)0x0e,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x66,(byte)0x6f,(byte)0x6f,(byte)0x2e,
                (byte)0x63,(byte)0x6f,(byte)0x6d,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0x30,(byte)0x16,(byte)0x86,
                (byte)0x0e,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x62,(byte)0x61,(byte)0x72,(byte)0x2e,
                (byte)0x63,(byte)0x6f,(byte)0x6d,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0x30,(byte)0x12,(byte)0x86,
                (byte)0x0a,(byte)0x66,(byte)0x69,(byte)0x6c,
                (byte)0x65,(byte)0x3a,(byte)0x2f,(byte)0x2f,
                (byte)0x6d,(byte)0x75,(byte)0x75,(byte)0x80,
                (byte)0x01,(byte)0x00,(byte)0x81,(byte)0x01,
                (byte)0x01,(byte)0xa1,(byte)0x44,(byte)0x30,
                (byte)0x16,(byte)0x86,(byte)0x0e,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x66,(byte)0x6f,
                (byte)0x6f,(byte)0x2e,(byte)0x63,(byte)0x6f,
                (byte)0x6d,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01,(byte)0x30,
                (byte)0x16,(byte)0x86,(byte)0x0e,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x62,(byte)0x61,
                (byte)0x72,(byte)0x2e,(byte)0x63,(byte)0x6f,
                (byte)0x6d,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01,(byte)0x30,
                (byte)0x12,(byte)0x86,(byte)0x0a,(byte)0x68,
                (byte)0x74,(byte)0x74,(byte)0x70,(byte)0x3a,
                (byte)0x2f,(byte)0x2f,(byte)0x6d,(byte)0x75,
                (byte)0x75,(byte)0x80,(byte)0x01,(byte)0x00,
                (byte)0x81,(byte)0x01,(byte)0x01
        };
    }
}
class myTrustManagerFactory extends TrustManagerFactory {
    public myTrustManagerFactory(TrustManagerFactorySpi spi, Provider prov,
            String alg) {
        super(spi, prov, alg);
    }
}
