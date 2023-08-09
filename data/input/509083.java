@TestTargetClass(SecureRandom.class)
public class SecureRandomTest extends TestCase {
    Provider p;
    protected void setUp() throws Exception {
        super.setUp();
        p = new SRProvider();
        Security.insertProviderAt(p, 1);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(p.getName());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verification of negative and boundary parameters missed",
        method = "next",
        args = {int.class}
    )
    public final void testNext() {
        MySecureRandom sr = new MySecureRandom();
        if (sr.nextElement(1) != 1 || sr.nextElement(2) != 3 || sr.nextElement(3) != 7) {
            fail("next failed");            
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "setSeed",
        args = {long.class}
    )
    public final void testSetSeedlong() {
        SecureRandom sr = new SecureRandom();
        sr.setSeed(12345);
        if (!RandomImpl.runEngineSetSeed) {
            fail("setSeed failed");
        }    
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "nextBytes",
        args = {byte[].class}
    )
    public final void testNextBytes() {
        byte[] b = new byte[5];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(b);
        for (int i = 0; i < b.length; i++) {
            if (b[i] != (byte)(i + 0xF1)) {
                fail("nextBytes failed");
            }
        }
        try {
            sr.nextBytes(null);
            fail("expected exception");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureRandom",
        args = {}
    )
    public final void testSecureRandom() {
        SecureRandom sr = new SecureRandom();
        if (!sr.getAlgorithm().equals("someRandom")  ||
                sr.getProvider()!= p) {
            fail("incorrect SecureRandom implementation" + p.getName());
        }    
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Null parameter checking missed",
        method = "SecureRandom",
        args = {byte[].class}
    )
    public final void testSecureRandombyteArray() {
        byte[] b = {1,2,3};
        new SecureRandom(b);
        if (!RandomImpl.runEngineSetSeed) {
            fail("No setSeed");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "NoSuchAlgorithmException checking missed",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public final void testGetInstanceString() {
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("someRandom");    
        } catch (NoSuchAlgorithmException e) {
            fail(e.toString());
        }
        if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
            fail("getInstance failed");
        }    
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public final void testGetInstanceStringString() throws Exception {
        SecureRandom sr = SecureRandom.getInstance("someRandom", "SRProvider");    
        if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
            fail("getInstance failed");
        }
        try {
            SecureRandom r = SecureRandom.getInstance("anotherRandom", "SRProvider");
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        } catch (NoSuchProviderException e) {
            fail("unexpected: " + e);
        } catch (IllegalArgumentException e) {
            fail("unexpected: " + e);
        } catch (NullPointerException e) {
            fail("unexpected: " + e);
        }
        try {
            SecureRandom r = SecureRandom.getInstance("someRandom", "UnknownProvider");
            fail("expected NoSuchProviderException");
        } catch (NoSuchProviderException e) {
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } catch (IllegalArgumentException e) {
            fail("unexpected: " + e);
        } catch (NullPointerException e) {
            fail("unexpected: " + e);
        }
        try {
            SecureRandom r = SecureRandom.getInstance("someRandom", (String)null);
            fail("expected IllegalArgumentException");
        } catch (NoSuchProviderException e) {
            fail("unexpected: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
            fail("unexpected: " + e);
        }
        try {
            SecureRandom r = SecureRandom.getInstance(null, "SRProvider");
            fail("expected NullPointerException");
        } catch (NoSuchProviderException e) {
            fail("unexpected: " + e);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } catch (IllegalArgumentException e) {
            fail("unexpected: " + e);
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public final void testGetInstanceStringProvider() throws Exception {
        Provider p = new SRProvider();
        SecureRandom sr = SecureRandom.getInstance("someRandom", p);
        if (sr.getProvider() != p || !"someRandom".equals(sr.getAlgorithm())) {
            fail("getInstance failed");
        }
        try {
            SecureRandom r = SecureRandom.getInstance("unknownRandom", p);
            fail("expected NoSuchAlgorithmException");
        } catch (NoSuchAlgorithmException e) {
        } catch (IllegalArgumentException e) {
            fail("unexpected: " + e);
        } catch (NullPointerException e) {
            fail("unexpected: " + e);
        }
        try {
            SecureRandom r = SecureRandom.getInstance(null, p);
            fail("expected NullPointerException");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } catch (IllegalArgumentException e) {
            fail("unexpected: " + e);
        } catch (NullPointerException e) {
        }
        try {
            SecureRandom r = SecureRandom.getInstance("anyRandom", (Provider)null);
            fail("expected IllegalArgumentException");
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected: " + e);
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e) {
            fail("unexpected: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification with null parameter missed",
        method = "setSeed",
        args = {byte[].class}
    )
    public final void testSetSeedbyteArray() {
        byte[] b = {1,2,3};
        SecureRandom sr = new SecureRandom();
        sr.setSeed(b);
        if (!RandomImpl.runEngineSetSeed) {
            fail("setSeed failed");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification with invalid parameter missed",
        method = "getSeed",
        args = {int.class}
    )
    public final void testGetSeed() {
        byte[] b = SecureRandom.getSeed(4);
        if( b.length != 4) {
            fail("getSeed failed");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification with invalid parameter missed",
        method = "generateSeed",
        args = {int.class}
    )
    public final void testGenerateSeed() {
        SecureRandom sr = new SecureRandom();
        byte[] b = sr.generateSeed(4);
        for (int i = 0; i < b.length; i++) {
            if (b[i] != (byte)i) {
                fail("generateSeed failed");
            }
        }
    }
    public class SRProvider extends Provider {
        public SRProvider() {
            super("SRProvider", 1.0, "SRProvider for testing");
            put("SecureRandom.someRandom",
                    "org.apache.harmony.security.tests.support.RandomImpl");
        }
    }
    class MySecureRandom extends SecureRandom {
        public MySecureRandom(){
            super();
        }
        public int nextElement(int numBits) {
            return super.next(numBits);
        }
    }
}
