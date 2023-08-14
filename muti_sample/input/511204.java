@TestTargetClass(PKIXCertPathBuilderResult.class)
public class PKIXCertPathBuilderResultTest extends TestCase {
    private static final byte[] testEncoding = new byte[] {
            (byte)1, (byte)2, (byte)3, (byte)4, (byte)5
    };
    private static PublicKey testPublicKey = new PublicKey() {
        private static final long serialVersionUID = -5529950703394751638L;
        public String getAlgorithm() {
            return "NeverMind";
        }
        public String getFormat() {
            return "NeverMind";
        }
        public byte[] getEncoded() {
            return new byte[] {};
        }
    };
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "PKIXCertPathBuilderResult",
        args = {java.security.cert.CertPath.class, java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathBuilderResult01()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
        assertTrue(r instanceof PKIXCertPathBuilderResult);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "PKIXCertPathBuilderResult",
        args = {java.security.cert.CertPath.class, java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathBuilderResult02()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    null,
                    testPublicKey);
        assertTrue(r instanceof PKIXCertPathBuilderResult);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "PKIXCertPathBuilderResult",
        args = {java.security.cert.CertPath.class, java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathBuilderResult03() {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        try {
            new PKIXCertPathBuilderResult(
                    null,
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "PKIXCertPathBuilderResult",
        args = {java.security.cert.CertPath.class, java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathBuilderResult04() {
        try {
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    null,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "PKIXCertPathBuilderResult",
        args = {java.security.cert.CertPath.class, java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathBuilderResult05() {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        try {
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    TestUtils.getPolicyTree(),
                    null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void test_clone() {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        assertNotNull(getName()
                + ": not performed (could not create test TrustAnchor)", ta);
        PKIXCertPathBuilderResult init = new PKIXCertPathBuilderResult(
                new MyCertPath(testEncoding), ta, TestUtils.getPolicyTree(),
                testPublicKey);
        PKIXCertPathBuilderResult clone = (PKIXCertPathBuilderResult) init
                .clone();
        assertSame(init.getCertPath(), clone.getCertPath());
        assertSame(init.getPolicyTree(), clone.getPolicyTree());
        assertSame(init.getPublicKey(), clone.getPublicKey());
        assertSame(init.getTrustAnchor(), clone.getTrustAnchor());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertPath",
        args = {}
    )
    public final void testGetCertPath() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPath cp = new MyCertPath(testEncoding);
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    cp,
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
        assertSame(cp, r.getCertPath());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        CertPathBuilderResult r =
            new PKIXCertPathBuilderResult(
                    new MyCertPath(testEncoding),
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
        assertNotNull(r.toString());
    }
}
