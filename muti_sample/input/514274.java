@TestTargetClass(MGF1ParameterSpec.class)
public class MGF1ParameterSpecTest extends TestCase {
    private static final String testAlgName = "TEST";
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "MGF1ParameterSpec",
        args = {java.lang.String.class}
    )
    public final void testMGF1ParameterSpec01() {
        try {
            MGF1ParameterSpec pgf = new MGF1ParameterSpec(testAlgName);
            assertNotNull(pgf);
            assertTrue(pgf instanceof MGF1ParameterSpec);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "MGF1ParameterSpec",
        args = {java.lang.String.class}
    )
    public final void testMGF1ParameterSpec02() {
        try {
            new MGF1ParameterSpec(null);
            fail("NullPointerException has not been thrown");
        } catch (NullPointerException ok) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDigestAlgorithm",
        args = {}
    )
    public final void testGetDigestAlgorithm() {
        MGF1ParameterSpec aps = new MGF1ParameterSpec(testAlgName);
        assertTrue(testAlgName.equals(aps.getDigestAlgorithm()));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Field testing",
            method = "!field SHA1",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Field testing",
            method = "!field SHA256",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Field testing",
            method = "!field SHA384",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Field testing",
            method = "!field SHA512",
            args = {}
        )
    })
    public final void testFieldsGetDigestAlgorithm() {
        assertEquals("SHA-1", MGF1ParameterSpec.SHA1.getDigestAlgorithm());
        assertEquals("SHA-256", MGF1ParameterSpec.SHA256.getDigestAlgorithm());
        assertEquals("SHA-384", MGF1ParameterSpec.SHA384.getDigestAlgorithm());
        assertEquals("SHA-512", MGF1ParameterSpec.SHA512.getDigestAlgorithm());
    }
}
