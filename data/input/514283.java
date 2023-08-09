@TestTargetClass(SecretKeySpec.class)
public class SecretKeySpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecretKeySpec",
        args = {byte[].class, java.lang.String.class}
    )
    public void testSecretKeySpec1() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";
        try {
            new SecretKeySpec(new byte[] {}, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of empty key.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SecretKeySpec(null, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null key.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SecretKeySpec(key, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null algorithm.");
        } catch (IllegalArgumentException e) {
        }
        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        key[0] ++;
        assertFalse("The change of key specified in the constructor "
                    + "should not cause the change of internal array.",
                    key[0] == ks.getEncoded()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecretKeySpec",
        args = {byte[].class, int.class, int.class, java.lang.String.class}
    )
    public void testSecretKeySpec2() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        int offset = 1;
        int len = 4;
        String algorithm = "Algorithm";
        try {
            new SecretKeySpec(new byte[] {}, 0, 0, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of empty key.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SecretKeySpec(null, 0, 0, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null key.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SecretKeySpec(key, offset, len, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short key algorithm.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SecretKeySpec(key, offset, key.length, algorithm);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null key.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SecretKeySpec(key, 0, -1, algorithm);
            fail("An ArrayIndexOutOfBoundsException should be thrown "
                    + "in the case of illegal length.");
        } catch (IllegalArgumentException e) {
            fail("Not expected IllegalArgumentException was thrown.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        key[offset] ++;
        assertFalse("The change of key specified in the constructor "
                    + "should not cause the change of internal array.",
                    key[offset] == ks.getEncoded()[0]);
        try {
            new SecretKeySpec(new byte[] { 2 }, 4, -100, "CCC");
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SecretKeySpec",
            args = {byte[].class, int.class, int.class, java.lang.String.class}
        )
    public void testSecretKeySpec3() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        int offset = 1;
        int len = 4;
        String algorithm = "Algorithm";
        try {
            new SecretKeySpec(key, -1, key.length, algorithm);
            fail("An ArrayIndexOutOfBoundsException should be thrown "
                    + "in the case of illegal offset.");
        } catch (IllegalArgumentException e) {
            fail("Not expected IllegalArgumentException was thrown.");
        } catch (ArrayIndexOutOfBoundsException e) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void testGetAlgorithm() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";
        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        assertEquals("The returned value does not equal to the "
                + "value specified in the constructor.",
                algorithm, ks.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getFormat",
        args = {}
    )
    public void testGetFormat() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";
        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        assertTrue("The returned value is not \"RAW\".",
                ks.getFormat() == "RAW");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEncoded",
        args = {}
    )
    public void testGetEncoded() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";
        SecretKeySpec ks = new SecretKeySpec(key, algorithm);
        byte[] result = ks.getEncoded();
        if (! Arrays.equals(key, result)) {
            fail("The returned key does not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getEncoded() method key "
                    + "should not cause the change of internal array.",
                    result[0] == ks.getEncoded()[0]);
        int offset = 1;
        int len = 4;
        SecretKeySpec sks = new SecretKeySpec(key, offset, len, algorithm);
        assertEquals("Key length is incorrect", len, sks.getEncoded().length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";
        SecretKeySpec ks1 = new SecretKeySpec(key, algorithm);
        SecretKeySpec ks2 = new SecretKeySpec(key, algorithm);
        assertTrue("Equal objects should have the same hash codes.",
                                            ks1.hashCode() == ks2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        byte[] key = new byte[] {1, 2, 3, 4, 5};
        String algorithm = "Algorithm";
        SecretKeySpec ks1 = new SecretKeySpec(key, algorithm);
        SecretKeySpec ks2 = new SecretKeySpec(key, algorithm);
        SecretKeySpec ks3 = new SecretKeySpec(key, algorithm);
        assertTrue("The equivalence relation should be reflexive.",
                                                        ks1.equals(ks1));
        assertTrue("Objects built on the same parameters should be equal.",
                                                        ks1.equals(ks2));
        assertTrue("The equivalence relation should be symmetric.",
                                                        ks2.equals(ks1));
        assertTrue("Objects built on the equal parameters should be equal.",
                                                        ks2.equals(ks3));
        assertTrue("The equivalence relation should be transitive.",
                                                        ks1.equals(ks3));
        assertFalse("Should not be equal to null object.",
                                                        ks1.equals(null));
        ks2 = new SecretKeySpec(new byte[] {1}, algorithm);
        assertFalse("Objects should not be equal.", ks1.equals(ks2));
        ks2 = new SecretKeySpec(key, "Another Algorithm");
        assertFalse("Objects should not be equal.", ks1.equals(ks2));
    }
    public static Test suite() {
        return new TestSuite(SecretKeySpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
