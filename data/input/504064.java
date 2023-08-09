@TestTargetClass(CertPathValidatorException.class)
public class CertPathValidatorExceptionTest extends TestCase {
    private static String[] msgs = {
            "",
            "Check new message",
            "Check new message Check new message Check new message Check new message Check new message" };
    private static Throwable tCause = new Throwable("Throwable for exception");
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPathValidatorException",
        args = {}
    )
    public void testCertPathValidatorException01() {
        CertPathValidatorException tE = new CertPathValidatorException();
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CertPathValidatorException",
        args = {java.lang.String.class}
    )
    public void testCertPathValidatorException02() {
        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i]);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class}
    )
    public void testCertPathValidatorException03() {
        String msg = null;
        CertPathValidatorException tE = new CertPathValidatorException(msg);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "CertPathValidatorException",
        args = {java.lang.Throwable.class}
    )
    public void testCertPathValidatorException04() {
        Throwable cause = null;
        CertPathValidatorException tE = new CertPathValidatorException(cause);
        assertNull("getMessage() must return null.", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CertPathValidatorException",
        args = {java.lang.Throwable.class}
    )
    public void testCertPathValidatorException05() {
        CertPathValidatorException tE = new CertPathValidatorException(tCause);
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
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathValidatorException06() {
        CertPathValidatorException tE = new CertPathValidatorException(null,
                null);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as the second parameter.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathValidatorException07() {
        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i], null);
            assertEquals("getMessage() must return: ".concat(msgs[i]), tE
                    .getMessage(), msgs[i]);
            assertNull("getCause() must return null", tE.getCause());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as the first parameter.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathValidatorException08() {
        CertPathValidatorException tE = new CertPathValidatorException(null,
                tCause);
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
        notes = "",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class}
    )
    public void testCertPathValidatorException09() {
        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i], tCause);
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as parameters.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class, java.security.cert.CertPath.class, int.class}
    )
    public void testCertPathValidatorException10() {
        CertPathValidatorException tE = new CertPathValidatorException(null,
                null, null, -1);
        assertNull("getMessage() must return null", tE.getMessage());
        assertNull("getCause() must return null", tE.getCause());
        assertNull("getCertPath() must return null", tE.getCertPath());
        assertEquals("getIndex() must be -1", tE.getIndex(), -1);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that IllegalArgumentException is thrown.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class, java.security.cert.CertPath.class, int.class}
    )
    public void testCertPathValidatorException11() {
        int[] indx = { 0, 1, 100, Integer.MAX_VALUE, Integer.MIN_VALUE };
        for (int j = 0; j < indx.length; j++) {
            for (int i = 0; i < msgs.length; i++) {
                try {
                    new CertPathValidatorException(msgs[i], tCause, null, indx[j]);
                    fail("Error. IllegalArgumentException was not thrown as expected. "
                            + " msg: "
                            + msgs[i]
                            + ", certPath is null and index is " + indx[j]);
                } catch (IllegalArgumentException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class, java.security.cert.CertPath.class, int.class}
    )
    public void testCertPathValidatorException12() {
        CertPathValidatorException tE;
        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, null, -1);
                String getM = tE.getMessage();
                String toS = tCause.toString();
                if (msgs[i].length() > 0) {
                    assertTrue("getMessage() must contain ".concat(msgs[i]),
                            getM.indexOf(msgs[i]) != -1);
                    if (!getM.equals(msgs[i])) {
                        assertTrue("getMessage() should contain ".concat(toS),
                                getM.indexOf(toS) != -1);
                    }
                }
                assertNotNull("getCause() must not return null", tE.getCause());
                assertEquals("getCause() must return "
                        .concat(tCause.toString()), tE.getCause(), tCause);
                assertNull("getCertPath() must return null", tE.getCertPath());
                assertEquals("getIndex() must return -1", tE.getIndex(), -1);
            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected exception: " + e.toString()
                        + " Parameters: msg: " + msgs[i]
                        + ", certPath is null and index is -1");
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class, java.security.cert.CertPath.class, int.class}
    )
    public void testCertPathValidatorException13() {
        myCertPath mcp = new myCertPath("X.509", "");
        CertPath cp = mcp.get("X.509");
        int[] indx = { -2, -100, 0, 1, 100, Integer.MAX_VALUE,
                Integer.MIN_VALUE };
        for (int j = 0; j < indx.length; j++) {
            for (int i = 0; i < msgs.length; i++) {
                try {
                    new CertPathValidatorException(msgs[i], tCause, cp, indx[j]);
                    fail("IndexOutOfBoundsException was not thrown as expected. "
                            + " msg: "
                            + msgs[i]
                            + ", certPath is null and index is " + indx[j]);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IndexOutOfBoundsException.",
        method = "CertPathValidatorException",
        args = {java.lang.String.class, java.lang.Throwable.class, java.security.cert.CertPath.class, int.class}
    )
    public void testCertPathValidatorException14() {
        CertPathValidatorException tE;
        myCertPath mcp = new myCertPath("X.509", "");
        CertPath cp = mcp.get("X.509");
        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, cp, -1);
                String getM = tE.getMessage();
                String toS = tCause.toString();
                if (msgs[i].length() > 0) {
                    assertTrue("getMessage() must contain ".concat(msgs[i]),
                            getM.indexOf(msgs[i]) != -1);
                    if (!getM.equals(msgs[i])) {
                        assertTrue("getMessage() should contain ".concat(toS),
                                getM.indexOf(toS) != -1);
                    }
                }
                assertNotNull("getCause() must not return null", tE.getCause());
                assertEquals("getCause() must return "
                        .concat(tCause.toString()), tE.getCause(), tCause);
                assertNotNull("getCertPath() must not return null", tE
                        .getCertPath());
                assertEquals(
                        "getCertPath() must return ".concat(cp.toString()), tE
                                .getCertPath(), cp);
                assertEquals("getIndex() must return -1", tE.getIndex(), -1);
            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected IndexOutOfBoundsException was thrown. "
                        + e.toString());
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies that getCertPath method returns the certification paththat was being validated when the exception was thrown.",
        method = "getCertPath",
        args = {}
    )
    public void testCertPathValidatorException15() {
        CertPathValidatorException tE = new CertPathValidatorException();
        assertNull("getCertPath() must return null.", tE.getCertPath());
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i]);
            assertNull("getCertPath() must return null ", tE.getCertPath());
        }
        Throwable cause = null;
        tE = new CertPathValidatorException(cause);
        assertNull("getCertPath() must return null.", tE.getCertPath());
        tE = new CertPathValidatorException(tCause);
        assertNull("getCertPath() must return null.", tE.getCertPath());
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i], tCause);
            assertNull("getCertPath() must return null", tE.getCertPath());
        }
        tE = new CertPathValidatorException(null, null, null, -1);
        assertNull("getCertPath() must return null", tE.getCertPath());
        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, null, -1);
                assertNull("getCertPath() must return null", tE.getCertPath());
            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected exception: " + e.getMessage());
            }
        }
        myCertPath mcp = new myCertPath("X.509", "");
        CertPath cp = mcp.get("X.509");
        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, cp, -1);
                assertNotNull("getCertPath() must not return null", tE
                        .getCertPath());
                assertEquals(
                        "getCertPath() must return ".concat(cp.toString()), tE
                                .getCertPath(), cp);
            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected IndexOutOfBoundsException was thrown. "
                        + e.toString());
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIndex",
        args = {}
    )
    public void testCertPathValidatorException16() {
        CertPathValidatorException tE = new CertPathValidatorException();
        assertEquals("getIndex() must be equals -1", -1, tE.getIndex());
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i]);
            assertEquals("getIndex() must be equals -1", -1, tE.getIndex());
        }
        Throwable cause = null;
        tE = new CertPathValidatorException(cause);
        assertEquals("getIndex() must be equals -1", -1, tE.getIndex());
        tE = new CertPathValidatorException(tCause);
        assertEquals("getIndex() must be equals -1", -1, tE.getIndex());
        for (int i = 0; i < msgs.length; i++) {
            tE = new CertPathValidatorException(msgs[i], tCause);
            assertEquals("getIndex() must be equals -1", -1, tE.getIndex());
        }
        tE = new CertPathValidatorException(null, null, null, -1);
        assertEquals("getIndex() must be equals -1", -1, tE.getIndex());
        myCertPath mcp = new myCertPath("X.509", "");
        CertPath cp = mcp.get("X.509");
        for (int i = 0; i < msgs.length; i++) {
            try {
                tE = new CertPathValidatorException(msgs[i], tCause, cp, -1);
                assertNotNull("getIndex() must not return null", tE
                        .getCertPath());
                assertEquals(
                        "getIndex() must return ".concat(cp.toString()), tE
                                .getCertPath(), cp);
            } catch (IndexOutOfBoundsException e) {
                fail("Unexpected IndexOutOfBoundsException was thrown. "
                        + e.getMessage());
            }
        }
    }
    class myCertPath extends CertPath {
        private static final long serialVersionUID = 5871603047244722511L;
        public List<Certificate> getCertificates() {
            return new Vector<Certificate>();
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public byte[] getEncoded(String s) {
            return new byte[0];
        }
        public Iterator<String> getEncodings() {
            return (Iterator<String>) (new StringTokenizer("ss ss ss ss"));
        }
        protected myCertPath(String s) {
            super(s);
        }
        public CertPath get(String s) {
            return new myCertPath(s);
        }
        public myCertPath(String s, String s1) {
            super(s);
        }
    }
}
