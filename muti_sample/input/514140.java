@TestTargetClass(CodeSigner.class)
public class CodeSignerTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(CodeSignerTest.class);
    }
    private CertPath cpath = TestCertUtils.genCertPath(3, 0);
    private Date now = new Date();
    private Timestamp ts = new Timestamp(now, cpath);
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "NPE case",
        method = "CodeSigner",
        args = {java.security.cert.CertPath.class, java.security.Timestamp.class}
    )
    public void testCodeSigner_00() {
        try {
            new CodeSigner(null, ts);
            fail("must not accept null");
        } catch (NullPointerException ex) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Null parameter timestamp checking",
        method = "CodeSigner",
        args = {java.security.cert.CertPath.class, java.security.Timestamp.class}
    )
    public final void testCodeSigner_01() {
        try {
            CodeSigner cs = new CodeSigner(cpath, null);
            assertNotNull(cs);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "CodeSigner",
        args = {java.security.cert.CertPath.class, java.security.Timestamp.class}
    )
    public final void testCodeSigner_02() {
        try {
            CodeSigner cs = new CodeSigner(cpath, ts);
            assertNotNull(cs);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public final void testEqualsObject() {
        CodeSigner one = new CodeSigner(cpath, ts);
        CodeSigner two = new CodeSigner(cpath, ts);
        CodeSigner three = new CodeSigner(cpath, null);
        CertPath cpath2 = TestCertUtils.genCertPath(5, 3);
        CodeSigner four = new CodeSigner(cpath2, null);
        assertTrue(one.equals(one));
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
        assertFalse(one.equals(three));
        assertFalse(three.equals(one));
        assertTrue(three.equals(three));
        assertFalse( three.equals(four));
        assertFalse( one.equals(null) );
        assertFalse( one.equals(new Object()) );
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSignerCertPath",
        args = {}
    )
    public void testGetSignerCertPath() {
        assertSame(new CodeSigner(cpath, null).getSignerCertPath(), cpath);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTimestamp",
        args = {}
    )
    public void testGetTimestamp() {
        assertNull(new CodeSigner(cpath, null).getTimestamp());
        assertSame(new CodeSigner(cpath, ts).getTimestamp(), ts);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        assertTrue(new CodeSigner(cpath, null).toString().contains(""));
        assertTrue(new CodeSigner(cpath, ts).toString().contains(""));
        assertTrue(new CodeSigner(cpath, null).toString().contains("Signer"));
        assertTrue(new CodeSigner(cpath, ts).toString().contains(ts.toString()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        CodeSigner cs1 = new CodeSigner(cpath, ts);
        CodeSigner cs2 = new CodeSigner(cpath, ts);
        CodeSigner cs3 = new CodeSigner(cpath, null);
        assertTrue(cs1.hashCode() == cs2.hashCode());
        assertTrue(cs2.hashCode() != cs3.hashCode());
    }
}
