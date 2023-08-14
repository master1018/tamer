@TestTargetClass(SSLContextSpi.class) 
public class SSLContextSpiTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLContextSpi",
        args = {}
    )
    public void test_Constructor() {
        try {
            SSLContextSpiImpl ssl = new SSLContextSpiImpl();
            assertTrue(ssl instanceof SSLContextSpi);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "engineCreateSSLEngine",
        args = {}
    )
    public void test_engineCreateSSLEngine_01() {
        SSLContextSpiImpl ssl = new SSLContextSpiImpl();
        try {
            SSLEngine sleng = ssl.engineCreateSSLEngine();
            fail("RuntimeException wasn't thrown");
        } catch (RuntimeException re) {
            String str = re.getMessage();
            if (!str.equals("Not initialiazed")) 
                fail("Incorrect exception message: " + str);
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "engineCreateSSLEngine",
        args = {java.lang.String.class, int.class}
    )
    public void test_engineCreateSSLEngine_02() {
        int[] invalid_port = {Integer.MIN_VALUE, -65535, -1, 65536, Integer.MAX_VALUE};
        SSLContextSpiImpl ssl = new SSLContextSpiImpl();
        try {
            SSLEngine sleng = ssl.engineCreateSSLEngine("localhost", 1080);
            fail("RuntimeException wasn't thrown");
        } catch (RuntimeException re) {
            String str = re.getMessage();
            if (!str.equals("Not initialiazed")) 
                fail("Incorrect exception message: " + str);
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }
        for (int i = 0; i < invalid_port.length; i++) {
            try {
                SSLEngine sleng = ssl.engineCreateSSLEngine("localhost", invalid_port[i]);
                fail("IllegalArgumentException wasn't thrown");
            } catch (IllegalArgumentException iae) {
            }            
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetClientSessionContext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetServerSessionContext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetServerSocketFactory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetSocketFactory",
            args = {}
        )
    })
    public void test_commonTest_01() {
        SSLContextSpiImpl ssl = new SSLContextSpiImpl();
        try {
            SSLSessionContext slsc = ssl.engineGetClientSessionContext();
            fail("RuntimeException wasn't thrown");
        } catch (RuntimeException re) {
            String str = re.getMessage();
            if (!str.equals("Not initialiazed")) 
                fail("Incorrect exception message: " + str);
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }
        try {
            SSLSessionContext slsc = ssl.engineGetServerSessionContext();
            fail("RuntimeException wasn't thrown");
        } catch (RuntimeException re) {
            String str = re.getMessage();
            if (!str.equals("Not initialiazed")) 
                fail("Incorrect exception message: " + str);
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }
        try {
            SSLServerSocketFactory sssf = ssl.engineGetServerSocketFactory();
            fail("RuntimeException wasn't thrown");
        } catch (RuntimeException re) {
            String str = re.getMessage();
            if (!str.equals("Not initialiazed")) 
                fail("Incorrect exception message: " + str);
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }
        try {
            SSLSocketFactory ssf = ssl.engineGetSocketFactory();
            fail("RuntimeException wasn't thrown");
        } catch (RuntimeException re) {
            String str = re.getMessage();
            if (!str.equals("Not initialiazed")) 
                fail("Incorrect exception message: " + str);
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {javax.net.ssl.KeyManager[].class, javax.net.ssl.TrustManager[].class, java.security.SecureRandom.class}
    )
    public void test_engineInit() {
        SSLContextSpiImpl ssl = new SSLContextSpiImpl();
        String defaultAlgorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(defaultAlgorithm);
            char[] pass = "password".toCharArray();
            kmf.init(null, pass);
            KeyManager[] km = kmf.getKeyManagers();
            defaultAlgorithm = Security.getProperty("ssl.TrustManagerFactory.algorithm");
            TrustManagerFactory trustMF = TrustManagerFactory.getInstance(defaultAlgorithm);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            trustMF.init(ks);
            TrustManager[] tm = trustMF.getTrustManagers();
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            try {
                ssl.engineInit(km, tm, sr);
            } catch (KeyManagementException kme) {
                fail(kme + " was throw for engineInit method");
            }
            try {
                ssl.engineInit(km, tm, null);
                fail("KeyManagementException wasn't thrown");
            } catch (KeyManagementException kme) {
            }
        } catch (Exception ex) {
            fail(ex + " unexpected exception");
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineCreateSSLEngine",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineCreateSSLEngine",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetClientSessionContext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetServerSessionContext",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetServerSocketFactory",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "engineGetSocketFactory",
            args = {}
        )
    })
    public void test_commonTest_02() {
        SSLContextSpiImpl ssl = new SSLContextSpiImpl();
        String defaultAlgorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(defaultAlgorithm);
            char[] pass = "password".toCharArray();
            kmf.init(null, pass);
            KeyManager[] km = kmf.getKeyManagers();
            defaultAlgorithm = Security.getProperty("ssl.TrustManagerFactory.algorithm");
            TrustManagerFactory trustMF = TrustManagerFactory.getInstance(defaultAlgorithm);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            trustMF.init(ks);
            TrustManager[] tm = trustMF.getTrustManagers();
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            ssl.engineInit(km, tm, sr);
        } catch (Exception ex) {
            fail(ex + " unexpected exception");
        }
        try {
            assertNotNull("Subtest_01: Object is NULL", ssl.engineCreateSSLEngine());
            SSLEngine sleng = ssl.engineCreateSSLEngine("localhost", 1080);
            assertNotNull("Subtest_02: Object is NULL", sleng);
            assertEquals(sleng.getPeerPort(), 1080);
            assertEquals(sleng.getPeerHost(), "localhost");
            assertNull("Subtest_03: Object not NULL", ssl.engineGetClientSessionContext());
            assertNull("Subtest_04: Object not NULL", ssl.engineGetServerSessionContext());
            assertNull("Subtest_05: Object not NULL", ssl.engineGetServerSocketFactory());
            assertNull("Subtest_06: Object not NULL", ssl.engineGetSocketFactory());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}
