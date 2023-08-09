@TestTargetClass(SecureRandom.class)
public class SecureRandom2Test extends TestCase {
    private static final byte[] SEED_BYTES = { (byte) 33, (byte) 15, (byte) -3,
            (byte) 22, (byte) 77, (byte) -16, (byte) -33, (byte) 56 };
    private static final int SEED_SIZE = 539;
    private static final long SEED_VALUE = 5335486759L;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void testGetProvider() {
        SecureRandom sr1 = new SecureRandom();
        assertNotNull(sr1.getProvider());
        SecureRandom sr2 = new SecureRandom(SEED_BYTES);
        assertNotNull(sr2.getProvider());
        MyProvider p = new MyProvider();
        MySecureRandom sr3 = new MySecureRandom(new MySecureRandomSpi(), p);
        assertEquals(p, sr3.getProvider());
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            assertNotNull(random.getProvider());
        } catch (NoSuchAlgorithmException e) {
            fail("Unexpected NoSuchAlgorithmException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {}
    )
    public void test_Constructor() {
        try {
            new SecureRandom();
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {byte[].class}
    )
    public void test_Constructor$B() {
        try {
            new SecureRandom(SEED_BYTES);
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }
        try {
            new SecureRandom(null);
            fail("NullPointerException was not thrown for NULL parameter");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {java.security.SecureRandomSpi.class, java.security.Provider.class}
    )
    public void test_ConstructorLjava_security_SecureRandomSpi_java_security_Provider() {
        try {
            new MySecureRandom(null, null);
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }
        try {
            MyProvider p = new MyProvider();
            MySecureRandom sr = new MySecureRandom(new MySecureRandomSpi(), p);
            assertEquals("unknown", sr.getAlgorithm());
            assertEquals(p, sr.getProvider());
            sr = new MySecureRandom(new MySecureRandomSpi(), null);
            sr = new MySecureRandom(null, p);
        } catch (Exception e) {
            fail("Constructor threw exception : " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "generateSeed",
        args = {int.class}
    )
    public void test_generateSeedI() {
        byte[] seed = new SecureRandom().generateSeed(SEED_SIZE);
        assertEquals("seed has incorrect size", SEED_SIZE, seed.length);
        try {
            new SecureRandom().generateSeed(-42);
            fail("expected an exception");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() {
        try {
            SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find a SHA1PRNG algorithm");
        }
        try {
            SecureRandom.getInstance("MD2");
            fail("NoSuchAlgorithmException should be thrown for MD2 algorithm");
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException, NoSuchProviderException, IllegalArgumentException checking missed",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() {
        try {
            Provider[] providers = Security
                    .getProviders("SecureRandom.SHA1PRNG");
            if (providers != null) {
                for (int i = 0; i < providers.length; i++) {
                    SecureRandom
                            .getInstance("SHA1PRNG", providers[i].getName());
                }
            } else {
                fail("No providers support SHA1PRNG");
            }
        } catch (NoSuchAlgorithmException e) {
            fail("getInstance did not find a SHA1PRNG algorithm");
        } catch (NoSuchProviderException e) {
            fail("getInstance did not find the provider for SHA1PRNG");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification of negative parameter missed",
        method = "getSeed",
        args = {int.class}
    )
    public void test_getSeedI() {
        byte[] seed = SecureRandom.getSeed(SEED_SIZE);
        assertEquals("seed has incorrect size", SEED_SIZE, seed.length);
        try {
            new SecureRandom().getSeed(-42);
            fail("expected an exception");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Null array checking missed",
        method = "nextBytes",
        args = {byte[].class}
    )
    public void test_nextBytes$B() {
        byte[] bytes = new byte[313];
        try {
            new SecureRandom().nextBytes(bytes);
        } catch (Exception e) {
            fail("next bytes not ok : " + e);
        }
        try {
            new SecureRandom().nextBytes(null);
            fail("expected exception");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Null array checking missed",
        method = "setSeed",
        args = {byte[].class}
    )
    public void test_setSeed$B() {
        try {
            new SecureRandom().setSeed(SEED_BYTES);
        } catch (Exception e) {
            fail("seed generation with bytes failed : " + e);
        }
        try {
            new SecureRandom().setSeed(null);
            fail("expected exception");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setSeed",
        args = {long.class}
    )
    public void test_setSeedJ() {
        try {
            new SecureRandom().setSeed(SEED_VALUE);
        } catch (Exception e) {
            fail("seed generation with long failed : " + e);
        }
        try {
            new SecureRandom().setSeed(-1);
        } catch (Exception e) {
            fail("unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() {
        SecureRandomSpi spi = new SecureRandomSpi() {
            protected void engineSetSeed(byte[] arg) {
            }
            protected void engineNextBytes(byte[] arg) {
            }
            protected byte[] engineGenerateSeed(int arg) {
                return null;
            }
        };
        SecureRandom sr = new SecureRandom(spi, null) {
        };
        assertEquals("unknown", sr.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "next",
        args = {int.class}
    )
    public void test_nextJ() throws Exception {
        MySecureRandom mySecureRandom = new MySecureRandom(
                new MySecureRandomSpi(), null);
        int numBits = 29;
        int random = mySecureRandom.getNext(numBits);
        assertEquals(numBits, Integer.bitCount(random));
        numBits = 0;
        random = mySecureRandom.getNext(numBits);
        assertEquals(numBits, Integer.bitCount(random));
        numBits = 40;
        random = mySecureRandom.getNext(numBits);
        assertEquals(32, Integer.bitCount(random));
        numBits = -1;
        random = mySecureRandom.getNext(numBits);
        assertEquals(0, Integer.bitCount(random));
    }
    class MySecureRandom extends SecureRandom {
        private static final long serialVersionUID = 1L;
        public MySecureRandom(SecureRandomSpi secureRandomSpi, Provider provider) {
            super(secureRandomSpi, provider);
        }
        public int getNext(int numBits) {
            return super.next(numBits);
        }
    }
    class MySecureRandomSpi extends SecureRandomSpi {
        private static final long serialVersionUID = 1L;
        @Override
        protected byte[] engineGenerateSeed(int arg0) {
            return null;
        }
        @Override
        protected void engineNextBytes(byte[] bytes) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) 0xFF;
            }
        }
        @Override
        protected void engineSetSeed(byte[] arg0) {
            return;
        }
    }
    class MyProvider extends Provider {
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
            put("MessageDigest.abc", "SomeClassName");
            put("Alg.Alias.MessageDigest.SHA1", "SHA-1");
        }
        MyProvider(String name, double version, String info) {
            super(name, version, info);
        }
        public void putService(Provider.Service s) {
            super.putService(s);
        }
        public void removeService(Provider.Service s) {
            super.removeService(s);
        }
    }
}
