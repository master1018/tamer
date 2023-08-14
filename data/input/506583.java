@TestTargetClass(CertPathBuilderException.class)
public class CertPathBuilderExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPathBuilderException",
        args = {}
    )
    public void testCertPathBuilderException01() {
        CertPathBuilderException tE = new CertPathBuilderException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CertPathBuilderException",
        args = {java.lang.String.class}
    )
    public void testCertPathBuilderException02() {
        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CertPathBuilderException",
        args = {java.lang.String.class}
    )
    public void testCertPathBuilderException03() {
        String msg = null;
        CertPathBuilderException tE = new CertPathBuilderException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CertPathBuilderException",
        args = {java.lang.Throwable.class}
    )
    public void testCertPathBuilderException04() {
        Throwable cause = null;
        CertPathBuilderException tE = new CertPathBuilderException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CertPathBuilderException",
        args = {java.lang.Throwable.class}
    )
    public void testCertPathBuilderException05() {
        CertPathBuilderException tE = new CertPathBuilderException(tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() should contain ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as parameters.",
        method = "CertPathBuilderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathBuilderException06() {
        CertPathBuilderException tE = new CertPathBuilderException(null, null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as the second parameter.",
        method = "CertPathBuilderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathBuilderException07() {
        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as the first parameter.",
        method = "CertPathBuilderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathBuilderException08() {
        CertPathBuilderException tE = new CertPathBuilderException(null, tCause);
        if (tE.getMessage() != null) {
            String toS = tCause.toString();
            String getM = tE.getMessage();
            assertTrue("getMessage() must should ".concat(toS), (getM
                    .indexOf(toS) != -1));
        }
        assertNotNull("getCause() must not return null", tE.getCause());
        assertEquals("getCause() must return ".concat(tCause.toString()), tE
                .getCause(), tCause);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "CertPathBuilderException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathBuilderException09() {
        CertPathBuilderException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathBuilderException(msgs[i], tCause);
            String getM = tE.getMessage();
            String toS = tCause.toString();
            if (msgs[i].length() > 0) {
                assertTrue("getMessage() must contain ".concat(msgs[i]), getM
                        .indexOf(msgs[i]) != -1);
                if (!getM.equals(msgs[i])) {
                    assertTrue("getMessage() should contain ".concat(toS), getM
                            .indexOf(toS) != -1);
                }
            }
            assertNotNull("getCause() must not return null", tE.getCause());
            assertEquals("getCause() must return ".concat(tCause.toString()),
                    tE.getCause(), tCause);
        }
    }
}
