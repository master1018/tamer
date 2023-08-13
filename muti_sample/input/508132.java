@TestTargetClass(SSLContext.class) 
public class SSLContext1Test extends TestCase {
    private static String srvSSLContext = "SSLContext";
    public static String defaultProtocol = "TLS";
    private static final String NotSupportMsg = "Default protocol is not supported";
    private static String defaultProviderName = null;
    private static Provider defaultProvider = null;
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static boolean DEFSupported = false;
    private static String[] validValues = new String[3];
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultProtocol, srvSSLContext);
        DEFSupported = (defaultProvider != null);
        if (DEFSupported) {
            defaultProviderName = (DEFSupported ? defaultProvider.getName()
                    : null);
            validValues[0] = defaultProtocol;
            validValues[1] = defaultProtocol.toUpperCase();
            validValues[2] = defaultProtocol.toLowerCase();
        } else {
            defaultProtocol = null;
        }
    }
    protected SSLContext[] createSSLCon() {
        if (!DEFSupported) {
            fail(defaultProtocol + " protocol is not supported");
            return null;
        }
        SSLContext[] sslC = new SSLContext[3];
        try {
            sslC[0] = SSLContext.getInstance(defaultProtocol);
            sslC[1] = SSLContext.getInstance(defaultProtocol, defaultProvider);
            sslC[2] = SSLContext.getInstance(defaultProtocol,
                    defaultProviderName);
            return sslC;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLContext",
        args = {javax.net.ssl.SSLContextSpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void test_ConstructorLjavax_net_ssl_SSLContextSpiLjava_security_ProviderLjava_lang_String()
        throws NoSuchAlgorithmException,
            KeyManagementException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        SSLContextSpi spi = new MySSLContextSpi();
        SSLContext sslContext = new MySslContext(spi, defaultProvider,
                defaultProtocol);
        assertEquals("Incorrect protocol", defaultProtocol,
                sslContext.getProtocol());
        assertEquals("Incorrect provider", defaultProvider,
                sslContext.getProvider());
        TrustManager[] tm = null;
        KeyManager[] km = null;
        sslContext.init(km, tm, new SecureRandom());
        assertNotNull("No SSLEngine created",
                sslContext.createSSLEngine());
        assertNotNull("No SSLEngine created",
                sslContext.createSSLEngine("host", 8888));
        try {
            sslContext.init(km, tm, null);
            fail("KeyManagementException should be thrown for null "
                    + "SecureRandom");
        } catch (KeyManagementException e) {
        }
        sslContext = new MySslContext(null, null, null);
        assertNull("Incorrect protocol", sslContext.getProtocol());
        assertNull("Incorrect provider", sslContext.getProvider());
        try {
            sslContext.createSSLEngine();
            fail("NullPointerException should be thrown");
        } catch (NullPointerException e) {
        }
        try {
            sslContext.getSocketFactory();
            fail("NullPointerException should be thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "UnsupportedOperationException checking missed",
        method = "createSSLEngine",
        args = {}
    )
    public void test_createSSLEngine() throws KeyManagementException {
        if (!DEFSupported) fail(NotSupportMsg);
        SSLContextSpi spi = new MySSLContextSpi();
        SSLContext sslContext = new MySslContext(spi, defaultProvider,
                defaultProtocol);
        sslContext.init(null, null, new SecureRandom());        
        SSLEngine sslEngine = sslContext.createSSLEngine();
        assertNotNull("SSL engine is null", sslEngine);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "UnsupportedOperationException checking missed",
        method = "createSSLEngine",
        args = {java.lang.String.class, int.class}
    )
    public void test_createSSLEngineLjava_lang_StringI()
        throws KeyManagementException {
        if (!DEFSupported) fail(NotSupportMsg);
        SSLContextSpi spi = new MySSLContextSpi();
        SSLContext sslContext = new MySslContext(spi, defaultProvider,
                defaultProtocol);
        sslContext.init(null, null, new SecureRandom());        
        SSLEngine sslEngine = sslContext.createSSLEngine("www.fortify.net", 80);
        assertNotNull("SSL engine is null", sslEngine);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getClientSessionContext",
        args = {}
    )
    public void test_getClientSessionContext() throws NoSuchAlgorithmException, KeyManagementException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        SSLContext[] sslC = createSSLCon();
        assertNotNull("SSLContext objects were not created", sslC);
        for (int i = 0; i < sslC.length; i++) {
            sslC[i].init(null, null, null);
            assertNotNull("Client session is incorrectly instantiated: " + i, 
                    sslC[i].getClientSessionContext());
            assertNotNull("Server session is incorrectly instantiated: " + i, 
                    sslC[i].getServerSessionContext());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String01()
            throws NoSuchAlgorithmException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        SSLContext sslContext;
        for (int i = 0; i < validValues.length; i++) {
            sslContext = SSLContext.getInstance(validValues[i]);
            assertNotNull("No SSLContext created", sslContext);
            assertEquals("Invalid protocol", validValues[i],
                    sslContext.getProtocol());
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
            SSLContext.getInstance(null);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (protocol is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SSLContext.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException was not thrown as expected for provider: "
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
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                SSLContext.getInstance(defaultProtocol, provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                SSLContext.getInstance(defaultProtocol, "");
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
            fail(NotSupportMsg);
            return;
        }
        try {
            SSLContext.getInstance(null, defaultProviderName);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (protocol is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SSLContext.getInstance(invalidValues[i], defaultProviderName);
                fail("NoSuchAlgorithmException was not thrown as expected (protocol: "
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
            fail(NotSupportMsg);
            return;
        }
        for (int i = 1; i < invalidValues.length; i++) {
            for (int j = 0; j < validValues.length; j++) {
                try {
                    SSLContext.getInstance(validValues[j], invalidValues[i]);
                    fail("NuSuchProviderException must be thrown (protocol: "
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
            fail(NotSupportMsg);
            return;
        }
        SSLContext sslContext;
        for (int i = 0; i < validValues.length; i++) {
            sslContext = SSLContext.getInstance(validValues[i],
                    defaultProviderName);
            assertNotNull("Not SSLContext created", sslContext);
            assertEquals("Invalid protocol",
                    validValues[i], sslContext.getProtocol());
            assertEquals("Invalid provider",
                    defaultProvider, sslContext.getProvider());
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
            fail(NotSupportMsg);
            return;
        }
        Provider provider = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                SSLContext.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
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
            fail(NotSupportMsg);
            return;
        }
        try {
            SSLContext.getInstance(null, defaultProvider);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (protocol is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SSLContext.getInstance(invalidValues[i], defaultProvider);
                fail("Expected NoSuchAlgorithmException was not thrown as expected");
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
            fail(NotSupportMsg);
            return;
        }
        SSLContext sslContext;
        for (int i = 0; i < validValues.length; i++) {
            sslContext = SSLContext
                    .getInstance(validValues[i], defaultProvider);
            assertNotNull("Not SSLContext created", sslContext);
            assertEquals("Invalid protocol", validValues[i], sslContext.getProtocol());
            assertEquals("Invalid provider", defaultProvider, sslContext.getProvider());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProtocol",
        args = {}
    )
    public void test_getProtocol()
        throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) fail(NotSupportMsg);
        SSLContextSpi spi = new MySSLContextSpi();
        SSLContext sslContext = new MySslContext(spi, defaultProvider,
                defaultProtocol);
        assertEquals("Incorrect protocol",
                defaultProtocol, sslContext.getProtocol());
        sslContext = new MySslContext(spi, defaultProvider,
                null);
        assertNull("Incorrect protocol", sslContext.getProtocol());
        sslContext = SSLContext.getInstance(defaultProtocol);
        assertEquals("Incorrect protocol",
                defaultProtocol, sslContext.getProtocol());
        sslContext = SSLContext.getInstance(defaultProtocol, defaultProvider);
        assertEquals("Incorrect protocol",
                defaultProtocol, sslContext.getProtocol());
        sslContext = SSLContext.getInstance(defaultProtocol, defaultProviderName);
        assertEquals("Incorrect protocol",
                defaultProtocol, sslContext.getProtocol());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() 
        throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DEFSupported) fail(NotSupportMsg);
        SSLContextSpi spi = new MySSLContextSpi();
        SSLContext sslContext = new MySslContext(spi, defaultProvider,
                defaultProtocol);
        assertEquals("Incorrect provider",
                defaultProvider, sslContext.getProvider());
        sslContext = SSLContext.getInstance(defaultProtocol, defaultProvider);
        assertEquals("Incorrect provider",
                defaultProvider, sslContext.getProvider());
        sslContext = SSLContext.getInstance(defaultProtocol, defaultProviderName);
        assertEquals("Incorrect provider",
                defaultProvider, sslContext.getProvider());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getServerSessionContext",
        args = {}
    )
    public void test_getServerSessionContext() throws NoSuchAlgorithmException,
        KeyManagementException, KeyStoreException,
        UnrecoverableKeyException {
        if (!DEFSupported) fail(NotSupportMsg);
        SSLContext[] sslC = createSSLCon();
        assertNotNull("SSLContext objects were not created", sslC);
        String tAlg = TrustManagerFactory.getDefaultAlgorithm();
        String kAlg = KeyManagerFactory.getDefaultAlgorithm();
        if (tAlg == null)
            fail("TrustManagerFactory default algorithm is not defined");
        if (kAlg == null)
            fail("KeyManagerFactory default algorithm is not defined");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(kAlg);
        kmf.init(null, new char[11]);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tAlg);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        tmf.init(ks);
        TrustManager[] tms = tmf.getTrustManagers();
        for (SSLContext sslCi : sslC) {
            sslCi.init(kmf.getKeyManagers(), tms, new SecureRandom());
            assertNotNull("Server context is incorrectly instantiated", sslCi
                    .getServerSessionContext());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getServerSocketFactory",
        args = {}
    )
     public void test_getServerSocketFactory() throws NoSuchAlgorithmException,
            KeyManagementException, KeyStoreException,
            UnrecoverableKeyException {
        if (!DEFSupported) {
            fail(NotSupportMsg);
            return;
        }
        SSLContext[] sslC = createSSLCon();
        assertNotNull("SSLContext objects were not created", sslC);
        String tAlg = TrustManagerFactory.getDefaultAlgorithm();
        String kAlg = KeyManagerFactory.getDefaultAlgorithm();
        if (tAlg == null) {
            fail("TrustManagerFactory default algorithm is not defined");
            return;
        }
        if (kAlg == null) {
            fail("KeyManagerFactory default algorithm is not defined");
            return;
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(kAlg);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try {
            ks.load(null, null);
        } catch (Exception e) {
            fail(e + " was thrown for method load(null, null)");
        }
        kmf.init(ks, new char[10]);
        KeyManager[] kms = kmf.getKeyManagers();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tAlg);
        tmf.init(ks);
        TrustManager[] tms = tmf.getTrustManagers();
        for (int i = 0; i < sslC.length; i++) {
            sslC[i].init(kms, tms, new SecureRandom());
            assertNotNull("No SSLServerSocketFactory available",
                    sslC[i].getServerSocketFactory());
            assertNotNull("No SSLSocketFactory available",
                    sslC[i].getSocketFactory());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSocketFactory",
        args = {}
    )
     public void test_getSocketFactory() throws NoSuchAlgorithmException,
         KeyManagementException, KeyStoreException,
         UnrecoverableKeyException {
         if (!DEFSupported) fail(NotSupportMsg);
         SSLContext[] sslC = createSSLCon();
         assertNotNull("SSLContext objects were not created", sslC);
         String tAlg = TrustManagerFactory.getDefaultAlgorithm();
         String kAlg = KeyManagerFactory.getDefaultAlgorithm();
         if (tAlg == null)
             fail("TrustManagerFactory default algorithm is not defined");
         if (kAlg == null)
             fail("KeyManagerFactory default algorithm is not defined");
         KeyManagerFactory kmf = KeyManagerFactory.getInstance(kAlg);
         kmf.init(null, new char[11]);
         TrustManagerFactory tmf = TrustManagerFactory.getInstance(tAlg);
         KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
         tmf.init(ks);
         TrustManager[] tms = tmf.getTrustManagers();
         for (SSLContext sslCi : sslC) {
             sslCi.init(kmf.getKeyManagers(), tms, new SecureRandom());
             assertNotNull("Socket factory is incorrectly instantiated",
                     sslCi.getSocketFactory());
         }
     }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {javax.net.ssl.KeyManager[].class, javax.net.ssl.TrustManager[].class, java.security.SecureRandom.class}
    )
     public void test_init$Ljavax_net_ssl_KeyManager$Ljavax_net_ssl_TrustManagerLjava_security_SecureRandom()
         throws Exception {
         if (!DEFSupported) fail(NotSupportMsg);
         SSLContextSpi spi = new MySSLContextSpi();
         SSLContext sslContext = new MySslContext(spi, defaultProvider,
                 defaultProtocol);
         try {
             sslContext.createSSLEngine();
             fail("Expected RuntimeException was not thrown");
         } catch (RuntimeException rte) {
         }
         try {
             sslContext.init(null, null, null);
             fail("KeyManagementException wasn't thrown");
         } catch (KeyManagementException kme) {
         }
         try {
             String tAlg = TrustManagerFactory.getDefaultAlgorithm();
             String kAlg = KeyManagerFactory.getDefaultAlgorithm();
             if (tAlg == null)
                 fail("TrustManagerFactory default algorithm is not defined");
             if (kAlg == null)
                 fail("KeyManagerFactory default algorithm is not defined");
             KeyManagerFactory kmf = KeyManagerFactory.getInstance(kAlg);
             kmf.init(null, new char[11]);
             TrustManagerFactory tmf = TrustManagerFactory.getInstance(tAlg);
             KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
             tmf.init(ks);
             TrustManager[] tms = tmf.getTrustManagers();
             sslContext.init(kmf.getKeyManagers(), tms, new SecureRandom());
         } catch (Exception e) {
             System.out.println("EE = " + e);
         }
     }
}
class MySslContext extends SSLContext {
    public MySslContext(SSLContextSpi spi, Provider prov, String alg) {
        super(spi, prov, alg);
    }
}
