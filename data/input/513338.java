@TestTargetClass(NullCipher.class)
public class NullCipherTest extends TestCase {
    private Cipher c;
    protected void setUp() throws Exception {
        super.setUp();
        c = new NullCipher();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Class checks inherited methods.",
            method = "getAlgorithm",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Class checks inherited methods.",
            method = "NullCipher",
            args = {}
        )
    })
    public void testGetAlgorithm() {
        c.getAlgorithm();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks inherited method from Cipher.",
        method = "getBlockSize",
        args = {}
    )
    public void testGetBlockSize() {
        assertEquals("Incorrect BlockSize", 1, c.getBlockSize());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed. Checks inherited method from Cipher.",
        method = "getOutputSize",
        args = {int.class}
    )
    public void testGetOutputSize() {
        assertEquals("Incorrect OutputSize", 111, c.getOutputSize(111));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks inherited method from Cipher.",
            method = "getIV",
            args = {}
        ),
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "Checks inherited method from Cipher.",
                clazz = CipherSpi.class,
                method = "engineGetIV",
                args = {}
        )
    })
    public void testGetIV() {
        assertTrue("Incorrect IV", Arrays.equals(c.getIV(), new byte[8]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks inherited method from Cipher.",
        method = "getParameters",
        args = {}
    )
    public void testGetParameters() {
        assertNull("Incorrect Parameters", c.getParameters());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Checks inherited method from Cipher.",
        method = "getExemptionMechanism",
        args = {}
    )
    public void testGetExemptionMechanism() {
        assertNull("Incorrect ExemptionMechanism", c.getExemptionMechanism());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidKeyException checking missed. Checks inherited method from Cipher.",
        method = "init",
        args = {int.class, java.security.Key.class}
    )
    public void testInitintKey() throws Exception {
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[1], "algorithm"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "InvalidKeyException checking missed. Checks inherited method from Cipher.",
        method = "init",
        args = {int.class, java.security.Key.class, java.security.SecureRandom.class}
    )
    public void testInitintKeySecureRandom() throws Exception {
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[1],
                "algorithm"), new SecureRandom());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "init",
        args = {int.class, java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class}
    )
    public void testInitintKeyAlgorithmParameterSpec() throws Exception {
        class myAlgorithmParameterSpec implements java.security.spec.AlgorithmParameterSpec {}
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[1],
                "algorithm"), new myAlgorithmParameterSpec());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IllegalStateException checking missed. Checks inherited method from Cipher.",
        method = "update",
        args = {byte[].class}
    )
    public void testUpdatebyteArray() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = c.update(b);
        assertEquals("different length", b.length, r.length);
        assertTrue("different content", Arrays.equals(b, r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "IllegalStateException checking missed. Checks inherited method from Cipher.",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void testUpdatebyteArrayintint() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = c.update(b, 0, 5);
        assertEquals("different length", b.length, r.length);
        assertTrue("different content", Arrays.equals(b, r));
        r = c.update(b, 1, 3);
        assertEquals("different length", 3, r.length);
        for (int i = 0; i < 3; i++) {
            assertEquals("different content", b[i + 1], r[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "update",
        args = {byte[].class, int.class, int.class, byte[].class}
    )
    public void testUpdatebyteArrayintintbyteArray() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = new byte[5]; 
        c.update(b, 0, 5, r);            
        assertTrue("different content", Arrays.equals(b, r));        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "update",
        args = {byte[].class, int.class, int.class, byte[].class, int.class}
    )
    public void testUpdatebyteArrayintintbyteArrayint() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = new byte[5]; 
        c.update(b, 0, 5, r, 0);            
        assertTrue("different content", Arrays.equals(b, r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {}
    )
    public void testDoFinal() throws Exception {
        assertNull("doFinal failed", c.doFinal());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class}
    )
    public void testDoFinalbyteArrayint() throws Exception {
        byte [] r = new byte[5];
        assertEquals("doFinal failed", 0, c.doFinal(r, 0));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class}
    )
    public void testDoFinalbyteArray() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = null; 
        r = c.doFinal(b);
        assertEquals("different length", b.length, r.length);
        assertTrue("different content", Arrays.equals(b, r));        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class}
    )
    public void testDoFinalbyteArrayintint() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = null;
        r = c.doFinal(b, 0, 5);
        assertEquals("different length", b.length, r.length);
        assertTrue("different content", Arrays.equals(b, r));
        r = c.doFinal(b, 1, 3);
        assertEquals("different length", 3, r.length);
        for (int i = 0; i < 3; i++) {
            assertEquals("different content", b[i + 1], r[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void testUpdatebyteArrayintint2() {
        try {
            new NullCipher().update(new byte[1], 1, Integer.MAX_VALUE);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class}
    )
    public void testDoFinalbyteArrayintintbyteArray() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = new byte[5]; 
        c.doFinal(b, 0, 5, r);            
        assertTrue("different content", Arrays.equals(b, r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class}
    )
    public void testDoFinalbyteArrayintintbyteArray2() throws Exception {
        try {
            new NullCipher().update(new byte[1], 1, Integer.MAX_VALUE, 
                    new byte[1]);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class}
    )
    public void testDoFinalbyteArrayintintbyteArray3() throws Exception {
        try {
            new NullCipher().update(new byte[1], 0, 1, new byte[0]);
            fail("Expected IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class, int.class}
    )
    public void testDoFinalbyteArrayintintbyteArrayint() throws Exception {
        byte [] b = {1, 2, 3, 4, 5};
        byte [] r = new byte[5]; 
        c.doFinal(b, 0, 5, r, 0);
        assertTrue("different content", Arrays.equals(b, r));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class, int.class}
    )
    public void testDoFinalbyteArrayintintbyteArrayint2() throws Exception {
        try {
            new NullCipher().update(new byte[1], 1, Integer.MAX_VALUE, 
                    new byte[1], 0);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Exceptions checking missed. Checks inherited method from Cipher.",
        method = "doFinal",
        args = {byte[].class, int.class, int.class, byte[].class, int.class}
    )
    public void testDoFinalbyteArrayintintbyteArrayint3() throws Exception {
        try {
            new NullCipher().update(new byte[1], 0, 1, 
                    new byte[0], 0);
            fail("Expected IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
    }
}
