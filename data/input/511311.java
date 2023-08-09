@TestTargetClass(RC5ParameterSpec.class)
public class RC5ParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RC5ParameterSpec",
        args = {int.class, int.class, int.class, byte[].class}
    )
    public void testRC5ParameterSpec1() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4};
        try {
            new RC5ParameterSpec(version, rounds, wordSize, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC5ParameterSpec(version, rounds, wordSize+8, iv);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC5ParameterSpec(version, rounds, wordSize, new byte[] {1, 2, 3});
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        iv[0] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[0] == ps.getIV()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RC5ParameterSpec",
        args = {int.class, int.class, int.class, byte[].class, int.class}
    )
    public void testRC5ParameterSpec2() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4, 5, 6};
        int offset = 2;
        try {
            new RC5ParameterSpec(version, rounds, wordSize, null, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC5ParameterSpec(version, rounds, wordSize+8, iv, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC5ParameterSpec(version, rounds, wordSize, iv, offset+1);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC5ParameterSpec(version, rounds, wordSize, new byte[] { 1, 2,
                    3, 4 }, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize,
                                                                    iv, offset);
        iv[offset] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[offset] == ps.getIV()[0]);
        try {
            new RC5ParameterSpec(0, 9, 77, new byte[] { 2 }, -100);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getVersion",
        args = {}
    )
    public void testGetVersion() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertTrue("The returned version value should be equal to the "
                + "value specified in the constructor.",
                ps.getVersion() == version);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getRounds",
        args = {}
    )
    public void testGetRounds() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertTrue("The returned rounds value should be equal to the "
                + "value specified in the constructor.",
                ps.getRounds() == rounds);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getWordSize",
        args = {}
    )
    public void testGetWordSize() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertTrue("The returned wordSize value should be equal to the "
                + "value specified in the constructor.",
                ps.getWordSize() == wordSize);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIV",
        args = {}
    )
    public void testGetIV() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4};
        RC5ParameterSpec ps = new RC5ParameterSpec(version, rounds,
                                                            wordSize, iv);
        byte[] result = ps.getIV();
        if (! Arrays.equals(iv, result)) {
            fail("The returned iv is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getIV() method iv "
                    + "should not cause the change of internal array.",
                    result[0] == ps.getIV()[0]);
        ps = new RC5ParameterSpec(version, rounds, wordSize);
        assertNull("The getIV() method should return null if the parameter "
                    + "set does not contain IV.", ps.getIV());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4, 5, 6};
        RC5ParameterSpec ps1 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        RC5ParameterSpec ps2 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        RC5ParameterSpec ps3 = new RC5ParameterSpec(version, rounds, wordSize,
                                                    new byte[] {1, 2, 3, 4});
        assertTrue("The equivalence relation should be reflexive.",
                                                        ps1.equals(ps1));
        assertTrue("Objects built on the same parameters should be equal.",
                                                        ps1.equals(ps2));
        assertTrue("The equivalence relation should be symmetric.",
                                                        ps2.equals(ps1));
        assertTrue("Objects built on the equal parameters should be equal.",
                                                        ps2.equals(ps3));
        assertTrue("The equivalence relation should be transitive.",
                                                        ps1.equals(ps3));
        assertFalse("Should return not be equal to null object.",
                                                        ps1.equals(null));
        ps2 = new RC5ParameterSpec(version+1, rounds, wordSize, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
        ps2 = new RC5ParameterSpec(version, rounds+1, wordSize, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
        ps2 = new RC5ParameterSpec(version, rounds, wordSize/2, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
        ps2 = new RC5ParameterSpec(version, rounds, wordSize,
                                                    new byte[] {4, 3, 2, 1});
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        byte[] iv = {1, 2, 3, 4, 5, 6};
        RC5ParameterSpec ps1 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        RC5ParameterSpec ps2 = new RC5ParameterSpec(version, rounds,
                                                                wordSize, iv);
        assertTrue("Equal objects should have the same hash codes.",
                                            ps1.hashCode() == ps2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "RC5ParameterSpec",
        args = {int.class, int.class, int.class}
    )
    public void test_constructorIII() {
        int version = 1;
        int rounds = 5;
        int wordSize = 16;
        RC5ParameterSpec ps1 = new RC5ParameterSpec(version, rounds, wordSize);
        RC5ParameterSpec ps2 = new RC5ParameterSpec(version, rounds, wordSize);
        RC5ParameterSpec ps3 = new RC5ParameterSpec(version, rounds, wordSize + 1);
        assertTrue(ps1.equals(ps2));
        assertFalse(ps1.equals(ps3));
    }
    public static Test suite() {
        return new TestSuite(RC5ParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
