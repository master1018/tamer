@TestTargetClass(CodeSource.class)
public class CodeSource2Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies constructor with valid URL and null certificate array",
        method = "CodeSource",
        args = {java.net.URL.class, java.security.cert.Certificate[].class}
    )
    public void test_ConstructorLjava_net_URL$Ljava_security_cert_Certificate()
            throws Exception {
        new CodeSource(new java.net.URL("file:
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CodeSource",
        args = {java.net.URL.class, java.security.CodeSigner[].class}
    )
    public void test_ConstructorLjava_net_URL$Ljava_security_CodeSigner() {
        try {
            new CodeSource(new URL("file:
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
        try {
            new CodeSource(null, (CodeSigner[]) null);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
        CertPath cpath = TestCertUtils.genCertPath(3, 0);
        Date now = new Date();
        Timestamp ts = new Timestamp(now, cpath);
        CodeSigner cs = new CodeSigner(cpath, ts);
        try {
            CodeSource codeSource = new CodeSource(new URL("file:
            assertNotNull(codeSource.getCertificates());
            assertNotNull(codeSource.getCodeSigners());
            assertTrue(Arrays.equals(new CodeSigner[] { cs }, codeSource.getCodeSigners()));
            assertEquals(new URL("file:
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "CodeSource object was created with CodeSource(URL url, Certificate[] certs) only",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() throws Exception {
        CodeSource cs1 = new CodeSource(new java.net.URL("file:
                (Certificate[]) null);
        CodeSource cs2 = new CodeSource(new java.net.URL("file:
                (Certificate[]) null);
        assertTrue("Identical objects were not equal()!", cs1.equals(cs2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() throws Exception {
        URL url = new java.net.URL("file:
        CodeSource cs = new CodeSource(url, (Certificate[]) null);
        assertEquals("Did not get expected hashCode!", cs.hashCode(), url
                .hashCode());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies case for null certificates only",
        method = "getCertificates",
        args = {}
    )
    public void test_getCertificates() throws Exception {
        CodeSource cs = new CodeSource(new java.net.URL("file:
                (Certificate[]) null);
        assertNull("Should have gotten null certificate list.", cs
                .getCertificates());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocation",
        args = {}
    )
    public void test_getLocation() throws Exception {
        CodeSource cs = new CodeSource(new java.net.URL("file:
                (Certificate[]) null);
        assertEquals("Did not get expected location!", "file:/test", cs
                .getLocation().toString());
        assertNotNull("Host should not be null", cs.getLocation().getHost());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void test_impliesLjava_security_CodeSource() throws Exception {
        CodeSource cs1 = new CodeSource(new URL("file:/d:/somedir"),
                (Certificate[]) null);
        CodeSource cs2 = new CodeSource(new URL("file:/d:/somedir/"),
                (Certificate[]) null);
        assertTrue("Does not add /", cs1.implies(cs2));
        cs1 = new CodeSource(new URL("file", null, -1, "/d:/somedir/"),
                (Certificate[]) null);
        cs2 = new CodeSource(new URL("file:/d:/somedir/"), (Certificate[]) null);
        assertTrue("null host should imply host", cs1.implies(cs2));
        assertFalse("host should not imply null host", cs2.implies(cs1));
	}
}
