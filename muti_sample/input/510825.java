@TestTargetClass(CodeSource.class)
public class CodeSourceTest extends TestCase {
    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(CodeSourceTest.class);
    }
    private java.security.cert.Certificate[] chain = null;
    private static URL urlSite;
    private static URL urlDir; 
    private static URL urlDirOtherSite; 
    private static URL urlDir_port80, urlDir_port81;
    private static URL urlDirWithSlash;
    private static URL urlDir_FileProtocol;
    private static URL urlDirIP;
    private static URL urlFile, urlFileWithAdditionalDirs, urlFileDirOtherDir;
    private static URL urlFileDirMinus;
    private static URL urlFileDirStar;
    private static URL urlRef1, urlRef2;
    private boolean init = false;
    private void init() {
        if (!init) {
            try {
                String siteName = "www.intel.com";
                InetAddress addr = InetAddress.getByName(siteName);
                String siteIP = addr.getHostAddress();
                urlSite = new URL("http:
                urlDir = new URL("http:
                urlDirOtherSite = new URL("http:
                urlDir_port80 = new URL("http:
                urlDir_port81 = new URL("http:
                urlDirWithSlash = new URL(urlDir + "/");
                urlDir_FileProtocol = new URL("file:
                urlDirIP = new URL("http:
                urlFile = new URL("http:
                urlFileWithAdditionalDirs = new URL(
                        "http:
                urlFileDirMinus = new URL("http:
                urlFileDirStar = new URL("http:
                urlFileDirOtherDir = new URL("http:
                urlRef1 = new URL("http:
                urlRef2 = new URL("http:
            } catch (MalformedURLException ex) {
                throw new Error(ex);
            } catch (UnknownHostException ex) {
                throw new Error(ex);
            } finally {
                init = true;
            }
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
        init();
        chain = TestCertUtils.getCertChain();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void testHashCode() {
        assertTrue(new CodeSource(null, (Certificate[]) null).hashCode() == 0);
        assertTrue(new CodeSource(urlSite, (Certificate[]) null).hashCode() == urlSite
                .hashCode());
        assertTrue(new CodeSource(urlSite, chain).hashCode() == urlSite
                .hashCode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CodeSource",
        args = {java.net.URL.class, java.security.cert.Certificate[].class}
    )
    public void testCodeSourceURLCertificateArray() {
        new CodeSource(null, (Certificate[]) null);
        new CodeSource(urlSite, (Certificate[]) null);
        new CodeSource(null, chain);
        new CodeSource(urlSite, chain);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies method with null parameters only",
        method = "CodeSource",
        args = {java.net.URL.class, java.security.CodeSigner[].class}
    )
    public void testCodeSourceURLCodeSignerArray() {
        if (!has_15_features()) {
            return;
        }
        new CodeSource(null, (CodeSigner[]) null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Null parameter checked",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsObject_00() {
        CodeSource thiz = new CodeSource(urlSite, (Certificate[]) null);
        assertFalse(thiz.equals(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Same objects checked",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsObject_01() {
        CodeSource thiz = new CodeSource(urlSite, (Certificate[]) null);
        assertTrue(thiz.equals(thiz));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsObject_02() {
        Certificate cert0 = new TestCertUtils.TestCertificate();
        Certificate cert1 = new TestCertUtils.TestCertificate();
        Certificate[] certs0 = new Certificate[] { cert0, cert1 };
        Certificate[] certs1 = new Certificate[] { cert1, cert0 };
        CodeSource thiz = new CodeSource(urlSite, certs0);
        CodeSource that = new CodeSource(urlSite, certs1);
        assertTrue(thiz.equals(that));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void testEqualsObject_04() {
        CodeSource thiz = new CodeSource(urlSite, (Certificate[]) null);
        CodeSource that = new CodeSource(null, (Certificate[]) null);
        assertFalse(thiz.equals(that));
        assertFalse(that.equals(thiz));
        that = new CodeSource(urlFile, (Certificate[]) null);
        assertFalse(thiz.equals(that));
        assertFalse(that.equals(thiz));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getCertificates",
        args = {}
    )
    public void testGetCertificates_00() {
        assertNull(new CodeSource(null, (Certificate[]) null).getCertificates());
        java.security.cert.Certificate[] got = new CodeSource(null, chain)
                .getCertificates();
        assertNotSame(got, chain);
        assertTrue(checkEqual(got, chain));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getCertificates",
        args = {}
    )
    public void testGetCertificates_01() {
        if (!has_15_features()) {
            return;
        }
        CertPath cpath = TestCertUtils.getCertPath();
        Certificate[] certs = (Certificate[]) cpath.getCertificates().toArray();
        CodeSigner[] signers = { new CodeSigner(cpath, null) };
        CodeSource cs = new CodeSource(null, signers);
        Certificate[] got = cs.getCertificates();
        assertTrue(presented(certs, got));
        assertTrue(presented(got, certs));
    }
    private static boolean checkEqual(java.security.cert.Certificate[] one,
            java.security.cert.Certificate[] two) {
        if (one == null) {
            return two == null;
        }
        if (two == null) {
            return false;
        }
        if (one.length != two.length) {
            return false;
        }
        for (int i = 0; i < one.length; i++) {
            if (one[i] == null) {
                if (two[i] != null) {
                    return false;
                }
            } else {
                if (!one[i].equals(two[i])) {
                    return false;
                }
            }
        }
        return true;
    }
    private static boolean presented(Certificate[] what, Certificate[] where) {
        boolean whereHasNull = false;
        for (int i = 0; i < what.length; i++) {
            if (what[i] == null) {
                if (whereHasNull) {
                    continue;
                }
                for (int j = 0; j < where.length; j++) {
                    if (where[j] == null) {
                        whereHasNull = true;
                        break;
                    }
                }
                if (!whereHasNull) {
                    return false;
                }
            } else {
                boolean found = false;
                for (int j = 0; j < where.length; j++) {
                    if (what[i].equals(where[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getCodeSigners",
        args = {}
    )
    public void testGetCodeSigners_00() {
        if (!has_15_features()) {
            return;
        }
        CodeSigner[] signers = { new CodeSigner(TestCertUtils.getCertPath(),
                null) };
        CodeSource cs = new CodeSource(null, signers);
        CodeSigner[] got = cs.getCodeSigners();
        assertNotNull(got);
        assertTrue(signers.length == got.length);
        for (int i = 0; i < signers.length; i++) {
            CodeSigner s = signers[i];
            boolean found = false;
            for (int j = 0; j < got.length; j++) {
                if (got[j] == s) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getCodeSigners",
        args = {}
    )
    public void testGetCoderSignersNull() throws Exception{
        assertNull(new CodeSource(new URL("http:
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocation",
        args = {}
    )
    public void testGetLocation() {
        assertTrue(new CodeSource(urlSite, (Certificate[]) null).getLocation() == urlSite);
        assertTrue(new CodeSource(urlSite, chain).getLocation() == urlSite);
        assertNull(new CodeSource(null, (Certificate[]) null).getLocation());
        assertNull(new CodeSource(null, chain).getLocation());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        new CodeSource(urlSite, chain).toString();
        new CodeSource(null, chain).toString();
        new CodeSource(null, (Certificate[]) null).toString();
    }
    private static boolean has_15_features() {
        Class klass = CodeSource.class;
        Class[] ctorArgs = { URL.class, new CodeSigner[] {}.getClass() };
        try {
            klass.getConstructor(ctorArgs);
        } catch (NoSuchMethodException ex) {
            return false;
        }
        return true;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_00() {
        CodeSource cs0 = new CodeSource(null, (Certificate[]) null);
        assertFalse(cs0.implies(null));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_01() throws Exception {
        CodeSource thizCS = new CodeSource(urlSite, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(null, (Certificate[]) null);
        assertTrue(thatCS.implies(thizCS));
        assertTrue(thatCS.implies(thatCS));
        assertFalse(thizCS.implies(thatCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_02() throws Exception {
        CodeSource thizCS = new CodeSource(urlSite, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(thizCS.getLocation(),
                (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        assertTrue(thatCS.implies(thizCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_03_tmp() throws Exception {
        CodeSource thizCS = new CodeSource(urlDir, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(urlDir_FileProtocol,
                (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
        assertFalse(thatCS.implies(thizCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_04() throws Exception {
        CodeSource thizCS = new CodeSource(urlDir, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(urlDirIP, (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        assertTrue(thatCS.implies(thizCS));
        thatCS = new CodeSource(urlDirOtherSite, (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
        thizCS = new CodeSource(new URL("http", null, "file1"),
                (Certificate[]) null);
        thatCS = new CodeSource(new URL("http", "another.host.com", "file1"),
                (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        assertFalse(thatCS.implies(thizCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_05() throws Exception {
        CodeSource thizCS = new CodeSource(urlDir_port80, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(urlDir, (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        assertTrue(thatCS.implies(thizCS));
        thizCS = new CodeSource(urlDir, (Certificate[]) null);
        thatCS = new CodeSource(urlDir_port81, (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        thizCS = new CodeSource(urlDir_port81, (Certificate[]) null);
        thatCS = new CodeSource(urlDir, (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
        thizCS = new CodeSource(urlDir_port80, (Certificate[]) null);
        thatCS = new CodeSource(urlDir_port81, (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_06() throws Exception {
        CodeSource thizCS = new CodeSource(urlFile, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(urlFile, (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_07() throws Exception {
        CodeSource thiz = new CodeSource(urlFileDirMinus, (Certificate[]) null);
        CodeSource that = new CodeSource(urlFile, (Certificate[]) null);
        assertTrue(thiz.implies(that));
        that = new CodeSource(urlFileWithAdditionalDirs, (Certificate[]) null);
        assertTrue(thiz.implies(that));
        that = new CodeSource(urlFileDirOtherDir, (Certificate[]) null);
        assertFalse(thiz.implies(that));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_08() throws Exception {
        CodeSource thiz = new CodeSource(urlFileDirStar, (Certificate[]) null);
        CodeSource that = new CodeSource(urlFile, (Certificate[]) null);
        assertTrue(thiz.implies(that));
        that = new CodeSource(urlFileWithAdditionalDirs, (Certificate[]) null);
        assertFalse(thiz.implies(that));
        that = new CodeSource(urlFileDirOtherDir, (Certificate[]) null);
        assertFalse(thiz.implies(that));
        that = new CodeSource(new URL(urlFile.toString() + "/"),
                (Certificate[]) null);
        assertFalse(thiz.implies(that));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_09() throws Exception {
        CodeSource thizCS = new CodeSource(urlDir, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(urlDirWithSlash,
                (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        assertFalse(thatCS.implies(thizCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_0A() throws Exception {
        CodeSource thizCS = new CodeSource(urlRef1, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(urlRef1, (Certificate[]) null);
        assertTrue(thizCS.implies(thatCS));
        thizCS = new CodeSource(urlRef1, (Certificate[]) null);
        thatCS = new CodeSource(urlRef2, (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_0B() {
        Certificate c0 = new TestCertUtils.TestCertificate("00");
        Certificate c1 = new TestCertUtils.TestCertificate("01");
        Certificate c2 = new TestCertUtils.TestCertificate("02");
        Certificate[] thizCerts = { c0, c1 };
        Certificate[] thatCerts = { c1, c0, c2 };
        CodeSource thiz = new CodeSource(urlSite, thizCerts);
        CodeSource that = new CodeSource(urlSite, thatCerts);
        assertTrue(thiz.implies(that));
        that = new CodeSource(urlSite, (Certificate[]) null);
        assertFalse(thiz.implies(that));
        assertTrue(that.implies(thiz));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_0C() throws Exception {
        URL url0 = new URL("http:
        URL url1 = new URL("http:
        CodeSource thizCS = new CodeSource(url0, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(url1, (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
        assertFalse(thatCS.implies(thizCS));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "implies",
        args = {java.security.CodeSource.class}
    )
    public void testImplies_0D() throws Exception {
        URL url0 = new URL("file:
                + File.separator + "someDir");
        URL url1 = new URL("file:
                + File.separator + "someOtherDir");
        CodeSource thizCS = new CodeSource(url0, (Certificate[]) null);
        CodeSource thatCS = new CodeSource(url1, (Certificate[]) null);
        assertFalse(thizCS.implies(thatCS));
        assertFalse(thatCS.implies(thizCS));
    }
}
