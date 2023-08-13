@TestTargetClass(RC2ParameterSpec.class)
public class RC2ParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RC2ParameterSpec",
        args = {int.class, byte[].class}
    )
    public void testRC2ParameterSpec1() {
        int effectiveKeyBits = 10;
        byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            new RC2ParameterSpec(effectiveKeyBits, null);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC2ParameterSpec(effectiveKeyBits, new byte[] {1, 2, 3, 4, 5});
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv);
        iv[0] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[0] == ps.getIV()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RC2ParameterSpec",
        args = {int.class, byte[].class, int.class}
    )
    public void testRC2ParameterSpec2() {
        int effectiveKeyBits = 10;
        byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        int offset = 2;
        try {
            new RC2ParameterSpec(effectiveKeyBits, null, offset);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of null iv.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new RC2ParameterSpec(effectiveKeyBits, iv, 4);
            fail("An IllegalArgumentException should be thrown "
                    + "in the case of short iv.");
        } catch (IllegalArgumentException e) {
        }
        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv, offset);
        iv[offset] ++;
        assertFalse("The change of iv specified in the constructor "
                    + "should not cause the change of internal array.",
                    iv[offset] == ps.getIV()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getEffectiveKeyBits",
        args = {}
    )
    public void testGetEffectiveKeyBits() {
        int effectiveKeyBits = 10;
        byte[] iv = {1, 2, 3, 4, 5, 6, 7, 8};
        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv);
        assertTrue("The returned effectiveKeyBits value is not equal to the "
                + "value specified in the constructor.",
                effectiveKeyBits == ps.getEffectiveKeyBits());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIV",
        args = {}
    )
    public void testGetIV() {
        int effectiveKeyBits = 10;
        byte[] iv = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        RC2ParameterSpec ps = new RC2ParameterSpec(effectiveKeyBits, iv);
        byte[] result = ps.getIV();
        if (! Arrays.equals(iv, result)) {
            fail("The returned iv is not equal to the specified "
                    + "in the constructor.");
        }
        result[0] ++;
        assertFalse("The change of returned by getIV() method iv "
                    + "should not cause the change of internal array.",
                    result[0] == ps.getIV()[0]);
        ps = new RC2ParameterSpec(effectiveKeyBits);
        assertNull("The getIV() method should return null if the parameter "
                    + "set does not contain iv.", ps.getIV());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEquals() {
        int effectiveKeyBits = 10;
        byte[] iv = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        RC2ParameterSpec ps1 = new RC2ParameterSpec(effectiveKeyBits, iv);
        RC2ParameterSpec ps2 = new RC2ParameterSpec(effectiveKeyBits, iv);
        RC2ParameterSpec ps3 = new RC2ParameterSpec(10,
                                    new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9});
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
        ps2 = new RC2ParameterSpec(11, iv);
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
        ps2 = new RC2ParameterSpec(11, new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1});
        assertFalse("Objects should not be equal.", ps1.equals(ps2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        int effectiveKeyBits = 0;
        byte[] iv = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
        RC2ParameterSpec ps1 = new RC2ParameterSpec(effectiveKeyBits, iv);
        RC2ParameterSpec ps2 = new RC2ParameterSpec(effectiveKeyBits, iv);
        assertTrue("Equal objects should have the same hash codes.",
                                            ps1.hashCode() == ps2.hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "RC2ParameterSpec",
        args = {int.class}
    )
    public void test_constructorI() {
        int effectiveKeyBits = 0;
        RC2ParameterSpec ps1 = new RC2ParameterSpec(effectiveKeyBits);
        RC2ParameterSpec ps2 = new RC2ParameterSpec(effectiveKeyBits);
        assertTrue(ps1.equals(ps2));
    }
    public static Test suite() {
        return new TestSuite(RC2ParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
