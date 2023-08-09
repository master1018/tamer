@TestTargetClass(PSSParameterSpec.class)
public class PSSParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameter.",
        method = "PSSParameterSpec",
        args = {int.class}
    )
    public final void testPSSParameterSpec0101() {
        AlgorithmParameterSpec aps = new PSSParameterSpec(20);
        assertTrue(aps instanceof PSSParameterSpec);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "PSSParameterSpec",
        args = {int.class}
    )
    public final void testPSSParameterSpec0102() {
        try {
            new PSSParameterSpec(-1);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies constructor with valid parameters.",
        method = "PSSParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, int.class, int.class}
    )
    public final void testPSSParameterSpec0201() {
        AlgorithmParameterSpec aps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertTrue(aps instanceof PSSParameterSpec);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "PSSParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, int.class, int.class}
    )
    public final void testPSSParameterSpec0202() {
        try {
            new PSSParameterSpec(null, "MGF1", MGF1ParameterSpec.SHA1, 20, 1);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "PSSParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, int.class, int.class}
    )
    public final void testPSSParameterSpec0203() {
        try {
            new PSSParameterSpec("SHA-1", null, MGF1ParameterSpec.SHA1, 20, 1);
            fail("Expected NPE not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IllegalArgumentException.",
        method = "PSSParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, int.class, int.class}
    )
    public final void testPSSParameterSpec0204() {
        try {
            new PSSParameterSpec("SHA-1", "MGF1",
                    MGF1ParameterSpec.SHA1, -20, 1);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "PSSParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, int.class, int.class}
    )
    public final void testPSSParameterSpec0205() {
        try {
            new PSSParameterSpec("SHA-1", "MGF1",
                    MGF1ParameterSpec.SHA1, 20, -1);
            fail("Expected IAE not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as AlgorithmParameterSpec parameter.",
        method = "PSSParameterSpec",
        args = {java.lang.String.class, java.lang.String.class, java.security.spec.AlgorithmParameterSpec.class, int.class, int.class}
    )
    public final void testPSSParameterSpec0206() {
        new PSSParameterSpec("SHA-1", "MGF1", null, 20, 1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getDigestAlgorithm",
        args = {}
    )
    public final void testGetDigestAlgorithm() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertEquals("SHA-1", pssps.getDigestAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getMGFAlgorithm",
        args = {}
    )
    public final void testGetMGFAlgorithm() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertEquals("MGF1", pssps.getMGFAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getMGFParameters",
        args = {}
    )
    public final void testGetMGFParameters01() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertTrue(MGF1ParameterSpec.SHA1.equals(pssps.getMGFParameters()));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "getMGFParameters",
        args = {}
    )
    public final void testGetMGFParameters02() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                null, 20, 1);
        assertNull(pssps.getMGFParameters());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getSaltLength",
        args = {}
    )
    public final void testGetSaltLength() {
        PSSParameterSpec pssps = new PSSParameterSpec(20);
        assertEquals(20, pssps.getSaltLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getTrailerField",
        args = {}
    )
    public final void testGetTrailerField() {
        PSSParameterSpec pssps = new PSSParameterSpec("SHA-1", "MGF1",
                MGF1ParameterSpec.SHA1, 20, 1);
        assertEquals(1, pssps.getTrailerField());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the name of default message digest algorithm.",
        method = "getDigestAlgorithm",
        args = {}
    )
    public final void testDEFAULTmdName() {
        assertEquals("SHA-1", PSSParameterSpec.DEFAULT.getDigestAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies the name of default mask generation function algorithm.",
        method = "getMGFAlgorithm",
        args = {}
    )
    public final void testDEFAULTmgfName() {
        assertEquals("MGF1", PSSParameterSpec.DEFAULT.getMGFAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies default algorithm parameters for mask generation function.",
        method = "getMGFParameters",
        args = {}
    )
    public final void testDEFAULTmgfSpec() {
        assertTrue(MGF1ParameterSpec.SHA1.equals(PSSParameterSpec.DEFAULT.getMGFParameters()));        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getSaltLength",
        args = {}
    )
    public final void testDEFAULTsaltLen() {
        assertEquals(20, PSSParameterSpec.DEFAULT.getSaltLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies default trailer field value.",
        method = "getTrailerField",
        args = {}
    )
    public final void testDEFAULTtrailerField() {
        assertEquals(1, PSSParameterSpec.DEFAULT.getTrailerField());
    }
}
