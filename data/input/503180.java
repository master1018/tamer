@TestTargetClass(SSLContext.class) 
public class SSLContext2Test extends TestCase {
    private static String srvSSLContext = "SSLContext";
    private static final String defaultProtocol = "S+S+L";
    public static final String SSLContextProviderClass = "org.apache.harmony.xnet.tests.support.MySSLContextSpi";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static final String[] validValues;
    static {
        validValues = new String[4];
        validValues[0] = defaultProtocol;
        validValues[1] = defaultProtocol.toLowerCase();
        validValues[2] = "s+S+L";
        validValues[3] = "S+s+L";
    }
    Provider mProv;
    protected void setUp() throws Exception {
        super.setUp();
        mProv = (new SpiEngUtils()).new MyProvider("MySSLContextProvider", "Provider for testing",
                srvSSLContext.concat(".").concat(defaultProtocol),
                SSLContextProviderClass);
        Security.insertProviderAt(mProv, 1);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }
    private void checkSSLContext(SSLContext sslC) 
            throws KeyManagementException {
        try {
            sslC.getSocketFactory();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", "Not initialiazed", e.getMessage());
        }
        try {
            sslC.getServerSocketFactory();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", "Not initialiazed", e.getMessage());
        }
        try {
            sslC.getServerSessionContext();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", "Not initialiazed", e.getMessage());
        }
        try {
            sslC.getClientSessionContext();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", "Not initialiazed", e.getMessage());
        }      
        try {
            sslC.createSSLEngine();
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", "Not initialiazed", e.getMessage());
        }
        try {
            sslC.createSSLEngine("host",1);
            fail("RuntimeException must be thrown");
        } catch (RuntimeException e) {
            assertEquals("Incorrect message", "Not initialiazed", e.getMessage());
        }
        TrustManager [] tm = new TManager[10];
        KeyManager [] km = new KManager[5];
        try {
            sslC.init(km, tm, null);
            fail("KeyManagementException must be thrown");
        } catch (KeyManagementException e) {
        }
        sslC.init(km, tm, new SecureRandom());
        SSLEngine sslE = sslC.createSSLEngine();
        assertTrue("Not null result",sslE instanceof SSLEngine);
        assertNull("Incorrect host", sslE.getPeerHost());
        assertEquals("Incorrect port", 0, sslE.getPeerPort());
        String host = "ZZZ";
        int port = 8080;
        sslE = sslC.createSSLEngine(host, port);
        assertTrue("Not null result",sslE instanceof SSLEngine);
        assertEquals("Incorrect host", sslE.getPeerHost(), host);
        assertEquals("Incorrect port", sslE.getPeerPort(), port);
        try {
            assertNull("Not null result", sslC.getServerSessionContext());
        } catch (NullPointerException e) {
        }
        try {            
            assertNull("Not null result", sslC.getClientSessionContext());
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws NoSuchAlgorithmException, 
            KeyManagementException {
        try {
            SSLContext.getInstance(null);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (protocol is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SSLContext.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (protocol: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        SSLContext sslC;
        for (int i = 0; i < validValues.length; i++) {
            sslC = SSLContext.getInstance(validValues[i]);
            assertTrue("Not instanceof SSLContext object",
                    sslC instanceof SSLContext);
            assertEquals("Incorrect protocol", sslC.getProtocol(),
                    validValues[i]);
            assertEquals("Incorrect provider", sslC.getProvider(), mProv);
            checkSSLContext(sslC);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException, 
            KeyManagementException {
        try {
            SSLContext.getInstance(null, mProv.getName());
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (protocol is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SSLContext.getInstance(invalidValues[i], mProv.getName());
                fail("NoSuchAlgorithmException must be thrown (protocol: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                SSLContext.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (protocol: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
            try {
                SSLContext.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty (protocol: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    SSLContext.getInstance(validValues[i], invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (protocol: "
                            .concat(invalidValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        SSLContext sslC;
        for (int i = 0; i < validValues.length; i++) {
            sslC = SSLContext.getInstance(validValues[i], mProv.getName());
            assertTrue("Not instanceof SSLContext object",
                    sslC instanceof SSLContext);
            assertEquals("Incorrect protocol", sslC.getProtocol(),
                    validValues[i]);
            assertEquals("Incorrect provider", sslC.getProvider().getName(),
                    mProv.getName());
            checkSSLContext(sslC);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_security_Provider()
        throws NoSuchAlgorithmException,
        IllegalArgumentException, KeyManagementException {
        try {
            SSLContext.getInstance(null, mProv);
            fail("NoSuchAlgorithmException or NullPointerException should be thrown (protocol is null");
        } catch (NoSuchAlgorithmException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                SSLContext.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (protocol: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                SSLContext.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (protocol: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        SSLContext sslC;
        for (int i = 0; i < validValues.length; i++) {
            sslC = SSLContext.getInstance(validValues[i], mProv);
            assertTrue("Not instanceof SSLContext object",
                    sslC instanceof SSLContext);
            assertEquals("Incorrect protocol", sslC.getProtocol(),
                    validValues[i]);
            assertEquals("Incorrect provider", sslC.getProvider(), mProv);
            checkSSLContext(sslC);
       }
    }
    class TManager implements TrustManager {
    }
    class KManager implements KeyManager {
    }
}