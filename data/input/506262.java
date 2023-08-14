@TestTargetClass(TrustManagerFactorySpi.class) 
public class TrustManagerFactorySpiTest extends TestCase {
    private TrustManagerFactorySpiImpl factory = new TrustManagerFactorySpiImpl();
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TrustManagerFactorySpi",
        args = {}
    )
    public void test_Constructor() {
        try {
            TrustManagerFactorySpiImpl tmf = new TrustManagerFactorySpiImpl();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {java.security.KeyStore.class}
    )
    public void test_engineInit_01() throws NoSuchAlgorithmException,
            KeyStoreException {
        factory.reset();
        Provider provider = new MyProvider();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("MyTMF",
                provider);
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            tmf.init(ks);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        assertTrue(factory.isEngineInitCalled());
        assertEquals(ks, factory.getKs());
        factory.reset();
        tmf.init((KeyStore) null);
        assertTrue(factory.isEngineInitCalled());
        assertNull(factory.getKs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {javax.net.ssl.ManagerFactoryParameters.class}
    )
    public void test_engineInit_02() throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException {
        factory.reset();
        Provider provider = new MyProvider();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("MyTMF",
                provider);
        Parameters pr = null;
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            pr = new Parameters(ks);
            tmf.init(pr);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
        assertTrue(factory.isEngineInitCalled());
        assertEquals(pr, factory.getSpec());
        factory.reset();
        tmf.init((ManagerFactoryParameters) null);
        assertTrue(factory.isEngineInitCalled());
        assertNull(factory.getSpec());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineGetTrustManagers",
        args = {}
    )
    public void test_engineGetTrustManagers() throws NoSuchAlgorithmException {
        factory.reset();
        Provider provider = new MyProvider();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("MyTMF",
                provider);
        TrustManager[] tm = tmf.getTrustManagers();
        assertTrue(factory.isEngineGetTrustManagersCalled());
        factory.reset();
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);
            tmf.init(ks);
            tm = tmf.getTrustManagers();
            assertTrue(factory.isEngineGetTrustManagersCalled());
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
}
class MyProvider extends Provider {
    public MyProvider() {
        super("MyProvider", 1.0, "My Test Provider");
        AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {
            public Void run() {
                put("TrustManagerFactory.MyTMF",
                        "org.apache.harmony.xnet.tests.support.TrustManagerFactorySpiImpl");
                return null;
            }
        });
    }
}
