@TestTargetClass(KeyManagerFactorySpi.class) 
public class KeyManagerFactorySpiTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "KeyManagerFactorySpi",
        args = {}
    )
    public void test_Constructor() {
        try {
            KeyManagerFactorySpiImpl kmf = new KeyManagerFactorySpiImpl();
            assertTrue(kmf instanceof KeyManagerFactorySpi);
        } catch (Exception e) {
            fail("Unexpected Exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {java.security.KeyStore.class, char[].class}
    )
    public void test_engineInit_01() {
        KeyManagerFactorySpiImpl kmf = new KeyManagerFactorySpiImpl();
        KeyStore ks;
        char[] psw = "password".toCharArray();
        try {
            kmf.engineInit(null, null);
            fail("NoSuchAlgorithmException wasn't thrown");
        } catch (NoSuchAlgorithmException kse) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NoSuchAlgorithmException");
        }
        try {
            kmf.engineInit(null, psw);
            fail("KeyStoreException wasn't thrown");
        } catch (KeyStoreException uke) {
        } catch (Exception e) {
            fail(e + " was thrown instead of KeyStoreException");
        }
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            kmf.engineInit(ks, null);
            fail("UnrecoverableKeyException wasn't thrown");
        } catch (UnrecoverableKeyException uke) {
        } catch (Exception e) {
            fail(e + " was thrown instead of UnrecoverableKeyException");
        }
        try {
            KeyStore kst = KeyStore.getInstance(KeyStore.getDefaultType());
            kst.load(null, null);
            kmf.engineInit(kst, psw);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineInit",
        args = {javax.net.ssl.ManagerFactoryParameters.class}
    )
    public void test_engineInit_02() {
        KeyManagerFactorySpiImpl kmf = new KeyManagerFactorySpiImpl();
        try {
            kmf.engineInit(null);
            fail("InvalidAlgorithmParameterException wasn't thrown");
        } catch (InvalidAlgorithmParameterException iape) {
        } catch (Exception e) {
            fail(e + " was thrown instead of InvalidAlgorithmParameterException");
        }
        try {
            char[] psw = "password".toCharArray();
            Parameters pr = new Parameters(psw);
            kmf.engineInit(pr);
        } catch (Exception e) {
            fail(e + " unexpected exception was thrown");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "engineGetKeyManagers",
        args = {}
    )
    public void test_engineGetKeyManagers() {
        KeyManagerFactorySpiImpl kmf = new KeyManagerFactorySpiImpl();
        try {
            KeyManager[] km = kmf.engineGetKeyManagers();
            fail("IllegalStateException wasn't thrown");
        } catch (IllegalStateException ise) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalStateException");
        }
        try {
            char[] psw = "password".toCharArray();
            Parameters pr = new Parameters(psw);
            kmf.engineInit(pr);
            KeyManager[] km = kmf.engineGetKeyManagers();
            assertNull("Object is not NULL", km);
        } catch (Exception e) {
            fail(e + " unexpected exception was thrown");
        }
    }
    public class Parameters implements ManagerFactoryParameters {
        private char[] passWD;
        public Parameters (char[] pass) {
            this.passWD = pass;
        }
        public char[] getPassword() {
            return passWD;
        }
    }
}
